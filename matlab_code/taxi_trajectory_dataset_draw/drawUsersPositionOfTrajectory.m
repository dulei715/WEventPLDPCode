function y = drawUsersPositionOfTrajectory(basicPath, latitudeBound, longitudeBound, chosenRatio) 
fig = figure;
hold on;
files = dir(basicPath);
files = files(~[files.isdir]);
for i = 1:length(files)
    tempFile = [basicPath,files(i).name];
    drawOneUserPositionOfTrajectory(tempFile, latitudeBound, longitudeBound, chosenRatio);
end
figure_FontSize = 25;
set(get(gca,'XLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(get(gca,'YLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(gca,'FontName','Times New Roman' ,'FontSize',figure_FontSize);
xlabel('longitude');
ylabel('latitude');
    