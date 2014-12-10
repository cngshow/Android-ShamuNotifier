package gov.va.shamu.android.provider;

public interface AlertDataColumns extends CommonDataColumns {
	
	public static final String ALERT_START_COLUMN_NAME = "alert_start";
	public static final String ELAPSED_TIME_COLUMN_NAME = "elapsed_time";
	public static final String[] ALL_COLUMNS = new String[] {
		    _ID, JOB_CODE_COLUMN_NAME,
			JLE_ID_COLUMN_NAME, JOB_METADATA_ID_COLUMN_NAME,
			SHORT_DESCRIPTION_COLUMN_NAME, LONG_DESCRIPTION_COLUMN_NAME, 
			LAST_COMPLETED_COLUMN_NAME,
			STATUS_COLUMN_NAME, ALERT_START_COLUMN_NAME,
			ELAPSED_TIME_COLUMN_NAME, IS_HTML_COLUMN_NAME,
			JOB_RESULT_COLUMN_NAME, URL_COLUMN_NAME };
}
