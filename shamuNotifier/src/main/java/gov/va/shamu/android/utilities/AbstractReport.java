package gov.va.shamu.android.utilities;

import android.content.ContentResolver;
import android.net.Uri;

import gov.va.shamu.android.provider.ShamuData.Reports;

public abstract class AbstractReport extends AbstractData {
    protected static final String TAG = AbstractReport.class.getSimpleName();
    protected AbstractReport(String job_code, String jle_id,
                             String job_metadata_id, String short_description, String long_description,
                             String last_completed, String status, String is_html, String job_result, String url) {

        super(job_code, jle_id, job_metadata_id, short_description, long_description,
                last_completed, status, is_html, job_result, url);
    }

    @Override
    protected String getWhereClause() {
        return Reports.JOB_CODE_COLUMN_NAME + " = '" + getJobCode() + "'";
    }

    @Override
    protected Uri getContentURI() {
        return getReportContentURI();
    }

    private static Uri getReportContentURI() {
        return  Reports.CONTENT_URI;
    }

    public static int deleteAll(ContentResolver cr) {
        final Uri contentURI = getReportContentURI();
        int deletes = cr.delete(contentURI, "1 = 1", null);
        if (deletes < 1)
            L.w(TAG, "DeleteAll failed for " + Reports.REPORTS_TABLE_NAME + " table");
        if (deletes > 1)
            L.e(TAG, "Delete failed (magnitude =" + deletes + ") for " + Reports.REPORTS_TABLE_NAME + " table.");
        L.d(TAG, "delete -- deleted " + deletes + " record(s)");
        return deletes;
    }
}
