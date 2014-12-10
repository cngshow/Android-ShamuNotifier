package gov.va.shamu.android.utilities;
import android.util.Log;

//wrapper around the logger
public final class L {
	
	//Verbose
    private static final boolean VL = true;
    //Debug
	private static final boolean DL = true;
	//Info
	private static final boolean IL = true;
	//warning
	private static final boolean WL = true;
	//Error
	private static final boolean EL = true;
	//Everyone knows what WTF stands for!
	private static final boolean WTFL = true;

    private static final String COMMON_TAG = "";

	    /**
	     * Send a {@link #VERBOSE} log message.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     */
	    public static int v(String tag, String msg) {
            msg = msg + COMMON_TAG;
	        if(VL)
	        	return Log.v(tag, msg);
	        return 0;
	    }

	    /**
	     * Send a {@link #VERBOSE} log message and log the exception.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     * @param tr An exception to log
	     */
	    public static int v(String tag, String msg, Throwable tr) {
            msg = msg + COMMON_TAG;
	    	if (VL)
	    		return Log.v(tag, msg, tr);
	    	return 0;
	    }

	    /**
	     * Send a {@link #DEBUG} log message.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     */
	    public static int d(String tag, String msg) {
            msg = msg + COMMON_TAG;
	        if (DL)
	        	return Log.d(tag, msg);
	        return 0;
	    }

	    /**
	     * Send a {@link #DEBUG} log message and log the exception.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     * @param tr An exception to log
	     */
	    public static int d(String tag, String msg, Throwable tr) {
            msg = msg + COMMON_TAG;
	    	if(DL)
	    		return Log.d(tag, msg, tr);
	    	return 0;
	    }

	    /**
	     * Send an {@link #INFO} log message.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     */
	    public static int i(String tag, String msg) {
            msg = msg + COMMON_TAG;
	    	if(IL )
	    		return Log.i(tag, msg);
	    	return 0;
	    }

	    /**
	     * Send a {@link #INFO} log message and log the exception.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     * @param tr An exception to log
	     */
	    public static int i(String tag, String msg, Throwable tr) {
            msg = msg + COMMON_TAG;
	    	if (IL)
	    		return Log.i(tag, msg, tr);
	    	return 0;
	    }

	    /**
	     * Send a {@link #WARN} log message.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     */
	    public static int w(String tag, String msg) {
            msg = msg + COMMON_TAG;
	    	if(WL)
	    		return Log.w(tag, msg);
	    	return 0;
	    }

	    /**
	     * Send a {@link #WARN} log message and log the exception.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     * @param tr An exception to log
	     */
	    public static int w(String tag, String msg, Throwable tr) {
            msg = msg + COMMON_TAG;
	    	if (WL)
	    		return Log.w(tag, msg, tr);
	    	return 0;
	    }

	    /**
	     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
	     *
	     *  The default level of any tag is set to INFO. This means that any level above and including
	     *  INFO will be logged. Before you make any calls to a logging method you should check to see
	     *  if your tag should be logged. You can change the default level by setting a system property:
	     *      'setprop log.tag.&lt;YOUR_LOG_TAG> &lt;LEVEL>'
	     *  Where level is either VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS. SUPPRESS will
	     *  turn off all logging for your tag. You can also create a local.prop file that with the
	     *  following in it:
	     *      'log.tag.&lt;YOUR_LOG_TAG>=&lt;LEVEL>'
	     *  and place that in /data/local.prop.
	     *
	     * @param tag The tag to check.
	     * @param level The level to check.
	     * @return Whether or not that this is allowed to be logged.
	     * @throws IllegalArgumentException is thrown if the tag.length() > 23.
	     */
	    public static boolean isLoggable(String tag, int level) {
	    	return L.isLoggable(tag, level);
	    }

	    /*
	     * Send a {@link #WARN} log message and log the exception.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param tr An exception to log
	     */
	    public static int w(String tag, Throwable tr) {
            tag = tag + COMMON_TAG;
	    	if (WL)
	    		return Log.w(tag, tr);
	    	return 0;
	    }

	    /**
	     * Send an {@link #ERROR} log message.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     */
	    public static int e(String tag, String msg) {
            tag = tag + COMMON_TAG;
            if(EL )
	    		return Log.e(tag, msg);
	    	return 0;
	    }

	    /**
	     * Send a {@link #ERROR} log message and log the exception.
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     * @param tr An exception to log
	     */
	    public static int e(String tag, String msg, Throwable tr) {
            tag = tag + COMMON_TAG;
            if(EL)
	    		return Log.e(tag, msg, tr);
	    	return 0;
	    }

	    /**
	     * What a Terrible Failure: Report a condition that should never happen.
	     * The error will always be logged at level ASSERT with the call stack.
	     * Depending on system configuration, a report may be added to the
	     * {@link android.os.DropBoxManager} and/or the process may be terminated
	     * immediately with an error dialog.
	     * @param tag Used to identify the source of a log message.
	     * @param msg The message you would like logged.
	     */
	    public static int wtf(String tag, String msg) {
            tag = tag + COMMON_TAG;
            if (WTFL)
	    		return Log.wtf(tag, msg);
	    	return 0;
	    }

	    /**
	     * What a Terrible Failure: Report an exception that should never happen.
	     * Similar to {@link #wtf(String, String)}, with an exception to log.
	     * @param tag Used to identify the source of a log message.
	     * @param tr An exception to log.
	     */
	    public static int wtf(String tag, Throwable tr) {
            tag = tag + COMMON_TAG;
            if(WTFL)
	    		return Log.wtf(tag, tr);
	    	return 0;
	    }

	    /**
	     * What a Terrible Failure: Report an exception that should never happen.
	     * Similar to {@link #wtf(String, Throwable)}, with a message as well.
	     * @param tag Used to identify the source of a log message.
	     * @param msg The message you would like logged.
	     * @param tr An exception to log.  May be null.
	     */
	    public static int wtf(String tag, String msg, Throwable tr) {
            tag = tag + COMMON_TAG;
            if(WTFL)
	        	return Log.wtf(tag, msg, tr);
	        return 0;
	    }


	    /**
	     * Handy function to get a loggable stack trace from a Throwable
	     * @param tr An exception to log
	     */
	    public static String getStackTraceString(Throwable tr) {
	        return Log.getStackTraceString(tr);
	    }

	    /**
	     * Low-level logging call.
	     * @param priority The priority/type of this log message
	     * @param tag Used to identify the source of a log message.  It usually identifies
	     *        the class or activity where the log call occurs.
	     * @param msg The message you would like logged.
	     * @return The number of bytes written.
	     */
	    public static int println(int priority, String tag, String msg) {
	    	return Log.println(priority, tag, msg);
	    }

	}
