package org.Stan.DataSMS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.Stan.DataSMS.Core.Core;
import org.Stan.db.Account;
import org.Stan.db.Message;
import org.Stan.db.DBAdapter;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * Conversation activity is for talking communication between two.
 */

public class Conversation extends ListActivity {
	private static final int CONVERSATIONMENUDELETE = 1;
	private static final int CONVERSATIONMENUABOUT = 2;
	private static final String MESSAGEVIEW = "messageView";
	private static final String TIMEVIEW = "timeView";
	private Button sendButton = null;
	private DBAdapter db;
	private static String accountName;
	private ArrayList<Message> msgList;
	private EditText msgView;
	private SMSNotify smsNotify;
	private Core core;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation);
		init();
	}

	private void init() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(Core.NOTIFICATION_ID);
		msgView = (EditText) findViewById(R.id.conversationMsgText);
		accountName = this.getIntent().getStringExtra(Account.NUMBER);
		sendButton = (Button) findViewById(R.id.conversationSendButton);
		sendButton.setOnClickListener(new sendButtonClick());
		registerReceiver();
		updateList();
	}

	private void updateList() {
		msgList = new ArrayList<Message>();
		msgList = readMsgList();
		setListAdapter(getList());
	}

	private void registerReceiver() {
		smsNotify = new SMSNotify();
		IntentFilter intentFilter = new IntentFilter(
				"org.Stan.DataSMS.ReceivedSMS");
		registerReceiver(smsNotify, intentFilter);
	}

	private SimpleAdapter getList() {
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		Iterator<Message> i = msgList.iterator();
		HashMap<String, String> map;
		while (i.hasNext()) {
			map = new HashMap<String, String>();
			Message msg = i.next();
			String msgView = "";
			String timeView = "";
			if (msg.getFromOrTo().equals(Message.TO)) {
				msgView += "Me: ";
			} else {
				msgView += msg.getNumber() + ": ";
			}
			msgView += msg.getMsg();
			timeView += msg.getTime();
			map.put(MESSAGEVIEW, msgView);
			map.put(TIMEVIEW, timeView);
			list.add(map);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, list,
				R.layout.conversation_message_list, new String[] { MESSAGEVIEW,
						TIMEVIEW }, new int[] {
						R.id.conversation_message_list_msg_text,
						R.id.conversation_message_list_time_text });
		return simpleAdapter;
	}

	class ConversationAdapter extends ArrayAdapter<String> {
		public ConversationAdapter(Context context, String[] names) {
			super(context, R.layout.conversation_message_list, names);
		}
	}

	public ArrayList<Message> readMsgList() {
		ArrayList<Message> mg = new ArrayList<Message>();
		db = new DBAdapter(this);
		db.open();
		Account acc = db.getAccount(accountName);
		mg = db.getOneContactMsgs(acc);
		db.close();
		return mg;
	}

	class sendButtonClick implements OnClickListener {
		public sendButtonClick() {

		}

		public void onClick(View arg0) {
			String msg = msgView.getText().toString();
			sendSMS(accountName, msg);
			msgView.setText("");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONVERSATIONMENUDELETE, 1, R.string.homeMenuListDelete);
		menu.add(0, CONVERSATIONMENUABOUT, 2, R.string.homeMenuListAbout);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == CONVERSATIONMENUDELETE) {
			// to deleteview
			deleteAllMessage();
		} else if (item.getItemId() == CONVERSATIONMENUABOUT) {
			// to compose view
			showAbout();
		}
		return super.onOptionsItemSelected(item);
	}

	private void deleteAllMessage() {
		System.out.println("get AccountName: " + accountName);
		db = new DBAdapter(this);
		db.open();
		Account acc = new Account(accountName, accountName, null);
		db.deletOneContactMsg(acc);
		db.close();
		updateList();
	}

	private void showAbout() {
		updateList();
	}

	private void sendSMS(String phoneNo, String msg) {
		core = new Core(this);
		if (core.composeMsg(phoneNo, msg)) {
			Toast.makeText(this, "Message has been send out",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Error occure", Toast.LENGTH_SHORT).show();
		}
		updateList();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(smsNotify);
		super.onDestroy();
	}

	class SMSNotify extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			updateList();
		}
	}
}
