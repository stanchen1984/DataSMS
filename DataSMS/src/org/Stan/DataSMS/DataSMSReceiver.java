package org.Stan.DataSMS;

import org.Stan.DataSMS.Core.Core;
import org.Stan.db.DBAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class DataSMSReceiver extends BroadcastReceiver {
	private DBAdapter db;
	private Core core;

	@Override
	public void onReceive(Context context, Intent intent) {
		// -- get the SMS message passed in ---
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs;
		String str = "";
		String address = "";
		core = new Core(context);

		if (bundle != null) {
			// retrieve the SMS message received
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];

			// get SMS from pdu
			for (int i = 0; i < pdus.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			}

			// display the message
			for (SmsMessage currentMessage : msgs) {
				address = currentMessage.getOriginatingAddress();
				String msg = new String(currentMessage.getUserData());
				str += msg;
			}
			core.parseMsg(address, str);
		}

	}

}
