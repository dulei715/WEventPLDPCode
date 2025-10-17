function y = drawOneUserPositionOfTrajectory(file, latitudeBound, longitudeBound, chosenRatio)
matrix = readtable(file);
if isempty(matrix)
    return;
end
raw_latitudes = matrix(:,4).Var4;
raw_longitudes = matrix(:,3).Var3;

latitudes = [];
longitudes = [];
for i  = length(raw_latitudes)
    if raw_latitudes(i) >= latitudeBound(1) && raw_latitudes(i) <= latitudeBound(2) && raw_longitudes(i) >= longitudeBound(1) && raw_longitudes(i) <= longitudeBound(2)
        latitudes = [latitudes; raw_latitudes(i)];
        longitudes = [longitudes; raw_longitudes(i)];
    end
end

dataLen = length(latitudes);
randomBound = unifrnd(0,1,dataLen,1);
chosenIndex = randomBound<=chosenRatio;
if length(chosenIndex) < 1 
    return;
end
latitudes = latitudes(chosenIndex);
longitudes = longitudes(chosenIndex);
plot(longitudes, latitudes,'.','Color','b');