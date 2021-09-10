# Implementation Details and Results

For the smallest details, just refer to the docstrings in the code, here we only report the most prominent design decisions.

## The Datasets
---

In order to extract the Glottal pulse, voiced speech samples are absolutely necessary.

What we need are long, monotone recordings of \a\ or \i\, there are many such datasets available, the links of the datasets that we used are in the README file.

They are also pretty big, so keep that in mind if you want to use them.

## Extracting The Glottal Pulse
---

The IAIF algorithm is implemented with orders 5 and 20.

Each sample is framed into 32 ms chunks, with 16 ms hops. Each frame is windowed with a Hamming window to make the singal more stationary.

Each time, 5 frames are consumed by the algorithm; in the end an estimate of the Glottal pulse flow derivative is extracted and then it needs to be integrated.

For intergration, a simple filter with pole near 1 is used. After this integration a DC component will still be present and needs to be extracted with a lowpass filter (like a moving average) and then removed from the signal.

Here is an example:

<p align=center>
<img src=../plots/glottal_shape.png width=400>
</p>

Each frame may carry more than one pulse (3 in the picture above), the algorithm will compute the mean of each pulse in the frame and use it as the feature estimation in that frame.

## Classification
---

Classification is done with a simple binary logistic regressor. We used MATLAB's own implementations for this. 
Our main objective is to find the best parameter, thus we are deliberately using a very simple classifier to test the merits of each parameter.

If you use more sophisticated classifiers, it is very possible that you may get MUCH better results, but this was not our main objective, nor was it the case in the paper.

For evaluation, each parameter distribution is plotted. The best parameter is the one that can cause the most disparit between the PD and healthy distributions.

An ROC curve can then be easily created from the results.

## Results
---

Our tests concluded with the following:

| Prameter Name | Parameter Accuracy (\%) |
|---------------|-------------------------|
| QOQ | 94 |
| NAQ | 75 |
| $t_e$ | 70 |
| $R_k$ | 69 |
| $t_p$ | 65 |
| $E_e$ | 53 |

This can be seen easily in their distributions also (Red: PD, Blue: Healthy):

<p align=center>
<img src=../plots/parameters_distribution.png width=450>
</p>

<br>
<br>

Intuitively, QOQ has the best accuracy because it was the most successful in decoupling the two distributions, while  $E_e$ is the worst because the distributions are basically the same!

Compared to the paper, most results are the same but NAQ has suffered the most damage due to our more simplistic approach. QOQ however, retained it's very high accuracy.

The ROC curves also show this, here is the result for all the parameters:

<p align=center>
<img src=../plots/final.png width=500>
</p>

And for QOQ alone:

<p align=center>
<img src=../plots/ROC_QOQ.png width=500>
</p>