package ir.sharif.messenger.utils;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * This is a test class for the AES256 implementation.
 * The tests come from various sources, the padding and inversion tests however
 * are pretty simple so i just made them up. For the other tests the resources
 * are provided in the links.
 * @author Arvin
 * @see <a href="https://www.cryptool.org/en/cto/aes-step-by-step">This absolutely awesome site</a>, 
 * <a href="https://csrc.nist.gov/CSRC/media/Projects/Cryptographic-Algorithm-Validation-Program/documents/aes/AESAVS.pdf">
 * Official NIST test vectors for AES (they don't mention paddings though)</a>
 */
public class AES256Test {

	@Test
	public void paddingTest() {
		String s1 = "Hello world";
		String s2 = "";
		String s3 = "A longer test to see if at least i have not yet messed up the padding!";
		
		String padded1 = AES256.pkcs7Padding(s1);
		String padded2 = AES256.pkcs7Padding(s2);
		String padded3 = AES256.pkcs7Padding(s3);
		
		assertEquals(s1, AES256.pkcs7Unpadding(padded1));
		assertEquals(s2, AES256.pkcs7Unpadding(padded2));
		assertEquals(s3, AES256.pkcs7Unpadding(padded3));
	}
	
	@Test
	public void inversionTests() {
		String stateFlatString = "9147008207631182";
		
		byte[] stateFlat = stateFlatString.getBytes();
		byte[][] state = AES256.toColumnMajor(stateFlat);
		
		byte[] subBytesTest = AES256.subBytesDec(AES256.subBytesEnc(stateFlat));
		byte[] shiftRowsTest = AES256.fromColumnMajor(AES256.shiftRowsInv(AES256.shiftRows(state)));
		byte[] mixColumnTest = AES256.fromColumnMajor(AES256.mixColumnsInv(AES256.mixColumns(state)));
		
		assertArrayEquals(stateFlat, subBytesTest);
		assertArrayEquals(stateFlat, shiftRowsTest);
		assertArrayEquals(stateFlat, mixColumnTest);
	}
	
	@Test
	public void mixColumnsTest() {
		byte[] test1 = {(byte) 0xdb, (byte) 0x13, (byte) 0x53, (byte) 0x45};
		byte[] test2 = {(byte) 0x2d, (byte) 0x26, (byte) 0x31, (byte) 0x4c};
		byte[] test3 = {(byte) 0xd4, (byte) 0xd4, (byte) 0xd4, (byte) 0xd5};
		
		byte[] ans1 = {(byte) 0x8e, (byte) 0x4d, (byte) 0xa1, (byte) 0xbc};
		byte[] ans2 = {(byte) 0x4d, (byte) 0x7e, (byte) 0xbd, (byte) 0xf8};
		byte[] ans3 = {(byte) 0xd5, (byte) 0xd5, (byte) 0xd7, (byte) 0xd6};
		
		assertArrayEquals(ans1, RijndaelUtils.mixColumns(test1));
		assertArrayEquals(ans2, RijndaelUtils.mixColumns(test2));
		assertArrayEquals(ans3, RijndaelUtils.mixColumns(test3));
		
		assertArrayEquals(test1, RijndaelUtils.mixColumnsInv(ans1));
		assertArrayEquals(test2, RijndaelUtils.mixColumnsInv(ans2));
		assertArrayEquals(test3, RijndaelUtils.mixColumnsInv(ans3));
	}
	
	@Test
	public void keyExpansionTest() {
		String key1 = "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4";
		String expandedKey = 
					  "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4" +
					  "9ba354118e6925afa51a8b5f2067fcdea8b09c1a93d194cdbe49846eb75d5b9a" +
					  "d59aecb85bf3c917fee94248de8ebe96b5a9328a2678a647983122292f6c79b3" +
					  "812c81addadf48ba24360af2fab8b46498c5bfc9bebd198e268c3ba709e04214" +
					  "68007bacb2df331696e939e46c518d80c814e20476a9fb8a5025c02d59c58239" +
					  "de1369676ccc5a71fa2563959674ee155886ca5d2e2f31d77e0af1fa27cf73c3" +
					  "749c47ab18501ddae2757e4f7401905acafaaae3e4d59b349adf6acebd10190d" +
					  "fe4890d1e6188d0b046df344706c631e";
		
		byte[] keyAsBytes1 = AES256.hexToBytes(key1);
		String generatedKeys1 = AES256.bytesToHex(AES256.flatten(AES256.keyExpansion(keyAsBytes1)));
		assertEquals(expandedKey, generatedKeys1);
	}
	
	@Test
	public void fullTest() {
		String key1 = "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4";
		String iv1 = "000102030405060708090a0b0c0d0e0f";
		String message1 = "6bc1bee22e409f96e93d7e117393172a";
		String enc1 = "f58c4c04d6e5f1ba779eabfb5f7bfbd6485a5c81519cf378fa36d42b8547edc0";
		
		String key2 = "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4";
		String iv2 = "f58c4c04d6e5f1ba779eabfb5f7bfbd6";
		String message2 = "ae2d8a571e03ac9c9eb76fac45af8e51";
		String enc2 = "9cfc4e967edb808d679f777bc6702c7d3a3aa5e0213db1a9901f9036cf5102d2";
		
		String key3 = "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4";
		String iv3 = "9cfc4e967edb808d679f777bc6702c7d";
		String message3 = "30c81c46a35ce411e5fbc1191a0a52ef";
		String enc3 = "39f23369a9d9bacfa530e263042314612f8da707643c90a6f732b3de1d3f5cee";
		
		String key4 = "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4";
		String iv4 = "f58c4c04d6e5f1ba779eabfb5f7bfbd6";
		String message4 = "30c81c46a35ce411e5fbc1191a0a52efae2d8a571e";
		
		String encodedMessage1 = AES256.encHex(message1, iv1, key1);
		assertEquals(enc1, encodedMessage1);
		String decodedMessage1 = AES256.decHex(encodedMessage1, iv1, key1);
		assertEquals(message1, decodedMessage1);
		
		String encodedMessage2 = AES256.encHex(message2, iv2, key2);
		assertEquals(enc2, encodedMessage2);
		String decodedMessage2 = AES256.decHex(encodedMessage2, iv2, key2);
		assertEquals(message2, decodedMessage2);
		
		String encodedMessage3 = AES256.encHex(message3, iv3, key3);
		assertEquals(enc3, encodedMessage3);
		String decodedMessage3 = AES256.decHex(encodedMessage3, iv3, key3);
		assertEquals(message3, decodedMessage3);
		
		String encodedMessage4 = AES256.encHex(message4, iv4, key4);
		String decodedMessage4 = AES256.decHex(encodedMessage4, iv4, key4);
		assertEquals(message4, decodedMessage4);
	}
}
