package gov.va.shamu.android.speechtotext;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import gov.va.shamu.android.AbstractBaseFragment;
import gov.va.shamu.android.R;
import gov.va.shamu.android.ShamuJobDisplayActivity;
import gov.va.shamu.android.ShamuMainActivity;
import gov.va.shamu.android.provider.CommonDataColumns;
import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.provider.ShamuNotifierContentProvider;
import gov.va.shamu.android.utilities.InterestingAlert;
import gov.va.shamu.android.utilities.L;
import gov.va.shamu.android.utilities.Report;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SpeechCommandFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SpeechCommandFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpeechCommandFragment extends AbstractBaseFragment implements RecognitionListener {

    private final String TAG = getClass().getSimpleName();
    private SpeechRecognizer speech;
    private TextView tv;
    private FrameLayout layout;
    private static volatile boolean activelyListening = true;
    public static boolean resumed = false;
    private static String[] listenStrings = new String[]{"Listening   ", "Listening.  ", "Listening.. ", "Listening..."};


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SpeechCommandFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpeechCommandFragment newInstance(String param1, String param2) {
        SpeechCommandFragment fragment = new SpeechCommandFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SpeechCommandFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.d(TAG,"onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        resumed = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setPageTitle(R.string.listening_title);
        View v = inflater.inflate(R.layout.fragment_speech_command, container, false);
        tv = (TextView) v.findViewById(R.id.speech_fragment_textview);
        layout = (FrameLayout) v.findViewById(R.id.speech_fragment);
        speech = SpeechRecognizer.createSpeechRecognizer(getActivity());
        speech.setRecognitionListener(this);
        //  Intent speechIntent = new Intent();
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        //   speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        activelyListening = true;
        speech.startListening(speechIntent);
        L.d(TAG, "is the text view isAttachedToWindow ? " + tv.isAttachedToWindow());
        return v;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        L.d(TAG, "onDestroy");
        stopListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        L.d(TAG, "onPause");
        stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        L.d(TAG, "onResume");
        if (resumed) {
            L.d(TAG,"I prevented this naughty fragment from showing up.");
            Intent intent = new Intent(getActivity(), ShamuMainActivity.class);
            startActivity(intent);
        }
        resumed = true;
    }

        // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //  getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        ListeningTextChanger tc = (new ListeningTextChanger(0));
        tc.execute();
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //L.d(TAG, "onRmsChanged " + rmsdB);
        //(new AudioColorChanger(rmsdB)).execute();
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {
        L.w(TAG,"onError " + error);
        stopListening();
        String text = getString(R.string.speech_to_text_error);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(getActivity(), text, duration);
        toast.show();
        Intent intent = new Intent(getActivity(), ShamuMainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResults(Bundle results) {
        L.d(TAG, "onResults" + results);
        List<String> spokenWords = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        L.d(TAG, spokenWords.toString());
        String[] reportPreambles = getResources().getStringArray(R.array.speech_to_text_report_preamble_array);
        String[] alertPreambles = getResources().getStringArray(R.array.speech_to_text_alert_preamble_array);
        List<String> findMe = lookupHunter(spokenWords, reportPreambles);
        boolean reportToLookup = !findMe.isEmpty();
        if (!reportToLookup) {
            findMe = lookupHunter(spokenWords, alertPreambles);
        }
        if (findMe.isEmpty()) {
            onError(0);
            return;
        }

        ContentResolver cr = getActivity().getContentResolver();
        String jobCode = null;
        for (String s : findMe) {

            if (reportToLookup) {
                L.d(TAG, "I will be looking up report " + s);
                List<Report> reports = ShamuData.Reports.getAllReports(cr);
                for(Report r : reports) {
                    if(s.trim().equalsIgnoreCase(r.getShortDescription())) {
                        jobCode = r.getJobCode();
                        break;
                    }
                }
            }
            if (!reportToLookup) {
                L.d(TAG, "I will be looking up alert " + s);
                List<InterestingAlert> alerts = ShamuData.InterestingAlertsData.getAllInterestingAlerts(cr);
                for(InterestingAlert a : alerts) {
                    if(s.trim().equalsIgnoreCase(a.getShortDescription())) {
                        jobCode = a.getJobCode();
                        break;
                    }
                }
            }
        }

        stopListening();

        //Do lookup for findMe
        if (jobCode != null) {
            ShamuNotifierContentProvider.UriType type = reportToLookup ?  ShamuNotifierContentProvider.UriType.REPORT : ShamuNotifierContentProvider.UriType.ALERT;
            L.d(TAG, "selected job code value = " + jobCode);
            Activity currentActivity = getActivity();
            Intent intent = new Intent(currentActivity, ShamuJobDisplayActivity.class);
            intent.putExtra(CommonDataColumns.JOB_CODE_COLUMN_NAME, jobCode);
            intent.putExtra(ShamuJobDisplayActivity.DISPLAY_TYPE,type);
            startActivity(intent);
        } else {
            String text = getString(R.string.speech_to_text_can_not_find);
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getActivity(), text, duration);
            toast.show();
            Intent intent = new Intent(getActivity(), ShamuMainActivity.class);
            startActivity(intent);
        }

    }

    private List<String> lookupHunter(List<String> spokenWords, String[] preamble) {
        List<String> findMe = new ArrayList<String>();
        for (String spoken : spokenWords) {
            for (String beginning : preamble) {
                spoken = spoken.toLowerCase().trim();
                beginning = beginning.toLowerCase().trim();
                if (spoken.startsWith(beginning)) {
                    L.d(TAG, "Speech match found!");
                    String found = spoken.replace(beginning, "").trim();
                    L.d(TAG, "I will find " + found);
                    findMe.add(found);
                }
            }
        }
        return findMe;
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void stopListening() {
        L.d(TAG, "stopListening");
        activelyListening = false;
        if (speech != null) {
            //speech.stopListening();
            speech.destroy();
            speech = null;
        }
    }

    public class AudioColorChanger extends AsyncTask<Void, Void, Void> {

        private final float audioNumber;

        AudioColorChanger(float audioNumber) {
            this.audioNumber = audioNumber;
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }


        @Override
        protected synchronized void onPostExecute(Void result) {

            layout.setBackgroundColor(Color.WHITE + Math.round(1000 * audioNumber));
        }
    }

    public class ListeningTextChanger extends AsyncTask<Void, Void, Void> {

        private int index;

        ListeningTextChanger() {
            this(0);
        }

        private ListeningTextChanger(int start) {
            this.index = start;
            L.d(TAG,"Listening text changer started!");
        }

        @Override
        protected Void doInBackground(Void... params) {
            L.d(TAG,"Listening text changer do in background complete.");
            return null;
        }

        @Override
        protected synchronized void onPostExecute(Void result) {
            if (activelyListening) {
                L.d(TAG,"Actively listening and changing the text");
                tv.setText(listenStrings[index % 4]);
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    L.w(TAG, "Sleep failure!", e);
                }
                (new ListeningTextChanger(++index)).execute();
            }
        }
    }
}
