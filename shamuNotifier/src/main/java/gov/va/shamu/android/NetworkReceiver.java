package gov.va.shamu.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import gov.va.shamu.android.utilities.L;

public class NetworkReceiver extends BroadcastReceiver {
	public static final String TAG = "NetworkReceiver";
	public static final String NETWORK_STATE = "gov.va.shamu.android.NETWORK_STATE";

	@Override
	public void onReceive(Context context, Intent intent) {

		boolean isNetworkDown = intent.getBooleanExtra(
				ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
//		((ShamuNotifierApplication) context.getApplicationContext()).notifyWidgetOfServiceRunState();
		((ShamuNotifierApplication) context.getApplicationContext()).notifyWidgetOfChange();

		if (isNetworkDown) {
			L.d(TAG, "onReceive: NOT connected");

		} else {
			L.d(TAG, "onReceive: connected");

		}
	}
}
