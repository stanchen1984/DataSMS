/*
 * y2 = x3 + ax + b, where x, y, a and b are real numbers,
 * where 4a3 + 27b2 <>0 – condition for distinct single roots (smooth curve).
 */

package org.Stan.Crypt.ECC;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * 
 * @author Stan Chen
 */
public class Curve {
	private BigInteger p;
	private BigInteger a, b;
	private BigInteger y, x, zp;
	private BigInteger seed;
	static final BigInteger FOUR = new BigInteger("4");
	static final BigInteger ZERO = new BigInteger("0");
	static final BigInteger TWINSEVEN = new BigInteger("27");
	private SecureRandom random = new SecureRandom();
	Point g = new Point();

	public Curve(String type) {
		int typeInt = 0;
		if (type.equals("secp224r1")) {
			typeInt = 256;
		}
		if (type.equals("secp384r1")) {
			typeInt = 384;
		}

		switch (typeInt) {
		case (256): {
			this.p = new BigInteger(
					"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF000000000000000000000001",
					16);
			this.a = new BigInteger(
					"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFE",
					16);
			this.b = new BigInteger(
					"B4050A850C04B3ABF54132565044B0B7D7BFD8BA270B39432355FFB4",
					16);
			this.x = new BigInteger(
					"B70E0CBD6BB4BF7F321390B94A03C1D356C21122343280D6115C1D21",
					16);
			this.y = new BigInteger(
					"BD376388B5F723FB4C22DFE6CD4375A05A07476444D5819985007E34",
					16);
			this.zp = new BigInteger(
					"FFFFFFFFFFFFFFFFFFFFFFFFFFFF16A2E0B8F03E13DD29455C5C2A3D",
					16);
			this.seed = new BigInteger(
					"BD71344799D5C7FCDC45B59FA3B9AB8F6A948BC5", 16);
			g.setX(x);
			g.setY(y);
			break;
		}
		case (384): {
			this.p = new BigInteger(
					"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFF0000000000000000FFFFFFFF",
					16);
			this.a = new BigInteger(
					"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFF0000000000000000FFFFFFFC",
					16);
			this.b = new BigInteger(
					"B3312FA7E23EE7E4988E056BE3F82D19181D9C6EFE8141120314088F5013875AC656398D8A2ED19D2A85C8EDD3EC2AEF",
					16);
			this.x = new BigInteger(
					"AA87CA22BE8B05378EB1C71EF320AD746E1D3B628BA79B9859F741E082542A385502F25DBF55296C3A545E3872760AB7",
					16);
			this.y = new BigInteger(
					"3617DE4A96262C6F5D9E98BF9292DC29F8F41DBD289A147CE9DA3113B5F0B8C00A60B1CE1D7E819D7A431D7C90EA0E5F",
					16);
			this.zp = new BigInteger(
					"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC7634D81F4372DDF581A0DB248B0A77AECEC196ACCC52973",
					16);
			this.seed = new BigInteger(
					"A335926AA319A27A1D00896A6773A4827ACDAC73", 16);
			g.setX(x);
			g.setY(y);
			break;
		}
		}
	}

	public Curve(int pSize) {
		generateAB();
		this.zp = BigInteger.probablePrime(pSize, random);
		while (!generateG(a.intValue(), b.intValue())) {
			generateAB();
		}
	}

	public Curve(BigInteger a, BigInteger b, BigInteger zp, BigInteger x,
			BigInteger y, BigInteger p) {
		this.a = a;
		this.b = b;
		this.zp = zp;
		this.p = p;
		this.g = new Point(this);
		g.setX(x);
		g.setY(y);
	}

	public Curve() {
	}

	public BigInteger getN() {
		return this.p;
	}

	public void setCurve(Curve curve) {
		this.a = curve.getA();
		this.b = curve.getB();
		this.zp = curve.getZp();
		g.equal(curve.getG());
	}

	private void generateAB() {
		a = BigInteger.valueOf(0);
		b = BigInteger.valueOf(0);
		while (!testSmooth(a, b) || a.compareTo(ZERO) != 1
				|| b.compareTo(ZERO) != 1) {
			a = new BigInteger(4, random);
			b = new BigInteger(4, random);
		}
	}

	// y2 = x3 + ax + b
	private boolean generateG(int a, int b) {
		boolean success = false;
		for (int x = 0; x < 60; x++) {
			int sum = x * x * x + a * x + b;
			if (isPerfectSquare(sum)) {
				g.setX(BigInteger.valueOf(x));
				g.setY(BigInteger.valueOf((int) Math.sqrt(sum)));
				success = true;
				break;
			}
		}
		return success;
	}

	private boolean isPerfectSquare(int n) {
		if (n < 0)
			return false;

		long test = (long) (Math.sqrt(n) + 0.5);
		return test * test == n;
	}

	public Point getG() {
		return g;
	}

	public BigInteger getA() {
		return a;
	}

	public BigInteger getB() {
		return b;
	}

	public void setAB(int a, int b) {
		if (testSmooth(BigInteger.valueOf(a), BigInteger.valueOf(b))) {
			this.a = BigInteger.valueOf(a);
			this.b = BigInteger.valueOf(b);
		}
	}

	// test 4a3 + 27b2 <>0
	private boolean testSmooth(BigInteger a, BigInteger b) {
		if (a.pow(3).multiply(FOUR).add(b.pow(2).multiply(TWINSEVEN))
				.equals(ZERO))
			return false;
		else
			return true;
	}

	@Override
	public String toString() {
		return "y^2 = " + "x^3 + " + a + "x + " + b + "\n" + " Point G is " + g;
	}

	public BigInteger getSeed() {
		return seed;
	}

	public BigInteger getZp() {
		return zp;
	}

}
