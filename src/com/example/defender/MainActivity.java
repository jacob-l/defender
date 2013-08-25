package com.example.defender;

import java.util.Timer;
import java.util.TimerTask;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private static final String PHONE_NUMBER = "";
	private static final String ACTIVATE = "activate";
	private static final String DEACTIVATE = "deactivate";
	private static final String INACTIVE = "inactive";

	private Timer autoUpdate;

	int delay = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.switchOn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				sendSms(PHONE_NUMBER, ACTIVATE);
			}
		});

		findViewById(R.id.switchOff).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				sendSms(PHONE_NUMBER, DEACTIVATE);
			}
		});
	}

	private void sendSms(String phoneNumber, String text) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber, null, text, null, null);
	}

	private SmsStatus getSmsStatus() {
		final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
		final String SORT = "date desc limit 1";
		Cursor cursor = getContentResolver().query(SMS_INBOX, null,
				"address = ?", new String[] { PHONE_NUMBER }, SORT);

		SmsStatus result = SmsStatus.UNKNOWN;

		if (cursor.moveToFirst()) {
			try {
				int index = cursor.getColumnIndex("body");
				String columnBody = cursor.getString(index);

				if (columnBody.contains(INACTIVE)) {
					result = SmsStatus.INACTIVE;
				} else {
					result = SmsStatus.ACTIVE;
				}
			} catch (Exception ex) {
			}
		}

		cursor.close();

		return result;
	}

	@Override
	public void onResume() {
		super.onResume();
		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						updateStatus();
					}
				});
			}
		}, 0, delay);
	}

	@Override
	public void onPause() {
		autoUpdate.cancel();
		super.onPause();
	}

	private void updateStatus() {
		SmsStatus status = getSmsStatus();
		ImageView iv = ((ImageView) findViewById(R.id.imageStatus));
		
		if (status == SmsStatus.ACTIVE) {
			iv.setImageResource(R.drawable.green_shield);
		} else if (status == SmsStatus.INACTIVE) {
			iv.setImageResource(R.drawable.red_shield);
		} else {
			iv.setImageResource(R.drawable.yellow_shield);
		}
	}
}
