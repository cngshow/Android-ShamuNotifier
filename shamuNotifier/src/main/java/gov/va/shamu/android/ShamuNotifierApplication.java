/**
 * 
 */
package gov.va.shamu.android;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import java.util.HashMap;

import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.utilities.L;

/**
 * @author Cris and Greg
 *
 */
public class ShamuNotifierApplication extends Application implements TextToSpeech.OnInitListener {
	
	public static final String SERVICE_STATE = "gov.va.shamu.android.SERVICE_STATE";
	private static final String TAG = ShamuNotifierApplication.class.getSimpleName();
	private PullToRefreshAbstractBaseFragment.AsyncRefresh mainActivityRefresher;//change this to PullTorefreshFragment
//	private PullToRefreshAlertsFragment.AsyncRefresh mainActivityRefresher;//change this to PullTorefreshFragment
	private ShamuConfigurationFragment.AsyncTest preferenceActivityTester;
	private Boolean widgetNotifiedOfServicePreviousState = null;
    private TextToSpeech chatterBox = null;
    private boolean chatterBoxInitialized = false;
    private volatile boolean serviceRunning;
    private String serviceResult;
    private  AudioManager audioManager;
    private SpeakListener speakListener= new SpeakListener();
    private int currentMusicVolume;
    private int currentAlarmVolume;
    private int ttsStream;

    static {
        L.d(TAG, "ShamuNotifierApplication was loaded");
    }
    
	@Override
	public void onCreate() { 
		super.onCreate();
		L.d(TAG, "onCreated");
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        chatterBox(true);
    }

    private void chatterBox(boolean beChatty) {
        L.d(TAG,"chatterBox called! " + beChatty);
        if (beChatty) {
            if (chatterBox == null) {
                chatterBox = new TextToSpeech(this, this);
                chatterBox.setOnUtteranceProgressListener(speakListener);
            }
        } else {
            if (chatterBox != null) {
                chatterBox.stop();
                chatterBox.shutdown();
            }
        }
//        if (!chatterBox.isSpeaking() && chatterBoxInitialized) {
//            chatterBox.speak("Hello World!", TextToSpeech.QUEUE_FLUSH, null);
//        }
    }

    @Override
	public void onTerminate() { //
		super.onTerminate();
		L.d(TAG, "onTerminated");
        chatterBox(false);
	}



    public String getServiceResult() {
		return serviceResult;
	}

	public void setServiceResult(String serviceResult) {
		this.serviceResult = serviceResult;
	}

	public synchronized boolean isServiceRunning() {
		return serviceRunning;
	}

    synchronized void setServiceRunning(boolean serviceRunning) {
    	L.d(TAG, "Setting service run state to " + serviceRunning);
        this.serviceRunning = serviceRunning;
		persistServiceState(serviceRunning);
        notifyWidgetOfChange();
//		notifyWidgetOfServiceRunState(serviceRunning);
	}
	
	private void persistServiceState(boolean serviceRunning) {
		L.i(TAG, "persisting key = value of " + ShamuData.Preferences.SHAMU_SERVICE_STATE + "=" + serviceRunning);
		ShamuData.Preferences.insertOrUpdatePreference(this.getContentResolver(), ShamuData.Preferences.SHAMU_SERVICE_STATE, ""+serviceRunning);
	}
	
	//todo remove this
	public void notifyWidgetOfServiceRunState() {
	  notifyWidgetOfServiceRunState(isServiceRunning());
	}
	public void notifyWidgetOfChange() {
        Intent widgetIntent = new Intent(this, ShamuWidget.class);
        this.sendBroadcast(widgetIntent);
        L.i(TAG, "Notifying widget that the something has changed and to re-paint itself");
	}

	public void notifyWidgetOfServiceRunState(boolean running) {
		notifyWidgetOfServiceRunState(running, isNetworkAvailable());
	}
	
   public void notifyWidgetOfServiceRunState(boolean running, boolean networkState) {
	  // L.d (TAG,"widget notify running = " + running + " network state = " + networkState);
			Intent widgetIntent = new Intent(this, ShamuWidget.class);
			widgetIntent.putExtra(SERVICE_STATE, running);
			widgetIntent.putExtra(NetworkReceiver.NETWORK_STATE, networkState);
			this.sendBroadcast(widgetIntent);
			widgetNotifiedOfServicePreviousState = running;
			L.i(TAG, "Notifying widget that the run state is " + running + " and the network state is " + networkState);
	}

/*
	public PullToRefreshAlertsFragment.AsyncRefresh getMainActivityRefresher() {
		return mainActivityRefresher;
	}
*/

	public PullToRefreshAbstractBaseFragment.AsyncRefresh getMainActivityRefresher() {
        L.v(TAG, "**** getMainActivityRefresher called " + mainActivityRefresher);
		return mainActivityRefresher;
	}

/*
	public void setMainActivityRefresher(PullToRefreshAlertsFragment.AsyncRefresh mainActivityRefresher) {
		this.mainActivityRefresher = mainActivityRefresher;
	}
*/

	public void setMainActivityRefresher(PullToRefreshAbstractBaseFragment.AsyncRefresh mainActivityRefresher) {
        L.v(TAG, "**** setMainActivityRefresher called " + mainActivityRefresher);
		this.mainActivityRefresher = mainActivityRefresher;
	}

	public ShamuConfigurationFragment.AsyncTest getPreferenceActivityTester() {
		return preferenceActivityTester;
	}

	public void setPreferencesTester(ShamuConfigurationFragment.AsyncTest tester) {
		this.preferenceActivityTester = tester;
	}

	public boolean isNetworkAvailable() {
		boolean networkAvailable = false;
		ConnectivityManager connectivityManager =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager != null ){
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

			if(networkInfo != null){
				networkAvailable = networkInfo.isConnected();
				L.d(TAG, "The network state is set to " + networkAvailable);

			}else {
				L.d(TAG, "network info is not available");
			}
		}
		//  if(networkInfo.isAvailable()){  // Old one
		//if(networkInfo.isAvailable() && networkInfo.isConnected()){  // New change added here
		//                    if(netType == ConnectivityManager.TYPE_WIFI)
		//                        {}
		//                    else if(netType == ConnectivityManager.TYPE_MOBILE )
		//                        {}
		//                        }
		//                    }
		//                }catch(Exception e){
		//        Log.d("Log", "checkNetworkConnection" + e.toString());
		//        return networkStatus;
		//	}

		return networkAvailable;
	}

    @Override
    public void onInit(int status) {
        // {@link TextToSpeech#SUCCESS} or {@link TextToSpeech#ERROR}.
        if (TextToSpeech.SUCCESS == status) {
            L.i(TAG,"TextToSpeech initialized and is ready to go!");
            chatterBoxInitialized = true;
        } else {
            L.e(TAG,"TextToSpeech did NOT initialize!  I am going to  be shutty upy :-(");
            chatterBoxInitialized = false;
        }
    }


    public void speak(String chat) {
        L.i(TAG,"Speak called for \"" + chat +"\"" );
        //KEY_PARAM_UTTERANCE_ID
        currentMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentMusicVolume == 0) {
            ttsStream = AudioManager.STREAM_ALARM;
            L.v(TAG,"Using audio stream STREAM_ALARM");
        } else {
            ttsStream = AudioManager.STREAM_MUSIC;
            L.v(TAG,"Using audio stream STREAM_MUSIC");
        }
       // ttsStream = AudioManager.STREAM_SYSTEM;
        HashMap<String,String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,chat);
        params.put(TextToSpeech.Engine.KEY_PARAM_STREAM,ttsStream + "");
        params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME,"1");
        if (chatterBoxInitialized) {
            chatterBox.playSilence(500, TextToSpeech.QUEUE_ADD, params);
            chatterBox.setSpeechRate(0.9f);
            chatterBox.speak(chat, TextToSpeech.QUEUE_ADD, params);
           // chatterBox.playSilence(250, TextToSpeech.QUEUE_ADD, params);
        }
    }


    private class SpeakListener extends UtteranceProgressListener {
        @Override
        public void onStart(String utteranceId) {
            L.d(TAG,"About to start uttering \"" + utteranceId+"\"");
            audioManager.requestAudioFocus(null,ttsStream,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }

        @Override
        public void onDone(String utteranceId) {
            L.d(TAG,"Done uttering \"" + utteranceId+"\"");

            audioManager.abandonAudioFocus(null);
        }

        @Override
        public void onError(String utteranceId) {
            L.e(TAG,"The utterance ID \"" + utteranceId + "\" failed!");
            audioManager.abandonAudioFocus(null);
        }

    }

//	public void setNetworkAvailable(boolean networkAvailable) {
//		notifyWidgetOfServiceRunState(isServiceRunning(), networkAvailable);
//		this.networkAvailable = networkAvailable;
//	}
}
