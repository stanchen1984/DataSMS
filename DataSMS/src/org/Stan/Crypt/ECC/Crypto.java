package org.Stan.Crypt.ECC;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.Stan.Crypt.AES.AES;
import org.Stan.DataSMS.Config;
import org.Stan.DataSMS.Conversation;
import org.Stan.DataSMS.R;
import org.Stan.db.Account;
import org.Stan.db.Message;
import org.Stan.db.DBAdapter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;

public class Crypto {
	private static final char ECDHPART1 = '0';
	private static final char ECDHPART2 = '1';
	private static final char AESINIT = '2';
	private static final char ACK = '3';
	private static final char CIPHER = '4';
	private static final char ERROR = '5';
	private static final char SEPTAG = '|';
	private static final String ECDH25 = "secp224r1";
	private static final String ECDH32 = "secp384r1";
	private static final String AES1 = "64";
	private static final String AES2 = "96";
	private static final String AES3 = "128";
	private static final String AESKEYSIZE = "aeskeysize";
	private static final String MAXMESSAGESIZEFORDATASMS = "140";
	private static final String ECDHPART1PRIVATEKEY = "ECDHpart1privatekey";
	private static final String ECDHPART2PRIVATEKEY = "ECDHpart2privatekey";
	private static final String ECDHPART1PUBKEY = "ECDHpart1PubKey";
	private static final String ECDHPART2PUBKEY = "ECDHpart2Pubkey";
	private static final String MASTERKEY = "masterkey";
	private static final String CURVE = "curve";
	private static final String CANT_PARSE_MESSAGE_FEEDBACK = "cant_parse_message_feedback";
	public static final int NOTIFICATION_ID = 1;
	private DBAdapter db;
	private Context context;
	private AES aes;
	private static String masterKey;
	private static KeyGenerate ECDHPart1;
	private static KeyGenerate ECDHPart2;
	private Curve curve;
	private Point myPubKey;
	private Point receiverPubKey;

	public Crypto() {

	}

	public Crypto(Context context) {
		this.context = context;
	}

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
				System.out.println("received AES Acknowledgement: " + msg);
				boolean success = receivedACKmsg(phoneNo, msg);
				System.out.println("success acknowledge or not : " + success);
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

	public String getMasterKey() {
		return masterKey;
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

	private void initKeyExchange(String phoneNo) {
		generateECDHkey(Config.ECDHOPTION);
		sendECDHpart1(phoneNo, ECDHPart1.getPublicKey().toString());
	}

	private void generateECDHkey(String ECDHkeysize) {
		curve = new Curve(ECDHkeysize);
		if (ECDHkeysize.equals(ECDH25)) {
			ECDHPart1 = new KeyGenerate(223, curve);
			savePreference(Crypto.CURVE, ECDH25);
			savePreference(Crypto.ECDHPART1PRIVATEKEY, ECDHPart1.getPrivateKey()
					.toString(16));
			savePreference(Crypto.ECDHPART1PUBKEY, ECDHPart1.getPublicKey()
					.toString());
		} else {
			ECDHPart1 = new KeyGenerate(324, curve);
			savePreference(Crypto.CURVE, ECDH25);
			savePreference(Crypto.ECDHPART1PRIVATEKEY, ECDHPart1.getPrivateKey()
					.toString(16));
			savePreference(Crypto.ECDHPART1PUBKEY, ECDHPart1.getPublicKey()
					.toString());
		}
	}

	private boolean receivedECDHPart1(String phoneNo, String ECDHReceiverPubKey) {
		myPubKey = new Point(ECDHReceiverPubKey);
		if (myPubKey.getX().bitLength() < 225) {
			curve = new Curve(ECDH25);
			ECDHPart2 = new KeyGenerate(223, curve);
			savePreference(Crypto.CURVE, ECDH25);
			savePreference(Crypto.ECDHPART2PRIVATEKEY, ECDHPart2.getPrivateKey()
					.toString(16));
			savePreference(Crypto.ECDHPART1PUBKEY, myPubKey.toString());
			this.sendECDHpart2(phoneNo, ECDHPart2.getPublicKey().toString());
			return true;
		}
		if (myPubKey.getX().bitLength() > 320) {
			curve = new Curve(ECDH32);
			ECDHPart2 = new KeyGenerate(324, curve);
			savePreference(Crypto.CURVE, ECDH32);
			savePreference(Crypto.ECDHPART2PRIVATEKEY, ECDHPart2.getPrivateKey()
					.toString(16));
			savePreference(Crypto.ECDHPART1PUBKEY, myPubKey.toString());
			this.sendECDHpart2(phoneNo, ECDHPart2.getPublicKey().toString());
			return true;
		} else {
			this.sendError(phoneNo, "ECDHPart1");
			return false;
		}
	}

	private boolean receivedECDHPart2(String phoneNo, String ECDHReceiverPubKey) {
		receiverPubKey = new Point(ECDHReceiverPubKey);
		if (receiverPubKey.getX().bitLength() < 225) {
			Crypto.masterKey = generateAESKey(AES1);
			System.out
					.println("----------------AES key generate: " + masterKey);
			savePreference(Crypto.ECDHPART2PUBKEY, receiverPubKey.toString());
			savePreference(Crypto.AESKEYSIZE, AES1);
			savePreference(Crypto.MASTERKEY, masterKey);
			sendAESMasterKey(phoneNo, encryptMasterKey(masterKey));
			return true;
		}
		if (receiverPubKey.getX().bitLength() > 320) {
			Crypto.masterKey = generateAESKey(AES3);
			savePreference(Crypto.ECDHPART2PUBKEY, receiverPubKey.toString());
			savePreference(Crypto.AESKEYSIZE, AES3);
			savePreference(Crypto.MASTERKEY, masterKey);
			sendAESMasterKey(phoneNo, encryptMasterKey(masterKey));
			return true;
		} else {
			this.sendError(phoneNo, "ECDHPart2");
			return false;
		}
	}

	private String encryptMasterKey(String key) {
		BigInteger ecdhPart1Privatekey = new BigInteger(
				loadPreference(Crypto.ECDHPART1PRIVATEKEY), 16);
		Point ecdhPart2Pubkey = new Point(loadPreference(Crypto.ECDHPART2PUBKEY));
		Curve curve = new Curve(loadPreference(Crypto.CURVE));
		Encryption encryp = new Encryption(ecdhPart1Privatekey,
				ecdhPart2Pubkey, key, curve);
		return encryp.getCipher();
	}

	private void receivedAESKey(String phoneNo, String aesKeyCipher) {
		BigInteger ecdhPart2PrivateKey = new BigInteger(
				loadPreference(Crypto.ECDHPART2PRIVATEKEY), 16);
		Point ecdhPart1PubKey = new Point(loadPreference(Crypto.ECDHPART1PUBKEY));
		Curve curve = new Curve(loadPreference(Crypto.CURVE));
		Decryption decryp = new Decryption(ecdhPart1PubKey,
				ecdhPart2PrivateKey, aesKeyCipher, curve);
		savePreference(Crypto.MASTERKEY, decryp.getMessage());
		System.out
				.println("----------AES key received: " + decryp.getMessage());
		sendACKMsg(phoneNo, decryp.getMessage());
	}

	private boolean receivedACKmsg(String phoneNo, String msg) {
		String masterKey = AESDecrypt(msg, loadPreference(Crypto.MASTERKEY));
		if (masterKey.equals(loadPreference(Crypto.MASTERKEY))) {
			return true;
		}
		return false;
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

	private void sendACKMsg(String phoneNo, String masterKey) {
		String ACKmsg = Character.toString(ACK) + Character.toString(SEPTAG)
				+ AESEncrypt(masterKey, loadPreference(Crypto.MASTERKEY));
		sendSMS(phoneNo, ACKmsg);
	}

	private String generateAESKey(String AESkeysize) {
		BigInteger mt = BigInteger.probablePrime(Integer.valueOf(AESkeysize),
				new SecureRandom());
		return mt.toString(16);
	}

	private void sendECDHpart1(String phoneNo, String msg) {
		String sendMsg = Character.toString(ECDHPART1)
				+ Character.toString(SEPTAG) + msg;
		sendSMS(phoneNo, sendMsg);
	}

	private void sendECDHpart2(String phoneNo, String msg) {
		String sendMsg = Character.toString(ECDHPART2)
				+ Character.toString(SEPTAG) + msg;
		sendSMS(phoneNo, sendMsg);
	}

	private void sendAESMasterKey(String phoneNo, String masterKey) {
		String msg = Character.toString(AESINIT) + Character.toString(SEPTAG)
				+ masterKey;
		sendSMS(phoneNo, msg);
	}

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

	private void sendCantParseFeedBack(String phoneNo) {
		String msg = Character.toString(ERROR) + Character.toString(SEPTAG)
				+ Crypto.CANT_PARSE_MESSAGE_FEEDBACK;
		sendSMS(phoneNo, msg);
	}

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

	private void recordNewAccount(String phoneNo) {
		if (phoneNo.startsWith("0")) {
			phoneNo = phoneNo.substring(1);
			String temp = "+61";
			temp += phoneNo;
			phoneNo = temp;
		}
		db = new DBAdapter(context);
		db.open();
		Account acc = new Account(phoneNo, phoneNo,
				loadPreference(Crypto.MASTERKEY));
		db.deletOneAccount(acc);
		db.insertAccount(acc);
		db.close();
	}

	private void sendReceivedACKBroadcost(Context context) {
		Intent intent = new Intent("org.Stan.DataSMS.ReceivedACKSMS");
		context.sendBroadcast(intent);
	}

	private void sendReceivedBroadcost(Context context) {
		Intent intent = new Intent("org.Stan.DataSMS.ReceivedSMS");
		context.sendBroadcast(intent);
	}

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
