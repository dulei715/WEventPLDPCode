function y = drawUsersPositionOfTrajectory(basicPath, fileNumber) 
fig = figure;
hold on;
for i = 1:fileNumber
    tempFile = [basicPath,num2str(i),'.txt'];
    drawOneUserPositionOfTrajectory(tempFile);
end
figure_FontSize = 25;
set(get(gca,'XLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(get(gca,'YLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(gca,'FontName','Times New Roman' ,'FontSize',figure_FontSize);
xlabel('longitude');
ylabel('latitude');
    