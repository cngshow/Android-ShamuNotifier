package gov.va.shamu.android;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import eu.erikw.PullToRefreshListView;
import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.provider.ShamuNotifierContentProvider;
import gov.va.shamu.android.utilities.InterestingAlert;
import gov.va.shamu.android.utilities.L;
import gov.va.shamu.android.utilities.PersistableAlert;

public class PullToRefreshAlertsFragment extends PullToRefreshAbstractBaseFragment {

    private final String TAG = PullToRefreshAlertsFragment.class.getSimpleName();
    protected static AsyncEmptyListCheck emptyCheck;


    @Override
    public int getLoaderID() {
        return getDisplayType().ordinal();
    }

    @Override
    protected ShamuNotifierContentProvider.UriType getDisplayType() {
        return ShamuNotifierContentProvider.UriType.ALERT;
    }

    @Override
    public Integer getDisplayedListViewCurrentCount(ContentResolver cr) {//Do not call on UI thread
        Map<String, String> pref = ShamuData.Preferences.getAllPreferences(cr);
        if (!pref.containsKey(ShamuData.Preferences.SHAMU_URL_KEY))
            return null;
        List<InterestingAlert> alerts = ShamuData.InterestingAlertsData.getAllInterestingAlerts(cr);
        int count = 0;
        for (InterestingAlert alert : alerts) {
            if (alert.isInteresting())
                count++;
        }
        return count;
    }

    @Override
    protected String getEmptyListViewTitle() {
        if (isAdded())
          return getString(R.string.alerts_empty_list_title);
        return null;
    }

    @Override
    protected String getEmptyListViewMessage() {
        if (isAdded())
          return getString(R.string.alerts_empty_list_message);
        return null;
    }

    @Override
    protected int getJobCodeTextViewRowID() {
        return R.id.alert_job_code_textview_row;
    }

    @Override
    protected Class getJobCodeDisplayActivityClass() {
        return ShamuJobDisplayActivity.class;
    }

    @Override
    protected void refresh() throws IOException {
        ShamuUpdaterService.refresh(getDisplayType(), getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saved) {

        L.i(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_pull_to_refresh_alerts, container, false);
        listView = (PullToRefreshListView) v.findViewById(R.id.alert_listing);
        checkRefreshState();
        listView.setClickable(true);
        listView.setOnItemClickListener(getListViewItemClickListener());
        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.alert_row,
                null,
                new String[]{ShamuData.InterestingAlertsData.JOB_CODE_COLUMN_NAME,
                        ShamuData.InterestingAlertsData.SHORT_DESCRIPTION_COLUMN_NAME,
                        ShamuData.InterestingAlertsData.STATUS_COLUMN_NAME,
                        ShamuData.InterestingAlertsData.ELAPSED_TIME_COLUMN_NAME},
                new int[]{R.id.alert_job_code_textview_row,
                        R.id.short_description_textview_row,
                        R.id.alert_status_ball, R.id.elapsed_time_textview_row},
                0);
        myLoader = getLoaderManager().initLoader(getLoaderID(), null, this);
        cr = getActivity().getContentResolver();
        SimpleCursorAdapter.ViewBinder savb = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                InterestingAlert alert = new InterestingAlert(cr, cursor);
                View alertBall = view.findViewById(R.id.alert_status_ball);
                TextView shortDescriptionTextView = (TextView) view
                        .findViewById(R.id.short_description_textview_row);
                TextView jobCodeTextView = (TextView) view
                        .findViewById(R.id.alert_job_code_textview_row);
                TextView elapsedTimeTextView = (TextView) view
                        .findViewById(R.id.elapsed_time_textview_row);
                if (i == ShamuData.InterestingAlertsData.getColumn(ShamuData.InterestingAlertsData.JOB_CODE_COLUMN_NAME)) {
                    jobCodeTextView.setText(alert.getJobCode());
                }
                if (i == ShamuData.InterestingAlertsData.getColumn(ShamuData.InterestingAlertsData.SHORT_DESCRIPTION_COLUMN_NAME)) {
                    String shortDescription = alert.getShortDescription();
                    if (shortDescription.equals("null")) {
                        shortDescription = getString(R.string.null_short_description);
                        shortDescriptionTextView.setTextColor(Color.GRAY);
                        shortDescriptionTextView.setTypeface(null, Typeface.ITALIC);
                    } else {
                        shortDescriptionTextView.setTextColor(Color.BLACK);
                        shortDescriptionTextView.setTypeface(null, Typeface.NORMAL);
                    }
                    shortDescription = shortDescription.equals("null") ? getString(R.string.null_short_description) : shortDescription;
                    shortDescriptionTextView.setText(shortDescription);
                }
                if (i == ShamuData.InterestingAlertsData.getColumn(ShamuData.InterestingAlertsData.STATUS_COLUMN_NAME)) {
                    if (alert.getStatus().equalsIgnoreCase(InterestingAlert.RED))
                        alertBall.getBackground().setColorFilter(Color.RED,
                                PorterDuff.Mode.SCREEN);
                    else if (alert.getStatus().equalsIgnoreCase(InterestingAlert.GREEN))
                        alertBall.getBackground().setColorFilter(Color.GREEN,
                                PorterDuff.Mode.SCREEN);
                    else
                        alertBall.getBackground().setColorFilter(Color.GRAY,
                                PorterDuff.Mode.SCREEN);
                }

                if (i == ShamuData.Alerts.getColumn(ShamuData.Alerts.ELAPSED_TIME_COLUMN_NAME)) {
                    if (alert.getStatus().equalsIgnoreCase(PersistableAlert.RED))
                        elapsedTimeTextView.setText(getString(R.string.red_elapsed_time) + " " + alert.getElapsedTime());
                    else if (alert.getStatus().equalsIgnoreCase(PersistableAlert.GREEN))
                        elapsedTimeTextView.setText(getString(R.string.green_elapsed_time) + " " + alert.getElapsedTime());
                    else
                        elapsedTimeTextView.setText(getString(R.string.gray_elapsed_time) + " " + alert.getElapsedTime());
                }
                return true;
            }
        };
        mAdapter.setViewBinder(savb);
        listView.setAdapter(mAdapter);
        listView.setOnRefreshListener(this);
        setPageTitle(getString(R.string.title_alerts));
        Bundle argBundle = getArguments();
        Boolean fromDrawer = (argBundle == null) ? false : argBundle.getBoolean(ShamuMainActivity.SENT_VIA_DRAWER);
        L.d(TAG, "From drawer = " + fromDrawer);
        if(fromDrawer) {
            argBundle.putBoolean(ShamuMainActivity.SENT_VIA_DRAWER,false);
            emptyCheck = null;
        }
        if (emptyCheck == null) {
            emptyCheck = new AsyncEmptyListCheck((BaseActivity) getActivity());
            emptyCheck.execute();
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        L.i(TAG, "onResume");
        if (ShamuConfigurationFragment.handleInterestChange) {
            ShamuConfigurationFragment.handleInterestChange = false;
            myLoader.onContentChanged();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        L.d(TAG, "onCreateLoader");
        // This is called when a new Loader needs to be created. This
        // sample only has one Loader, so we don't care about the ID.
        Uri baseUri = ShamuData.InterestingAlertsData.CONTENT_URI;
        String where = ShamuData.InterestingAlertsData.IS_INTERESTING_COLUMN_NAME + " = 'true'";
        return new CursorLoader(getActivity(), baseUri, null, where, null,
                ShamuData.InterestingAlertsData.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        L.i(TAG, "onAttach");
        (new AsyncPrefCheck((ShamuMainActivity)activity)).execute();
    }

    public class AsyncPrefCheck extends AsyncTask<Void, Void, Map<String, String>> {

        private final String TAG = this.getClass().getSimpleName();
        private final ShamuMainActivity activity;

        AsyncPrefCheck(ShamuMainActivity a) {
            activity = a;
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            L.d(TAG, "Getting all preferences");
            return ShamuData.Preferences.getAllPreferences(activity.getContentResolver());
        }

        @Override
        protected synchronized void onPostExecute(Map<String, String> prefs) {
            String urlString = prefs.get(ShamuData.Preferences.SHAMU_URL_KEY);
            String pollingIntervalString = prefs.get(ShamuData.Preferences.SHAMU_POLLING_INTERVAL_KEY);

            L.d(TAG, "URL pref = " + urlString + " -- polling interval pref = " + pollingIntervalString);
            if (urlString == null ||
                urlString.equals("") ||
                pollingIntervalString == null ||
                pollingIntervalString.equals("")) {
                activity.setDrawerPosition(ShamuMainActivity.POSITION_CONFIGURATION);
                if (!(((BaseActivity)PullToRefreshAlertsFragment.this.getActivity()).dialogging)) {
                    ((ShamuMainActivity) getActivity()).startFragmentWithDialog(
                            getActivity(),
                            new ShamuConfigurationFragment(),
                            getString(R.string.welcome_message),
                            getString(R.string.welcome_title));
                }
                else {
                    getActivity().getFragmentManager().beginTransaction().replace(R.id.shamu_fragment_content_frame, new ShamuConfigurationFragment()).commit();
                }
            }
            else {
                boolean isServiceRunning = shamu.isServiceRunning();//this is if it really is running
                final String shamuServiceStatePref = prefs.get(ShamuData.Preferences.SHAMU_SERVICE_STATE);//this is the user's last action
                boolean shamuServiceState = Boolean.parseBoolean(shamuServiceStatePref == null ? "false" : shamuServiceStatePref);

                if (isServiceRunning != shamuServiceState) {
                    Intent intent = new Intent(getActivity(), ShamuUpdaterService.class);
                    intent.setAction(BaseActivity.OUT_OF_SYNC_SERVER_TOGGLE);

                    if (shamuServiceState) {
                        L.d(TAG, "starting service based on user preference...possible reinstall");
                        getActivity().startService(intent);
                    } else {
                        L.wtf(TAG, "WTF - stopping service!");
                        getActivity().stopService(intent);
                    }
                }
            }
        }
    }

    public PullToRefreshAlertsFragment() {
        // Required empty public constructor
    }
}
