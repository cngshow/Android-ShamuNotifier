package gov.va.shamu.android;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.utilities.L;

public class BootReceiver extends BroadcastReceiver {

	public final String TAG = "BootReceiver";
	public static final String BOOT_RECIEVER_START_SERVICE = "gov.va.shamu.android.BOOT_RECIEVER_START_SERVICE";

	@Override
	public void onReceive(Context context, Intent intent) {
		L.i(TAG, "SHAMU BOOT RECEIVER onReceive");
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			L.i(TAG, "Boot completed");
		}
		//Service started? 
		ContentResolver cr = context.getContentResolver();
		String startedString = ShamuData.Preferences.getPreference(cr, ShamuData.Preferences.SHAMU_SERVICE_STATE);
		L.d(TAG, "Preference key for " + ShamuData.Preferences.SHAMU_SERVICE_STATE + " = " + startedString);
		boolean startService = Boolean.valueOf(startedString);
		if (startService) {
			L.d(TAG, "Bootreceiver is starting the service");
			Intent serviceIntent = new Intent(context, ShamuUpdaterService.class);
			serviceIntent.setAction(BOOT_RECIEVER_START_SERVICE);
			context.startService(intent);
		} else {
			L.d(TAG, "Boot Reciever is NOT starting the service!");
		}
		L.d(TAG, "onReceived");
	}
}