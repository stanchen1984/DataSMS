/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.Stan.Crypt.ECC;

import java.util.Vector;
import java.math.BigInteger;

/**
 * 
 * @author Stan Chen
 */
public class Decryption {
	Vector pltxt = new Vector();
	String message;

	public Decryption(Point sdPuKey, BigInteger reSeKey, String cip, Curve curve) {
		Vector cipher = new Vector();
		Point cipPoint = new Point(cip);
		cipher.add(cipPoint);
		Point g = new Point(curve);
		Point temp = new Point(curve);
		sdPuKey.setZp(curve.getZp());
		g.equal(sdPuKey.factors(reSeKey, sdPuKey));
		g.setY(g.getY().negate().mod(sdPuKey.getZp()));

		for (int i = 0; i < cipher.size(); i++) {
			temp.equal(g);
			pltxt.addElement(temp.pointAdd((Point) cipher.elementAt(i)));
		}

		InverseMessage mes = new InverseMessage(pltxt);
		message = mes.getMessage().toString();
	}

	public String getMessage() {
		return message;
	}
}
