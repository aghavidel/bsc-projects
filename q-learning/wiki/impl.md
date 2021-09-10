# Implementation

Here we will talk a little about the implementations. The implementation will be in two parts, the *emulator* class, `Shadlen` and the *agent* class `Monkey`.

## Shadlen
---

This class is prettt straightforward and consists of the following attributes:

- `action_correctness`: A list that records weather or not $G = G_M$
- `step`: The current level in $\mathcal{T}$
- `criterion`: A value used in our version of $f_g$, always ***0.5***
- `stimuli`: The list of current objects that shall be shown to the monkey.
- `state`: The one-hot array that encodes the current level in the tree (step) and the stimuli.

The main interfaces into this class are `reset` and `response`.

- `reset`: Reinitialize the emulator and return the current state. This calls the method `generate_stimuli` that randomly pulls out 4 objects from the set (this can include repetitions) and puts them in `stimuli`.
- `response`: Traverse one level in the graph, return rewards (zero of non-terminal), states and weather or not we have reached a terminal.

The emulator need only be instantiated once:

```python
emulator = Shadlen()
```

## Implementation Of The Algorithm
---

Before we get to the implementation of these classes, a little reciew of Q-Learning might help.

First we define the *Q*, it is a function, $Q: S \times A \rightarrow \mathbb{R}$, that maps a tuple of action and state into a reward. Of course if we knew this function we could have just take the action corresponding to: 

$$
    \underset{a}{\mathrm{argmax}} \; Q(s, a)
$$ 

The whole point of our learning process is estimating *Q*, so keep that in mind. From now on we refer to this table as the ***quality*** table.

With that said, the whole process for learning *Q* is still a bit too hazy, when we start the process, we know literary nothing about the emulator, so no action really seems better than the other. Plus if our goal is to maximize the rewards, we need to make sure that we traverse to deepest layers of the graph, since the highes rewards could be in there afterall.

So, why not *pick random actions until we learn something*?

This is a good idea, however after a while, it would probably be wise to ignore some of these random outcomes, for two reasons:
- We assume that the agents interactions with the emulator, obey the *markov property*, that means that the emulator will not behave differently when an experiment with the same state, action and transition is repeated several times.
- The parameters of the emulator are stationary (this means that the tree for example, does not change after a few tests ...)

All of this, means that if the agent knows the quality of a specific pair $(s, a)$, then they can *cheat* and be confident that it will not change, thus they can pick the action with the best quality, thus greedily maximise their results.

And this actually work! provided that the agent ***explores*** enough (searchs for all the rewards in the tree) and then starts to ***exploit*** the Q table.

And here is where the problem starts:
> **what if the Q-table is just too big?**

This is where the *deep* part in the title of the algortihm comes to the rescue.

Instead of literary creating a table for Q, we try to encode most of it in a neural network. Of course, there is the problem of how we would estimate the required capacity of the network, but that is the case in literary any learning problem so assume that we'll just brute force it later.

So, we should equip our agent with some decision making network, preferably a recurrent one to keep the encoding of the levels in the tree (which do obey some order due to their nature).

One particularly tempting choice for such a network are ***LSTMs***, recurrent networks that can (intuitively) *forget* bad things that they learned.

There is still two problems though:
- The balance between ***exploring*** and ***exploiting***, when should we atually switch between these two modes?
- How to prevent overfitting? This is actually a very delicate problem, because consider this scenario; we give you a tree that has a preety nice reward somewhere in the upper levels, but there is a slightly bigger reward all the way down the tree, after many many levels. What do you think your network will do? **It'll probably converge to the first reward and just sit there for all eternity!**

Both of these problems are addressed in this algorithm, now that we have these out of the way, let's talk about the algorithm itself.

## The Algorithm
---

Let's get straight to the point, here is the algorithm:
1. Initialize the emulator and obtain the starting state $s_1$
2. Initialize the predictor network weights $\theta_1$
3. For each episode of training:
    - initialize $t \leftarrow 1$
    - while no terminal has been reached:
        - predict $Q(s_t, .)$ by feeding $s_t$ to the network
        - with probability $\epsilon$, choose a random action $a_t$, if not, then choose the best action acording to your network.
        - execute action in emulator, $(r_t, s_{t+1}) \leftarrow \mathcal{E}$
        - calculate $Q^*(s_t, a_t)$ by:
        -   $$
            Q^*(s_t, a_t) \leftarrow r_t + \gamma \max_a Q(s_{t+1}, a)
            $$
        - Perform a gradient descent on your network, update the weights $\theta$ such that the networks output, $Q(s_t, a_t)$ converges to $Q^*(s_t, a_t)$
        - $t \leftarrow t+1$

Let's talk about this for a moment.

In the original form of q-learning, we essentially try to fill a table that maps pairs of state and actions to real numbers. But we left two things undiscussed:

- The eploration/eploitation policy
- The whole update rule!

Now we need to talk about these. Let's start with the policy.

### Exploration / Eploitation policy

There are many different ways to implement this, two of the most famous of them being the "$\epsilon \; \text{greedy}$" policy and the Boltzman policy.

As you might have guessed, we went with the first one. With probability $\epsilon$ we choose a random action and if not, we will exploit our current knowledge of the Q table.

There is a problem though, ideally you want to explore in the first few episodes to learn about the environment and you want to eploit more in the later episodes to use you current knowledge of the environment.

So, there is almost always a *decay* in this policy, in our implementation for example, the probability $\epsilon$ decays exponentially with a time constant of 1000 episodes.

### Bellman Equation For Q Value Update

This is the important part, how do we even update the values? The original Q-Learning algorithm had a simple update, given a learning rate of $\alpha$ the update can be done by:

$$
    Q(s_t, a_t) \leftarrow Q(s_t, a_t) + \alpha (r_t + \gamma \max_{a^{\prime}} Q(s_{t+1}, a^{\prime}) - Q(s_t, a_t))
$$

The creature that is multiplied by $\alpha$ is called the *temporal difference*, this is just a simple iteration update that tries to converge $Q(s_t, a_t)$ to $r_t + \gamma \max_{a^{\prime}} Q(s_{t+1}, a^{\prime})$.

One thing is left though, what is with that $\gamma$ coefficient?

This is called a *discount factor*, it dampens the rewards for state transitions as to encourage the model to rely solely on the terminal rewards instead of transition rewards (note that we always have $0 < \gamma \leq 1$). This is also a hyperparameter that the experimenter can tune to fit the environment better. Note that Q-learning is ***model free***, this means that at no point we attempt to model the environment, you will never see probabilistic quantities like $P(s_{t+1} | s_t, a_t)$ in these models. So hyperparameters are necessary to allow for a more flexible model.
