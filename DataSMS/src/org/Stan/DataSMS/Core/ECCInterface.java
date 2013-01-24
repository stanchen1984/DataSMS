package org.Stan.DataSMS.Core;

import org.Stan.Crypt.ECC.ECC;
import org.Stan.Crypt.ECC.Point;

public class ECCInterface {
	/**
	 * This is the Interface class for ECC library.
	 */

	String ECCLibrary = "/org/Stan/Crypt/ECC/ECC.java";

	private ECC ecc;

	public ECCInterface(String ECDHOPTION) {
		ecc = new ECC(ECDHOPTION);
	}

	public void generateNewKeyPair() {
		ecc.generateNewKey();
	}

	public String getPriKey() {
		return ecc.getSecKey();
	}

	public String getPubKey() {
		return ecc.getPubKey();
	}

	public String encryption(String privatekey, String pubkey, String msg) {
		return ecc.encryption(privatekey, new Point(pubkey), msg);
	}

	public String decryption(String privatekey, String pubkey, String msg) {
		return ecc.decryption(privatekey, new Point(pubkey), msg);
	}
}
