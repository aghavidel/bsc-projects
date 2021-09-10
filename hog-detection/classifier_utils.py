from sklearn.svm import SVC
from sklearn.metrics import plot_roc_curve, plot_precision_recall_curve
import numpy as np
import itertools
import tqdm
import time

"""
This file contains utilities for testing and creating SVM classifiers.
"""

def score_svm_classifier(train_data, train_labels, val_data, val_labels, **kwargs):
    """
    Create, train and test a SVM classifier with given parameters
    on a given train dataset.

    The train data are used for fitting the model and the validation
    dataset is used to estimate accuracy.

    Inputs:
    --> train_data, train_labels: Train data and labels.
    --> val_data, val_labels: Validation data and labels.
    --> kwargs: Arguments to pass to sklearn.svm.SVC()

    Returns:
    ==> The final classifier, along with the accuracy on validation
        dataset.
    """

    classifier = SVC(**kwargs)
    classifier.fit(train_data, train_labels)
    print(f"Final score: {classifier.score(val_data, val_labels)}")
    
    return classifier

def evaluate_params(
    train_data, train_labels, val_data, val_labels, poly_degree, **kwargs
):
    """
    This function evaluates differnt SVMs (i.e. SVM with different parameters)
    on a dataset and reports the accuracy, plus returning the classifiers.

    Inputs:
    --> train_data, train_label: The train data and labels.
    --> val_data, val_label: The validation data and labels.
    --> poly_degree: The degrees of the polynomial kernel (if not needed, pass '[]').
    --> kwargs: kwargs to pass to sklearn.svm.SVC, a dict object, where keys are attributes
        of SVC and values are lists of possible choices for that attribute.

    Returns:
    ==> Prints the accuracy of each classifier on validation data.
    ==> svm_objects: A list of tuples, the first elements are the attributes of the
        classifier and the second is the classifier object itself.
    """

    param_names = []
    param_lists = []
    if len(poly_degree) > 0:
        other_names = ['kernel', 'degree']
        other_param_lists = [['poly'], poly_degree]
    
    for key, value in kwargs.items():
        param_names.append(key)
        param_lists.append(value)
        if len(poly_degree) > 0:
            if key != 'kernel':
                other_names.append(key)
                other_param_lists.append(value)
        
    combinations = list(itertools.product(*param_lists))
    if len(poly_degree) > 0:
        other_combinations = list(itertools.product(*other_param_lists))
    
    new_kwargs = []
    for combination in combinations:
        new_dict = dict()
        for param_name, element in zip(param_names, combination):
            new_dict[param_name] = element
        new_kwargs.append(new_dict)
        
    if len(poly_degree) > 0:
        other_new_kwargs = []
        for other_combination in other_combinations:
            new_dict = dict()
            for param_name, element in zip(other_names, other_combination):
                new_dict[param_name] = element
            other_new_kwargs.append(new_dict)
    
    if len(poly_degree) > 0:
        final_kwargs = new_kwargs + other_new_kwargs
    else:
        final_kwargs = new_kwargs
        
    svm_objects = []
    
    for svm_kwargs in tqdm.tqdm(final_kwargs):
        classifier = score_svm_classifier(
            train_data, train_labels, val_data, val_labels, **svm_kwargs
        )
        svm_objects.append((svm_kwargs, classifier))
        time.sleep(1)
        
    return svm_objects

def platt_scale(l):
    """
    Return a given array of numbers in Platt scale.

    Inputs:
    --> l: An array of numbers.

    Returns:
    ==> Array 'l' scaled to the Platt scale.
    """

    return 1 / (1 + np.exp(-l))

def get_roc(model, X, Y):
    """
    Get the ROC curve of a model on the datas X and Y.

    Inputs:
    --> model: A 'sklearn' classifier model.
    --> X, Y: The data to evaluate the model on it.
    Returns:
    ==> only plots the ROC curve.
    """

    plot_roc_curve(model, X, Y)

def get_percision_recall(model, X, Y):
    """
    Get the precision-recall curve of a model on the datas X and Y.

    Inputs:
    --> model: A 'sklearn' classifier model.
    --> X, Y: The data to evaluate the model on it.
    Returns:
    ==> only plots the precision-recall curve.
    """

    plot_precision_recall_curve(model, X, Y)