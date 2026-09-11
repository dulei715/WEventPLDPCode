#!/bin/bash
# 维度可扩展性实验（tlns）
# 取值域大小列表，需与 config/parameter_config.xml 中 DomainSize 的 fixed 标签一致
domain_size_list="2 5 10 25 50"
# 每个 d 跑几轮；改成 1 可快速验证（趋势图 1~3 轮就够，正式结果建议 10 轮）
repeat_round=10
dataset_dir_name="TLNS"
main_java_file_name="TLNS"
final_output_dir_name="3.tlns_result"
basic_path="../0.dataset/${dataset_dir_name}"
totalMem="-Xms8g"
maxMem="-Xmx28g"
for d in ${domain_size_list}; do
  # 1. 跑 repeat_round 轮（数据集由 MainRun 内部按 -Dexp.domain 自动生成，无需单独生成）
  for i in $(seq 1 ${repeat_round}); do
    java ${totalMem} ${maxMem} -Dexp.tag=fixed -Dexp.domain=${d} -cp WEventPLDPCode-1.0-SNAPSHOT-jar-with-dependencies.jar hnu.dll.run.d_total_run._5_main_run.${main_java_file_name}MainRun $((i - 1))
    mkdir -p ${basic_path}/round_${i}_main_ldp
    mv ${basic_path}/basic_info ${basic_path}/round_${i}_main_ldp &
    mv ${basic_path}/runInput ${basic_path}/round_${i}_main_ldp &
    mv ${basic_path}/group_generated_parameters ${basic_path}/round_${i}_main_ldp &
    mv ${basic_path}/group_output ${basic_path}/round_${i}_main_ldp &
    mv ${basic_path}/group_output_time_cost ${basic_path}/round_${i}_main_ldp 2>/dev/null &
    wait
  done
  # 2. 合并本 d 的 repeat_round 轮
  #    按实际读到的 round 目录数取平均，config 中 combineRound 只是上限（需 >= repeat_round）
  java ${totalMem} ${maxMem} -cp WEventPLDPCode-1.0-SNAPSHOT-jar-with-dependencies.jar hnu.dll.run.d_total_run._5_main_run.post_process.${main_java_file_name}MainLDPPosterRun
  # 3. 结果目录改名备份，避免被下一个 d 覆盖
  mv ../1.main_ldp_result/${final_output_dir_name} ../1.main_ldp_result/${final_output_dir_name}_d${d}
  # 4. 清理本 d 的轮次目录，避免与下一个 d 混淆
  rm -rf ${basic_path}/round_*_main_ldp
done
