/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.Stan.Crypt.ECC;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * 
 * @author Sandro
 */
public class KeyGenerate {
	private BigInteger xg, yg, zp, privateKey, seed;

	private Point pointG = new Point();
	private SecureRandom random = new SecureRandom();

	public KeyGenerate(int keySize, Curve curve) {
		this.zp = curve.getZp();
		this.pointG = new Point(curve);
		this.seed = curve.getSeed();
		privateKey = BigInteger.probablePrime(keySize,
				new SecureRandom(seed.toByteArray()));
		pointG.equal(pointG.factors(privateKey, pointG));
	}

	public Point getPublicKey() {
		return pointG;
	}

	public BigInteger getPrivateKey() {
		return privateKey;
	}
}
