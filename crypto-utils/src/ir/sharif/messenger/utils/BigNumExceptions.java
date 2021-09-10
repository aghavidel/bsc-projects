package ir.sharif.messenger.utils;

public class BigNumExceptions {
	public void notAChunkException() throws NotAChunkException {
		new NotAChunkException();
	}
	public void factorizationException() throws FactorizationException {
		new FactorizationException();
	}
}

class NotAChunkException extends RuntimeException {
	public NotAChunkException() {
		System.out.println("This is not a chunk, it exceeds BLOCK_WEIGHT value.");
	}
}

class FactorizationException extends Exception {
	public FactorizationException() {
		System.out.println("Factorization failed.");
	}
}