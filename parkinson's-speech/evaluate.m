%   This function evaluates the final parameters by creating a logistic
%   regression model on each one and calculating the ROC curve and it's
%   features, in the end the results are plotted and the final AUC and
%   Accuracy values are printed.
%
%   Inputs:
%   --> pd_data, healthy_data: They are structs containing all the
%         necessary fields, i.e. Q, N, TP, TE, RK and E for PD and Healthy
%         patients.
%
%   Outputs:   
%   => model: An struct containing the evaluated model for each of the
%         parameters in fields identical to the data structs (although they 
%         are in lowercase instead of capital)

function model = evaluate(pd_data, healthy_data)
    labels = [zeros(length(pd_data.Q), 1); ones(length(healthy_data.Q), 1)];
    
    q = [pd_data.Q; healthy_data.Q];
    n = [pd_data.N; healthy_data.N];
    tp = [pd_data.TP; healthy_data.TP];
    te = [pd_data.TE; healthy_data.TE];
    rk = [pd_data.RK; healthy_data.RK];
    e = [pd_data.E; healthy_data.E];
    
    model = struct();
    model.q = fitglm(q, labels, 'Distribution', 'binomial', 'Link', 'logit');
    model.n = fitglm(n, labels, 'Distribution', 'binomial', 'Link', 'logit');
    model.tp = fitglm(tp, labels, 'Distribution', 'binomial', 'Link', 'logit');
    model.te = fitglm(te, labels, 'Distribution', 'binomial', 'Link', 'logit');
    model.rk = fitglm(rk, labels, 'Distribution', 'binomial', 'Link', 'logit');
    model.e = fitglm(e, labels, 'Distribution', 'binomial', 'Link', 'logit');
    
    scores = struct();
    scores.q = model.q.Fitted.Probability;
    scores.n = model.n.Fitted.Probability;
    scores.tp = model.tp.Fitted.Probability;
    scores.te = model.te.Fitted.Probability;
    scores.rk = model.rk.Fitted.Probability;
    scores.e = model.e.Fitted.Probability;
    
    X = struct();
    Y = struct();
    AUC = struct();
    
    [X.q, Y.q, ~, AUC.q] = perfcurve(labels, scores.q, 0);
    [X.n, Y.n, ~, AUC.n] = perfcurve(labels, scores.n, 0);
    [X.tp, Y.tp, ~, AUC.tp] = perfcurve(labels, scores.tp, 0);
    [X.te, Y.te, ~, AUC.te] = perfcurve(labels, scores.te, 0);
    [X.rk, Y.rk, ~, AUC.rk] = perfcurve(labels, scores.rk, 0);
    [X.e, Y.e, ~, AUC.e] = perfcurve(labels, scores.e, 0);
    
    hold on
    plot(X.q, Y.q);
    plot(X.n, Y.n);
    plot(X.tp, Y.tp);
    plot(X.te, Y.te);
    plot(X.rk, Y.rk);
    plot(X.e, Y.e);
    title('Final Evaluaion');
    
    legend('QOQ', 'NAQ', 't_{p}', 't_{e}', 'R_{k}', 'E_{e}', 'Location', 'northwest');
    
    fprintf('AUC values: \n\n');
    fprintf('%20s: %20s\n', 'QOQ', num2str(AUC.q));
    fprintf('%20s: %20s\n', 'NAQ', num2str(AUC.n));
    fprintf('%20s: %20s\n', 'tp', num2str(AUC.tp));
    fprintf('%20s: %20s\n', 'te', num2str(AUC.te));
    fprintf('%20s: %20s\n', 'Rk', num2str(AUC.rk));
    fprintf('%20s: %20s\n', 'E', num2str(AUC.e));
    
    fprintf('\n\nAccuracy Values: \n\n');
    fprintf('%20s: %20s\n', 'QOQ', num2str(get_acc_from_auc(AUC.q)));
    fprintf('%20s: %20s\n', 'NAQ', num2str(get_acc_from_auc(AUC.n)));
    fprintf('%20s: %20s\n', 'tp', num2str(get_acc_from_auc(AUC.tp)));
    fprintf('%20s: %20s\n', 'te', num2str(get_acc_from_auc(AUC.te)));
    fprintf('%20s: %20s\n', 'Rk', num2str(get_acc_from_auc(AUC.rk)));
    fprintf('%20s: %20s\n', 'E', num2str(get_acc_from_auc(AUC.e)));
end