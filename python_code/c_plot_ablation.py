# -*- coding: utf-8 -*-
"""消融实验的批量绘图（对应 matlab_code/experiment_result 中的 c_1、c_2）

MATLAB 对应关系：
    c_1_plotAblationBudgetChangeForError.m     -> c_1_plot_ablation_budget_change_for_error
    c_2_plotAblationWindowSizeChangeForError.m -> c_2_plot_ablation_window_size_change_for_error

输出规则与 MATLAB 一致：
    figures/experiment_result_add/{数据集}_{变化维度}_{指标}_ablation_ldp.pdf
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import config  # noqa: E402
import a_plot_single_ds as plot_a  # noqa: E402
from utils.dir_utils import build_output_path  # noqa: E402


def _iter_datasets(input_basic_path):
    """遍历所有数据集目录（与 b_plot_main.py 中同名函数一致）"""
    for dir_name, output_name in zip(config.DATASET_DIRS, config.OUTPUT_DATASET_NAMES):
        yield os.path.join(str(input_basic_path), dir_name), output_name


def c_1_plot_ablation_budget_change_for_error(input_basic_path, output_basic_path,
                                              show_legend=None):
    """c_1：消融实验误差/散度随隐私预算 ε 变化（固定窗口大小，AMSE 与 AJSD 各一组图）

    对应 c_1_plotAblationBudgetChangeForError.m
    """
    outputs = []
    for metric in (config.METRIC_AMSE, config.METRIC_AJSD):
        for dataset_path, dataset_name in _iter_datasets(input_basic_path):
            output_file = build_output_path(
                output_basic_path,
                config.OUTPUT_SUB_DIR_ADD,
                "%s_budget_change_%s_ablation_ldp" % (dataset_name, metric["display_name"]),
            )
            result = plot_a.a_5_plot_basic_ablate_budget_change_for_single_ds(
                dataset_path, config.DEFAULT_WINDOW_SIZE, metric, output_file, show_legend
            )
            if result:
                outputs.append(result)
    return outputs


def c_2_plot_ablation_window_size_change_for_error(input_basic_path, output_basic_path,
                                                   show_legend=None):
    """c_2：消融实验误差/散度随窗口大小 w 变化（固定隐私预算，AMSE 与 AJSD 各一组图）

    对应 c_2_plotAblationWindowSizeChangeForError.m
    """
    outputs = []
    for metric in (config.METRIC_AMSE, config.METRIC_AJSD):
        for dataset_path, dataset_name in _iter_datasets(input_basic_path):
            output_file = build_output_path(
                output_basic_path,
                config.OUTPUT_SUB_DIR_ADD,
                "%s_window_size_change_%s_ablation_ldp" % (dataset_name, metric["display_name"]),
            )
            result = plot_a.a_6_plot_basic_ablate_w_change_for_single_ds(
                dataset_path, config.DEFAULT_BUDGET, metric, output_file, show_legend
            )
            if result:
                outputs.append(result)
    return outputs


if __name__ == "__main__":
    # 简单用法：python c_plot_ablation.py <input_basic_path> <output_basic_path>
    input_path = sys.argv[1] if len(sys.argv) > 1 else config.INPUT_BASIC_PATH
    output_path = sys.argv[2] if len(sys.argv) > 2 else config.OUTPUT_BASIC_PATH
    c_1_plot_ablation_budget_change_for_error(input_path, output_path)
    c_2_plot_ablation_window_size_change_for_error(input_path, output_path)
