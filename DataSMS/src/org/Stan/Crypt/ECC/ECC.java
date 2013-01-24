package org.Stan.Crypt.ECC;

import java.math.BigInteger;

/**
 * ECC is the main interface for ECC package. It provides key generation,
 * encryption, decryption, and signature vertification.
 * 
 * @author Stan
 * 
 */

public class ECC {

	/**
	 * The ECC elements
	 */
	private static KeyGenerate ECDHPart;
	private Curve curve;
	private String ECDHkeysize;
	private String pubKey;
	private String secKey;

	/**
	 * ECC types
	 * 
	 */
	private static final String ECDH25 = "secp224r1";
	private static final String ECDH32 = "secp384r1";

	public ECC(String ECDHkeysize) {
		curve = new Curve(ECDHkeysize);
		this.ECDHkeysize = ECDHkeysize;
	}

	// generate new key
	public void generateNewKey() {
		if (ECDHkeysize.equals(ECDH25)) {
			ECDHPart = new KeyGenerate(223, curve);
		}
		if (ECDHkeysize.equals(ECDH32)) {
			ECDHPart = new KeyGenerate(324, curve);
		}
		pubKey = ECDHPart.getPublicKey().toString();
		secKey = ECDHPart.getPrivateKey().toString(16);
	}

	// encrypt the message
	public String encryption(String privatekey, Point pubkey, String msg) {
		Encryption encrypt = new Encryption(new BigInteger(privatekey, 16),
				pubkey, msg, curve);
		return encrypt.getCipher();
	}

	public String decryption(String privateKey, Point pubKey, String msg) {
		Decryption decrypt = new Decryption(pubKey, new BigInteger(privateKey,
				16), msg, curve);
		return decrypt.getMessage();
	}

	public String getPubKey() {
		return this.pubKey;
	}

	public String getSecKey() {
		return this.secKey;
	}

}
