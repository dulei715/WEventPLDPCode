function y = drawUsersPositionOfCheckIn(basicPath, fileNumber) 
fig = figure;
hold on;
for i = 1:fileNumber
    tempFile = [basicPath,num2str(i),'.txt'];
    drawOneUserPositionOfCheckIn(tempFile);
end

    