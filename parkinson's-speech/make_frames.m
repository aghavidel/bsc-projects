% This function creates frames of a given length and a given hop from a big
% signal, no windowing happens here however, we use Hamming window latter.
%
%   Inputs:
%   --> signal: A long signal that we want to frame.
%   --> fs: The sampling freq.
%   --> t_win: The length of the window in ms.
%   --> t_hop: The hop distance when moving to the next frame.
%
%   Outputs:
%   => windowed_signal: The framed signal as a 2D array.

function windowed_signal = make_frames(signal, fs, t_win, t_hop)
    l_win = floor(t_win * fs * 1e-3);
    l_hop = floor(t_hop * fs * 1e-3);
    q = floor((length(signal) - l_win) / l_hop)+ 1;
    windowed_signal = zeros(l_win, q);
    
    for i = 1:q
        start = (i-1) * l_hop + 1;
        finish = start + l_win - 1;
        windowed_signal(:, i) = signal(start:finish, 1);
    end
end