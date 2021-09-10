package ir.sharif.messenger.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class RSATest {

	@Test
	public void encodeDecodeTest() {
		final String RANDOM_GEN_KEY = "KEY FOR THE RANDOM NUMBER GENERATOR";
		final String RANDOM_MESSAGE_KEY = "KEY FOR RANDOM MESSAGE GENERATOR";
		RSA rsa = new RSA(RANDOM_GEN_KEY);
		RC4 gen = new RC4(RANDOM_MESSAGE_KEY.getBytes());
		
		rsa.getPublics();
		rsa.getSecrets();
		
		for (int i = 0; i < 500; i++) { 
			byte[] messageBytes = getARandomByteArray(128, gen);
			byte[] encode = rsa.enc(messageBytes);
			byte[] decode = rsa.dec(encode);
			
			assertArrayEquals(decode, messageBytes);
		}
	}
	
	private byte[] getARandomByteArray(int len, RC4 gen) {
		byte[] messageBytes;
		do {
			messageBytes = gen.cycle(len);
			messageBytes[0] = (byte) (0x7F & messageBytes[0]);	
		}
		while (messageBytes[0] == (byte) 0);
		
		return messageBytes;
	}

}
