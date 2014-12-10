package gov.va.shamu.android.utilities;

import android.content.ContentResolver;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReportTracker {

	private static final String TAG = ReportTracker.class.getSimpleName();

	private ReportTracker() {}

	public static void addShamuJSONUpate(String json, Context context) {
		ContentResolver cr = context.getContentResolver();
		L.d(TAG, "JSON update Report Tracker: " + json);
		JSONObject jo = null;

        Report.deleteAll(cr);

		try {
			jo = new JSONObject(json);
			JSONArray reportJSON = jo.names();
			int length = reportJSON.length();

			for (int i = 0; i < length; i++) {
				L.d(TAG, "Report " + i + " = " + reportJSON.getString(i));
				String report_name = reportJSON.getString(i);
				JSONObject currentReport = jo.getJSONObject(report_name);
				String jle_id = currentReport.getString("jle_id");
				String job_metadata_id = currentReport.getString("job_metadata_id");
				String short_description = currentReport.getString("short_description");
				String long_description = currentReport.getString("long_description");
				String last_completed = currentReport.getString("last_completed");
				String status = currentReport.getString("status");
				String is_html = currentReport.getString("is_html");
				String job_result = currentReport.getString("job_result");
				String url = currentReport.getString("url");
				Report reportStruct = new Report(cr, report_name, jle_id,
						job_metadata_id, short_description, long_description, last_completed,
						status, is_html, job_result,
						url);
                reportStruct.insertOrUpdate();
			}
		} catch (JSONException e) {
			L.wtf(TAG, "SHAMU gave us invalid JSON!!", e);
		}

	}
}
