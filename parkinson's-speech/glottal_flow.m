% This function calculates the glottal flow and derivative from a given
% frame.
%   1) The IAIF algorithm is used to get the glottal derivative.
%   2) The pitch is then calculated by 'get_pitch_from_derivative'.
%   3) The flow derivative is integrated with a filter to give the flow.
%   4) The lingering DC components are computed using a moving average
%       filter with a long window, we found out that the length equal to the
%       pitch works nice.
%   5) The flow is calculated by removing the previous DC component 
%       from the output of step 3.
%   6) We want clean pulses from start to finish, there is always some
%       incomplete pulses near the beginning or the end of the frame, we
%       will find the nadirs of the flow and then cut the flow and flow
%       derivative from the first nadir to the last, if we for example have
%       m + 1 detected nadirs, we will get m clean pulses out of this step.
%
%   Inputs:
%   --> signal: A single frame of signal.
%   --> fs: The sampling freq.
%   --> nv: The LPC order for the vocal tract estimation (High Number).
%   --> ng: The LPC order fot the glottal pulse estimation (Low Number).
%   
%   Outputs:
%   => flow: The glottal flow.
%   => flow_der: The glottal flow derivative.
%   => p: The pitch of the frame.

function [flow, flow_der, p] = glottal_flow(signal, fs, nv, ng)
    [~, ag, ~, s_g1] = gfmiaif(signal, nv, ng, 0.99, hamming(length(signal)));
    glottis_derivative_pulse = filter([0, -ag(2:end)], 1, s_g1);
    [p, ~] = get_pitch_from_derivative(glottis_derivative_pulse, fs);
    flow_moving = filter(1, [1, -0.999], glottis_derivative_pulse);
    pitch_hop =floor(p * 1e-3 * fs);
    flow_dc = flow_moving - movmean(flow_moving, pitch_hop);
    [~, locs] = findpeaks(-1 .* flow_dc, fs, 'MinPeakDistance', 4e-3);
    locs_start = floor(min(locs) * fs);
    locs_finish = floor(max(locs) * fs);
    flow = flow_dc(locs_start:locs_finish);
    flow_der = glottis_derivative_pulse(locs_start:locs_finish);
end