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
public class Encryption {
	Vector cipher = new Vector();

	public Encryption(BigInteger sdSeKey, Point rePuKey, String msg, Curve curve) {
		Embedding embed = new Embedding(curve);
		Vector msgPoints = new Vector();
		msgPoints = embed.embeddingMessage(msg, curve);
		for (int i = 0; i < msgPoints.size(); i++) {
			cipher.addElement(rePuKey.factors(sdSeKey, rePuKey).pointAdd(
					(Point) msgPoints.elementAt(i)));
		}
	}

	public String getCipher() {
		return cipher.elementAt(0).toString();
	}

}
