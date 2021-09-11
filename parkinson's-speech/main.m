%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% For detailed discription refer to the report.pdf file in the same
% directory as this file.
%
% For plot images, refer to the 'plots' folder.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

clear
clc 
%% Constants Go Here %%
WINDOW_TIME = 32;                       % The length of window in ms
WINDOW_HOP = 16;                        % The hop distance of frames in ms
GLOTTAL_LPC_ORDER = 5;                  % LPC order for glottal pulse
VOCAL_TRACT_LPC_ORDER = 40;             % LPC order for vocal tract filter
NUMBER_OF_SAMPLES = 50;                 % How many sound samples of each class to consider
FRAME_AMP = 5;                          % Governs the number of frames used in each parameter estimation
data_path = './Data/Subren/';           % The path to your dataset relative to working dir.
%% Constants End Here %%

% First we get the set of folders for 'Healthy' and 'PD' classes.

healthy_set = dir([data_path, 'Healthy/']);
pd_set = dir([data_path, 'PD/']);
%% Extracting Glottal Flow %%

% 1) An audio file is loaded and normalized.
% 2) The audio file is framed into windows of the given length of hop size.
% 3) Glottal flow is estimated by:
%           a) Running the IAIF algorithm and exracting the glottal flow
%               derivative.
%           b) Integrating the derivative with a filter to get the glottal
%               flow.

[x, fs] = audioread([data_path, 'PD/', pd_set(10).name]);
x = x ./ max(abs(x));
frames = make_frames(x, fs, WINDOW_TIME, WINDOW_HOP);

[flow, flow_der, p] = glottal_flow(frames(:, 350), fs, VOCAL_TRACT_LPC_ORDER, GLOTTAL_LPC_ORDER);
t = (1:length(flow)) / fs;

figure
subplot(211);
plot(t, flow_der);
xlabel('t(s)');
title('Example Glottal Flow derivative');
subplot(212);
hold on
plot(t, flow);
xlabel('t(s)');
title('Example Glottal Flow');

% saveas(gcf, './plots/glottal_shape.png', 'png');
%% Inference from parameters %%

% We aim to create a simple classifier, for that we need some
% classification features, the paper will tell us what parameters work
% best, now have created two function to help us in this task.
%
%  -> The 'check_all' function will extract the needed parameters across 
%       dfferent voice samples (i.e. different peoples).
%  -> The 'check' function will extract the neede parameters from all
%       frames for ONLY a single person to get the best estimate for a
%       single person (This function shall not be used, it is a relic of the 
%       debugging age).

clc
% vals is a struct containing the parameters requested.
vals = check_all([data_path, 'PD/'], pd_set, [data_path, 'Healthy/'], healthy_set, ...
    NUMBER_OF_SAMPLES, FRAME_AMP, VOCAL_TRACT_LPC_ORDER, GLOTTAL_LPC_ORDER,...
    WINDOW_TIME, WINDOW_HOP, 'QOQ');

% saveas(gcf, './plots/parameters_distribution.png', 'png');

pd_data = vals.PD;
healthy_data = vals.Healthy;

datas = [pd_data; healthy_data];
labels = [zeros(length(pd_data), 1); ones(length(healthy_data), 1)];

model = fitglm(datas, labels, 'Distribution', 'binomial', 'Link', 'logit');
scores = model.Fitted.Probability;

[X, Y, T, AUC] = perfcurve(labels, scores, 0);
disp(['AUC is : ', num2str(AUC)]);
figure();
plot(X, Y);
title('ROC Curve for QOQ');
xlabel('True positive rate');
ylabel('False positive rate');

% saveas(gcf, './plots/ROC_QOQ.png', 'png');
%% Full Feature Extraction %%

% Now instead of just QOQ, we will use all the parameters and determine
% which one works best.

clc
vals = check_all([data_path, 'PD/'], pd_set, [data_path, 'Healthy/'], healthy_set, ...
    NUMBER_OF_SAMPLES, FRAME_AMP, VOCAL_TRACT_LPC_ORDER, GLOTTAL_LPC_ORDER,...
    WINDOW_TIME, WINDOW_HOP, 'ALL');

pd_data = vals.PD;
healthy_data = vals.Healthy;
%% Final Evaluation %%

% Now we will fit a logistic regression model to each feature and determine
% their performance.

clc
model = evaluate(pd_data, healthy_data);
% saveas(gcf, './plots/final.png', 'png');
