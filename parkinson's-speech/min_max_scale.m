% Min-Max scaling for vector x, this function normalizes the given vector
% to the range (m, M), if the vector has length one, it will return the
% mean of m and M.
%
%   Inputs:
%   --> x: The vector to be normalized.
%   --> m, M: The min and Max values of the output vector.
%
%   Outputs:
%   => x_scale: Normalized vector.

function x_scale = min_max_scale(x, m, M)
    if length(x) == 1
        x_scale = (m + M) / 2;
    else
        x_std = (x - min(x)) / (max(x) - min(x));
        x_scale = x_std * (M - m) + m;
    end
end