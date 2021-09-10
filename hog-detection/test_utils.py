from skimage.transform import pyramid_gaussian
from classifier_utils import platt_scale
from visual_utils import plot_cv_with_plt
from data_utils import get_image_hog
import numpy as np
import tqdm
import matplotlib.pyplot as plt

"""
This file contains utilities for evaluating the final classifier on images.
"""

def sliding_window(chosen_classifier, image, win_size, downscale, max_layer, stride_size, batch_size):
    """
    Do sliding window on a given image with a given classifier.

    This is done by just sliding a window of a given size in a given
    stride on an image, windows are then process to get their HOG
    descriptors and the results are accumulated until they reach
    a batch_size, then the whole batch is consumed by the classifier
    to return a set of scores (which are Platt scaled later), the 
    scores and window anchor points are saved and later returned.

    Inputs:
    --> chosen_classifier: A sklearn classifier.
    --> image: The image to use, assumed to be RGB.
    --> win_size: The size of the window, assumed to be the same as the
        HOG window size.
    --> downscale: The image can be downscaled in gaussian pyramid to
        find faces in larger scales, this is the downscale factor.
    --> max_layer: Maximum number of downscales in the gaussian pyramid.
    --> stride_size: The stride of the sliding window.
    --> batch_size: Number of windows that are consumed by the classifier
        at the same time, speeds up the process very much ...

    Returns:
    ==> num_list: A list of Platt scaled scores for each window.
    ==> anchor_list: A list of 4 tuple anchor points for each image.
    """

    c = image.shape[-1]

    assert c == 3, "Wrong image type, an RGB image is needed."
    
    pyramid = list(pyramid_gaussian(image, max_layer=max_layer, downscale=downscale, multichannel=True))
    
    win_list = []
    anchor_list = []
    num_list = []

    for t in range(len(pyramid)):
        p = (pyramid[t] * 255).astype(np.uint8)     # Pyramid Gaussian outputs float instead of uint8!
        h, w, _ = p.shape
        kh = 1 + int(np.floor((h-win_size)/stride_size))
        kw = 1 + int(np.floor((w-win_size)/stride_size))

        if kh < 1  or kw < 1:
            continue 
        else:
            for ih in tqdm.tqdm(range(kh)):
                for iw in range(kw):
                    new_win = p[ih*stride_size:ih*stride_size+win_size, iw*stride_size:iw*stride_size+win_size, :]
                    win_list.append(np.squeeze(get_image_hog(new_win)))

                    x1, y1 = int(ih*stride_size*downscale**t), int(iw*stride_size*downscale**t)
                    x2, y2 = int((ih*stride_size+win_size)*downscale**t), int((iw*stride_size+win_size)*downscale**t)
                    anchor_list.append((x1, y1, x2, y2))

                    if len(win_list) % batch_size == 0:
                        new_nums = platt_scale(chosen_classifier.decision_function(np.asarray(win_list)))
                        num_list.extend(new_nums)
                        win_list = []

    if len(win_list) > 0:
        new_nums = platt_scale(chosen_classifier.decision_function(np.asarray(win_list)))
        num_list.extend(new_nums)

    return num_list, anchor_list

def check_scores(outputs, score_th=0.9):
    """
    Returns the index of scores larger than a threshold value.

    Inputs:
    --> outputs: A list of scores.
    --> score_th: A score threshold, scores under this value are
        ignored.

    Returns:
    ==> Index of chosen scores.
    """

    indices = np.arange(len(outputs))
    indicator = outputs >= score_th
    return indices[indicator]

def plot_boxes(image, box_xy_list):
    """
    Plot boxes on an image given a list of anchor points.

    Inputs:
    --> image: An RGB image.
    --> box_xy_list: List of 4 tuple anchor points.

    Returns:
    ==> Plots the image with the boxes on it ...
    """

    border_color = [50, 200, 100]                   # This is the color of the border (Deep green)
    t = 5                                                       # This is the thickness of the lines

    img_boxed = np.copy(image)
    
    h, w, _ = np.shape(image)
    for i in range(len(box_xy_list)):
        x1, y1, x2, y2 = box_xy_list[i]
        
        if x1-t<0 or x2+t >= h:
            continue
        if y1-t<0 or y2+t >= w:
            continue

        for pixel_x in range(x1-t, x2+t):
            img_boxed[pixel_x, (y1-t):y1, :] = border_color
            img_boxed[pixel_x, y2:(y2+t), :] = border_color
        for pixel_y in range(y1-t, y2+t):
            img_boxed[(x1-t):x1, pixel_y, :] = border_color
            img_boxed[x2:(x2+t), pixel_y, :] = border_color

    plt.figure(figsize=(12, 12))
    plt.axis('off')
        
    plot_cv_with_plt(img_boxed)

def soft_nms(dets, sc, Nt=0.3, sigma=0.5, thresh=0.001, method=2):
    """
    Performs Hard of Soft NMS on a given list of scores and anchor points, this helps us
    remove many of the windows that overlap too much (they are not necessarily wrong, but
    lead to a very cluttered final output).

    Inputs:
    --> dets: The 4-tuple anchor points list.
    --> sc: The list of maximum scores corresponding with each anchor point.
    --> Nt: The IoU threshold, if two boxes overlap more than this value, they
        shall be deleted, unless they have a very high score.
    --> sigma: The soft supression is done with a gaussian decay function, the higher the
        variance, the lower the effects of supression, the lower the variance, the higher
        the chance of deleting more windows (in the case of sigma=0, we reach a Hard nms for
        example ...),
    --> thresh: The minmum softmax score to even consider using the box, it is no longer
        needed here, since we will remove all low score windows before this, this is a relic
        of the debugging age :).
    --> method: This code implements 2 differnt soft nms and a normal hard nms function:
        --> method = 1: A linear soft nms.
        --> method = 2: A Gaussian soft nms (which we shall use).
        --> method = 3: Normal hard nms (which we will also use).

    Returns:
    ==> keep: A list of indexes that passed the nms test.
    """

    N = dets.shape[0]
    indexes = np.array([np.arange(N)])
    dets = np.concatenate((dets, indexes.T), axis=1)

    y1 = dets[:, 0]
    x1 = dets[:, 1]
    y2 = dets[:, 2]
    x2 = dets[:, 3]
    scores = sc
    areas = (x2 - x1 + 1) * (y2 - y1 + 1)

    for i in tqdm.tqdm(range(N)):
        tBD = dets[i, :].copy()
        tscore = scores[i].copy()
        tarea = areas[i].copy()
        pos = i + 1

        #
        if i != N-1:
            maxscore = np.max(scores[pos:], axis=0)
            maxpos = np.argmax(scores[pos:], axis=0)
        else:
            maxscore = scores[-1]
            maxpos = 0
        if tscore < maxscore:
            dets[i, :] = dets[maxpos + i + 1, :]
            dets[maxpos + i + 1, :] = tBD
            tBD = dets[i, :]

            scores[i] = scores[maxpos + i + 1]
            scores[maxpos + i + 1] = tscore
            tscore = scores[i]

            areas[i] = areas[maxpos + i + 1]
            areas[maxpos + i + 1] = tarea
            tarea = areas[i]

        xx1 = np.maximum(dets[i, 1], dets[pos:, 1])
        yy1 = np.maximum(dets[i, 0], dets[pos:, 0])
        xx2 = np.minimum(dets[i, 3], dets[pos:, 3])
        yy2 = np.minimum(dets[i, 2], dets[pos:, 2])

        w = np.maximum(0.0, xx2 - xx1 + 1)
        h = np.maximum(0.0, yy2 - yy1 + 1)
        inter = w * h
        ovr = inter / (areas[i] + areas[pos:] - inter)

        # Three methods: 1.linear 2.gaussian 3.original NMS
        if method == 1:  # linear
            weight = np.ones(ovr.shape)
            weight[ovr > Nt] = weight[ovr > Nt] - ovr[ovr > Nt]
        elif method == 2:  # gaussian
            weight = np.exp(-(ovr * ovr) / sigma)
        else:  # original NMS
            weight = np.ones(ovr.shape)
            weight[ovr > Nt] = 0

        scores[pos:] = weight * scores[pos:]

    # select the boxes and keep the corresponding indexes
    inds = dets[:, 4][scores > thresh]
    keep = inds.astype(int)

    return keep

def face_plotter(image, anchors, nums, th, iou_th):
    """
    Plot faces given a list of anchors and scores.
    This is done by:
        1) Filter low scores.
        2) Perform Hard NMS on resulting windows and scores.
        3) Plot the chosen windows on the image.

    Inputs:
    --> image: An input image.
    --> anchors: A list of 4 tuple anchor points.
    --> nums: Scores corresponding to the windows in 'anchors'.
    --> th: Score threshold to even consider using a winow.
    --> iou_th: Intersection over Union threshold, a value higher than
        this threshold will cause the overlaping window with lower score
        to be removed.

    Returns:
    ==> just plots the final result.
    """

    anchors = np.asarray(anchors)
    nums = np.asarray(nums)
    indices = check_scores(nums, th)
    chosen_windows = anchors[indices]
    chosen_scores = nums[indices]
    nms_indices = soft_nms(chosen_windows, chosen_scores, Nt=iou_th, method=3)
    nms_win = chosen_windows[nms_indices]
    plot_boxes(image, nms_win)