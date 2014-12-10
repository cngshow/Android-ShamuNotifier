package gov.va.shamu.android.utilities;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.va.shamu.android.BaseActivity;
import gov.va.shamu.android.R;
import gov.va.shamu.android.ShamuMainActivity;
import gov.va.shamu.android.ShamuNotifierApplication;
import gov.va.shamu.android.ShamuWidget;
import gov.va.shamu.android.provider.ShamuData;

public class AlertTracker {

	private static final String TAG = AlertTracker.class.getSimpleName();

    private AlertTracker() {}

    public static void addShamuJSONUpate(String json, Context context) {
		// L.d(TAG, "Calling the delete!");
		// cr.delete(Alerts.CONTENT_URI, null, null);
		ContentResolver cr = context.getContentResolver();
		L.d(TAG, "JSON update: " + json);
		JSONObject jo = null;
		List<InterestingAlert> previous = null;
		List<PersistableAlert> current = null;
		try {
			jo = new JSONObject(json);
			JSONArray trackables = jo.names();
			int length = trackables.length();
			previous = ShamuData.InterestingAlertsData.getAllInterestingAlerts(cr);
			current = new ArrayList<PersistableAlert>();
			for (int i = 0; i < length; i++) {
				L.d(TAG, "Trackable " + i + " = " + trackables.getString(i));
				String trackable_name = trackables.getString(i);
				JSONObject currentAlert = jo.getJSONObject(trackable_name);
				String jle_id = currentAlert.getString("jle_id");
				String job_metadata_id = currentAlert.getString("job_metadata_id");
				String short_description = currentAlert.getString("short_description");
				String long_description = currentAlert.getString("long_description");
				String last_completed = currentAlert.getString("last_completed");
				String status = currentAlert.getString("status");
				String alert_start = currentAlert.getString("alert_start");
				String elapsed_time = currentAlert.getString("elapsed_time");
				String is_html = currentAlert.getString("is_html");
				String job_result = currentAlert.getString("job_result");
				String url = currentAlert.getString("url");
				PersistableAlert alertStruct = new PersistableAlert(cr, trackable_name, jle_id,
						job_metadata_id, short_description, long_description, last_completed,
						status, alert_start, elapsed_time, is_html, job_result,
						url);
				//L.d(TAG, alertStruct.toString());
				current.add(alertStruct);
			}
		} catch (JSONException e) {
			L.wtf(TAG, "SHAMU gave us invalid JSON!!", e);
		}

		// delete these guys
		Set<InterestingAlert> deleteUs = new HashSet<InterestingAlert>(previous);
		deleteUs.removeAll(current);
		for (InterestingAlert a : deleteUs) {
			a.delete();
		}
		for (PersistableAlert a : current) {
			a.insertOrUpdate();
		}

		notifyAlerts(previous, current, context);

        ((ShamuNotifierApplication)context.getApplicationContext()).notifyWidgetOfChange();

//		notifyWidget(ShamuData.InterestingAlertsData.getAllInterestingAlerts(cr), context);

	} // end addShamuJSONUpate

	private static void notifyAlerts(List<InterestingAlert> previous,
			List<PersistableAlert> current, Context context) {
		previous.retainAll(current);
		Set<PersistableAlert> notify = new HashSet<PersistableAlert>();
		Map<String, InterestingAlert> lookUpPrevHelper = new HashMap<String, InterestingAlert>();
		int numRedsCurrent = 0;
		int numAlertsCleared = 0;
		for (InterestingAlert a : previous) {
			lookUpPrevHelper.put(a.getJobCode(), a);
		}
		// if (isServiceStarted) {
		for (PersistableAlert a : current) {
			String jc = a.getJobCode();
			InterestingAlert prev = lookUpPrevHelper.get(jc);
			if ((prev != null) && (prev.isInteresting())) {
				String prevStatus = (prev == null) ? PersistableAlert.GREEN
						: prev.getStatus();
				String currStatus = a.getStatus();
				if (currStatus.equalsIgnoreCase(PersistableAlert.RED))
					numRedsCurrent++;
				if (!prevStatus.equals(currStatus)) {
					if (currStatus.equals(PersistableAlert.GREEN))
						numAlertsCleared++;
					notify.add(a);
				}
			}
		}
		if (!notify.isEmpty()) {
			L.d(TAG, "Notifying on: " + notify);
			Builder notificationBuilder = new Notification.Builder(context);
			Bitmap icon = null;
			int smallIcon = 0;
			String message = null;
            String allCleared = context.getString(R.string.notification_all_clear);
            String redPlural = context.getString(R.string.notification_reds_plural);
            String redSingular = context.getString(R.string.notification_reds_singular);
            String clearedPlural = context.getString(R.string.notification_cleared_plural);
            String clearedSingular = context.getString(R.string.notification_cleared_singular);
			if (numRedsCurrent != 0) {
				icon = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.error);
				smallIcon = R.drawable.red_circle;
				message = numRedsCurrent + " " + redPlural + " ";
				if (numRedsCurrent == 1)
					message = redSingular + " ";
				if (numAlertsCleared > 1) {
					message += numAlertsCleared + " " + clearedPlural;
				} else if (numAlertsCleared == 1) {
					message += clearedSingular;
				}
			} else {
				icon = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.clear_green_button);
				message = allCleared;
				smallIcon = R.drawable.green_circle;
			}
			Intent notificationIntent = new Intent(context,
					ShamuMainActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 1,
					notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationBuilder.setLargeIcon(icon);
			notificationBuilder.setSmallIcon(smallIcon);
			notificationBuilder.setWhen(System.currentTimeMillis());
			notificationBuilder.setTicker(message);
			notificationBuilder.setAutoCancel(true);
			notificationBuilder.setContentTitle(context
					.getString(R.string.app_name));
			notificationBuilder.setContentText(message);
			notificationBuilder.setContentIntent(contentIntent);

            ContentResolver cr = context.getContentResolver();
            String vibrate = ShamuData.Preferences.getPreference(cr, ShamuData.Preferences.SETTINGS_VIBRATE);

            if (Boolean.TRUE.toString().equals(vibrate)){
                notificationBuilder.setVibrate(new long[] { 0, 1000, 500, 2000, 500, 1000 });
            }

            String audible = ShamuData.Preferences.getPreference(cr, ShamuData.Preferences.SETTINGS_AUDIBLE);

            if (Boolean.TRUE.toString().equals(audible)) {
                notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            }

			Notification n = notificationBuilder.build();
			nm.notify(TAG, 1, n);
		} else {
			L.d(TAG, "Nothing to notify, no state change!");
		}

        String announce = ShamuData.Preferences.getPreference(context.getContentResolver(), ShamuData.Preferences.SETTINGS_ANNOUNCE);
        if (Boolean.TRUE.toString().equals(announce)) {
            //last we speak to the user if the speaking preference is set
            speakOnAlerts(notify, context);
		}
	}

    private static void speakOnAlerts(Set<PersistableAlert> notify, Context context) {
        //first check configuration to see if we are being chatty...
        ShamuNotifierApplication shamu = (ShamuNotifierApplication)context.getApplicationContext();
        for (PersistableAlert alert : notify) {
            String shortDescription = alert.getShortDescription();
            String status = alert.getStatus();
            String chat = status.equals(AbstractAlert.GREEN) ? context.getString(R.string.speak_green) : context.getString(R.string.speak_red);
            chat = chat.replace(":ALERT:", shortDescription);
            shamu.speak(chat);
        }
    }

    public static synchronized void synchronizeWidget(Context c) {
		notifyWidget(ShamuData.InterestingAlertsData.getAllInterestingAlerts(c.getContentResolver()),c);
	}
	
	public static synchronized void synchronizeWidget( List<InterestingAlert> current, Context c) {
		notifyWidget(current, c);
	}
	
	private static void notifyWidget( List<InterestingAlert> current, Context context) {
	//	boolean isServiceStarted = ((ShamuNotifierApplication)context.getApplicationContext()).isServiceRunning();
	//	((ShamuNotifierApplication)context.getApplicationContext()).notifyWidgetOfServiceRunState(isServiceStarted);
		Intent widgetIntent = new Intent(context, ShamuWidget.class);
		int numReds = 0;
		for (InterestingAlert maybeInteresting : current) {
			if ((maybeInteresting != null) && maybeInteresting.isInteresting() && maybeInteresting.getStatus().equalsIgnoreCase(AbstractAlert.RED)) {
				numReds++;
			}
		}
		L.d(TAG, "Notifying widget number reds is " + numReds);
		widgetIntent.putExtra(PersistableAlert.NOTIFICATION_RED_COUNT, numReds);
		context.sendBroadcast(widgetIntent);
	}
}
