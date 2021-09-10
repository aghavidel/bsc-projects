%% Q1 %%
clear
clc
%% Part 1 %%
[samples, text, raw] = xlsread("./resources/California_birth_1.xlsx");
for i = 1:14 
    if (text(1, i) == "TGRAMS")
        break
    end
end

for j = 1:14
    if (text(1, j) == "WEEKS")
        break
    end
end

mean_TGRAMS = mean(samples(:, i));
median_TGRAMS = median(samples(:, i));
var_TGRAMS = var(samples(:, i));
max_TGRAMS = max(samples(:, i));
min_TGRAMS = min(samples(:, i));
mean_WEEKS = mean(samples(:, j));
median_WEEKS = median(samples(:, j));
var_WEEKS = var(samples(:, j));
max_WEEKS = max(samples(:, j));
min_WEEKS = min(samples(:, j));

%% FIRST ARGUMENT %%
alpha = 0.05;

for i = 1:14 
    if (text(1, i) == "MAGE")
        break
    end
end

mean_MAGE = mean(samples(:, i));
var_MAGE = var(samples(:, i));

% we use the bessel corrected S for calculating W
s_MAGE = sqrt(var_MAGE * 200 / 199);
% we assume mu = 25
% H0: the mean age is less than 25
w2_MAGE = make_W2(mean_MAGE, 200, 25, s_MAGE);
criterion = tinv(1 - alpha, 199);
if (w2_MAGE < criterion) 
    disp("H0 could be right!,  so there is no evidence");
else
    disp("H0 is False! so the mean is above 25");
end

fprintf("p-value is %f\n", 2 * (1 - tcdf(abs(w2_MAGE), 199)));
%% SECOND ARGUMENT %%
% we use the bessel corrected S for calculating W
s_WEEKS = sqrt(var_WEEKS * 200 / 199);
% we assume mu = 39
% H0: mean weeks is over 39
w2_WEEKS = make_W2(mean_WEEKS, 200, 39, s_WEEKS);
criterion = tinv(alpha, 199);
if (w2_WEEKS > criterion) 
    disp("H0 could be right!,  so there is no evidence");
else
    disp("H0 is False! so the mean is less than 39");
end

fprintf('p-value is : %f \n', 2 * (1 - tcdf(abs(w2_MAGE), 199)));
%% THIRD ARGUMENT %%
% here we employ Welch's test...
% first we seperate smoking and non-smoking mothers
smoker = [];
non_smoker = [];

for i = 1:200
    if(samples(i, 9) == 1)
        smoker = [smoker, samples(i, 12)];
    else
        non_smoker = [non_smoker, samples(i, 12)];
    end
end

% H0: smokers are more or equal to non_smokers

smoker_mean = mean(smoker);
non_smoker_mean = mean(non_smoker);
smoker_var = var(smoker);
non_smoker_var = var(non_smoker);

% we know the sizes by now so we use them directly

s_smoker = sqrt(smoker_var * 28 /27);
s_non_smoker = sqrt(non_smoker_var * 172 / 171);

s_delta = sqrt(s_smoker^2 / 28 + s_non_smoker^2 / 172);
W = (smoker_mean - non_smoker_mean) / s_delta; %if positive it goes toward accepting H0
nu = s_delta ^ 4 / ((s_smoker^2 / 28)^2 / 27 + (s_non_smoker^2 / 172)^2 / 171);

criterion = tinv(1-alpha, nu);

if (W > criterion)
    disp("H0 could be right!, so there is no evidence");
else
    disp("H0 is false, so smokers are less than non smokers");
end

fprintf("Criterion: %f\n", criterion);

hold on
histogram(smoker, 'Normalization', 'probability');
histogram(non_smoker, 'Normalization', 'probability');
legend('smokers', 'non smokers');
title('Smoker and Non smoker samples normalized in probability');
%% Q2 %%
%% Part 1 %%
clear
clc
%% Preparing dataset %%

% we have 5 classes -> K = 5
% we have 8 features -> N = 8

dataset = load("./resources/nursery.mat");
dataset = dataset.nursery;
dataset = rmmissing(dataset); % remove NaN from data

global parents has_nurs form children housing finance social health class

parents = table2array(unique(dataset(:, 1)));
has_nurs = table2array(unique(dataset(:, 2)));
form = table2array(unique(dataset(:, 3)));
children = table2array(unique(dataset(:, 4)));
housing = table2array(unique(dataset(:, 5)));
finance = table2array(unique(dataset(:, 6)));
social = table2array(unique(dataset(:, 7)));
health = table2array(unique(dataset(:, 8)));
class = table2array(unique(dataset(:, 9)));

%% Part 2 %%
lables = randperm(9720);
train_dataset = dataset(lables(1:4860), :);
test_dataset = dataset(lables(4861:9720), :);
%% Part 3 %%
temp_dataset = dataset;
dataset = train_dataset;

%1
for i = 1:length(parents)
    for j = 1:length(class)
        a = find((table2array(dataset(:, 1)) == parents(i)) & (table2array(dataset(:, 9)) == class(j)));
        p1(i, j) = length(a);
    end
end
%2
for i = 1:length(has_nurs)
    for j = 1:length(class)
        a = find((table2array(dataset(:, 2)) == has_nurs(i)) & (table2array(dataset(:, 9)) == class(j)));
        p2(i, j) = length(a);
    end
end
%3
for i = 1:length(form)
    for j = 1:length(class)
        a = find((table2array(dataset(:, 3)) == form(i)) & (table2array(dataset(:, 9)) == class(j)));
        p3(i, j) = length(a);
    end
end
%4
for i = 1:3
    for j = 1:length(class)
        a = find((table2array(dataset(:, 4)) == i) & (table2array(dataset(:, 9)) == class(j)));
        p4(i, j) = length(a);
    end
end
p4 = p4(1:3, :);
%5
for i = 1:length(housing)
    for j = 1:length(class)
        a = find((table2array(dataset(:, 5)) == housing(i)) & (table2array(dataset(:, 9)) == class(j)));
        p5(i, j) = length(a);
    end
end
%6
for i = 1:length(finance)
    for j = 1:length(class)
        a = find((table2array(dataset(:, 6)) == finance(i)) & (table2array(dataset(:, 9)) == class(j)));
        p6(i, j) = length(a);
    end
end
%7
for i = 1:length(social)
    for j = 1:length(class)
        a = find((table2array(dataset(:, 7)) == social(i)) & (table2array(dataset(:, 9)) == class(j)));
        p7(i, j) = length(a);
    end
end
%8
for i = 1:length(health)
    for j = 1:length(class)
        a = find((table2array(dataset(:, 8)) == health(i)) & (table2array(dataset(:, 9)) == class(j)));
        p8(i, j) = length(a);
    end
end
%9
for i = 1:length(class)
    a = find(table2array(dataset(:, 9)) == class(i));
    p9(i) = length(a);
end

p1 = p1./4860;
p2 = p2./4860;
p3 = p3./4860;
p4 = p4./4860;
p5 = p5./4860;
p6 = p6./4860;
p7 = p7./4860;
p8 = p8./4860;
p9 = p9./4860;
%% Part 4 %%
temp = zeros(length(class), 1);
for i = 1:4860
    x = test_dataset(i, 1:8);
    i1 = find(parents == table2array(x(:, {'parents'})));
    i2 = find(has_nurs == table2array(x(:, {'has_nurs'})));
    i3 = find(form == table2array(x(:, {'form'})));
    i4 = find(children == table2array(x(:, {'childern'})));
    i5 = find(housing == table2array(x(:, {'housing'})));
    i6 = find(finance == table2array(x(:, {'finance'})));
    i7 = find(social == table2array(x(:, {'social'})));
    i8 = find(health == table2array(x(:, {'health'})));
    for k = 1:length(class)
        temp(k, 1) = p9(k) .* p1(i1, k) .* p2(i2, k) .* p3(i3, k) .* p4(i4, k) .* p5(i5, k) .* p6(i6, k) .* p7(i7, k) .* p8(i8, k);
    end
    m = max(temp);
    for k = 1:length(class)
        if(temp(k) == m)
            check(i) = k;
            break
        end
    end
end

hit = 0;
miss = 0;

for i = 1:4860
    if(table2array(test_dataset(i, {'class'})) == class(check(i)))
        hit = hit + 1;
    else
        miss = miss + 1;
    end
end
fprintf("hit: %d, miss %d\n\n", hit, miss);
%% Q3 %% 
clc
clear
%% Part 1 %%
load("./resources/forestfires.mat");
data = forestfires;

month_num = [];
day_num = [];

for i = 1:517
    month = table2array(data(i, {'month'}));
    day = table2array(data(i, {'day'}));
    
    switch (month)
        case 'jan'
            a = 1;
        case 'feb'
            a = 2;
        case 'mar'
            a = 3;
        case 'apr'
            a = 4;
        case 'may'
            a = 5;
        case 'jun'
            a = 6;
        case 'jul'
            a = 7;
        case 'aug'
            a = 8;
        case 'sep'
            a = 9;
        case 'oct'
            a = 10;
        case 'nov'
            a = 11;
        case 'dec'
            a = 12;
    end
    
    switch (day)
        case 'sat'
            b = 1;
        case 'sun'
            b = 2;
        case 'mon'
            b = 3;
        case 'thu'
            b = 4;
        case 'wed'
            b = 5;
        case 'tue'
            b = 6;
        case 'fri'
            b = 7;
    end
    day_num(i, 1) = b;
    month_num(i, 1) = a;
end

data.month = [];
data.day = [];

t_day_month = array2table([day_num, month_num], 'VariableName', {'day', 'month'});
data = [data, t_day_month]; % now the data is fully numeric
data = data(:, [1, 2, 12, 13, 3:11]);
data_array = table2array(data);
%% Part 2 %%
lable = randperm(517);

train = data(lable(1:258), :);
test = data(lable(259:517), :);

lm = fitlm(train);
%% Part 3 %%
%refer to report
%% Part 4 %%
fit = lm.Fitted;
MSE = (fit - table2array(train(:, {'area'}))) .^2;
MSE = mean(MSE);
fprintf("train MSE is: %f\n", MSE);
%% Part 5 %%
test_x = test;
test_x(:, {'area'}) = [];
test_y = test(:, {'area'});
pred = lm.predict(table2array(test_x));
MSE = (table2array(test_y) - pred) .^ 2;
MSE = mean(MSE);
fprintf("test MSE is: %f\n", MSE);
%% Functions %%

function  W2 = make_W2(sample_mean, n, mu, s)
    W2 = (sample_mean - mu)./(s/sqrt(n));
end