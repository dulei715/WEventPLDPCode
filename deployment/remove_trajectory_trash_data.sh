#!/bin/bash
dataset_dir_name="T-drive_Taxi_Trajectories";
basic_path="../0.dataset/$dataset_dir_name";

for i in {1..10}; do
  rm -rf ${basic_path}/round_${i}_main_ldp &
  wait
done

rm -rf ${basic_path}/group_output
rm -rf ${basic_path}/group_generated_parameters