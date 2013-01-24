package org.Stan.DataSMS.Core;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.Stan.Crypt.AES.AES;
import org.Stan.DataSMS.Config;
import org.Stan.DataSMS.Conversation;
import org.Stan.DataSMS.R;
import org.Stan.db.Account;
import org.Stan.db.DBAdapter;
import org.Stan.db.Message;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;

public class Core {

	/**
	 * List the six stages of this application.
	 */
	private static final char ECDHPART1 = '0';
	private static final char ECDHPART2 = '1';
	private static final char AESINIT = '2';
	private static final char ACK = '3';
	private static final char CIPHER = '4';
	private static final char ERROR = '5';
	private static final char SEPTAG = '|';

	/**
	 * List the configuration options
	 */
	private static final String ECDH25 = "secp224r1";
	private static final String ECDH32 = "secp384r1";
	private static final String AES1 = "64";
	private static final String AES2 = "96";
	private static final String AES3 = "128";
	private static final String AESKEYSIZE = "aeskeysize";
	private static final String MAXMESSAGESIZEFORDATASMS = "140";

	/**
	 * List the package heads
	 */
	private static final String ECDHOPTION = "ECDHOption";
	private static final String ECDHPART1PRIVATEKEY = "ECDHpart1privatekey";
	private static final String ECDHPART2PRIVATEKEY = "ECDHpart2privatekey";
	private static final String ECDHPART1PUBKEY = "ECDHpart1PubKey";
	private static final String ECDHPART2PUBKEY = "ECDHpart2Pubkey";
	private static final String MASTERKEY = "masterkey";
	private static final String CURVE = "curve";
	private static final String CANT_PARSE_MESSAGE_FEEDBACK = "cant_parse_message_feedback";

	/**
	 * List the configuration of Notification
	 */
	public static final int NOTIFICATION_ID = 1;

	/**
	 * Reference to
	 */
	private DBAdapter db;
	private Context context;
	private AES aes;
	private static String masterKey;
	private ECCInterface ecc;

	/**
	 * Main class
	 */
	public Core() {

	}

	public Core(Context context) {
		this.context = context;
	}

	public String getMasterKey() {
		return masterKey;
	}

	/**
	 * Parse Message
	 */
	public boolean parseMsg(String phoneNo, String msg) {
		boolean readable = false;
		char i = SEPTAG;
		if (msg.charAt(1) == i) {
			char tag = msg.charAt(0);
			msg = msg.substring(2);
			switch (tag) {
			case (ECDHPART1): {
				System.out.println("Received ECDHP1: " + msg);
				receivedECDHPart1(phoneNo, msg);
				break;
			}
			case (ECDHPART2): {
				System.out.println("Received ECDHP2: " + msg);
				receivedECDHPart2(phoneNo, msg);
				break;
			}
			case (AESINIT): {
				System.out.println("received AES KEY: " + msg);
				receivedAESKey(phoneNo, msg);
				recordNewAccount(phoneNo);
				break;
			}
			case (CIPHER): {
				System.out.println("cipher received: " + msg);
				if (receivedCipher(phoneNo, msg)) {
					readable = true;
				} else {
					sendCantParseFeedBack(phoneNo);
					System.out.println("readable : " + readable);
					System.out.println("not readable");
				}
				break;
			}
			case (ERROR): {
				receivedCantParseMsg(phoneNo);
				Toast.makeText(context, "Received a error message",
						Toast.LENGTH_SHORT).show();
				break;
			}
			case (ACK): {
				System.out.println("Received AES Acknowledgement: " + msg);
				boolean success = receivedACKmsg(phoneNo, msg);
				System.out.println("Success acknowledge or not : " + success);
				Toast.makeText(
						context,
						"Init secure process finished, please resend the message.",
						Toast.LENGTH_SHORT).show();
				recordNewAccount(phoneNo);
				sendReceivedACKBroadcost(context);
				break;
			}
			default: {
				Toast.makeText(context, "Received a error message",
						Toast.LENGTH_SHORT).show();
				break;
			}
			}
		}

		return readable;
	}

	/**
	 * Stage One: 1. initiate new private key pair 2. Send ECDH Part 1 message
	 * 3. Receive ECDH Part 1 message 4. Send ECDH Part 2 message
	 * 
	 */
	private void initKeyExchange(String phoneNo) {
		String ECDHPart1PubKey = generateECDHkey(Config.ECDHOPTION);
		sendECDHpart1(phoneNo, ECDHPart1PubKey);
	}

	private String generateECDHkey(String ECDHOption) {
		ECCInterface ecc = new ECCInterface(Config.ECDHOPTION);
		ecc.generateNewKeyPair();
		savePreference(Core.ECDHPART1PRIVATEKEY, ecc.getPriKey());
		savePreference(Core.ECDHPART1PUBKEY, ecc.getPubKey());
		return ecc.getPubKey();
	}

	private void sendECDHpart1(String phoneNo, String msg) {
		String sendMsg = Character.toString(ECDHPART1)
				+ Character.toString(SEPTAG) + Config.ECDHOPTION
				+ Character.toString(SEPTAG) + msg;
		System.out.println("Send ECDHPart1: " + sendMsg);
		sendSMS(phoneNo, sendMsg);
	}

	private boolean receivedECDHPart1(String phoneNo, String ECDHReceiverPubKey) {
		if (ECDHReceiverPubKey.charAt(9) == SEPTAG) {
			String ECDHOption = ECDHReceiverPubKey.substring(0, 9);
			String PubKey1 = ECDHReceiverPubKey.substring(10);
			ECCInterface ecc = new ECCInterface(ECDHOption);
			ecc.generateNewKeyPair();
			savePreference(Core.CURVE, ECDH25);
			savePreference(Core.ECDHPART2PRIVATEKEY, ecc.getPriKey());
			savePreference(Core.ECDHPART1PUBKEY, PubKey1);
			sendECDHpart2(phoneNo, ecc.getPubKey(), ECDHOption);
			return true;
		} else {
			this.sendError(phoneNo, "ECDHPART1");
			return false;
		}
	}

	private void sendECDHpart2(String phoneNo, String msg, String ECDHOption) {
		String sendMsg = Character.toString(ECDHPART2)
				+ Character.toString(SEPTAG) + ECDHOption
				+ Character.toString(SEPTAG) + msg;
		System.out.println("Send ECDHPart2: " + sendMsg);
		sendSMS(phoneNo, sendMsg);
	}

	/**
	 * Stage two, 1. Receive ECDH part 2 message 2. Send AES Master Key 3.
	 * Receive AES Master key
	 * 
	 * 
	 */

	private boolean receivedECDHPart2(String phoneNo, String ECDHReceiverPubKey) {
		if (ECDHReceiverPubKey.charAt(9) == SEPTAG) {
			String ECDHOption = ECDHReceiverPubKey.substring(0, 9);
			String PubKey2 = ECDHReceiverPubKey.substring(10);
			ECCInterface ecc = new ECCInterface(ECDHOption);
			savePreference(Core.ECDHOPTION, ECDHOption);
			if (ECDHOption.equals(ECDH25)) {
				Core.masterKey = generateAESKey(AES1);
				savePreference(Core.AESKEYSIZE, AES1);
			}
			if (ECDHOption.equals(ECDH32)) {
				Core.masterKey = generateAESKey(AES3);
				savePreference(Core.AESKEYSIZE, AES3);
			}
			savePreference(Core.ECDHPART2PUBKEY, PubKey2);
			savePreference(Core.MASTERKEY, masterKey);
			System.out.println("Master key : " + masterKey);
			sendAESMasterKey(phoneNo, encryptMasterKey(masterKey));
			return true;
		} else {
			this.sendError(phoneNo, "ECDHPART2");
			return false;
		}
	}

	private String encryptMasterKey(String key) {
		ECCInterface ecc = new ECCInterface(loadPreference(Core.ECDHOPTION)
				.toString());
		String ECDHPart1PrivateKey = loadPreference(Core.ECDHPART1PRIVATEKEY);
		String ECDHPart2PubKey = loadPreference(Core.ECDHPART2PUBKEY);
		String cipher = ecc.encryption(ECDHPart1PrivateKey, ECDHPart2PubKey,
				key);
		return cipher;
	}

	private void sendAESMasterKey(String phoneNo, String masterKey) {
		String msg = Character.toString(AESINIT) + Character.toString(SEPTAG)
				+ masterKey;
		sendSMS(phoneNo, msg);
	}

	/**
	 * Stage 3: 1.Received AES Key 2.Send back ACK message 3.Save the new
	 * account information
	 */

	private void receivedAESKey(String phoneNo, String aesKeyCipher) {
		String ECDHPart2PrivateKey = loadPreference(Core.ECDHPART2PRIVATEKEY);
		String ECDHPart1PubKey = loadPreference(Core.ECDHPART1PUBKEY);
		ECCInterface ecc = new ECCInterface(loadPreference(Core.ECDHOPTION));
		String AESKey = ecc.decryption(ECDHPart2PrivateKey, ECDHPart1PubKey,
				aesKeyCipher);
		savePreference(Core.MASTERKEY, AESKey);
		System.out.println("AES key received: " + AESKey);
		recordNewAccount(phoneNo);
		sendACKMsg(phoneNo, AESKey);
	}

	private void sendACKMsg(String phoneNo, String masterKey) {
		String ACKmsg = Character.toString(ACK) + Character.toString(SEPTAG)
				+ AESEncrypt(masterKey, loadPreference(Core.MASTERKEY));
		sendSMS(phoneNo, ACKmsg);
	}

	private void recordNewAccount(String phoneNo) {
		// adjust phone number form
		if (phoneNo.startsWith("0")) {
			phoneNo = phoneNo.substring(1);
			String temp = "+61";
			temp += phoneNo;
			phoneNo = temp;
		}
		db = new DBAdapter(context);
		db.open();
		Account acc = new Account(phoneNo, phoneNo,
				loadPreference(Core.MASTERKEY));
		db.deletOneAccount(acc);
		db.insertAccount(acc);
		db.close();
	}

	/**
	 * Stage 4: 1. Receive ACK message 2.Record new account 3. Send broadcost to
	 * notify the send function.
	 */

	private boolean receivedACKmsg(String phoneNo, String msg) {
		String masterKey = AESDecrypt(msg, loadPreference(Core.MASTERKEY));
		if (masterKey.equals(loadPreference(Core.MASTERKEY))) {
			return true;
		}
		return false;
	}

	/**
	 * Stage 5: 1. Send Cipher 2.Receive Cipher
	 */

	private void sendCipher(String phoneNo, String msg) {
		if (phoneNo.startsWith("0")) {
			phoneNo = phoneNo.substring(1);
			String temp = "+61";
			temp += phoneNo;
			phoneNo = temp;
		}
		db = new DBAdapter(context);
		db.open();
		Account acc = db.getAccount(phoneNo);
		Message message = new Message(Message.TO, acc.getName(),
				new CurrentTime().getCurrentTime(),
				new CurrentTime().getCurrentMillionTime(), false, msg);
		db.insertMessage(message);
		db.close();
		String cipher = Character.toString(CIPHER) + Character.toString(SEPTAG)
				+ AESEncrypt(msg, acc.getMasterKey());
		System.out.println("cipher been sendout : " + cipher);
		sendSMS(phoneNo, cipher);
	}

	private boolean receivedCipher(String phoneNo, String msg) {
		db = new DBAdapter(context);
		db.open();
		Account acc = db.getAccount(phoneNo);
		if (acc.getMasterKey().equals("Empty")) {
			return false;
		}
		String plaintText = AESDecrypt(msg, acc.getMasterKey());
		Message message = new Message(Message.FROM, acc.getName(),
				new CurrentTime().getCurrentTime(),
				new CurrentTime().getCurrentMillionTime(), false, plaintText);
		db.insertMessage(message);
		db.close();
		addNewMsgNotification(phoneNo, plaintText);
		sendReceivedBroadcost(context);
		return true;
	}

	public boolean composeMsg(String phoneNo, String msg) {
		boolean sendSuccess = true;
		if (phoneNo.startsWith("0")) {
			phoneNo = phoneNo.substring(1);
			String temp = "+61";
			temp += phoneNo;
			phoneNo = temp;
		}
		db = new DBAdapter(context);
		db.open();
		Account acc = db.getAccount(phoneNo);
		db.close();
		// if it is a new account
		if (acc.getMasterKey().equals("Empty")) {
			sendSuccess = false;
			initKeyExchange(phoneNo);
			return sendSuccess;
		} else {
			// if it is a existed account
			sendCipher(phoneNo, msg);
			return sendSuccess;
		}
	}

	/**
	 * error handling
	 */

	private void sendCantParseFeedBack(String phoneNo) {
		String msg = Character.toString(ERROR) + Character.toString(SEPTAG)
				+ Core.CANT_PARSE_MESSAGE_FEEDBACK;
		sendSMS(phoneNo, msg);
	}

	private void receivedCantParseMsg(String phoneNo) {
		db = new DBAdapter(context);
		db.open();
		String displace_msg = "If you see this message, means your previous message has not been successful deliberated for missing secure element in the other side.";
		Message message = new Message(Message.TO, phoneNo,
				new CurrentTime().getCurrentTime(),
				new CurrentTime().getCurrentMillionTime(), true, displace_msg);
		db.insertMessage(message);
		db.close();
		addNewMsgNotification(phoneNo, displace_msg);
		sendReceivedBroadcost(context);
	}

	/**
	 * Other basic functions
	 */

	private void sendError(String phoneNo, String error) {
		String msg = Character.toString(ERROR) + Character.toString(SEPTAG)
				+ error + " error";
		sendSMS(phoneNo, msg);
		Toast.makeText(context, "Protocol error", Toast.LENGTH_SHORT).show();
	}

	private void sendSMS(String phoneNo, String msg) {
		SmsManager sm = SmsManager.getDefault();
		short port = 1200;
		sm.sendDataMessage(phoneNo, null, port, msg.getBytes(), null, null);
	}

	private void addNewMsgNotification(String phoneNo, String msg) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(ns);
		int icon = R.drawable.sms_icon;
		CharSequence contentTitle = phoneNo + ": ";
		CharSequence contentText = msg;
		Notification notification = new Notification(icon, contentTitle,
				System.currentTimeMillis());
		Intent notificationIntent = new Intent(context,
				new Conversation().getClass());
		notificationIntent.putExtra(Account.NUMBER, phoneNo);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				pendingIntent);

		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	/**
	 * AES functions
	 * 
	 * @param AESkeysize
	 * @return
	 */
	private String generateAESKey(String AESkeysize) {
		BigInteger mt = BigInteger.probablePrime(Integer.valueOf(AESkeysize),
				new SecureRandom());
		return mt.toString(16);
	}

	private String AESEncrypt(String msg, String masterKey) {
		aes = new AES();
		aes.setKey(masterKey);
		return aes.Encrypt(msg);
	}

	private String AESDecrypt(String msg, String masterKey) {
		aes = new AES();
		aes.setKey(masterKey);
		return aes.Decrypt(msg);
	}

	/**
	 * Save and load Preference
	 * 
	 * @param key
	 * @param value
	 */

	private void savePreference(String key, String value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(key,
				0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	private String loadPreference(String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(key,
				0);
		String result = sharedPreferences.getString(key, "");
		return result;
	}

	/**
	 * Send Broadcost notification
	 */

	private void sendReceivedACKBroadcost(Context context) {
		Intent intent = new Intent("org.Stan.DataSMS.ReceivedACKSMS");
		context.sendBroadcast(intent);
	}

	private void sendReceivedBroadcost(Context context) {
		Intent intent = new Intent("org.Stan.DataSMS.ReceivedSMS");
		context.sendBroadcast(intent);
	}

	/**
	 * Current time class
	 * 
	 * @author Stan Chen
	 * 
	 */
	class CurrentTime {
		public CurrentTime() {

		}

		public String getCurrentTime() {
			Calendar calendar = new GregorianCalendar();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DATE);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			return day + "/" + month + "/" + year + ", " + hour + ":" + minute;
		}

		public String getCurrentMillionTime() {
			String time = "";
			Calendar calendar = new GregorianCalendar();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DATE);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			time += String.valueOf(year);
			time += String.valueOf(month);
			time += String.valueOf(day);
			time += String.valueOf(hour);
			time += String.valueOf(minute);
			time += String.valueOf(second);
			return time;
		}
	}
}
