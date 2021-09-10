# Implementations And Results

One thing that you will probably notive when training this network is that it tends to overfit quite easily! This can make training a lot harder than it has any rights to be.

One of the main aspects of the paper is fine-tuning the many parameters mentioned, this is a task that we really didn't want to tackle here, testing all of the tasks takes a really, really long time. This was a course assignment so I really did not have the luxury :).

For the general implementation though, the code is sectioned into different parts and it will probably be easy to follow, the bare bones of the implementation is:

- The model is called `rectifiedRNN` and is just a simple RNN implemented from scratch. The main modification of the weights is done in `segregate_weights` which is called during `forward`.
- As per the instructions, 20 percent of the neurons are Inhibitory.
- For optimization we just used the builtin Adam optimizer. The optimized loss function is MSE.

The main thing in each test is deciding the hyperparameters. What we considered were the following:
- The number of hidden neurons in each layer.
- The `alpha` parameter, which also tuned in the paper. This dictates the amount of memory in the network. If you set it to 0, then the network will have no mamory at all. You can see this in the code as well:

```Python
    def forward(self, input_data, hidden):
        new_weight = self.segregate_weights(self.fc_hh.weight)
        self.fc_hh.weight = torch.nn.Parameter(new_weight)

        hidden_next =   torch.tanh((1 - alpha) * hidden + 
                        alpha * (self.fc_ih(input_data) + 
                        self.fc_hh(torch.tanh(hidden))))

        output = torch.tanh(self.fc_ho(hidden_next))
        hidden = hidden_next
```

- The learning rate, but that really goes without saying, doesn't it?

For each task, these were the values used:

| Perceptual Decision Making | Sequence Execution | Working Memory |
|:--------------------------:|:------------------:|:--------------:|
| alpha = 0.5 | alpha = 0.5 | alpha = 0.5 |
| lr = 1e-3 | lr = 1e-3 | lr = 1e-3 |
| epochs = 5 | epochs = 5 | epochs = 5 |
| hiddens = 20 | hiddens = 20 | hiddens = 20 |