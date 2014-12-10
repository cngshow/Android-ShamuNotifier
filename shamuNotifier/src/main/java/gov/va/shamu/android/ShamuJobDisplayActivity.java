package gov.va.shamu.android;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;

import gov.va.shamu.android.provider.CommonDataColumns;
import gov.va.shamu.android.provider.ShamuNotifierContentProvider;
import gov.va.shamu.android.utilities.L;

public class ShamuJobDisplayActivity extends BaseActivity {

    public static final String DISPLAY_TYPE = "gov.va.shamu.android.ShamuDisplayActivity.displayType";
	public static final int JOB_RESULT_PAGE=0;
	public static final int JOB_METADATA_PAGE=1;
	// Make strings for logging
	private final String TAG = this.getClass().getSimpleName();
	private ViewPager mPager;
	private FragmentStatePagerAdapter mPagerAdapter;
	private static final int NUM_PAGES = 2;
	private Button jcButton;
	private CharSequence jobCode;
	static volatile String mostRecentResultURL;


	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.shamu_job_display_activity);

        final Bundle extras = getIntent().getExtras();
        jobCode = extras.getCharSequence(CommonDataColumns.JOB_CODE_COLUMN_NAME);
        ShamuNotifierContentProvider.UriType displayType = (ShamuNotifierContentProvider.UriType)extras.getSerializable(ShamuJobDisplayActivity.DISPLAY_TYPE);
		jcButton = (Button) findViewById(R.id.job_code_details_button);
		SpannableString underlinedJobCode = new SpannableString(jobCode);
		underlinedJobCode.setSpan(new UnderlineSpan(), 0, underlinedJobCode.length(), 0);
		jcButton.setText(underlinedJobCode);
		jcButton.setOnClickListener(new URLButtonListener());
		mPager = (ViewPager) findViewById(R.id.job_code_details_pager);
		mPagerAdapter = new JobDetailsPagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
		L.i(TAG, "onCreate");

        if (displayType.equals(ShamuNotifierContentProvider.UriType.REPORT)) {
            setPageTitle(getString(R.string.report_details));
        }
        else {
            setPageTitle(getString(R.string.alert_details));
        }
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// Notification that the activity will be started
		L.i(TAG, "onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Notification that the activity is starting
		L.i(TAG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Notification that the activity will interact with the user
		L.i(TAG, "onResume");
	}

	protected void onPause() {
		super.onPause();
		// Notification that the activity will stop interacting with the user
		L.i(TAG, "onPause" + (isFinishing() ? " Finishing" : ""));
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Notification that the activity is no longer visible
		L.i(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Notification the activity will be destroyed
		L.i(TAG, "onDestroy "
		// Log which, if any, configuration changed
				+ Integer.toString(getChangingConfigurations(), 16));
	}

	// ////////////////////////////////////////////////////////////////////////////
	// Called during the lifecycle, when instance state should be saved/restored
	// ////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onSaveInstanceState(Bundle state) {
		// Save instance-specific state
		super.onSaveInstanceState(state);
		L.i(TAG, "onSaveInstanceState");

	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		L.i(TAG, "onRetainNonConfigurationInstance");
		return new Integer(getTaskId());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		L.i(TAG, "onRestoreInstanceState");
	}

	// ////////////////////////////////////////////////////////////////////////////
	// These are the minor lifecycle methods, you probably won't need these
	// ////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onPostCreate(Bundle savedState) {
		super.onPostCreate(savedState);
		L.i(TAG, "onPostCreate");

	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		L.i(TAG, "onPostResume");
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		L.i(TAG, "onUserLeaveHint");
	}

	@Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

	/**
     * A simple pager adapter that represents 2 alert fragment objects, in
     * sequence.
     */
    private class JobDetailsPagerAdapter extends FragmentStatePagerAdapter {
        public JobDetailsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
            public Fragment getItem(int position) {
                Fragment retFragment = new ShamuJobResultFragment();

                if (position == JOB_METADATA_PAGE) {
                    retFragment =  new ShamuJobMetadataFragment();
                }
                return retFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

	private class URLButtonListener implements View.OnClickListener {
		private final String TAG = this.getClass().getSimpleName();

		@Override
		public void onClick(View v) {
			L.d(TAG, "On click called");
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mostRecentResultURL));
			startActivity(browserIntent);
		}
	}
}
