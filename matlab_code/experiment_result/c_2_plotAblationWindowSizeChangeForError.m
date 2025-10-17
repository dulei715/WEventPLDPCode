function y = c_2_plotAblationWindowSizeChangeForError(input_basicPath, output_basicPath)
dataset_dirs = ["1.trajectory_result", "2.checkIn_result", "3.tlns_result", "4.sin_result", "5.log_result"];
output_dataset_names = ["trajectory", "check_in", "tlns", "sin", "log"];
default_budget = 0.6;

i = 0;
metric_col_index = 9; % 
metric_name = "AMRE";
shrink_ratio = 1;
metric_whether_log = 1;
for dir_name = dataset_dirs
    i = i + 1;
    abs_dir = strcat(input_basicPath, dir_name);
    outputFileName = fullfile(char(output_basicPath), 'figures', 'experiment_result_add', char(strcat(output_dataset_names(i),"_window_size_change_", metric_name,"_ablation_ldp.eps")));
    a_6_plotBasicAblateWChangeInfluenceGivenMetricForSingleDS(abs_dir, default_budget, metric_col_index, metric_name, metric_whether_log, shrink_ratio, outputFileName);
end

i = 0;
metric_col_index = 10; % 
metric_name = "AJSD";
metric_whether_log = 1;
for dir_name = dataset_dirs
    i = i + 1;
    abs_dir = strcat(input_basicPath, dir_name);
    outputFileName = fullfile(char(output_basicPath), 'figures', 'experiment_result_add', char(strcat(output_dataset_names(i),"_window_size_change_", metric_name,"_ablation_ldp.eps")));
    a_6_plotBasicAblateWChangeInfluenceGivenMetricForSingleDS(abs_dir, default_budget, metric_col_index, metric_name, metric_whether_log, shrink_ratio, outputFileName);
end