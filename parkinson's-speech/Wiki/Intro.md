# Introduction And a Description of The Papers

This project is based on the paper referenced in the README file, this paper aims to find parameters that are:
- Easy to extract
- Robust against noise or computational errors
- Contain effective information about the health conditions of the patient

There are many candidates, the paper describes many in it's text. In the end we will use these parameters as features and train our classifier on these parameters. The best parameter is the one that can split the distributions of PD and healthy patients the most.

## Where to start
---

The paper starts by fitting a model to the Glottal pulse.
This pulse is the main source of information in our tests. While the paper goes on to perform curve-fitting using the LF model on the pulse, we preferred not to do that. It is a hard process and requires a lot of fine tuning. However we will demonstrate that even with this lack of accuracy, there is still a lot that can be done.

The paper does not specifically mention what classifier was used in the end; we just used a simple binary logistic regressor which is fast and still provides accurate results.

In the end we aim to find out:
- Which parameter has the best performance
- How accurate that parameter is

## Basic Steps
---

The first step is extracting the Glottal pulse. This pulse holdes all the information that we need and precise exraction is necessary.

The writers have used the IAIF algorithm in this problem which is fairly common and gives accurate results. 

Implementations exists that can help one extract the pulse from a whole dataset, like [Praat](https://www.fon.hum.uva.nl/praat/); we, however, preferred to implement it ourselves. This was mainly out of curiosity and we do recommend using Praat if you are using this code.

After the Glottal pulse is extracted, parameters that are suggested by the paper are calculated. Each parameter will induce a distribution on the population of the patients. We will then plot these distribution and try to find the one that shows the most disparity between distributions.

Once this lucky feature is found, a classifier is trained on this specific parameter and then the final accuracy is reported.

## Evaluations
---

In order to compare our results to the paper results, an ROC curve and an accuracy evaluation will be done in the end.

Our implementation is much simpler than the one in the paper, so we do expect a general decrease in the accuracy of each parameter; but as we shall see, one of the parameters changes only slightly, and still provides pretty good accuracy; so the sacrifice can be rationalized.
