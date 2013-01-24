/*
 * Embedding class to convert message to point.
 */

package org.Stan.Crypt.ECC;

import java.util.Vector;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * 
 * @author Stan Chen
 */
public class Embedding {
	BigInteger k, n, p, j;
	private SecureRandom random = new SecureRandom();

	public Embedding() {
	}

	public Embedding(Curve curve) {
		this.p = curve.getZp();
		this.k = new BigInteger("20");
		getP();
		j = new BigInteger(3, random);
	}

	public Vector embeddingMessage(String message, Curve curve) {
		Vector msgInt = new Vector();
		msgInt = msgBlocking(message);
		// System.out.println("msgInt:"+msgInt);
		Vector messagePoints = new Vector();
		for (int i = 0; i < msgInt.size(); i++) {
			messagePoints.addElement(embedding(
					(BigInteger) msgInt.elementAt(i), curve));
		}
		return messagePoints;
	}

	private void getP() {
		while (!p.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
			p = BigInteger.probablePrime(p.bitLength() - 1, random);
		}
	}

	private int getBlockSize() {
		return (p.subtract(k)).divide(k).toString().length() / 3 - 1;
	}

	private Point embedding(BigInteger message, Curve curve) {
		Point messagePoint = new Point();
		messagePoint.setX(getXj(message));
		messagePoint.setY(getZj(messagePoint.getX(), curve));
		return messagePoint;
	}

	// For j=0, …, k-1
	// Set xj = m*k + j ; wj = x j 3 + a xj + b; zj = wj((p+1)/4)
	private BigInteger getXj(BigInteger message) {
		return message.multiply(k).add(j);
	}

	private BigInteger getZj(BigInteger xj, Curve curve) {
		BigInteger wj = xj.modPow(BigInteger.valueOf(3), curve.getZp())
				.add(xj.multiply(curve.getA())).add(curve.getB())
				.mod(curve.getZp());
		return wj.modPow(
				(p.add(BigInteger.valueOf(1)).divide(BigInteger.valueOf(4))),
				curve.getZp());
	}

	private Vector msgBlocking(String message) {
		int blocksize = getBlockSize();
		Vector msgs = new Vector();
		for (int i = 0; i < message.length() / blocksize + 1; i++) {
			BigInteger tempInt;
			if (message.length() >= (i + 1) * blocksize) {
				tempInt = new BigInteger(message.substring(i * blocksize,
						(i + 1) * blocksize).getBytes());
			} else {
				tempInt = new BigInteger(message.substring(i * blocksize,
						message.length()).getBytes());
			}

			if (tempInt != null) {
				msgs.addElement(tempInt);
			}
		}
		return msgs;
	}
}
