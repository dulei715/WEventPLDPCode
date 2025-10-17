function folders = list_dir_name(basic_path)
basic_path = char(basic_path);
files = dir(basic_path);
folders = {};
%folders = [];
for i = 1:length(files)
    temp_file_name = files(i).name;
    if files(i).isdir && ~strcmp(temp_file_name, '.') && ~strcmp(temp_file_name, '..')
        folders{end+1} = files(i).name;
        %folders = [folders; files(i).name];
    end
end
% folders = cell2mat(folders, ";");
