package gov.va.shamu.android;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.IOException;

import eu.erikw.PullToRefreshListView;
import gov.va.shamu.android.provider.CommonDataColumns;
import gov.va.shamu.android.provider.ShamuNotifierContentProvider;
import gov.va.shamu.android.utilities.L;

public abstract class PullToRefreshAbstractBaseFragment extends AbstractBaseFragment implements
        PullToRefreshListView.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    protected String TAG = PullToRefreshAbstractBaseFragment.class.getSimpleName();
    protected SimpleCursorAdapter mAdapter;
    protected ContentResolver cr;
    protected PullToRefreshListView listView;
    protected Loader<Cursor> myLoader;

    @Override
    public abstract View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle saved);

    /**
     * this must be unique in the concrete class
     * @return the unique loader id in the concrete class
     */
    public abstract int getLoaderID();
    protected abstract int getJobCodeTextViewRowID();
    protected abstract Class getJobCodeDisplayActivityClass();
    protected abstract ShamuNotifierContentProvider.UriType getDisplayType();

    protected AdapterView.OnItemClickListener getListViewItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CharSequence jobCode = ((TextView) view.findViewById(getJobCodeTextViewRowID())).getText();
                L.d(TAG, "selected job code value = " + jobCode);
                Activity currentActivity = getActivity();
                Intent intent = new Intent(currentActivity, getJobCodeDisplayActivityClass());
                intent.putExtra(CommonDataColumns.JOB_CODE_COLUMN_NAME, jobCode);
                intent.putExtra(ShamuJobDisplayActivity.DISPLAY_TYPE, getDisplayType());
                startActivity(intent);
            }
        };
    }

    @Override
    public void onRefresh() {
        L.d(TAG, "ListView Refresh called");
        AsyncRefresh task = new AsyncRefresh(shamu, ((BaseActivity)getActivity()),this, listView, TAG);
        //        public AsyncRefresh(ShamuNotifierApplication app, BaseActivity activity,AbstractBaseFragment frag, PullToRefreshListView lv, String refreshTag) {
        shamu.setMainActivityRefresher(task);
        task.execute();
    }

    // must not be called on the UI thread
    //Return null if 0 is expected!
    public abstract Integer getDisplayedListViewCurrentCount(ContentResolver cr);

    protected abstract String getEmptyListViewTitle();
    protected abstract String getEmptyListViewMessage();

    public abstract Loader<Cursor> onCreateLoader(int id, Bundle args);

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        L.d(TAG, "onLoadFinished");
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        L.d(TAG, "onLoaderReset");
        mAdapter.swapCursor(null);
    }

    protected void checkRefreshState() {
        AsyncRefresh currentRefresher = shamu.getMainActivityRefresher();
        if (currentRefresher == null) {
            return;
        }

        String currentRefreshTag = currentRefresher.getRefreshTag();
        boolean isSameTag = TAG.equals(currentRefreshTag);

        if (isSameTag && currentRefresher.isRunning()) {
            synchronized (currentRefresher) {
                if (currentRefresher.isRunning()) {
                    currentRefresher.setCurrentFragment(this);
                    currentRefresher.setListView(listView);
                    listView.setRefreshing();
                }
            }
        }
    }

/*
    public class AsyncPrefCheck extends AsyncTask<Void, Void, Map<String, String>> {

        private final String TAG = this.getClass().getSimpleName();

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            L.d(TAG, "Getting all preferences");
            final Activity activity = getActivity();
            final ContentResolver contentResolver = activity.getContentResolver();
            return ShamuData.Preferences.getAllPreferences(contentResolver);
        }

        @Override
        protected synchronized void onPostExecute(Map<String, String> prefs) {
            String urlString = prefs.get(ShamuData.Preferences.SHAMU_URL_KEY);
            String pollingIntervalString = prefs.get(ShamuData.Preferences.SHAMU_POLLING_INTERVAL_KEY);
            L.d(TAG, "URL pref = " + urlString + " -- polling interval pref = " + pollingIntervalString);
            if (urlString == null || urlString.equals("") ||
                    pollingIntervalString == null || pollingIntervalString.equals("")) {

                if (!(((BaseActivity)PullToRefreshAbstractBaseFragment.this.getActivity()).dialogging)) {
                    ((ShamuMainActivity) getActivity()).startFragmentWithDialog(getActivity(), new ShamuConfigurationFragment(),
                            getString(R.string.welcome_message), getString(R.string.welcome_title));
                }
                else {
                    getActivity().getFragmentManager().beginTransaction().replace(R.id.shamu_fragment_content_frame, new ShamuConfigurationFragment()).commit();
                }
            }
        }
    }
*/

    /**
     * This implementation requires the refreshType to be defined in the concrete class
     * For Example:
     * The implementation could be: refreshType = ShamuNotifierContentProvider.UriType.ALERT
     */
    protected abstract void refresh() throws IOException;

    protected class AsyncEmptyListCheck extends AsyncTask<Void, Void, Void> {

        private BaseActivity activity;
        private Integer count;

        AsyncEmptyListCheck(BaseActivity activity) {
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            count = getDisplayedListViewCurrentCount(activity.getContentResolver());
            L.v(TAG,"The count in this ListView is " + count);
            return null;
        }

        @Override
        protected synchronized void onPostExecute(Void result) {
            String message = getEmptyListViewMessage();
            String title = getEmptyListViewTitle();
            if (!activity.dialogging && count != null && count == 0  && title != null && message != null) {
                L.v(TAG,"Sending the empty list view dialog up");
                activity.buildAlertDialog(message, title);
            }
        }
    }

    public static class AsyncRefresh extends AsyncTask<Void, Void, Void> {

        private static final String TAG = "AsyncRefresh";
        private final BaseActivity activity;
        private PullToRefreshAbstractBaseFragment currentFragment;
        private final ShamuNotifierApplication mainApp;
        private volatile boolean isRunning = false;
        private PullToRefreshListView lv;
        private boolean refreshExceptionThrown = false;
        private String refreshTag = null;

        public AsyncRefresh(ShamuNotifierApplication app, BaseActivity activity,PullToRefreshAbstractBaseFragment frag, PullToRefreshListView lv, String refreshTag) {
            L.v(TAG, "AsyncRefresh: instantiated !");
            this.lv = lv;
            this.refreshTag = refreshTag;
            this.activity = activity;
            mainApp = app;
            this.currentFragment=frag;
        }

        public String getRefreshTag() {
            return refreshTag;
        }

        public synchronized void setCurrentFragment(PullToRefreshAbstractBaseFragment frag) {
            currentFragment = frag;
        }

        @Override
        protected Void doInBackground(Void... params) {
            refreshExceptionThrown = false;
            isRunning = true;
            try {
                currentFragment.refresh();
            } catch (Exception e) {
                L.e(TAG, "Pull to refresh failed to reach SHAMU!", e);
                refreshExceptionThrown = true;
            } finally {
                isRunning = false;
            }
            return null;
        }

        public synchronized void setListView(PullToRefreshListView lv) {
            this.lv = lv;
        }

        @Override
        protected synchronized void onPostExecute(Void result) {
            if (refreshExceptionThrown && currentFragment.isAdded())
                activity.buildAlertDialog(currentFragment.getString(R.string.main_activity_failed_refresh_message), currentFragment.getString(R.string.main_activity_failed_refresh_title));
            lv.onRefreshComplete();
            mainApp.setMainActivityRefresher(null);
        }

        public boolean isRunning() {
            return isRunning;
        }

    }

}
