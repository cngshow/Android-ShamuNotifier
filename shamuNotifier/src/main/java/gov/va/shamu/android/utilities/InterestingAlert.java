package gov.va.shamu.android.utilities;

import android.content.ContentResolver;
import android.database.Cursor;

import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.provider.ShamuData.InterestingAlertsData;

public class InterestingAlert extends AbstractAlert {
	
	private boolean isInteresting;

	public InterestingAlert(ContentResolver cr, Cursor c) {
		super(
		c.getString(InterestingAlertsData.getColumn(InterestingAlertsData.JOB_CODE_COLUMN_NAME)),
		c.getString(InterestingAlertsData.getColumn(InterestingAlertsData.JLE_ID_COLUMN_NAME)),
		c.getString(InterestingAlertsData.getColumn(InterestingAlertsData.JOB_METADATA_ID_COLUMN_NAME)),
		c.getString(InterestingAlertsData.getColumn(InterestingAlertsData.SHORT_DESCRIPTION_COLUMN_NAME)),
		c.getString(InterestingAlertsData.getColumn(InterestingAlertsData.LONG_DESCRIPTION_COLUMN_NAME)),
		c.getString(InterestingAlertsData.getColumn(InterestingAlertsData.LAST_COMPLETED_COLUMN_NAME)),
		c.getString(InterestingAlertsData.getColumn(InterestingAlertsData.STATUS_COLUMN_NAME)),
	    c.getString(InterestingAlertsData.getColumn(InterestingAlertsData.ALERT_START_COLUMN_NAME)),
		c.getString(InterestingAlertsData.getColumn(InterestingAlertsData.ELAPSED_TIME_COLUMN_NAME)),
		c.getString(InterestingAlertsData.getColumn(InterestingAlertsData.IS_HTML_COLUMN_NAME)),
		c.getString(InterestingAlertsData.getColumn(ShamuData.InterestingAlertsData.JOB_RESULT_COLUMN_NAME)),
		c.getString(ShamuData.InterestingAlertsData.getColumn(ShamuData.InterestingAlertsData.URL_COLUMN_NAME)));
		String interesting = c.getString(ShamuData.InterestingAlertsData.getColumn(ShamuData.InterestingAlertsData.IS_INTERESTING_COLUMN_NAME));
		isInteresting = interesting.equalsIgnoreCase(Boolean.TRUE.toString());
		this.cr = cr;
	}
		
	public boolean isInteresting() {
		return isInteresting;
	}
}
