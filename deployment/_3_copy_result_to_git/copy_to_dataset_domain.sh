#!/bin/bash
# add ${gitee_path} to $PATH
# 把维度可扩展性实验的结果（2.domain_change_ldp_result）从各服务器目录拷回本地数据集目录
# 主实验结果请用 copy_to_dataset.sh（1.main_ldp_result）

cp -rf ${gitee_path}/server_trans/data-trans2325-vldb05/3_2.wEventPLDP/2.domain_change_ldp_result ${dataset_path}/3-2_stream_ldp
cp -rf ${gitee_path}/server_trans/data-trans2321-vldb01/3_2.wEventPLDP/2.domain_change_ldp_result ${dataset_path}/3-2_stream_ldp
cp -rf ${gitee_path}/server_trans/data-trans2334-direct01-leileidu/3_2.wEventPLDP/2.domain_change_ldp_result ${dataset_path}/3-2_stream_ldp
#cp -rf ${gitee_path}/server_trans/data-trans2309-direct02-leileidu/3_2.wEventPLDP/2.domain_change_ldp_result ${dataset_path}/3-2_stream_ldp
cp -rf ${gitee_path}/server_trans/data-trans2327-direct04-leileidu/3_2.wEventPLDP/2.domain_change_ldp_result ${dataset_path}/3-2_stream_ldp
