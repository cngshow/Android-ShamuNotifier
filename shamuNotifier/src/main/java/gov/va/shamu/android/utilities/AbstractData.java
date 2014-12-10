package gov.va.shamu.android.utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import gov.va.shamu.android.provider.ShamuData.Alerts;

public abstract class AbstractData {

	protected String jobCode;
	protected String jleID;
	protected String jobMetadataID;
	protected String shortDescription;
	protected String longDescription;
	protected String lastCompleted;
	protected String status;
	protected String jobResult;
	protected String url;
    protected String isHtml;
    protected boolean persisted = false;

	protected ContentValues cv = new ContentValues();
	protected ContentResolver cr;
	protected static final String TAG = AbstractData.class.getSimpleName();

    protected AbstractData(String job_code, String jle_id,
                           String job_metadata_id, String short_description, String long_description,
                           String last_completed, String status, String is_html, String job_result, String url) {
		this.jobCode = job_code;
		cv.put(Alerts.JOB_CODE_COLUMN_NAME, jobCode);
		this.jleID = jle_id;
		cv.put(Alerts.JLE_ID_COLUMN_NAME, jleID);
		this.jobMetadataID = job_metadata_id;
		cv.put(Alerts.JOB_METADATA_ID_COLUMN_NAME, jobMetadataID);
		this.shortDescription = short_description;
		cv.put(Alerts.SHORT_DESCRIPTION_COLUMN_NAME, shortDescription);
		this.longDescription = long_description;
		cv.put(Alerts.LONG_DESCRIPTION_COLUMN_NAME, longDescription);
		this.lastCompleted = last_completed;
		cv.put(Alerts.LAST_COMPLETED_COLUMN_NAME, lastCompleted);
		this.status = status;
		cv.put(Alerts.STATUS_COLUMN_NAME, status);
		this.isHtml = is_html;
		cv.put(Alerts.IS_HTML_COLUMN_NAME, isHtml);
		this.jobResult = job_result;
		cv.put(Alerts.JOB_RESULT_COLUMN_NAME, jobResult);
		this.url = url;
		cv.put(Alerts.URL_COLUMN_NAME, url);
	}

	public String getJobCode() {
		return jobCode;
	}

	public String getJleID() { return jleID; }

	public String getJobMetadataID() {
		return jobMetadataID;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public String getLastCompleted() {
		return lastCompleted;
	}

	public String getStatus() {
		return status;
	}

	public String getJobResult() {
		return jobResult;
	}

	public String getUrl() {
		return url;
	}
	
	public boolean isHtml() {
		return Boolean.valueOf(isHtml);
	}

	// rails app guarantees uniqueness on jobCode
	@Override
	public int hashCode() {
		return jobCode.hashCode();
	}

	//equals method does not currently care if an alert is persisted or not.  Beware!
	@Override
	public boolean equals(Object object) {
        return object instanceof AbstractData && jobCode.equals(((AbstractData) object).getJobCode());
    }

	public String toString() {
		return jobCode + ": " + "{jleID: " + jleID + " |jobMetadataID: "
				+ jobMetadataID + " |shortDescription:" + shortDescription
				+ " |longDescription:" + longDescription
				+ " |lastCompleted: " + lastCompleted + " |status: " + status
				+ " |jobResult: " + jobResult + " |url: " + url
				+ "}";
	}

	public ContentValues getContentValues() {
        return new ContentValues(cv);
	}

    protected abstract String getWhereClause();
    protected abstract Uri getContentURI();

	public int delete() {
		//L.d(TAG, "delete -- " + toString() + " content resolver = " + cr);
        final Uri contentURI = getContentURI();
        int deletes = cr.delete(contentURI, getWhereClause(), null);
		if (deletes < 1)
			L.w(TAG, "Delete failed for " + toString());
		if (deletes > 1)
			L.e(TAG, "Delete failed (magnitude =" + deletes + ") for "
					+ toString());
		L.d(TAG, "delete -- deleted " + deletes + " record(s)");
		return deletes;
	}

    public int insertOrUpdate() {
        final Uri contentURI = getContentURI();
        if (persisted)
            return 0;
        L.d(TAG, "insertOrUpdate");
        int updates = 0;
        updates = cr.update(contentURI, getContentValues(), getWhereClause(), null);
        L.d(TAG, "update attempt returned " + updates);
        if (updates == 0) {
            // do an insert
            Uri uri = cr.insert(contentURI, getContentValues());
            L.d(TAG, "insertOrUpdate did an update " + uri.toString());
            updates = 1;
        }
        if (updates > 0) {
            persisted = true;
        }

        return updates;
    }

    public boolean isPersisted() {
        return persisted;
    }

}

