% This function extracts the pitch from the flow derivative, this is very
% simple, since the peaks or nadirs of the flow derivative are seperated by
% the pitch length, so we just take the distance of two successive nadirs
% and calculae the pitch.
%
% Note: The minimum pitch for most humans is about 4 ms (corresponding to a
% freq. of 250 Hz, since we do not want to use the smaller nadirs and only
% the big ones that mark the beginning and end of a pulse, we will
% constraine the peaks to be at least this much apart from each other, we
% will always do this when we use findpeaks().
%
%   Inputs:
%   --> signal: The frame.
%   --> fs: Sampling freq.
%   
%   Outputs:
%   => pitch: The pitch in ms.
%   => pitch_freq: The pitch freq. in Hz.

function [pitch, pitch_freq] = get_pitch_from_derivative(signal, fs)
    [peaks, locs] = findpeaks(signal, fs, 'MinPeakDistance', 4e-3);
    [~, I] = maxk(peaks, 2);
    
    pitch = abs(locs(I(2)) - locs(I(1))) * 1e3;
    pitch_freq = 1 / abs(locs(I(2)) - locs(I(1)));
end