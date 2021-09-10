# Naive Bayesian Classifier
We are trying to implement a simple Bayesian classifier from scratch. Then we are going to use it on a dataset to predict a few things about some of the samples.

First a little primer on the classifier itsels.

## The problem
----

We are given a dataset, this dataset holds N different columns of data which come from distributions $X_1, X_2, ..., X_n$ and each sample is of the form $x_1, x_2, ..., x_n$.

We are provided K labels $C_1, ..., C_K$ and asked to classify each sample. So we need to construct an entity that by some logic maps the features $x$ to a class, let's call it $y(x)$.

The "Naive Bayesian Classifier" implements this mapping as:

$$
y(x) = \argmax_{k=1:K}{P(C_k | x)}
$$

you can probably see where the name comes from now, the classifier relies completely on *prior knowledge* about the labeling logic on this dataset, thus it is really easy to fool it with some arbitrary random variable. But we do hope at least to find some logic in our datasets from time to time don't we?

Putting aside the aforementioned problem, how do you move on from this definition? Well it's in the name of the classifier isn't it?

$$
y(x) = \argmax_{k=1:K}{P(C_k | x)} = \argmax_{k=1:K}{\frac{P(C_k) P(x | C_k)}{P(x)}}
$$

Now the term $P(x)$ can be safely ignored, this is because it has no effect on the classification logic by itself, it only mirrors the parent distribution. 

So more easily:

$$
y(x) = \argmax_{k=1:K}{P(C_k) P(x | C_k)} = \argmax_{k=1:K}{P(C_k, x)}
$$

Now we are stuck, the join distribution can be arbitrary sophisticated. If we want to solve this we need more knowledge about the distribution.

Here's where the "naive" part comes from, because we are going to propose something that really blows up the distribution.

> Assume features are independent

You may be angry now, but this preposition isn't too bad, *if you study your data before going head to head with the dataset*!

This is why a really big part of any learning problem is getting to know your dataset, you don't want variables that are important but you can't see (most people call these trouble makers *Latent Confounders*). They will ruin these assumptions really bad.

Let us move on, you probably can see the solution from a mile away now, just apply the chain rule until all independency rules are exhausted, you'll get the following:

$$
y(x) = \argmax_{k=1:K}{P(C_k) \prod_{i=1}^{i=n} P(x_i | C_k)}
$$

Now we are done! The first expression is simple, you will find the frequency of each class and use it as a probability measure. If the dataset is large, you are not expected to be too far of the real value in the parent distribution.

The second term can also be found easily; fix each class and count the frequency of each feature. (our dataset consists entirely of discrete variables; if it was continuous, we probably would have need to quantize it first)

## Dataset
---

We are using the [UCI Nursery Dataset](http://archive.ics.uci.edu/ml/datasets/Nursery), the description of each feature is:

| Feature Name | Feature Space |
|--------------|---------------|
|   parents    |    usual, pretentious, great_pret
|   has_nurs   |    proper, less_proper, improper, critical, very_crit
|   form       |    complete, completed, incomplete, foster
|   children   |    1, 2, 3, more
|   housing    |    convenient, less_conv, critical
|   finance    |    convenient, inconv
|   social     |    non-prob, slightly_prob, problematic
|   health     |    recommended, priority, not_recom

Given these features, each nursery is assigned a class, the classes and their frequency are:

| class | N | N[\%\] |
|-------|---|--------|
| not_recom | 4320 | 33.333 \% |
| recommend | 2 | 0.015 \% |
| very_recom | 328 | 2.531 \% |
| priority | 4266 | 32.917 \% |
| spec_prior | 4044 | 31.204 \% |

This is basically $P(C_k)$.

## Implementation
---

The program will do the following:
- Load the dataset
- Convert to numerical (map feature space to numbers, keep the mapping!)
- Randomly partition the dataset into *train* and *test*
- Use the *train* dataset to get feature frequencies conditioned on classes
- Test your luck on the *train* dataset by finding which class maximises the probability

Most of this is straightforward, so no deep detail is needed.

## Results
---

We used 50 percent of the dataset for training. Evaluation is simple, just predict the class from the features and compare to the original value.

We'll go with a simple Hit/Miss evaluation here. Here is the results from one of our runs:

| # Hit | Hit \% | # Miss | Miss \% |
|-------|--------|--------|---------|
| 4137 | 85 | 723 | 15 |

Not too shabby.

## Suggestions?
---

As I said before, one of the most important factors before you attempt to do anything with a dataset is getting to know it, and by "getting to know* it, I do not mean learning the names and the valid sets of each variable. What I mean is identifying how much each variable is supposed to effect your model. This goes a lot deeper than you might expect, there are whole fields that are devoted to identifying the relationships between variables in population distributions, Causal-Inference being just one of them.

Sometimes, it is easy to see which variable might be the most important. When we are talking about the possibility of a forest catching fire in June, the variable of *humidity* probably plays a much more important role than say, *the number explosions in the most recent Michael Bay movie*. Though you might be surprised how much people are tempted to accept differnt variables into their models because of some correlation!

So what does that mean for the previous problem? Well unnecessary complexity is one of the main things that really hampers the performance of any classifier (more so with the naive classifier above), so it will work wonders if you would go on to study your data and reduce it's complexity. One of these ways is just *reducing dimensionality*, which can be done through something like PCA or the many other methods that exist.

Do this, and you can improve the accuracy quite a lot!