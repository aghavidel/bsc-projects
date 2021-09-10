# Some Cryptographic Utilities

This project was supposed to be an implementation of MTProto; A fully encrypted messaging app.

I implemented it when I took a cryptography course, this was the final project and was concerned with:
- Implementing all cryptographic subroutines from scratch (AES, RSA, SHA, Diffie-Hellman, etc.)
- Aggregate all of these projects into a secure messaging app using MTProto.

The project did not allow the use of any other libraries beside *java.lang*, including *BigInteger*. So we had to implement it by hand (a ludicrous requirement indeed, since speed becomes extremely important for cryptographic implementations).

This project at the moment contains only the cryptographic subroutines and many unit tests for each of them. I am not allowed to release the protocol implementation since it was a joint effort. The big integer implementation is very messy and sluggish (even though it is the bulk of this project still!).

You will find detailed implementations of:
- AES-256
- RSA-256
- SHA-256
- Some large number utilities
- A random number generator based on RC4
- Unit tests for each of them in JUnit

Most of them already have javadocs, but I will probably add some wikis later.

Feel free to use this if you are trying to implement the same things in Java or any other language.
