# -*- coding: utf-8 -*-
"""
config.py
=========
全局配置中心（对应 MATLAB 的 init_params.m 以及各绘图脚本中散落的常量）。

为什么要集中配置：
    MATLAB 版本里，"方法名（图例文字）"、字体大小、线型、输出目录等常量
    分散在 a_1 ~ a_6、b_1 ~ b_4、c_1 ~ c_2、bar_main、bar_ablation 等十几个 .m 文件里，
    一旦论文里改了方法名，就需要逐个文件修改，非常容易漏改。
    本文件把它们全部集中管理，做到"改一处，全局生效"。

最常用的三个修改点：
    1. INPUT_BASIC_PATH / OUTPUT_BASIC_PATH : 输入输出根目录（也可用 run_all.py 的命令行参数覆盖）
    2. METHOD_DISPLAY_NAMES                 : 图例中显示的方法名（改方法名只动这里）
    3. OUTPUT_EXT                           : 输出图片格式（.pdf / .png / .eps / .svg）
"""

import os

# =====================================================================================
# 1. 路径配置
# =====================================================================================
# 实验结果所在根目录，其下应包含 DATASET_DIRS 中列出的 5 个数据集结果目录
# （对应 MATLAB 中 b_*/c_* 函数的 input_basicPath，如 ".../1.main_ldp_result"）
INPUT_BASIC_PATH = r"E:\MainFiles\1.Research\dataset\3-2_stream_ldp\1.main_ldp_result"

# 图片输出根目录，图片会输出到其下的 figures/experiment_result 与 figures/experiment_result_add
# （对应 MATLAB 中 b_*/c_* 函数的 output_basicPath）
OUTPUT_BASIC_PATH = r"E:\MainFiles\1.Research\dataset\3-2_stream_ldp\draw_output"

# =====================================================================================
# 2. 数据集配置（对应 b_*/c_* 中的 dataset_dirs 与 output_dataset_names）
# =====================================================================================
DATASET_DIRS = [
    "1.trajectory_result",
    "2.checkIn_result",
    "3.tlns_result",
    "4.sin_result",
    "5.log_result",
]
# 与 DATASET_DIRS 一一对应，用于输出文件命名
OUTPUT_DATASET_NAMES = [
    "trajectory",
    "check_in",
    "tlns",
    "sin",
    "log",
]

# 输出子目录（对应 MATLAB 中 fullfile(output_basicPath, 'figures', ...)）
OUTPUT_SUB_DIR_FIGURES = "figures"                                            # 图例等公共图片
OUTPUT_SUB_DIR_MAIN = os.path.join("figures", "experiment_result")            # 主实验结果
OUTPUT_SUB_DIR_ADD = os.path.join("figures", "experiment_result_add")         # 消融 / 时间开销等补充结果
OUTPUT_SUB_DIR_TIME = os.path.join("figures", "experiment_result_add", "time_cost_fig")  # 时间开销图
OUTPUT_SUB_DIR_DOMAIN = os.path.join("figures", "experiment_result_add", "domain_size_fig")  # 维度可扩展性图

# =====================================================================================
# 2.1 维度可扩展性实验（取值域大小 d 变化）配置
#
# 由 deployment/_4_domain_size_scripts/*.sh 产出：
#   1) 每个 d 跑完 10 轮并合并后，脚本把结果目录改名为 "{数据集目录}_d{d}"，
#      例如 3.tlns_result -> 3.tlns_result_d5；
#   2) 因为 parameter_config.xml 里 fixed 标签的 PrivacyBudget / WindowSize 的
#      single 与 variance 都是单值，所以每个 d 目录下只会有一个参数目录 p_1-5_w_120。
#
# 因此"随 d 变化"的图与"随 ε / w 变化"的图取数方向正好相反：
#   后者是在【一个】结果目录内扫描多个 p_ε_w 子目录；
#   前者是遍历【多个】结果目录（每个 d 一个），各取其中固定的那个参数目录。
# =====================================================================================
# 维度实验结果的根目录，与主实验的 INPUT_BASIC_PATH 分开
# （对应 deployment/_4_domain_size_scripts/*.sh 里的 result_output_dir_name，
#   由后处理 MainLDPPosterRun 的 -Dexp.result.dir 指定）
DOMAIN_INPUT_BASIC_PATH = r"E:\MainFiles\1.Research\dataset\3-2_stream_ldp\2.domain_change_ldp_result"
# 要绘制的 d 列表，需与脚本中的 domain_size_list 以及
# parameter_config.xml 中 independentVariables/DomainSize 的 fixed 标签一致
DOMAIN_SIZE_LIST = [2, 5, 10, 25, 50]
# 结果目录名模板：数据集目录 + "_d" + d（与脚本里的 mv 保持一致）
DOMAIN_RESULT_DIR_FORMAT = "%s_d%s"
# 只有合成数据集能改取值域大小，两个真实数据集没有这组实验
DOMAIN_DATASET_DIRS = ["3.tlns_result", "4.sin_result", "5.log_result"]
DOMAIN_OUTPUT_DATASET_NAMES = ["tlns", "sin", "log"]
# 画 d 变化图时固定的隐私预算与窗口大小，与 fixed 标签下的 single 值一致
# 注意与 parameter_config.xml 中 WindowSize 的 fixed 标签保持同步（已从 120 改为 40）
DOMAIN_FIXED_BUDGET = 1.5
DOMAIN_FIXED_WINDOW_SIZE = 40

# =====================================================================================
# 2.2 隐私参数与数据分布相关实验（group shift）
#
# 由 deployment/_1_main_run_scripts/sin_group_shift_run.sh 产出：
#   4.sin_result_positive_delta0
#   4.sin_result_positive_delta0-05
#   4.sin_result_positive_delta0-1
#   4.sin_result_positive_delta0-15
#   4.sin_result_positive_delta0-20
#   4.sin_result_reverse_delta0-10
# 每个目录下面只有一个参数目录 p_0-5_w_200。
# =====================================================================================
GROUP_SHIFT_INPUT_BASIC_PATH = r"E:\MainFiles\1.Research\dataset\3-2_stream_ldp\3.group_shift_ldp_result"
GROUP_SHIFT_OUTPUT_SUB_DIR = os.path.join("figures", "group_shift")
GROUP_SHIFT_DATASET_DIR = "4.sin_result"
GROUP_SHIFT_OUTPUT_DATASET_NAME = "sin"
GROUP_SHIFT_DIRECTION_LIST = ["positive", "reverse"]
GROUP_SHIFT_DELTA_LIST = [0.0, 0.05, 0.10, 0.15, 0.20]
X_LABEL_GROUP_SHIFT = r"$\delta$"

# =====================================================================================
# 3. 固定参数（对应 b_*/c_* 中的 default_window_size / default_budget）
# =====================================================================================
# 1) 画"隐私预算 ε 变化"的图时，固定窗口大小 w
DEFAULT_WINDOW_SIZE = 120
# 2) 画"窗口大小 w 变化"的图时，固定隐私预算 ε
DEFAULT_BUDGET = 1.5

# =====================================================================================
# 4. 方法（方案）配置：图例名、线型、颜色
# =====================================================================================
# ---------------------------------------------------------------------------------
# 4.1 方法名（图例文字）
#
# 注意（TODO）：这里的名字是"与 Java 实现一一对应"的中性名，不是论文最终命名。
# 确定论文命名后，只需修改下面字典的 value（不要改 key），
# 所有图（主实验图、消融图、单独导出的图例）都会同步更新。
#
# 之所以不再使用 MATLAB 那套 "PLPD-RP" / "PLPD-OPS"：
#   RP、OPS 是上一代对组件的叫法（旧代码里被注释掉的 "AblateRP-PLPD+"、"Ablate-OPS-Scheme"），
#   现在 Java 侧组件已改叫 GAOPS 与 BAPUE，继续用 -RP/-OPS 会让人对不上号，
#   因此这里的 key 与图例都改成与 Constant 常量一一对应的写法。
#
# key 的含义（与 result.txt 中的数据行一一对应，见 4.3 RESULT_ROW_KEYS）：
#   lpd / lpa                   : 非个性化对比方案（LPD-Scheme / LPA-Scheme）
#   direct_plpd / direct_plpa   : 直接方案（PLPD-Direct-Scheme / PLPA-Direct-Scheme）
#   basic_plpd / basic_plpa     : 只含基础扰动的方案（PLPD-Basic-Scheme / PLPA-Basic-Scheme）
#   gaops_plpd / gaops_plpa     : 消融：只保留 GAOPS（*-Containing-GAOPS-Scheme）
#   bapue_*                     : 消融：只保留 BAPUE（*-Containing-BAPUE-{Pre,Post}-Scheme）
#   plus_*                      : 完整的增强方案（PLPD-Plus-* / PLPA-Plus-*）
# ---------------------------------------------------------------------------------
METHOD_DISPLAY_NAMES = {
    "lpd": "LPD",
    "lpa": "LPA",
    "direct_plpd": "Direct-PLPD",
    "direct_plpa": "Direct-PLPA",
    "basic_plpd": "Basic-PLPD",
    "basic_plpa": "Basic-PLPA",
    "gaops_plpd": "GAOPS-PLPD",
    "gaops_plpa": "GAOPS-PLPA",
    "bapue_plpd_pre": "BAPUE-PLPD-Pre",
    "bapue_plpd_post": "BAPUE-PLPD-Post",
    "bapue_plpa_pre": "BAPUE-PLPA-Pre",
    "bapue_plpa_post": "BAPUE-PLPA-Post",
    "plus_plpd_pre": "PLPD+-Pre",
    "plus_plpd_post": "PLPD+-Post",
    "plus_plpa_pre": "PLPA+-Pre",
    "plus_plpa_post": "PLPA+-Post",
}

# ---------------------------------------------------------------------------------
# 4.2 线型 / 颜色 / 标记（与 MATLAB plot 的字符串参数一一对应）
#   MATLAB: 'ks-'  =>  color='k'(黑), marker='s'(方块), linestyle='-'
# ---------------------------------------------------------------------------------
METHOD_STYLES = {
    # 前 4 项沿用 MATLAB 的配色（ks- / mo- / bs-- / go--）
    "lpd": {"color": "k", "marker": "s", "linestyle": "-"},
    "lpa": {"color": "m", "marker": "o", "linestyle": "-"},
    "direct_plpd": {"color": "b", "marker": "s", "linestyle": "--"},
    "direct_plpa": {"color": "g", "marker": "o", "linestyle": "--"},
    # 消融：只含基础扰动（灰色系，方块/圆点）
    "basic_plpd": {"color": "dimgray", "marker": "s", "linestyle": "-"},
    "basic_plpa": {"color": "dimgray", "marker": "o", "linestyle": "--"},
    # 消融：只保留 GAOPS（蓝色系，菱形）
    "gaops_plpd": {"color": "tab:blue", "marker": "d", "linestyle": "-"},
    "gaops_plpa": {"color": "tab:blue", "marker": "d", "linestyle": "--"},
    # 消融：只保留 BAPUE（绿色系，上/下三角区分 Pre/Post，实线/虚线区分 PLPD/PLPA）
    "bapue_plpd_pre": {"color": "tab:green", "marker": "^", "linestyle": "-"},
    "bapue_plpd_post": {"color": "tab:green", "marker": "v", "linestyle": "-"},
    "bapue_plpa_pre": {"color": "tab:green", "marker": "^", "linestyle": "--"},
    "bapue_plpa_post": {"color": "tab:green", "marker": "v", "linestyle": "--"},
    # 完整的增强方案 Plus（红色系，上/下三角区分 Pre/Post，点划线/点线区分 PLPD/PLPA）
    "plus_plpd_pre": {"color": "tab:red", "marker": "^", "linestyle": "-."},
    "plus_plpd_post": {"color": "tab:red", "marker": "v", "linestyle": "-."},
    "plus_plpa_pre": {"color": "tab:red", "marker": "^", "linestyle": ":"},
    "plus_plpa_post": {"color": "tab:red", "marker": "v", "linestyle": ":"},
}

# ---------------------------------------------------------------------------------
# 4.3-0 期望的"行号 -> 方案名"清单（用于自检）
#
# Java 侧修复后，result.txt 的 Name 列就是 Constant 里的长名，顺序与
# FixedSegmentBasicParameterParallelRun.runSegmentBatch 的写入顺序一致。
# 用 `python run_all.py --check-names -i <结果目录>` 可以拿真实文件和这份清单逐行比对，
# 确认"第 N 行到底是谁"，避免按行号画图时张冠李戴。
# ---------------------------------------------------------------------------------
EXPECTED_ROW_NAMES = [
    "NonPrivacy-Scheme",
    "LPD-Scheme", "LPA-Scheme",
    "PLPD-Direct-Scheme", "PLPA-Direct-Scheme",
    "PLPD-Basic-Scheme", "PLPA-Basic-Scheme",
    "PLPD-Containing-GAOPS-Scheme", "PLPA-Containing-GAOPS-Scheme",
    "PLPD-Containing-BAPUE-Pre-Scheme", "PLPD-Containing-BAPUE-Post-Scheme",
    "PLPA-Containing-BAPUE-Pre-Scheme", "PLPA-Containing-BAPUE-Post-Scheme",
    "PLPD-Plus-Pre-Scheme", "PLPD-Plus-Post-Scheme",
    "PLPA-Plus-Pre-Scheme", "PLPA-Plus-Post-Scheme",
]

# ---------------------------------------------------------------------------------
# 4.3 result.txt 中的数据行顺序（三套命名对照，务必看清）
#
# result.txt 由 Java 端 RepeatUtils 合并生成：combineBeanList 用 nameStringArray 初始化，
# 而 update() 只累加数值、不覆盖 name，因此 result.txt 的 Name 列就是 nameStringArray 里的名字。
#
#   * 修复前：nameStringArray 里是与 Constant 脱节的"短名"（LPD、Direct-PLPD、ARP-PLPD+-Pre...），
#     与 getSimpleName() 返回的长名不是一回事（详见下方对照表与 4.3 末尾说明）；
#   * 修复后：nameStringArray 已改为直接引用 Constant，新跑出来的 result.txt 是"长名"
#     （LPD-Scheme、PLPD-Containing-BAPUE-Pre-Scheme...），与实现同源，不会再出现 AOPS/ARP 这类旧称。
# 存量结果文件仍是短名，所以下面的 NAME_TO_METHOD_KEY 两套名字都收录了。
#
# 完整 17 行对照（第 1 行为 NP，不参与绘图）：
#   行  Constant 长名（result.txt 里的名字）          旧短名              Python key          图例名
#   1   NonPrivacy-Scheme                            NP                  -                   （不画）
#   2   LPD-Scheme                                   LPD                 lpd                 LPD
#   3   LPA-Scheme                                   LPA                 lpa                 LPA
#   4   PLPD-Direct-Scheme                           Direct-PLPD         direct_plpd         Direct-PLPD
#   5   PLPA-Direct-Scheme                           Direct-PLPA         direct_plpa         Direct-PLPA
#   6   PLPD-Basic-Scheme                            Basic-PLPD          basic_plpd          Basic-PLPD
#   7   PLPA-Basic-Scheme                            Basic-PLPA          basic_plpa          Basic-PLPA
#   8   PLPD-Containing-GAOPS-Scheme                 AOPS-PLPD+          gaops_plpd          GAOPS-PLPD
#   9   PLPA-Containing-GAOPS-Scheme                 AOPS-PLPA+          gaops_plpa          GAOPS-PLPA
#   10  PLPD-Containing-BAPUE-Pre-Scheme             ARP-PLPD+-Pre       bapue_plpd_pre      BAPUE-PLPD-Pre   <== 见下方注意
#   11  PLPD-Containing-BAPUE-Post-Scheme            ARP-PLPD+-Post      bapue_plpd_post     BAPUE-PLPD-Post  <== 见下方注意
#   12  PLPA-Containing-BAPUE-Pre-Scheme             ARP-PLPA+-Pre       bapue_plpa_pre      BAPUE-PLPA-Pre
#   13  PLPA-Containing-BAPUE-Post-Scheme            ARP-PLPA+-Post      bapue_plpa_post     BAPUE-PLPA-Post
#   14  PLPD-Plus-Pre-Scheme                         PLPD+-Pre           plus_plpd_pre       PLPD+-Pre       <== 完整方案（已启用）
#   15  PLPD-Plus-Post-Scheme                        PLPD+-Post          plus_plpd_post      PLPD+-Post      <== 完整方案（已启用）
#   16  PLPA-Plus-Pre-Scheme                         PLPA+-Pre           plus_plpa_pre       PLPA+-Pre       <== 完整方案（已启用）
#   17  PLPA-Plus-Post-Scheme                        PLPA+-Post          plus_plpa_post      PLPA+-Post      <== 完整方案（已启用）
#
# 注意：MATLAB 图里标成 "PLPD+" / "PLPA+" 的其实是第 10、11 行，
#       即"只保留 BAPUE 的消融方案"的两个 model（Pre/Post，都属于 PLPD 系列），
#       并不是 AdvancedPLPMechanism（第 14~17 行的 PLPD-Plus-* / PLPA-Plus-*）。
#       也就是说 MATLAB 从来没有画出过真正的完整方案。
#       Python 版已修正：主实验图与消融图都改画第 14~17 行的
#       PLPD+-{Pre,Post} / PLPA+-{Pre,Post}（见 4.4 MAIN_METHOD_KEYS）。
# ---------------------------------------------------------------------------------
FIRST_DATA_ROW = 2          # 第 1 个可绘图方案（LPD）所在的行号（第 1 行是 NP）
RESULT_ROW_KEYS = [
    "lpd", "lpa",                                                   # 第 2、3 行
    "direct_plpd", "direct_plpa",                                   # 第 4、5 行
    "basic_plpd", "basic_plpa",                                     # 第 6、7 行
    "gaops_plpd", "gaops_plpa",                                     # 第 8、9 行
    "bapue_plpd_pre", "bapue_plpd_post",                            # 第 10、11 行
    "bapue_plpa_pre", "bapue_plpa_post",                            # 第 12、13 行
    "plus_plpd_pre", "plus_plpd_post",                              # 第 14、15 行
    "plus_plpa_pre", "plus_plpa_post",                              # 第 16、17 行
]
# 上面已经补齐到第 17 行，所以按行号就能取到全部方案，不再需要依赖名字匹配。

# 可选：按 result.txt 中 "Name" 列的名称定位方案（默认关闭，按行号定位，与 MATLAB 完全一致）。
# 若开启，会优先用名字匹配，行号不匹配时更稳健。
#
# 这里同时收录两套名字，因此新旧结果文件都能匹配：
#   * 长名（如 "PLPD-Containing-BAPUE-Pre-Scheme"）：Java 侧按方案四修复后 result.txt 中的名字
#     （RepeatUtils.nameStringArray 改为直接引用 Constant，与 getSimpleName() 同源）
#   * 短名（如 "ARP-PLPD+-Pre"）：修复前 result.txt 中的名字，用于兼容已有的历史结果文件
# 注意：advanced_* 的行号未加入 RESULT_ROW_KEYS，想画它们请开启本开关（或补齐 RESULT_ROW_KEYS）。
MATCH_METHOD_BY_NAME = False
NAME_TO_METHOD_KEY = {
    # ---- 长名（修复后的新结果文件，即 Constant 常量）----
    "NonPrivacy-Scheme": "",   # 非隐私方案，不参与绘图
    "LPD-Scheme": "lpd",
    "LPA-Scheme": "lpa",
    "PLPD-Direct-Scheme": "direct_plpd",
    "PLPA-Direct-Scheme": "direct_plpa",
    "PLPD-Basic-Scheme": "basic_plpd",
    "PLPA-Basic-Scheme": "basic_plpa",
    "PLPD-Containing-GAOPS-Scheme": "gaops_plpd",
    "PLPA-Containing-GAOPS-Scheme": "gaops_plpa",
    "PLPD-Containing-BAPUE-Pre-Scheme": "bapue_plpd_pre",
    "PLPD-Containing-BAPUE-Post-Scheme": "bapue_plpd_post",
    "PLPA-Containing-BAPUE-Pre-Scheme": "bapue_plpa_pre",
    "PLPA-Containing-BAPUE-Post-Scheme": "bapue_plpa_post",
    "PLPD-Plus-Pre-Scheme": "plus_plpd_pre",
    "PLPD-Plus-Post-Scheme": "plus_plpd_post",
    "PLPA-Plus-Pre-Scheme": "plus_plpa_pre",
    "PLPA-Plus-Post-Scheme": "plus_plpa_post",
    # ---- 短名（修复前的历史结果文件，兼容用）----
    "NP": "",
    "LPD": "lpd",
    "LPA": "lpa",
    "Direct-PLPD": "direct_plpd",
    "Direct-PLPA": "direct_plpa",
    "Basic-PLPD": "basic_plpd",
    "Basic-PLPA": "basic_plpa",
    "AOPS-PLPD+": "gaops_plpd",
    "AOPS-PLPA+": "gaops_plpa",
    "ARP-PLPD+-Pre": "bapue_plpd_pre",
    "ARP-PLPD+-Post": "bapue_plpd_post",
    "ARP-PLPA+-Pre": "bapue_plpa_pre",
    "ARP-PLPA+-Post": "bapue_plpa_post",
    "PLPD+-Pre": "plus_plpd_pre",
    "PLPD+-Post": "plus_plpd_post",
    "PLPA+-Pre": "plus_plpa_pre",
    "PLPA+-Post": "plus_plpa_post",
}

# ---------------------------------------------------------------------------------
# 4.4 每类图绘制哪些方案（顺序即图例顺序）
# ---------------------------------------------------------------------------------
# 主实验（对应 a_3、a_4、bar_main）
# 共 10 条线：LPD / LPA / Direct-PLPD / Direct-PLPA / Basic-PLPD / Basic-PLPA
#             + 完整方案的 Pre/Post × PLPD/PLPA。
# 与 MATLAB 的差异：MATLAB 只画了 2 条（且取的是第 10、11 行的 BAPUE 消融方案），
# 这里改为真正的完整方案（第 14~17 行的 PLPD-Plus-* / PLPA-Plus-*），并区分 Pre/Post。
# 若只想保留其中一种 model，删掉 *_post（或 *_pre）对应的两项即可。
MAIN_METHOD_KEYS = [
    "lpd", "lpa",
    "direct_plpd", "direct_plpa",
    "basic_plpd", "basic_plpa",
    "plus_plpd_pre", "plus_plpd_post",
    "plus_plpa_pre", "plus_plpa_post",
]
# 时间开销图（对应 a_1、a_2，输出在 experiment_result_add/time_cost_fig）
# 原先直接复用 MAIN_METHOD_KEYS，因此画不出 Basic；这里单独列一份，加入两个 Basic 方案。
# 如果想让时间开销图和消融图完全一致（12 条），把下面这行改成：
#   TIME_COST_METHOD_KEYS = ABLATION_METHOD_KEYS
TIME_COST_METHOD_KEYS = [
    "lpd", "lpa",
    "direct_plpd", "direct_plpa",
    "basic_plpd", "basic_plpa",
    "plus_plpd_pre", "plus_plpd_post",
    "plus_plpa_pre", "plus_plpa_post",
]

# 消融实验（对应 a_5、a_6、bar_ablation）
# 共 12 条线：Basic 2 条（两个组件都没有）
#            + GAOPS-only 2 条（只有 GAOPS）
#            + BAPUE-only 4 条（只有 BAPUE，Pre/Post × PLPD/PLPA）
#            + Plus 4 条（两个组件都有，即完整方案）
# 说明：Direct-PLPD/PLPA 属于"对比基线"，不参与组件消融，已移至主实验图（MAIN_METHOD_KEYS）。
ABLATION_METHOD_KEYS = [
    "basic_plpd", "basic_plpa",
    "gaops_plpd", "gaops_plpa",
    "bapue_plpd_pre", "bapue_plpd_post",
    "bapue_plpa_pre", "bapue_plpa_post",
    "plus_plpd_pre", "plus_plpd_post",
    "plus_plpa_pre", "plus_plpa_post",
]

# =====================================================================================
# 5. 指标配置（对应 b_*/c_* 中的 metric_col_index 等参数）
# =====================================================================================
# 字段说明：
#   col_index    : result.txt 中的列号（1-based，与 MATLAB 的 metric_col_index 完全一致）
#   col_name     : 表头名称；非空且能匹配到表头时，优先按列名取数（可填 "" 只按列号取）
#   display_name : y 轴标签与输出文件名中使用的名字
#   file_name    : 输出文件名中使用的名字（仅时间开销图用，用于区分 "running time (s)" 中的空格）
#   shrink_ratio : 数值缩放系数（对应 MATLAB 的 shrink_ratio，如 ms -> s 用 0.001）
#   whether_log  : 是否对原始值取自然对数（对应 MATLAB 的 metric_whether_log）
METRIC_TIME_COST = {
    "col_index": 4,
    "col_name": "Time Cost",
    "display_name": "running time (s)",
    "file_name": "running_time",
    "shrink_ratio": 0.001,
    "whether_log": False,
}
METRIC_AMSE = {
    "col_index": 9,
    "col_name": "",
    "display_name": "AMSE",
    "file_name": "AMSE",
    "shrink_ratio": 1.0,
    "whether_log": True,
}
METRIC_AJSD = {
    "col_index": 10,
    "col_name": "",
    "display_name": "AJSD",
    "file_name": "AJSD",
    "shrink_ratio": 1.0,
    "whether_log": True,
}

# =====================================================================================
# 6. 图形样式（对应 MATLAB 脚本中的 figure_MarkerSize / figure_FontSize 等）
# =====================================================================================
FONT_FAMILY = "Times New Roman"      # 对应 MATLAB 的 'FontName','Times New Roman'
FONT_FAMILY_FALLBACK = "DejaVu Serif"  # Linux 服务器上没有 Times New Roman 时的兜底字体

# 以下数值与 matlab_code/experiment_result/a_1 ~ a_6 完全一致
FIGURE_MARKER_SIZE = 20              # MATLAB: figure_MarkerSize = 20
LINE_WIDTH = 2                       # MATLAB: 'LineWidth', 2

# marker 是否空心："none" 表示空心（只画边框，点堆叠时不会糊成一片）；
# 想恢复实心把它改成 None 或 "auto"。
MARKER_FACE_COLOR = "none"
# 空心 marker 的边框粗细（marker 越大、颜色越密，这个值可以再调大一点）
# MATLAB 用的是实心点，没有边框参数；这里为了空心点看得清才需要，取适中的 2.0
MARKER_EDGE_WIDTH = 2.0
# 字号：刻度与 x 轴标签 28；y 轴标签（如 running time (s)）单独调小为 24
FIGURE_FONT_SIZE = 28                # 刻度数字字号（set(gca,'FontSize',...)）
FIGURE_FONT_SIZE_X = 28              # x 轴标签字号
FIGURE_FONT_SIZE_Y = 24              # y 轴标签字号
LEGEND_FONT_SIZE_IN_FIG = 14         # 图中图例字号（MATLAB: set(h,'FontSize',14)）
# 单独导出图例时的字号（MATLAB: legend_FontSize = 20）
# 说明：图例图宽度 = 列数 × 每项宽度，随字号增大而变宽；
#       超出 \textwidth 时请在 LaTeX 里用 \includegraphics[width=\textwidth]{...} 缩放。
LEGEND_FONT_SIZE_ONLY = 20

# 图片尺寸（英寸）——固定基准，不要随字号一起放大！
# 原因：LaTeX 里按固定宽度（如 0.19\textwidth）插入时，视觉字号 ∝ 字号 ÷ 图宽。
#      若把画布和字号同时放大，两者相抵，字看起来没变大，PDF 反而变大导致一行放不下。
#      所以要放大"看起来"的字，只改 FIGURE_FONT_SIZE_* / LINE_WIDTH，这里保持不变。
FIG_SIZE = (5.5, 4.1)

# 绘图区（axes）在画布中的占比，用于控制四周留白。
# 注意：SAVE_TIGHT=False 时输出严格等于 FIG_SIZE，边距必须容纳刻度+轴标签，否则会被裁掉
# （left 要装下 y 轴刻度数字 + 旋转的 y 标签；bottom 要装下 x 轴刻度数字 + x 标签）。
AXES_ADJUST = {"left": 0.24, "right": 0.94, "top": 0.94, "bottom": 0.27}
# 说明：x 轴两端刻度标签（"40"/"200"）以刻度线为中心、各伸出一半宽度（28pt 字号约 21pt），
#      所以 right/top 不能太贴近 1.0，否则两端的 "200"、最上排刻度会被裁掉。
# y 轴最多显示几条刻度（字号很大时防止刻度标签互相重叠）
Y_TICK_BINS = 5
# 数据图是否按内容裁剪白边：False 表示严格按 FIG_SIZE 输出，保证 LaTeX 中排版尺寸稳定
SAVE_TIGHT = False

# x 轴标签：用 LaTeX 数学字体（斜体，mathtext 字体为 stix，接近 Times）
#   r"$\varepsilon$" -> ε（正常 ε 形状，斜体）  <== 当前使用
#   r"$\epsilon$"    -> ϵ（弯月形的"变体"，不要）
X_LABEL_BUDGET = r"$\epsilon$"    # 隐私预算
X_LABEL_WINDOW_SIZE = r"$w$"         # 窗口大小
X_LABEL_DOMAIN_SIZE = r"$d$"         # 取值域大小（维度可扩展性实验）

# 是否显示图例：MATLAB 各脚本最后都调用了 legend('off')，
# 即"只生成图例句柄但不显示"，图例单独由 bar_main/bar_ablation 导出，因此这里默认 False。
SHOW_LEGEND_IN_FIGURE = False
LEGEND_LOCATION = "best"

# 单独导出图例（bar_main / bar_ablation）时的列数：都排成两行
# 主实验 10 条 -> 5 列 × 2 行；消融实验 12 条 -> 6 列 × 2 行
# 列数越多图越宽；想让图例更窄就把列数调小（行数会自动增加）
LEGEND_ONLY_NCOL_MAIN = 5
LEGEND_ONLY_NCOL_ABLATION = 6
# 消融图例单栏版：每行 4 个（12 条 -> 4 列 × 3 行），适合较窄的版面
LEGEND_ONLY_NCOL_ABLATION_SINGLE = 4
# 以下与 MATLAB bar_main.m / bar_ablation.m 一致
LEGEND_ONLY_MARKER_SIZE = 15         # MATLAB: figure_MarkerSize = 15
LEGEND_ONLY_LINE_WIDTH = 2           # MATLAB: 'LineWidth', 2
LEGEND_ONLY_MARKER_EDGE_WIDTH = 1.2  # 空心点边框（MATLAB 为实心点，无对应参数）
# 图例画布尺寸：宽 = 列数 × 每项宽度，高 = 行数 × 每行高度
LEGEND_ONLY_WIDTH_PER_COL = 2.3
LEGEND_ONLY_ROW_HEIGHT = 1.5
# 图例内部间距（单位：字号倍数）与标记线段长度，调小可让图例更紧凑、在同样宽度下用更大字号
LEGEND_COLUMN_SPACING = 0.8
LEGEND_HANDLE_TEXT_PAD = 0.35
LEGEND_BORDER_PAD = 0.25
LEGEND_HANDLE_LENGTH = 1.2

# =====================================================================================
# 7. 输出配置（对应 export_fig(fig, outputFileName, '-pdf', '-r256', '-transparent')）
# =====================================================================================
OUTPUT_DPI = 256                     # 对应 '-r256'
OUTPUT_TRANSPARENT = True            # 对应 '-transparent'
# 导出图片时四周保留的空白（英寸）。
# 注意不能设成 0：bbox_inches='tight' 按 marker 顶点计算边界，不含线宽
#（菱形/三角形的尖角处线宽会向外溢出约 1~2pt），pad 太小时尖角会被裁掉（缺角）。
OUTPUT_PAD_INCHES = 0.05
OUTPUT_EXT = ".pdf"                  # 对应 '-pdf'；可改成 ".png" / ".eps" / ".svg"
