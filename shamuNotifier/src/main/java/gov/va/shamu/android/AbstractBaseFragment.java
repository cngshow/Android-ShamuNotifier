package gov.va.shamu.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
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
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.Map;

import eu.erikw.PullToRefreshListView;
import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.utilities.InterestingAlert;
import gov.va.shamu.android.utilities.L;
import gov.va.shamu.android.utilities.PersistableAlert;

public abstract class AbstractBaseFragment extends Fragment {
    protected ShamuNotifierApplication shamu;
    protected ContentResolver cr;
    private final String TAG = getClass().getSimpleName();

    protected void setPageTitle(String title) {
        BaseActivity activity = (BaseActivity)getActivity();
        activity.setPageTitle(title);
    }

    protected void setPageTitle(int r_id) {
        setPageTitle(getActivity().getString(r_id));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        L.i(TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        shamu = (ShamuNotifierApplication) getActivity().getApplication();
        cr = getActivity().getContentResolver();
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
        BaseActivity activity = (BaseActivity)getActivity();
        activity.checkForDialog(getArguments());
    }

    @Override
    public void onResume() {
        super.onResume();
        L.i(TAG, "onResume");
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
}
