package ir.sharif.messenger.utils;

import java.math.BigInteger;

interface BigNumberRoutines {
	public byte[] getBytes(String a);
	public String fromBytes(byte[] bs);
	public String add(String a, String b);
	public String mul(String a, String b);
	public String mod(String a, String b);
	public String modPow(String a, String b, String c);
	public String gcd(String a, String b);
	public String lcm(String a, String b);
	public boolean probablePrime(String a, int rounds);
	public String getAPrime(int bitLen, int rounds, RC4 gen);
	public byte[] padToLen(byte[] input, int bitLen);
	public String[] factorizePQ(String pq) throws FactorizationException;
}

public class BigNumHandler implements BigNumberRoutines {
	public byte[] getBytes(String a) {
		return new BigInteger(a).abs().toByteArray();
	}
	
	public String fromBytes(byte[] bs) {
		return new BigInteger(bs).abs().toString();
	}
	
	public String add(String a, String b) {
		BigInteger aB = new BigInteger(a);
		return aB.add(new BigInteger(b)).toString();
	}
	
	public String sub(String a, String b) {
		BigInteger aB = new BigInteger(a);
		return aB.subtract(new BigInteger(b)).abs().toString();
	}
	
	public String mul(String a, String b) {
		BigInteger aB = new BigInteger(a);
		return aB.multiply(new BigInteger(b)).toString();
	}
	
	public String mod(String a, String b) {
		BigInteger aB = new BigInteger(a);
		return aB.mod(new BigInteger(b)).toString();
	}
	
	public String div(String a, String b) {
		BigInteger aB = new BigInteger(a);
		BigInteger bB = new BigInteger(b);
		return aB.divide(bB).toString();
	}
	
	public String modPow(String a, String b, String c) {
		BigInteger aB = new BigInteger(a);
		return aB.modPow(new BigInteger(b), new BigInteger(c)).toString();
	}
	
	public String gcd(String a, String b) {
		BigInteger aB = new BigInteger(a);
		BigInteger bB = new BigInteger(b);
		return aB.gcd(bB).toString();
	}
	
	public String lcm(String a, String b) {
		String mult = mul(a, b).toString();
		return div(mult, gcd(a, b));
	}
	
	public String modInv(String a, String n) {
		BigInteger aB = new BigInteger(a);
		BigInteger nB = new BigInteger(n);
		return aB.modInverse(nB).toString();
	}
	
	public boolean probablePrime(String a, int rounds) {
		BigInteger aB = new BigInteger(a);
		return aB.isProbablePrime(rounds);
	}
	
	public String getAPrime(int bitLen, int rounds, RC4 gen) {
		int numberOfBytes;
		
		if (bitLen % 8 == 0) {
			numberOfBytes = bitLen / 8;
		}
		else {
			numberOfBytes = bitLen / 8 + 1;
		}
		
		while(true) {
			byte[] randomNumber = padToLen(gen.cycle(numberOfBytes), bitLen);
			BigInteger candidate = new BigInteger(randomNumber);
			if (probablePrime(candidate.toString(), rounds)) {
				return candidate.abs().toString();
			}
		}
	}
	
	public byte[] padToLen(byte[] input, int bitLen) {
		int len = input.length;
		int pad = bitLen;
		byte[] output = new byte[len];
		
		if (bitLen >= len * 8) {
			return input;
		}
		
		for (int i = len-1; i >= 0; i--) {
			if (pad - 8 >= 0) {
				pad -= 8;
				output[i] = input[i];
			}
			else {
				if (pad == 0) {
					break;
				}
				else {
					byte mask = (byte) 0x01;
					for (int j = 0; j < pad-1; j++) {
						mask <<= 1;
					}
					output[i] = (byte) (mask & input[i]);
					break;
				}
			}
		}
		
		return output;
	}
	
	public String[] factorizePQ(String pq) {
		String x = "2";
		String y = "2";
		String d = "1";
		
		while (d.equalsIgnoreCase("1")) {
			x = mod(add(mul(x, x), "1"), pq);
			y = mod(add(mul(mod(add(mul(y, y), "1"), pq), mod(add(mul(y, y), "1"), pq)), "1"), pq);
			d = gcd(sub(x, y), pq);
		}
		
		if (d.equalsIgnoreCase(pq)) {
			throw new RuntimeException("Factorization failed!");
		}
		else {
			String p = d;
			String q = div(pq, p);
			
			return new String[]{p, q};
		}
	}
}