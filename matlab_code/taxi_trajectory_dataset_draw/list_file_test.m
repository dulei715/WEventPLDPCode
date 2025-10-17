function y = list_file_test(path)
files = dir(path);
files = files(~[files.isdir]);
for i = 1:length(files)
    disp(['FileName: ', files(i).name]);
end