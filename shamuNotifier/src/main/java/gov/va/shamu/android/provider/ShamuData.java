package gov.va.shamu.android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.va.shamu.android.utilities.InterestingAlert;
import gov.va.shamu.android.utilities.L;
import gov.va.shamu.android.utilities.Report;

public class ShamuData {
	public static final int ID_COLUMN = 0;
	public static final int KEY = 1;
	public static final int VALUE = 2;

	// this defines the path to the database
	public static final String AUTHORITY = "gov.va.shamu.android.ShamuNotifier";

	// root@android:/data/data/com.oreilly.demo.android.pa.finchvideo/databases
	// # ls\//BTW this damn well better match up with
	// this portion of the xml in AndroidManifest.xml
	// <provider
	// android:name=".provider.SimpleFinchVideoContentProvider"
	// android:authorities="com.oreilly.demo.android.pa.finchvideo.SimpleFinchVideo"
	// />

	public static final class Alerts implements AlertDataColumns {
		public static final String DEFAULT_SORT_ORDER = "job_code asc";
		// creates a table called alerts in shamu_notifier.db
		public static final String ALERTS_TABLE_NAME = "alerts";

		private static final Map<String, Integer> columnMapping = new HashMap<String, Integer>();

		// This class cannot be instantiated
		private Alerts() {
		}

		// uri references all alerts (by job code)
		public static final Uri ALERTS_URI = Uri.parse("content://" + AUTHORITY
				+ "/" + ALERTS_TABLE_NAME);

		/**
		 * The content:// style URI for this table
		 */
		public static final Uri CONTENT_URI = ALERTS_URI;

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * alerts.
		 */
		public static final String CONTENT_TYPE_MANY = "vnd.android.cursor.dir/vnd.shamu.alert";
		public static final String CONTENT_TYPE_ONE = "vnd.android.cursor.item/vnd.shamu.alert";

		/**
		 * Column name
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		static {
			for (int i = 0; i < ALL_COLUMNS.length; i++) {
				columnMapping.put(ALL_COLUMNS[i], i);
			}
		}

		public static int getColumn(String s) {
			Integer column = columnMapping.get(s);
			if (column == null)
				throw new IllegalArgumentException(
						"Alerts table does not have a column named " + s);
			return column;
		}

	}

	public static final class Reports implements ReportDataColumns {
		public static final String DEFAULT_SORT_ORDER = "job_code asc";
		// creates a table called reports in shamu_notifier.db
		public static final String REPORTS_TABLE_NAME = "reports";

		private static final Map<String, Integer> columnMapping = new HashMap<String, Integer>();

		// This class cannot be instantiated
		private Reports() {
		}

		// uri references all alerts (by job code)
		public static final Uri REPORTS_URI = Uri.parse("content://" + AUTHORITY
				+ "/" + REPORTS_TABLE_NAME);

        // The content:// style URI for this table
		public static final Uri CONTENT_URI = REPORTS_URI;

		 //The MIME type of {@link #CONTENT_URI} providing a directory of alerts.
		public static final String CONTENT_TYPE_MANY = "vnd.android.cursor.dir/vnd.shamu.report";
		public static final String CONTENT_TYPE_ONE = "vnd.android.cursor.item/vnd.shamu.report";

		/**
		 * Column name
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		static {
			for (int i = 0; i < ALL_COLUMNS.length; i++) {
				columnMapping.put(ALL_COLUMNS[i], i);
			}
		}

		public static int getColumn(String s) {
			Integer column = columnMapping.get(s);
			if (column == null)
				throw new IllegalArgumentException(
						"Reports table does not have a column named " + s);
			return column;
		}


        public static  List<Report> getAllReports(ContentResolver cr) {
            List<Report> rVal = new ArrayList<Report>();
            Cursor c = cr.query(Reports.CONTENT_URI, null, null, null, Reports.DEFAULT_SORT_ORDER);
            boolean dataAvailable = c.moveToFirst();
            while (dataAvailable) {
                Report report = new Report(cr, c);
                rVal.add(report);
                dataAvailable = c.moveToNext();
            }
            return rVal;
        }

	}

	public static final class InterestingAlertsData implements
            InterestingAlertDataColumns {

		public static final String INTERESTING_ALERTS_VIEW_NAME = "v_interesting_alerts";

		public static final String DEFAULT_SORT_ORDER = "status desc, job_code asc";

		private static final Map<String, Integer> columnMapping = new HashMap<String, Integer>();

		// This class cannot be instantiated
		private InterestingAlertsData() {
		}

		// uri references all alerts (by job code)
		public static final Uri INTERESTING_ALERTS_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + INTERESTING_ALERTS_VIEW_NAME);

		/**
		 * The content:// style URI for this table
		 */
		public static final Uri CONTENT_URI = INTERESTING_ALERTS_URI;

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * alerts.
		 */
		public static final String CONTENT_TYPE_MANY = "vnd.android.cursor.dir/vnd.shamu.interesting_alert";
		public static final String CONTENT_TYPE_ONE = "vnd.android.cursor.item/vnd.shamu.interesting_alert";

		/**
		 * Column name
		 * <P>
		 * Type: TEXT
		 * </P>
		 */

		static {
			for (int i = 0; i < ALL_COLUMNS.length; i++) {
				columnMapping.put(ALL_COLUMNS[i], i);
			}
		}

		public static int getColumn(String s) {
			Integer column = columnMapping.get(s);
			if (column == null)
				throw new IllegalArgumentException(
						"Interesting Alerts view does not have a column named "
								+ s);
			return column;
		}
		
		public static  List<InterestingAlert> getAllInterestingAlerts(ContentResolver cr) {
			List<InterestingAlert> rVal = new ArrayList<InterestingAlert>();
			Cursor c = cr.query(InterestingAlertsData.CONTENT_URI, null, null, null, InterestingAlertsData.DEFAULT_SORT_ORDER);
			boolean dataAvailable = c.moveToFirst();
			while (dataAvailable) {
				InterestingAlert alert = new InterestingAlert(cr, c);
				rVal.add(alert);
				dataAvailable = c.moveToNext();
			}
			return rVal;
		}

	}

	/**
	 * Videos content provider public API for more advanced videos example.
	 */
	public static final class Preferences implements BaseColumns {
		public static final String DEFAULT_SORT_ORDER = "key asc";
		// creates a table called preferences in shamu_notifier.db
		public static final String PREFERENCES_TABLE_NAME = "preferences";
		public static String TAG = "ShamuData.Preferences";
		
		public static final String SHAMU_URL_KEY = "gov.va.shamu.android.provider.SHAMU_URL_KEY";
		public static final String SHAMU_POLLING_INTERVAL_KEY = "gov.va.shamu.android.provider.SHAMU_POLLING_INTERVAL_KEY";
		public static final String SHAMU_SERVICE_STATE = "gov.va.shamu.android.provider.SHAMU_SERVICE_STATE";
		public static final String SETTINGS_VIBRATE = "gov.va.shamu.android.provider.SETTINGS_VIBRATE";
		public static final String SETTINGS_AUDIBLE = "gov.va.shamu.android.provider.SETTINGS_AUDIBLE";
		public static final String SETTINGS_ANNOUNCE = "gov.va.shamu.android.provider.SETTINGS_ANNOUNCE";

		// This class cannot be instantiated
		private Preferences() {
		}

		// uri references all prefernces
		public static final Uri PREFERENCES_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + PREFERENCES_TABLE_NAME);

		/**
		 * The content:// style URI for this table
		 */
		public static final Uri CONTENT_URI = PREFERENCES_URI;

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
		 */
		public static final String CONTENT_TYPE_MANY = "vnd.android.cursor.dir/vnd.shamu.preference";
		public static final String CONTENT_TYPE_ONE = "vnd.android.cursor.item/vnd.shamu.preference";

		public static final String KEY_COLUMN_NAME = "key";
		public static final int KEY_COLUMN_POSITION = 1;

		public static final String VALUE_COLUMN_NAME = "value";
		public static final int VALUE_COLUMN_POSITION = 2;

		public static int insertOrUpdatePreference(ContentResolver cr,
				CharSequence key, CharSequence value) {
			L.i(TAG, "insertOrUpdatePreference " + key + " -- " + value);
			int updates = 0;
			ContentValues cv = new ContentValues();
			cv.put(Preferences.KEY_COLUMN_NAME, key.toString());
			cv.put(Preferences.VALUE_COLUMN_NAME, value.toString());
			updates = cr.update(Preferences.CONTENT_URI, cv,
					Preferences.KEY_COLUMN_NAME + "='" + key + "'", null);
			L.d(TAG, "update attempt returned " + updates);
			if (updates == 0) {
				// do an insert
				Uri uri = cr.insert(Preferences.CONTENT_URI, cv);
				L.d(TAG, "insertorUpdate did an insert " + uri.toString());
				if (uri != null)
					updates = 1;
			}
			return updates;
		}

		public static  Map<String, String> getAllPreferences(ContentResolver cr) {
			Map<String, String> rVal = new HashMap<String, String>();
			Cursor c = null;
			try {
				c = cr.query(Preferences.CONTENT_URI, null, null, null, null);
				boolean dataAvailable = c.moveToFirst();
				while (dataAvailable) {
					String k = c.getString(Preferences.KEY_COLUMN_POSITION);
					String v = c.getString(Preferences.VALUE_COLUMN_POSITION);
					rVal.put(k, v);
					dataAvailable = c.moveToNext();
				}
			} finally {
				if (c!= null)
				  c.close();
			}
			return rVal;
		}
		
		public static String getPreference(ContentResolver cr, String prefKey) {
			String ret = null;
			Cursor c = null;
			try {
				c = cr.query(Preferences.CONTENT_URI, null, "key=?",
						new String[] { prefKey }, null);
				if (c.moveToFirst()) {
					ret = c.getString(Preferences.VALUE_COLUMN_POSITION);
				}
			}
			finally
			{
				if (c != null) {
					c.close();
				}
			}
			return ret;
		}
	}
}
