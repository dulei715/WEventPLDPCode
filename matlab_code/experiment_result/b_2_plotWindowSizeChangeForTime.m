function y = b_2_plotWindowSizeChangeForTime(input_basicPath, output_basicPath)
dataset_dirs = ["1.trajectory_result", "2.checkIn_result", "3.tlns_result", "4.sin_result", "5.log_result"];
output_dataset_names = ["trajectory", "check_in", "tlns", "sin", "log"];
%default_budget = 0.6;
default_budget = 1.5;


i = 0;
metric_col_index = 4; % 
metric_name = "running time (s)";
metric_file_name = "running_time";
shrink_ratio = 0.001;
metric_whether_log = 0;
for dir_name = dataset_dirs
    i = i + 1;
    abs_dir = strcat(input_basicPath, dir_name);
    outputFileName = fullfile(char(output_basicPath), 'figures', 'experiment_result_add','time_cost_fig', char(strcat(output_dataset_names(i),"_window_size_change_", metric_file_name,".eps")));
    a_2_plotBasicWindowSizeTimeCost(abs_dir, default_budget, metric_col_index, metric_name, metric_whether_log, shrink_ratio, outputFileName);
end