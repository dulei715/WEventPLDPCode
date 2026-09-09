# -*- coding: utf-8 -*-
"""utils 包：对应 matlab_code/experiment_result/utils 目录

- dir_utils     : 目录/文件名解析（对应 list_dir_name.m、extractBudgetAndWSizeFromDirName.m 等）
- result_reader : 实验结果文件 result.txt 的读取（对应 MATLAB 的 readtable）
- plot_utils    : 图形样式与保存（对应 MATLAB 的 set(gca,...) 与 export_fig）

plot_utils 依赖 matplotlib。这里做了容错：matplotlib 未安装时，不会导致整个包无法导入
（这样 --check-names 这类只读数据的功能仍可使用），但一旦真正调用绘图相关的函数，
就会抛出一个带安装提示的 ImportError，而不是难懂的 'NoneType' object has no attribute。
"""

from . import dir_utils
from . import result_reader


class _MissingMatplotlib:
    """matplotlib 未安装时的占位对象

    对它的任何属性访问都会抛出带安装提示的 ImportError，
    把"缺依赖"这件事在调用点明明白白地报出来。
    """

    def __init__(self, error):
        self._error = error

    def __getattr__(self, name):
        raise ImportError(
            "缺少绘图依赖 matplotlib，无法画图。请先安装：\n"
            "    pip install -r requirements.txt    （或 pip install matplotlib）\n"
            "原始错误：%s" % self._error
        )


try:
    from . import plot_utils
except ImportError as error:  # matplotlib 未安装
    plot_utils = _MissingMatplotlib(error)
