/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.Stan.Crypt.ECC;

import java.math.BigInteger;
import java.util.*;

/**
 * 
 * @author Stan Chen
 */
public class InverseMessage {
	BigInteger k = new BigInteger("20");
	private String message;

	public InverseMessage(Vector cipherPoints) {
		// Iterator i = messagePoints.iterator();
		StringBuffer temp = new StringBuffer();

		for (int i = 0; i < cipherPoints.size(); i++) {
			temp.append(new String(
					backToLine((Point) cipherPoints.elementAt(i)).toByteArray()));
		}
		this.message = new String(temp);
	}

	private BigInteger backToLine(Point messagePoint) {
		return messagePoint.getX().divide(k);
	}

	public String getMessage() {
		return this.message;
	}
}
