package ir.sharif.messenger.utils;

import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.math.BigInteger;

public class BigNum {
	private String strVal;
	private int[] arrVal;
	private static final int NUMBER_OF_DIGITS = 9;
	private static final int BLOCK_WEIGHT = 1000000000;

	private static final BigNum ZERO = new BigNum("0");
	private static final BigNum ONE = new BigNum("1");
	private static final BigNum TWO = new BigNum("2");
	
	private static final long LONG_MASK = 0xffffffffL;
	
	public enum Comparison {
			SMALLER,
			EQUAL,
			LARGER
	};
	
	public BigNum(String strInput) {
		strVal = strInput;
		arrVal = BigNum.stringToArr(strInput);
	}
	
	public BigNum(int[] arrInput) {
		strVal = BigNum.arrToString(arrInput);
		arrVal = arrInput;
	}
	
	private static String arrToString(int[] arrInput) {
		String holder = "";
		int numberOfBlocks = arrInput.length;
		
		for (int i=1; i < numberOfBlocks; i++) {
			holder = holder + zeroPadLeft(arrInput[i]);
		}
		
		return Integer.toString(arrInput[0]) + holder;
	}
	
	private static int[] stringToArr(String strInput) {
		int strLen = strInput.length();
		int numberOfBlocks = BigNum.getNumberOfBlocksFromString(strInput);
		int[] holder = new int[numberOfBlocks];
		
		int stringIndexer = strLen;
		
		for (int i = numberOfBlocks-1; i > 0; i--) {
			String inputChunk = strInput.substring(stringIndexer-NUMBER_OF_DIGITS, stringIndexer); 
			holder[i] = Integer.valueOf(inputChunk);
			stringIndexer -= NUMBER_OF_DIGITS;
		}
		holder[0] = Integer.valueOf(strInput.substring(0, stringIndexer));
		
		return holder;
	}
	
	private static int getNumberOfBlocksFromString(String strVal) {
		int strLen = strVal.length();
		
		if (strLen % NUMBER_OF_DIGITS == 0) {
			return strLen / NUMBER_OF_DIGITS;
		}
		else {
			return strLen / NUMBER_OF_DIGITS + 1;
		}
	}
	
	// Utils
	private static String zeroPadLeft(int chunk) {
		if (chunk >= BLOCK_WEIGHT) {
			throw new NotAChunkException();
		}
		
		String asStr = Integer.toString(chunk);
		int len = asStr.length();
		
		int numberOfPaddingZeros = NUMBER_OF_DIGITS - len;
		for (int i=0; i<numberOfPaddingZeros; i++) {
			asStr = "0" + asStr;
		}
		
		return asStr;
	}
	
	private static BigNum shiftBlocks(BigNum a, int n) {
		if (n <= 0) {
			return a;
		}
		
		int[] aChunks = a.valueInChunks();
		int aLen = aChunks.length;
		int[] newChunks = new int[aLen + n];
		System.arraycopy(aChunks, 0, newChunks, 0, aLen);
		return new BigNum(newChunks);
	}
	
	private static BigNum shiftBlocks(String aStr, int n) {
		BigNum a = new BigNum(aStr);
		return shiftBlocks(a, n);
	}
	
	private static BigNum singleRightShift(BigNum a) {
		int[] aChunks = a.valueInChunks();
		int len = aChunks.length;
		
		int[] newChunks = new int[len];
		int currentChunk;
		boolean borrow = false;
		
		for (int i = 0; i < len; i++) {
			currentChunk = aChunks[i];
			newChunks[i] = currentChunk >>> 1;
			if (borrow) {
				newChunks[i] += 500000000;
			}
			
			if (currentChunk % 2 == 0) {
				borrow = false;
			}
			else {
				borrow = true;
			}
		}
		
		return new BigNum(newChunks);
	}
	
	public static BigNum rightShift(BigNum a, int n) {
		int[] aChunks = a.valueInChunks();
		int len = aChunks.length;
		
		if (len == 1) {
			return new BigNum(Integer.toString(aChunks[0] >> 1));
		}
		else if (n > 32 * len) {
			return ZERO;
		}
		
		BigNum holder = new BigNum(a.value());
		
		for (int i=0; i < n; i++) {
			holder = singleRightShift(holder);
		}
		
		return holder;
	}
	
	private static BigNum singleLeftShift(BigNum a) {
		int[] aChunks = a.valueInChunks();
		int len = aChunks.length;
		
		int[] newChunks = new int[len];
		int currentChunk;
		int val;
		boolean carry = false;
		
		for (int i = len - 1; i >= 0; i--) {
			currentChunk = aChunks[i];
			val = currentChunk << 1;
			
			if (carry) {
				++val;
			}
			
			if (val >= BLOCK_WEIGHT) {
				newChunks[i] = val - BLOCK_WEIGHT;
				carry = true;
			}
			else {
				newChunks[i] = val;
				carry = false;
			}
		}
		
		if (carry) {
			int[] chunksWithCarry = new int[len+1];
			for (int i = 0; i < len; i++) {
				chunksWithCarry[i+1] = newChunks[i];
			}
			chunksWithCarry[0] = 1;
			
			return new BigNum(chunksWithCarry);
		}
		
		return new BigNum(newChunks);
	}
	
	public static BigNum leftShift(BigNum a, int n) {
		if (a == ZERO) {
			return ZERO;
		}
		
		BigNum holder = new BigNum(a.value());
		for (int i = 0; i < n; i++) {
			holder = singleLeftShift(holder);
		}
		
		return holder;
	}
	
	public static BigNum powerOfTwo(int n) {
		if (n <= 255) {
			String pow = "-1";
			try {
				pow = Files.lines(Paths.get("resources//powers")).skip(n).findFirst().get();	
		    } 
		    catch (IOException e) {
		        e.printStackTrace();
		    }
			
			return new BigNum(pow);
		}
		else {
			int m = n - 255;
			return mul(powerOfTwo(m), powerOfTwo(255));
		}
	}
	
	public static int trailingZeros(BigNum a) throws NotImplementedException {
		int d = 0;
		BigNum q = new BigNum(a.value());
		
		while (!isOdd(q)) {
			++d;
			q = div(q, TWO);
		}
		
		return d;
	}
	
	private static boolean isOdd(BigNum a) {
		String aStr = a.value();
		char lastDigit = aStr.charAt(aStr.length()-1);
		if (lastDigit == '0' | lastDigit == '2' | lastDigit == '4' | lastDigit == '6' | lastDigit == '8') {
			return false;
		}
		else {
			return true;
		}
	}
	
	private static BigNum zeroPadRight(BigNum a) {
		String asStr = a.value();
		
		for (int i=0; i<NUMBER_OF_DIGITS; i++) {
			asStr = asStr + "0";
		}
		
		return new BigNum(asStr);
	}
	
	private static BigNum[] splitFromTheEnd(BigNum a) {
		BigNum[] holder = new BigNum[2];
		int[] aInChunks = a.valueInChunks();
		int lenA = aInChunks.length;
		
		int[] endChunk = {aInChunks[lenA-1]};
		int[] restChunks = new int[lenA-1];
		
		for (int i=0; i<lenA-1; i++) {
			restChunks[i] = aInChunks[i];
		}
		
		holder[0] = new BigNum(restChunks);
		holder[1] = new BigNum(endChunk);
		
		return holder;
	}
	
	// Compare
	public static Comparison compare(BigNum a, BigNum b) {
		int[] aChunks = a.valueInChunks();
		int[] bChunks = b.valueInChunks();
		
		int lenA = a.value().length();
		int lenB = b.value().length();
		
		if (lenA > lenB) {
			return Comparison.LARGER;
		}
		else if (lenA < lenB) {
			return Comparison.SMALLER;
		}
		else {
			int numberOfBlocks = getNumberOfBlocksFromString(a.value());
			
			for (int i=0; i < numberOfBlocks; i++) {
				if (aChunks[i] > bChunks[i]) {
					return Comparison.LARGER;
				}
				else if (aChunks[i] < bChunks[i]) {
					return Comparison.SMALLER;
				}
				else {
					continue;
				}
			}
			
			return Comparison.EQUAL;
		}
	}
	
	public static boolean isLarger(BigNum a, BigNum b) {
		Comparison result = compare(a, b);
		
		if (result == Comparison.LARGER) {
			return true;
		}
		return false;
	}
	
	public static boolean isSmaller(BigNum a, BigNum b) {
		Comparison result = compare(a, b);
		
		if (result == Comparison.SMALLER) {
			return true;
		}
		return false;
	}

	public static boolean isEqual(BigNum a, BigNum b) {
		Comparison result = compare(a, b);
		
		if (result == Comparison.EQUAL) {
			return true;
		}
		return false;
	}
	
	public static BigNum max(BigNum a, BigNum b) {
		if (isLarger(a, b)) {
			return a;
		}
		return b;
	}

	public static BigNum min(BigNum a, BigNum b) {
		if (isSmaller(a, b)) {
			return a;
		}
		return b;
	}
	
	public static int max(int a, int b ) {
		if (a > b) {
			return a;
		}
		return b;
	}
	
	public static int min(int a, int b) {
		if (a < b) {
			return a;
		}
		return b;
	}
	
	// Addition
	public static BigNum add(BigNum a, BigNum b) {
		int[] aChunks = a.valueInChunks();
		int[] bChunks = b.valueInChunks();
		
		int lenA = aChunks.length;
		int lenB = bChunks.length;
		int numberOfStages = max(lenA, lenB);
		int carry = 0;
		int chunk;
		
		String holder = "";
		
		int aIndexer = lenA-1;
		int bIndexer = lenB-1;
		
		for (int i=numberOfStages-1; i>=0; i--) {
			chunk = carry;
			
			if (aIndexer >= 0) {
				chunk += aChunks[aIndexer];
			}
			if (bIndexer >= 0) {
				chunk += bChunks[bIndexer];
			}
			
			if (chunk >= BLOCK_WEIGHT) {
				chunk -= BLOCK_WEIGHT;
				carry = 1;
			}
			else  {
				carry = 0;
			}
			
			if (i > 0) {
				holder = zeroPadLeft(chunk) + holder;
			}
			else {
				holder = Integer.toString(chunk) + holder;
			}
			
			aIndexer -= 1;
			bIndexer -= 1;
		}
		
		return new BigNum(holder);
	}
	
	// Subtraction
	public static BigNum sub(BigNum a, BigNum b) throws NotImplementedException {
		if (compare(a, b) == Comparison.SMALLER) {
			throw new NotImplementedException("Negative numbes are undefined yet");
		}
		
		int[] aChunks = a.valueInChunks();
		int[] bChunks = b.valueInChunks();
		
		int lenA = aChunks.length;
		int lenB = bChunks.length;
		int aIndexer, bIndexer;
		int currentChunk = -1;
		int lastChunk = -1;
		
		String holder = "";
		
		for (int stage=0; stage < lenA; stage++) {
			aIndexer = stage;
			bIndexer = stage - (lenA - lenB);
			lastChunk = currentChunk;
			
			if (bIndexer < 0) {
				currentChunk = aChunks[aIndexer];
			}
			else {
				currentChunk = aChunks[aIndexer] - bChunks[bIndexer];
			}
			
			if (currentChunk < 0) {
				lastChunk -= 1;
				currentChunk += BLOCK_WEIGHT;
			}
			
			if (lastChunk >= 0) {
				holder = holder + zeroPadLeft(lastChunk);
			}
		}
		
		holder = holder + zeroPadLeft(currentChunk);
		return new BigNum(holder);
	}
	
	// Multiplication
	public static BigNum mul(BigNum a, BigNum b) {
		int[] aChunks = a.valueInChunks();
		int[] bChunks = b.valueInChunks();
		
		int lenA = aChunks.length;
		int lenB = bChunks.length;
		
		if (lenA == 1 && lenB == 1) {
			long result = ((long) aChunks[0]) * bChunks[0];
			return new BigNum(Long.toString(result));
		}
		else {
			if (lenA == 1 && lenB > 1) {
				BigNum[] ab = splitFromTheEnd(b);
				BigNum A = ab[0];
				BigNum B = ab[1];
				BigNum C = a;
				
				return add(zeroPadRight(mul(A, C)), mul(B, C));
			}
			else if (lenB == 1 && lenA > 1) {
				BigNum[] ab = splitFromTheEnd(a);
				BigNum A = ab[0];
				BigNum B = ab[1];
				BigNum C = b;

				return add(zeroPadRight(mul(A, C)), mul(B, C));
			}
			else {
				BigNum[] ab = splitFromTheEnd(a);
				BigNum[] cd = splitFromTheEnd(b);
				
				BigNum A = ab[0];
				BigNum B = ab[1];
				BigNum C = cd[0];
				BigNum D = cd[1];
				
				BigNum doublePad = mul(A, C);
				BigNum singlePad = add(mul(A, D), mul(B, C));
				BigNum noPad = mul(B, D);
				
				return add(add(noPad, zeroPadRight(singlePad)), zeroPadRight(zeroPadRight(doublePad)));
			}
		}
	}
	
//	public static BigNum baseCaseMul(BigNum a, BigNum b) {
//		int[] aChunks = a.valueInChunks();
//		int[] bChunks = b.valueInChunks();
//		
//		int lenA = aChunks.length;
//		int lenB = bChunks.length; 
//		
//		if (lenA == 1 && lenB == 1) {
//			long result = ((long) aChunks[0]) * bChunks[0];
//			return new BigNum(Long.toString(result));
//		}
//		else {
//			long[] polyCoeffs = new long[lenA + lenB];
//			
//			for (int i = 0; i < lenA; i++) {
//				for (int j = 0; j < lenB; j++) {
//					polyCoeffs[i+j] += ((long) aChunks[i]) * bChunks[j];
//				}
//			}
//			
//			BigNum result = ZERO;
//			for (int i = 0; i < lenA + lenB; i++) {
//				result = add(result, shiftBlocks(Long.toString(polyCoeffs[i]), lenA + lenB - 2 - i));
//			}
//			
//			return result;
//		}
//	}
	
	public static BigNum baseCaseMul(BigNum a, BigNum b) {
		int[] aChunks = a.valueInChunks();
		int[] bChunks = b.valueInChunks();
		
		int lenA = aChunks.length;
		int lenB = bChunks.length; 
		
		if (lenA == 1 && lenB == 1) {
			long result = ((long) aChunks[0]) * bChunks[0];
			return new BigNum(Long.toString(result));
		}
		else {
			BigNum product = ZERO;
			for (int i = 0; i < lenB; i++) {
				product = add(shiftBlocks(mulByInt(a, bChunks[i]), i), product);
			}
			
			return product;
		}
	}
	
	public static BigNum mulByInt(BigNum a, int b) {
		int[] aChunks = a.valueInChunks();
		int aLen = aChunks.length;
		int[] m = new int[aLen + 1];
		long bL = b & LONG_MASK;
		long carry = 0L;
		int mIndexer = m.length - 1;
		long product;
		
		for (int i = aLen - 1; i >= 0; i--) {
			product = (aChunks[i] & LONG_MASK) * bL + carry;
			m[mIndexer--] = (int) product;
			carry = product >>> 32;
		}
		
		if (carry == 0L) {
			int[] mSmall = new int[aLen];
			System.arraycopy(m, 1, mSmall, 0, aLen);
			
			return new BigNum(mSmall);
		}
		else {
			m[mIndexer] = (int) carry;
		}
		
		return new BigNum(m);
	}
	
//	public static BigNum karatsubaMul( BigNum a, BigNum b) {
//		int[] aChunks = a.valueInChunks();
//		int[] bChunks = b.valueInChunks();
//		
//		int lenA = aChunks.length;
//		int lenB = bChunks.length;
//		
//		
//	}
	
	// Remainder
	public static BigNum rem(BigNum a, BigNum b) throws NotImplementedException {
		Comparison res = compare(a, b);
		
		if (res == Comparison.SMALLER) {
			return a;
		}
		else if (res == Comparison.EQUAL) {
			return ZERO;
		}
		else {
			int lenA = a.value().length();
			int lenB = b.value().length();
			
			if (lenA == lenB) {
				try {
					while(true ) {
						a = sub(a, b);
					}
				}
				catch (NotImplementedException e) {
					return a;
				}
			}
			else {
				BigNum newB = b;
				BigNum lastB = b;
				
				while (res == Comparison.LARGER) {
					lastB = newB;
					newB = new BigNum(newB.value() + "0");
					res = compare(a, newB);
				}
				
				return rem(new BigNum(sub(a, lastB).value()), b);
			}
		}
	}
	
	// Integer division
	public static BigNum div(BigNum a, BigNum b) throws NotImplementedException {
		Comparison res = compare(a, b);
		
		if (res == Comparison.SMALLER) {
			return ZERO;
		}
		else if (res == Comparison.EQUAL) {
			return ONE;
		}
		else {
			int lenA = a.value().length();
			int lenB = b.value().length();
			
			if (lenA == lenB) {
				int q = 0;
				try {
					while(true) {
						a = sub(a, b);
						++q;
					}
				}
				catch (NotImplementedException e) {
					return new BigNum(Integer.toString(q));
				}
			}
			else {
				BigNum newB = b;
				BigNum lastB = b;
				int i = 0;
				
				while (res == Comparison.LARGER) {
					lastB = newB;
					newB = new BigNum(newB.value() + "0");
					res = compare(a, newB);
					++i;
				}
				
				int j = 0;
				try {
					while(true) {
						a = sub(a, lastB);
						++j;
					}
				}
				catch (NotImplementedException e) {
					String q = "";
					q += Integer.toString(j);
					for (int countTens = 0; countTens < i-1; countTens++) {
						q += "0";
					}
					
					return div(new BigNum(a.value()), b, new BigNum(q));
				}
			}
		}
	}
	
	private static BigNum div(BigNum a, BigNum b, BigNum q) throws NotImplementedException {
		return BigNum.add(q, div(a, b));
	}

	// GCD
	public static BigNum gcd(BigNum a, BigNum b) throws NotImplementedException {
		if (a.value() == "0")
			return b;
		else if (b.value() == "0")
			return a;
		
		BigNum bigger = a;
		BigNum smaller = b;
		
		if (isEqual(a, b)) {
			return a;
		}
		else {
			if (isLarger(a, b)) {
				bigger = a;
				smaller = b;
			}
			
			if (isSmaller(a, b)) {
				smaller = a;
				bigger = b;
			}
		}
		
		BigNum temp = smaller;
		smaller = rem(bigger, smaller);
		bigger = temp;
		
		return gcd(bigger, new BigNum(smaller.value()));
	}
	
	// Decimal to binary
//	public String binary(BigNum a) {
//		int n = 0;
//		BigNum d = new BigNum("1");
//		BigNum two = new BigNum("2");
//		
//		if (a.value() == "1") {
//			return "1";
//		}
//		
//		while (isLarger(a, d)) {
//			++n;
//			d = mul(a, two);
//		}
//	}
	
	// Power
	
	public static BigNum pow(BigNum a, BigNum e) throws NotImplementedException {
		if (a.value() == "1") {
			return ONE;
		}
		else if (a.value() == "0") {
			return ZERO;
		}
		
		if (e.value() == "0") {
			return ONE;
		}
		
		if (isSmaller(e, new BigNum("1000000000"))) {
			return pow(a, e.valueInChunks()[0]);
		}
		else {
			if (isOdd(e)) {
				return mul(a, pow(mul(a, a), div(sub(e, ONE), TWO)));
			}
			else {
				return pow(mul(a, a), div(e, TWO));
			}
		}
	}
	
	private static BigNum pow(BigNum a, int e) throws NotImplementedException {
		if (a.value() == "1") {
			return ONE;
		}
		else if (a.value() == "0") {
			return ZERO;
		}
		
		if (e == 0) {
			return ONE;
		}
		
		if (e == 1) {
			return a;
		}
		
		if (e%2 == 1) {
			return mul(a, pow(mul(a, a), (e-1)/2));
		}
		else {
			return pow(mul(a, a), e/2);
		}
	}
	
	private static BigNum modPow(BigNum a, BigNum e, BigNum N, boolean caller) throws NotImplementedException {
		if (a == ONE) {
			return ONE;
		}
		else if (a == ZERO) {
			return ZERO;
		}
		
		if (e == ZERO) {
			return ONE;
		}
		
		if (isSmaller(e, new BigNum("1000000000"))) {
			return modPow(a, Integer.valueOf(e.value()), N);
		}
		
		BigNum aMod = rem(a, N);
		
		if (aMod == ONE) {
			return ONE;
		}
		else if (aMod == ZERO) {
			return ZERO;
		}
		
		if (e == ZERO) {
			return ONE;
		}
		
		if (isOdd(e)) {
			return mul(a, modPow(mul(aMod, aMod), rightShift(e, 1), N, false));
		}
		else {
			return modPow(mul(aMod, aMod), rightShift(e, 1), N, false);
		}
	}
	
	private static BigNum modPow(BigNum a, int e, BigNum N, boolean caller) throws NotImplementedException {
		BigNum aMod = rem(a, N);
		
		if (aMod == ONE) {
			return ONE;
		}
		else if (aMod == ZERO) {
			return ZERO;
		}
		
		if (e == 0) {
			return ONE;
		}
		
		if (e == 1) {
			return a;
		}
		
		if (e%2 == 1) {
			return mul(aMod, modPow(mul(aMod, aMod), e >> 1, N, false));
		}
		else {
			return modPow(mul(aMod, aMod), e >> 1, N, false);
		}
	}
	
	private static BigNum modPow(BigNum a, int e, BigNum N) throws NotImplementedException {
		return rem(modPow(a, e, N, false), N);
	}
	
	public static BigNum modPow(BigNum a, BigNum e, BigNum N) throws NotImplementedException {
		return rem(modPow(a, e, N, false), N);
	}
	
//	// Miller-Rabin test for primes
//	
//	public static boolean millerRabin(BigNum p) {
//		RC4 gen = new RC4("Miller-Rabin".getBytes());
//		BigNum one = new BigNum("1");
//		BigNum pMinusOne = sub(p, one);
//	}
	
//	public static bool
	
	public String value() {
		int lenStr = strVal.length();
		for (int i=0; i<lenStr; ++i) {
			if (strVal.charAt(i) == '0') {
				continue;
			}
			return strVal.substring(i);
		}
		
		return "0";
	}
	
	public int[] valueInChunks() {
		return arrVal;
	}
}