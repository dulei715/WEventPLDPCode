function y = a_3_plotBasicMainBudgetChangeInfluenceGivenMetricForSingleDS(basic_path, default_window_size, metric_col_index, metric_name, metric_whether_log, shrink_ratio, outputFileName)
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
    if temp_window_size ~= default_window_size
        continue;
    end
    % disp(temp_budget);
    i = i + 1;
    x(i) = temp_budget;
    data_path = fullfile(char(basic_path), temp_name, 'result.txt');
    temp_table = readtable(data_path);
    if metric_whether_log
            y_lpd(i) = log(temp_table{2,metric_col_index})*shrink_ratio;
            y_lpa(i) = log(temp_table{3,metric_col_index})*shrink_ratio;
            y_basic_plpd(i) = log(temp_table{4,metric_col_index})*shrink_ratio;
            y_basic_plpa(i) = log(temp_table{5,metric_col_index})*shrink_ratio;
            y_ablate_ops_plpd(i) = log(temp_table{6,metric_col_index})*shrink_ratio;
            y_ablate_ops_plpa(i) = log(temp_table{7,metric_col_index})*shrink_ratio;
            y_ablate_rp_plpd(i) = log(temp_table{8,metric_col_index})*shrink_ratio;
            y_ablate_rp_plpa(i) = log(temp_table{9,metric_col_index})*shrink_ratio;
            y_enhanced_plpd(i) = log(temp_table{10,metric_col_index})*shrink_ratio;
            y_enhanced_plpa(i) = log(temp_table{11,metric_col_index})*shrink_ratio;
    else
            y_lpd(i) = temp_table{2,metric_col_index}*shrink_ratio;
            y_lpa(i) = temp_table{3,metric_col_index}*shrink_ratio;
            y_basic_plpd(i) = temp_table{4,metric_col_index}*shrink_ratio;
            y_basic_plpa(i) = temp_table{5,metric_col_index}*shrink_ratio;
            y_ablate_ops_plpd(i) = temp_table{6,metric_col_index}*shrink_ratio;
            y_ablate_ops_plpa(i) = temp_table{7,metric_col_index}*shrink_ratio;
            y_ablate_rp_plpd(i) = temp_table{8,metric_col_index}*shrink_ratio;
            y_ablate_rp_plpa(i) = temp_table{9,metric_col_index}*shrink_ratio;
            y_enhanced_plpd(i) = temp_table{10,metric_col_index}*shrink_ratio;
            y_enhanced_plpa(i) = temp_table{11,metric_col_index}*shrink_ratio;    
    end

end


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
xLabelName = "$\epsilon$";
%xLabelName = "$\mathcal{E}$";
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

xlabel(xLabelName, 'Interpreter', 'latex');
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
%set(get(gca,'XLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(get(gca,'XLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(get(gca,'YLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');

set(gca,'FontName','Times New Roman' ,'FontSize',figure_FontSize);
%set(findobj('FontSize',10),'FontSize',figure_FontSize);


set(get(gca,'XLabel'),'FontSize',figure_FontSize_X,'FontName','Time New Roman');
%set(get(gca,'YLabel'),'FontSize',figure_FontSize_Y,'FontName','Times New Roman');

%h = legend(legend_names(1), legend_names(2), legend_names(3), legend_names(4), legend_names(5), legend_names(6), legend_names(7), legend_names(8), legend_names(9), legend_names(10), 'Location','Best');
h = legend(legend_names(1), legend_names(2), legend_names(3), legend_names(4), legend_names(5), legend_names(6), 'Location','Best');

set(h,'FontName','Times New Roman','FontSize',14,'FontWeight','normal');
legend('off');

export_fig(fig, outputFileName, '-pdf' , '-r256' , '-transparent'); %for windows system
