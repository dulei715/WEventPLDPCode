# -*- coding: utf-8 -*-
"""主实验与时间开销的批量绘图（对应 matlab_code/experiment_result 中的 b_1 ~ b_4）

MATLAB 对应关系：
    b_1_plotBudgetChangeForTime.m         -> b_1_plot_budget_change_for_time
    b_2_plotWindowSizeChangeForTime.m     -> b_2_plot_window_size_change_for_time
    b_3_plotMainBudgetChangeForError.m    -> b_3_plot_main_budget_change_for_error
    b_4_plotMainWindowSizeChangeForError.m-> b_4_plot_main_window_size_change_for_error

每个函数都是"遍历 5 个数据集 -> 调用 a_* 函数"，输出规则与 MATLAB 保持一致：
    b_1/b_2: figures/experiment_result_add/time_cost_fig/{数据集}_{变化维度}_{指标}.pdf
    b_3/b_4: figures/experiment_result/{数据集}_{变化维度}_{指标}_main_ldp.pdf
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import config  # noqa: E402
import a_plot_single_ds as plot_a  # noqa: E402
from utils.dir_utils import build_output_path  # noqa: E402


def _iter_datasets(input_basic_path):
    """遍历所有数据集目录（对应 MATLAB 中的 for dir_name = dataset_dirs）

    :return: 生成器，元素为 (数据集目录绝对路径, 数据集输出名)
    """
    for dir_name, output_name in zip(config.DATASET_DIRS, config.OUTPUT_DATASET_NAMES):
        yield os.path.join(str(input_basic_path), dir_name), output_name


def _iter_domain_datasets():
    """遍历参加维度可扩展性实验的数据集（只有合成数据集能改取值域大小）

    与 _iter_datasets 的区别：这里产出的是"目录名"而不是绝对路径，
    因为 d 变化的图需要按 {目录名}_d{d} 逐个拼出结果目录。

    :return: 生成器，元素为 (数据集结果目录名, 数据集输出名)
    """
    for dir_name, output_name in zip(config.DOMAIN_DATASET_DIRS, config.DOMAIN_OUTPUT_DATASET_NAMES):
        yield dir_name, output_name


def b_1_plot_budget_change_for_time(input_basic_path, output_basic_path, show_legend=None):
    """b_1：时间开销随隐私预算 ε 变化（固定窗口大小）

    对应 b_1_plotBudgetChangeForTime.m
    """
    metric = config.METRIC_TIME_COST
    outputs = []
    for dataset_path, dataset_name in _iter_datasets(input_basic_path):
        output_file = build_output_path(
            output_basic_path,
            config.OUTPUT_SUB_DIR_TIME,
            "%s_budget_change_%s" % (dataset_name, metric["file_name"]),
        )
        result = plot_a.a_1_plot_basic_budget_time_cost(
            dataset_path, config.DEFAULT_WINDOW_SIZE, metric, output_file, show_legend
        )
        if result:
            outputs.append(result)
    return outputs


def b_2_plot_window_size_change_for_time(input_basic_path, output_basic_path, show_legend=None):
    """b_2：时间开销随窗口大小 w 变化（固定隐私预算）

    对应 b_2_plotWindowSizeChangeForTime.m
    """
    metric = config.METRIC_TIME_COST
    outputs = []
    for dataset_path, dataset_name in _iter_datasets(input_basic_path):
        output_file = build_output_path(
            output_basic_path,
            config.OUTPUT_SUB_DIR_TIME,
            "%s_window_size_change_%s" % (dataset_name, metric["file_name"]),
        )
        result = plot_a.a_2_plot_basic_window_size_time_cost(
            dataset_path, config.DEFAULT_BUDGET, metric, output_file, show_legend
        )
        if result:
            outputs.append(result)
    return outputs


def b_3_plot_main_budget_change_for_error(input_basic_path, output_basic_path, show_legend=None):
    """b_3：主实验误差/散度随隐私预算 ε 变化（AMSE 与 AJSD 各一组图）

    对应 b_3_plotMainBudgetChangeForError.m
    """
    outputs = []
    for metric in (config.METRIC_AMSE, config.METRIC_AJSD):
        for dataset_path, dataset_name in _iter_datasets(input_basic_path):
            output_file = build_output_path(
                output_basic_path,
                config.OUTPUT_SUB_DIR_MAIN,
                "%s_budget_change_%s_main_ldp" % (dataset_name, metric["display_name"]),
            )
            result = plot_a.a_3_plot_basic_main_budget_change_for_single_ds(
                dataset_path, config.DEFAULT_WINDOW_SIZE, metric, output_file, show_legend
            )
            if result:
                outputs.append(result)
    return outputs


def b_4_plot_main_window_size_change_for_error(input_basic_path, output_basic_path,
                                               show_legend=None):
    """b_4：主实验误差/散度随窗口大小 w 变化（AMSE 与 AJSD 各一组图）

    对应 b_4_plotMainWindowSizeChangeForError.m
    """
    outputs = []
    for metric in (config.METRIC_AMSE, config.METRIC_AJSD):
        for dataset_path, dataset_name in _iter_datasets(input_basic_path):
            output_file = build_output_path(
                output_basic_path,
                config.OUTPUT_SUB_DIR_MAIN,
                "%s_window_size_change_%s_main_ldp" % (dataset_name, metric["display_name"]),
            )
            result = plot_a.a_4_plot_basic_main_w_change_for_single_ds(
                dataset_path, config.DEFAULT_BUDGET, metric, output_file, show_legend
            )
            if result:
                outputs.append(result)
    return outputs


def b_5_plot_domain_change_for_error(input_basic_path, output_basic_path, show_legend=None):
    """b_5：主实验误差/散度随取值域大小 d 变化（AMSE 与 AJSD 各一组图）

    无 MATLAB 对应脚本。只遍历合成数据集（tlns / sin / log），
    输出到 figures/experiment_result_add/domain_size_fig/。
    """
    outputs = []
    for metric in (config.METRIC_AMSE, config.METRIC_AJSD):
        for dataset_dir, dataset_name in _iter_domain_datasets():
            output_file = build_output_path(
                output_basic_path,
                config.OUTPUT_SUB_DIR_DOMAIN,
                "%s_domain_size_change_%s_main_ldp" % (dataset_name, metric["display_name"]),
            )
            result = plot_a.a_7_plot_domain_change_for_single_ds(
                input_basic_path, dataset_dir, metric, output_file, show_legend
            )
            if result:
                outputs.append(result)
    return outputs


def b_6_plot_domain_change_for_time(input_basic_path, output_basic_path, show_legend=None):
    """b_6：时间开销随取值域大小 d 变化

    无 MATLAB 对应脚本。与 b_5 同样的取数方式，指标换成时间开销。
    """
    metric = config.METRIC_TIME_COST
    outputs = []
    for dataset_dir, dataset_name in _iter_domain_datasets():
        output_file = build_output_path(
            output_basic_path,
            config.OUTPUT_SUB_DIR_DOMAIN,
            "%s_domain_size_change_%s" % (dataset_name, metric["file_name"]),
        )
        result = plot_a.a_8_plot_domain_change_time_cost_for_single_ds(
            input_basic_path, dataset_dir, metric, output_file, show_legend
        )
        if result:
            outputs.append(result)
    return outputs


if __name__ == "__main__":
    # 简单用法：python b_plot_main.py <input_basic_path> <output_basic_path>
    input_path = sys.argv[1] if len(sys.argv) > 1 else config.INPUT_BASIC_PATH
    output_path = sys.argv[2] if len(sys.argv) > 2 else config.OUTPUT_BASIC_PATH
    b_1_plot_budget_change_for_time(input_path, output_path)
    b_2_plot_window_size_change_for_time(input_path, output_path)
    b_3_plot_main_budget_change_for_error(input_path, output_path)
    b_4_plot_main_window_size_change_for_error(input_path, output_path)
    b_5_plot_domain_change_for_error(input_path, output_path)
    b_6_plot_domain_change_for_time(input_path, output_path)
