function y = drawOneUserPositionOfCheckIn(file)
% draw POI.txt
% matrix = importdata(file, 'Delimiter', '\t');
matrix = readtable(file);
if isempty(matrix)
    return;
end
latitudes = matrix(:,2).Var2;
longitudes = matrix(:,3).Var3;
%length(latitudes)
%length(longitudes)
plot(longitudes, latitudes,'.','Color','b');

figure_FontSize = 25;
set(get(gca,'XLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(get(gca,'YLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(gca,'FontName','Times New Roman' ,'FontSize',figure_FontSize);
xlabel('longitude');
ylabel('latitude');