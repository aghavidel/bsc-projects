# A LiFi Communication Circuit

This small project is a rather naive implementation of a receiver and transmitter using light.

Basically, a high power LED mounted on a heat sink is biased in a suitable operating point, then a power amplifier mixes two signals with the voltage of the LED, thus modulating the light source with those two signals. The signals are:

- Sound waves, with a frequency of a few Hz all the way up to 4-5 kHz.
- An NRZ encoded pulse, with a rate of 15 kHz.

The light source will be biased in such a way that it will exhibit the smallest amount of non-linear behaviors. The power amplifier also will not amplify the signals too much, this is to prevent noise and THD.

The receiver end then:
- Receives the light source via a photosensor.
- Completely removes the DC portion.
- Amplify the signal a bit to get clear waves.
- Branch out, use an LPF to get the voice signal and a BPF to get the data.
- For the voice signal, apply a series of differential amplifiers until the sound is clear, it will be tunable with a potentiometer.
- Do the same with the data portion, feed the whole signal to a comparator to regenerate the data.

The full documentation will take ages, I might do it some day though, at the current moment only a persian report exists and the resources used to design the PCBs.

This project contains:
- *captures*: Some screenshots of the freq. responses and power curves.
- *datasheets*: Datasheets for pretty much every single element used in the PCBs.
- *LIB*: Part libraries for Altium.
- *Simulations*: LTSpice simulation files.

The pcb files in *PCB* are:
- Reciever.PcbDoc
- Transmitter.PcbDoc

This project is completely educational in it's purpose, it is also full of unwise design choices, I would love to come back and correct them, but at the moment it is not feasible.