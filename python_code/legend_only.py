# -*- coding: utf-8 -*-
"""单独导出"纯图例"图片（对应 matlab_code/experiment_result 中的 bar_main.m / bar_ablation.m）

MATLAB 中的做法：
    bar_main.m     : 画 6 条 Visible='off' 的曲线，用 legend(..., 'northoutside', 'horizontal')
                     生成图例后 axis off，得到一张只含主实验图例的图片；
    bar_ablation.m : 同理，只是图例项为 8 个消融方案。

Python 版不需要真的画曲线，直接构造图例句柄即可（见 utils.plot_utils.create_legend_only）。
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import config  # noqa: E402
from utils import plot_utils  # noqa: E402
from utils.dir_utils import build_output_path  # noqa: E402


def draw_main_legend(output_basic_path, file_name="legend_main"):
    """导出主实验图例（对应 bar_main.m）

    :param output_basic_path: 输出根目录
    :param file_name: 输出文件名（不含扩展名）
    :return: 输出文件路径
    """
    output_file = build_output_path(output_basic_path, config.OUTPUT_SUB_DIR_FIGURES, file_name)
    plot_utils.setup_matplotlib()
    return plot_utils.create_legend_only(
        config.MAIN_METHOD_KEYS,
        output_file,
        ncol=config.LEGEND_ONLY_NCOL_MAIN,
    )


def draw_ablation_legend(output_basic_path, file_name="legend_ablation"):
    """导出去消融实验图例（对应 bar_ablation.m）

    :param output_basic_path: 输出根目录
    :param file_name: 输出文件名（不含扩展名）
    :return: 输出文件路径
    """
    output_file = build_output_path(output_basic_path, config.OUTPUT_SUB_DIR_FIGURES, file_name)
    plot_utils.setup_matplotlib()
    return plot_utils.create_legend_only(
        config.ABLATION_METHOD_KEYS,
        output_file,
        ncol=config.LEGEND_ONLY_NCOL_ABLATION,
    )

def draw_ablation_legend_single_column(output_basic_path, file_name="legend_ablation_single_column"):
    """导出消融实验图例（每行 4 个，12 条排成 4 列 × 3 行，画布按图例实际宽度收缩、不留白）

    与 draw_ablation_legend 的唯一区别是列数与去白边，图例项、字号、标记样式完全一致。

    :param output_basic_path: 输出根目录
    :param file_name: 输出文件名（不含扩展名）
    :return: 输出文件路径
    """
    output_file = build_output_path(output_basic_path, config.OUTPUT_SUB_DIR_FIGURES, file_name)
    plot_utils.setup_matplotlib()
    return plot_utils.create_legend_only(
        config.ABLATION_METHOD_KEYS,
        output_file,
        ncol=config.LEGEND_ONLY_NCOL_ABLATION_SINGLE,
        # fit_width=True,
    )

def draw_ablation_legend_horizontal(output_basic_path, file_name="legend_ablation_horizontal"):
    """导出消融实验图例（4 列 × 3 行，按行横向阅读）

    matplotlib 图例默认按列填充，这里把方案顺序重排后再交给 create_legend_only，
    使其显示为“先填满一行再换行”的横向排列。不修改任何已有函数。

    :param output_basic_path: 输出根目录
    :param file_name: 输出文件名（不含扩展名）
    :return: 输出文件路径
    """
    ncol = config.LEGEND_ONLY_NCOL_ABLATION_SINGLE
    keys = config.ABLATION_METHOD_KEYS
    nrow = -(-len(keys) // ncol)  # 向上取整，12/4 = 3
    # 列填充下，输入下标 k 落在 (row=k%nrow, col=k//nrow)；
    # 要让该位置显示“行优先”顺序的第 (row*ncol+col) 项，故按此重排输入列表。
    ordered = [keys[(k % nrow) * ncol + (k // nrow)] for k in range(len(keys))]

    output_file = build_output_path(output_basic_path, config.OUTPUT_SUB_DIR_FIGURES, file_name)
    plot_utils.setup_matplotlib()
    return plot_utils.create_legend_only(ordered, output_file, ncol=ncol)


def draw_group_shift_legend(output_basic_path, file_name="legend_group_shift"):
    """单独导出 group shift 实验图例（方案集合见 config.GROUP_SHIFT_METHOD_KEYS）。"""
    output_file = build_output_path(
        output_basic_path, config.OUTPUT_SUB_DIR_FIGURES, file_name
    )
    plot_utils.setup_matplotlib()
    return plot_utils.create_legend_only(
        config.GROUP_SHIFT_METHOD_KEYS,
        output_file,
        ncol=config.LEGEND_ONLY_NCOL_MAIN,
    )


if __name__ == "__main__":
    # 简单用法：python legend_only.py <output_basic_path>
    output_path = sys.argv[1] if len(sys.argv) > 1 else config.OUTPUT_BASIC_PATH
    print(draw_main_legend(output_path))
    print(draw_ablation_legend(output_path))
