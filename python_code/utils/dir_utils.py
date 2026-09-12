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


def build_domain_result_dir_name(dataset_dir, domain_size):
    """拼接某个取值域大小 d 对应的结果目录名

    对应 deployment/_4_domain_size_scripts/*.sh 中的改名操作：
        mv ../2.domain_change_ldp_result/3.tlns_result ../2.domain_change_ldp_result/3.tlns_result_d5
    即 "3.tlns_result" + d=5 -> "3.tlns_result_d5"。

    :param dataset_dir: 数据集结果目录名，如 "3.tlns_result"
    :param domain_size: 取值域大小 d
    :return: 带 d 后缀的目录名
    """
    return config.DOMAIN_RESULT_DIR_FORMAT % (str(dataset_dir), str(domain_size))


def find_param_dir(basic_path, budget=None, window_size=None):
    """在结果目录下定位参数目录（形如 p_1-5_w_120）

    用于维度可扩展性实验：每个 d 的结果目录下只有一个参数目录，
    因此这里既支持"直接取唯一的那个"，也支持"按 ε / w 精确匹配"。

    :param basic_path: 单个结果目录（如 ".../3.tlns_result_d5"）
    :param budget: 期望的隐私预算；为 None 时不校验
    :param window_size: 期望的窗口大小；为 None 时不校验
    :return: 参数目录名；找不到时返回 None
    """
    dir_names = list_dir_name(basic_path)
    if not dir_names:
        return None
    if budget is None and window_size is None:
        return dir_names[0] if len(dir_names) == 1 else None
    for dir_name in dir_names:
        current_budget, current_window = extract_budget_and_w_size_from_dir_name(dir_name)
        if current_budget is None or current_window is None:
            continue
        if budget is not None and abs(float(current_budget) - float(budget)) > 1e-6:
            continue
        if window_size is not None and abs(float(current_window) - float(window_size)) > 1e-6:
            continue
        return dir_name
    return None


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
