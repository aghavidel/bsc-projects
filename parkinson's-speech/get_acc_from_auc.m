% This function converts the Area Under Curve (AUC) into accuracy in
% percent, as per the method in the paper, if auc is bigger than 0.5, then
% we just multiply it by 100, else we first subtract it from 1 and then
% multiply it by 100.
%
%   Inputs:
%   --> auc: An AUC value.
%
%   Outputs:
%   => acc: An accuracy value.

function acc = get_acc_from_auc(auc)
    if auc > 0.5
        acc = auc * 100;
    else
        acc = (1 - auc) * 100;
    end
end