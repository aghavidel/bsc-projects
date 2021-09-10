package ir.sharif.messenger.utils;

import java.nio.charset.StandardCharsets;

/**
 * An implementation of the AES256 symmetric encryption algorithm in CBC mode.
 * We may reference the original standard at some points but the bulk of this
 * code was written using the materials in the following links.
 * @author Arvin
 * @see
 * <a href="https://engineering.purdue.edu/kak/compsec/NewLectures/Lecture8.pdf">Lecture
 * on AES by Avi Kak, Purdue University</a>, The book Introduction to Modern Cryptography
 * by Katz and Lindel, and the lecture notes from the course that i took in the university
 * which i am not sure i am allowed to provide. 
 */
public class AES256 {
	/**
	 * Block length is fixed to 128 bits, i.e. 16 bytes.
	 */
	private static final int BLOCK_LENGTH_BYTE = 16;
	
	/**
	 * This is AES256, thus 256 bits for the input key, which
	 * adds up to 32 bytes for the input key.
	 */
	private static final int KEY_LEN_BYTE = 32;
	
	/**
	 * The length of the input key in chunks of 32 bit (words).
	 */
	private static final int KEY_LEN_WORDS = 8;
	
	/**
	 * AES256 has 14 rounds by standard plus one initial whitening round
	 * in the beginning, this adds up to 15 rounds which use a key of length
	 * 128 bit, or 4 words. So we need 4*15 = 60 words of key. 8 are provided
	 * in the beginning and the remaining 52 words must be generated through key
	 * expansion, we need 7 rounds to get these 52 words and excess is just
	 * ignored.
	 */
	private static final int NUMBER_OF_KEY_EXPANSION_ROUNDS = 7;
	
	/**
	 * This comes from the standard.
	 * @see <a href="http://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.197.pdf">NIST paper on AES</a>
	 */
	private static final int NUMBER_OF_ROUNDS = 14;
	
	/**
	 * Round constants during the key expansion algorithm.
	 * every time the MSB of the words are XORed with these
	 * values and the rest are remained untouched.
	 */
	private static final byte[][] rCon = {
			{(byte)0x01}, {(byte)0x02}, {(byte)0x04}, {(byte)0x08},
			{(byte)0x10}, {(byte)0x20}, {(byte)0x40}, {(byte)0x80}, 
			{(byte)0x1B}, {(byte)0x36}, {(byte)0x00}, {(byte)0x00}, 
			{(byte)0x00}, {(byte)0x00}, {(byte)0x00}, {(byte)0x00}
	};
	
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	
	/**
	 * Pad input byte array with pkcs7 standard.
	 * @param input: byte array that is to be padded.
	 * @return the input byte array padded with pkcs7 standard. here
	 * it will padded to a length that is a multiple of {@code BLOCK_LENGTH_BYTE}
	 * @see <a href="https://tools.ietf.org/html/rfc5652#section-6.3">RFC 5652</a> 
	 */
	public static byte[] pkcs7Padding(byte[] input) {
		int l = input.length;
		int r = l % BLOCK_LENGTH_BYTE;
		int numberOfPaddingBytes = BLOCK_LENGTH_BYTE - r;
		byte padVal = (byte) numberOfPaddingBytes;
		
		byte[] output = new byte[l + numberOfPaddingBytes];
		
		System.arraycopy(input, 0, output, 0, l);
		
		for (int i = 0; i < numberOfPaddingBytes; i++) {
			output[i + l] = padVal;
		}
		
		return output;
	}
	
	/**
	 * Pad input string with pkcs7 standard.
	 * @param input: ASCII string input to pad.
	 * @return Padded input as a string.
	 * @see {@link #pkcs7Padding(byte[])}
	 */
	public static String pkcs7Padding(String input) {
		byte[] inputBytes = input.getBytes();
		byte[] outputBytes = pkcs7Padding(inputBytes);
		return new String(outputBytes, StandardCharsets.UTF_8);
	}
	
	/**
	 * Unpad an input byte array padded with pkcs7 standard.
	 * @param input: byte array of the padded array.
	 * @return the original unpadded string.
	 * @see <a href="https://tools.ietf.org/html/rfc5652#section-6.3">RFC 5652</a> 
	 */
	public static byte[] pkcs7Unpadding(byte[] input) {
		int l = input.length;
		int padVal = 0xFF & input[l-1];
		byte[] output = new byte[l - padVal];
		System.arraycopy(input, 0, output, 0, l-padVal);
		return output;
	}
	
	/**
	 * Unpad an input string the was padded with pkcs7 standard.
	 * @param input the padded string.
	 * @return unpadded string.
	 * @see {@link #pkcs7Unpadding(byte[])}
	 */
	public static String pkcs7Unpadding(String input) {
		byte[] inputBytes = input.getBytes();
		byte[] outputBytes = pkcs7Unpadding(inputBytes);
		return new String(outputBytes, StandardCharsets.UTF_8);
	}
	
	/**
	 * Utility for converting byte array to hex string.
	 * @param bytes byte array.
	 * @return hex string.
	 */
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars).toLowerCase();
	}
	
	/**
	 * Utility to convert hex string to byte array.
	 * @param strAsHex The hex string.
	 * @return Byte array of the hex string.
	 */
	public static byte[] hexToBytes(String strAsHex) {
		byte[] bytes = new byte[strAsHex.length()/2];
		int val;
		
		if (strAsHex.length() % 2 != 0) {
			throw new RuntimeException("Invalid input hex string.");
		}
		
		for (int i = 0; i < strAsHex.length()/2; i++) {
			val = Integer.valueOf(strAsHex.substring(i*2, (i+1)*2), 16);
			bytes[i] = (byte) val;
		}
		
		return bytes;
	}
	
	/**
	 * Circularly shift a byte array to the left.
	 * @param input The original byte array
	 * @param n Number of shifts
	 * @return The input byte array, shifted n times to left.
	 */
	private static byte[] leftCircsularShift(byte[] input, int n) {
		if (n == 0) {
			return input;
		}
		
		int len = input.length;
		byte[] output = new byte[len];
		for (int i = 0; i < len; i++) {
			output[(i - n + len) % len] = input[i];
		}
		
		return output;
	}

	/**
	 * Perform the SubstituteByte step.
	 * @param input A byte array
	 * @return Substituted byte array.
	 */
	public static byte[] subBytesEnc(byte[] input) {
		int len = input.length;
		byte[] output = new byte[len];
		
		for (int i = 0; i < len; i++) {
			output[i] = RijndaelUtils.getSubstitute(input[i]);
		}
		
		return output;
	}
	
	/**
	 * Perform the inversion of SubstituteByte step.
	 * @param input A substituted byte array
	 * @return Original byte array.
	 * @see {@link #subBytesEnc(byte[])}
	 */
	public static byte[] subBytesDec(byte[] input) {
		int len = input.length;
		byte[] output = new byte[len];
		
		for (int i = 0; i < len; i++) {
			output[i] = RijndaelUtils.getSubstituteInv(input[i]);
		}
		
		return output;
	}
	
	/**
	 * XOR two byte arrays, the arrays are padded to the same length
	 * with zeros (this is pretty much just for the Round Constant adding
	 * while key expanding.
	 * @param a First byte array 
	 * @param b Second byte array
	 * @return The XOR of two byte arrays
	 */
	private static byte[] xor(byte[] a, byte[] b) {
		int len;
		boolean useA = false;
		
		if (a.length < b.length) {
			len = a.length;
		}
		else {
			len = b.length;
			useA = true;
		}
		
		byte[] output;
		if (useA) {
			output = a.clone();
		}
		else {
			output = b.clone();
		}
		
		int val;
		for (int i = 0; i < len; i++) {
			val = a[i] ^ b[i];
			output[i] = (byte) val;
		}
		
		return output;
	}
	
	/**
	 * Perform the ShiftRows step.
	 * @param state The byte array of the state, a 4x4 array.
	 * @return Shifted state array.
	 */
	public static byte[][] shiftRows(byte[][] state) {
		for (int i = 0; i < 4; i++) {
			state[i] = leftCircsularShift(state[i], i);
		}
		
		return state;
	}
	
	/**
	 * Perform the inverse of the ShiftRows step.
	 * @param state The shifted byte array of the state, a 4x4 array.
	 * @return Original state array.
	 * @see {@link #shiftRows(String[][])}
	 */
	public static byte[][] shiftRowsInv(byte[][] state) {
		for (int i = 0; i < 4; i++) {
			state[i] = leftCircsularShift(state[i], 4-i);
		}
		
		return state;
	}
	
	/**
	 * Perform the MixColumn step on the state array, a 4x4 byte
	 * array.
	 * @param state The state array.
	 * @return Mixed state array.
	 */
	public static byte[][] mixColumns(byte[][] state) {
		byte[] temp = new byte[4];
		
		for (int i = 0; i < 4; i++) {
			temp = RijndaelUtils.mixColumns(state[0][i], state[1][i], state[2][i], state[3][i]);
			for (int j = 0; j < 4; j++) {
				state[j][i] = temp[j];
			}
		}
		
		return state;
	}
	
	/**
	 * Perform the inverse of the MixColumn step on the state 
	 * array, a 4x4 byte array.
	 * @param state The mixed state array.
	 * @return Original state array.
	 * @see {@link #mixColumns(byte[][])}
	 */
	public static byte[][] mixColumnsInv(byte[][] state) {
		byte[] temp = new byte[4];
		
		for (int i = 0; i < 4; i++) {
			temp = RijndaelUtils.mixColumnsInv(state[0][i], state[1][i], state[2][i], state[3][i]);
			for (int j = 0; j < 4; j++) {
				state[j][i] = temp[j];
			}
		}
		
		return state;
	}
	
	/**
	 * Add the round key to the flattened state array. (we call this
	 * a chunk).
	 * @param chunk The flattened state byte array.
	 * @param roundKey The round key byte array.
	 * @return {@code chunk ^ roundKey}
	 */
	private static byte[] addRoundKey(byte[] chunk, byte[] roundKey) {
		return xor(chunk, roundKey);
	}
	
	/**
	 * Flatten a 2D byte array. we use this to convert state array to chunk
	 * array. (see {@link #mixColumns(byte[][])}
	 * @param input state byte array.
	 * @return chunk byte array.
	 */
	public static byte[] flatten(byte[][] input) {
		int len1, len2;
		len1 = input.length;
		len2 = input[0].length;
		
		byte[] output = new byte[len1 * len2];
		for (int i = 0; i < len1; i++) {
			for (int j = 0; j < len2; j++) {
				output[i*len2 + j] = input[i][j];
			}
		}
		
		return output;
	}
	
	/**
	 * Convert a byte array (usually the roundKey) to an array of words.
	 * Each words is {@code wordLen} bytes and the output is a 2D array of size 
	 * {@code input.length/wordLen x wordLen}, usually {@code wordLen=4}. 
	 * @param input The original byte array.
	 * @param wordLen The length of words in bytes. 
	 * @return The word array.
	 */
	private static byte[][] getWords(byte[] input, int wordLen) {
		int len = input.length;
		
		byte[][] words = new byte[len/wordLen][wordLen]; 
		for (int i = 0; i < len/wordLen; i++) {
			byte[] temp = new byte[wordLen];
			for (int j = 0; j < wordLen; j++) {
				temp[j] = input[wordLen*i + j];
			}
			words[i] = temp;
		}
		
		return words;
	}
	
	/**
	 * This utility convert a flat byte array into a column major 4 by 4
	 * 2D byte array, thus expects the length of the input to be 4, the depth is
	 * assumed to be 4 but not checked.
	 * @param input A flat byte array.
	 * @return Column major 2D byte array of the input array.
	 */
	public static byte[][] toColumnMajor(byte[] input) {
		int len = input.length;
		if (len != BLOCK_LENGTH_BYTE) {
			throw new RuntimeException("The input is not padded to BLOCK_LENGTH");
		}
		
		byte[][] output = new byte[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				output[j][i] = input[i*4 + j];
			}
		}
		
		return output;
	}
	
	/**
	 * This utility inverts the effects of {@link #toColumnMajor(byte[])}.
	 * @param input 2D column major byte array.
	 * @return Flat byte array.
	 */
	public static byte[] fromColumnMajor(byte[][] input) {
		byte[] output = new byte[BLOCK_LENGTH_BYTE];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				output[i*4 + j] = input[j][i];
			}
		}
		
		return output;
	}
	
	/**
	 * Perform the G function used to create the first word of the expanded
	 * key from the previous key.
	 * @param input The previous round byte array key.
	 * @param round The number of the key expansion round, starts from
	 * 0 all the way to {@code NUMBER_OF_KEY_EXPANSION_ROUNDS-1}.
	 * @return The byte array of the first word in the expanded key.
	 */
	private static byte[] gFunc(final byte[] input, final int round) {
		if (input.length != 4) {
			throw new RuntimeException("The input array to gFunc can only be of length 4");
		}
		byte[] output = leftCircsularShift(input, 1);
		output = subBytesEnc(output);
		output = xor(output, rCon[round]);
		
		return output;
	}
	
	/**
	 * Given the previous round key, expand it to create the next round
	 * key.
	 * @param key Byte array of length {@code KEY_LEN_BYTE} 
	 * @param round The particular key expansion round number, 
	 * starts from 0 all the way to {@code NUMBER_OF_KEY_EXPANSION_ROUNDS-1}.
	 * @return The expanded key.
	 */
	private static byte[] expandKey(final byte[] key, int round) {
		byte[][] keyWords = getWords(key, 4);
		byte[][] expandedKeyWords = new byte[KEY_LEN_WORDS][4];
		expandedKeyWords[0] = xor(keyWords[0], gFunc(keyWords[KEY_LEN_WORDS-1], round));
		
		for (int i = 1; i < KEY_LEN_WORDS; i++) {
			if (i != 4) {
				expandedKeyWords[i] = xor(expandedKeyWords[i-1], keyWords[i]);
			}				
			else {
				expandedKeyWords[i] = xor(subBytesEnc(expandedKeyWords[i-1]), keyWords[i]);
			}
		}
		
		return flatten(expandedKeyWords);
	}
	
	/**
	 * Perform the whole key expansion algorithm.
	 * @param key The first key in byte array.
	 * @return The expanded key including the original key as a byte array.
	 */
	public static byte[][] keyExpansion(final byte[] key) {
		byte[] expandedKeys = new byte[(NUMBER_OF_KEY_EXPANSION_ROUNDS+1) * (KEY_LEN_BYTE)];
		System.arraycopy(key, 0, expandedKeys, 0, KEY_LEN_BYTE);
		
		byte[] keyWordsFlat = key.clone();
		for (int round = 0; round < NUMBER_OF_KEY_EXPANSION_ROUNDS; round++) {
			keyWordsFlat = expandKey(keyWordsFlat, round);
			System.arraycopy(keyWordsFlat, 0, expandedKeys, (round+1) * KEY_LEN_BYTE, KEY_LEN_BYTE);
		}
		
		byte[] roundKeysFlat = new byte[BLOCK_LENGTH_BYTE * (NUMBER_OF_ROUNDS+1)];
		System.arraycopy(expandedKeys, 0, roundKeysFlat, 0, roundKeysFlat.length);
		
		return getWords(roundKeysFlat, BLOCK_LENGTH_BYTE);
	}
	
	/**
	 * Encrypt a chunk with a given round key.
	 * @param chunk The state array in chunk form.
	 * @param roundKey The round key
	 * @return The new state array in chunk form
	 */
	public static byte[] intermediateRoundEnc(byte[] chunk, final byte[] roundKey) {
		byte[] stateFlat = new byte[BLOCK_LENGTH_BYTE];
		System.arraycopy(chunk, 0, stateFlat, 0, BLOCK_LENGTH_BYTE);
		
		byte[][] stateInColumnMajor;
		
		stateFlat = subBytesEnc(chunk);
		stateInColumnMajor = toColumnMajor(stateFlat);
		stateInColumnMajor = shiftRows(stateInColumnMajor);
		stateInColumnMajor = mixColumns(stateInColumnMajor);
		stateFlat = fromColumnMajor(stateInColumnMajor);
		stateFlat = addRoundKey(stateFlat, roundKey);
		
		return stateFlat;
	}
	
	/**
	 * The initial round of encryption essentially only whitens the input with
	 * the starting key. 
	 * @param chunk Flat initial state array (the IV).
	 * @param startingKey The byte array of the starting key (only a block length of
	 * it's elements of course).
	 * @return whitened initial state array.
	 */
	public static byte[] initialRound(byte[] chunk, final byte[] startingKey) {
		return addRoundKey(chunk, startingKey);
	}
	
	/**
	 * Final encryption round, omits the MixColumn step from the intermediate
	 * rounds.
	 * @param chunk Flat state array.
	 * @param finalKey Byte array key of the final round.
	 * @return Final state array.
	 */
	public static byte[] finalRoundEnc(byte[] chunk, final byte[] finalKey) {
		byte[] stateFlat = new byte[BLOCK_LENGTH_BYTE];
		System.arraycopy(chunk, 0, stateFlat, 0, BLOCK_LENGTH_BYTE);
		
		byte[][] stateInColumnMajor;
		
		stateFlat = subBytesEnc(stateFlat);
		stateInColumnMajor = toColumnMajor(stateFlat);
		stateInColumnMajor = shiftRows(stateInColumnMajor);
		stateFlat = fromColumnMajor(stateInColumnMajor);
		stateFlat = addRoundKey(stateFlat, finalKey);
		
		return stateFlat;
	}
	
	/**
	 * Decrypt a chunk with a given round key.
	 * @param chunk The state array in chunk form.
	 * @param roundKey The round key
	 * @return The new state array in chunk form
	 */
	public static byte[] intermediateRoundDec(byte[] chunk, final byte[] roundKey) {
		byte[] stateFlat = new byte[BLOCK_LENGTH_BYTE];
		System.arraycopy(chunk, 0, stateFlat, 0, BLOCK_LENGTH_BYTE);
		
		byte[][] stateInColumnMajor;
		stateInColumnMajor = toColumnMajor(stateFlat);
		stateInColumnMajor = shiftRowsInv(stateInColumnMajor);
		stateFlat = fromColumnMajor(stateInColumnMajor);
		stateFlat = subBytesDec(stateFlat);
		stateFlat = addRoundKey(stateFlat, roundKey);
		stateInColumnMajor = toColumnMajor(stateFlat);
		stateInColumnMajor = mixColumnsInv(stateInColumnMajor);
		stateFlat = fromColumnMajor(stateInColumnMajor);
		
		return stateFlat;
	}
	
	/**
	 * Final decryption round, omits the InvMixColumn step from the intermediate
	 * rounds.
	 * @param chunk Flat state array.
	 * @param finalKey Byte array key of the final round.
	 * @return Final state array.
	 */
	public static byte[] finalRoundDec(byte[] chunk, final byte[] finalKey) {
		byte[] stateFlat = new byte[BLOCK_LENGTH_BYTE];
		System.arraycopy(chunk, 0, stateFlat, 0, BLOCK_LENGTH_BYTE);
		
		byte[][] stateInColumnMajor;
		
		stateInColumnMajor = toColumnMajor(stateFlat);
		stateInColumnMajor = shiftRowsInv(stateInColumnMajor);
		stateFlat = fromColumnMajor(stateInColumnMajor);
		stateFlat = subBytesDec(stateFlat);
		stateFlat = addRoundKey(stateFlat, finalKey);
		
		return stateFlat;
	}
	
	/**
	 * Fully encrypt a single chunk of message (i.e. a message of length 
	 * {@code BLOCK_LENGTH_BYTE}).
	 * @param chunk The chunk to encrypt.
	 * @param expandedKeys The output of key expansion algorithm.
	 * @return Encrypted chunk.
	 */
	public static byte[] chunkEnc(byte[] chunk, final byte[][] expandedKeys) {
		byte[] startingKey = new byte[BLOCK_LENGTH_BYTE];
		byte[][] roundKeys = new byte[NUMBER_OF_ROUNDS - 1][BLOCK_LENGTH_BYTE];
		byte[] finalKey = new byte[BLOCK_LENGTH_BYTE];
		
		startingKey = expandedKeys[0];
		for (int i = 0; i < NUMBER_OF_ROUNDS-1; i++) {
			roundKeys[i] = expandedKeys[i+1]; 
		}
		finalKey = expandedKeys[NUMBER_OF_ROUNDS];
		
		byte[] state;
		
		state = initialRound(chunk, startingKey);
		for (int round = 0; round < NUMBER_OF_ROUNDS-1; round++) {
			state = intermediateRoundEnc(state, roundKeys[round]);
		}
		state = finalRoundEnc(state, finalKey);
		
		return state;
	}
	
	/**
	 * Fully decrypt a single chunk of message (i.e. a message of length 
	 * {@code BLOCK_LENGTH_BYTE}).
	 * @param chunk The encrypted chunk.
	 * @param expandedKeys The output of key expansion algorithm.
	 * @return Decrypted chunk.
	 */
	public static byte[] chunkDec(byte[] chunk, final byte[][] expandedKeys) {
		byte[] startingKey = new byte[BLOCK_LENGTH_BYTE];
		byte[][] roundKeys = new byte[NUMBER_OF_ROUNDS - 1][BLOCK_LENGTH_BYTE];
		byte[] finalKey = new byte[BLOCK_LENGTH_BYTE];
		
		finalKey = expandedKeys[0];
		for (int i = 0; i < NUMBER_OF_ROUNDS-1; i++) {
			roundKeys[i] = expandedKeys[(NUMBER_OF_ROUNDS-1) - i];
		}
		startingKey = expandedKeys[NUMBER_OF_ROUNDS];
		
		byte[] state;
		
		state = initialRound(chunk, startingKey);
		for (int round = 0; round < NUMBER_OF_ROUNDS-1; round++) {
			state = intermediateRoundDec(state, roundKeys[round]);
		}
		state = finalRoundDec(state, finalKey);
		
		return state;
	}
	
	/**
	 * Encode a byte array.
	 * @param message Byte array of the message.
	 * @param iv The initial vector as a byte array.
	 * @param key The initial key as a byte array.
	 * @return Encoded message as byte array.
	 */
	public static byte[] enc(byte[] message, byte[] iv, final byte[] key) {		
		if (key.length != KEY_LEN_BYTE) {
			throw new RuntimeException(
					String.format("The initial key can be only of length %d bytes.", KEY_LEN_BYTE)
			);
		}
		
		if (iv.length != BLOCK_LENGTH_BYTE) {
			throw new RuntimeException(
					String.format("The initial value can be only of length %d bytes.", BLOCK_LENGTH_BYTE)
			);	
		}
		
		byte[] messagePadded = pkcs7Padding(message);
		final byte[][] roundKeys = keyExpansion(key);

		byte[][] chunks = getWords(messagePadded, BLOCK_LENGTH_BYTE);
		byte[][] chunksEnc = chunks.clone();
		
		for (int i = 0; i < chunks.length; i++) {
			if (i == 0) {
				chunksEnc[i] = chunkEnc(xor(chunks[i], iv), roundKeys);
			}
			else {
				chunksEnc[i] = chunkEnc(xor(chunks[i], chunksEnc[i-1]), roundKeys);
			}
		}
		
		return flatten(chunksEnc);
	}
	
	
	/**
	 * Encode a hex string message with a given hex key and return it as
	 * a hex string.
	 * @param messageHex Message as a hex string.
	 * @param ivHex The iv as a hex string.
	 * @param keyHex Key as a hex string.
	 * @return The encrypted message as a hex string.
	 */
	public static String encHex(String messageHex, String ivHex, final String keyHex) {
		byte[] message = hexToBytes(messageHex);
		byte[] iv = hexToBytes(ivHex);
		byte[] key = hexToBytes(keyHex);
		byte[] messageEncStr = enc(message, iv, key);
		return bytesToHex(messageEncStr);
	}

	/**
	 * Decode a byte array of encoded message with a given IV and key.
	 * @param messageEnc The encoded byte array.
	 * @param iv IV as byte array.
	 * @param key Key as byte array.
	 * @return The decoded byte array.
	 */
	public static byte[] dec(byte[] messageEnc, byte[] iv, final byte[] key) {
		if (key.length != KEY_LEN_BYTE) {
			throw new RuntimeException(
					String.format("The initial key can be only of length %d bytes.", KEY_LEN_BYTE)
			);
		}
		
		if (iv.length != BLOCK_LENGTH_BYTE) {
			throw new RuntimeException(
					String.format("The initial value can be only of length %d bytes.", BLOCK_LENGTH_BYTE)
			);	
		}
		
		final byte[][] roundKeys = keyExpansion(key);
		
		byte[][] chunksEnc = getWords(messageEnc, BLOCK_LENGTH_BYTE);
		byte[][] chunks = chunksEnc.clone();
		
		for (int i = 0; i < chunks.length; i++) {
			chunks[i] = chunkDec(chunksEnc[i], roundKeys);
		}
		
		for (int i = chunks.length - 1; i >= 0; i--) {
			if (i == 0) {
				chunks[i] = xor(iv, chunks[i]);
			}
			else {
				chunks[i] = xor(chunksEnc[i-1], chunks[i]);
			}
		}
		
		return pkcs7Unpadding(flatten(chunks));
	}
	
	/**
	 * Decode a hex string message with a given hex key and return it as
	 * a hex string.
	 * @param messageEncHex Encoded message as a hex string.
	 * @param ivHex The iv as a hex string.
	 * @param keyHex Key as a hex string.
	 * @return The decrypted message as a hex string.
	 */
	public static String decHex(String messageEncHex, String ivHex, final String keyHex) {
		byte[] messageEnc = hexToBytes(messageEncHex);
		byte[] iv = hexToBytes(ivHex);
		byte[] key = hexToBytes(keyHex);
		byte[] messageStr = dec(messageEnc, iv, key);
		return bytesToHex(messageStr);
	}
}
