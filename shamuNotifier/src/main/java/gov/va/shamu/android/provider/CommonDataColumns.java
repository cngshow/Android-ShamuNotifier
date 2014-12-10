package gov.va.shamu.android.provider;

import android.provider.BaseColumns;

public interface CommonDataColumns extends BaseColumns {
	
	public static final String JOB_CODE_COLUMN_NAME = "job_code";
	public static final String JLE_ID_COLUMN_NAME = "jle_id";
	public static final String JOB_METADATA_ID_COLUMN_NAME = "job_metadata_id";
	public static final String LAST_COMPLETED_COLUMN_NAME = "last_completed";
	public static final String SHORT_DESCRIPTION_COLUMN_NAME = "short_description";
	public static final String LONG_DESCRIPTION_COLUMN_NAME = "long_description";
	public static final String STATUS_COLUMN_NAME = "status";
	public static final String IS_HTML_COLUMN_NAME = "is_html";
	public static final String JOB_RESULT_COLUMN_NAME = "job_result";
	public static final String URL_COLUMN_NAME = "url";
	public static final String[] ALL_COLUMNS = new String[] {
		    _ID, JOB_CODE_COLUMN_NAME,
			JLE_ID_COLUMN_NAME, JOB_METADATA_ID_COLUMN_NAME,
			SHORT_DESCRIPTION_COLUMN_NAME, LONG_DESCRIPTION_COLUMN_NAME, 
			LAST_COMPLETED_COLUMN_NAME,
			STATUS_COLUMN_NAME, IS_HTML_COLUMN_NAME,
			JOB_RESULT_COLUMN_NAME, URL_COLUMN_NAME };

}
