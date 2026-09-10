# -*- coding: utf-8 -*-
"""单个数据集上的基础绘图函数（对应 matlab_code/experiment_result 中的 a_1 ~ a_6）

MATLAB 对应关系：
    a_1_plotBasicBudgetTimeCost.m                                      -> a_1_plot_basic_budget_time_cost
    a_2_plotBasicWindowSizeTimeCost.m                                  -> a_2_plot_basic_window_size_time_cost
    a_3_plotBasicMainBudgetChangeInfluenceGivenMetricForSingleDS.m     -> a_3_plot_basic_main_budget_change_for_single_ds
    a_4_plotBasicMainWChangeInfluenceGivenMetricForSingleDS.m          -> a_4_plot_basic_main_w_change_for_single_ds
    a_5_plotBasicAblateBudgetChangeInfluenceGivenMetricForSingleDS.m   -> a_5_plot_basic_ablate_budget_change_for_single_ds
    a_6_plotBasicAblateWChangeInfluenceGivenMetricForSingleDS.m        -> a_6_plot_basic_ablate_w_change_for_single_ds

这 6 个函数的流程完全一致（读数据 -> 画线 -> 设样式 -> 导出），
因此这里抽出通用函数 plot_metric_change_for_single_ds，
a_1 ~ a_6 只保留各自的参数与调用方式，方便与 MATLAB 代码一一对照。
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import config  # noqa: E402
from utils import plot_utils  # noqa: E402
from utils.result_reader import collect_metric_series  # noqa: E402


def build_y_label(metric):
    """生成 y 轴标签（对应 MATLAB 中 ln(metric_name) 或 metric_name）"""
    name = metric.get("display_name", "")
    if metric.get("whether_log", False):
        return "ln(" + name + ")"
    return name


def plot_metric_change_for_single_ds(basic_path, x_type, fixed_value, metric, method_keys,
                                     output_file, x_label=None, y_label=None,
                                     show_legend=None, sort_by_x=True):
    """画"某指标随预算/窗口变化"的折线图（a_1 ~ a_6 的公共实现）

    :param basic_path: 单个数据集的结果目录，如 ".../1.trajectory_result"
    :param x_type: "budget"（x 轴为隐私预算 ε）或 "window_size"（x 轴为窗口大小 w）
    :param fixed_value: 另一个被固定的参数值
    :param metric: config 中的指标字典
    :param method_keys: 要绘制的方案 key 列表
    :param output_file: 输出文件路径
    :param x_label: x 轴标签；为 None 时使用 config 中的默认标签
    :param y_label: y 轴标签；为 None 时由指标自动生成
    :param show_legend: 是否显示图例；为 None 时使用 config.SHOW_LEGEND_IN_FIGURE
    :param sort_by_x: 是否按 x 轴数值排序（默认 True）
    :return: 输出文件路径；无数据时返回 None
    """
    if x_label is None:
        x_label = (config.X_LABEL_BUDGET if x_type == "budget"
                   else config.X_LABEL_WINDOW_SIZE)
    if y_label is None:
        y_label = build_y_label(metric)

    # ---------- 1. 读取数据（对应 a_* 中的 for 循环 + readtable） ----------
    x_list, y_series = collect_metric_series(
        basic_path=basic_path,
        x_type=x_type,
        fixed_value=fixed_value,
        metric=metric,
        method_keys=method_keys,
        method_row_keys=config.RESULT_ROW_KEYS,
        first_data_row=config.FIRST_DATA_ROW,
        name_to_method_key=config.NAME_TO_METHOD_KEY,
        match_by_name=config.MATCH_METHOD_BY_NAME,
        sort_by_x=sort_by_x,
    )
    if not x_list:
        print("[WARN] 未读取到任何数据，跳过绘图：%s" % basic_path)
        return None

    # ---------- 2. 绘图（对应 a_* 中的 figure; hold on; plot(...)） ----------
    plot_utils.setup_matplotlib()
    fig, ax = plot_utils.new_figure()

    handles = []
    for method_key in method_keys:
        handles.append(plot_utils.plot_one_line(ax, x_list, y_series[method_key], method_key))

    # ---------- 3. 坐标轴与样式（对应 xlim / XTick / set(gca,...)） ----------
    plot_utils.set_axis_labels(ax, x_label, y_label)
    plot_utils.style_axes(ax)

    # 对应 MATLAB 的 xlim([round(x(1),1) x(end)]) 与 set(gca,'XTick',round(x,1))
    x_ticks = [round(value, 1) for value in x_list]
    ax.set_xlim(x_ticks[0], x_ticks[-1])
    ax.set_xticks(x_ticks)

    # 对应 MATLAB 中 legend(...) + legend('off')
    plot_utils.add_legend(ax, handles=handles, show=show_legend)

    # ---------- 4. 导出（对应 export_fig(..., '-pdf', '-r256', '-transparent')） ----------
    return plot_utils.save_figure(fig, output_file)


# =====================================================================================
# 下面是与 MATLAB 一一对应的 6 个函数
# =====================================================================================
def a_1_plot_basic_budget_time_cost(basic_path, default_window_size, metric, output_file,
                                    show_legend=None):
    """a_1：时间开销随隐私预算 ε 变化（固定窗口大小），主实验方案

    对应 a_1_plotBasicBudgetTimeCost.m
    """
    return plot_metric_change_for_single_ds(
        basic_path=basic_path,
        x_type="budget",
        fixed_value=default_window_size,
        metric=metric,
        method_keys=config.TIME_COST_METHOD_KEYS,
        output_file=output_file,
        show_legend=show_legend,
    )


def a_2_plot_basic_window_size_time_cost(basic_path, default_budget, metric, output_file,
                                         show_legend=None):
    """a_2：时间开销随窗口大小 w 变化（固定隐私预算），主实验方案

    对应 a_2_plotBasicWindowSizeTimeCost.m
    """
    return plot_metric_change_for_single_ds(
        basic_path=basic_path,
        x_type="window_size",
        fixed_value=default_budget,
        metric=metric,
        method_keys=config.TIME_COST_METHOD_KEYS,
        output_file=output_file,
        show_legend=show_legend,
    )


def a_3_plot_basic_main_budget_change_for_single_ds(basic_path, default_window_size, metric,
                                                    output_file, show_legend=None):
    """a_3：给定指标（误差/散度）随隐私预算 ε 变化，主实验方案

    对应 a_3_plotBasicMainBudgetChangeInfluenceGivenMetricForSingleDS.m
    """
    return plot_metric_change_for_single_ds(
        basic_path=basic_path,
        x_type="budget",
        fixed_value=default_window_size,
        metric=metric,
        method_keys=config.MAIN_METHOD_KEYS,
        output_file=output_file,
        show_legend=show_legend,
    )


def a_4_plot_basic_main_w_change_for_single_ds(basic_path, default_budget, metric,
                                               output_file, show_legend=None):
    """a_4：给定指标（误差/散度）随窗口大小 w 变化，主实验方案

    对应 a_4_plotBasicMainWChangeInfluenceGivenMetricForSingleDS.m
    """
    return plot_metric_change_for_single_ds(
        basic_path=basic_path,
        x_type="window_size",
        fixed_value=default_budget,
        metric=metric,
        method_keys=config.MAIN_METHOD_KEYS,
        output_file=output_file,
        show_legend=show_legend,
    )


def a_5_plot_basic_ablate_budget_change_for_single_ds(basic_path, default_window_size, metric,
                                                      output_file, show_legend=None):
    """a_5：给定指标随隐私预算 ε 变化，消融实验方案

    对应 a_5_plotBasicAblateBudgetChangeInfluenceGivenMetricForSingleDS.m
    """
    return plot_metric_change_for_single_ds(
        basic_path=basic_path,
        x_type="budget",
        fixed_value=default_window_size,
        metric=metric,
        method_keys=config.ABLATION_METHOD_KEYS,
        output_file=output_file,
        show_legend=show_legend,
    )


def a_6_plot_basic_ablate_w_change_for_single_ds(basic_path, default_budget, metric,
                                                 output_file, show_legend=None):
    """a_6：给定指标随窗口大小 w 变化，消融实验方案

    对应 a_6_plotBasicAblateWChangeInfluenceGivenMetricForSingleDS.m
    """
    return plot_metric_change_for_single_ds(
        basic_path=basic_path,
        x_type="window_size",
        fixed_value=default_budget,
        metric=metric,
        method_keys=config.ABLATION_METHOD_KEYS,
        output_file=output_file,
        show_legend=show_legend,
    )
