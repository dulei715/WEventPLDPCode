function y = windowRecycle(size, window, epsilon)
x = [1:size];
y = zeros(1,size);
y(1) = epsilon / 4;
for i = [2 : size]
    y(i) = y(i-1)/ 2;
    if i > window
        break;
    end
end
for i = [window+1 : size]
    y(i) = (y(i-1)+y(i-window)) / 2;
end
plot(x,y);

