package gov.va.shamu.android.utilities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import gov.va.shamu.android.provider.ShamuData.Alerts;

public class PersistableAlert extends AbstractAlert {

	public PersistableAlert(ContentResolver cr, String job_code, String jle_id,
			String job_metadata_id, String short_description, String long_description,
			String last_completed, String status, String alert_start,
			String elapsed_time, String is_html, String job_result, String url) {
		
		super(job_code, jle_id,job_metadata_id, short_description, long_description, last_completed, status, alert_start,
			elapsed_time, is_html, job_result, url);
		this.cr =  cr;
		
	}

	public PersistableAlert(ContentResolver cr, Cursor c) {
		super(
		c.getString(Alerts.getColumn(Alerts.JOB_CODE_COLUMN_NAME)),
		c.getString(Alerts.getColumn(Alerts.JLE_ID_COLUMN_NAME)),
		c.getString(Alerts.getColumn(Alerts.JOB_METADATA_ID_COLUMN_NAME)),
		c.getString(Alerts.getColumn(Alerts.SHORT_DESCRIPTION_COLUMN_NAME)),
		c.getString(Alerts.getColumn(Alerts.LONG_DESCRIPTION_COLUMN_NAME)),
		c.getString(Alerts.getColumn(Alerts.LAST_COMPLETED_COLUMN_NAME)),
		c.getString(Alerts.getColumn(Alerts.STATUS_COLUMN_NAME)),
	    c.getString(Alerts.getColumn(Alerts.ALERT_START_COLUMN_NAME)),
		c.getString(Alerts.getColumn(Alerts.ELAPSED_TIME_COLUMN_NAME)),
		c.getString(Alerts.getColumn(Alerts.IS_HTML_COLUMN_NAME)),
		c.getString(Alerts.getColumn(Alerts.JOB_RESULT_COLUMN_NAME)),
		c.getString(Alerts.getColumn(Alerts.URL_COLUMN_NAME)));
		this.cr = cr;
		persisted = true;
	}
}
//
// {"TEST_RED_GREEN":{"jle_id":4,"job_metadata_id":51,"short_description":"Test Job 1","last_completed"
// :"2013-08-05T15:33:29Z","status":"RED","alert_start":"2013-08-05T15:33:29Z","elapsed_time":"02h 23m 21s",
// "job_result":"EMAIL_RESULT_BELOW:\nSUBJECT: RED GREEN\nRED LIGHT!!!\nEMAIL_RESULT_ABOVE:",
// "url":"http://localhost:3000/job_log_entry/4"},
// "TEST_RED_GREEN2":{"jle_id":5,"job_metadata_id":52,"short_description":"Another silly Test Job","last_completed":"2013-08-05T15:33:35Z","status":"GREEN","alert_start":"2013-08-05T15:32:27Z","elapsed_time":"02h 24m 23s","job_result":"EMAIL_RESULT_BELOW:\nSUBJECT: RED GREEN\nGREEN_LIGHT!!!\nAnother line\nEMAIL_RESULT_ABOVE:","url":"http://localhost:3000/job_log_entry/5"}}