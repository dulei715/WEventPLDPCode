#!/bin/bash
dataset_dir_name="Log";
basic_path="../0.dataset/$dataset_dir_name";
#totalMem="-Xms120g"
#maxMem="-Xmx2400g"
for i in {1..10}; do
  java ${totalMem} ${maxMem} -cp WEventPLDPCode-1.0-SNAPSHOT-jar-with-dependencies.jar hnu.dll.run.d_total_run._5_main_run.${dataset_dir_name}MainRun $((i - 1))
  mkdir -p ${basic_path}/round_${i}_main_ldp
  mv ${basic_path}/basic_info ${basic_path}/round_${i}_main_ldp &
  mv ${basic_path}/runInput ${basic_path}/round_${i}_main_ldp &
  mv ${basic_path}/group_generated_parameters ${basic_path}/round_${i}_main_ldp &
  mv ${basic_path}/group_output ${basic_path}/round_${i}_main_ldp &
  wait
done