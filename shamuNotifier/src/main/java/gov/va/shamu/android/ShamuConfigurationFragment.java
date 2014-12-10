package gov.va.shamu.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.utilities.AlertTracker;
import gov.va.shamu.android.utilities.InterestingAlert;
import gov.va.shamu.android.utilities.L;

public class ShamuConfigurationFragment extends AbstractBaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private ShamuNotifierApplication shamu;
    private final String TAG = getClass().getSimpleName();
    private SimpleCursorAdapter mAdapter;
    private ContentResolver cr;
    private ListView listView;
    private Button testButton, selectAllButton, deselectAllButton;
    private EditText shamuUrlEditText;
    private EditText pollingEditText;
    static volatile boolean handleInterestChange = false;
    private Loader<Cursor> myLoader;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        L.i(TAG, "onAttach");
        (new AsyncPrefLoad((BaseActivity)getActivity())).execute();
    }

    @Override
    public void onCreate(Bundle savedState) {
        shamu = (ShamuNotifierApplication) getActivity().getApplication();
        super.onCreate(savedState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saved) {

        L.i(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_shamu_configuration, container, false);
        listView = (ListView) v.findViewById(R.id.preferences_interesting_alert_listview);
        shamuUrlEditText = (EditText) v.findViewById(R.id.preferences_url_edit_text);
        pollingEditText = (EditText) v.findViewById(R.id.preferences_polling_interval_edit_text);
        testButton = (Button) v.findViewById(R.id.preferences_test_button);
        selectAllButton = (Button) v.findViewById(R.id.preferences_select_all_button);
        deselectAllButton = (Button)v.findViewById(R.id.preferences_deselect_all_button);

        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.configuration_interesting_alert_row,
                null,
                ShamuData.InterestingAlertsData.ALL_COLUMNS,
                new int[]{R.id.preferences_row_interesting_alert_checkbox}, 0);
        myLoader = getLoaderManager().initLoader(0, null, this);
        cr = getActivity().getContentResolver();
        SimpleCursorAdapter.ViewBinder savb = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                InterestingAlert alert = new InterestingAlert(cr, cursor);
                String jc = alert.getJobCode();
                boolean checked = alert.isInteresting();
                CheckBox cb = (CheckBox) view.findViewById(R.id.preferences_row_interesting_alert_checkbox);
                cb.setOnCheckedChangeListener(ShamuConfigurationFragment.this);
                cb.setText(jc);
                cb.setChecked(checked);
                return true;
            }
        };
        mAdapter.setViewBinder(savb);
        listView.setAdapter(mAdapter);
        testButton.setOnClickListener(this);
        selectAllButton.setOnClickListener(this);
        deselectAllButton.setOnClickListener(this);
        BaseActivity activity = (BaseActivity)getActivity();
        activity.setPageTitle(activity.getString(R.string.title_configure));
        checkTestingState();
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
        L.i(TAG, "onResume");
        // Notification that the activity will interact with the user
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
   // ----------------------------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. This
        // sample only has one Loader, so we don't care about the ID.
        Uri baseUri = ShamuData.InterestingAlertsData.CONTENT_URI;
        return new CursorLoader(getActivity(), baseUri, null, null, null,
                ShamuData.InterestingAlertsData.JOB_CODE_COLUMN_NAME + " asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final CharSequence jobCode = buttonView.getText();
        final String checked = ((Boolean)isChecked).toString();
        final ContentResolver cr = getActivity().getContentResolver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ShamuData.Preferences.insertOrUpdatePreference(cr, jobCode, checked);
                handleInterestChange = true;
                AlertTracker.synchronizeWidget(getActivity());
            }
        }).start();
    }


    @Override
    public void onClick(View v) {
        L.d(TAG, "onClick");
        if (v.equals(testButton)) {
            L.d(TAG, "Test button clicked!");
            if (shamuUrlEditText != null && pollingEditText != null) {
                String urlString = shamuUrlEditText.getText().toString();
                String pollingIntervalString = pollingEditText.getText().toString();
                if (pollingIntervalString.equals(""))
                    pollingIntervalString = getString(R.string.configuration_default_polling_interval);
                int pollingIntervalInt = Integer.parseInt(pollingIntervalString);
                pollingIntervalString = Integer.toString(pollingIntervalInt);
                pollingEditText.setText("");
                pollingEditText.append(pollingIntervalString);//set cursor at end
                try {
                    URL urlToSHAMU = new URL("http://"+urlString);
                    urlToSHAMU.toURI();
                    L.d(TAG, "SHAMU URL is " + urlToSHAMU);
                } catch (Exception e) {
                    ((BaseActivity)getActivity()).buildAlertDialog(getString(R.string.configuration_invalid_url_dialogue_please_fix), getString(R.string.configuration_invalid_url_dialogue_title));
                    return;
                }
                if (shamu.isNetworkAvailable()) {
                    testButton.setEnabled(false);
                    testButton.setText(getString(R.string.configuration_testing_button_in_test));
                    AsyncTest tester = new AsyncTest(urlString,pollingIntervalString, testButton);
                    shamu.setPreferencesTester(tester);
                    tester.execute();
                } else {
                    ((BaseActivity)getActivity()).buildAlertDialog(getString(R.string.configuration_no_active_network_dialogue), getString(R.string.configuration_no_active_network_title));
                }
            }
        } else if (v.equals(selectAllButton)) {
            selectAllAlerts(true);
        } else if (v.equals(deselectAllButton)) {
            selectAllAlerts(false);
        }
    }
    private void selectAllAlerts(boolean b) {
        L.d(TAG, "Select all Alerts called with value " + b);
        (new AsyncInterestingAlertAllSettter((BaseActivity)getActivity(),b)).execute();
    }


    public class AsyncInterestingAlertAllSettter extends AsyncTask<Void, Void, Void> {

        private BaseActivity activity;
        private String allOn;

        AsyncInterestingAlertAllSettter(BaseActivity activity, boolean allOn) {
            this.allOn = Boolean.toString(allOn);
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            List<InterestingAlert> alerts = ShamuData.InterestingAlertsData.getAllInterestingAlerts(ShamuConfigurationFragment.this.getActivity().getContentResolver());
            for (InterestingAlert a : alerts) {
                String jobCode = a.getJobCode();
                ShamuData.Preferences.insertOrUpdatePreference(ShamuConfigurationFragment.this.getActivity().getContentResolver(), jobCode, allOn);
            }
            //AlertTracker.synchronizeWidget(alerts, getActivity());
            activity.getShamu().notifyWidgetOfChange();
            return null;
        }

        @Override
        protected synchronized void onPostExecute(Void ignore) {
            handleInterestChange = true;
            myLoader.onContentChanged();
        }

    }

    public class AsyncPrefLoad extends AsyncTask<Void, Void, Map<String, String>> {

        BaseActivity activity;

        AsyncPrefLoad(BaseActivity activity) {
            this.activity = activity;
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            return ShamuData.Preferences.getAllPreferences(activity.getContentResolver());
        }

        @Override
        protected synchronized void onPostExecute(Map<String, String> prefs) {
            String urlString = prefs.get(ShamuData.Preferences.SHAMU_URL_KEY);
            String pollingIntervalString = prefs.get(ShamuData.Preferences.SHAMU_POLLING_INTERVAL_KEY);
            if (urlString != null  && shamuUrlEditText.getText().toString().equals("")) {
                shamuUrlEditText.setText("");
                shamuUrlEditText.append(urlString);
            }
            if (pollingIntervalString != null && pollingEditText.getText().toString().equals("")) {
                pollingEditText.setText("");
                pollingEditText.append(pollingIntervalString);
            }
        }
    }


    private void checkTestingState() {
        AsyncTest tester = shamu.getPreferenceActivityTester();
        if (tester != null && tester.isRunning()) {
            synchronized (tester) {
                if (tester.isRunning()) {
                    testButton.setEnabled(false);
                    testButton.setText(getString(R.string.configuration_testing_button_in_test));
                    tester.setTestButton(testButton);
                }
            }
        }
    }
    public class AsyncTest extends AsyncTask<Void, Void, Boolean> {
        private final String TAG = this.getClass().getSimpleName();
        private volatile boolean isRunning = false;

        String url, interval;
        Button tb;

        AsyncTest(String url, String interval, Button b) {
            this.url = url;
            this.interval = interval;
            tb = b;
        }

        synchronized void setTestButton(Button b) {
            tb = b;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            isRunning = true;
            Boolean success = Boolean.TRUE;
            //add in code to store prefs
            ShamuData.Preferences.insertOrUpdatePreference(getActivity().getContentResolver(), ShamuData.Preferences.SHAMU_URL_KEY, url);
            ShamuData.Preferences.insertOrUpdatePreference(getActivity().getContentResolver(), ShamuData.Preferences.SHAMU_POLLING_INTERVAL_KEY, interval);
            try {
                ShamuUpdaterService.refreshAlerts(getActivity());
            } catch (IOException e) {
                L.w(TAG, "Testing failed to reach shamu:  " + e.toString());
                success = Boolean.FALSE;
            } finally {
                isRunning = false;
            }
            return success;
        }

        @Override
        protected synchronized void onPostExecute(Boolean success) {
            if (success) {
                ((BaseActivity)getActivity()).buildAlertDialog(getString(R.string.configuration_success_test_message),
                        getString(R.string.configuration_success_test_title));
            } else {
                //display dialogue that SHAMU was unreachable
                ((BaseActivity)getActivity()).buildAlertDialog(getString(R.string.configuration_shamu_unreachable_message),
                        getString(R.string.configuration_shamu_unreachable_title));
            }
            tb.setEnabled(true);
            tb.setText(getString(R.string.configuration_test_button));
            shamu.setPreferencesTester(null);
        }

        public boolean isRunning() {
            return isRunning;
        }
    }


}
