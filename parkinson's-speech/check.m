% This function shall not be used in the final code, it is a relic of the
% debugging age and does the same thing that 'check_all' does but only on a
% single person for a given frame count.

function check(pd_file, pd_set, healthy_file, healthy_set, N, nv, ng, tw, th)
    frames_pd = [];
    frames_healthy = [];
    i = 13;
    while size(frames_pd, 2) < N
        [x_pd, fs_pd] = audioread([pd_file, pd_set(i).name]);
        x_pd = x_pd ./ max(abs(x_pd));

        frames_pd_full = make_frames(x_pd, fs_pd, tw, th);
        new_frames = frames_pd_full(:, 50:end-50);
        frames_pd = [frames_pd, new_frames];
        i = i+1;
    end
    
    i = 10;
    while size(frames_healthy, 2) < N
        [x_healthy, fs_healthy] = audioread([healthy_file, healthy_set(i).name]);
        x_healthy = x_healthy ./ max(abs(x_healthy));
        new_frames = make_frames(x_healthy, fs_healthy, tw, th);
        frames_healthy = [frames_healthy, new_frames];
        i = i+1;
    end
    
    frames_healthy = frames_healthy(:, 1:N);
    frames_pd = frames_pd(:, 1:N);
    
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
    title('T_{p}');
    
    subplot(234);
    hold on
    histogram(te_pd, 20, 'FaceColor', 'r');
    histogram(te_healthy, 20, 'FaceColor', 'b');
    title('T_{e}');
    
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
end