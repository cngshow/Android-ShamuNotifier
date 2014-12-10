package gov.va.shamu.android;

import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.IOException;

import eu.erikw.PullToRefreshListView;
import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.provider.ShamuNotifierContentProvider;
import gov.va.shamu.android.utilities.L;
import gov.va.shamu.android.utilities.Report;

public class PullToRefreshReportsFragment extends PullToRefreshAbstractBaseFragment {
    private final String TAG = PullToRefreshReportsFragment.class.getSimpleName();
    protected static AsyncEmptyListCheck emptyCheck;


    @Override
    protected ShamuNotifierContentProvider.UriType getDisplayType() {
        return ShamuNotifierContentProvider.UriType.REPORT;
    }

    @Override
    protected void refresh() throws IOException {
        ShamuUpdaterService.refresh(getDisplayType(), getActivity());
    }

    @Override
    protected int getJobCodeTextViewRowID() {
        return R.id.report_job_code_textview_row;
    }

    @Override
    protected Class getJobCodeDisplayActivityClass() {
        return ShamuJobDisplayActivity.class;
    }

    @Override
    public int getLoaderID() {
        return getDisplayType().ordinal();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        L.d(TAG, "onCreateLoader");
        // This is called when a new Loader needs to be created. This
        // sample only has one Loader, so we don't care about the ID.
        Uri uri =  ShamuData.Reports.CONTENT_URI;
        String where = "1 = 1";
        return new CursorLoader(getActivity(), uri, null, where, null, ShamuData.Reports.DEFAULT_SORT_ORDER);
    }

    @Override
    public Integer getDisplayedListViewCurrentCount(ContentResolver cr){
        return ShamuData.Reports.getAllReports(cr).size();
    }

    @Override
    protected String getEmptyListViewTitle() {
        if (isAdded())
          return getString(R.string.reports_empty_listview_title);
        return null;
    }

    @Override
    protected String getEmptyListViewMessage() {
        if (isAdded())
          return getString(R.string.reports_empty_listview_message);
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved)
    {
        L.i(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_pull_to_refresh_reports, container, false);
        listView = (PullToRefreshListView) v.findViewById(R.id.report_listing);
        checkRefreshState();
        listView.setClickable(true);
        listView.setOnItemClickListener(getListViewItemClickListener());
        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.report_row,
                null,
                new String[]{ShamuData.Reports.JOB_CODE_COLUMN_NAME,
                        ShamuData.Reports.SHORT_DESCRIPTION_COLUMN_NAME,
                        ShamuData.Reports.LAST_COMPLETED_COLUMN_NAME
                },
                new int[]{R.id.report_job_code_textview_row,
                        R.id.report_short_description_textview_row,
                        R.id.report_last_completed_textview_row
                },
                0);
        myLoader = getLoaderManager().initLoader(getLoaderID(), null, this);
        cr = getActivity().getContentResolver();
        SimpleCursorAdapter.ViewBinder savb = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                Report report = new Report(cr, cursor);
                TextView shortDescriptionTextView = (TextView) view
                        .findViewById(R.id.report_short_description_textview_row);
                TextView jobCodeTextView = (TextView) view
                        .findViewById(R.id.report_job_code_textview_row);
                TextView lastCompletedTextView = (TextView) view
                        .findViewById(R.id.report_last_completed_textview_row);

                if (i == ShamuData.Reports.getColumn(ShamuData.Reports.JOB_CODE_COLUMN_NAME)) {
                    jobCodeTextView.setText(report.getJobCode());
                }

                if (i == ShamuData.Reports.getColumn(ShamuData.Reports.SHORT_DESCRIPTION_COLUMN_NAME)) {
                    String shortDescription = report.getShortDescription();

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

                if (i == ShamuData.Reports.getColumn(ShamuData.Reports.LAST_COMPLETED_COLUMN_NAME)) {
                    lastCompletedTextView.setText(report.getLastCompleted());
                }

                return true;
            }
        };
        mAdapter.setViewBinder(savb);
        listView.setAdapter(mAdapter);
        listView.setOnRefreshListener(this);
        setPageTitle(getString(R.string.title_reports));
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

    public PullToRefreshReportsFragment() {
        // Required empty public constructor
    }
}
