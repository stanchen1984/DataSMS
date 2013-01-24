package org.Stan.DataSMS;

import java.util.ArrayList;
import org.Stan.DataSMS.Core.Core;
import org.Stan.db.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DataSMSSender extends Activity {
	private static final String TEMPMSG = "tempMsg";
	private static final String TEMPPHONENO = "tempPhoneNo";
	private static ArrayList<Account> accountList = new ArrayList<Account>();
	Button sendMsgButton = null;
	EditText txtPhoneNo = null;
	EditText txtMsg = null;
	DBAdapter db = null;
	private Core core;
	private ACKNotify aCKNotify;
	private ProgressDialog waitingDialog;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);
		sendMsgButton = (Button) findViewById(R.id.sendMsgButton);
		txtPhoneNo = (EditText) findViewById(R.id.phoneNumber);
		txtMsg = (EditText) findViewById(R.id.messageContext);
		core = new Core(this);
		sendMsgButton.setOnClickListener(new sendMsgButtonClick(this));
	}

	class sendMsgButtonClick implements OnClickListener {
		Context context;

		public sendMsgButtonClick(Context context) {
			this.context = context;
		}

		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			String phoneNo = txtPhoneNo.getText().toString();
			String msg = txtMsg.getText().toString();

			if (phoneNo.length() > 0) {
				if (!core.composeMsg(phoneNo, msg)) {
					savePreference(TEMPMSG, msg);
					savePreference(TEMPPHONENO, phoneNo);
					txtPhoneNo.setText("");
					txtMsg.setText("");
					registerACKReceiver();
					waitingDialog = ProgressDialog.show(context, "",
							"Frist time init process....", true);
					waitingDialog.show();
				} else {
					txtPhoneNo.setText("");
					txtMsg.setText("");
					Toast.makeText(context, "Message has been send out",
							Toast.LENGTH_SHORT).show();
				}
			} else
				Toast.makeText(context, "Please enter a phone number",
						Toast.LENGTH_SHORT).show();
		}

	}

	private void savePreference(String key, String value) {
		SharedPreferences sharedPreferences = getSharedPreferences(key, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	private String loadPreference(String key) {
		SharedPreferences sharedPreferences = getSharedPreferences(key, 0);
		String result = sharedPreferences.getString(key, "");
		return result;
	}

	private void sendSMSAfterACK() {
		unregisterReceiver(aCKNotify);
		core.composeMsg(loadPreference(TEMPPHONENO), loadPreference(TEMPMSG));
		txtPhoneNo.setText("");
		txtMsg.setText("");
		waitingDialog.dismiss();
		Toast.makeText(this, "Message has been send out", Toast.LENGTH_SHORT)
				.show();
	}

	private void registerACKReceiver() {
		aCKNotify = new ACKNotify();
		IntentFilter intentFilter = new IntentFilter(
				"org.Stan.DataSMS.ReceivedACKSMS");
		registerReceiver(aCKNotify, intentFilter);
	}

	class ACKNotify extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			System.out.println("ACKReceiver has been received.");
			sendSMSAfterACK();
		}
	}

}