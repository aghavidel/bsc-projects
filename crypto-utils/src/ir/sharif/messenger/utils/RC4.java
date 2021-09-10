package ir.sharif.messenger.utils;

public class RC4 {
	private int keyLen;
	private byte[] S;
	
	public RC4(byte[] key) {
		keyLen = key.length;
		S = new byte[256];
		
		for (int i=0; i<256; i++) {
			S[i] = (byte) i; 
		}
		
		int j=0;
		
		for (int i=0; i<256; i++) {
			j = (j + toUnsigned(S[i]) + toUnsigned(key[i % keyLen])) % 256;
			S = swap(S, i, j);
		}
	}
	
	public byte[] swap(byte[] array, int i , int j ) {
		byte temp;
		temp = array[i];
		array[i] = array[j];
		array[j] = temp;
		
		return array;
	}
	
	public byte[] cycle(int numberOfBytes) {
		byte[] output = new byte[numberOfBytes];
		int i=0;
		int j=0;
		
		for (int counter=0; counter < numberOfBytes; counter++) {
			i = (i+1) % 256;
			j = (j + toUnsigned(S[i])) % 256;
			S = swap(S, i, j);
			output[counter] = S[((toUnsigned(S[i]) + toUnsigned(S[j])) % 256)];
		}
		
		return output;
	}
	
	private int toUnsigned(byte b) {
		return 0xFF & b;
	}
}
