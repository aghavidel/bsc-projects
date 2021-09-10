% This function calculates the QOQ from the glottal flow.
% This is done by first seperating the flow peaks so that we have several
% seperated flow pulses, then for each of them we calculate the points that
% they cross 50 percent of their max values and determine their distances,
% call this value delta_t and call the single flow pulse length N, in the end the
% QOQ is calculated as the mean of the vector delta_t / N.
%
% Inputs:
% --> flow: The Glottal flow
% --> fs: The sampling rate.
%
% Outputs:
% => q: The calculated QOQ.

function q = qoq(flow, fs)
    [~, locs] = findpeaks(-[flow(1:end-1); 4000], fs, 'MinPeakDistance', 4e-3);
    holder = [];
    for i = 1:length(locs)-1
        start = floor(locs(i) * fs);
        finish= floor(locs(i+1) * fs);
        if start == 0
            start = 1;
        end
        cut_flow = flow(start:finish);
        cut_flow = cut_flow > max(cut_flow) * 0.50;
        holder = [holder; nnz(cut_flow) / length(cut_flow)];
    end
    q = mean(holder);
end