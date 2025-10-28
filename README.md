# Data Sets
## Real data set links
*Taxi* Data set (T-Drive trajectory data sample): [https://www.microsoft.com/en-us/research/publication/t-drive-trajectory-data-sample/](https://www.microsoft.com/en-us/research/publication/t-drive-trajectory-data-sample/).

*Foursquare* Data set (Global-scale Check-in Dataset): [https://sites.google.com/site/yangdingqi/home/foursquare-dataset](https://sites.google.com/site/yangdingqi/home/foursquare-dataset).

# Run Process 

## Pre-process
1. Create paths `${basic_path}/0.dataset/${dataset_name}` where `${basic_path}` is any fixed path setting by yourself, `${dataset_name}` is any element in {`T-drive_Taxi_Trajectories`, `CheckIn_dataset_TIST2015`, `TLNS`, `Sin`, `Log`}. 
2. Download *Taxi* and *Foursquare* data sets into paths `${basic_path}/0.dataset/T-drive_Taxi_Trajectories` and `${basic_path}/0.dataset/CheckIn_dataset_TIST2015`, respectively.
3. Copy files in `deployment` to path `${basic_path/deployment}` and direct to `${basic_path}/deployment`.
4. Pre-handle real data sets (*Taxi* and *Foursquare*) respectively by: 
	+ Run script `_0_initialize_scripts/initialize_trajectory_run.sh`.
	+ Run script `_0_initialize_scripts/initialize_check_in_run.sh`.


## Repeat run for real data sets

* Run for *Taxi*:
	* run script `_1_main_run_scripts/trajectory_main_repeat_run.sh`
* Run for *Foursquare*: 
	* run script `_1_main_run_scripts/checkIn_main_repeat_run.sh`

## Repeat initialize and run for synthetic data sets 

* Run for *TLns*:
	* run script `_1_main_run_scripts/tlns_main_repeat_run.sh`
* Run for *Sin*: 
	* run script `_1_main_run_scripts/sin_main_repeat_run.sh`
* Run for *Log*: 
	* run script `_1_main_run_scripts/log_main_repeat_run.sh`


## Combine
* Run for *Taxi*:
	* run script `_2_combine_scripts/combine_trajectory_main_ldp_for_all_rounds.sh`
* Run for *Foursquare*: 
	* run script `_2_combine_scripts/combine_checkIn_main_ldp_for_all_rounds.sh`
* Run for *TLns*:
	* run script `_2_combine_scripts/combine_tlns_main_ldp_for_all_rounds.sh`
* Run for *Sin*: 
	* run script `_2_combine_scripts/combine_sin_main_ldp_for_all_rounds.sh`
* Run for *Log*: 
	* run script `_2_combine_scripts/combine_log_main_ldp_for_all_rounds.sh`
	
The final results are record in `${basic_path}/1.main_ldp_result`
	

# Draw Results
## Initialize MATLAB (2017a)
1. Direct to `${project_path}/matlab_code/experiment_result`.
2. Run function `init_params()`.
3. Create directory `${output_basic_path}/figures/experiment_result` and `${output_basic_path}/figures/experiment_result_add` to record the experiment results where `${output_basic_path}` is  any fixed path setting by yourself.
## Plot Results for the Error and Divergence
* Run function `draw_main()` to get the bar of main figures.
* Run function `draw_ablation()` to get the bar of ablation study figures.
* Run function `b_3_plotMainBudgetChangeForError(${basic_path}/1.main_ldp_result,${output_basic_path})`.
* Run function `b_4_plotMainWindowSizeChangeForError(${basic_path}/1.main_ldp_result,${output_basic_path})`.
* Run function `c_1_plotAblationBudgetChangeForError(${basic_path}/1.main_ldp_result,${output_basic_path})`.
* Run function `c_2_plotAblationWindowSizeChangeForError(${basic_path}/1.main_ldp_result,${output_basic_path})`.
## Plot Results for Time Cost
* Run function `b_1_plotBudgetChangeForTime(${basic_path}/1.main_ldp_result,${output_basic_path})`.
* Run function `b_2_plotWindowSizeChangeForTime(${basic_path}/1.main_ldp_result,${output_basic_path})`.
