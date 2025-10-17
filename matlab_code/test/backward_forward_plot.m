function y = backward_forward_plot(minimal_total_forward_budget, p_i, q_i, interval)
% parameter=128,4,3.5,5
backward_lower_bound = minimal_total_forward_budget / power(2,p_i);
backward_upper_bound = minimal_total_forward_budget / power(2,q_i);
init_budget = minimal_total_forward_budget / 2;
x = [0:interval:100];
y1 = zeros(size(x));
y2 = zeros(size(x));
y_L = ones(size(x))*backward_lower_bound;
y_R = ones(size(x))*backward_upper_bound;
remain_1 = init_budget;
remain_2 = init_budget;
k = 1;
for i = x(1:end)
    y1(k) = remain_1 / 2;
    y2(k) = remain_2 / 2;
    if y1(k) >= backward_upper_bound 
        temp_cost_1 = backward_lower_bound;
    else
        temp_cost_1 = y1(k);
    end
    if y2(k) >= backward_lower_bound 
        temp_cost_2 = backward_upper_bound;
    else
        temp_cost_2 = y2(k);
    end
    remain_1 = remain_1 - temp_cost_1;
    remain_2 = remain_2 - temp_cost_2;
    k = k + 1;
end
fig = figure;
hold on;
figure_MarkerSize = 28;
figure_FontSize = 20;
plot(x, y1, 'g-','LineWidth',2, 'MarkerSize',figure_MarkerSize);
plot(x, y2, 'k-','LineWidth',2, 'MarkerSize',figure_MarkerSize);
plot(x, y_L, 'm-', 'LineWidth', 2, 'MarkerSize',figure_MarkerSize);
plot(x, y_R, 'b-','LineWidth',2, 'MarkerSize',figure_MarkerSize);

plot(x(1),y2(1),'r*');
text(x(1)+1.5,y2(1)+1.5,'A','Color','red','FontSize',figure_FontSize);

y_temp = backward_lower_bound;
x_temp = (init_budget/2 - y_temp)/(backward_upper_bound/2/interval);
plot(x_temp,y_temp,'r*');
text(x_temp-3.5,y_temp-1.5,'B','Color','red','FontSize',figure_FontSize);

plot(x(length(x)),y2(length(x)),'r*');
text(x(length(x))-4.5,y2(length(x))+2.5,'C','Color','red','FontSize',figure_FontSize);

y_temp = backward_upper_bound;
x_temp = (init_budget/2 - y_temp)/(backward_lower_bound/2/interval);
plot(x_temp,y_temp,'r*');
text(x_temp+2,y_temp+2,'D','Color','red','FontSize',figure_FontSize);

plot(0,backward_lower_bound,'r*');
text(1.5,backward_lower_bound-1.5,'E','Color','red','FontSize',figure_FontSize);

plot(x(length(x)),backward_upper_bound,'r*');
text(x(length(x))-4.5,backward_upper_bound+1.5,'F','Color','red','FontSize',figure_FontSize);

%plot(x,y2,x,y_L,x,y_R);
h = legend('\epsilon_{F,i,t}^{(2,R)}','\epsilon_{F,i,t}^{(2,L)}','\epsilon_L^{(B,M)}(i)','\epsilon_R^{(B,M)}(i)','Location','Best');
set(get(gca,'XLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(get(gca,'YLabel'),'FontSize',figure_FontSize,'FontName','Times New Roman');
set(gca,'FontName','Times New Roman' ,'FontSize',figure_FontSize);