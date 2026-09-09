# -*- coding: utf-8 -*-
"""绘图样式与图片保存工具

对应 MATLAB 中的两类操作：
    1) set(gca, 'FontName', 'Times New Roman', 'FontSize', ...)  ->  本文件的样式设置函数
    2) export_fig(fig, outputFileName, '-pdf', '-r256', '-transparent')  ->  save_figure
"""

import os
import sys

# 保证可以直接以脚本方式运行（python xxx.py）时也能找到 config / utils 包
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import matplotlib
# 使用无界面后端，保证在服务器 / 无 GUI 环境下也能出图
matplotlib.use("Agg")

import matplotlib.pyplot as plt  # noqa: E402  （必须在 matplotlib.use 之后导入）
from matplotlib.lines import Line2D  # noqa: E402

import config  # noqa: E402


def setup_matplotlib():
    """全局字体与数学字体设置（对应 MATLAB 的 'FontName','Times New Roman'）

    MATLAB 中 xlabel 用了 latex 解释器（'Interpreter','latex'），
    这里用 matplotlib 的 mathtext（fontset='stix'）渲染 $\\epsilon$ / $w$，效果接近 Times。
    """
    plt.rcParams["font.family"] = "serif"
    plt.rcParams["font.serif"] = [config.FONT_FAMILY, config.FONT_FAMILY_FALLBACK]
    plt.rcParams["mathtext.fontset"] = "stix"
    plt.rcParams["axes.unicode_minus"] = False


def new_figure():
    """创建画布（对应 MATLAB 的 fig = figure; hold on;）"""
    fig, ax = plt.subplots(figsize=config.FIG_SIZE)
    return fig, ax


def set_axis_labels(ax, x_label, y_label):
    """设置坐标轴标签（对应 set(get(gca,'XLabel'),...) 与 xlabel/ylabel）"""
    ax.set_xlabel(
        x_label,
        fontsize=config.FIGURE_FONT_SIZE_X,
        fontname=config.FONT_FAMILY,
        fontfamily="serif",
    )
    ax.set_ylabel(
        y_label,
        fontsize=config.FIGURE_FONT_SIZE_Y,
        fontname=config.FONT_FAMILY,
        fontfamily="serif",
    )


def style_axes(ax):
    """设置刻度字号与字体（对应 set(gca,'FontName',...,'FontSize',figure_FontSize)）"""
    ax.tick_params(labelsize=config.FIGURE_FONT_SIZE)
    for label in ax.get_xticklabels() + ax.get_yticklabels():
        label.set_fontname(config.FONT_FAMILY)
        label.set_fontfamily("serif")


def plot_one_line(ax, x_list, y_list, method_key):
    """画一条折线（对应 MATLAB 的 plot(x, y, 'ks-', 'LineWidth', 2, 'MarkerSize', 20)）

    :param ax: 坐标轴
    :param x_list: x 轴数据
    :param y_list: y 轴数据
    :param method_key: 方案 key，用于取线型、颜色与图例名
    :return: matplotlib 的 Line2D 对象（用于生成图例）
    """
    style = config.METHOD_STYLES[method_key]
    line, = ax.plot(
        x_list,
        y_list,
        color=style["color"],
        marker=style["marker"],
        linestyle=style["linestyle"],
        linewidth=config.LINE_WIDTH,
        markersize=config.FIGURE_MARKER_SIZE,
        label=method_display_name(method_key),
    )
    return line


def method_display_name(method_key):
    """取方案在图例中显示的名字（统一入口，改方法名只需改 config.METHOD_DISPLAY_NAMES）"""
    return config.METHOD_DISPLAY_NAMES.get(method_key, method_key)


def add_legend(ax, handles=None, show=None):
    """添加图例

    MATLAB 各绘图脚本中先 legend(...) 再 legend('off')，
    即"生成图例句柄但不显示"，图例由 bar_main/bar_ablation 单独导出，
    因此这里默认不显示（由 config.SHOW_LEGEND_IN_FIGURE 控制）。

    :param handles: 图例句柄列表；为 None 时自动从 ax 中获取
    :param show: 是否显示；为 None 时使用 config.SHOW_LEGEND_IN_FIGURE
    :return: 图例对象或 None
    """
    if show is None:
        show = config.SHOW_LEGEND_IN_FIGURE
    if not show:
        return None
    legend = ax.legend(
        handles=handles,
        loc=config.LEGEND_LOCATION,
        fontsize=config.LEGEND_FONT_SIZE_IN_FIG,
        frameon=True,
    )
    for text in legend.get_texts():
        text.set_fontname(config.FONT_FAMILY)
        text.set_fontfamily("serif")
    return legend


def save_figure(fig, output_file, dpi=None, transparent=None, tight=True):
    """保存图片（对应 export_fig(fig, outputFileName, '-pdf', '-r256', '-transparent')）

    :param fig: 画布
    :param output_file: 输出路径（无扩展名时自动补 config.OUTPUT_EXT）
    :param dpi: 分辨率，默认 config.OUTPUT_DPI
    :param transparent: 是否透明背景，默认 config.OUTPUT_TRANSPARENT
    :param tight: 是否去除白边（对应 export_fig 的默认裁剪行为）
    """
    dpi = config.OUTPUT_DPI if dpi is None else dpi
    transparent = config.OUTPUT_TRANSPARENT if transparent is None else transparent

    output_file = str(output_file)
    root, ext = os.path.splitext(output_file)
    if not ext:
        ext = config.OUTPUT_EXT
        output_file = root + ext

    parent = os.path.dirname(output_file)
    if parent:
        os.makedirs(parent, exist_ok=True)

    fig.savefig(
        output_file,
        dpi=dpi,
        transparent=transparent,
        bbox_inches="tight" if tight else None,
        format=ext.lstrip("."),
    )
    plt.close(fig)
    return output_file


def create_legend_only(method_keys, output_file, ncol=None, font_size=None, marker_size=None):
    """单独导出只包含图例的图片（对应 bar_main.m / bar_ablation.m）

    MATLAB 中先画一堆 Visible='off' 的曲线，再用
    legend(..., 'Location','northoutside', 'Orientation','horizontal') 生成图例，
    最后 axis off，得到一张"纯图例"图片。这里直接构造图例句柄，效果等价。

    :param method_keys: 方案中 key 列表（顺序即图例顺序）
    :param output_file: 输出路径
    :param ncol: 图例列数（控制换行）
    :param font_size: 图例字号
    :param marker_size: 图例中标记的大小
    """
    font_size = config.LEGEND_FONT_SIZE_ONLY if font_size is None else font_size
    marker_size = config.LEGEND_ONLY_MARKER_SIZE if marker_size is None else marker_size
    if ncol is None:
        ncol = len(method_keys)

    handles = []
    for method_key in method_keys:
        style = config.METHOD_STYLES[method_key]
        handles.append(
            Line2D(
                [], [],
                color=style["color"],
                marker=style["marker"],
                linestyle=style["linestyle"],
                linewidth=config.LINE_WIDTH,
                markersize=marker_size,
                label=method_display_name(method_key),
            )
        )

    fig = plt.figure(figsize=(max(3, ncol * 2.6), 1.2))
    legend = fig.legend(
        handles=handles,
        loc="upper center",
        ncol=ncol,
        frameon=False,
        fontsize=font_size,
    )
    for text in legend.get_texts():
        text.set_fontname(config.FONT_FAMILY)
        text.set_fontfamily("serif")
        text.set_color("black")

    return save_figure(fig, output_file)
