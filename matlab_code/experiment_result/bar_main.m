function bar_main()
fig = figure;
hold on;

figure_MarkerSize = 15;
legend_FontSize = 20;

x = [1:0.1:10]; 

y_lpd = x * 2;
y_lpa = x * 2;
y_basic_plpd = x * 2;
y_basic_plpa = x * 2;
y_enhanced_plpd = x * 2;
y_enhanced_plpa = x * 2;

a = plot(x, y_lpd, 'ks-','LineWidth',2, 'MarkerSize',figure_MarkerSize);
b = plot(x, y_lpa, 'mo-','LineWidth',2, 'MarkerSize',figure_MarkerSize);
c = plot(x, y_basic_plpd, 'bs--', 'LineWidth', 2, 'MarkerSize',figure_MarkerSize);
d = plot(x, y_basic_plpa, 'go--','LineWidth',2, 'MarkerSize',figure_MarkerSize);

%plot(x, y_ablate_ops_plpd, 'k^-.', 'LineWidth', 2, 'MarkerSize', figure_MarkerSize);
%plot(x, y_ablate_ops_plpa, 'm^-.', 'LineWidth', 2, 'MarkerSize', figure_MarkerSize);
%plot(x, y_ablate_rp_plpd, 'bd-.', 'LineWidth', 2, 'MarkerSize', figure_MarkerSize);
%plot(x, y_ablate_rp_plpa, 'gd-.', 'LineWidth', 2, 'MarkerSize', figure_MarkerSize);

i = plot(x, y_enhanced_plpd, 'cs:','LineWidth',2, 'MarkerSize',figure_MarkerSize);
j = plot(x, y_enhanced_plpa, 'ro:','LineWidth',2, 'MarkerSize',figure_MarkerSize);

a.Visible='off';
b.Visible='off';
c.Visible='off';
d.Visible='off';

i.Visible='off';
j.Visible='off';

%legend_names = ["LPD";"LPA";"PLPD";"PLPA"; "PLPD-RP"; "PLPA-RP"; "PLPD-OPS"; "PLPA-OPS";"PLPD+";"PLPA+"];
legend_names = ["LPD";"LPA";"PLPD";"PLPA"; "PLPD+";"PLPA+"];

%h = legend(legend_names(1), legend_names(2), legend_names(3), legend_names(4), legend_names(5), legend_names(6), legend_names(7), legend_names(8), legend_names(9), legend_names(10), 'Location','Best');
h = legend(legend_names(1), legend_names(2), legend_names(3), legend_names(4), legend_names(5), legend_names(6), 'Location','Best');

locationType = 'northoutside';
orientationType = 'horizontal';
textColor = 'black';
h = legend(legend_names, 'Location',locationType,'Orientation',orientationType);
set(h,'FontName','Times New Roman','FontSize',legend_FontSize,'FontWeight','normal');
set(h, 'TextColor',textColor);
h.Box = 'off';
for i = 1:length(h.Children)
    if isfield(h.Children(i), 'String') % 检查是否为文本对象
        set(h.Children(i), 'Color', 'black');
    end
end
axis off;