package ir.sharif.messenger.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class RC4Tests {
	/**
	 * These tests were taken from <a href="https://en.wikipedia.org/wiki/RC4#Test_vectors">Wikipedia</a>,
	 *  they are not official. 
	 */
	@Test
	public void rc4StreamTest() {
		String key1 = "Key";
		String key2 = "Wiki";
		String key3 = "Secret";
		
		RC4 gen1 = new RC4(key1.getBytes());
		RC4 gen2 = new RC4(key2.getBytes());
		RC4 gen3 = new RC4(key3.getBytes());
		
		
		byte[] output1 = new byte[10];
		output1 = gen1.cycle(output1.length);

		byte[] output2 = new byte[6];
		output2 = gen2.cycle(output2.length);

		byte[] output3 = new byte[8];
		output3 = gen3.cycle(output3.length);
		
		assertEquals("EB9F7781B734CA72A719", bytesToHex(output1));
		assertEquals("6044DB6D41B7", bytesToHex(output2));
		assertEquals("04D46B053CA87B59", bytesToHex(output3));
	}
	
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	
	private static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    
	    return new String(hexChars);
	}
}
