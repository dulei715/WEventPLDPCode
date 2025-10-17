function [dir,file_name] = getParentAndFileName(path)
if endsWith(path, filesep)
    path = path(1:length(path)-1);
end
[dir,file_name] = fileparts(path);