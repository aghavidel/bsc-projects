# Detection of The Onset of Parkinson's Disease Using Speech Samples
**Note**: This code was tested and written with MATLAB R2017b.

This project tries to implement a classifier that is trained on sound samples and is tuned to detect patients that could be at the risk of developing Parkinson's.

The bulk of this code is based on the following papers.

- [Hanratty, Jane, et al. "Analysis of glottal source parameters in Parkinsonian speech." 2016 38th Annual International Conference of the IEEE Engineering in Medicine and Biology Society (EMBC). IEEE, 2016.](https://ieeexplore.ieee.org/abstract/document/7591523/)
- [Chien, Yu-Ren, et al. "Evaluation of glottal inverse filtering algorithms using a physiologically based articulatory speech synthesizer." IEEE/ACM Transactions on Audio, Speech, and Language Processing 25.8 (2017): 1718-1730.](https://ieeexplore.ieee.org/abstract/document/7946161/)

Refer to the code comments and docstrings for the implementation details.

Refer to the *plots* folder for the outputed plots.

Remember to put your dataset in the correct path, there is a variable that lets you define where your dataset is; in the end your dataset should contain only recording 
files (in whatever format, .wav or .mp4) and consisting of two folders, the 'PD' for
Parkinsons afflicted patients and 'Healthy' for healthy patients. 

Our datasets came from verious sources, for the healthy patients we used the [Saarbrucken Voice Database](http://stimmdb.coli.uni-saarland.de/).

For the PD afflicted patients we used [Hlavnička, Jan; Čmejla, Roman; Klempíř, Jiří; Růžička, Evžen; Rusz, Jan (2019): Synthetic vowels of speakers with Parkinson’s disease and Parkinsonism.](https://doi.org/10.6084/m9.figshare.7628819)

These are both big datasets and we cannot zip the code with the files ...

The project itself consists of the following:
- A series of independent MATLAB function files.
- A driver code *main.m* which is the entry point of the project.

Check the wiki for details.