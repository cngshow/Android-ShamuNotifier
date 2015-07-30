package gov.va.shamu.android;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import gov.va.shamu.android.provider.CommonDataColumns;
import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.provider.ShamuData.Alerts;
import gov.va.shamu.android.provider.ShamuNotifierContentProvider;
import gov.va.shamu.android.utilities.AbstractData;
import gov.va.shamu.android.utilities.L;
import gov.va.shamu.android.utilities.PersistableAlert;
import gov.va.shamu.android.utilities.Report;

public class ShamuJobResultFragment extends Fragment {
	// Make strings for logging
	private WebView jobResultsView;
	private CharSequence jobCode;
    private ShamuNotifierContentProvider.UriType displayType;

	// get a label for our log entries
	private final String TAG = this.getClass().getSimpleName();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		L.i(TAG, "onAttach");
	}

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
	}
//todo change for report v alert
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle saved) {
		View v = inflater.inflate(R.layout.job_display_details, container, false);
        final Bundle extras = getActivity().getIntent().getExtras();
        jobCode = extras.getCharSequence(CommonDataColumns.JOB_CODE_COLUMN_NAME);
        displayType = (ShamuNotifierContentProvider.UriType) extras.getSerializable(ShamuJobDisplayActivity.DISPLAY_TYPE);
		jobResultsView = (WebView) v.findViewById(R.id.job_result_details);
		WebSettings webSettings = jobResultsView.getSettings();
		webSettings.setBuiltInZoomControls(true);
		jobResultsView.requestFocusFromTouch();
		Object  o = jobResultsView.getSettings();
		//jobResultsView.getSettings().setJavaScriptEnabled(true);
		jobResultsView.setWebChromeClient(new WebChromeClient());

		(new AsyncLookup()).execute();
		L.i(TAG, "onCreateView");
		return v;
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		L.i(TAG, "onActivityCreated");
	}

	@Override
	public void onStart() {
		super.onStart();
		L.i(TAG, "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		// Notification that the activity will interact with the user
		L.i(TAG, "onResume");
//		(new AsyncLookup()).execute();
	}

	@Override
	public void onPause() {
		super.onPause();
		L.i(TAG, "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		L.i(TAG, "onStop");
	}

	// ////////////////////////////////////////////////////////////////////////////
	// Called during the lifecycle, when instance state should be saved/restored
	// ////////////////////////////////////////////////////////////////////////////

	@Override
	public void onSaveInstanceState(Bundle toSave) {
		super.onSaveInstanceState(toSave);
		L.i(TAG, "onSaveinstanceState");
	}

    public class AsyncLookup extends AsyncTask<Void, Void, AbstractData> {
        @Override
        protected AbstractData doInBackground(Void... params) {
            ContentResolver cr = getActivity().getContentResolver();
            Cursor c = null;

            try {
                if (displayType.equals(ShamuNotifierContentProvider.UriType.ALERT)) {
                    c = cr.query(Alerts.CONTENT_URI, Alerts.ALL_COLUMNS,
                            Alerts.JOB_CODE_COLUMN_NAME + " = ?",
                            new String[]{jobCode.toString()}, null);
                    if (c.moveToFirst())
                        return new PersistableAlert(cr, c);
                } else {
                    c = cr.query(ShamuData.Reports.CONTENT_URI, ShamuData.Reports.ALL_COLUMNS,
                            ShamuData.Reports.JOB_CODE_COLUMN_NAME + " = ?",
                            new String[]{jobCode.toString()}, null);
                    if (c.moveToFirst())
                        return new Report(cr, c);
                }

            } finally {
                if (c != null)
                    c.close();
            }
            return null;
        }

		@Override
		protected synchronized void onPostExecute(AbstractData result) {
			if (result == null)
				return;
			ShamuJobDisplayActivity.mostRecentResultURL = result.getUrl();
			String jobResults = result.getJobResult();
			if (!result.isHtml()) {
				jobResults = "<html><body><pre>" + jobResults
						+ "</pre></body></html>";
				jobResults = jobResults.replaceAll("\n", "<br>");
				jobResults = jobResults.replaceAll("\t", "     ");
			}
			L.d(TAG, jobResults);
			jobResultsView.loadData(jobResults, "text/html", null);
		}

	}
}
