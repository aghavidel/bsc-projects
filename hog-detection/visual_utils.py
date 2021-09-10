import cv2 as cv
import matplotlib.pyplot as plt
import numpy as np

def get_frame(frame_num, low=True):
    """
    This function return the frame number 'frame_num' from the given 
    videos. (or the decimated video that i created ...)
    
    Inputs:
    --> frame_num: Number of the frame to return as an image.
    
    --> low: Whether to use the decimated video (1/4 of the resolution 
        of the given video and named 'video-low.m4v'), defaults to True.
        
    Returns:
    ==> The image, associated with the given frame number.
    """
    
    if not low:
        vid = cv.VideoCapture('video.mp4')
    else:
        vid = cv.VideoCapture('video-low.m4v')
        
    sucess, image = vid.read()
    counter = 0
    
    while sucess:
        if counter == frame_num:
            return image
        sucess, image = vid.read()
        counter += 1

def plot_cv_with_plt(image, title=None):
    """
    Helper function, plots OpenCV colored image with matplotlib.
    
    Inputs:
    --> image: OpenCV colored image array
    
    Returns:
    ==> Nothing, just plots the image in the right color format.
    """
    
    plt.imshow(cv.cvtColor(image, cv.COLOR_BGR2RGB))
    
    if title:
        plt.title(title)

def get_kp_from_matches(matches, kp1, kp2, des1=None, des2=None):
    """
    Given a list of OpenCV Match objects, it will extract the corresponding 
    keypoint elements and descriptors from the main lists.
    
    Inputs:
    --> matches: A list of cv.Match objects extracted with SIFT.
    
    --> kp1, kp2: A list of all extracted keypoints from both images.
    
    --> des1, des2: Descriptors of the previous keypoints, it can be None and
        the function will just ignor them and only return the krypoints (Opeional).
        
    Returns:
    ==> kp1_small, kp2_small: The refined list of keypoints.
    
    ==> des1_small, des2_small: The refined list of desctiptors, will be returned
        only if the des1 and des2 arguments were provided.
    """
    
    kp1_small = []
    kp2_small = []
    des1_small = []
    des2_small = []
    
    if des1 is None:
        if np.ndim(matches) == 1:
            for match in matches:
                kp1_small.append(kp1[match.queryIdx])
                kp2_small.append(kp1[match.trainIdx])
        else:
            for match in matches:
                kp1_small.append(kp1[match[0].queryIdx])
                kp2_small.append(kp2[match[0].trainIdx])
        
        return kp1_small, kp2_small
    
    else:
        if np.ndim(matches) == 1:
            for match in matches:
                kp1_small.append(kp1[match.queryIdx])
                kp2_small.append(kp1[match.trainIdx])
        else:
            for match in matches:
                kp1_small.append(kp1[match[0].queryIdx])
                kp2_small.append(kp2[match[0].trainIdx])
                des1_small.append(des1[match[0].queryIdx])
                des2_small.append(des2[match[0].trainIdx])
        
        return kp1_small, kp2_small, des1_small, des2_small

def Lowe_ratio_test(matches, threshold, num_match=None):
    """
    This function performs Lowe's ratio test, by checking each
    matching pair and throwing away the ambiguous pairs by checking
    whether or not the distance to the 1st and 2nd closest points are
    different enough.
                                            Distance to the closest point
        pair is unambiguous iff: r = _________________________________________ < threshold
                        
                                        Distance to the second closest point
                                        
    Inputs:
    --> matches: A list of matching pairs proposed by a method like SIFT.
    
    --> threshold: The threshold of rejecting the ratio test if r exceeds that
        value.
        
    --> num_match: The number of matched pairs to be printed later, here it is used
        to set the mask in a proper way so that only num_match pair of points will
        be connected when we print them later with cv.drawMatchesKnn().
        
    Returns:
    ==> good: A new list containing the chosen pairs.
    
    ==> matchesMask: A mask for printing the matched pairs.
    """
    
    matchesMask = [[0, 0] for i in range(len(matches))]
    
    good = []
    counter = 0
    
    for i, (m, n) in enumerate(matches):
        if m.distance < threshold * n.distance:
            good.append([m])
            
            if num_match:
                if counter < num_match:
                    matchesMask[i] = [1, 0]
                    counter += 1
            else:
                matchesMask[i] = [1, 0]
            
    return good, matchesMask

def do_sift(image1, image2, plot=True):
    """
    This function performs SIFT on two given images and extracts the interest
    points and their descriptors, it will also draw the key points and show them
    at the same time.
    
    Inputs:
    --> image1, image2: Two OpenCV image arrays, they are assumed to be colored and
        are converted to grayscale before passing them to SIFT.
        
    Returns:
    ==> kp1, kp2: Keypoints (interest points) in image 1 and image 2.
    
    ==> des1, des2: Descriptors of the pixels around kp1 and kp2, contains
        the 128 element vector of gradient histogram.
    """
    image1_gray = cv.cvtColor(image1, cv.COLOR_BGR2GRAY)
    image2_gray = cv.cvtColor(image2, cv.COLOR_BGR2GRAY)
    
    sift = cv.SIFT_create()
    
    kp1 = sift.detect(image1_gray, None)
    kp2 = sift.detect(image2_gray, None)
    
    if plot:
        new_img1 = cv.drawKeypoints(image1_gray, kp1, None, color=(0, 255, 0))
        new_img2 = cv.drawKeypoints(image2_gray, kp2, None, color=(0, 255, 0))
        
        plt.figure(figsize=(15, 15))
        frame = plot_in_single_frame(new_img1, new_img2)
        plot_cv_with_plt(frame)

    kp1, des1 = sift.compute(image1_gray, kp1)
    kp2, des2 = sift.compute(image2_gray, kp2)
    
    return kp1, kp2, des1, des2
    

def feature_matching(image1, image2, threshold=0.7, plot_kps=False, 
                     plot_matches=False, num_match=None, **draw_params):
    """
    This functions extracts features from two given images and then matches them
    using a brute force matcher, i.e. it will compare every single possible pair
    and choose the one with minimum distance.
    
    For better results, a ratio test is applied to the output of the BF matcher and
    in the end, The matches are drawn on the image.
    
    Inputs:
    --> image1, image2: The two openCV image arrays.
    
    --> threhold: The threshold value to reject the ratio test, defaults to 0.7 (Optional).
    
    --> plot_kps: Plots the keypoints extracted from the ratio test, defaults to False 
        (Optional).
        
    --> plot_matches: Draws the matched points, defaults to False (Optional).
    
    --> num_match: Number of matched points to draw in the end, defaults to None by using
        all available points (Optional).
    
    --> draw_params: Optional kwargs passed to cv.drawMatchesKnn() (Optional).
    
    Returns:
    ==> good: The list of accepted match pairs.
    
    ==> kp1, kp2, des1, des2: Refer to do_sift().
    """
    
    kp1, kp2, des1, des2 = do_sift(image1, image2, plot=False)
    
    new_img1 = cv.drawKeypoints(image1, kp1, None, color=(0, 255, 0))
    new_img2 = cv.drawKeypoints(image2, kp2, None, color=(0, 255, 0))
    
    bf = cv.BFMatcher()
    
    matches = bf.knnMatch(des1, des2, k=2)
    good, matchesMask = Lowe_ratio_test(matches, threshold, num_match)
    
    good = sorted(good, key=lambda m: m[0].distance)
    
    kp1_small, kp2_small, des1_small, des2_small = get_kp_from_matches(good, kp1, kp2, des1, des2)
    
    if plot_kps:
        newkps_1 = cv.drawKeypoints(new_img1, kp1_small, None, color=(255, 0, 0))
        newkps_2 = cv.drawKeypoints(new_img2, kp2_small, None, color=(255, 0, 0))
        
        frame = plot_in_single_frame(newkps_1, newkps_2)
        plt.figure(figsize=(20, 20))
        plot_cv_with_plt(frame)
        
    if plot_matches:
        matched_frame = cv.drawMatchesKnn(image1, kp1, image2, kp2, matches, None,
                                           matchesMask=matchesMask, **draw_params)
            
        plt.figure(figsize=(20, 20))
        plot_cv_with_plt(matched_frame)
    
    return good, kp1, kp2, des1, des2

def plot_in_single_frame(image1, image2):
    """
    Helper function, plots two OpenCV images in a single frame.
    
    Inputs:
    --> image1, image2: OpenCV image arrays.
    
    Returns:
    ==> frame: A numpy array which contains the two images at the same time.
    """
    
    shape1 = np.shape(image1)
    shape2 = np.shape(image2)
    h1, w1 = shape1[0], shape1[1]
    h2, w2 = shape2[0], shape2[1]
    
    h = np.max([h1, h2])
    w = w1 + w2
    
    frame = np.zeros(shape=(h, w, 3), dtype=np.uint8)
    frame[:h1, :w1, :] = image1[:, :, :]
    frame[:h2, w1:, :] = image2[:, :, :]
    
    plot_cv_with_plt(frame)

def homography_with_ransac(image1, image2, matches, kp1, kp2, des1, des2, ransac_iter):
    """
    This function will create the homography by using RANSAC.
    
    Inputs:
    --> image1, image2: The two opencv image arrays.
    
    --> matches: A list of opencv Match objects.
    
    --> kp1, kp2: The kypoints from each image.
    
    --> des1, des2: Descriptors correponing to kp1 and kp2 respectively.
    
    --> ransac_iter: Number of iterations to run RANSAC, we use 1000 most of
        the time.
        
    Returns:
    ==> H: The calculated homography matrix.
    
    ==> matchesMask: A mask that determines the inliers used for the homography
        calculations.
    """
    
    assert len(matches) >= 4, "Not enought points for computing homography."

    src_pts = np.float32([kp1[m[0].queryIdx].pt for m in matches]).reshape(-1,1,2)
    dst_pts = np.float32([kp2[m[0].trainIdx].pt for m in matches]).reshape(-1,1,2)
    
    H, mask = cv.findHomography(src_pts, dst_pts, cv.RANSAC, maxIters=ransac_iter)
    matchesMask = mask.ravel().tolist()
    
    kp1_small, kp2_small = get_kp_from_matches(matches, kp1, kp2)
        
    draw_params = {
        "matchColor": (0, 255, 0),
        "matchesMask": matchesMask, 
        "flags": cv.DRAW_MATCHES_FLAGS_NOT_DRAW_SINGLE_POINTS
    }
    
    newkps_1 = cv.drawKeypoints(image1, kp1_small, None, color=(255, 0, 0))
    newkps_2 = cv.drawKeypoints(image2, kp2_small, None, color=(255, 0, 0))
    
    return H, matchesMask

def write_frames_to_video(frames, writer):
    """
    Write a list of frames into a video at once.
    
    Inputs:
    --> frames: List of frame images.
    
    --> writer: An OpenCV VideoWriter object.
    
    Returns:
    Nothing, just write the frames to the video.
    """
    
    for frame in frames:
        writer.write(frame)

"""
These two inline functions will send or return a point to or from
the projective space, i.e. if we have a point (x, y), we send it
to the projective space by appending a 1 to it, i.e. (x, y, 1).
If we want to return it, we take a point (x, y, z) and return
(x/z, y/z).
"""
goto_projective = lambda inp: [inp[0], inp[1], 1]
from_projective = lambda inp: [inp[0]/inp[2], inp[1]/inp[2]]

def do_homography(points, H):
    """
    Given a list of points, applies the homography matrix on 
    them and returns them.
    
    Inputs:
    --> points: List of of points in [x, y] format.
    
    --> H: A valid homography matrix.
    
    Returns:
    ==> h_points: List of the given points, when the homography
        applies to them.
    """
    
    h_points = []
    
    for point in points:
        h_point = np.matmul(H, goto_projective(point))
        h_points.append(np.floor(from_projective(h_point)))
    
    return h_points

def test_homography(polygon, image1, image2, H):
    """
    Given a list of corner locations for a polygon, this function will
    first write it to a given picture, and then write it's projection with
    a given homography matrix to another picture.
    
    Inputs:
    --> polygon: List of corner location in [x, y] format for an arbitrary
        polygon, here we aim for rectangles, and thus need a list of 4 points.
    
    --> image1, image2: Two arbitrary images.
    
    --> H: The homography between the above images.
    
    Returns:
    Nothing, only plots the results in a single frame.
    """
    
    pts = np.array(polygon, np.int32)
    pts = pts.reshape((-1,1,2))
    
    image1_poly = cv.polylines(np.copy(image1), [pts], True, (0, 255, 255), 10)
    poly_warped = do_homography(polygon, H)
    
    pts = np.array(poly_warped, np.int32)
    pts = pts.reshape((-1,1,2))
    
    image2_poly = cv.polylines(np.copy(image2), [pts], True, (0, 255, 255), 10)
    
    plt.figure(figsize=(20, 10))
    plot_in_single_frame(image1_poly, image2_poly)

def draw_point_list_on_image(image, pts, color, r, pad=False):
    """
    Utility that draws a list of points on an image, padding is applied
    if needed.

    Inputs:
    --> image: The input image.

    --> pts: The list of points, assumed to be a numpy array, each entry
        of form [x, y].

    --> color: The color of the drawn points, a tuple.

    --> r: The circles of the points drawn.

    --> pad: Whether or not padding is needed, defaults to False.

    Returns:
    ==> im_out: The final image.
    """

    y_len, x_len, _ = np.shape(image)
    
    xs = pts[:, 0]
    ys = pts[:, 1]
    
    mx = np.min(xs)
    my = np.min(ys)
    Mx = np.max(xs)
    My = np.max(ys)

    start_x = np.min([mx, 0])
    start_y = np.min([my, 0])
    finish_x = np.max([x_len, Mx])
    finish_y = np.max([y_len, My])

    X_len = finish_x - start_x
    Y_len = finish_y - start_y
    
    k = np.ceil(X_len/10000)
    
    image = cv.resize(image, (0, 0), fx=1/k, fy=1/k, interpolation=cv.INTER_AREA)
    y_len, x_len, _ = np.shape(image)
    
    start_x = int(start_x/k)
    start_y = int(start_y/k)
    
    im_out = np.zeros((int(Y_len/k), int(X_len/k), 3), dtype=np.uint8)
    im_out[-start_y:-start_y+y_len, -start_x:-start_x+x_len, :] = image
    
    if pad:
        pad_len = 5*r
        im_out = cv.copyMakeBorder(
            im_out, pad_len, pad_len, pad_len, pad_len, cv.BORDER_CONSTANT, None, (0, 0, 0)
        )
    else:
        pad_len = 0
    
    for x, y in pts:
        im_out = cv.circle(
            im_out, (int(x/k) - start_x + pad_len, int(y/k) - start_y + pad_len), r, color, cv.FILLED
        )
    
    return im_out    

def draw_line_list_on_image(
    image, endpoints1, endpoints2, color_line, color, t, r, pad=False, points=None
):
    """
    Utility that can draw lines and points on an image and pad it if needed.

    Inputs:
    --> image: The input image.

    --> endpoints1, ednpoints2: Line endpoints, numpy arrays each, with entries of
        form [x, y].

    --> color_line: The color of the drawn lines.

    --> color: The color of the circles drawn.

    --> t: The thickness of the line.

    --> pad: Whether or not padding is needed, defaults to False.

    --> points: A list of points, numpy array, each element of form [x, y], defaults
        to None.

    Returns:
    ==> im_out: The final image.
    """

    y_len, x_len, _ = np.shape(image)
    
    xs = np.hstack((endpoints1[:, 0], endpoints2[:, 0]))
    ys = np.hstack((endpoints1[:, 1], endpoints2[:, 1]))

    if points is not None:
        xs = np.hstack((xs, points[:, 0]))
        ys = np.hstack((ys, points[:, 1]))
    
    mx = np.min(xs)
    my = np.min(ys)
    Mx = np.max(xs)
    My = np.max(ys)

    start_x = np.min([mx, 0])
    start_y = np.min([my, 0])
    finish_x = np.max([x_len, Mx])
    finish_y = np.max([y_len, My])

    X_len = finish_x - start_x
    Y_len = finish_y - start_y
    
    k = np.ceil(X_len/10000)
    
    image = cv.resize(image, (0, 0), fx=1/k, fy=1/k, interpolation=cv.INTER_AREA)
    y_len, x_len, _ = np.shape(image)
    
    start_x = int(start_x/k)
    start_y = int(start_y/k)
    
    im_out = np.zeros((int(Y_len/k), int(X_len/k), 3), dtype=np.uint8)
    
    try:
        im_out[-start_y:-start_y+y_len, -start_x:-start_x+x_len, :] = image
    except:
        try:
            im_out[-start_y:-start_y+y_len+1, -start_x:-start_x+x_len, :] = image
        except:
            try:
                im_out[-start_y:-start_y+y_len, -start_x:-start_x+x_len+1, :] = image
            except:
                raise ValueError('Failed to resize image.')
    
    if pad:
        pad_len = 5*r
        im_out = cv.copyMakeBorder(
            im_out, pad_len, pad_len, pad_len, pad_len, cv.BORDER_CONSTANT, None, (0, 0, 0)
        )
    else:
        pad_len = 0
    
    for (x1, y1), (x2, y2) in zip(endpoints1, endpoints2):
        im_out = cv.line(
            im_out, 
            (int(x1/k) - start_x + pad_len, int(y1/k) - start_y + pad_len),
            (int(x2/k) - start_x + pad_len, int(y2/k) - start_y + pad_len),
            color_line, t
        )
        
    if points is not None:
        for x, y in points:
            im_out = cv.circle(
                im_out, 
                (int(x/k) - start_x + pad_len, int(y/k) - start_y + pad_len), 
                r, color, cv.FILLED
            )
    
    return im_out

def draw_points_list_gray(image, points, draw_image=False):
    """
    Utility function same as draw points list on image, instead it
    uses grayscale.

    Inputs:
    --> image: Input image.

    --> points: Numpy array of points, each element of the form [x, y].

    --> draw_image: Whether or not to draw the image, defaults to False.

    Returns:
    ==> output: Scaled output image with the points drawn.
    """

    h, w, _ = np.shape(image)
    
    points_x = points[:, 0]
    points_y = points[:, 1]
    
    mx = np.min(points_x)
    my = np.min(points_y)
    Mx = np.max(points_x)
    My = np.max(points_y)
    
    start_x = np.min([mx, 0])
    finish_x = np.max([Mx, w])
    start_y = np.min([my, 0])
    finish_y = np.max([My, h])
    
    H = finish_y - start_y
    W = finish_x - start_x
    
    kx = np.ceil(W/10000)
    ky = np.ceil(H/5000)
    
    start_x /= kx
    start_y /= ky
    finish_x /= kx
    finish_y /= ky
    
    start_x = int(start_x)
    start_y = int(start_y)
    finish_x = int(finish_x)
    finish_y = int(finish_y)
    print((H, W))
    if kx > 1 and ky > 1:
        output = np.zeros((5000, 10000), dtype=np.uint8)
    elif kx > 1:
        output = np.zeros((H, 10000), dtype=np.uint8)
    elif ky > 1:
        output = np.zeros((5000, W), dtype=np.uint8)
    else:
        output = np.zeros((H, W), dtype=np.uint8)
    
    for x, y in points:
        x0 = int(x/kx) - start_x
        y0 = int(y/ky) - start_y
        output = cv.circle(output, (x0, y0), 20, 255, -1)
        
    if draw_image:
        l1 = int(h/ky)
        l2 = int(w/kx)
        output[-start_y:-start_y + l1, 
               -start_x:-start_x + l2] = np.ones((l1, l2), dtype=np.uint8) * 255
    
    return output

def cross_product(a, b):
    """
    Computes the cross product of two vectors of length 3.

    Inputs:
    --> a, b: The vectors.

    Returns:
    --> a x b.
    """

    assert len(a) == len(b) == 3, 'Vectors should be of length 3'
    
    return np.array([
        a[1]*b[2] - a[2]*b[1],
        a[2]*b[0] - a[0]*b[2],
        a[0]*b[1] - a[1]*b[0]
    ])