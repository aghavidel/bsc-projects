package ir.sharif.messenger.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import java.math.BigInteger;

public class BigNumTest {

	@Test
	public void constructorTest() {
		String bigNumberStr1 = "123456789123456789123456789";
		String bigNumberStr2 = "456789123456789123456789";
		String smallNumberStr = "123456";
		String numberWithMiddleZerosStr = "123000000000000000005465465";
		
		int[] bigNumberArr1 = {123456789, 123456789, 123456789};
		int[] bigNumberArr2 = {456789, 123456789, 123456789};
		int[] smallNumberArr = {123456};
		int[] numberWithMiddleZerosArr = {123000000, 0, 5465465};
		
		BigNum numberFromArray1 = new BigNum(bigNumberArr1);
		BigNum numberFromArray2 = new BigNum(bigNumberArr2);
		
		BigNum numberFromString1 = new BigNum(bigNumberStr1);
		BigNum numberFromString2 = new BigNum(bigNumberStr2);
		
		BigNum smallFromString = new BigNum(smallNumberStr);
		BigNum smallFromArray = new BigNum(smallNumberArr);
		
		BigNum numberWithMiddleZerosFromArray = new BigNum(numberWithMiddleZerosArr);
		BigNum numberWithMiddleZerosFromString = new BigNum(numberWithMiddleZerosStr);
		
		assertEquals(bigNumberStr1, numberFromArray1.value());
		assertEquals(bigNumberStr2, numberFromArray2.value());
		assertEquals(smallNumberStr, smallFromArray.value());
		assertEquals(numberWithMiddleZerosStr, numberWithMiddleZerosFromArray.value());
		
		assertArrayEquals(bigNumberArr1, numberFromString1.valueInChunks());
		assertArrayEquals(bigNumberArr2, numberFromString2.valueInChunks());
		assertArrayEquals(smallNumberArr, smallFromString.valueInChunks());
		assertArrayEquals(numberWithMiddleZerosArr, numberWithMiddleZerosFromString.valueInChunks());
	}
	
	@Test
	public void comparisonTest() {
		String num1 = "562985651";
		String num2 = "123456789456";
		String num3 = num1;
		String num4 = "987654123459498798523124654";
		String num5 = "987654121478955921234598756";	
		String num7 = "939849174703040427227081680";
		String num8 = "409943108258342299629508520";
		
		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		BigNum a5 = new BigNum(num5);
		BigNum a7 = new BigNum(num7);
		BigNum a8 = new BigNum(num8);
		
		assertEquals(false, BigNum.isLarger(a1, a2));
		assertEquals(false, BigNum.isSmaller(a4, a5));
		assertEquals(false, BigNum.isLarger(a3, a5));
		assertEquals(true, BigNum.isEqual(a1, a3));
		assertEquals(false, BigNum.isEqual(a4, a5));
		assertEquals(false, BigNum.isLarger(a8, a7));
	}
	
	@Test
	public void additionTest() throws NotAChunkException{
		String num1 = "5629";
		String num2 = "123456789";
		String num3 = "59876532156789951235915599875";
		String num4 = "12345921321321245957";
		String num5 = "6959999999395959592121324459872321325498798";
		String num6 = "123456789123456";

		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		BigNum a5 = new BigNum(num5);
		BigNum a6 = new BigNum(num6);
		
		assertEquals("123462418", BigNum.add(a1, a2).value());
		assertEquals("59876532156789951236039056664", BigNum.add(a2, a3).value());
		assertEquals("59876532169135872557236845832", BigNum.add(a3, a4).value());
		assertEquals("6959999999396019468653481249823557241098673", BigNum.add(a3, a5).value());
		assertEquals("123456912580245", BigNum.add(a6, a2).value());
	}
	
	@Test
	public void multiplicationTest() throws NotAChunkException {
		String num1 = "5629";
		String num2 = "1234";
		String num3 = "59876532156789951235915599875";
		String num4 = "12345921321321245957";
		String num5 = "6959999999395959592121324459872321325498798";
		String num6 = "0";
		String num7 = "9999999999999999999999999999999999999999999999999999999";
		String num8 = "9999999999999999999999999999999999999999999999999999";
		
		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		BigNum a5 = new BigNum(num5);
		BigNum a6 = new BigNum(num6);
		BigNum a7 = new BigNum(num7);
		BigNum a8 = new BigNum(num8);
		
//		assertEquals("6946186", BigNum.mul(a1, a2).value());
//		assertEquals("337044999510570635506968911696375", BigNum.mul(a1, a3).value());
//		assertEquals("739230955001290267756589967386032474528073455375", BigNum.mul(a4, a3).value());
//		assertEquals("0", BigNum.mul(a5, a6).value());
		
//		assertEquals("6946186", BigNum.baseCaseMul(a1, a2).value());
//		assertEquals("337044999510570635506968911696375", BigNum.baseCaseMul(a1, a3).value());
//		assertEquals("739230955001290267756589967386032474528073455375", BigNum.baseCaseMul(a4, a3).value());
//		assertEquals(
//			"99999999999999999999999999999999999999999999999999989990000000000000000000000000000000000000000000000000001", 
//			BigNum.baseCaseMul(a7, a8).value());
//		assertEquals("0", BigNum.baseCaseMul(a5, a6).value());
		
//		BigInteger test1 = new BigInteger(num3);
//		BigInteger test2 = new BigInteger(num4);
//		
//		for (int i = 0; i < 10000; i++)
//		assertEquals(
//				"739230955001290267756589967386032474528073455375",
//				test1.multiply(test2).toString()
//				);
		
//		for (int i = 0; i < 10000; i++)
//			assertEquals(
//				"739230955001290267756589967386032474528073455375", 
//				BigNum.baseCaseMul(a4, a3).value()
//				);
	}
	
	@Test
	public void subtractionTest() throws NotImplementedException {
		String num1 = "5629";
		String num2 = "1234";
		String num3 = "59876532156789951235915599875";
		String num4 = "12345921321321245957";
		String num5 = "6959999999395959592121324459872321325498798";
		String num6 = "0";
		String num7 = "5921321321245957456454678";
		String num8 = "1234000000000000000000000";
		
		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		BigNum a5 = new BigNum(num5);
		BigNum a6 = new BigNum(num6);
		BigNum a7 = new BigNum(num7);
		BigNum a8 = new BigNum(num8);
		
		assertEquals("4395", BigNum.sub(a1, a2).value());
		assertEquals("59876532156789951235915598641", BigNum.sub(a3, a2).value());
		assertEquals("59876532144444029914594353918", BigNum.sub(a3, a4).value());
		assertEquals("6959999999395899715589167669921085409898923", BigNum.sub(a5, a3).value());
		assertEquals("6959999999395959592121324459872321325498798", BigNum.sub(a5,  a6).value());
		assertEquals("4687321321245957456454678", BigNum.sub(a7, a8).value());
	}
	
	@Test
	public void remainderTest() throws NotImplementedException {
		String num1 = "56290";
		String num2 = "12340";
		String num3 = "59876532156789951235915599875";
		String num4 = "12345921321321245957456454678";
		String num5 = "6959999999395959592121324459872321325498798";
		String num6 = "958665859";	
		String num7 = "939849174703040427227081680";
		String num8 = "409943108258342299629508520";
		
		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		BigNum a5 = new BigNum(num5);
		BigNum a6 = new BigNum(num6);
		BigNum a7 = new BigNum(num7);
		BigNum a8 = new BigNum(num8);
		
		assertEquals("6930", BigNum.rem(a1, a2).value());
		assertEquals("10492846871504967406089781163", BigNum.rem(a3, a4).value());
		assertEquals("2018", BigNum.rem(a4, a2).value());
		assertEquals("14303897286793026376417643798", BigNum.rem(a5, a3).value());
		assertEquals("8538", BigNum.rem(a5, a2).value());
		assertEquals("119962958186355827968064640", BigNum.rem(a7, a8).value());
		assertEquals("496067322", BigNum.rem(a8, a6).value());
	}
	
	@Test
	public void gcdTest() throws NotImplementedException {
		String num1 = "56290";
		String num2 = "12340";
		String num3 = "59876532156789951235915599875";
		String num4 = "12345921321321245957456454678";
		String num5 = "6959999999395959592121324459872321325498798";
		String num6 = "958665859";	
		String num7 = "26372019823136566122346666808945000";
		String num8 = "3065462399733956442753916145106165204602690591120";
		String num9 = "18638113891032227147646883379486929543615010149240";
		String num10 = "2314301563904181629170915421040234978323082657395477494358960";
		
		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		BigNum a5 = new BigNum(num5);
		BigNum a6 = new BigNum(num6);
		BigNum a7 = new BigNum(num7);
		BigNum a8 = new BigNum(num8);
		BigNum a9 = new BigNum(num9);
		BigNum a10 = new BigNum(num10);
		
		assertEquals("10", BigNum.gcd(a1, a2).value());
		assertEquals("1", BigNum.gcd(a3, a4).value());
		assertEquals("1", BigNum.gcd(a5, a6).value());
		assertEquals("440440", BigNum.gcd(a8, a7).value());
		assertEquals("409943108258342299629508520", BigNum.gcd(a9, a10).value());
	}
	
	@Test
	public void divTest() throws NotImplementedException {
		String num1 = "65000000000";
		String num2 = "5000000000";
		String num3 = "59876532156789951235915599875";
		String num4 = "12345921321321245957456454678";
		String num5 = "6959999999395959592121324459872321325498798";
		String num6 = "958665859";	
		String num7 = "26372019823136566122346666808945000";
		String num8 = "3065462399733956442753916145106165204602690591120";
		String num9 = "18638113891032227147646883379486929543615010149240";
		String num10 = "2314301563904181629170915421040234978323082657395477494358960";

		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		BigNum a5 = new BigNum(num5);
		BigNum a6 = new BigNum(num6);
		BigNum a7 = new BigNum(num7);
		BigNum a8 = new BigNum(num8);
		BigNum a9 = new BigNum(num9);
		BigNum a10 = new BigNum(num10);
		
//		assertEquals(a1.value(), BigNum.div(BigNum.mul(a1, a2), a2).value());
//		assertEquals(a3.value(), BigNum.div(BigNum.mul(a3, a4), a4).value());
//		assertEquals(a5.value(), BigNum.div(BigNum.mul(a5, a6), a6).value());
//		assertEquals(a7.value(), BigNum.div(BigNum.mul(a7, a8), a8).value());
//		assertEquals(a9.value(), BigNum.div(BigNum.mul(a9, a10), a10).value());
//		assertEquals("2414085723589100536823138729341392951726136997432676", BigNum.div(a10, a6).value());
//		assertEquals("2414085723589100536823138729341392951726136997432676", BigNum.div(a10, a6).value());
//		assertEquals("2414085723589100536823138729341392951726136997432676", BigNum.div(a10, a6).value());
//		assertEquals("2414085723589100536823138729341392951726136997432676", BigNum.div(a10, a6).value());
//		assertEquals("2414085723589100536823138729341392951726136997432676", BigNum.div(a10, a6).value());
		
//		for (int i = 0; i < 1000; i++) {
//			assertEquals("2414085723589100536823138729341392951726136997432676", BigNum.div(a10, a6).value());
//		}
		
		BigInteger test1 = new BigInteger(num10);
		BigInteger test2 = new BigInteger(num6);
//		for (int i = 0; i < 1000; i++) {
//			assertEquals("2414085723589100536823138729341392951726136997432676", test1.divide(test2).toString());	
//		}
	}
	
	@Test
	public void powersOfTwoTest() throws NotAChunkException {
		assertEquals("1", BigNum.powerOfTwo(0).value());
		assertEquals("340282366920938463463374607431768211456", BigNum.powerOfTwo(128).value());
		assertEquals(
			"63657374260452690195888927762793067532858387302060507832379389042324415617604272068231168", 
			BigNum.powerOfTwo(295).value()
		);
		assertEquals(
			"1174271291386916613944740298394668513687841274454159935353645485766104512557304221731849499192384351515967488", 
			BigNum.powerOfTwo(359).value()
		);
	}
	
	@Test
	public void trailingZeros() throws NotImplementedException {
		String num1 = "65000000000";
		String num3 = "59876532156789951235915599875";
		String num5 = "6959999999395959592121324459872321325498798";
		String num10 = "2314301563904181629170915421040234978323082657395477494358960";
		
		BigNum a1 = new BigNum(num1);
		BigNum a3 = new BigNum(num3);
		BigNum a5 = new BigNum(num5);
		BigNum a10 = new BigNum(num10);

		assertEquals(9, BigNum.trailingZeros(a1));
		assertEquals(0, BigNum.trailingZeros(a3));
		assertEquals(1, BigNum.trailingZeros(a5));
		assertEquals(4, BigNum.trailingZeros(a10));
	}
	
	@Test
	public void powerTest() throws NotImplementedException {
		String num1 = "10"; String exp1 = "65";
		String num2 = "15986"; String exp2 = "20";
		String num3 = "1"; String exp3 = "12345487987545";
		String num4 = "2"; String exp4 = "300";
		
		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		
		BigNum e1 = new BigNum(exp1);
		BigNum e2 = new BigNum(exp2);
		BigNum e3 = new BigNum(exp3);
		BigNum e4 = new BigNum(exp4);
		
		assertEquals(
			"100000000000000000000000000000000000000000000000000000000000000000", 
			BigNum.pow(a1, e1).value()
		);
		assertEquals(
			"1187944558853141442271508042672546004468864614820988171560965484358566081705542680576",
			BigNum.pow(a2, e2).value()
		);
		assertEquals("1", BigNum.pow(a3, e3).value());
		assertEquals(
			"2037035976334486086268445688409378161051468393665936250636140449354381299763336706183397376", 
			BigNum.pow(a4, e4).value()
		);
	}
	
	@Test
	public void modPowerTest() throws NotImplementedException {
		String num1 = "10"; String exp1 = "65"; String mod1 = "72";
		String num2 = "15986"; String exp2 = "20"; String mod2 = "54678951324456567897456789";
		String num3 = "45667895156896"; String exp3 = "12345487987545"; String mod3 = "657894512345659";
		String num4 = "2"; String exp4 = "300";
		
		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		
		BigNum e1 = new BigNum(exp1);
		BigNum e2 = new BigNum(exp2);
		BigNum e3 = new BigNum(exp3);
		BigNum e4 = new BigNum(exp4);	
		
		BigNum N1 = new BigNum(mod1);
		BigNum N2 = new BigNum(mod2);
		BigNum N3 = new BigNum(mod3);
		
		BigInteger test1 = new BigInteger(num3);
		BigInteger test2 = new BigInteger(exp3);
		BigInteger test3 = new BigInteger(mod3);
		assertEquals("655010667390110", test1.modPow(test2, test3).toString());
		
//		assertEquals("64", BigNum.modPow(a1, e1, N1).value());
//		assertEquals("24760346508885414297974896", BigNum.modPow(a2, e2, N2).value());
//		assertEquals("655010667390110", BigNum.modPow(a3, e3, N3).value());
	}
	
	@Test
	public void rightShiftTest() {
		String num1 = "2048";
		String num2 = "26372019823136566122346666808945000";
		String num3 = "3065462399733956442753916145106165204602690591120";
		String num4 = "18638113891032227147646883379486929543615010149240";
		String num5 = "2314301563904181629170915421040234978323082657395477494358960";
		String num6 = "753508788";
		
		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		BigNum a5 = new BigNum(num5);
		BigNum a6 = new BigNum(num6);
		
		assertEquals("1024", BigNum.rightShift(a1, 1).value());
		assertEquals("25753925608531802853854166805610", BigNum.rightShift(a2, 10).value());
		assertEquals("748403906185047959656717808863809864404953757", BigNum.rightShift(a3, 12).value());
		assertEquals("0", BigNum.rightShift(a4, 165).value());
		assertEquals("35313439390627771441206595169681319859666178243949546727", BigNum.rightShift(a5, 16).value());
		assertEquals("376754394", BigNum.rightShift(a6, 1).value());
	}
	
	@Test
	public void leftShiftTest() {
		String num1 = "2048";
		String num2 = "26372019823136566122346666808945000";
		String num3 = "3065462399733956442753916145106165204602690591120";
		String num4 = "18638113891032227147646883379486929543615010149240";
		String num5 = "2314301563904181620000000000004978323082657395477494358960";
		
		BigNum a1 = new BigNum(num1);
		BigNum a2 = new BigNum(num2);
		BigNum a3 = new BigNum(num3);
		BigNum a4 = new BigNum(num4);
		BigNum a5 = new BigNum(num5);
		
		assertEquals("4096", BigNum.leftShift(a1, 1).value());
		assertEquals("54009896597783687418565973624719360000", BigNum.leftShift(a2, 11).value());
		assertEquals(
			"1645757594246877752789570752394671250217713095308413501440", 
			BigNum.leftShift(a3, 29).value()
		);
		assertEquals(
			"10492325346817700433911706743108051881070778673488708141716602880", 
			BigNum.leftShift(a4, 49).value()
		);
		assertEquals(
			"787516013934144445200114799050171486402719327937634871189283786190269308504735931188488448245760", 
			BigNum.leftShift(a5, 128).value()
		);
	}
	
	@Test
	public void factorizationTest() {
		BigNumHandler helper = new BigNumHandler();
		RC4 gen = new RC4("Some random key.".getBytes());
		
		String p = helper.getAPrime(30, 10, gen);
		String q = helper.getAPrime(35, 20, gen);
		String pq = helper.mul(p, q);
		
		try {
			String[] test = helper.factorizePQ(pq);
			String f1 = test[1];
			String f2 = test[0];
			
			if (test[0].length() < test[1].length()) {
				f1 = test[0];
				f2 = test[1];
			}
			
			
			assertEquals(f1, p);
			assertEquals(f2, q);
		}
		catch (RuntimeException e) {
			fail();
		}
	}
}
