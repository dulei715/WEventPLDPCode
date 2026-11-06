#!/usr/bin/env bash

# Aggregate group-shift metric sidecar files on the server.
#
# Usage from deployment/:
#   bash _2_combine_scripts/aggregate_group_shift_metrics.sh \
#       ../3.group_shift_ldp_result
#
# Add --delete-source only after checking the generated CSV files:
#   bash _2_combine_scripts/aggregate_group_shift_metrics.sh \
#       ../3.group_shift_ldp_result --delete-source
#
# The script reads files below each 4.sin_result_*/group_shift_metrics directory
# and writes only a few CSV summaries under <result-root>/aggregated. Source
# metric files are not changed or deleted.

set -euo pipefail

delete_source=false
positional_args=()
for argument in "$@"; do
  case "${argument}" in
    --delete-source)
      delete_source=true
      ;;
    --help|-h)
      sed -n '3,18p' "$0"
      exit 0
      ;;
    *)
      positional_args+=("${argument}")
      ;;
  esac
done

result_root=${positional_args[0]:-../3.group_shift_ldp_result}
output_dir=${positional_args[1]:-${result_root}/aggregated}

if [[ ! -d "${result_root}" ]]; then
  echo "Result directory does not exist: ${result_root}" >&2
  exit 1
fi

mkdir -p "${output_dir}"

file_list=$(mktemp "${TMPDIR:-/tmp}/group_shift_metric_files.XXXXXX")
trap 'rm -f "${file_list}"' EXIT

find "${result_root}" \
  -type f \
  -path '*/4.sin_result_*/group_shift_metrics/*/segment_*/batch_*.txt' \
  -print > "${file_list}"

if [[ ! -s "${file_list}" ]]; then
  echo "No group_shift_metrics batch files found below ${result_root}" >&2
  exit 1
fi

round_summary="${output_dir}/group_shift_round_summary.csv"
paper_summary="${output_dir}/group_shift_paper_summary.csv"

awk -v file_list="${file_list}" -v round_out="${round_summary}" \
  -v paper_out="${paper_summary}" '
BEGIN {
    FS = ",";
    OFS = ",";
    while ((getline metric_file < file_list) > 0) {
        process_file(metric_file);
    }
    close(file_list);
}

function csv_number(value) {
    return sprintf("%.12g", value + 0);
}

function parse_path(path, parts, n, condition, round_id, method, segment_id, batch_id) {
    n = split(path, parts, "/");
    batch_id = parts[n];
    sub(/^batch_/, "", batch_id);
    sub(/\.txt$/, "", batch_id);
    segment_id = parts[n - 1];
    sub(/^segment_/, "", segment_id);
    method = parts[n - 2];

    if (parts[n - 4] == "group_shift_metrics" && parts[n - 3] ~ /^round_/) {
        condition = parts[n - 5];
        round_id = parts[n - 3];
        sub(/^round_/, "", round_id);
    } else {
        condition = parts[n - 4];
        round_id = "0";
    }

    current_condition = condition;
    current_round = round_id + 0;
    current_method = method;
}

function parse_condition(condition, pieces, remainder, delta_text) {
    remainder = condition;
    sub(/^4\.sin_result_/, "", remainder);
    split(remainder, pieces, "_delta");
    current_direction = pieces[1];
    delta_text = pieces[2];
    gsub(/-/, ".", delta_text);
    current_delta = delta_text + 0;
}

function process_file(path, line, fields, field_count, i, value, key) {
    parse_path(path);
    parse_condition(current_condition);
    key = current_condition SUBSEP current_round SUBSEP current_method;
    source_file_count[key]++;

    first_line = 1;
    while ((getline line < path) > 0) {
        field_count = split(line, fields, FS);
        if (first_line) {
            for (i = 2; i <= field_count; i++) {
                if (!(i in metric_name)) {
                    metric_name[i] = fields[i];
                    metric_count++;
                }
            }
            first_line = 0;
            continue;
        }
        if (field_count < 2 || fields[1] == "") {
            continue;
        }
        timestamp_count[key]++;
        for (i = 2; i <= field_count; i++) {
            if (fields[i] == "") {
                continue;
            }
            value = fields[i] + 0;
            if (value != value) {
                continue;
            }
            sum[key, i] += value;
            square_sum[key, i] += value * value;
        }
    }
    close(path);
}

END {
    round_header = "condition,direction,delta,round,method,timestamp_count,source_file_count";
    paper_header = "condition,direction,delta,method,round_count,timestamp_count";
    for (i = 2; i <= metric_count + 1; i++) {
        round_header = round_header "," metric_name[i] "_mean," metric_name[i] "_std";
        paper_header = paper_header "," metric_name[i] "_mean," metric_name[i] "_std";
    }
    print round_header > round_out;
    print paper_header > paper_out;

    for (key in timestamp_count) {
        split(key, key_parts, SUBSEP);
        condition = key_parts[1];
        round_id = key_parts[2] + 0;
        method = key_parts[3];
        parse_condition(condition);
        printf "%s,%s,%s,%d,%s,%d,%d", condition, current_direction,
            csv_number(current_delta), round_id, method,
            timestamp_count[key], source_file_count[key] > round_out;

        paper_key = condition SUBSEP method;
        paper_round_count[paper_key]++;
        paper_timestamp_count[paper_key] += timestamp_count[key];
        for (i = 2; i <= metric_count + 1; i++) {
            mean = sum[key, i] / timestamp_count[key];
            variance = 0;
            if (timestamp_count[key] > 1) {
                variance = (square_sum[key, i] - sum[key, i] * sum[key, i] / timestamp_count[key]) / (timestamp_count[key] - 1);
            }
            if (variance < 0) {
                variance = 0;
            }
            printf ",%s,%s", csv_number(mean), csv_number(sqrt(variance)) > round_out;
            paper_sum[paper_key, i] += mean;
            paper_square_sum[paper_key, i] += mean * mean;
        }
        print "" > round_out;
    }

    for (paper_key in paper_round_count) {
        split(paper_key, key_parts, SUBSEP);
        condition = key_parts[1];
        method = key_parts[2];
        parse_condition(condition);
        printf "%s,%s,%s,%s,%d,%d", condition, current_direction,
            csv_number(current_delta), method,
            paper_round_count[paper_key], paper_timestamp_count[paper_key] > paper_out;
        for (i = 2; i <= metric_count + 1; i++) {
            mean = paper_sum[paper_key, i] / paper_round_count[paper_key];
            variance = 0;
            if (paper_round_count[paper_key] > 1) {
                variance = (paper_square_sum[paper_key, i] - paper_sum[paper_key, i] * paper_sum[paper_key, i] / paper_round_count[paper_key]) / (paper_round_count[paper_key] - 1);
            }
            if (variance < 0) {
                variance = 0;
            }
            printf ",%s,%s", csv_number(mean), csv_number(sqrt(variance)) > paper_out;
        }
        print "" > paper_out;
    }
}
' /dev/null

# Sort data rows while keeping the header at the top. This is only for easier
# inspection and does not alter the numeric values.
for summary in "${round_summary}" "${paper_summary}"; do
  {
    head -1 "${summary}"
    tail -n +2 "${summary}" | sort -t, -k1,1 -k4,4n -k5,5
  } > "${summary}.sorted"
  mv "${summary}.sorted" "${summary}"
done

if [[ "${delete_source}" == "true" ]]; then
  while IFS= read -r condition_dir; do
    metrics_dir="${condition_dir}/group_shift_metrics"
    if [[ -d "${metrics_dir}" ]]; then
      rm -rf -- "${metrics_dir}"
    fi
  done < <(find "${result_root}" -mindepth 1 -maxdepth 1 -type d -name '4.sin_result_*' -print)
  echo "Deleted source group_shift_metrics directories after successful aggregation."
fi

echo "Aggregated metric files from ${result_root}"
echo "Round summary  ${round_summary}"
echo "Paper summary  ${paper_summary}"
