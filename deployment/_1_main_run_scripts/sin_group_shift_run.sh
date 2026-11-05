#!/bin/bash
# 隐私参数与数据分布相关实验（sin）
#
# 主实验默认：高隐私组 epsilon=0.5, w=40
# 低隐私组：epsilon=2.5, w=40
# 在同一个时间戳 t 上，两组主状态概率分别为 p(t)+delta 和 p(t)-delta。
# delta=0 是同一组隐私参数分配下的独立分布基线；positive/reverse 用来检查相关方向。
# 五个取值与论文中其他自变量的实验组织保持一致。
# 如需压力测试，可设置 GROUP_SHIFT_HIGH_WINDOW_SIZE=200；这不会修改 XML。
# 快速检查时可用环境变量覆盖，例如 GROUP_SHIFT_REPEAT_ROUND=1。
shift_delta_list=${GROUP_SHIFT_DELTA_LIST:-"0 0.05 0.10 0.15 0.20"}
shift_direction_list=${GROUP_SHIFT_DIRECTION_LIST:-"positive reverse"}
repeat_round=${GROUP_SHIFT_REPEAT_ROUND:-10}
dataset_dir_name="Sin"
main_java_file_name="Sin"
final_output_dir_name="4.sin_result"
basic_path="../0.dataset/${dataset_dir_name}"
# 快速试跑时可指定另一个结果根目录，避免与正式实验的结果目录重名。
result_output_dir_name=${GROUP_SHIFT_RESULT_DIR_NAME:-"3.group_shift_ldp_result"}
# 正式实验留空，使用 parameter_config.xml 中的 10000 用户和 10000 时刻。
# 快速检查可设置为较小的正整数，例如 100 和 20。
user_size=${GROUP_SHIFT_USER_SIZE:-}
timestamp_size=${GROUP_SHIFT_TIMESTAMP_SIZE:-}
# 可选的组参数覆盖。默认从 parameter_config.xml 读取；压力测试时可设置
# GROUP_SHIFT_HIGH_WINDOW_SIZE=200 单独恢复旧的极端配置。
group_shift_high_window_option=()
group_shift_low_window_option=()
group_shift_high_epsilon_option=()
group_shift_low_epsilon_option=()
if [[ -n "${GROUP_SHIFT_HIGH_WINDOW_SIZE:-}" ]]; then
  group_shift_high_window_option=(-Dexp.shift.high.window=${GROUP_SHIFT_HIGH_WINDOW_SIZE})
fi
if [[ -n "${GROUP_SHIFT_LOW_WINDOW_SIZE:-}" ]]; then
  group_shift_low_window_option=(-Dexp.shift.low.window=${GROUP_SHIFT_LOW_WINDOW_SIZE})
fi
if [[ -n "${GROUP_SHIFT_HIGH_EPSILON:-}" ]]; then
  group_shift_high_epsilon_option=(-Dexp.shift.high.epsilon=${GROUP_SHIFT_HIGH_EPSILON})
fi
if [[ -n "${GROUP_SHIFT_LOW_EPSILON:-}" ]]; then
  group_shift_low_epsilon_option=(-Dexp.shift.low.epsilon=${GROUP_SHIFT_LOW_EPSILON})
fi
# 正式实验默认使用原来的内存配置；快速试跑可通过环境变量降低内存，
# 例如 GROUP_SHIFT_XMS=-Xms512m GROUP_SHIFT_XMX=-Xmx4g。
totalMem=${GROUP_SHIFT_XMS:-"-Xms8g"}
maxMem=${GROUP_SHIFT_XMX:-"-Xmx28g"}
run_mode="$1"

for direction in ${shift_direction_list}; do
  for delta in ${shift_delta_list}; do
    delta_dir_name=${delta//./-}

    # 1. 跑 repeat_round 轮。每一轮都重新生成数据和用户参数，
    #    但同一个 condition 使用相同的随机种子序列。
    for i in $(seq 1 ${repeat_round}); do
      java ${totalMem} ${maxMem} \
        -Dexp.tag=shift \
        -Dexp.shift=${delta} \
        -Dexp.shift.direction=${direction} \
        -Dexp.user.size=${user_size} \
        -Dexp.timestamp.size=${timestamp_size} \
        "${group_shift_high_window_option[@]}" \
        "${group_shift_low_window_option[@]}" \
        "${group_shift_high_epsilon_option[@]}" \
        "${group_shift_low_epsilon_option[@]}" \
        -cp WEventPLDPCode-1.0-SNAPSHOT-jar-with-dependencies.jar \
        hnu.dll.run.d_total_run._5_main_run.${main_java_file_name}MainRun $((i - 1)) ${run_mode}

      mkdir -p ${basic_path}/round_${i}_main_ldp
      mv ${basic_path}/basic_info ${basic_path}/round_${i}_main_ldp &
      mv ${basic_path}/runInput ${basic_path}/round_${i}_main_ldp &
      mv ${basic_path}/group_generated_parameters ${basic_path}/round_${i}_main_ldp &
      mv ${basic_path}/group_output ${basic_path}/round_${i}_main_ldp 2>/dev/null &
      mv ${basic_path}/group_output_time_cost ${basic_path}/round_${i}_main_ldp 2>/dev/null &
      wait
    done

    # 2. 合并当前相关强度和方向的 repeat_round 轮。
    java ${totalMem} ${maxMem} \
      -Dexp.tag=shift \
      -Dexp.shift=${delta} \
      -Dexp.shift.direction=${direction} \
      "${group_shift_high_window_option[@]}" \
      "${group_shift_low_window_option[@]}" \
      "${group_shift_high_epsilon_option[@]}" \
      "${group_shift_low_epsilon_option[@]}" \
      -Dexp.run.mode=${run_mode} \
      -Dexp.repeat.round=${repeat_round} \
      -Dexp.result.dir=../${result_output_dir_name} \
      -cp WEventPLDPCode-1.0-SNAPSHOT-jar-with-dependencies.jar \
      hnu.dll.run.d_total_run._5_main_run.post_process.${main_java_file_name}MainLDPPosterRun

    # 3. 保留每个 condition 的独立结果目录，避免下一个 condition 覆盖。
    mkdir -p ../${result_output_dir_name}
    mv ../${result_output_dir_name}/${final_output_dir_name} \
      ../${result_output_dir_name}/${final_output_dir_name}_${direction}_delta${delta_dir_name}
    cp ${basic_path}/round_1_main_ldp/basic_info/group_shift_info.txt \
      ../${result_output_dir_name}/${final_output_dir_name}_${direction}_delta${delta_dir_name}/group_shift_info.txt

    # 3. 清理当前 condition 的轮次目录。
    rm -rf ${basic_path}/round_*_main_ldp
  done
done
