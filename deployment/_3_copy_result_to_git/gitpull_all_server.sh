#!/bin/bash
# add ${gitee_path} to $PATH

server_file_names=(
  data-trans2325-vldb05
  data-trans2321-vldb01
  data-trans2334-direct01-leileidu
  data-trans2309-direct02-leileidu
  data-trans2327-direct04-leileidu
)

for name in "${server_file_names[@]}"; do
  cd ${gitee_path}/server_trans/$name
  git pull
done