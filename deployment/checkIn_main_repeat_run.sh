#!/bin/bash
dataset_dir_name="CheckIn_dataset_TIST2015";
main_java_file_name="CheckIn"
basic_path="../0.dataset/$dataset_dir_name";
#totalMem="-Xms120g"
#maxMem="-Xmx2400g"
for i in {1..10}; do
  java ${totalMem} ${maxMem} -cp WEventPLDPCode-1.0-SNAPSHOT-jar-with-dependencies.jar hnu.dll.run.d_total_run._5_main_run.${main_java_file_name}MainRun $((i - 1))
  mkdir -p ${basic_path}/round_${i}_main_ldp/basic_info
  mv ${basic_path}/basic_info/location_to_Index.txt ${basic_path}/round_${i}_main_ldp/basic_info &
  mv ${basic_path}/basic_info/user_to_Index.txt ${basic_path}/round_${i}_main_ldp/basic_info &
  mv ${basic_path}/group_generated_parameters ${basic_path}/round_${i}_main_ldp &
  mv ${basic_path}/group_output ${basic_path}/round_${i}_main_ldp &
  wait
done