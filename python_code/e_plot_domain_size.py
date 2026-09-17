#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""维度可扩展性实验绘图：误差随取值域大小 d 变化

与 MATLAB / 其他画图脚本的关系：
    b_* / c_* 是按"隐私预算 epsilon"或"窗口大小 w"扫描；
    本脚本是维度实验专用，x 轴为取值域大小 d（对数刻度）。

用法（在 python_code 目录下执行）：

    # 1) 画全部数据集（tlns / sin / log）的 AMSE 与 AJSD
    python3 e_plot_domain_size.py \
        -i <维度实验结果目录> \
        -o <图片输出目录>

    # 2) 只画 sin，输出 png，图内显示图例
    python3 e_plot_domain_size.py -i <结果> -o <输出> -d sin --ext .png --show-legend

    # 3) 额外导出图例
    python3 e_plot_domain_size.py -i <结果> -o <输出> --export-legend

输入目录结构（由 deployment/_4_domain_size_scripts/*.sh 产出）：
    <维度实验结果目录>/
        3.tlns_result_d2/ ... 3.tlns_result_d50/
        4.sin_result_d2/  ... 4.sin_result_d50/
        5.log_result_d2/  ... 5.log_result_d50/
        每个目录下是参数目录（fixed 标签下只有一个，形如 p_1-5_w_40）
"""

import argparse
import math
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import config  # noqa: E402
from utils import plot_utils  # noqa: E402
from utils.dir_utils import list_dir_name  # noqa: E402
from utils.result_reader import read_result_table, resolve_col_index  # noqa: E402

# (结果目录名前缀, 输出文件名中使用的数据集名)
DATASET_LIST = [
    ("3.tlns_result", "tlns"),
    ("4.sin_result", "sin"),
    ("5.log_result", "log"),
]

# 从形如 "4.sin_result_d50" 的目录名中取出 d
DOMAIN_PATTERN = re.compile(r"_d(\d+)$")


def get_domain_size(dir_name):
    match = DOMAIN_PATTERN.search(str(dir_name))
    if match is None:
        return None
    return int(match.group(1))


def find_domain_dirs(domain_root, dataset_prefix):
    """找出某数据集下所有 {prefix}_d{N} 目录，返回 [(d, 目录路径), ...]（按 d 升序）"""
    result = []
    for name in list_dir_name(domain_root):
        if not name.startswith(dataset_prefix):
            continue
        domain_size = get_domain_size(name)
        if domain_size is None:
            continue
        result.append((domain_size, os.path.join(str(domain_root), name)))
    result.sort(key=lambda item: item[0])
    return result


def read_value_map(result_file, metric):
    """读 result.txt，返回 {Name列的值: 该行指标列的字符串}"""
    header, rows = read_result_table(result_file)
    if not rows:
        return None
    col_index = resolve_col_index(header, metric)
    if col_index is None:
        return None
    value_map = {}
    for row in rows:
        if row and col_index < len(row):
            value_map[row[0]] = row[col_index]
    return value_map


def transform(value, metric):
    """按指标配置做缩放 / 取对数"""
    shrink_ratio = metric.get("shrink_ratio", 1.0)
    if metric.get("whether_log", False):
        if value <= 0:
            return None
        return math.log(value) * shrink_ratio
    return value * shrink_ratio


def collect_series(domain_root, dataset_prefix, method_keys, metric):
    """返回 (d_list, {method_key: [值, ...]})"""
    d_list = []
    series = {key: [] for key in method_keys}
    for domain_size, dir_path in find_domain_dirs(domain_root, dataset_prefix):
        param_dirs = list_dir_name(dir_path)
        if not param_dirs:
            continue
        # fixed 标签下只有一个参数目录
        result_file = os.path.join(dir_path, param_dirs[0], "result.txt")
        if not os.path.isfile(result_file):
            print("[WARN] 未找到结果文件，跳过 d=%d：%s" % (domain_size, result_file))
            continue
        value_map = read_value_map(result_file, metric)
        if value_map is None:
            print("[WARN] 读取失败，跳过 d=%d：%s" % (domain_size, result_file))
            continue

        valid = True
        values = {}
        for key in method_keys:
            raw = None
            for name, value in value_map.items():
                if config.NAME_TO_METHOD_KEY.get(name) == key:
                    raw = value
                    break
            if raw is None:
                valid = False
                break
            try:
                number = float(raw)
            except ValueError:
                valid = False
                break
            temp = transform(number, metric)
            if temp is None:
                valid = False
                break
            values[key] = temp
        if not valid:
            print("[WARN] 方案数据缺失，跳过 d=%d" % domain_size)
            continue

        d_list.append(domain_size)
        for key in method_keys:
            series[key].append(values[key])
    return d_list, series


def plot_one_dataset(domain_root, output_dir, dataset_prefix, dataset_name,
                     method_keys, metric, show_legend):
    d_list, series = collect_series(domain_root, dataset_prefix, method_keys, metric)
    if len(d_list) < 2:
        print("[WARN] 数据点不足（%d 个），跳过：%s - %s"
              % (len(d_list), dataset_name, metric["display_name"]))
        return None

    plot_utils.setup_matplotlib()
    fig, ax = plot_utils.new_figure()

    y_label = ("ln(" + metric["display_name"] + ")"
               if metric.get("whether_log", False) else metric["display_name"])
    plot_utils.set_axis_labels(ax, config.X_LABEL_DOMAIN_SIZE, y_label)

    handles = []
    for key in method_keys:
        handles.append(plot_utils.plot_one_line(ax, d_list, series[key], key))

    # x 轴为对数刻度（d 按 2/5/10/25/50 这类近似等比取值）
    ax.set_xscale("log")
    ax.set_xticks(d_list)
    ax.set_xticklabels([str(item) for item in d_list])
    plot_utils.style_axes(ax)
    ax.set_xlim(d_list[0], d_list[-1])

    plot_utils.add_legend(ax, handles=handles, show=show_legend)

    output_file = os.path.join(
        str(output_dir), config.OUTPUT_SUB_DIR_ADD, "domain_size",
        "%s_domain_change_%s" % (dataset_name, metric["display_name"]))
    return plot_utils.save_figure(fig, output_file)


def parse_args(argv=None):
    parser = argparse.ArgumentParser(
        description="维度可扩展性实验绘图（误差随取值域大小 d 变化）",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument("-i", "--input-basic-path", required=True,
                        help="维度实验结果根目录（其下含 4.sin_result_d2 等目录）")
    parser.add_argument("-o", "--output-basic-path", required=True,
                        help="图片输出根目录")
    parser.add_argument("-d", "--datasets", default="tlns,sin,log",
                        help="要画的数据集，逗号分隔，可选：tlns,sin,log")
    parser.add_argument("-m", "--methods", default="main",
                        help="画哪些方案：main（主实验清单）或 ablation（消融清单）")
    parser.add_argument("--ext", default=config.OUTPUT_EXT,
                        help="输出图片扩展名，如 .pdf / .png")
    parser.add_argument("--show-legend", action="store_true",
                        help="在图内显示图例（默认不显示，与其他图一致）")
    parser.add_argument("--export-legend", action="store_true",
                        help="额外单独导出一张图例图片")
    return parser.parse_args(argv)


def main(argv=None):
    args = parse_args(argv)
    config.OUTPUT_EXT = args.ext

    method_keys = (config.ABLATION_METHOD_KEYS
                   if args.methods.lower().startswith("ab")
                   else config.MAIN_METHOD_KEYS)

    wanted = [item.strip() for item in args.datasets.split(",") if item.strip()]
    outputs = []
    for dataset_prefix, dataset_name in DATASET_LIST:
        if dataset_name not in wanted:
            continue
        for metric in (config.METRIC_AMSE, config.METRIC_AJSD):
            result = plot_one_dataset(
                args.input_basic_path, args.output_basic_path,
                dataset_prefix, dataset_name, method_keys, metric,
                True if args.show_legend else None)
            if result:
                outputs.append(result)

    if args.export_legend:
        plot_utils.setup_matplotlib()
        legend_file = os.path.join(
            str(args.output_basic_path), config.OUTPUT_SUB_DIR_ADD,
            "domain_size", "legend_domain_size")
        result = plot_utils.create_legend_only(method_keys, legend_file)
        if result:
            outputs.append(result)

    for item in outputs:
        print("        -> %s" % item)
    print("[DONE] 维度可扩展性图绘制完成")
    return 0


if __name__ == "__main__":
    sys.exit(main())
