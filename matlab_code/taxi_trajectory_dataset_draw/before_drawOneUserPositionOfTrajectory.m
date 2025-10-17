function y = drawOneUserPositionOfTrajectory(file)
matrix = importdata(file,',', 0);
if isempty(matrix)
    return;
end
y = matrix.data(:,1:2);
plot(y(:,1),y(:,2),'.','Color','b');