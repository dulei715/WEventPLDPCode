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


if __name__ == "__main__":
    # 简单用法：python legend_only.py <output_basic_path>
    output_path = sys.argv[1] if len(sys.argv) > 1 else config.OUTPUT_BASIC_PATH
    print(draw_main_legend(output_path))
    print(draw_ablation_legend(output_path))
