import numpy as np
import cv2 as cv
import os
import tqdm

"""
This file contains utilities for creating the datasets.
"""

# Function that checks whether a list of given file names are all .jpg or not.
is_jpg = lambda file_list: np.sum(['.jpg' in file_name for file_name in file_list]) == len(file_list)

def get_image_hog(image):
    """
    Get the HOG descriptor of a given image, the parameters were fine 
    tuned to following values.

    - winSize: (128, 128)
    - blockSize: (16, 16)
    - cellSize: (8, 8)
    - nbins: 9

    Inputs:
    --> image: An RGB image.
    
    Returns:
    ==> Image HOG descriptor.
    """

    winSize = (128, 128)
    blockSize = (16, 16)
    blockStride = (8, 8)
    cellSize = (8, 8)
    nbins = 9
    
    hog = cv.HOGDescriptor(
        winSize,
        blockSize,
        blockStride,
        cellSize,
        nbins
    )
        
    return hog.compute(image)

def load_images_from_paths_and_do(paths, func, resize, crop=None):
    """
    Loads the image corresponding to a given list of paths and performs
    a given function on them (like the 'get_image_hog' function).
    If needed, the function can also resize and crop an image which 
    we need for the negaive images.

    Inputs:
    --> paths: A list of path strings.
    --> func: A function that shall be performed on each image.
    --> resize: A tuple, used to resize each image.
    --> crop: Whether or not to crop the image to the HOG window size (
        which is hard-coded to be 128).

    Returns:
    ==> im_list: List of the reulst of func on each image.
    """

    im_list = []
    
    if crop is None:
        for path in tqdm.tqdm(paths):
            image = cv.imread(path)
            image = cv.resize(image, resize, interpolation=cv.INTER_AREA)
            im_list.append(func(image))
    else:   
        for path in tqdm.tqdm(paths):
            image = cv.imread(path)
            image = cv.resize(image, resize, interpolation=cv.INTER_AREA)
            h, w, _ = np.shape(image)
            
            anchor_x = np.random.randint(0, h-crop[0])
            anchor_y = np.random.randint(0, w-crop[1])
            image = image[
                anchor_x:anchor_x+crop[0], 
                anchor_y:anchor_y+crop[1],
                :
            ]
                
            im_list.append(func(image))
            
    return np.array(im_list)

def prepare_positive(func, path_to_data, num_of_train, num_of_test=None, num_of_val=None):
    """
    Prepare the positive dataset, by resizing each image to the HOG window size
    and extracting the HOG features.

    Inputs:
    --> func: The function to perform on each image.
    --> path_to_data: The path to the intended dataset.
    --> num_of_train, num_of_test, num_of_val: Number of needed train, test and validation
        samples.

    Returns:
    ==> train_image, test_image, val_image: Train, test and validation image arrays.
    """

    image_list = []
    path_list = []
    
    for root, dirs, files in os.walk(path_to_data):
        if len(dirs) > 0:
            continue
        else:
            if is_jpg(files):
                image_list.extend([os.path.join(root, file_name) for file_name in files])
    
    image_list = np.array(image_list)
    
    N = num_of_train
    if num_of_test is not None:
        N += num_of_test
    if num_of_val is not None:
        N += num_of_val
    
    positive_image_indices = np.random.choice(
        np.arange(len(image_list)), 
        N,
        False
    )
    
    train_paths = image_list[positive_image_indices[
        0:num_of_train
    ]]
    
    if num_of_test is not None:
        test_paths = image_list[positive_image_indices[
            num_of_train+num_of_val:num_of_train+num_of_val+num_of_test
        ]]
    
    if num_of_val is not None:
        val_paths = image_list[positive_image_indices[
            num_of_train:num_of_train+num_of_val
        ]]
    
    train_images = load_images_from_paths_and_do(train_paths, func, (128, 128))
    
    if num_of_test is None:
        return train_images
    
    test_image = load_images_from_paths_and_do(test_paths, func, (128, 128))
    if num_of_val is None:
        return train_images, test_image
    
    val_images = load_images_from_paths_and_do(val_paths, func, (128, 128))
    
    return train_images, test_image, val_images

def prepare_negative(func, path_to_data, num_of_train, num_of_test=None, num_of_val=None):
    """
    Prepare the positive dataset, by resizing each image to (256, 256), then
    cropping it to the HOG window size and extracting the HOG features.

    Inputs:
    --> func: The function to perform on each image.
    --> path_to_data: The path to the intended dataset.
    --> num_of_train, num_of_test, num_of_val: Number of needed train, test and validation
        samples.

    Returns:
    ==> train_image, test_image, val_image: Train, test and validation image arrays.
    """

    image_list = []
    path_list = []
    
    for root, dirs, files in os.walk(path_to_data):
        if len(dirs) > 0:
            continue
        else:
            if is_jpg(files):
                image_list.extend([os.path.join(root, file_name) for file_name in files])
    
    image_list = np.array(image_list)
    
    N = num_of_train
    if num_of_test is not None:
        N += num_of_test
    if num_of_val is not None:
        N += num_of_val
    
    positive_image_indices = np.random.choice(
        np.arange(len(image_list)), 
        N,
        False
    )
    
    train_paths = image_list[positive_image_indices[
        0:num_of_train
    ]]
    
    if num_of_test is not None:
        test_paths = image_list[positive_image_indices[
            num_of_train+num_of_val:num_of_train+num_of_val+num_of_test
        ]]
    
    if num_of_val is not None:
        val_paths = image_list[positive_image_indices[
            num_of_train:num_of_train+num_of_val
        ]]
    
    train_images = load_images_from_paths_and_do(train_paths, func, (256, 256), (128, 128))
    
    if num_of_test is None:
        return train_images
    
    test_image = load_images_from_paths_and_do(test_paths, func, (256, 256), (128, 128))
    if num_of_val is None:
        return train_images, test_image
    
    val_images = load_images_from_paths_and_do(val_paths, func, (256, 256), (128, 128))
    
    return train_images, test_image, val_images