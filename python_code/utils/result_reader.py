# -*- coding: utf-8 -*-
"""实验结果文件 result.txt 的读取

对应 MATLAB 中的：
    temp_table = readtable(data_path);
    temp_table{row, metric_col_index}

result.txt 由 Java 端 RepeatUtils 写出，格式为逗号分隔的 CSV：
    第 1 行：表头
    第 2 行起：各方案（第 1 个方案行是 NP，之后依次为待绘图的方案）
"""

import math
import os
import sys

# 保证可以直接以脚本方式运行（python xxx.py）时也能找到 config / utils 包
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from utils.dir_utils import list_dir_name, extract_budget_and_w_size_from_dir_name  # noqa: E402


def read_result_table(result_file):
    """读取 result.txt（对应 MATLAB 的 readtable）

    :param result_file: result.txt 的路径
    :return: (header, rows)
        header: 表头列表
        rows  : 数据行列表（每行是字符串列表）
        读取失败时返回 ([], [])
    """
    if not os.path.isfile(result_file):
        return [], []
    with open(result_file, "r", encoding="utf-8-sig", errors="ignore") as file:
        # 去掉空行与首尾空白
        lines = [line.strip() for line in file if line.strip()]
    if not lines:
        return [], []
    header = [item.strip() for item in lines[0].split(",")]
    rows = [[item.strip() for item in line.split(",")] for line in lines[1:]]
    return header, rows


def resolve_col_index(header, metric):
    """确定指标所在的列下标（0-based）

    优先按表头名称匹配（需要 metric["col_name"] 非空且能匹配上），
    否则使用 metric["col_index"]（1-based，与 MATLAB 完全一致）。

    :param header: 表头列表
    :param metric: config 中的指标字典
    :return: 0-based 列下标；无法确定时返回 None
    """
    col_name = metric.get("col_name") or ""
    if col_name and header:
        for index, name in enumerate(header):
            if name.lower() == col_name.lower():
                return index
    col_index = metric.get("col_index")
    if col_index is None:
        return None
    return int(col_index) - 1  # MATLAB 的列号是 1-based


def get_metric_value(table, method_key, metric, method_row_keys, first_data_row=2,
                     name_to_method_key=None, match_by_name=False):
    """取某个方案在指定指标上的原始数值（对应 temp_table{row, metric_col_index}）

    :param table: read_result_table 的返回值 (header, rows)
    :param method_key: 方案的内部 key，如 "lpd"
    :param metric: config 中的指标字典
    :param method_row_keys: config.RESULT_ROW_KEYS，方案行顺序
    :param first_data_row: 第一个方案行在 MATLAB 语义下的行号（默认 2）
    :param name_to_method_key: 方案名 -> key 的映射（按名匹配时使用）
    :param match_by_name: 是否按 "Name" 列匹配方案
    :return: 数值（float）；取不到时返回 None
    """
    header, rows = table
    if not rows:
        return None

    col_index = resolve_col_index(header, metric)
    if col_index is None:
        return None

    row = None
    if match_by_name and name_to_method_key and header:
        # 按 Name 列（第 1 列）匹配方案
        for candidate in rows:
            if candidate and candidate[0] in name_to_method_key:
                if name_to_method_key[candidate[0]] == method_key:
                    row = candidate
                    break
    if row is None and method_key in method_row_keys:
        # 按行号匹配：方案行号 -> Python 下标
        #   first_data_row 是"第一个可绘图方案"的行号（1-based，NP 行为 1），
        #   RESULT_ROW_KEYS 中的第 k 个方案对应行号 = first_data_row + k，
        #   而 rows 是去掉表头后的 0-based 列表（rows[0] 是 NP 行），故下标 = 行号 - 1。
        row_index = first_data_row + method_row_keys.index(method_key) - 1
        if 0 <= row_index < len(rows):
            row = rows[row_index]
    if row is None or col_index >= len(row):
        return None

    try:
        return float(row[col_index])
    except ValueError:
        return None


def build_row_report(table, expected_names=None, name_to_method_key=None,
                     method_row_keys=None, first_data_row=2):
    """生成"行号 -> 方案名 -> Python 取用的 key"对照报告（用于自检）

    把 result.txt 里每一行的实际 Name，与：
      1) 期望的清单（config.EXPECTED_ROW_NAMES）；
      2) 按行号定位得到的 key（RESULT_ROW_KEYS）；
      3) 按名字定位得到的 key（NAME_TO_METHOD_KEY）；
    做交叉比对，三者一致才说明绘图配置是对的。

    :param table: read_result_table 的返回值
    :param expected_names: 期望的方案名清单（按行）
    :param name_to_method_key: 名字 -> key 映射
    :param method_row_keys: 按行号定位时的 key 清单
    :param first_data_row: 第一个"可绘图方案"所在的行号（1-based，NP 行为 1）
    :return: 每项为 dict 的报告列表
    """
    _, rows = table
    report = []
    for index, row in enumerate(rows):
        row_no = index + 1                       # 数据行号：1 = NP
        actual_name = row[0] if row else ""
        expected_name = ""
        if expected_names and index < len(expected_names):
            expected_name = expected_names[index]

        key_by_row = ""
        if method_row_keys:
            key_index = row_no - first_data_row
            if 0 <= key_index < len(method_row_keys):
                key_by_row = method_row_keys[key_index]

        key_by_name = ""
        if name_to_method_key:
            key_by_name = name_to_method_key.get(actual_name, "") or ""

        consistent = None
        if key_by_row and key_by_name:
            consistent = (key_by_row == key_by_name)

        report.append({
            "row": row_no,
            "name": actual_name,
            "expected": expected_name,
            "name_matched": (actual_name == expected_name) if expected_name else None,
            "key_by_row": key_by_row,
            "key_by_name": key_by_name,
            "consistent": consistent,
        })
    return report


def transform_value(value, metric):
    """对原始值做缩放 / 取对数（对应 MATLAB 中的 log(...) * shrink_ratio）

    :param value: 原始数值
    :param metric: config 中的指标字典
    :return: 变换后的数值；value 无效时返回 None
    """
    if value is None:
        return None
    shrink_ratio = metric.get("shrink_ratio", 1.0)
    if metric.get("whether_log", False):
        if value <= 0:
            return None  # 对数要求正数，MATLAB 下会得到 -Inf/NaN，这里直接丢弃该点
        return math.log(value) * shrink_ratio
    return value * shrink_ratio


def collect_metric_series(basic_path, x_type, fixed_value, metric, method_keys,
                          method_row_keys, first_data_row=2,
                          name_to_method_key=None, match_by_name=False, sort_by_x=True):
    """扫描一个数据集目录下的所有参数目录，取出"某个指标随 x 变化"的曲线数据

    对应 a_1 ~ a_6 中读取数据的 for 循环部分：
        - 遍历 basic_path 下的参数目录（目录名形如 p_2-0_w_120）
        - 按 x_type 决定 x 轴取预算还是窗口大小，并固定另一个参数
        - 读出每个方案在该指标上的值

    与 MATLAB 的差异（有意为之的"变通"）：
        MATLAB 中 a_1/a_3/a_5 直接按 dir() 的遍历顺序画，未排序；
        这里统一按 x 轴数值升序排序，避免出现折线来回折返的问题。

    :param basic_path: 单个数据集的结果目录，如 ".../1.trajectory_result"
    :param x_type: "budget"（x 轴为隐私预算，固定窗口大小）或 "window_size"（反之）
    :param fixed_value: 被固定的那个参数的值
    :param metric: config 中的指标字典
    :param method_keys: 本次要绘制的方案 key 列表
    :param method_row_keys: config.RESULT_ROW_KEYS
    :param first_data_row: 第一个方案行在 MATLAB 语义下的行号
    :param name_to_method_key: 方案名 -> key 映射
    :param match_by_name: 是否按名字匹配方案
    :param sort_by_x: 是否按 x 轴数值升序排序
    :return: (x_list, {method_key: y_list})；无数据时 x_list 为空
    """
    records = []  # 每个元素为 (x_value, {method_key: y_value})
    for dir_name in list_dir_name(basic_path):
        budget, window_size = extract_budget_and_w_size_from_dir_name(dir_name)
        if budget is None or window_size is None:
            continue

        if x_type == "budget":
            x_value, fixed_current = budget, window_size
        else:
            x_value, fixed_current = window_size, budget

        # 浮点比较用容差，避免 0.6 != 0.6000000000000001 之类的问题
        if fixed_current is None or abs(float(fixed_current) - float(fixed_value)) > 1e-6:
            continue

        result_file = os.path.join(str(basic_path), dir_name, "result.txt")
        if not os.path.isfile(result_file):
            continue

        table = read_result_table(result_file)
        y_values = {}
        valid = True
        for method_key in method_keys:
            raw_value = get_metric_value(
                table, method_key, metric, method_row_keys, first_data_row,
                name_to_method_key, match_by_name,
            )
            value = transform_value(raw_value, metric)
            if value is None:
                valid = False
                break
            y_values[method_key] = value
        if valid:
            records.append((x_value, y_values))

    # 按 x 轴数值升序排序（MATLAB 的 a_2/a_4/a_6 用 sortrows，这里对所有情况统一排序）
    if sort_by_x:
        records.sort(key=lambda item: item[0])

    x_list = [item[0] for item in records]
    y_series = {key: [item[1][key] for item in records] for key in method_keys}
    return x_list, y_series
