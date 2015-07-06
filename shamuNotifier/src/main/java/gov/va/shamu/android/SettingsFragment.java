package gov.va.shamu.android;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.Map;

import gov.va.shamu.android.provider.ShamuData;
import gov.va.shamu.android.utilities.L;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SettingsFragment extends AbstractBaseFragment implements CompoundButton.OnCheckedChangeListener{
    public static final String TAG = "SettingsFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private CheckBox cbVibrate;
    private CheckBox cbAudible;
    private CheckBox cbAnnounce;

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        L.d(TAG,"onCreateView");
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        cbVibrate = (CheckBox) v.findViewById(R.id.settings_vibrate_checkbox);
        cbAudible = (CheckBox) v.findViewById(R.id.settings_audible_checkbox);
        cbAnnounce = (CheckBox) v.findViewById(R.id.settings_announce_checkbox);
        (new AsyncSettings(getActivity())).execute();
        cbVibrate.setOnCheckedChangeListener(this);
        cbAudible.setOnCheckedChangeListener(this);
        cbAnnounce.setOnCheckedChangeListener(this);
        BaseActivity activity = (BaseActivity)getActivity();
        activity.setPageTitle(activity.getString(R.string.title_settings));
        return v;
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
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String key = null;

        if (buttonView.equals(cbVibrate)) {
            key = ShamuData.Preferences.SETTINGS_VIBRATE;
        }
        else if (buttonView.equals(cbAudible)) {
            key = ShamuData.Preferences.SETTINGS_AUDIBLE;
        }
        else if (buttonView.equals(cbAnnounce)) {
            key = ShamuData.Preferences.SETTINGS_ANNOUNCE;
        }

        //final ContentResolver cr = getActivity().getContentResolver();
        new Thread(new SettingsChanger( (BaseActivity)getActivity(), cr, key, isChecked)).start();
    }

    class SettingsChanger implements Runnable {
        private final BaseActivity activity;
        String key;
        Boolean isChecked;
        ContentResolver cr;

        SettingsChanger(BaseActivity a, ContentResolver cr, String key, Boolean isChecked) {
            this.cr = cr;
            this.key = key;
            this.isChecked = isChecked;
            activity = a;
            if(activity == null) {
                L.wtf(TAG, "The SettingsChanger got a NULL activity!");
            } else {
                L.d(TAG, "The SettingsChanger got a non null activity!");
            }
        }
        @Override
        public void run() {
            ShamuData.Preferences.insertOrUpdatePreference(cr, key, isChecked.toString());
            activity.getShamu().notifyWidgetOfChange();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public class AsyncSettings extends AsyncTask<Void, Void, Map<String, String>> {

        Activity activity;

        AsyncSettings(Activity a) {
            activity = a;
        }

        private final String TAG = this.getClass().getSimpleName();

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            L.d(TAG, "Getting all preferences");
            return ShamuData.Preferences.getAllPreferences(activity.getContentResolver());
        }

        @Override
        protected synchronized void onPostExecute(Map<String, String> settings) {
            String vibrate = settings.get(ShamuData.Preferences.SETTINGS_VIBRATE);
            String audible = settings.get(ShamuData.Preferences.SETTINGS_AUDIBLE);
            String announce = settings.get(ShamuData.Preferences.SETTINGS_ANNOUNCE);
            cbVibrate.setChecked(vibrate.equals(Boolean.TRUE.toString()));
            cbAudible.setChecked(audible.equals(Boolean.TRUE.toString()));
            cbAnnounce.setChecked(announce.equals(Boolean.TRUE.toString()));

            L.d(TAG, "vibrate is = " + vibrate + " :: audible is " + audible + " :: announce is " + announce);
        }
    }
}
