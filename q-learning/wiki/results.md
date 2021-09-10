# Results

Most of the tests are performed in 10000 epsidoes, while some may require 20000 episodes. The signes of a good training is two things:

- There are random elements in this model, do not expect a clean, steady loss function, there is going to be spikes. But the *frequency of the spikes* and the *short-term mean* most decrease.
- The rewards should show a pretty steady incline.

Here is a typical result with $\gamma = 0.3$ and $\epsilon = 0.5$:

<p align=center>
    <img src="..\Results\10000_gamma = 0.3, e0 = 0.5.PNG" width=300>
    <img src="..\Results\loss_10000_gamma = 0.3, e0 = 0.5.PNG" width=292>
</p>

There is the problem of the hyperparameters though, for that you should just explore, there are many different results in the `Results` folder so you can check them out.

But for a general rule (which applies only *most* of the time):
- Increasing $\gamma$ and reducing $\epsilon$ favors *exploitation* while rewarding the network in the first phases. This gives a good headstart but can cause the network to sit in suboptimal points.
- Decreasing $\gamma$ and increasing $\epsilon$ favors *exploration* while penalizing transition rewards. This causes the loss function to be much more *spiky* and causes the training process to be unnecessarily long, but it can help the network converge to higher results later on.

Here is the best that we got, with 20000 episodes of training and $\gamma=0.4, \epsilon=0.6$:

<p align=center>
    <img src="..\Results\20000_gamma = 0.4_e0 = 0.6_final.PNG" width=276>
    <img src="..\Results\loss_20000_gamma = 0.4, e0 = 0.6_final.PNG" width=300>
</p>

Here, the plots are smoothed a bit with a moving average to make them look better, since epsilon is pretty high, the results would be a mess without smoothing.
