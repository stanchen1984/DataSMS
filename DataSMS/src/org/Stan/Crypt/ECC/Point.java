/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.Stan.Crypt.ECC;

import java.math.BigInteger;

/**
 * 
 * @author Stan
 */
public class Point {
	private BigInteger x, y;
	static BigInteger zp, a, b, o;
	static final BigInteger THREE = new BigInteger("3");
	static final BigInteger TWO = new BigInteger("2");
	static final BigInteger ONE = new BigInteger("1");
	static final BigInteger ZERO = new BigInteger("0");
	static Point res = new Point();

	public Point(Curve curve) {
		this.x = curve.getG().getX();
		this.y = curve.getG().getY();
		Point.zp = curve.getZp();
		Point.o = curve.getZp();
		Point.a = curve.getA();
		Point.b = curve.getB();
	}

	public Point() {
	}

	public Point(String x, String y) {
		this.x = new BigInteger(x, 16);
		this.y = new BigInteger(y, 16);
	}

	public Point(String point) {
		String position[] = point.split("\\|");
		this.x = new BigInteger(position[0], 16);
		this.y = new BigInteger(position[1], 16);
	}

	public void equal(Point point) {
		this.x = point.getX();
		this.y = point.getY();
		Point.zp = point.getZp();
	}

	public Point factors(BigInteger n, Point g) {
		Point temp = g;
		res = g;
		int len = n.bitLength();
		for (int i = 1; i < len; i++) {
			temp = temp.pointAdd(temp);
			if (n.testBit(i)) {
				res = res.pointAdd(temp);
			}
		}
		if (n.testBit(0)) {
			res = res.pointAdd(g);
		}
		return res;
	}

	public Point pointAdd(Point q) {
		BigInteger xp = this.x;
		BigInteger yp = this.y;
		BigInteger xq = q.getX();
		BigInteger yq = q.getY();
		if ((xp.equals(xq) && !yp.equals(yq))
				|| (xp.equals(xq) && yp.equals(yq) && yp.equals(ZERO)))
			return methodO();
		if ((xp.equals(o) || xq.equals(o)))
			return methodOAdd(xp, xq, yp, yq);
		if (xp.equals(xq) && yp.equals(yq) && !yp.equals(o))
			return methodTwo(xp, xq, yp, yq);
		else
			return methodOne(xp, xq, yp, yq);
	}

	// Adding distinct points P and Q (1)
	// When P = (xP,yP) and Q = (xQ,yQ) and P Q, P  -Q, 
	// P + Q = R(xR, yR) with xR = s2 - xP - xQ and yR = s(xP - xR) - yP 
	// where s = (yP - yQ) / (xP - xQ)
	private Point methodOne(BigInteger xp, BigInteger xq, BigInteger yp,
			BigInteger yq) {
		Point res = new Point();
		BigInteger temp = (xp.subtract(xq)).modInverse(zp);
		BigInteger s = (yp.subtract(yq)).multiply(temp).mod(zp);
		res.setX(s.multiply(s).subtract(xp).subtract(xq).mod(zp));
		res.setY(s.multiply(xp.subtract(res.getX())).subtract(yp).mod(zp));
		return res;
	}

	// Doubling the point P (2) 
	// When yP is not O,2P = R(xR, yR) with xR = s2 - 2xP and yR = s(xP - xR)
	// -yP
	// where s = (3xP2 + a) / (2yP )
	private Point methodTwo(BigInteger xp, BigInteger xq, BigInteger yp,
			BigInteger yq) {
		Point res = new Point();
		BigInteger s = (THREE.multiply(xp.multiply(xp)).add(a)).mod(zp)
				.multiply(yp.add(yp).modInverse(zp)).mod(zp);
		res.setX(s.multiply(s).subtract(xp).subtract(xq).mod(zp));
		res.setY(s.multiply(xp.subtract(res.getX())).subtract(yp).mod(zp));
		return res;
	}

	private Point methodO() {
		Point res = new Point();
		res.setX(o);
		res.setY(o);
		return res;
	}

	private Point methodOAdd(BigInteger xp, BigInteger xq, BigInteger yp,
			BigInteger yq) {
		Point res = new Point();
		if (xp.equals(o)) {
			res.setX(xq);
			res.setY(yq);
		}
		if (xq.equals(o)) {
			res.setX(xp);
			res.setY(yp);
		}
		return res;
	}

	public void setX(BigInteger x) {
		this.x = x;
	}

	public void setY(BigInteger y) {
		this.y = y;
	}

	public void setZp(BigInteger zp) {
		Point.zp = zp;
	}

	public BigInteger getX() {
		return x;
	}

	public BigInteger getY() {
		return y;
	}

	@Override
	public String toString() {
		return x.toString(16) + "|" + y.toString(16);
	}

	public BigInteger getZp() {
		return zp;
	}

}
