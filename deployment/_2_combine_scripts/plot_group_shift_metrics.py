#!/usr/bin/env python3
"""Plot the main group-shift figures from group_shift_paper_summary.csv.

This script only reads the aggregated CSV produced by
aggregate_group_shift_metrics.sh. It writes vector PDF figures and 300 dpi PNG
figures for quick inspection.
"""

import argparse
import csv
from pathlib import Path


METHODS = [
    ("PLPA-Plus-Pre-Scheme", "PLPA+ (PRE)", "#0072B2", "o", "-"),
    ("PLPA-Plus-Post-Scheme", "PLPA+ (POST)", "#D55E00", "s", "-"),
    ("PLPD-Plus-Pre-Scheme", "PLPD+ (PRE)", "#009E73", "o", "--"),
    ("PLPD-Plus-Post-Scheme", "PLPD+ (POST)", "#CC79A7", "s", "--"),
]


def load_rows(summary_path):
    with summary_path.open("r", newline="") as handle:
        return list(csv.DictReader(handle))


def numeric(row, column):
    value = row.get(column, "")
    return float(value) if value else None


def plot_metric(rows, metric, y_label, output_dir):
    import matplotlib.pyplot as plt

    directions = ["positive", "reverse"]
    figure, axes = plt.subplots(1, 2, figsize=(7.0, 3.1), sharey=True)
    handles = []

    for axis, direction in zip(axes, directions):
        direction_rows = [row for row in rows if row.get("direction") == direction]
        for method, label, color, marker, line_style in METHODS:
            method_rows = [
                row for row in direction_rows if row.get("method") == method
            ]
            method_rows.sort(key=lambda row: float(row["delta"]))
            if not method_rows:
                continue
            x_values = [float(row["delta"]) for row in method_rows]
            y_values = [numeric(row, metric + "_mean") for row in method_rows]
            error_values = [numeric(row, metric + "_std") or 0.0 for row in method_rows]
            line = axis.errorbar(
                x_values,
                y_values,
                yerr=error_values,
                label=label,
                color=color,
                marker=marker,
                linestyle=line_style,
                linewidth=1.5,
                markersize=4.5,
                capsize=2.5,
                elinewidth=0.8,
            )
            if direction == "positive":
                handles.append(line)

        axis.set_title(direction.capitalize())
        axis.set_xlabel(r"Shift magnitude $\delta$")
        axis.set_xticks(sorted({float(row["delta"]) for row in direction_rows}))
        axis.grid(axis="y", color="#D9D9D9", linewidth=0.6)
        axis.set_axisbelow(True)
        axis.spines["top"].set_visible(False)
        axis.spines["right"].set_visible(False)

    axes[0].set_ylabel(y_label + " (lower is better)")
    if handles:
        figure.legend(
            handles=handles,
            loc="lower center",
            ncol=2,
            frameon=False,
            bbox_to_anchor=(0.5, -0.04),
        )
    figure.tight_layout(rect=(0, 0.10, 1, 1))

    output_dir.mkdir(parents=True, exist_ok=True)
    stem = "group_shift_" + metric
    figure.savefig(output_dir / (stem + ".pdf"), bbox_inches="tight")
    figure.savefig(output_dir / (stem + ".png"), dpi=300, bbox_inches="tight")
    plt.close(figure)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--summary", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    args = parser.parse_args()

    rows = load_rows(args.summary)
    if not rows:
        raise SystemExit("The summary CSV is empty")

    try:
        import matplotlib  # noqa: F401
    except ImportError as error:
        raise SystemExit(
            "matplotlib is required. Run this script in an environment with "
            "matplotlib installed."
        ) from error

    plot_metric(rows, "weighted_amse", "Weighted AMSE", args.output_dir)
    plot_metric(rows, "weighted_ajsd", "Weighted AJSD", args.output_dir)
    print("Figures written to {}".format(args.output_dir))


if __name__ == "__main__":
    main()
