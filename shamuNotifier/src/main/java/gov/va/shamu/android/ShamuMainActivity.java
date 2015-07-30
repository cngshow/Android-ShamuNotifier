package gov.va.shamu.android;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.*;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import gov.va.shamu.android.speechtotext.SpeechCommandFragment;
import gov.va.shamu.android.utilities.L;

public class ShamuMainActivity extends BaseActivity {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private  ListView drawerList;
    private  String[] drawerTitles;
    private SimpleAdapter drawerAdapter;
    static final int POSITION_ALERT=1;
    static final int POSITION_REPORT=2;
    static final int POSITION_CONFIGURATION=3;
    static final int POSITION_SETTINGS=4;
    static final int POSITION_REAL_TIME_CHARTS=5;
    static final int POSITION_LAST=5;//always make equal to the last value above.
    static final String DRAWER_IMAGE_PREAMBLE = "drawer_";
    static final String LAST_DRAWER_SELECTION="DRAWER_SELECTION";
    static final String SENT_VIA_DRAWER = "SENT_VIA_DRAWER";
    static Integer lastFragmentSetPosition = POSITION_ALERT;//1 based as pulling zero out of a bundle means not found

    // Make strings for logging
	private final String TAG = this.getClass().getSimpleName();
    private boolean externalDrawerChange =false;

	@Override
	public void onCreate(Bundle savedState) {
        L.v(TAG,"onCreate");
        if (savedState == null) {
            L.v(TAG,"Bundle is null");
            savedState = new Bundle();
            if (lastFragmentSetPosition != null) {
                //If android call onCreate w/o onRestoreInstanceState we have the same instance.  Refer to the instance variable.
                savedState.putInt(LAST_DRAWER_SELECTION, lastFragmentSetPosition);
            }
        }

        L.v(TAG,"The last selection state position was " + savedState.getInt(LAST_DRAWER_SELECTION));
		super.onCreate(savedState);
		setContentView(R.layout.activity_shamu_main);
        setUpDrawer(savedState);
        FragmentManager fragmentManager = getFragmentManager();
        int fragPosition = savedState.getInt(LAST_DRAWER_SELECTION);
        if (fragPosition >= 1) {
            launchFragment(fragPosition,savedState, true);

        } else {
            fragmentManager.beginTransaction().replace(R.id.shamu_fragment_content_frame, new PullToRefreshAlertsFragment()).commit();
            lastFragmentSetPosition = POSITION_ALERT;
        }
	}

    private void setUpDrawer(final Bundle bundle) {
        drawer =  (DrawerLayout) findViewById(R.id.main_drawer);
        drawerTitles = getResources().getStringArray(R.array.nav_drawer_array);
        drawerList = (ListView) findViewById(R.id.main_drawer_list);
        drawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        drawerList.setOnItemClickListener(new DrawerItemClickListener(bundle));
        drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        //ArrayAdapter<String> drawerAdapter = new ArrayAdapter<String>(this,R.layout.drawer_row,R.id.drawer_textview_right, drawerTitles);
         int[] to = new int[] {R.id.drawer_textview_right };

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < drawerTitles.length; i++){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("drawer_textview_right", drawerTitles[i]);
            fillMaps.add(map);
        }

        drawerAdapter = new SimpleAdapter(this, fillMaps, R.layout.drawer_row, new String[]{"drawer_textview_right"},to);
        drawerList.setAdapter(drawerAdapter);

        SimpleAdapter.ViewBinder vb = new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                ViewParent grandma = view.getParent().getParent();//takes me to the table row.
                if (grandma instanceof View) {
                    ImageView im = (ImageView)(((View)grandma).findViewById(R.id.drawer_image_left));
                    String resourceName = DRAWER_IMAGE_PREAMBLE + lowercase(textRepresentation);
                    int id = ShamuMainActivity.this.getResources().getIdentifier(resourceName, "drawable", ShamuMainActivity.this.getPackageName());
                    im.setImageResource(id);
                }
                ((TextView)view).setText(textRepresentation);
                return true;
            }
        };
        drawerAdapter.setViewBinder(vb);

        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawer,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                L.v(TAG,"onDrawerClosed");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                //setSelection((ListView)view, bundle);
            }

            public void onDrawerOpened(View drawerView) {
                L.v(TAG,"onDrawerOpened");
                setSelection((ListView)drawerView, bundle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawer.setDrawerListener(drawerToggle);
        drawerList.setOnScrollListener(new DrawerScrollListener(bundle));
    }

    private void setSelection(ListView listView, Bundle bundle) {
        L.v(TAG, "setSelection: The last selection state position was " + bundle.getInt(LAST_DRAWER_SELECTION));
        int initialColor = ((ColorDrawable)drawerList.getBackground()).getColor();
        ListView lv = (ListView)listView;
        View selectedView = null;
        int currentSelection = lastFragmentSetPosition;//bundle.getInt(ShamuMainActivity.LAST_DRAWER_SELECTION);
        currentSelection--;//Selections are stored as one based.  We convert...
        if (currentSelection >= 0) {
            selectedView = lv.getChildAt(currentSelection);
           // selectedView = lv.getAdapter().getView(currentSelection, null, null);
            if (selectedView != null) {
                selectedView.setBackgroundColor(Color.GRAY);
            } else {
                L.v(TAG,"Main list item is null!");
            }
            for (int i = POSITION_ALERT - 1; i <= POSITION_LAST - 1; i++ ) {
                if (i == currentSelection)
                    continue;
                selectedView = lv.getChildAt(i);//our positions are 1 based
                //selectedView = lv.getAdapter().getView(i, null, null);
                if (selectedView != null)
                    selectedView.setBackgroundColor(initialColor);
            }
        }
    }

    void setDrawerPosition(int position) {
        L.d(TAG,"setDrawerPosition: " + position);

        switch (position) {
            case POSITION_SETTINGS:
            case POSITION_CONFIGURATION:
            case POSITION_REPORT:
            case POSITION_ALERT:
            case POSITION_REAL_TIME_CHARTS:
                if (position != lastFragmentSetPosition) {
                    drawerList.setItemChecked(position - 1, true);
                    externalDrawerChange = true;
                    lastFragmentSetPosition = position;
                }
                break;
            default:
                L.w(TAG,"setDrawerPosition called with an invalid position");
        }
    }

    private class DrawerScrollListener implements AbsListView.OnScrollListener {

        private final Bundle b;

        DrawerScrollListener(final Bundle b) {
            this.b = b;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) { L.v(TAG, "onScrollStateChanged");}

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            L.v(TAG, "onScroll");
            ShamuMainActivity.this.setSelection((ListView)view, b);
        }
    }

    private void launchFragment(int position, Bundle b, boolean viaOncreate) {
        if (shamu.getMainActivityRefresher() != null) {
            //We do not allow fragment changes on a refresh.  The current refresh must complete.  For some strange reason
            //all AsyncTasks wait for the refresh to complete before starting, leaving uninitialized UIs.
            L.w(TAG, "The drawer is ignoring a fragment request launch.  The current fragment is refreshing!");
            if(!dialogging && !viaOncreate)
                buildAlertDialog(getString(R.string.drawer_currently_refreshing_message),getString(R.string.drawer_currently_refreshing_title));
            return;
        }

        int lastPosition = b.getInt(LAST_DRAWER_SELECTION);
        L.d(TAG,"position(1 based) = " + position + " : last position = " + lastPosition);
        if((position == lastPosition) && !viaOncreate && !externalDrawerChange)
            return;
        externalDrawerChange = false;
        switch (position) {
            case POSITION_ALERT:
                L.v(TAG, "Drawer is sending me to the alert fragment");
                drawer.closeDrawer(Gravity.LEFT);
                PullToRefreshAlertsFragment aFrag = new PullToRefreshAlertsFragment();
                if (!viaOncreate) {//viaOnCreate will often mean it is via rotation (but not always!)
                    Bundle bund = new Bundle();
                    bund.putBoolean(SENT_VIA_DRAWER, true);
                    aFrag.setArguments(bund);
                    L.d(TAG, "From drawer = " + SENT_VIA_DRAWER);
                }
                getFragmentManager().beginTransaction().replace(R.id.shamu_fragment_content_frame, aFrag).commit();
                lastFragmentSetPosition = POSITION_ALERT;
                b.putInt(LAST_DRAWER_SELECTION,POSITION_ALERT );
                break;
            case POSITION_REPORT:
                L.v(TAG,"Drawer is sending me to the report fragment");
                drawer.closeDrawer(Gravity.LEFT);
                PullToRefreshReportsFragment rFrag = new PullToRefreshReportsFragment();
                if (!viaOncreate) {
                    Bundle bund = new Bundle();
                    bund.putBoolean(SENT_VIA_DRAWER, true);
                    rFrag.setArguments(bund);
                }
                getFragmentManager().beginTransaction().replace(R.id.shamu_fragment_content_frame, rFrag).commit();
                lastFragmentSetPosition = POSITION_REPORT;
                b.putInt(LAST_DRAWER_SELECTION,POSITION_REPORT);
                break;
            case POSITION_CONFIGURATION:
                L.v(TAG, "Drawer is sending me to the configuration activity");
                drawer.closeDrawer(Gravity.LEFT);
                lastFragmentSetPosition = POSITION_CONFIGURATION;
                b.putInt(LAST_DRAWER_SELECTION,POSITION_CONFIGURATION);
                getFragmentManager().beginTransaction().replace(R.id.shamu_fragment_content_frame, new ShamuConfigurationFragment()).commit();
                break;
            case POSITION_SETTINGS:
                L.v(TAG,"Drawer is sending me to the settings fragment");
                drawer.closeDrawer(Gravity.LEFT);
                getFragmentManager().beginTransaction().replace(R.id.shamu_fragment_content_frame, new SettingsFragment()).commit();
                lastFragmentSetPosition = POSITION_SETTINGS;
                b.putInt(LAST_DRAWER_SELECTION, POSITION_SETTINGS);
                break;
            case POSITION_REAL_TIME_CHARTS:
                L.v(TAG,"Drawer is sending me to the Real TIme Charts fragment");
                drawer.closeDrawer(Gravity.LEFT);
                getFragmentManager().beginTransaction().replace(R.id.shamu_fragment_content_frame, new RealTimeChartFragment()).commit();//
                lastFragmentSetPosition = POSITION_REAL_TIME_CHARTS;
                b.putInt(LAST_DRAWER_SELECTION,POSITION_REAL_TIME_CHARTS);
                break;
            default:
                throw new IllegalArgumentException("Please keep " + TAG + " in sync with the drawer constants contained in Strings.xml!");

        }
    }
    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        private final Bundle b;

        DrawerItemClickListener(Bundle b) {
            this.b = b;
        }

        FragmentManager fragmentManager = getFragmentManager();
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            position = position + 1;
            drawerList.setItemChecked(position, true);
            drawer.closeDrawer(Gravity.LEFT);
            launchFragment(position,b, false);//When putting position in a bundle zero means not found.  We adjust to one based...
            ShamuMainActivity.this.setSelection((ListView) view.getParent(), b);
            lastFragmentSetPosition = position;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return  super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
       // boolean drawerOpen = drawer.isDrawerOpen(drawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void setUpHomeIcon() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
       // getActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
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
        super.onSaveInstanceState(state);
		L.i(TAG, "onSaveInstanceState");
        if (lastFragmentSetPosition != null) {
            L.v(TAG, "Writing LAST_DRAWER_SELECTION to the bundle=" + lastFragmentSetPosition);
            state.putInt(ShamuMainActivity.LAST_DRAWER_SELECTION, lastFragmentSetPosition);
        }
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
	protected void onPostCreate(Bundle bundle) {
		super.onPostCreate(bundle);
		L.i(TAG, "onPostCreate");
        drawerToggle.syncState();
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
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

    protected void setupReturnHomeOnAppIConClick() {
        ActionBar actionBar = getActionBar();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    private String lowercase(String line)
    {
        return Character.toLowerCase(line.charAt(0)) + line.substring(1);
    }

    @Override
    protected boolean listeningSupported() {
        return true;
    }

    @Override
    public void onBackPressed() {
        L.d(TAG,"onBackPressed");
    }
}