package ir.sharif.messenger.utils;

public class SHA256 {
	private static final int[] initialH = {
		0x6a09e667,
		0xbb67ae85,
		0x3c6ef372,
		0xa54ff53a,
		0x510e527f,
		0x9b05688c,
		0x1f83d9ab,
		0x5be0cd19,
	};
	
	private static final int[] initialK = {
		0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
		0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
		0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
		0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
		0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
		0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
		0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
		0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
	};
	
	public static int[] getInitialH() {
		return initialH;
	}
	
	public static byte[] initialMessagePadding(byte[] originalMessage) {
		int L = originalMessage.length * 8;
		int K = getNumberOfPaddingBits(L);
		byte[] lengthPadding = getPadding(L);
		byte[] oneAndZerosPadding = new byte[(K+1)/8];
		oneAndZerosPadding[0] = (byte) -128;
		
		byte[] paddedMessage = new byte[originalMessage.length + oneAndZerosPadding.length + lengthPadding.length];
		System.arraycopy(
				originalMessage, 
				0, paddedMessage, 
				0, originalMessage.length
		);
		System.arraycopy(
				oneAndZerosPadding, 
				0, paddedMessage, 
				originalMessage.length, oneAndZerosPadding.length
		);
		System.arraycopy(
				lengthPadding, 
				0, paddedMessage, 
				originalMessage.length + oneAndZerosPadding.length, lengthPadding.length
		);
		
		return paddedMessage;
	}
	
	public static int[] schedule(byte[] chunk) {
		int[] words = new int[64];
		
		for (int i = 0; i < 16; i++) {
			byte[] word = {
					chunk[i*4],
					chunk[i*4+1],
					chunk[i*4+2],
					chunk[i*4+3]
			};
			
			words[i] = toInt(word);
		}
		
		for (int i = 16; i < 64; i++) {
			words[i] = sigma1(words[i-2]) + words[i-7] + sigma0(words[i-15]) + words[i-16]; 
		}
		
		return words;
	}
	
	public static int[] compress(int[] words, int[] H) {
		int a, b, c, d, e, f, g, h;
		int[] output = new int[8];
		
		a = H[0]; b = H[1]; c = H[2]; d = H[3];
		e = H[4]; f = H[5]; g = H[6]; h = H[7];
		
		int t0, t1;
		
		for (int i = 0; i < 64; i++) {
			t0 = h + bigSigma1(e) + choice(e, f, g) + initialK[i] + words[i];
			t1 = bigSigma0(a) + majority(a, b, c);
			h = g; g = f; f = e; e = d + t0;
			d = c; c = b; b = a; a = t0 + t1;
		}
		
		output[0] = H[0] + a; output[1] = H[1] + b; output[2] = H[2] + c; output[3] = H[3] + d;
		output[4] = H[4] + e; output[5] = H[5] + f; output[6] = H[6] + g; output[7] = H[7] + h;
		
		return output;
	}
	
	public static int[] hash(byte[] originalMessage) {
		byte[] paddedMessage = initialMessagePadding(originalMessage);
		byte[][] chunks = toChunks(paddedMessage);
		int[] H = initialH;
		
		int[] chunkScheduled;
		for (byte[] chunk: chunks) {
			chunkScheduled = schedule(chunk);
			H = compress(chunkScheduled, H);
		}
		
		return H;
	}
	
	public static int[] hash(String originalMessage) {
		return hash(originalMessage.getBytes());
	}
	
	public static byte[] hashByte(byte[] originalMessage) {
		int[] intHash = hash(originalMessage);
		byte[] byteHash = new byte[intHash.length * 4];
		
		for (int i = 0; i < intHash.length; i++) {
			System.arraycopy(toByteArray(intHash[i]), 0, byteHash, i*4, 4);
		}
		
		return byteHash;
	}
	
	public static String hash(String originalMessage, String outputFormat) {
		if (outputFormat.equals("Hex") | outputFormat.equals("hex")) {
			return intsToHex(hash(originalMessage));
		}
		else {
			int[] hashAsInts = hash(originalMessage);
			String hashAsIntString = "";
			
			for (int chunk: hashAsInts) {
				hashAsIntString += Integer.toString(chunk);
			}
			
			return hashAsIntString;
		}
	}
	
	private static byte[][] toChunks(byte[] paddedMessage) {
		int l = paddedMessage.length;
		int m = l / 64;
		byte[][] chunks = new byte[m][64];
		
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < 64; j++) {
				chunks[i][j] = paddedMessage[64*i + j];
			}
		}
		
		return chunks;
	}
	
	
	private static int getNumberOfPaddingBits(int L) {
		int x = L + 1 + 64;
		int r = x % 512;
		
		if (r == 0) {
			return L;
		}
		else {
			return 512 - r;
		}
	}
	
	private static byte[] getPadding(int L) {
		byte[] kAsBytes = toByteArray(L);
		byte[] padding = new byte[8];
		
		for (int i = 4; i < 8; i++) {
			padding[i] = kAsBytes[i-4];
		}
		
		return padding;
	}
	
	private static byte[] toByteArray(int value) {
	    return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value
	    };
	}
	
	private static int toInt(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) + ((bytes[1] & 0xFF) << 16) + ((bytes[2] & 0xFF) << 8) + (bytes[3] & 0xFF);
	}
	
	private static int rotr(int n, int x) {
		return (x >>> n) | (x << 32-n);
	}
	
	private static int bigSigma0(int x) {
		return rotr(2, x) ^ rotr(13, x) ^ rotr(22, x);
	}
	
	private static int bigSigma1(int x) {
		return rotr(6, x) ^ rotr(11, x) ^ rotr(25, x);
	}
	
	private static int sigma0(int x) {
		return rotr(7, x) ^ rotr(18, x) ^ (x >>> 3);
	}
	
	private static int sigma1(int x) {
		return rotr(17, x) ^ rotr(19, x) ^ (x >>> 10);
	}
	
	private static int choice(int x, int y, int z) {
		return (x & y) ^ (~x & z);
	}
	
	private static int majority(int x, int y, int z) {
		return (x & y) ^ (x & z) ^ (y & z);
	}
	
    final protected static char[] encoding = "0123456789ABCDEF".toCharArray();
    public static String intsToHex(int[] arr) {
        char[] encodedChars = new char[arr.length * 4 * 2];
        for (int i = 0; i < arr.length; i++) {
            int v = arr[i];
            int idx = i * 4 * 2;
            for (int j = 0; j < 8; j++) {
                encodedChars[idx + j] = encoding[(v >>> ((7-j)*4)) & 0x0F];
            }
        }
        return new String(encodedChars);
    }
}
