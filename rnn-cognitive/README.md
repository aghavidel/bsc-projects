# RNN Based On Biological Models (a bit!)

This little project is an implementaion of an RNN in PyTorch, based on the criteria in the paper [Training Excitatory-Inhibitory Recurrent Neural Network for Cognitive Tasks](https://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1004792).

Basically, neurons in the network are segregated into *Excitatory* and *Inhibitory* groups; as the name suggests they can only increase or decrease action values, which means that they can only have weight values of a certain sign (positive or negative).

There are a few other things (like structural noise, each level we get deep in the network, some noise will be added to the action values) which we preferred to largely ignore.

The paper proposes certain cognitive tasks to test the effectiveness of this structure, we'll be implementing the following:
- Perceptual decision-making with variable length stimulus
- Perceptual decision-making with fixed length stimulus
- Parametric working memory
- Sequence execution

At each task, we need to create a dataset and then train our model on it. We will observe the responses, loss functions and the weight matrix.

- The main code is in `impl.ipynb`.
- The results are in the `Results` folder.

There will be details in the wiki (this project is completely educational in it's purpose and can certainly be improved).