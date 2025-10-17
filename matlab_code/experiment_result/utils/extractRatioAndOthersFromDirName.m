function [ratio, budget, w_size] = extractRatioAndOthersFromDirName(dir_name) %patter like: u_0-1_p_0-6 or u_0-1_w_100
segment_name = strsplit(dir_name, '_');
ratio_str = strrep(segment_name(2), '-', '.');
other_str = strrep(segment_name(4), '-', '.');
ratio = str2double(ratio_str);
if strcmp('p', segment_name(3))
    budget = str2double(other_str);
    w_size = '';
else
    budget = '';
    w_size = str2num(cell2mat(other_str));
end
