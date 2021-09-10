package ir.sharif.messenger.utils;

import java.math.BigInteger;

public class RSA {
	private RC4 gen;
	private String p, q, phi, d;
	public String n, e;
	private static final int PRIME_LEN_BIT = 1024;
	private static final int NUMBER_OF_ROUNDS = 10;
	
	BigNumHandler bigNumberHelper = new BigNumHandler();
	
	public RSA(String seedForRC4) {
		gen = new RC4(seedForRC4.getBytes());
		this.keyGen();
	}
	
	public void keyGen() {
		p = bigNumberHelper.getAPrime(PRIME_LEN_BIT, NUMBER_OF_ROUNDS, gen);
		q = bigNumberHelper.getAPrime(PRIME_LEN_BIT, NUMBER_OF_ROUNDS, gen);
		n = bigNumberHelper.mul(p, q);
		phi = bigNumberHelper.lcm(bigNumberHelper.sub(q, "1"), bigNumberHelper.sub(p, "1"));
		e = new BigInteger("65537").toString();
		d = bigNumberHelper.modInv(e, phi);
	}
	
	public static byte[] enc(byte[] message, String e, String n) {
		BigNumHandler bigNumberHelper = new BigNumHandler();
		String messageAsString = new BigInteger(message).toString();
		String messageEnc = bigNumberHelper.modPow(messageAsString, e, n);
		return bigNumberHelper.getBytes(messageEnc);
	}
	
	public byte[] enc(byte[] message) {
		return enc(message, e, n);
	}
	
	public static byte[] dec(byte[] messageEnc, String d, String n) {
		BigNumHandler bigNumberHelper = new BigNumHandler();
		String messageEncAsString = new BigInteger(messageEnc).toString();
		String message = bigNumberHelper.modPow(messageEncAsString, d, n);
		return bigNumberHelper.getBytes(message);
	}
	
	public byte[] dec(byte[] messageEnc) {
		return dec(messageEnc, d, n);
	}
	
	public void getPublics() {
		System.out.println("e: " + e);
		System.out.println("n: " + n);
	}
	
	void getSecrets() {
		System.out.println("d: " + d);
		System.out.println("n: " + n);
	}
}
