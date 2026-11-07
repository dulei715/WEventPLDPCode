# -*- coding: utf-8 -*-
"""绘制 group shift 的组级加权指标图，风格与主实验图一致。

数据来源
--------
aggregate_group_shift_metrics.sh 生成的 group_shift_paper_summary.csv，
每个 (direction, delta, method) 一行，指标列为
    weighted_amse_mean / weighted_amse_std
    weighted_ajsd_mean / weighted_ajsd_std
其中 std 是 10 轮之间的标准差，本脚本把它画成误差棒。

与旧脚本的区别
--------------
deployment/_2_combine_scripts/plot_group_shift_metrics.py 自带一套
matplotlib 默认样式（DejaVu Sans、去上右边框、网格、bbox_inches='tight'），
与 python_code 画出的主实验图不一致。本脚本改为复用 config.py +
utils/plot_utils.py：Times New Roman / mathtext cm、METHOD_STYLES 的
线型标记、空心 marker、固定画布尺寸、256 dpi transparent 导出、图内不放图例。

另外，config.METHOD_STYLES 里 4 个 Plus 方案同为 tab:red，只靠线型区分。
本脚本用 WEIGHTED_METHOD_COLORS 做**局部**颜色覆盖，仅作用于组级加权图
（含单独导出的图例），不修改 config，也不影响主实验图与消融图。

用法
----
python g_plot_group_shift_weighted.py \
    --summary <.../aggregated/group_shift_paper_summary.csv> \
    --output-dir <.../add-result/aggregated/figures>

默认输出 4 张单图（2 指标 × positive/reverse），一个 PDF 只含一张图。
--combined     可选：改回 2 张双栏图（Positive / Reverse 并排于同一 PDF）
--legend-only  只导出纯图例图片（图内始终不画图例）
--methods      覆盖默认绘制的方案，取 config 中的 method key
"""

import argparse
import csv
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import config  # noqa: E402
from utils import plot_utils  # noqa: E402
import matplotlib.pyplot as plt  # noqa: E402
from matplotlib.lines import Line2D  # noqa: E402


METRICS = {
    "weighted_amse": "Weighted AMSE",
    "weighted_ajsd": "Weighted AJSD",
}

# 默认画 4 个完整增强方案（与旧脚本一致），可用 --methods 覆盖
DEFAULT_METHOD_KEYS = [
    "plus_plpa_pre", "plus_plpa_post",
    "plus_plpd_pre", "plus_plpd_post",
]

# 仅本脚本使用的配色，不改 config.METHOD_STYLES。
# 用 Okabe-Ito 色板（色盲友好），让 4 条 Plus 曲线可以直接按颜色区分。
WEIGHTED_METHOD_COLORS = {
    "plus_plpa_pre": "#0072B2",   # 蓝
    "plus_plpa_post": "#D55E00",  # 朱红
    "plus_plpd_pre": "#009E73",   # 绿
    "plus_plpd_post": "#CC79A7",  # 紫红
}
# --methods 传入其它方案时按顺序取用
WEIGHTED_COLOR_PALETTE = [
    "#0072B2", "#D55E00", "#009E73", "#CC79A7",
    "#56B4E9", "#E69F00", "#F0E442", "#000000",
]

# 误差棒样式（主图没有误差棒，这几个值只在本脚本使用）
ERROR_BAR_CAP_SIZE = 3
ERROR_BAR_LINE_WIDTH = 1.2
ERROR_BAR_CAP_THICK = 1.2

# 双栏模式下画布宽 = 两张主图并排，使每个子图的字号/线宽与主图一致。
# top 比主图的 AXES_ADJUST 低一些，给 Positive / Reverse 子图标题留出空间：
# 数据图固定尺寸导出（SAVE_TIGHT=False），标题超出画布会被裁掉。
COMBINED_ADJUST = dict(left=0.13, right=0.98, top=0.85, bottom=0.27, wspace=0.30)


def _color_of(method_key, index):
    """取组级图专用颜色；未登记的方案按 --methods 中的顺序分配。"""
    return WEIGHTED_METHOD_COLORS.get(
        method_key, WEIGHTED_COLOR_PALETTE[index % len(WEIGHTED_COLOR_PALETTE)]
    )


def load_rows(summary_path):
    with open(str(summary_path), "r", newline="") as handle:
        rows = list(csv.DictReader(handle))
    if not rows:
        raise SystemExit("汇总 CSV 为空：%s" % summary_path)
    return rows


def _series(rows, direction, metric, method_key):
    """取出某个方案在某个方向下按 delta 排序的 (x, y, yerr)。"""
    selected = [
        row for row in rows
        if row.get("direction") == direction
        and config.NAME_TO_METHOD_KEY.get(row.get("method")) == method_key
    ]
    selected.sort(key=lambda row: float(row["delta"]))
    x, y, err = [], [], []
    for row in selected:
        mean_value = row.get(metric + "_mean", "")
        if not mean_value:
            continue
        std_value = row.get(metric + "_std", "")
        x.append(float(row["delta"]))
        y.append(float(mean_value))
        err.append(float(std_value) if std_value else 0.0)
    return x, y, err


def _draw_lines(ax, rows, direction, metric, method_keys):
    for index, method_key in enumerate(method_keys):
        x, y, err = _series(rows, direction, metric, method_key)
        if not x:
            print("[WARN] 无数据：%s / %s / %s" % (direction, metric, method_key))
            continue
        style = config.METHOD_STYLES[method_key]
        ax.errorbar(
            x, y, yerr=err,
            color=_color_of(method_key, index),
            marker=style["marker"],
            linestyle=style["linestyle"],
            linewidth=config.LINE_WIDTH,
            markersize=config.FIGURE_MARKER_SIZE,
            markerfacecolor=config.MARKER_FACE_COLOR,
            markeredgewidth=config.MARKER_EDGE_WIDTH,
            elinewidth=ERROR_BAR_LINE_WIDTH,
            capsize=ERROR_BAR_CAP_SIZE,
            capthick=ERROR_BAR_CAP_THICK,
            label=plot_utils.method_display_name(method_key),
        )


def _decorate(ax, x_values, x_label, y_label):
    plot_utils.set_axis_labels(ax, x_label, y_label)
    plot_utils.style_axes(ax)
    if x_values:
        ax.set_xticks(x_values)
        if len(x_values) >= 2:
            # δ 的刻度标签是 "0.00" / "0.20"（4 字符），28pt 字号下若把 xlim
            # 顶到两端，最右标签会被固定尺寸导出裁掉，故留少量边距。
            span = x_values[-1] - x_values[0]
            margin = span * 0.04
            ax.set_xlim(x_values[0] - margin, x_values[-1] + margin)
    # 图内不放图例，与 config.SHOW_LEGEND_IN_FIGURE=False 保持一致
    plot_utils.add_legend(ax, show=False)


def _save(fig, output_dir, stem):
    """按 plot_utils.save_figure 的参数导出 PDF，并额外给一份 PNG 便于预览。

    不直接复用 save_figure：它会 plt.close(fig)，导致无法再导出第二种格式。
    """
    output_dir = str(output_dir)
    os.makedirs(output_dir, exist_ok=True)
    root = os.path.join(output_dir, stem)
    for ext in (".pdf", ".png"):
        fig.savefig(
            root + ext,
            dpi=config.OUTPUT_DPI,
            transparent=config.OUTPUT_TRANSPARENT,
            bbox_inches=None,
            pad_inches=0.1,
            format=ext.lstrip("."),
        )
    plt.close(fig)
    return root + config.OUTPUT_EXT


def plot_single(rows, metric, y_label, method_keys, output_dir):
    """默认布局：每个方向一张单图，一个 PDF 只含一张图，布局完全等同主实验图。"""
    outputs = []
    for direction in config.GROUP_SHIFT_DIRECTION_LIST:
        fig, ax = plot_utils.new_figure()
        _draw_lines(ax, rows, direction, metric, method_keys)
        _decorate(ax, config.GROUP_SHIFT_DELTA_LIST,
                  config.X_LABEL_GROUP_SHIFT, y_label)
        outputs.append(_save(
            fig, output_dir, "group_shift_%s_%s" % (metric, direction)
        ))
    return outputs


def plot_combined(rows, metric, y_label, method_keys, output_dir):
    """可选布局（--combined）：一个指标一张图，Positive / Reverse 并排。"""
    fig, axes = plt.subplots(
        1, 2, sharey=True,
        figsize=(config.FIG_SIZE[0] * 2, config.FIG_SIZE[1]),
    )
    fig.subplots_adjust(**COMBINED_ADJUST)
    for index, direction in enumerate(config.GROUP_SHIFT_DIRECTION_LIST):
        ax = axes[index]
        _draw_lines(ax, rows, direction, metric, method_keys)
        _decorate(ax, config.GROUP_SHIFT_DELTA_LIST,
                  config.X_LABEL_GROUP_SHIFT,
                  y_label if index == 0 else "")
        ax.set_title(
            direction.capitalize(),
            fontsize=config.FIGURE_FONT_SIZE_Y,
            fontname=config.FONT_FAMILY,
            fontfamily="serif",
        )
    return _save(fig, output_dir, "group_shift_" + metric)


def draw_weighted_legend(output_dir, method_keys,
                         file_name="legend_group_shift_weighted"):
    """单独导出纯图例，颜色与组级图一致。

    不能直接用 plot_utils.create_legend_only：它固定从
    config.METHOD_STYLES 取颜色，会与本脚本的局部配色不一致。
    """
    plot_utils.setup_matplotlib()
    ncol = 2
    handles = []
    for index, method_key in enumerate(method_keys):
        style = config.METHOD_STYLES[method_key]
        handles.append(Line2D(
            [], [],
            color=_color_of(method_key, index),
            marker=style["marker"],
            linestyle=style["linestyle"],
            linewidth=config.LEGEND_ONLY_LINE_WIDTH,
            markersize=config.LEGEND_ONLY_MARKER_SIZE,
            markerfacecolor=config.MARKER_FACE_COLOR,
            markeredgewidth=config.LEGEND_ONLY_MARKER_EDGE_WIDTH,
            label=plot_utils.method_display_name(method_key),
        ))

    nrow = max(1, int(math.ceil(len(handles) / float(ncol))))
    fig = plt.figure(figsize=(
        max(3.0, ncol * config.LEGEND_ONLY_WIDTH_PER_COL),
        max(1.0, nrow * config.LEGEND_ONLY_ROW_HEIGHT),
    ))
    legend = fig.legend(
        handles=handles,
        loc="upper center",
        ncol=ncol,
        frameon=False,
        fontsize=config.LEGEND_FONT_SIZE_ONLY,
        columnspacing=config.LEGEND_COLUMN_SPACING,
        handletextpad=config.LEGEND_HANDLE_TEXT_PAD,
        borderpad=config.LEGEND_BORDER_PAD,
        handlelength=config.LEGEND_HANDLE_LENGTH,
    )
    for text in legend.get_texts():
        text.set_fontname(config.FONT_FAMILY)
        text.set_fontfamily("serif")
        text.set_color("black")

    output_file = os.path.join(str(output_dir), file_name)
    return plot_utils.save_figure(fig, output_file, tight=True)


def main():
    parser = argparse.ArgumentParser(
        description="用主实验图样式绘制 group shift 组级加权指标图")
    parser.add_argument("--summary", default=None,
                        help="group_shift_paper_summary.csv 路径")
    parser.add_argument("--output-dir", required=True, help="图片输出目录")
    parser.add_argument("--combined", action="store_true",
                        help="改为输出 2 张双栏图（Positive / Reverse 并排于同一 PDF）")
    parser.add_argument("--legend-only", action="store_true",
                        help="只导出纯图例图片")
    parser.add_argument("--methods", nargs="*", default=None,
                        help="覆盖默认方案列表（config 中的 method key）")
    args = parser.parse_args()

    method_keys = args.methods or DEFAULT_METHOD_KEYS
    unknown = [key for key in method_keys if key not in config.METHOD_STYLES]
    if unknown:
        parser.error("未知 method key：%s" % unknown)

    plot_utils.setup_matplotlib()

    if args.legend_only:
        print(draw_weighted_legend(args.output_dir, method_keys))
        return
    if not args.summary:
        parser.error("绘图时需要通过 --summary 指定汇总 CSV")

    rows = load_rows(args.summary)
    for metric, y_label in METRICS.items():
        if args.combined:
            outputs = [plot_combined(rows, metric, y_label, method_keys, args.output_dir)]
        else:
            outputs = plot_single(rows, metric, y_label, method_keys, args.output_dir)
        for path in outputs:
            print("-> " + path)


if __name__ == "__main__":
    main()
