# -*- coding: utf-8 -*-
"""一键绘图的命令行入口（整合 b_1 ~ b_4、c_1 ~ c_2、图例导出，以及方案名自检）

用法示例（在 python_code 目录下执行）：

    # 1) 先自检：确认 result.txt 的行名与 Python 的映射是否对得上（不画图，不需要 matplotlib）
    python run_all.py --check-names -i D:/exp/1.main_ldp_result

    # 2) 全部图（主实验 + 消融 + 时间开销 + 图例）
    python run_all.py --input-basic-path D:/exp/1.main_ldp_result --output-basic-path D:/exp

    # 3) 只画主实验误差图，输出 png，并在图内显示图例
    python run_all.py -i D:/exp/1.main_ldp_result -o D:/exp --tasks b3,b4 --ext .png --show-legend

    # 4) 路径也可以直接写在 config.py 的 INPUT_BASIC_PATH / OUTPUT_BASIC_PATH 中，之后只需
    python run_all.py

注意：所有方法名（图例文字）都集中在 config.py 的 METHOD_DISPLAY_NAMES 中，
      修改那里的 value 即可一次性替换全部图例。
"""

import argparse
import importlib
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import config  # noqa: E402
from utils.dir_utils import list_dir_name  # noqa: E402
from utils.result_reader import read_result_table, build_row_report  # noqa: E402

# 任务名 -> (说明, 模块名, 函数名)
# 这里保存模块名/函数名而不是直接 import，是为了让 --check-names 在没有 matplotlib 的环境下也能运行
TASKS = {
    "b1": ("时间开销随隐私预算变化", "b_plot_main", "b_1_plot_budget_change_for_time"),
    "b2": ("时间开销随窗口大小变化", "b_plot_main", "b_2_plot_window_size_change_for_time"),
    "b3": ("主实验误差随隐私预算变化", "b_plot_main", "b_3_plot_main_budget_change_for_error"),
    "b4": ("主实验误差随窗口大小变化", "b_plot_main", "b_4_plot_main_window_size_change_for_error"),
    "c1": ("消融实验误差随隐私预算变化", "c_plot_ablation", "c_1_plot_ablation_budget_change_for_error"),
    "c2": ("消融实验误差随窗口大小变化", "c_plot_ablation", "c_2_plot_ablation_window_size_change_for_error"),
}
TASK_LEGEND = ("单独导出主实验/消融实验图例", "legend_only", "")


def run_task(task_name, input_basic_path, output_basic_path, show_legend=None):
    """执行单个任务

    :param task_name: 任务名（b1~b4、c1~c2、legend）
    :param input_basic_path: 实验结果根目录
    :param output_basic_path: 图片输出根目录
    :param show_legend: 是否在图中显示图例
    :return: 输出文件列表
    """
    task_name = task_name.lower()
    if task_name == "legend" or task_name == "all_legend":
        module = importlib.import_module(TASK_LEGEND[1])
        return [
            module.draw_main_legend(output_basic_path),
            module.draw_ablation_legend(output_basic_path),
        ]
    if task_name not in TASKS:
        raise ValueError("未知任务：%s（可选：%s）" % (task_name, ", ".join(sorted(TASKS) + ["legend"])))
    _, module_name, function_name = TASKS[task_name]
    module = importlib.import_module(module_name)
    return getattr(module, function_name)(input_basic_path, output_basic_path, show_legend)


def check_names(input_basic_path):
    """自检：打印每个数据集 result.txt 的"行号 -> 方案名 -> 图例名"对照表

    用于确认按行号画图时不会张冠李戴，核对三件事：
        1) 文件里的名字是否与 config.EXPECTED_ROW_NAMES 一致；
        2) 按行号定位到的 key 与按名字定位到的 key 是否一致；
        3) 该 key 最终显示的图例名是什么。

    :param input_basic_path: 实验结果根目录
    :return: 全部一致返回 True，否则 False
    """
    all_ok = True
    for dataset_dir, dataset_name in zip(config.DATASET_DIRS, config.OUTPUT_DATASET_NAMES):
        dataset_path = os.path.join(str(input_basic_path), dataset_dir)
        param_dirs = list_dir_name(dataset_path)
        if not param_dirs:
            print("[WARN] 目录下没有参数子目录，跳过：%s" % dataset_path)
            all_ok = False
            continue

        # 只取第一个参数目录做检查即可（各参数目录的行顺序应当一致）
        param_dir = param_dirs[0]
        result_file = os.path.join(dataset_path, param_dir, "result.txt")
        table = read_result_table(result_file)
        if not table[1]:
            print("[WARN] 读取失败，跳过：%s" % result_file)
            all_ok = False
            continue

        report = build_row_report(
            table,
            expected_names=config.EXPECTED_ROW_NAMES,
            name_to_method_key=config.NAME_TO_METHOD_KEY,
            method_row_keys=config.RESULT_ROW_KEYS,
            first_data_row=config.FIRST_DATA_ROW,
        )

        print("")
        print("=" * 108)
        print("[%s] %s  (样例文件：%s/result.txt)" % (dataset_name, dataset_dir, param_dir))
        print("-" * 108)
        print("%-4s %-36s %-36s %-6s %-8s" % ("行", "文件中的名字", "期望的名字", "同名?", "图例"))
        print("-" * 108)
        for item in report:
            name_matched = item["name_matched"]
            mark = "OK" if name_matched else ("--" if name_matched is None else "XX")
            if name_matched is False:
                all_ok = False
            if item["consistent"] is False:
                all_ok = False
                mark = "XX"
            legend = config.METHOD_DISPLAY_NAMES.get(item["key_by_row"], "") if item["key_by_row"] else ""
            print("%-4s %-36s %-36s %-6s %-8s"
                  % (item["row"], item["name"], item["expected"], mark, legend))
            if item["consistent"] is False:
                print("     [!!] 按行号取到 '%s'，按名字取到 '%s'，两者不一致，请检查 config"
                      % (item["key_by_row"], item["key_by_name"]))
        print("-" * 108)

    print("")
    if all_ok:
        print("[CHECK] 全部数据集的行名与配置一致")
    else:
        print("[CHECK] 存在不一致，请根据上面标 XX 的行修改 config.py")
    return all_ok


def parse_args(argv=None):
    """解析命令行参数"""
    parser = argparse.ArgumentParser(
        description="WEventPLDP 实验结果绘图（Python 版，与 matlab_code 一一对应）",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument("-i", "--input-basic-path", default=config.INPUT_BASIC_PATH,
                        help="实验结果根目录（其下含 1.trajectory_result 等数据集目录）")
    parser.add_argument("-o", "--output-basic-path", default=config.OUTPUT_BASIC_PATH,
                        help="图片输出根目录")
    parser.add_argument("-t", "--tasks", default="b1,b2,b3,b4,c1,c2,legend",
                        help="要执行的任务，逗号分隔，可选：b1,b2,b3,b4,c1,c2,legend,all")
    parser.add_argument("--ext", default=config.OUTPUT_EXT,
                        help="输出图片扩展名，如 .pdf / .png / .eps")
    parser.add_argument("--show-legend", action="store_true",
                        help="是否在每张图内部显示图例（默认不显示，与 MATLAB 的 legend('off') 一致）")
    parser.add_argument("--check-names", action="store_true",
                        help="只做方案名自检：打印行号/方案名/图例名对照表，不画图")
    parser.add_argument("--list-tasks", action="store_true", help="列出所有任务后退出")
    return parser.parse_args(argv)


def main(argv=None):
    args = parse_args(argv)

    if args.list_tasks:
        for name in sorted(TASKS):
            print("%-4s %s" % (name, TASKS[name][0]))
        print("%-4s %s" % ("legend", TASK_LEGEND[0]))
        return 0

    if not args.input_basic_path:
        print("[ERROR] 请先通过 -i 指定结果路径，或在 config.py 中填写 INPUT_BASIC_PATH")
        return 1

    if args.check_names:
        return 0 if check_names(args.input_basic_path) else 2

    if not args.output_basic_path:
        print("[ERROR] 请先通过 -o 指定输出路径，或在 config.py 中填写 OUTPUT_BASIC_PATH")
        return 1

    # 覆盖全局配置（输出格式等）
    config.OUTPUT_EXT = args.ext
    if args.show_legend:
        config.SHOW_LEGEND_IN_FIGURE = True

    task_names = [item.strip() for item in args.tasks.split(",") if item.strip()]
    if "all" in [name.lower() for name in task_names]:
        task_names = ["b1", "b2", "b3", "b4", "c1", "c2", "legend"]

    show_legend = True if args.show_legend else None
    for task_name in task_names:
        print("[RUN ] 任务 %s 开始 ..." % task_name)
        outputs = run_task(task_name, args.input_basic_path, args.output_basic_path, show_legend)
        for output in outputs:
            if output:
                print("        -> %s" % output)
    print("[DONE] 全部任务完成")
    return 0


if __name__ == "__main__":
    sys.exit(main())
