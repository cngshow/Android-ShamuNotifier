package gov.va.shamu.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.provider.ShamuData.Preferences;
import gov.va.shamu.android.provider.ShamuNotifierContentProvider;
import gov.va.shamu.android.utilities.AlertTracker;
import gov.va.shamu.android.utilities.L;
import gov.va.shamu.android.utilities.ReportTracker;

public class ShamuUpdaterService extends Service {

	private static final String TAG = ShamuUpdaterService.class.getSimpleName();
	private ExecutorService worker;
	private ShamuNotifierApplication shamu;
	private static volatile int pollingInterval = 600000;
	private static final String RESTFUL_PATH_TO_JSON_ALERTS = "/mobile_trackable_list_json";
	private static final String RESTFUL_PATH_TO_JSON_REPORTS = "/mobile_nontrackable_list_json";

	// to do pull from prefs
	//private static String urlString = "http://192.168.0.2:3000/mobile_trackable_list_json";
	
	static {
		L.d(TAG, "ShamuUpdaterService loaded");
	}

	public ShamuUpdaterService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.shamu = (ShamuNotifierApplication) getApplication(); 
		// this.updater = new Updater(); //Build updater thread class
		L.d(TAG, "onCreated");
		// get from configuration!
		// http://localhost:3000/android_trackable_list_json
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startID) {
		L.d(TAG, "Shamu service recieved start command!");
		if (shamu.isServiceRunning()) {
			return Service.START_STICKY;
		}
        //the intent is null if this is called after swiping the app away!!
        String action = "";
        if (intent != null ) {
            action = intent.getAction();
        }

		boolean continueStart = false;
		if ( (action != null) && (action.equals(BaseActivity.PREFERENCE_SERVER_TOGGLE) ||
                action.equals(BaseActivity.OUT_OF_SYNC_SERVER_TOGGLE) ||
				action.equals(BootReceiver.BOOT_RECIEVER_START_SERVICE)	)) {
			continueStart = true;
		} else {
            //we believe andriod might attempt a sticky start
			String serviceActive = ShamuData.Preferences.getPreference(getContentResolver(), ShamuData.Preferences.SHAMU_SERVICE_STATE);
			L.d(TAG, ShamuData.Preferences.SHAMU_SERVICE_STATE + " = " + serviceActive);
			continueStart = Boolean.valueOf(serviceActive);			
		}		
		if (continueStart) {
			Map<String, String> prefs = Preferences.getAllPreferences(getContentResolver());
			String urlString = prefs.get(Preferences.SHAMU_URL_KEY);
			if (urlString == null) {
				L.w(TAG, "Shamu's updater service recieved a start request before the configuration have been set by the user.");
				L.w(TAG, "This is likely a start request from the network receiver and if this is the case this warning is ignorable.");
				L.w(TAG, "The request to start is being ignored!");
				stopSelf();
				return Service.START_NOT_STICKY;
			}
			super.onStartCommand(intent, flags, startID);
			shamu.setServiceRunning(true);
			worker = Executors.newSingleThreadExecutor();
			worker.submit(new RestfulShamuQuery());
			L.d(TAG, "onStarted");
			// }
			return Service.START_STICKY;
		} else {
			L.d(TAG, "Something told me to start, but my current state is recorded as stopped.  I am stopping myself!");
			stopSelf();
		}
		return Service.START_STICKY_COMPATIBILITY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		shamu.setServiceRunning(false);
		L.d(TAG, "onDestroyed");
	}

	class RestfulShamuQuery implements Runnable {

		private final String TAG = RestfulShamuQuery.class.getSimpleName();
		private boolean trouble = false;

		@Override
		public void run() {
            final boolean serviceRunning = shamu.isServiceRunning();
            L.w(TAG, "service running is " + serviceRunning);
            while (shamu.isServiceRunning()) {
				try {
					// Do all work in here!
					String pollingIntervalString =  Preferences.getPreference(ShamuUpdaterService.this.getContentResolver(),Preferences.SHAMU_POLLING_INTERVAL_KEY);
					if (pollingIntervalString != null)
					  pollingInterval = Integer.parseInt(pollingIntervalString)*1000*60;
					refreshAlerts(ShamuUpdaterService.this);
					L.d(TAG, "Service is running!");
					try {
						L.d(TAG, "Sleeping for " + pollingInterval + " milliseconds");
						Thread.sleep(pollingInterval);
					} catch (InterruptedException e) {
						L.w(TAG, "Sleep in service died prematurely!", e);
					}
					// last line!

				} catch (Exception e) {
					shamu.setServiceResult(e.toString());
					trouble = true;
					L.e(TAG, "Abnormal failure in the Shamu Updater!", e);
				} finally {
					// cleanup, if any
				}
			}
			L.d(TAG, "Run method has terminated!");
			if (!trouble)
				shamu.setServiceResult(ShamuUpdaterService.this
						.getString(R.string.service_graceful_death));
		}

	}

	// Called from configuration. Alert user in configuration if String
	// does not find SHAMU!//If it is a MalformedURLException force the user to try again.
	public static void connectToSHAMU(String urlString)
			throws IOException {
		HttpURLConnection urlConnection = null;
		URL urlToSHAMU = new URL(urlString);
		try {
			urlConnection = (HttpURLConnection) urlToSHAMU.openConnection();
			urlConnection.getInputStream();
		} finally {
			urlConnection.disconnect();
		}
	}

    //todo remove me!
	//Called from service and on pull down of listview for alerts
	public static synchronized void refreshAlerts(Context context) throws IOException {
        String result = refresh(context, RESTFUL_PATH_TO_JSON_ALERTS);
        if (result != null) {
            AlertTracker.addShamuJSONUpate(result, context);
        }
	}

	//Called from service and on pull down of listview for reports
	public static synchronized void refresh(ShamuNotifierContentProvider.UriType refreshType, Context context) throws IOException {
        String json_path = null;

        if (refreshType == ShamuNotifierContentProvider.UriType.ALERT) {
            json_path = RESTFUL_PATH_TO_JSON_ALERTS;
        }
        else if (refreshType == ShamuNotifierContentProvider.UriType.REPORT) {
            json_path = RESTFUL_PATH_TO_JSON_REPORTS;
        }
        else {
            L.e(TAG, "IllegalArgumentException thrown in refresh for " + refreshType + ".");
            throw new IllegalArgumentException("The refresh method must take either the ALERT or REPORT UriType from ShamuNotifierContentProvider as its first argument.");
        }

        //
        String result = refresh(context, json_path);

        if (result != null) {
            if (refreshType == ShamuNotifierContentProvider.UriType.ALERT) {
                AlertTracker.addShamuJSONUpate(result, context);
            }
            else if (refreshType == ShamuNotifierContentProvider.UriType.REPORT) {
                ReportTracker.addShamuJSONUpate(result, context);
            }
	    }
    }

	public static String fetchJSONData(Context context, String webServicePath) throws IOException {
		return refresh(context,webServicePath);
	}

	//Called from service and on pull down of listview
	private static synchronized String refresh(Context context, String webServicePath) throws IOException {
		ShamuNotifierApplication shamu = (ShamuNotifierApplication)context.getApplicationContext();
		boolean networkAvailability = shamu.isNetworkAvailable();

//		shamu.notifyWidgetOfServiceRunState(shamu.isServiceRunning(), networkAvailability);

        if (!networkAvailability) {
			L.i(TAG, "Network is not available ignoring refresh!");
            return null;
		}
		StringBuilder result = new StringBuilder();
		//Map<String, String> prefs = Preferences.getAllPreferences(context.getContentResolver());
		String urlString = Preferences.getPreference(context.getContentResolver(),Preferences.SHAMU_URL_KEY);
		if (urlString == null ) {
			L.w(TAG, "Refresh called with null url preference");
            return null;
		}
		urlString = "http://" + urlString + webServicePath;
		URL urlToSHAMU = new URL(urlString);
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) urlToSHAMU.openConnection();
			BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String line = null;
			while ((line = r.readLine()) != null) {
				result.append(line);
			}
		} finally {
			urlConnection.disconnect();
		}
		L.v(TAG, result.toString());
		return result.toString();
	}
}
