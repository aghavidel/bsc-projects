# Introduction

<img src="https://render.githubusercontent.com/render/math?math=e^{i%20\pi}%20=%20-1&mode=inline">

We all know the normal RNN network models. There are certainly problems when we try to make them deep (i.e. vanishing/exploding) so we came up with better models (like LSTMs) but rarely were we concerned with making them more consitent with biological models.

If we are thinking in terms of performance, making neural networks adhere to biological models may not be a good idea at all! At some point we'll be adding complexity just for the sake of it, not for the sake of improving learning capacity. 

But there might be some other benefits to it, mainly the hope that we can understands biological decision making structures better. So don't expect anything mind-blowing in terms of *improved learning capacity* or *improved convergence rates*, expect some insight into how a real, biological structure might go on choosing betwwen A and B.

## Summary Of The Paper
---

You are certainly going to have a better understanding if you read the paper itself. But for the sake of brevity, we'll do a quick summary here.

We want a recurrent neural network whose weight matrix obeys the following:
- Diagonal elements are zero, neurons can only be influenced by other neurons.
- About 20 percent of the neurons are *Inhibitory*. They try to calm other neurons down and are associated with negative weight values. The rest are *Excitatory* and will have positive weights.

The paper proposes a diagonal matrix $D$ that will be comprised of 1s and negative 1s on it's diagonal. Without loss of generality, if we are training for example 100 neurons, neurons 1 to 20 will be inhibitory and neurons 21 to 100 will be excitatory. 

After each training we will check the matrix:
- Excitatory neurons with *positive* weights and Inhibitory neurons with *negative* weights are left alone.
- The rest are *wrong* in this model, we'll set them to 0.

This can be easily done by setting:

$$
    W_{corrected} = \frac{W + |W|D}{2} \;\;\;\;\;\; diag(W_{corrected}) = 0 \\

    W \leftarrow W_{corrected}
$$

For $D$, assume that the number of Excitatory and Inhibitory neurons are $n_e$ and $n_i$, then $D$ shall be:

$$
    D := 
    \left(
    \begin{array}{cc}
    I_{n_e \times n_e} & 0 \\
    \\
    0 & -I_{n_i \times n_i} \\
    \end{array} \right)
$$

So we'll have this resolved for now!

To make visualization more simple, our network (which we called it's class `rectifiedRNN`) will only have 10 neurons, thus both $D$ and $W$ are just 10 x 10 matrices.

Here is an example of how the matrix looks like after correction:

<img src=../Results/matrix_parity.PNG width=250>

Now the paper will go on to introduce the congnitive tasks, we'll talk about these in the next section.
