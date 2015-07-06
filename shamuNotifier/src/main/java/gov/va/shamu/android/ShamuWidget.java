package gov.va.shamu.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import java.util.List;
import java.util.Map;

import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.utilities.AbstractAlert;
import gov.va.shamu.android.utilities.InterestingAlert;
import gov.va.shamu.android.utilities.L;
//TODO This widget bombs out if there are zero alerts provisioned. Need to make the widget display Green if there are no alerts and we have done a pull.
public class ShamuWidget extends AppWidgetProvider {
	private final String TAG = this.getClass().getSimpleName();
    private RemoteViews views;
    private AppWidgetManager appWidgetManager;
    private ComponentName thisAppWidget;

    @Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
        L.d(TAG, "onReceive!!!");
        setup(context);
        setClickHandlers(context, views);
        new AsyncStateCheck(context).execute();
	}


	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		L.d(TAG, "onDisabled");
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
//        setup(context);
		L.d(TAG, "******************** onEnabled called ********************");
	}

    private void setup(Context context) {
        L.d(TAG, "Calling setup...");
        appWidgetManager = AppWidgetManager.getInstance(context);
        thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
        views = new RemoteViews(context.getPackageName(), R.layout.shamu_widget);
//        new AsyncStateCheck(context).execute();
    }

    @Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		L.d(TAG, "onUpdate");
		// final int N = appWidgetIds.length;
		ComponentName thisAppWidget = new ComponentName(
				context.getPackageName(), getClass().getName());

		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		// for (int i = 0; i < N; i++) {
		// L.d(TAG, "Looping in onUpdate " + i);
		// int appWidgetId = appWidgetIds[i];
        if (views == null) {
            setup(context);
        }
		setClickHandlers(context, views);
		// widget
		appWidgetManager.updateAppWidget(thisAppWidget, views);
		// appWidgetManager.updateAppWidget(appWidgetIds, views);
		// }
		L.d(TAG, "Done with onUpdate");
	}

	private void setClickHandlers(Context context, RemoteViews views) {
        L.d(TAG,"setClickHandlers called");
		Intent intent = new Intent(context, ShamuMainActivity.class);
		// intent.setComponent(thisAppWidget);
		// intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
		// // Identifies the particular widget...
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// Get the layout for the App Widget and attach an on-click listener
		// to the button

		views.setOnClickPendingIntent(R.id.shamu_widget_layout, pendingIntent); // Tell
																				// the
																				// AppWidgetManager
																				// to
																				// perform
																				// an
																				// update
																				// on
																				// the
																				// current
																				// app
	}

    public class AsyncStateCheck extends AsyncTask<Void, Void, Map<String, String>> {

        private final String TAG = this.getClass().getSimpleName();
        private Context context;
        private static final String SHAMU_STATE = "gov.va.shamu.android.ShamuWidget.AsyncStateCheck.shamuState";

        AsyncStateCheck(Context context) {
            this.context = context;
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            L.d(TAG, "Getting all preferences in doInBackground");
            final ContentResolver contentResolver = context.getContentResolver();
            Map<String, String> data = ShamuData.Preferences.getAllPreferences(contentResolver);

            if (data.containsKey(ShamuData.Preferences.SHAMU_URL_KEY)) {
                List<InterestingAlert> alerts = ShamuData.InterestingAlertsData.getAllInterestingAlerts(contentResolver);

                if (alerts.size() > 0) {
                    boolean redFound = false;
                    for (InterestingAlert interestingAlert : alerts) {
                        if (interestingAlert.isInteresting() && interestingAlert.getStatus().equals(AbstractAlert.RED)) {
                            redFound = true;
                            break;
                        }
                    }

                    if (redFound) {
                        data.put(SHAMU_STATE, AbstractAlert.RED);
                    }
                    else {
                        data.put(SHAMU_STATE, AbstractAlert.GREEN);
                    }
                }
            }

            return data;
        }

        @Override
        protected synchronized void onPostExecute(Map<String, String> data) {
//            setup(context, false);
            String urlString = data.get(ShamuData.Preferences.SHAMU_URL_KEY);
            L.d(TAG, "URL pref in postExecute = " + urlString);

            if (urlString != null && ! urlString.equals("") ) {
                L.d(TAG, "setting UI .....................");
                ConnectivityManager check = (ConnectivityManager)this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                boolean connected = false;
                NetworkInfo ni = check.getActiveNetworkInfo();
                if (ni != null)
                  connected = ni.isConnected();

                if (data.get(SHAMU_STATE).equals(AbstractAlert.GREEN)) {
                    L.d(TAG, "the shamu state is getting set to green");
                    views.setImageViewResource(R.id.alert_widget, R.drawable.green);
                    views.setTextColor(R.id.text_view_widget, 0xFF008000);//dark green
                } else {
                    L.d(TAG, "the shamu state is getting set to red");
                    views.setImageViewResource(R.id.alert_widget, R.drawable.red);
                    views.setTextColor(R.id.text_view_widget, Color.RED);
                }

                boolean runningService = Boolean.parseBoolean(data.get(ShamuData.Preferences.SHAMU_SERVICE_STATE));
                L.d(TAG, "the running state is " + runningService);

                if (runningService) {
                    if (connected) {
                        views.setImageViewResource(R.id.polling_widget, R.drawable.polling_running);
                    } else {
                        views.setImageViewResource(R.id.polling_widget,R.drawable.polling_waiting);
                    }
                } else {
                    views.setImageViewResource(R.id.polling_widget, R.drawable.polling_dead3);
                }

                final boolean bell = Boolean.parseBoolean(data.get(ShamuData.Preferences.SETTINGS_AUDIBLE));
                L.d(TAG, "the bell is " + bell);
                views.setImageViewResource(R.id.widget_bell, bell ? R.drawable.bell_ring : R.drawable.bell_no_ring);

                final boolean vibrate = Boolean.parseBoolean(data.get(ShamuData.Preferences.SETTINGS_VIBRATE));
                L.d(TAG, "the vibrate is " + vibrate);
                views.setImageViewResource(R.id.widget_phone, vibrate ? R.drawable.phone_vibrate : R.drawable.phone_no_vibrate);

                final boolean voice = Boolean.parseBoolean(data.get(ShamuData.Preferences.SETTINGS_ANNOUNCE));
                L.d(TAG, "the speak is " + voice);
                views.setImageViewResource(R.id.widget_speak, voice ? R.drawable.speak: R.drawable.speak_not);
            }

            appWidgetManager.updateAppWidget(thisAppWidget, views);
        }
    }
}
