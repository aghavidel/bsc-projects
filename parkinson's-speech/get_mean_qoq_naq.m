% This function calculates pretty much all the parameters (don't mind the
% name please :) ), it gets a set of frames and calculate the mean of each
% parameter in each frame.
%
%   Inputs:
%   --> frame: A set of frames.
%   --> fs: Sampling freq.
%   --> nv: LPC order for the vocal tract.
%   --> ng: LPC order for the glottal pulse.
%   
%   Outputs:
%   => q: The mean QOQ.
%   => n: The mean NAQ.
%   => tp: The mean tp.
%   => te: The mean te.
%   => e: The mean Ee.

function [q, n, tp, te, e] = get_mean_qoq_naq(frame, fs, nv, ng)
    q = [];
    n = [];
    tp = [];
    te = [];
    e = [];
    for i = 1:size(frame, 2)
        [flow, flow_der, p] = glottal_flow(frame(:, i), fs, nv, ng);
        new_q = qoq(flow, fs);
        [new_n, new_tp, new_te, new_e] = naq(flow, flow_der, fs, p);
        q = [q; new_q];
        n = [n; new_n];
        tp = [tp; new_tp];
        te = [te; new_te];
        e = [e; new_e];
    end
end