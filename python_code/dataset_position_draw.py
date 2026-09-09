# -*- coding: utf-8 -*-
"""原始数据集的位置散点图

对应 matlab_code 下的两个目录：
    check_in_dataset_draw/drawUsersPositionOfCheckIn.m      -> draw_users_position_of_check_in
    check_in_dataset_draw/drawOneUserPositionOfCheckIn.m    -> draw_one_user_position_of_check_in
    taxi_trajectory_dataset_draw/drawUsersPositionOfTrajectory.m    -> draw_users_position_of_trajectory
    taxi_trajectory_dataset_draw/drawOneUserPositionOfTrajectory.m  -> draw_one_user_position_of_trajectory
    taxi_trajectory_dataset_draw/before_drawUsersPositionOfTrajectory.m    -> before_draw_users_position_of_trajectory
    taxi_trajectory_dataset_draw/before_drawOneUserPositionOfTrajectory.m  -> before_draw_one_user_position_of_trajectory

这些图用于展示数据集本身的空间分布（经度-纬度散点），与实验结果图无关，单独放在本文件中。

列号说明（与 MATLAB readtable 一致，均为 1-based）：
    Check-in 数据集 : 第 2 列纬度，第 3 列经度
    Taxi 轨迹数据集 : 第 4 列纬度，第 3 列经度
"""

import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import config  # noqa: E402
from utils import plot_utils  # noqa: E402

# 可能用到的分隔符（MATLAB 的 readtable 会自动探测，这里手动实现类似逻辑）
CANDIDATE_SEPARATORS = ["\t", ",", ";", " "]

# 散点图上的字号（对应这些脚本中的 figure_FontSize = 25）
SCATTER_FONT_SIZE = 25
# 随机采样时的随机种子；设为 None 则每次结果不同（MATLAB 的 unifrnd 不做固定种子）
RANDOM_SEED = 0


def _detect_separator(lines):
    """猜测分隔符：选择能切分出最多列的分隔符（列数按所有行的最小值计）"""
    best_separator, best_columns = ",", 1
    for separator in CANDIDATE_SEPARATORS:
        column_counts = [len(line.split(separator)) for line in lines if line]
        if not column_counts:
            continue
        columns = min(column_counts)
        if columns > best_columns:
            best_columns, best_separator = columns, separator
    return best_separator


def _to_float(text):
    """字符串转 float，失败返回 None"""
    try:
        return float(text.strip())
    except (ValueError, AttributeError):
        return None


def read_points(file_path, latitude_col, longitude_col, separator=None):
    """从文本文件读取经纬度点（对应 MATLAB 的 readtable + 取列）

    :param file_path: 数据文件路径
    :param latitude_col: 纬度列号（1-based）
    :param longitude_col: 经度列号（1-based）
    :param separator: 分隔符；为 None 时自动探测
    :return: (latitudes, longitudes) 两个列表
    """
    if not os.path.isfile(file_path):
        return [], []
    with open(file_path, "r", encoding="utf-8-sig", errors="ignore") as file:
        lines = [line.strip() for line in file if line.strip()]
    if not lines:
        return [], []

    if separator is None:
        separator = _detect_separator(lines)

    latitudes, longitudes = [], []
    max_col = max(latitude_col, longitude_col)
    for line in lines:
        parts = line.split(separator) if separator else line.split()
        if len(parts) < max_col:
            continue
        latitude = _to_float(parts[latitude_col - 1])
        longitude = _to_float(parts[longitude_col - 1])
        if latitude is None or longitude is None:
            continue  # 跳过表头等无法解析的行
        latitudes.append(latitude)
        longitudes.append(longitude)
    return latitudes, longitudes


def _new_scatter_axes():
    """新建散点图画布并设置标签与字号（对应这些脚本末尾的 xlabel/ylabel 与 set(gca,...)）"""
    plot_utils.setup_matplotlib()
    fig, ax = plot_utils.new_figure()
    ax.set_xlabel("longitude", fontsize=SCATTER_FONT_SIZE,
                  fontname=config.FONT_FAMILY, fontfamily="serif")
    ax.set_ylabel("latitude", fontsize=SCATTER_FONT_SIZE,
                  fontname=config.FONT_FAMILY, fontfamily="serif")
    ax.tick_params(labelsize=SCATTER_FONT_SIZE)
    for label in ax.get_xticklabels() + ax.get_yticklabels():
        label.set_fontname(config.FONT_FAMILY)
        label.set_fontfamily("serif")
    return fig, ax


def draw_one_user_position_of_check_in(file_path, ax=None, separator=None):
    """画单个 Check-in 用户的位置散点（对应 drawOneUserPositionOfCheckIn.m）

    :return: (fig, ax)；传入 ax 时 fig 为 None
    """
    latitudes, longitudes = read_points(file_path, 2, 3, separator)
    if not latitudes:
        return None, ax
    if ax is None:
        fig, ax = _new_scatter_axes()
    else:
        fig = None
    ax.plot(longitudes, latitudes, ".", color="b")
    return fig, ax


def draw_users_position_of_check_in(basic_path, file_number, output_file=None, separator=None):
    """画多个 Check-in 用户的位置散点（对应 drawUsersPositionOfCheckIn.m）

    文件名规则与 MATLAB 一致：{basicPath}{i}.txt，i = 1..file_number

    :param basic_path: 数据文件所在目录（需以路径分隔符结尾，或用 os.path.join 处理）
    :param file_number: 文件数量
    :param output_file: 输出图片路径；为 None 时不保存
    :return: (fig, ax)
    """
    fig, ax = _new_scatter_axes()
    for index in range(1, int(file_number) + 1):
        file_path = os.path.join(str(basic_path), "%d.txt" % index)
        draw_one_user_position_of_check_in(file_path, ax=ax, separator=separator)
    if output_file:
        plot_utils.save_figure(fig, output_file)
        return None, ax
    return fig, ax


def draw_one_user_position_of_trajectory(file_path, latitude_bound=None, longitude_bound=None,
                                         chosen_ratio=1.0, ax=None, separator=None):
    """画单个出租车轨迹的位置散点（对应 drawOneUserPositionOfTrajectory.m）

    :param latitude_bound: 纬度范围 [下界, 上界]；为 None 时不过滤
    :param longitude_bound: 经度范围 [下界, 上界]；为 None 时不过滤
    :param chosen_ratio: 采样比例（对应 MATLAB 的 chosenRatio，用 unifrnd 随机筛选）
    :return: (fig, ax)；传入 ax 时 fig 为 None
    """
    latitudes, longitudes = read_points(file_path, 4, 3, separator)
    if not latitudes:
        return None, ax

    # 经纬度范围过滤（对应 MATLAB 中的 if 判断）
    if latitude_bound is not None and longitude_bound is not None:
        filtered = [
            (lat, lon) for lat, lon in zip(latitudes, longitudes)
            if latitude_bound[0] <= lat <= latitude_bound[1]
            and longitude_bound[0] <= lon <= longitude_bound[1]
        ]
        latitudes = [item[0] for item in filtered]
        longitudes = [item[1] for item in filtered]
    if not latitudes:
        return None, ax

    # 随机采样（对应 MATLAB 的 unifrnd(0,1,dataLen,1) <= chosenRatio）
    if chosen_ratio is not None and chosen_ratio < 1.0:
        if RANDOM_SEED is not None:
            random.seed(RANDOM_SEED)
        sampled = [
            (lat, lon) for lat, lon in zip(latitudes, longitudes)
            if random.random() <= chosen_ratio
        ]
        latitudes = [item[0] for item in sampled]
        longitudes = [item[1] for item in sampled]
    if not latitudes:
        return None, ax

    if ax is None:
        fig, ax = _new_scatter_axes()
    else:
        fig = None
    ax.plot(longitudes, latitudes, ".", color="b")
    return fig, ax


def draw_users_position_of_trajectory(basic_path, latitude_bound=None, longitude_bound=None,
                                      chosen_ratio=1.0, output_file=None, separator=None):
    """画目录下所有出租车轨迹文件的位置散点（对应 drawUsersPositionOfTrajectory.m）

    :param basic_path: 数据文件所在目录
    :return: (fig, ax)
    """
    fig, ax = _new_scatter_axes()
    if os.path.isdir(basic_path):
        for name in sorted(os.listdir(basic_path)):
            file_path = os.path.join(str(basic_path), name)
            if os.path.isfile(file_path):
                draw_one_user_position_of_trajectory(
                    file_path, latitude_bound, longitude_bound, chosen_ratio,
                    ax=ax, separator=separator,
                )
    if output_file:
        plot_utils.save_figure(fig, output_file)
        return None, ax
    return fig, ax


def before_draw_one_user_position_of_trajectory(file_path, ax=None):
    """旧版轨迹散点绘制（对应 before_drawOneUserPositionOfTrajectory.m）

    MATLAB 中用 importdata(file, ',', 0) 读取，取前两列作为 (x, y)。
    :return: (fig, ax)；传入 ax 时 fig 为 None
    """
    if not os.path.isfile(file_path):
        return None, ax
    with open(file_path, "r", encoding="utf-8-sig", errors="ignore") as file:
        lines = [line.strip() for line in file if line.strip()]
    xs, ys = [], []
    for line in lines:
        parts = line.split(",")
        if len(parts) < 2:
            continue
        x_value, y_value = _to_float(parts[0]), _to_float(parts[1])
        if x_value is None or y_value is None:
            continue
        xs.append(x_value)
        ys.append(y_value)
    if not xs:
        return None, ax

    if ax is None:
        fig, ax = _new_scatter_axes()
    else:
        fig = None
    ax.plot(xs, ys, ".", color="b")
    return fig, ax


def before_draw_users_position_of_trajectory(basic_path, file_number, output_file=None):
    """旧版：画多个轨迹文件（文件名 1.txt ~ file_number.txt），对应 before_drawUsersPositionOfTrajectory.m

    :return: (fig, ax)
    """
    fig, ax = _new_scatter_axes()
    for index in range(1, int(file_number) + 1):
        file_path = os.path.join(str(basic_path), "%d.txt" % index)
        before_draw_one_user_position_of_trajectory(file_path, ax=ax)
    if output_file:
        plot_utils.save_figure(fig, output_file)
        return None, ax
    return fig, ax
