package gov.va.shamu.android.utilities;

import android.content.ContentResolver;
import android.database.Cursor;

import gov.va.shamu.android.provider.ShamuData.Reports;

public class Report extends AbstractReport {

    public Report(ContentResolver cr, String job_code, String jle_id,
                  String job_metadata_id, String short_description, String long_description,
                  String last_completed, String status, String is_html, String job_result, String url) {

        super(job_code, jle_id, job_metadata_id, short_description, long_description, last_completed, status, is_html, job_result, url);
        this.cr = cr;
    }

    public Report(ContentResolver cr, Cursor c) {
        super(
                c.getString(Reports.getColumn(Reports.JOB_CODE_COLUMN_NAME)),
                c.getString(Reports.getColumn(Reports.JLE_ID_COLUMN_NAME)),
                c.getString(Reports.getColumn(Reports.JOB_METADATA_ID_COLUMN_NAME)),
                c.getString(Reports.getColumn(Reports.SHORT_DESCRIPTION_COLUMN_NAME)),
                c.getString(Reports.getColumn(Reports.LONG_DESCRIPTION_COLUMN_NAME)),
                c.getString(Reports.getColumn(Reports.LAST_COMPLETED_COLUMN_NAME)),
                c.getString(Reports.getColumn(Reports.STATUS_COLUMN_NAME)),
                c.getString(Reports.getColumn(Reports.IS_HTML_COLUMN_NAME)),
                c.getString(Reports.getColumn(Reports.JOB_RESULT_COLUMN_NAME)),
                c.getString(Reports.getColumn(Reports.URL_COLUMN_NAME)));
        this.cr = cr;
        persisted = true;
    }
}
