# -*- coding: utf-8 -*-
"""目录与文件名解析工具

一一对应 matlab_code/experiment_result/utils 下的函数：
    list_dir_name.m                      -> list_dir_name
    extractBudgetAndWSizeFromDirName.m   -> extract_budget_and_w_size_from_dir_name
    extractRatioAndOthersFromDirName.m   -> extract_ratio_and_others_from_dir_name
    getParentAndFileName.m               -> get_parent_and_file_name
"""

import os
import re
import sys

# 保证可以直接以脚本方式运行（python xxx.py）时也能找到 config 模块
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import config  # noqa: E402


def list_dir_name(basic_path):
    """列出目录下的所有子目录名（对应 list_dir_name.m）

    与 MATLAB 的 dir() 不同，这里返回按名称排序后的结果，
    保证多次运行得到的顺序稳定（绘图时会再按 x 轴数值排序，因此顺序不影响结果）。

    :param basic_path: 根目录
    :return: 子目录名列表（不含 "." / ".."）
    """
    basic_path = str(basic_path)
    if not os.path.isdir(basic_path):
        return []
    return sorted(
        name for name in os.listdir(basic_path)
        if os.path.isdir(os.path.join(basic_path, name))
    )


def _to_number(text):
    """把形如 '2-0'、'0-5' 的字符串转成浮点数 2.0 / 0.5；失败返回 None

    说明：Java 端为避免路径中出现 '.'，把小数点写成了 '-'（见 ParameterGroupInitializeUtils.toPathName）。
    这里额外兼容 '_' 写法（如 p_0_5_w_120），否则 float('0_5') 会被解析成 5.0 而不是 0.5。
    """
    if text is None:
        return None
    try:
        return float(str(text).replace("-", ".").replace("_", "."))
    except ValueError:
        return None


def extract_budget_and_w_size_from_dir_name(dir_name):
    """从目录名中解析隐私预算与窗口大小（对应 extractBudgetAndWSizeFromDirName.m）

    目录名格式形如：p_2-0_w_20 / p_0-5_w_120
    其中 '-' 表示小数点（Java 端为避免路径中的 '.' 而做的替换）。

    :param dir_name: 目录名
    :return: (budget, window_size)；解析失败时对应项为 None
    """
    segments = str(dir_name).split("_")
    if len(segments) < 4:
        return None, None
    budget = _to_number(segments[1])          # 第 2 段：p 后面的预算值
    try:
        window_size = int(segments[3])        # 第 4 段：w 后面的窗口大小
    except ValueError:
        window_size = None
    return budget, window_size


def extract_ratio_and_others_from_dir_name(dir_name):
    """从目录名中解析比例以及其他参数（对应 extractRatioAndOthersFromDirName.m）

    目录名格式形如：u_0-1_p_0-6（第 3 段为 p 时解析预算）或 u_0-1_w_100（第 3 段为 w 时解析窗口大小）。

    :param dir_name: 目录名
    :return: (ratio, budget, window_size)，未被解析到的项为 None / ""
    """
    segments = str(dir_name).split("_")
    if len(segments) < 4:
        return None, None, None
    ratio = _to_number(segments[1])
    budget, window_size = None, None
    other = _to_number(segments[3])
    if segments[2] == "p":
        budget = other
    else:
        window_size = int(other) if other is not None else None
    return ratio, budget, window_size


def get_parent_and_file_name(path):
    """拆分路径为父目录与文件名（对应 getParentAndFileName.m）

    :param path: 文件或目录路径（末尾的分隔符会被去掉）
    :return: (父目录, 文件名)
    """
    path = str(path).rstrip("/\\")
    return os.path.split(path)


def is_number(text):
    """判断字符串是否可以解析为数字"""
    return re.fullmatch(r"[+-]?(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?", str(text).strip()) is not None


def build_output_path(output_basic_path, sub_dir, file_name, ext=None):
    """拼接图片输出路径（对应 MATLAB 中的 fullfile(output_basicPath,'figures',...)）

    :param output_basic_path: 输出根目录
    :param sub_dir: 子目录，如 config.OUTPUT_SUB_DIR_MAIN
    :param file_name: 文件名（不含扩展名）
    :param ext: 扩展名；为 None 时使用 config.OUTPUT_EXT
    :return: 完整输出路径
    """
    if ext is None:
        ext = config.OUTPUT_EXT
    if ext and not ext.startswith("."):
        ext = "." + ext
    return os.path.join(str(output_basic_path), sub_dir, str(file_name) + ext)
