package gov.va.shamu.android.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

import gov.va.shamu.android.provider.ShamuData.Reports;
import gov.va.shamu.android.provider.ShamuData.Alerts;
import gov.va.shamu.android.provider.ShamuData.Preferences;
import gov.va.shamu.android.utilities.InterestingAlert;
import gov.va.shamu.android.utilities.L;

public class ShamuNotifierContentProvider extends ContentProvider {

	private static final String TAG = ShamuNotifierContentProvider.class
			.getSimpleName();
	private static final int MANY_ALERTS = 1;
	private static final int ONE_ALERT = 2;
	private static final int MANY_PREFERENCES = 3;
	private static final int ONE_PREFERENCE = 4;
	private static final int MANY_INTERESTING_ALERTS = 5;
	private static final int ONE_INTERESTING_ALERT = 6;
    private static final int MANY_REPORTS = 7;
    private static final int ONE_REPORT = 8;
	private static UriMatcher sUriMatcher;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(ShamuData.AUTHORITY,
				Alerts.ALERTS_TABLE_NAME, MANY_ALERTS);
		sUriMatcher.addURI(ShamuData.AUTHORITY,
				Alerts.ALERTS_TABLE_NAME + "/#", ONE_ALERT);
		sUriMatcher.addURI(ShamuData.AUTHORITY,
				Reports.REPORTS_TABLE_NAME, MANY_REPORTS);
		sUriMatcher.addURI(ShamuData.AUTHORITY,
                Reports.REPORTS_TABLE_NAME + "/#", ONE_REPORT);
		sUriMatcher.addURI(ShamuData.AUTHORITY,
				Preferences.PREFERENCES_TABLE_NAME + "/#",
				ONE_PREFERENCE);
		sUriMatcher.addURI(ShamuData.AUTHORITY,
				Preferences.PREFERENCES_TABLE_NAME, MANY_PREFERENCES);
		sUriMatcher.addURI(ShamuData.AUTHORITY,
				ShamuData.InterestingAlertsData.INTERESTING_ALERTS_VIEW_NAME + "/#",
				ONE_INTERESTING_ALERT);
		sUriMatcher.addURI(ShamuData.AUTHORITY,
				ShamuData.InterestingAlertsData.INTERESTING_ALERTS_VIEW_NAME, MANY_INTERESTING_ALERTS);
	}

	private static class ShamuNotifierDbHelper extends SQLiteOpenHelper {
		private static final String TAG = ShamuNotifierDbHelper.class
				.getSimpleName();

		// creates shamu_notifier.db
		public static final String DATABASE_NAME = "shamu_notifier.db";
		private static int DATABASE_VERSION = 2;

		ShamuNotifierDbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase) {
			L.i(TAG, "Created our sqlite database " + DATABASE_NAME);
			createAlertsTable(sqLiteDatabase);
			createReportsTable(sqLiteDatabase);
			createPreferencesTable(sqLiteDatabase);
			createInterestingAlertsView(sqLiteDatabase);
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
			L.i(TAG, "Upgrading database from version " + oldv + " to version "
					+ newv);
			onCreate(sqLiteDatabase);
		}
		
		@Override
		public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
			L.i(TAG, "Downgrading database from version " + oldv + " to version "
					+ newv);
			onCreate(sqLiteDatabase);		}

		private void createPreferencesTable(SQLiteDatabase sqLiteDatabase) {
			dropTableOrView(sqLiteDatabase, Preferences.PREFERENCES_TABLE_NAME, true);
			
			String qs = "CREATE TABLE "
					+ Preferences.PREFERENCES_TABLE_NAME + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ Preferences.KEY_COLUMN_NAME + " TEXT, "
					+ Preferences.VALUE_COLUMN_NAME + " TEXT);";
			sqLiteDatabase.execSQL(qs);
            setupPreferencesDefaultData(sqLiteDatabase);
		}

        private void setupPreferencesDefaultData(SQLiteDatabase sqLiteDatabase) {
            String sql = "";
            //create the default vibrate preference
            sql = "Insert into " + Preferences.PREFERENCES_TABLE_NAME + " ( "
            +  Preferences.KEY_COLUMN_NAME + ", " + Preferences.VALUE_COLUMN_NAME + ") VALUES ( ?, ? )";

            L.i(TAG, "setting up default preferences");
            sqLiteDatabase.execSQL(sql, new String[] {Preferences.SETTINGS_VIBRATE, Boolean.TRUE.toString()});
            sqLiteDatabase.execSQL(sql, new String[] {Preferences.SETTINGS_AUDIBLE, Boolean.TRUE.toString()});
            sqLiteDatabase.execSQL(sql, new String[] {Preferences.SETTINGS_ANNOUNCE, Boolean.FALSE.toString()});
            L.i(TAG, "default preferences completed!");

/*
            //create the default audible preference
            sql = "Insert into " + Preferences.PREFERENCES_TABLE_NAME + " ( "
            +  Preferences.KEY_COLUMN_NAME + ", " + Preferences.VALUE_COLUMN_NAME + ") VALUES ( "
            +  Preferences.SETTINGS_AUDIBLE + ", true)";
            sqLiteDatabase.execSQL(sql);

            //create the default announce preference
            sql = "Insert into " + Preferences.PREFERENCES_TABLE_NAME + " ( "
            +  Preferences.KEY_COLUMN_NAME + ", " + Preferences.VALUE_COLUMN_NAME + ") VALUES ( "
            +  Preferences.SETTINGS_ANNOUNCE + ", false)";
            sqLiteDatabase.execSQL(sql);
*/
        }

		private void createAlertsTable(SQLiteDatabase sqLiteDatabase) {
			dropTableOrView(sqLiteDatabase, Alerts.ALERTS_TABLE_NAME, true);
			String qs = getCreateAlertSQL();
			sqLiteDatabase.execSQL(qs);
		}

		private void createReportsTable(SQLiteDatabase sqLiteDatabase) {
			dropTableOrView(sqLiteDatabase, ShamuData.Reports.REPORTS_TABLE_NAME, true);
			String qs = getCreateReportSQL();
			sqLiteDatabase.execSQL(qs);
		}

		private void createInterestingAlertsView(SQLiteDatabase sqLiteDatabase) {
			dropTableOrView(sqLiteDatabase, "v_interesting_alerts", false);
			String qs = getCreateInterestingAlertsViewSQL();
			sqLiteDatabase.execSQL(qs);
		}

		private void dropTableOrView(SQLiteDatabase sqLiteDatabase, String tableName, boolean table) {
			sqLiteDatabase.execSQL("DROP " + (table ? "table" : "view") + " IF EXISTS " + tableName + ";");
		}

		private String getCreateInterestingAlertsViewSQL() {
			String[] alertColumns = Alerts.ALL_COLUMNS;
			StringBuilder sb = new StringBuilder("create view v_interesting_alerts as select ");
			
			for (int i = 0; i < alertColumns.length; i++) {
				sb.append("alerts.").append(alertColumns[i]).append(", ");
			}

			sb.append("ifnull(preferences.").append(Preferences.VALUE_COLUMN_NAME).append(",'false') as is_interesting ");
			sb.append("from alerts left outer join preferences on (alerts.job_code = preferences.key) ");
			sb.append("order by alerts.job_code asc;");
			String rVal = sb.toString();
			L.d(TAG, "Create Interesting Alerts View sql is: " + rVal);
			return rVal;
		}
		
		private String getCreateAlertSQL() {
			String[] allColumns = Alerts.ALL_COLUMNS;
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE ").append(Alerts.ALERTS_TABLE_NAME)
					.append("( ");
			sb.append(BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
			// Starting from 1 to ignore id column and ignore last
			for (int i = 1; i < allColumns.length - 1; i++) {
				sb.append(allColumns[i]).append(" TEXT, ");
			}
			sb.append(allColumns[allColumns.length - 1]).append(" TEXT);");
			String rVal = sb.toString();
			L.d(TAG, "Create Alert sql is: " + rVal);
			return rVal;

		}
		private String getCreateReportSQL() {
			String[] allColumns = Reports.ALL_COLUMNS;
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE ").append(Reports.REPORTS_TABLE_NAME)
					.append("( ");
			sb.append(BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
			// Starting from 1 to ignore id column and ignore last
			for (int i = 1; i < allColumns.length - 1; i++) {
				sb.append(allColumns[i]).append(" TEXT, ");
			}
			sb.append(allColumns[allColumns.length - 1]).append(" TEXT);");
			String rVal = sb.toString();
			L.d(TAG, "Create Report sql is: " + rVal);
			return rVal;
		}
	}

	// _ID, JOB_CODE_COLUMN_NAME,
	// JLE_ID_COLUMN_NAME, JOB_METADATA_ID_COLUMN_NAME,
	// SHORT_DESCRIPTION_COLUMN_NAME, LONG_DESCRIPTION_COLUMN_NAME, LAST_COMPLETED_COLUMN_NAME,
	// STATUS_COLUMN_NAME, ALERT_START_COLUMN_NAME,
	// ELAPSED_TIME_COLUMN_NAME, JOB_RESULT_COLUMN_NAME,
	// URL_COLUMN_NAME };

	public ShamuNotifierContentProvider() {
		// TODO Auto-generated constructor stub
	}

	private ShamuNotifierDbHelper mOpenDbHelper;

	@Override
	public boolean onCreate() {
		mOpenDbHelper = new ShamuNotifierDbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String where,
			String[] whereArgs, String sortOrder) {

		int match = sUriMatcher.match(uri);
		String orderByA = (sortOrder == null) ? Alerts.DEFAULT_SORT_ORDER
				: sortOrder;
		String orderByP = (sortOrder == null) ? Preferences.DEFAULT_SORT_ORDER
				: sortOrder;
		
		String orderByI = (sortOrder == null) ? ShamuData.InterestingAlertsData.DEFAULT_SORT_ORDER
				: sortOrder;

		Cursor c;
		switch (match) {
		case MANY_ALERTS:
			c = getDb().query(Alerts.ALERTS_TABLE_NAME, projection,
					where, whereArgs, null, null, orderByA);

			c.setNotificationUri(getContext().getContentResolver(),
					Alerts.CONTENT_URI);
			break;
		case ONE_ALERT:
			// query the database for a specific video
			long alertID = ContentUris.parseId(uri);
			c = getDb().query(Alerts.ALERTS_TABLE_NAME, projection,
					BaseColumns._ID + " = " + alertID, null, null, null,
					orderByA);
			c.setNotificationUri(getContext().getContentResolver(),
					Alerts.CONTENT_URI);
			break;
		case MANY_REPORTS:
			c = getDb().query(Reports.REPORTS_TABLE_NAME, projection,
					where, whereArgs, null, null, orderByA);

			c.setNotificationUri(getContext().getContentResolver(),
					Reports.CONTENT_URI);
			break;
		case ONE_REPORT:
			long reportId = ContentUris.parseId(uri);
			c = getDb().query(Reports.REPORTS_TABLE_NAME, projection,
					BaseColumns._ID + " = " + reportId, null, null, null,
					orderByA);
			c.setNotificationUri(getContext().getContentResolver(),
					Reports.CONTENT_URI);
			break;
		case MANY_PREFERENCES:
			L.d(TAG, "MANY_PREFERENCES query");
			c = getDb().query(Preferences.PREFERENCES_TABLE_NAME,
					projection, where, whereArgs, null, null, orderByP);

			c.setNotificationUri(getContext().getContentResolver(),
					Preferences.CONTENT_URI);
			break;

		case ONE_PREFERENCE:
			L.d(TAG, "ONE_PREFERENCE query");
			// query the database for a specific video
			long preferenceID = ContentUris.parseId(uri);
			c = getDb().query(Alerts.ALERTS_TABLE_NAME, projection,
					BaseColumns._ID + " = " + preferenceID, null, null, null,
					orderByP);
			c.setNotificationUri(getContext().getContentResolver(),
					Preferences.CONTENT_URI);
			break;
			
		case ONE_INTERESTING_ALERT:
			L.d(TAG, "One interesting alert");
			throw new IllegalArgumentException("One interesting alert query is unsupported");
		
		case MANY_INTERESTING_ALERTS:
			L.d(TAG, "MANY interesting alert");
			c = getDb().query(ShamuData.InterestingAlertsData.INTERESTING_ALERTS_VIEW_NAME, projection,
					where, whereArgs, null, null, orderByI);

			c.setNotificationUri(getContext().getContentResolver(),
					Alerts.CONTENT_URI);
			break;
			
		default:
			throw new IllegalArgumentException("unsupported uri: " + uri);
		}

		return c;

	}

	@Override
	public String getType(Uri uri) {
		L.d(TAG, "getType called for Uri " + uri.toString());
		switch (sUriMatcher.match(uri)) {
		case MANY_ALERTS:
			return Alerts.CONTENT_TYPE_MANY;
		case ONE_ALERT:
			return Alerts.CONTENT_TYPE_ONE;
		case MANY_REPORTS:
			return Reports.CONTENT_TYPE_MANY;
		case ONE_REPORT:
			return Reports.CONTENT_TYPE_ONE;

		case ONE_PREFERENCE:
			return Preferences.CONTENT_TYPE_ONE;
		case MANY_PREFERENCES:
			return Preferences.CONTENT_TYPE_MANY;
			
		case ONE_INTERESTING_ALERT:
			return ShamuData.InterestingAlertsData.CONTENT_TYPE_ONE;
			
		case MANY_INTERESTING_ALERTS:
			return ShamuData.InterestingAlertsData.CONTENT_TYPE_MANY;

		default:
			throw new IllegalArgumentException(
					"Unknown URI type: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		L.d(TAG, "Insert called for URI " + uri.toString());
		String type = getType(uri);
		checkInterestingNotSupported(uri);
		// getUriType will throw an exception when an invalid uri is given
        final UriType uriType = getUriType(uri);
        Object[] dml = getDMLInfo(uriType);

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		// insert the initialValues into a new database row
		SQLiteDatabase db = mOpenDbHelper.getWritableDatabase();
		long rowId = db.insert((String)dml[0], (String)dml[1], values);
		if (rowId > 0) {
			Uri resultURI = ContentUris.withAppendedId((Uri)dml[2], rowId);
			getContext().getContentResolver().notifyChange(resultURI, null);
			L.d(TAG, "insert occurred, returning " + resultURI.toString());
			return resultURI;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

    private Object[] getDMLInfo(UriType uriType) {
        String tableName = null;
        String columnName = null;
        Uri contentURI = null;

        if (uriType == UriType.ALERT) {
            tableName = Alerts.ALERTS_TABLE_NAME;
            columnName = Alerts.JOB_CODE_COLUMN_NAME;
            contentURI = Alerts.CONTENT_URI;
        }
        if (uriType == UriType.REPORT) {
            tableName = Reports.REPORTS_TABLE_NAME;
            columnName = Reports.JOB_CODE_COLUMN_NAME;
            contentURI = Reports.CONTENT_URI;
        }
        if (uriType == UriType.PREFERENCE) {
            tableName = Preferences.PREFERENCES_TABLE_NAME;
            columnName = Preferences.KEY_COLUMN_NAME;
            contentURI = Preferences.CONTENT_URI;
        }
        return new Object[]{tableName, columnName, contentURI};
    }

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		L.d(TAG, "Delete called for URI " + uri);
		checkInterestingNotSupported(uri);
		int match = sUriMatcher.match(uri);
        L.v(TAG, "Delete: match is " + match);
        int affected = 0;
        final UriType uriType = getUriType(uri);
        L.v(TAG, "Delete: uriType is " + uriType);
        Object[] dml = getDMLInfo(uriType);

		switch (match) {
		case MANY_ALERTS:
        case MANY_REPORTS:
		case MANY_PREFERENCES:
			affected = getDb().delete((String)dml[0], where, whereArgs);
			L.d(TAG, "Many delete called for table " + (String)dml[0] + " "
					+ affected + " row(s) affected.");
			break;
		case ONE_ALERT:
		case ONE_REPORT:
		case ONE_PREFERENCE:
			long id = ContentUris.parseId(uri);
			affected = getDb().delete((String)dml[0], BaseColumns._ID + "=" + id,
					null);
			L.d(TAG, "One delete " + affected);
			break;
		default:
			L.wtf(TAG,
					"delete: Horrifying mismatch between getType and the urimatcher!");
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return affected;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
        L.d(TAG, "Update called for URI " + uri);
        int match = sUriMatcher.match(uri);
        L.v(TAG, "Update: match is " + match);
        checkInterestingNotSupported(uri);
		// getUriType will throw an exception when an invalid uri is given
//		String tableName = (getUriType(uri)) ? Alerts.ALERTS_TABLE_NAME
//				: Preferences.PREFERENCES_TABLE_NAME;

        final UriType uriType = getUriType(uri);
        L.v(TAG, "Update: uriType is " + uriType);

        Object[] dml = getDMLInfo(uriType);
		L.d(TAG, "Update against table " + (String)dml[0]);

		int affected = 0;

		switch (match) {
		case MANY_ALERTS:
		case MANY_REPORTS:
		case MANY_PREFERENCES:
			L.d(TAG, "MANY_ALERTS / MANY_PREFERENCES update");
			affected = getDb().update((String)dml[0], values, where, whereArgs);
			break;

		case ONE_ALERT:
        case ONE_REPORT:
        case ONE_PREFERENCE:
			L.d(TAG, "ONE_ALERT / ONE_PREFERENCE update");
			String id = uri.getPathSegments().get(1);
			affected = getDb().update((String)dml[0], values,
					BaseColumns._ID + "=" + id, null);
			break;

		default:
			L.wtf(TAG,
					"update: Horrifying mismatch between getType and the urimatcher!");
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return affected;

	}

	private SQLiteDatabase getDb() {
		return mOpenDbHelper.getWritableDatabase();
	}
	
	private void checkInterestingNotSupported(Uri uri) {
		String type = getType(uri);
		if (type.equals(ShamuData.InterestingAlertsData.CONTENT_TYPE_MANY) ||
				type.equals(ShamuData.InterestingAlertsData.CONTENT_TYPE_ONE))
			throw new IllegalArgumentException("Not supported for multiple tables");
	}

	private UriType getUriType(Uri uri) {
        String type = getType(uri);
        UriType ret = null;

        if (type.equals(Alerts.CONTENT_TYPE_MANY) || type.equals(Alerts.CONTENT_TYPE_ONE)) {
            ret = UriType.ALERT;
        }
        if (type.equals(Reports.CONTENT_TYPE_MANY) || type.equals(Reports.CONTENT_TYPE_ONE)) {
            ret = UriType.REPORT;
        }
        if (type.equals(Preferences.CONTENT_TYPE_MANY) || type.equals(Preferences.CONTENT_TYPE_ONE)) {
            ret = UriType.PREFERENCE;
        }
        if (type.equals(ShamuData.InterestingAlertsData.CONTENT_TYPE_MANY) || type.equals(ShamuData.InterestingAlertsData.CONTENT_TYPE_ONE)) {
            ret = UriType.INTERESTING_ALERTS;
        }
        return ret;
    }

    public enum UriType {
        REPORT, ALERT, PREFERENCE, INTERESTING_ALERTS
    }
}

