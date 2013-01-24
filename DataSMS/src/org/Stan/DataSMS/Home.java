package org.Stan.DataSMS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import org.Stan.db.*;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Home extends ListActivity {
	private static final String PHONENUMBER = "phoneNo";
	private static final int HOMEMENULISTDELETE = 1;
	private static final int HOMEMENULISTCOMPOSE = 2;
	private static final int HOMEMENULISTSETTINGS = 3;
	private static final int HOMEMENULISTABOUT = 4;
	private static final String LASTMSG = "lastMessage";
	private Button composeButton = null;
	private DBAdapter db;
	private ArrayList<Account> accountList;
	private ArrayList<Message> msgList;
	private HashMap<String, String> viewList;
	private SMSNotify smsNotify;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		updateList();
		receivedNewSMSUpdateList();
		composeButton = (Button) findViewById(R.id.homeComposeButton);
		composeButton.setOnClickListener(new ComposeButtonClick());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, HOMEMENULISTDELETE, 1, R.string.homeMenuListDelete);
		menu.add(0, HOMEMENULISTCOMPOSE, 2, R.string.homeMenuListCompose);
		menu.add(0, HOMEMENULISTSETTINGS, 3, R.string.homeMenuListSettings);
		menu.add(0, HOMEMENULISTABOUT, 4, R.string.homeMenuListAbout);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		updateList();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(smsNotify);
		super.onDestroy();
	}

	// select a menu opiton
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == HOMEMENULISTDELETE) {
			deleteAllRecords();
			// to deleteview
		} else if (item.getItemId() == HOMEMENULISTCOMPOSE) {
			// to compose view
			composeNewMsg();
		} else if (item.getItemId() == HOMEMENULISTSETTINGS) {
			showtime();
			// to setting view
		} else if (item.getItemId() == HOMEMENULISTABOUT) {
			showAbout();
		}
		return super.onOptionsItemSelected(item);
	}

	private SimpleAdapter homeList() {
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		Iterator<Account> i = accountList.iterator();
		HashMap<String, String> map;
		while (i.hasNext()) {
			map = new HashMap<String, String>();
			Account acc = i.next();
			Iterator<Message> j = msgList.iterator();
			Message msg = new Message();
			while (j.hasNext()) {
				Message tmsg = j.next();
				if ((tmsg.getNumber().equals(acc.getName()))
						&& Long.valueOf(tmsg.getMillionTime()) > Long
								.valueOf(msg.getMillionTime())) {
					msg = tmsg;
				}
			}
			map.put(Account.NUMBER, acc.getName());
			map.put(LASTMSG, msg.getMsg());
			list.add(map);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, list,
				R.layout.home_account_list, new String[] { Account.NUMBER,
						LASTMSG }, new int[] { R.id.home_list_account_name,
						R.id.home_list_account_message });
		return simpleAdapter;
	}

	// click a contact thread
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		HashMap<String, String> sltMap = (HashMap<String, String>) l
				.getItemAtPosition(position);
		Intent intent = new Intent(this, new Conversation().getClass());
		intent.putExtra(Account.NUMBER, sltMap.get(Account.NUMBER));
		startActivity(intent);
		super.onListItemClick(l, v, position, id);
	}

	// update contact list
	private void updateList() {
		if (db == null) {
			db = new DBAdapter(this);
		}
		db.open();
		accountList = db.getAllAccount();
		msgList = db.getAllMsgs();
		db.close();
		setListAdapter(homeList());
	}

	// show about information
	private void showAbout() {
		Log.d("SIP HOME", "Good");
		Intent intent = new Intent(this, new About().getClass());
		startActivity(intent);
	}

	class ComposeButtonClick implements OnClickListener {
		public ComposeButtonClick() {
		}

		public void onClick(View arg0) {
			composeNewMsg();
		}

	}

	private void composeNewMsg() {
		Intent intent = new Intent(this, new DataSMSSender().getClass());
		startActivity(intent);
	}

	private void receivedNewSMSUpdateList() {
		smsNotify = new SMSNotify();
		IntentFilter intentFilter = new IntentFilter(
				"org.Stan.DataSMS.ReceivedSMS");
		registerReceiver(smsNotify, intentFilter);
	}

	private void deleteAllRecords() {
		db = new DBAdapter(this);
		db.open();
		db.deletAllAccount();
		db.deletAllMsg();
		db.close();
		updateList();
	}

	class SMSNotify extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			updateList();
		}
	}

	private void showtime() {
		System.out.println("getCurrentTime: "
				+ new CurrentTime().getCurrentTime());
		String milliTime = new CurrentTime().getCurrentMillionTime();
		long te = Long.valueOf(milliTime);
		System.out.println("getCurrentMillionTime: " + te);
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
