/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.Stan.Crypt.ECC;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * 
 * @author Stan
 */
public class Signature {

	public Signature() {
	}

	public Point generateSig(Curve curve, String message, BigInteger privateKey) {
		SecureRandom random = new SecureRandom();
		BigInteger e, n, r, j, k, s;
		Point g = new Point(curve);
		Point b = new Point(curve);
		Point signature = new Point(curve);
		signature.equal(curve.getG());
		g.equal(curve.getG());
		k = privateKey;
		n = curve.getN();
		// e = BigInteger.valueOf(4);
		e = BigInteger.valueOf(message.hashCode()).mod(curve.getZp());
		// 1. Select a random or pseudorandom integer j such that j < p
		// 2. Compute b = jg mod p and r = b mod q if r = 0, then goto step 1
		do {
			do {
				do {
					j = new BigInteger(curve.getZp().bitLength(), random);
					// j = new BigInteger("5");
				} while (j.equals(BigInteger.valueOf(0)));
				b.equal(g.factors(j, g));
				r = b.getX().mod(n);
			} while (r.compareTo(BigInteger.valueOf(0)) == 0);
			// 3. Compute 1/j mod q
			// 4. Assume message m is small ( if m is large then compute hash
			// value of
			// the message m if m is big e =SHA-1(m)
			// 5. Compute s = 1/j {e + kr} mod q. If s = 0 then go to step 1.
			s = j.modInverse(n).multiply(e.add(k.multiply(r))).mod(n);
		} while (s.compareTo(BigInteger.valueOf(0)) == 0);
		// 6 Your signature for the message is (r,s)
		signature.setX(r);
		signature.setY(s);
		return signature;
	}

	public boolean testSig(Curve curve, Point signature, String message,
			Point pubKey) {
		// 1. Verify that r and s are in the range [1,q-1]=[1,13-1]
		// 2. Compute e = SHA-1(m) ; e = m if m small
		// 3. Compute w = 1/s mod n
		// 4. Compute u1 = e*w mod n and u2 = r*w mod n
		// 5. Compute (x1,y1) = u1g + u2Q (Q is your public key)
		// 6. if x1 = r mod n then, your signature is valid.
		Point publicKey = new Point();
		publicKey.equal(pubKey);
		BigInteger n = curve.getN();
		// BigInteger e = new BigInteger("4");
		BigInteger e = BigInteger.valueOf(message.hashCode());
		e = e.mod(curve.getZp());
		BigInteger w = signature.getY().modInverse(n);
		BigInteger u1 = e.multiply(w).mod(n);
		BigInteger u2 = signature.getX().multiply(w).mod(n);
		Point inverSig = new Point(curve);
		// System.out.println(n+" "+e+" "+u1+" "+u2+" "+inverSig+ "  "+
		// publicKey);
		Point temp = new Point();
		// System.out.println("temp.equal(Curve.getG())"+ temp);
		// temp.setZp(curve.getN());
		inverSig.equal(temp.factors(u1, curve.getG()));
		// System.out.println("             "+inverSig);
		// pubKey.setZp(curve.getN());
		// System.out.println("pubKey: "+publicKey);
		// System.out.println("pubKey.factors(u2, pubKey)"+publicKey.factors(u2,
		// pubKey));
		inverSig.equal(inverSig.pointAdd(publicKey.factors(u2, publicKey)));
		// System.out.println("Inverse Sig"+inverSig);
		if (signature.getX().equals(inverSig.getX().mod(n)))
			return true;
		return false;
	}
}
