% This function outptus the calculated values for a frame set.
% First at least N different people both in the PD and Healthy class are
% considered and then given a frame count, the frames in an interval with
% that length around the center frame are considered for analysis, this
% gives us 2 * frame_count + 1 for each individual, the reason we avoid
% going to the edges of the signal and thus start in the middle is:
%
%   1) Sometimes there is a silence in the beginning or the end and it will
%       ruin the rest of the runtime, so we start at the middle to make sure we
%       don't go anywhere near them.
%   2) Cutting the frames disrupts the local stationary assumption that we
%       allways make when analyzing speech signal, going to the middle however
%       remedies this problem a bit.
%
% After computing the frames, QOQ, NAQ, te, tp and Ee are calculated, the
% Rk hoever remains, we calculate them seperatly since it is much easier to
% just do it here.
%
% After this, the distribution of each parameter is shown with a histogram
% and the mean and std of each parameter is printed, then the desired
% parameter is returned as a struct containing fields PD for the Parkinsons
% Disease and Healthy for the healthy parameters.
% If the user wants all the parameters, then a full struct with the form:
%
%           vals.PARAM_NAME_IN_CAPITAL.ClassName
%
% is returned.
%
% Inputs:
%
% --> pd_file, healthy_file: The path to the healthy and PD datasets
%       relative to the working dir.
% --> pd_set, healthy_set: The 'dir()' struct of the healthy and PD folders,
%       we only need tha name fields to get the recording folder.
% --> N: The number of recordings to consider for each class, in the end we
%       will have to discriminate 2*N different people.
% --> N_frame: The frame_count above, don't give a big number, it will
%       significantly increase the runtime duration.
% --> nv, ng: The LPC orders for the vocal tract and glottal pulse
%       estimation.
% --> tw, tg: The window length and window hop length for frame making in
%       ms.
% --> param: A single string that defines what parameter you wish to be
%       returned to you, for now we have 'QOQ', 'NAQ', 'TP', 'TE', 'RK' and 'E',
%       if you supply anything other than that (for example 'ALL'), the function
%       will return all the parameters in the aforementioned full struct.
%
%   Outputs:
% => vals: The parameter struct object.

function vals = check_all(pd_file, pd_set, healthy_file, healthy_set, N, N_frame, nv, ng, tw, th, param)
    frames_pd = [];
    frames_healthy = [];
    
    for i = 3:min([length(pd_set), N])
        [x_pd, fs_pd] = audioread([pd_file, pd_set(i).name]);
        x_pd = x_pd ./ max(abs(x_pd));
        frames_pd_full = make_frames(x_pd, fs_pd, tw, th);
        k = floor(size(frames_pd_full, 2) / 2);
        indices = linspace(k - N_frame, k + N_frame, 2 * N_frame + 1);
        new_frames = frames_pd_full(:, indices);
        frames_pd = [frames_pd, new_frames];
    end
    
    for i = 3:min([length(healthy_set), N])
        [x_healthy, fs_healthy] = audioread([healthy_file, healthy_set(i).name]);
        x_healthy = x_healthy ./ max(abs(x_healthy));
        new_frames = make_frames(x_healthy, fs_healthy, tw, th);
        k = floor(size(new_frames, 2) / 2);
        indices = linspace(k - N_frame, k + N_frame, 2 * N_frame + 1);
        frames_healthy = [frames_healthy, new_frames(:, indices)];
    end
    
    [q_pd, n_pd, tp_pd, te_pd, e_pd] = get_mean_qoq_naq(frames_pd, fs_pd, nv, ng);
    [q_healthy, n_healthy, tp_healthy, te_healthy, e_healthy] = get_mean_qoq_naq(...
        frames_healthy, fs_healthy, nv, ng);
    
    rk_pd = te_pd ./ tp_pd- 1;
    rk_healthy = te_healthy ./ tp_healthy - 1;
    
    subplot(231);
    hold on
    histogram(q_pd, 20, 'FaceColor', 'r');
    histogram(q_healthy, 20, 'FaceColor', 'b');
    title('QOQ');
    
    subplot(232);
    hold on
    histogram(n_pd, 20, 'FaceColor', 'r');
    histogram(n_healthy, 20, 'FaceColor', 'b');
    title('NAQ');
    
    subplot(233);
    hold on
    histogram(tp_pd, 20, 'FaceColor', 'r');
    histogram(tp_healthy, 20, 'FaceColor', 'b');
    title('T_p');
    
    subplot(234);
    hold on
    histogram(te_pd, 20, 'FaceColor', 'r');
    histogram(te_healthy, 20, 'FaceColor', 'b');
    title('T_e');
    
    subplot(235);
    hold on
    histogram(rk_pd, 20, 'FaceColor', 'r');
    histogram(rk_healthy, 20, 'FaceColor', 'b');
    title('R_{k}');
    
    subplot(236);
    hold on
    histogram(e_pd, 20, 'FaceColor', 'r');
    histogram(e_healthy, 20, 'FaceColor', 'b');
    title('E_{e}');
     
    fprintf('PD Statistics \n\n');
    fprintf('%-20s %20s \n', 'Mean QOQ:', num2str(mean(q_pd)));
    fprintf('%-20s %20s \n', 'Mean NAQ:', num2str(mean(n_pd)));
    fprintf('%-20s %20s \n', 'Mean tp:', num2str(mean(tp_pd)));
    fprintf('%-20s %20s \n', 'Mean te:', num2str(mean(te_pd)));
    fprintf('%-20s %20s \n', 'Mean rk:', num2str(mean(rk_pd)));
    fprintf('%-20s %20s \n', 'Mean e:', num2str(mean(e_pd)));
    fprintf('%-20s %20s \n', 'STD of QOQ:', num2str(std(q_pd)));
    fprintf('%-20s %20s \n', 'STD of NAQ:', num2str(std(n_pd)));
    fprintf('%-20s %20s \n', 'STD of tp:', num2str(std(tp_pd)));
    fprintf('%-20s %20s \n', 'STD of te:', num2str(std(te_pd)));
    fprintf('%-20s %20s \n', 'STD of rk:', num2str(std(rk_pd)));
    fprintf('%-20s %20s \n\n\n', 'STD of e:', num2str(std(e_pd)));
    
    fprintf('Healthy Statistics\n\n');
    fprintf('%-20s %20s \n', 'Mean QOQ:', num2str(mean(q_healthy)));
    fprintf('%-20s %20s \n', 'Mean NAQ:', num2str(mean(n_healthy)));
    fprintf('%-20s %20s \n', 'Mean tp:', num2str(mean(tp_healthy)));
    fprintf('%-20s %20s \n', 'Mean te:', num2str(mean(te_healthy)));
    fprintf('%-20s %20s \n', 'Mean rk:', num2str(mean(rk_healthy)));
    fprintf('%-20s %20s \n', 'Mean e:', num2str(mean(e_healthy)));
    fprintf('%-20s %20s \n', 'STD of QOQ:', num2str(std(q_healthy)));
    fprintf('%-20s %20s \n', 'STD of NAQ:', num2str(std(n_healthy)));
    fprintf('%-20s %20s \n', 'STD of tp:', num2str(std(tp_healthy)));
    fprintf('%-20s %20s \n', 'STD of te:', num2str(std(te_healthy)));
    fprintf('%-20s %20s \n', 'STD of rk:', num2str(std(rk_healthy)));
    fprintf('%-20s %20s \n\n\n', 'STD of e:', num2str(std(e_healthy)));
    
    if strcmp(param, 'QOQ')
        vals = struct('PD', q_pd, 'Healthy', q_healthy);
    elseif strcmp(param, 'NAQ')
        vals = struct('PD', n_pd, 'Healthy', n_healthy);
    elseif strcmp(param, 'TP')
        vals = struct('PD', tp_pd, 'Healthy', tp_healthy);
    elseif strcmp(param, 'TE')
        vals = struct('PD', te_pd, 'Healthy', te_healthy);
    elseif strcmp(param, 'RK')
        vals = struct('PD', rk_pd, 'Healthy', rk_healthy);
    elseif strcmp(param, 'E')
        vals = struct('PD', e_pd, 'Healthy', e_healthy);
    else
        vals = struct();
        
        vals.PD.Q = q_pd;
        vals.PD.N = n_pd;
        vals.PD.TP = tp_pd;
        vals.PD.TE = te_pd;
        vals.PD.RK = rk_pd;
        vals.PD.E = e_pd;
        
        vals.Healthy.Q = q_healthy;
        vals.Healthy.N = n_healthy;
        vals.Healthy.TP = tp_healthy;
        vals.Healthy.TE = te_healthy;
        vals.Healthy.RK = rk_healthy;
        vals.Healthy.E = e_healthy;
    end   
end