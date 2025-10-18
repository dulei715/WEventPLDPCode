function y = a_2_plotBasicWindowSizeTimeCost( ...
    basic_path, default_budget, metric_col_index, metric_name, metric_whether_log, shrink_ratio, outputFileName)
dir_names = list_dir_name(basic_path);
y_lpd = zeros(1,5);
y_lpa = zeros(1,5);
y_basic_plpd = zeros(1,5);
y_basic_plpa = zeros(1,5);

y_ablate_ops_plpd = zeros(1,5);
y_ablate_ops_plpa = zeros(1,5);

y_ablate_rp_plpd = zeros(1,5);
y_ablate_rp_plpa = zeros(1,5);

y_enhanced_plpd = zeros(1,5);
y_enhanced_plpa = zeros(1,5);

x = zeros(1,5);
i = 0;


for temp_name = dir_names
    temp_name = cell2mat(temp_name);
    [temp_budget, temp_window_size] = extractBudgetAndWSizeFromDirName(temp_name);
    if temp_budget ~= default_budget
        continue;
    end
    % disp(temp_budget);
    i = i + 1;
    data(i,1) = temp_window_size;
    data_path = fullfile(char(basic_path), temp_name, 'result.txt');
    temp_table = readtable(data_path);

    if metric_whether_log
        data(i,2) = log(temp_table{2,metric_col_index})*shrink_ratio;
        data(i,3) = log(temp_table{3,metric_col_index})*shrink_ratio;
        data(i,4) = log(temp_table{4,metric_col_index})*shrink_ratio;
        data(i,5) = log(temp_table{5,metric_col_index})*shrink_ratio;
        data(i,6) = log(temp_table{6,metric_col_index})*shrink_ratio;
        data(i,7) = log(temp_table{7,metric_col_index})*shrink_ratio;
        data(i,8) = log(temp_table{8,metric_col_index})*shrink_ratio;
        data(i,9) = log(temp_table{9,metric_col_index})*shrink_ratio;
        data(i,10) = log(temp_table{10,metric_col_index})*shrink_ratio;
        data(i,11) = log(temp_table{11,metric_col_index})*shrink_ratio;
    else
        data(i,2) = temp_table{2,metric_col_index}*shrink_ratio;
        data(i,3) = temp_table{3,metric_col_index}*shrink_ratio;
        data(i,4) = temp_table{4,metric_col_index}*shrink_ratio;
        data(i,5) = temp_table{5,metric_col_index}*shrink_ratio;
        data(i,6) = temp_table{6,metric_col_index}*shrink_ratio;
        data(i,7) = temp_table{7,metric_col_index}*shrink_ratio;
        data(i,8) = temp_table{8,metric_col_index}*shrink_ratio;
        data(i,9) = temp_table{9,metric_col_index}*shrink_ratio;
        data(i,10) = temp_table{10,metric_col_index}*shrink_ratio;
        data(i,11) = temp_table{11,metric_col_index}*shrink_ratio;
    end

end

data = sortrows(data);
x = data(:,1);

y_lpd = data(:,2);
y_lpa = data(:,3);
y_basic_plpd = data(:,4);
y_basic_plpa = data(:,5);

y_ablate_ops_plpd = data(:,6);
y_ablate_ops_plpa = data(:,7);

y_ablate_rp_plpd = data(:,8);
y_ablate_rp_plpa = data(:,9);

y_enhanced_plpd = data(:,10);
y_enhanced_plpa = data(:,11);


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
xLabelName = "$w$";
% yLabelName = 'MRE';
if metric_whether_log
    yLabelName = "ln(" + metric_name + ")";
else
    yLabelName = metric_name;
end

%legend_names = ["LPD";"LPA";"PLPD";"PLPA"; "PLPD-RP"; "PLPA-RP"; "PLPD-OPS"; "PLPA-OPS";"PLPD+";"PLPA+"];
legend_names = ["LPD";"LPA";"PLPD";"PLPA"; "PLPD+";"PLPA+"];


figure_MarkerSize = 20;
figure_FontSize = 28;
figure_FontSize_X = 28;
figure_FontSize_Y = 28;

fig = figure;
hold on;

xlabel(xLabelName,'Interpreter', 'latex');
plot(x, y_lpd, 'ks-','LineWidth',2, 'MarkerSize',figure_MarkerSize);
plot(x, y_lpa, 'mo-','LineWidth',2, 'MarkerSize',figure_MarkerSize);
plot(x, y_basic_plpd, 'bs--', 'LineWidth', 2, 'MarkerSize',figure_MarkerSize);
plot(x, y_basic_plpa, 'go--','LineWidth',2, 'MarkerSize',figure_MarkerSize);

%plot(x, y_ablate_ops_plpd, 'k^-.', 'LineWidth', 2, 'MarkerSize', figure_MarkerSize);
%plot(x, y_ablate_ops_plpa, 'm^-.', 'LineWidth', 2, 'MarkerSize', figure_MarkerSize);
%plot(x, y_ablate_rp_plpd, 'bd-.', 'LineWidth', 2, 'MarkerSize', figure_MarkerSize);
%plot(x, y_ablate_rp_plpa, 'gd-.', 'LineWidth', 2, 'MarkerSize', figure_MarkerSize);

plot(x, y_enhanced_plpd, 'cs:','LineWidth',2, 'MarkerSize',figure_MarkerSize);
plot(x, y_enhanced_plpa, 'ro:','LineWidth',2, 'MarkerSize',figure_MarkerSize);

ylabel(yLabelName);


xlim([round(x(1),1) x(length(x))]);
set(gca,'XTick',round(x,1));

%figure_FontSize = 18;
set(get(gca,'XLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(get(gca,'YLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');

set(gca,'FontName','Times New Roman' ,'FontSize',figure_FontSize);
%set(findobj('FontSize',10),'FontSize',figure_FontSize);


set(get(gca,'XLabel'),'FontSize',figure_FontSize_X,'FontName','Times New Roman');
%set(get(gca,'YLabel'),'FontSize',figure_FontSize_Y,'FontName','Times New Roman');

%h = legend(legend_names(1), legend_names(2), legend_names(3), legend_names(4), legend_names(5), legend_names(6), legend_names(7), legend_names(8), legend_names(9), legend_names(10), 'Location','Best');
h = legend(legend_names(1), legend_names(2), legend_names(3), legend_names(4), legend_names(5), legend_names(6), 'Location','Best');

set(h,'FontName','Times New Roman','FontSize',14,'FontWeight','normal');
legend('off');
export_fig(fig, outputFileName, '-pdf' , '-r256' , '-transparent');
