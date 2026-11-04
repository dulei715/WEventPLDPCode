# -*- coding: utf-8 -*-
"""隐私参数与数据分布相关实验的绘图。

Java 侧用同一组高/低隐私用户参数生成不同相关强度 delta 的合成数据，
结果目录格式为：
    4.sin_result_positive_delta0-10
    4.sin_result_reverse_delta0-10

这里沿用主实验的方案映射、指标读取和绘图样式，只把横轴换成相关强度 delta。
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import config  # noqa: E402
import a_plot_single_ds as plot_a  # noqa: E402
from utils.dir_utils import list_dir_name, build_output_path  # noqa: E402
from utils.result_reader import (  # noqa: E402
    get_metric_value,
    read_result_table,
    transform_value,
)


def _extract_delta(dir_name, direction):
    prefix = "%s_%s_delta" % (config.GROUP_SHIFT_DATASET_DIR, direction)
    if not dir_name.startswith(prefix):
        return None
    value = dir_name[len(prefix):]
    if not value:
        return None
    try:
        return float(value.replace("-", "."))
    except ValueError:
        return None


def _find_result_file(dataset_path):
    for parameter_dir in list_dir_name(dataset_path):
        result_file = os.path.join(dataset_path, parameter_dir, "result.txt")
        if os.path.isfile(result_file):
            return result_file
    return None


def collect_group_shift_metric_series(input_basic_path, direction, metric, method_keys):
    records = []
    for dir_name in list_dir_name(input_basic_path):
        delta = _extract_delta(dir_name, direction)
        if delta is None:
            continue
        result_file = _find_result_file(os.path.join(str(input_basic_path), dir_name))
        if result_file is None:
            continue

        table = read_result_table(result_file)
        y_values = {}
        valid = True
        for method_key in method_keys:
            raw_value = get_metric_value(
                table,
                method_key,
                metric,
                config.RESULT_ROW_KEYS,
                config.FIRST_DATA_ROW,
                config.NAME_TO_METHOD_KEY,
                config.MATCH_METHOD_BY_NAME,
            )
            value = transform_value(raw_value, metric)
            if value is None:
                valid = False
                break
            y_values[method_key] = value
        if valid:
            records.append((delta, y_values))

    records.sort(key=lambda item: item[0])
    x_list = [item[0] for item in records]
    y_series = {key: [item[1][key] for item in records] for key in method_keys}
    return x_list, y_series


def plot_group_shift_for_single_direction(input_basic_path, direction, metric,
                                          output_file, show_legend=None):
    x_list, y_series = collect_group_shift_metric_series(
        input_basic_path, direction, metric, config.MAIN_METHOD_KEYS
    )
    if not x_list:
        print("[WARN] 未读取到 group shift 结果，跳过绘图：%s (%s)" %
              (input_basic_path, direction))
        return None
    return plot_a.draw_lines_for_single_ds(
        x_list,
        y_series,
        config.MAIN_METHOD_KEYS,
        output_file,
        config.X_LABEL_GROUP_SHIFT,
        plot_a.build_y_label(metric),
        show_legend,
    )


def f_1_plot_group_shift_for_error(input_basic_path, output_basic_path, show_legend=None):
    """画 AMSE/AJSD 随相关强度 delta 变化的 positive/reverse 两组曲线。"""
    outputs = []
    for direction in config.GROUP_SHIFT_DIRECTION_LIST:
        for metric in (config.METRIC_AMSE, config.METRIC_AJSD):
            output_file = build_output_path(
                output_basic_path,
                config.GROUP_SHIFT_OUTPUT_SUB_DIR,
                "%s_group_shift_%s_%s" % (
                    config.GROUP_SHIFT_OUTPUT_DATASET_NAME,
                    direction,
                    metric["display_name"],
                ),
            )
            result = plot_group_shift_for_single_direction(
                input_basic_path, direction, metric, output_file, show_legend
            )
            if result:
                outputs.append(result)
    return outputs


if __name__ == "__main__":
    f_1_plot_group_shift_for_error(
        config.GROUP_SHIFT_INPUT_BASIC_PATH,
        config.OUTPUT_BASIC_PATH,
    )
