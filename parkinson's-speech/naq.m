% This function calculates the NAQ, tp, te and E for a given flow and flow
% dervative, we also use the pitch.
% For the formula you may refer to the report, basically, we first seperate
% individual flow pulses by finding the location of nadirs, this in turn
% correspondes to the location of the zero crossings for the flow
% derivative, allowing us to seperate them into individual pulses, since
% matlab only lables peaks if they are actually higher, it does not detect
% peaks that happen to be in the end of the frame (which they very well
% can), in order to make sure this does not happend, we calculate the peaks
% on the all the flow values, but replace the last value with a really
% large number so that when we actually use findpeaks on the negative of
% the flow, we get the peak in the end.
%   
%   Inputs:
%
%   --> flow: The glottal flow.
%   --> flow_der: The glottal flow derivative.
%   --> fs: The sampling frequency.
%   --> p: The pitch in ms.
%
%   Outputs:
%   => n: The parameter NAQ in the paper.   
%   => tp, te: The parameter tp and te in the paper.
%   => e: The parameter Ee in the paper.

function [n, tp, te, e] = naq(flow, flow_der, fs, p)
    [~, locs] = findpeaks(-[flow(1:end-1); 40000], fs, 'MinPeakDistance', 2.5e-3);
    holder = [];
    holder_tp = [];
    holder_te = [];
    holder_e = [];
    for i = 1:length(locs)-1
        start = floor(locs(i) * fs);
        finish= floor(locs(i+1) * fs);
        if start == 0
            start = 1;
        end
        cut_flow = flow(start:finish);
        cut_flow_der = flow_der(start:finish);
        [~, tp] = max(cut_flow);
        A = 0.5 * (max(cut_flow) + abs(min(cut_flow)));
        [~, te] = min(cut_flow_der);
        E = abs(min(cut_flow_der));
        holder = [holder; (A / p)];
        holder_tp = [holder_tp; tp];
        holder_te = [holder_te; te];
        holder_e = [holder_e; E];
    end
    
    n_vec = holder ./ min_max_scale(holder_e, 1, 2);
    n = mean(n_vec);
    e = mean(min_max_scale(holder_e, 1, 2));
    tp = (mean(holder_tp) * 1e3 / fs) / p;
    te = (mean(holder_te) * 1e3 / fs) / p;
end