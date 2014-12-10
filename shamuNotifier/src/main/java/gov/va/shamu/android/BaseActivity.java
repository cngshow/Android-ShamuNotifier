package gov.va.shamu.android;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import gov.va.shamu.android.utilities.L;

/**
 * The base activity
 */
public class BaseActivity extends Activity {
	private final String TAG = this.getClass().getSimpleName();

	ShamuNotifierApplication shamu;
	protected Boolean dialogging = Boolean.FALSE;
	protected String currentDialogTitle, currentDialogMessage;
	protected AlertDialog currentDialog;

	static final String DIALOG_STATE = "gov.va.shamu.android.DIALOG_STATE";
	static final String DIALOG_TITLE = "gov.va.shamu.android.DIALOG_TITLE";
	static final String DIALOG_MESSAGE = "gov.va.shamu.android.DIALOG_MESSAGE";
	static final String PREFERENCE_SERVER_TOGGLE = "gov.va.shamu.android.PREFERENCE_SERVER_TOGGLE";
	static final String OUT_OF_SYNC_SERVER_TOGGLE = "gov.va.shamu.android.OUT_OF_SYNC_SERVER_TOGGLE";

    public ShamuNotifierApplication getShamu() {
        return shamu;
    }

    void buildAlertDialog(String message, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(message)
        .setTitle(title);
        builder.setPositiveButton(getString(R.string.configuration_invalid_url_dialogue_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                dialogging = false;
                currentDialogMessage = null;
                currentDialogTitle = null;
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        dialogging = true;
        currentDialogMessage = message;
        currentDialogTitle = title;
        currentDialog = alert;
	}
	
	protected void startActivityWithDialog(Context c, Class<?> clazz, String message, String title) {
        Intent intent = new Intent(c, clazz);
        intent.putExtra(BaseActivity.DIALOG_STATE, true);
        intent.putExtra(BaseActivity.DIALOG_TITLE, title);
        intent.putExtra(BaseActivity.DIALOG_MESSAGE, message);
        startActivity(intent);
    }

	protected void startFragmentWithDialog(Context c, Fragment frag, String message, String title) {
        Bundle b = new Bundle();
        b.putBoolean(BaseActivity.DIALOG_STATE, true);
        b.putString(BaseActivity.DIALOG_TITLE, title);
        b.putString(BaseActivity.DIALOG_MESSAGE, message);
        frag.setArguments(b);
        getFragmentManager().beginTransaction().replace(R.id.shamu_fragment_content_frame, frag).commit();
    }
	
	 void checkForDialog(Bundle extras) {
         if (!dialogging) {
             if (extras != null && extras.containsKey(DIALOG_STATE) && extras.getBoolean(DIALOG_STATE)) {
                 buildAlertDialog(extras.getString(DIALOG_MESSAGE), extras.getString(DIALOG_TITLE));
                 extras.remove(DIALOG_STATE);
                 extras.remove(DIALOG_MESSAGE);
                 extras.remove(DIALOG_TITLE);
             }
         }
     }

	 void checkForDialog() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
        checkForDialog(extras);
	}

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		shamu = (ShamuNotifierApplication) getApplication();
        setUpHomeIcon();
        if (savedState != null && savedState.containsKey(DIALOG_STATE)) {
            if (savedState.getBoolean(DIALOG_STATE)) {
                dialogging = true;
                currentDialogMessage = savedState.getString(DIALOG_MESSAGE);
                currentDialogTitle = savedState.getString(DIALOG_TITLE);
               // buildAlertDialog(currentDialogMessage, currentDialogTitle);//called in onResume
            }
        } else if(dialogging)
            buildAlertDialog(currentDialogMessage, currentDialogTitle);
    }

	// Called only once first time menu is clicked on
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.shamu_menu, menu);
		return true;
	}
	
	protected void onPause() {
		super.onPause();
		// Notification that the activity will stop interacting with the user
		L.i(TAG, "onPause" + (isFinishing() ? " Finishing" : ""));
        if(dialogging)
            currentDialog.dismiss();
	}
	

	@Override
	protected void onSaveInstanceState(Bundle state) {
		// Save instance-specific state
		super.onSaveInstanceState(state);
		L.i(TAG, "onSaveInstanceState");
		if (dialogging) {
			state.putBoolean(DIALOG_STATE, dialogging);
			state.putString(DIALOG_TITLE, currentDialogTitle);
			state.putString(DIALOG_MESSAGE, currentDialogMessage);
            //dialogging = false;//Only do this when the OKJ button is pressed!
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

    @Override
    protected void onResume() {
        L.d(TAG,"onResume");
        super.onResume();
        if(dialogging)
            buildAlertDialog(currentDialogMessage, currentDialogTitle);
    }
	
	// Called every time user clicks on a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        boolean optionFound = false;
		switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, ShamuMainActivity.class));
                optionFound = true;
                break;
//		case R.id.menu_preferences:
//			 startActivity(new Intent(this, ShamuConfigurationActivity.class));
//            optionFound = true;
//			// .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
//			break;
		case R.id.toggle_service:
			Intent intent = new Intent(this, ShamuUpdaterService.class);
			intent.setAction(PREFERENCE_SERVER_TOGGLE);
			if (shamu.isServiceRunning()) {
				stopService(intent);
			} else {
				startService(intent);
			}
            optionFound = true;
			break;
		}
		return optionFound;
	}

	// Called every time menu is opened
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		//Menu menu_l = (Menu)this.findViewById(R.menu.shamu_menu);
		MenuItem toggleItem = menu.findItem(R.id.toggle_service);
		setToggleService(toggleItem);
		return true;
	}

	private void setToggleService(MenuItem item) {
		if (shamu.isServiceRunning()) {
			item.setTitle(R.string.service_active_string);
		} else {
			item.setTitle(R.string.service_inactive_string);
		}
	}

    protected void setUpHomeIcon() {
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
    }

    protected void setPageTitle(String title) {
        ActionBar actionBar = getActionBar();
        String t = getString(R.string.app_name) + "-" + title;
        actionBar.setTitle(t);
    }
}
