package gov.va.shamu.android.utilities;

import android.net.Uri;

import gov.va.shamu.android.provider.ShamuData.Alerts;

public abstract class AbstractAlert extends AbstractData {
	protected static final String TAG = AbstractAlert.class.getSimpleName();
    protected String alertStart;
    protected String elapsedTime;
    public static final String RED = "RED";
	public static final String GREEN = "GREEN";
	public static final String NOTIFICATION_RED_COUNT = "gov.va.shamu.android.utilities.NOTIFICATION_RED_COUNT";
	
	protected AbstractAlert(String job_code, String jle_id,
			String job_metadata_id, String short_description, String long_description,
			String last_completed, String status, String alert_start,
			String elapsed_time,String is_html, String job_result, String url) {

        super(job_code, jle_id, job_metadata_id, short_description, long_description,
                last_completed, status, is_html, job_result, url);

        this.alertStart = alert_start;
        cv.put(Alerts.ALERT_START_COLUMN_NAME, alertStart);
        this.elapsedTime = elapsed_time;
        cv.put(Alerts.ELAPSED_TIME_COLUMN_NAME, elapsedTime);
    }

    public String getAlertStart() { return alertStart;  }

    public String getElapsedTime() {
        return elapsedTime;
    }


    @Override
    protected String getWhereClause() {
        return  Alerts.JOB_CODE_COLUMN_NAME + " = '" + getJobCode() + "'";
    }

    @Override
    protected Uri getContentURI() {
        return Alerts.CONTENT_URI;
    }

}
