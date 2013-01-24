/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.Stan.Crypt.ECC;

import java.math.BigInteger;

/**
 * 
 * @author Sandro
 */
public class Managerment {

	public Managerment() {
	}

	public static void main(String arg[]) {
		String message = "aaa123123123123123123123123asdaaaaawqqqqqqqqqqqqqqqqqqq"
				+ "qqqqqq1a123123123123123123123123asdaaaaawqqqqqqqqqqqqqqqqq"
				+ "a123123123123123123123123asdaaaaawqqqqqqqqqqqqqqqqq"
				+ "a123123123123123123123123asdaaaaawqqqqqqqqqqqqqqqqq"
				+ "a123123123123123123123123asdaaaaawqqqqqqqqqqqqqqqqq"
				+ "a12312312312312312311231111111111111111111111111111";
		// String message = "123123123";
		// System.out.println(message.hashCode());
		// //
		// String sp = "01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
		// + "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
		// + "FFFFFFFFFFFFFFFFFFFFFFFF";
		// BigInteger p = new BigInteger(sp,16);
		// System.out.println(p);
		// String sa = "01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
		// + "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
		// + "FFFFFFFFFFFFFFFFFFFFFFFC";
		// BigInteger a = new BigInteger(sa,16);
		// String sb = "0051953EB9618E1C9A1F929A21A0B68540EEA2DA725B99B315F3"
		// + "B8B489918EF109E156193951EC7E937B1652C0BD3BB1BF073573DF88"
		// + "3D2C34F1EF451FD46B503F00";
		// BigInteger b = new BigInteger(sb,16);
		// String bgx = "0200C6858E06B70404E9CD9E3ECB662395B4429C648139053FB521"
		// + "F828AF606B4D3DBAA14B5E77EFE75928FE1DC127A2FFA8DE3348B3C1"
		// + "856A429BF97E7E31C2E5BD66";
		// BigInteger gx = new BigInteger(bgx,16);
		// String bgy = "0400C6858E06B70404E9CD9E3ECB662395B4429C648139053F"
		// + "B521F828AF606B4D3DBAA14B5E77EFE75928FE1DC127A2FFA8DE3348"
		// + "B3C1856A429BF97E7E31C2E5BD66011839296A789A3BC0045C8A5FB4"
		// + "2C7D1BD998F54449579B446817AFBD17273E662C97EE72995EF42640"
		// + "C550B9013FAD0761353C7086A272C24088BE94769FD16650";
		// // System.out.println("hash" + bgy.hashCode());
		// BigInteger gy = new BigInteger (bgy,16);
		// String orderN =
		// "01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
		// + "FFFFFFFFFFFFFFFA51868783BF2F966B7FCC0148F709A5D03BB5C9B8"
		// + "899C47AEBB6FB71E91386409";
		// BigInteger n = new
		// BigInteger("68647976601306097149819007990813932172694353"
		// + "00143305409394463459185543183397655394245057"
		// + "74633321719753296399637136332111386476861244"
		// + "0380340372808892707005449");

		// String AAp2 = "DB7C2ABF62E35E668076BEAD208B";
		// BigInteger p = new BigInteger(AAp2,16);
		// System.out.println(p);
		//
		// String a2 = "DB7C2ABF62E35E668076BEAD2088";
		// BigInteger a = new BigInteger(a2,16);
		//
		// String b2 = "659EF8BA043916EEDE8911702B22";
		// BigInteger b = new BigInteger(b2,16);
		//
		// String x2 = "0209487239995A5EE76B55F9C2F098";
		// BigInteger gx = new BigInteger(x2,16);
		// System.out.println(gx);
		//
		// String y2 =
		// "0409487239995A5EE76B55F9C2F098A89CE5AF8724C0A23E0E0FF77500";
		// BigInteger gy = new BigInteger(y2,16);
		//
		// String orderN2 = "DB7C2ABF62E35E7628DFAC6561C5";
		// BigInteger n = new BigInteger(orderN2,16);
		// System.out.print(n.compareTo(p));

		// BigInteger a = new BigInteger("1");
		// BigInteger b = new BigInteger("6");
		// BigInteger p = new BigInteger("11");
		// BigInteger n = new BigInteger("13");
		// BigInteger gx = new BigInteger("2");
		// BigInteger gy = new BigInteger("7");
		// BigInteger k = new BigInteger("7");
		// //
		// Curve curve = new Curve(a, b, p,gx,gy,n) ;
		// Point G = new Point(curve);
		// G.equal(G.factors(curve.getN(), G));
		// System.out.println(G);

		Curve curve = new Curve("secp224r1");
		System.out.println("Curve is " + curve);
		KeyGenerate sender = new KeyGenerate(223, curve);
		KeyGenerate reciever = new KeyGenerate(223, curve);
		System.out.println("ZP is" + curve.getZp());
		System.out.println("Sender Private Key =" + sender.getPrivateKey());
		String part1[] = sender.getPublicKey().toString().split("\\|");
		BigInteger mm = new BigInteger(part1[0], 16);
		System.out.println("point x length: " + mm.bitLength());
		System.out.println("Sender's bulic Key"
				+ sender.getPublicKey().toString());
		System.out.println("Reciever Private Key =" + reciever.getPrivateKey());
		System.out.println("Reciever's bulic Key" + reciever.getPublicKey());
		Encryption encryp = new Encryption(sender.getPrivateKey(),
				reciever.getPublicKey(), message, curve);
		// Signature sig = new Signature();
		// Point signa = new Point(curve);
		// signa.equal(sig.generateSig(curve, message, k));
		// System.out.println(signa);
		System.out.println("Cipher: " + encryp.getCipher());
		Decryption decryp = new Decryption(sender.getPublicKey(),
				reciever.getPrivateKey(), encryp.getCipher(), curve);
		System.out.println("Message: "
				+ (decryp.getMessage().hashCode() == message.hashCode()) + "  "
				+ decryp.getMessage());
		// System.out.println("Inverse Signature: "+ sig.testSig(curve,
		// signa, message.toString(),
		// G.factors(k, G)));
		// System.out.println("Inverse Signature: "+ sig.testSig(curve,signa,
		// message,G));
	}

}
