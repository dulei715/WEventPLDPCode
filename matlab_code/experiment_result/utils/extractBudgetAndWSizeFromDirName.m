function [budget, w_size] = extractBudgetAndWSizeFromDirName(dir_name) %patter like: p_2-0_w_20
segment_name = strsplit(dir_name, '_');
budget_str = strrep(segment_name(2), '-', '.');
w_size_str = segment_name(4);
%disp(budget_str);
%disp(w_size_str);
budget = str2double(budget_str);
w_size = str2num(cell2mat(w_size_str));