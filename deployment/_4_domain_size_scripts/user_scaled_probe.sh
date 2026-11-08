#!/bin/bash
# 维度实验「用户数随 d 缩放」验证脚本（Sin 数据集）
#
# 目的：验证「d 变大时把用户数按 d/5 同步放大，能否让方案优劣排序保持和 d=5 一致」。
# 与原 sin_domain_size_run.sh 的实验差异只有一个：
#     每个 d 额外传 -Dexp.user.size = 1200000 * d / 5
#   （使「每组每类样本数」N/(2*w*G*d) 不随 d 变化；其余 ε=1.5、w=40、α=1.2、T 全部不变）
#
# 隔离说明（不影响其他实验的关键）：
#   Java 里 Constant.projectPath = user.dir，且 XML 中 basicPath = "../0.dataset"，
#   因此本脚本先 cd 到自己的 work/ 目录，并在其上级建立 0.dataset/。
#   这样预处理生成的数据、round 目录、结果目录全部落在 user_scaled_probe/ 内，
#   不会触碰共享的 0.dataset、1.main_ldp_result，也不修改任何 Java/XML/原有脚本。
#   config 从 work/config/parameter_config.xml 读取（原 XML 的只读副本）。
#
# 用法：
#   bash user_scaled_probe.sh                      # 最小验证：只跑 d=50，1 轮
#   PROBE_D_LIST="2 5 10 25 50" PROBE_ROUND=10 bash user_scaled_probe.sh   # 全套
#   PROBE_XMX=-Xmx64g bash user_scaled_probe.sh  # 内存不足时调大堆
#   PROBE_TIMESTAMP_SIZE=10000 bash user_scaled_probe.sh  # 与 v8 的 T=10000 严格对齐
#
# 判据：脚本结束会打印各方案相对 Direct 的 ln(MRE) 差，负数表示增强方案更优。
#       d=5 时 PLPA-Plus-Post 约为 -0.17；v8 的 d=50 为 +0.95（反转）。
#       1 轮只用于看方向，正式对比请用 PROBE_ROUND=10。
#
# 时刻数口径：默认 1000，等于当前 parameter_config.xml 的 fixed 值（即"其余参数不动"）。
#   但 v8 结果文件的 BatchSize=10000，若要和论文图做严格数值对照，请显式设
#   PROBE_TIMESTAMP_SIZE=10000；两者不可混用，否则同 v8/v9 一样对不上。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
JAR="${REPO_ROOT}/deployment/WEventPLDPCode-1.0-SNAPSHOT-jar-with-dependencies.jar"
CONFIG_SRC="${REPO_ROOT}/deployment/config/parameter_config.xml"

# ---------------- 可调参数（全部通过环境变量覆盖，无需改脚本） ----------------
PROBE_D_LIST="${PROBE_D_LIST:-50}"                  # 默认只跑 d=50
PROBE_ROUND="${PROBE_ROUND:-1}"                     # 默认 1 轮，仅判方向
PROBE_BASE_USER_SIZE="${PROBE_BASE_USER_SIZE:-1200000}"   # d=5 时的用户数（v8 口径）
PROBE_BASE_D="${PROBE_BASE_D:-5}"
PROBE_XMS="${PROBE_XMS:--Xms16g}"
PROBE_XMX="${PROBE_XMX:--Xmx40g}"
PROBE_TIMESTAMP_SIZE="${PROBE_TIMESTAMP_SIZE:-1000}"   # 当前 XML 的 fixed 值；对齐 v8 请设 10000
PROBE_JAVA_BIN="${PROBE_JAVA_BIN:-java}"

# ---------------- 沙箱目录（全部在脚本自己目录下） ----------------
PROBE_ROOT="${PROBE_ROOT:-${SCRIPT_DIR}/user_scaled_probe}"
WORK_DIR="${PROBE_ROOT}/work"
DATA_ROOT="${PROBE_ROOT}/0.dataset"
DATASET_PATH="${DATA_ROOT}/Sin"
RESULT_DIR_NAME="2.domain_change_ldp_result_user_scaled"
RESULT_ROOT="${PROBE_ROOT}/${RESULT_DIR_NAME}"

for required_file in "${JAR}" "${CONFIG_SRC}"; do
    if [[ ! -f "${required_file}" ]]; then
        echo "[ERROR] 缺少文件：${required_file}" >&2
        exit 1
    fi
done
if (( PROBE_ROUND < 1 || PROBE_ROUND > 10 )); then
    echo "[ERROR] PROBE_ROUND 必须在 1..10（代码里 randomArray 长度为 10）" >&2
    exit 1
fi

mkdir -p "${WORK_DIR}/config" "${DATASET_PATH}" "${RESULT_ROOT}"
# 沙箱内所有产物都不进 git（仓库有定时自动 commit 的脚本）
if [[ ! -f "${PROBE_ROOT}/.gitignore" ]]; then
    printf '*\n' > "${PROBE_ROOT}/.gitignore"
fi
cp -f "${CONFIG_SRC}" "${WORK_DIR}/config/parameter_config.xml"

MAIN_CLASS="hnu.dll.run.d_total_run._5_main_run.SinMainRun"
POSTER_CLASS="hnu.dll.run.d_total_run._5_main_run.post_process.SinMainLDPPosterRun"

report() {
    local result_file="$1" d="$2"
    if [[ ! -f "${result_file}" ]]; then
        echo "  [WARN] 未找到结果文件：${result_file}"
        return
    fi
    awk -F, -v d="${d}" '
        $1=="PLPA-Direct-Scheme"              {a=$9}
        $1=="PLPA-Plus-Post-Scheme"          {b=$9}
        $1=="PLPD-Direct-Scheme"              {c=$9}
        $1=="PLPD-Plus-Post-Scheme"          {e=$9}
        $1=="PLPD-Containing-GAOPS-Scheme"   {g=$9}
        END {
            printf "  d=%s 判据：ln(MRE) 差，负数 = 增强方案更优\n", d
            if (a>0 && b>0) printf "    PLPA-Plus-Post vs PLPA-Direct : %+.3f\n", log(b)-log(a)
            if (c>0 && e>0) printf "    PLPD-Plus-Post vs PLPD-Direct : %+.3f\n", log(e)-log(c)
            if (c>0 && g>0) printf "    PLPD-GAOPS    vs PLPD-Direct : %+.3f\n", log(g)-log(c)
        }' "${result_file}"
}

cd "${WORK_DIR}"

for d in ${PROBE_D_LIST}; do
    if [[ ! "${d}" =~ ^[0-9]+$ ]]; then
        echo "[ERROR] 非法的 d：${d}" >&2
        exit 1
    fi
    user_size=$(( PROBE_BASE_USER_SIZE * d / PROBE_BASE_D ))
    echo "=============================================================="
    echo " d=${d}  user_size=${user_size}  rounds=${PROBE_ROUND}"
    echo " user.dir = ${WORK_DIR}"
    echo "=============================================================="

    for (( i=1; i<=PROBE_ROUND; i++ )); do
        echo "[RUN ] d=${d} round ${i}/${PROBE_ROUND}"
        "${PROBE_JAVA_BIN}" "${PROBE_XMS}" "${PROBE_XMX}" \
            -Dexp.tag=fixed \
            -Dexp.domain="${d}" \
            -Dexp.user.size="${user_size}" \
            -Dexp.timestamp.size="${PROBE_TIMESTAMP_SIZE}" \
            -cp "${JAR}" \
            "${MAIN_CLASS}" "$((i-1))"

        rm -rf "${DATASET_PATH}/round_${i}_main_ldp"
        mkdir -p "${DATASET_PATH}/round_${i}_main_ldp"
        for item in basic_info runInput group_generated_parameters group_output group_output_time_cost; do
            if [[ -e "${DATASET_PATH}/${item}" ]]; then
                mv "${DATASET_PATH}/${item}" "${DATASET_PATH}/round_${i}_main_ldp/"
            fi
        done
    done

    echo "[POST] d=${d} 合并 ${PROBE_ROUND} 轮"
    "${PROBE_JAVA_BIN}" "${PROBE_XMS}" "${PROBE_XMX}" \
        -Dexp.tag=fixed \
        -Dexp.domain="${d}" \
        -Dexp.user.size="${user_size}" \
        -Dexp.repeat.round="${PROBE_ROUND}" \
        -Dexp.result.dir="../${RESULT_DIR_NAME}" \
        -cp "${JAR}" \
        "${POSTER_CLASS}"

    if [[ -d "${RESULT_ROOT}/4.sin_result" ]]; then
        if [[ -d "${RESULT_ROOT}/4.sin_result_d${d}" ]]; then
            rm -rf "${RESULT_ROOT}/4.sin_result_d${d}"
        fi
        mv "${RESULT_ROOT}/4.sin_result" "${RESULT_ROOT}/4.sin_result_d${d}"
    fi

    report "${RESULT_ROOT}/4.sin_result_d${d}/p_1-5_w_40/result.txt" "${d}"
    rm -rf "${DATASET_PATH}"/round_*_main_ldp
done

echo "=============================================================="
echo "[DONE] 结果目录：${RESULT_ROOT}"
echo "       与本实验无关的任何目录均未被写入。"
echo "       若只跑了 1 轮，结论仅看方向；正式对比请加 PROBE_ROUND=10。"
