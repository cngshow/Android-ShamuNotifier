package gov.va.shamu.android;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.Plot;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import gov.va.shamu.android.utilities.L;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RealTimeChartFragment extends AbstractBaseFragment {

    private static final String RESTFUL_PATH_TO_REALTIME_CHART_DATA = "/real_time_charting/real_time_chart?format=json";
   // private static final long REFRESH_RATE = 10 * 1000;
    private static final short MAX_POINTS = 30;

    public static final String TAG = "RealTimeChartFragment";
    // TODO: Rename parameter arguments, choose names that match
    private XYPlot xyPlot;


    private OnFragmentInteractionListener mListener;


    public static RealTimeChartFragment newInstance(String param1, String param2) {
        RealTimeChartFragment fragment = new RealTimeChartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public RealTimeChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        L.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_real_time_chart, container, false);
        BaseActivity activity = (BaseActivity) getActivity();
        activity.setPageTitle(activity.getString(R.string.title_real_time));
        xyPlot = (XYPlot) v.findViewById(R.id.realTimePlot);
        xyPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
//        xyPlot.getGraphWidget().getGridLinePaint().setColor(Color.BLACK);
//        xyPlot.getGraphWidget().getGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1,1}, 1));
        xyPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        xyPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

        xyPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        xyPlot.getBorderPaint().setStrokeWidth(1);
        xyPlot.getBorderPaint().setAntiAlias(true);
        xyPlot.getBorderPaint().setColor(Color.WHITE);
        // get rid of decimal points in our range labels:
        xyPlot.setRangeValueFormat(new DecimalFormat("0"));

        xyPlot.setDomainValueFormat(new DomainFormatter());
        xyPlot.getGraphWidget().setPaddingRight(2);
        // xyPlot.setPlotMargins(100,100,100,100);
        xyPlot.getGraphWidget().getDomainLabelPaint().setTextSize(25);
        xyPlot.getGraphWidget().getRangeLabelPaint().setTextSize(25);
        xyPlot.setMarkupEnabled(false);
        xyPlot.setPlotMargins(0, 0, 0, 0);
        xyPlot.setPlotPadding(50, 50, 50, 75);
       // xyPlot.getLegendWidget().setSize(new SizeMetrics(15, SizeLayoutType.ABSOLUTE, 200, SizeLayoutType.ABSOLUTE));
        xyPlot.getLegendWidget().getTextPaint().setTextSize(25);
        xyPlot.getDomainLabelWidget().getLabelPaint().setTextSize(25);
        xyPlot.getRangeLabelWidget().getLabelPaint().setTextSize(25);
        xyPlot.getTitleWidget().getLabelPaint().setTextSize(25);
       // xyPlot.getDomainLabelWidget().setSize(new SizeMetrics(3000,SizeLayoutType.FILL,10,SizeLayoutType.FILL));
        xyPlot.getLegendWidget().position(0, XLayoutStyle.RELATIVE_TO_LEFT,0, YLayoutStyle.RELATIVE_TO_BOTTOM);
        xyPlot.getDomainLabelWidget().position(0,XLayoutStyle.RELATIVE_TO_CENTER,0,YLayoutStyle.RELATIVE_TO_BOTTOM);
        xyPlot.getGraphWidget().setMarginBottom(25);
        xyPlot.getGraphWidget().setMarginRight(45);
        // xyPlot.getGraphWidget().
//        TextLabelWidget tw = xyPlot.getTitleWidget();
//        SizeMetrics sm = new SizeMetrics(100, SizeLayoutType.FILL, 100,SizeLayoutType.FILL);
//        tw.setSize(sm);
        AsyncRealTimeDataFetcher.keepRefreshing = true;
        (new AsyncRealTimeDataFetcher(activity, 0, xyPlot)).execute();

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
    }

    @Override
    public void onPause() {
        super.onPause();
        AsyncRealTimeDataFetcher.keepRefreshing = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        AsyncRealTimeDataFetcher.keepRefreshing = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        AsyncRealTimeDataFetcher.keepRefreshing = false;
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

    private static class DomainFormatter extends Format {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        @Override
        public StringBuffer format(Object object, StringBuffer toAppendTo, FieldPosition pos) {
            // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
            // we multiply our timestamp by 1000:
            long timestamp = ((Number) object).longValue();
            Date date = new Date(timestamp);

            L.d(TAG, "formatting date=" + new SimpleDateFormat("HH:mm:ss").format(date));

            return dateFormat.format(date, toAppendTo, pos);
        }

        @Override
        public Object parseObject(String string, ParsePosition position) {
            return null;
        }
    }

    static class AsyncRealTimeDataFetcher extends AsyncTask<Void, Void, JSONObject> {

        static volatile boolean keepRefreshing = true;
        Activity activity;
        long sleepTime;
        XYPlot plot;
        Number[] rangeValues;
        Number[] domainValues;
        String chartTitle, xTitle, yTitle, elementDescription, timeSpan;
        int refreshInterval;

        static private LineAndPointFormatter formatter = new LineAndPointFormatter(Color.rgb(0, 0, 0), Color.BLUE, Color.TRANSPARENT, null);


        AsyncRealTimeDataFetcher(Activity a, long sleepTime, XYPlot plot) {
            activity = a;
            this.sleepTime = sleepTime;
            this.plot = plot;
        }

        private final String TAG = this.getClass().getSimpleName();

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                Thread.currentThread().sleep(sleepTime);
            } catch (InterruptedException e) {
                L.w(TAG, "Refresh wait period is failing!", e);
            }
            if (keepRefreshing) {
                L.d(TAG, "Getting all preferences");
                try {
                    //sample JSON document looks like:
                    //[{"_id":1435687343674.0,"Total_Count":16.0,"start_time_epoch":1435687333674.0,"end_time_epoch":1435687343674.0}
                    String jsonString = ShamuUpdaterService.fetchJSONData(activity, RESTFUL_PATH_TO_REALTIME_CHART_DATA);
                    //String jsonString =json1;
                    if(false)
                        throw new IOException();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        return jsonObject;
                    } catch (JSONException e) {
                        L.wtf(TAG, "The endpoint is giving us invalid JSON!!!!!!!", e);
                    }
                } catch (IOException e) {
                    L.w(TAG, "Could not reach endpoint to get real time data! ", e);
                }
            }
            return null;
        }

        @Override
        protected synchronized void onPostExecute(JSONObject json) {
            //sample JSON document looks like:
            //[{"_id":1435687343674.0,"Total_Count":16.0,"start_time_epoch":1435687333674.0,"end_time_epoch":1435687343674.0}
            //((JSONObject)(new JSONArray(jsonString)).get(0)).get("Total_Count")
            if (json != null) {
                try {
                    JSONArray realTimeData = json.getJSONArray("real_time_data");
                    chartTitle = json.getString("chart_title");
                    xTitle = json.getString("x_title");
                    yTitle = json.getString("y_title");
                    timeSpan = json.getString("time_span");//currently unused
                    refreshInterval = json.getInt("refresh_interval");
                    elementDescription = json.getJSONArray("element_description").getString(0);//currently only supporting one line being drawn
                    int length = realTimeData.length();
                    rangeValues = new Number[length];
                    domainValues = new Number[length];
                    for (int i = 0; i < length; i++) {

                        JSONObject obj = (JSONObject) realTimeData.get(i);
                        double tc = obj.getDouble("Total_Count");
                        double ep = obj.getDouble("end_time_epoch");
                        rangeValues[i] = tc;
                        domainValues[i] = ep;
                        L.d(TAG, "I got a total count of " + tc + " and a time of " + ep);
                        //check and ensure I am attached before interacting with the chart.
                    }
                    plotData();
                } catch (JSONException e) {
                    L.wtf(TAG, "The endpoint gave nested invalid JSON", e);
                }
            } else {
                //no plot data.  Likely could not reach the endpoint
                L.d(TAG, "No JSON data received from the endpoint!");
            }

            if (keepRefreshing) {
                (new AsyncRealTimeDataFetcher(activity, refreshInterval * 1000, plot)).execute();
            }
        }

        private void plotData() {
            try {
                plot.clear();
                List<Number> domainList = Arrays.asList(domainValues);
                List<Number> rangeList = Arrays.asList(rangeValues);
                if (domainList.size() > MAX_POINTS) {
                    domainList = domainList.subList(domainList.size() - MAX_POINTS, domainList.size());
                    rangeList = rangeList.subList(rangeList.size() - MAX_POINTS, rangeList.size());
                }
                // create our series from our array of nums:
                XYSeries series = new SimpleXYSeries(domainList, rangeList, elementDescription);


                plot.addSeries(series, formatter);

                // draw a domain tick for each year:
                plot.setDomainStep(XYStepMode.SUBDIVIDE, domainList.size() / 6);//TODO 5 minute intervals

                // customize our domain/range labels
                plot.setDomainLabel(xTitle);// TODO get the following from the JSON
                plot.setRangeLabel(yTitle);
                plot.setTitle(chartTitle);
//                TextLabelWidget tw = plot.getTitleWidget();
//                SizeMetrics sm = new SizeMetrics(20, SizeLayoutType.FILL, 20,SizeLayoutType.FILL);
//                tw.setSize(sm);
//                tw.setText("HELLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLO!!!!!!!!!!!!!!!!!!!!!");
                plot.redraw();

            } catch (Exception e) {
                //if for any reason updating the UI's plot fails do not let the application die.  Log the error only!
                L.e(TAG, "Update of the real time plot failed!", e);
            }
        }
    }
static String json1 = "{\"chart_title\":\"DAS Documents - Mocked Real Time Transactions\",\"x_title\":\"Time\",\"y_title\":\"Transaction Count\",\"time_span\":\"\",\"refresh_interval\":10,\"element_description\":[\"DAS Documents\"],\"real_time_data\":[{\"_id\":1435866072290.0,\"Total_Count\":3.0,\"start_time_epoch\":1435866062290.0,\"end_time_epoch\":1435866072290.0},{\"_id\":1435866082313.0,\"Total_Count\":22.0,\"start_time_epoch\":1435866072291.0,\"end_time_epoch\":1435866082313.0},{\"_id\":1435866092320.0,\"Total_Count\":4.0,\"start_time_epoch\":1435866082314.0,\"end_time_epoch\":1435866092320.0},{\"_id\":1435866102328.0,\"Total_Count\":21.0,\"start_time_epoch\":1435866092321.0,\"end_time_epoch\":1435866102328.0},{\"_id\":1435866112339.0,\"Total_Count\":4.0,\"start_time_epoch\":1435866102329.0,\"end_time_epoch\":1435866112339.0},{\"_id\":1435866122347.0,\"Total_Count\":12.0,\"start_time_epoch\":1435866112340.0,\"end_time_epoch\":1435866122347.0},{\"_id\":1435866132359.0,\"Total_Count\":6.0,\"start_time_epoch\":1435866122348.0,\"end_time_epoch\":1435866132359.0},{\"_id\":1435866142367.0,\"Total_Count\":14.0,\"start_time_epoch\":1435866132360.0,\"end_time_epoch\":1435866142367.0},{\"_id\":1435866152374.0,\"Total_Count\":11.0,\"start_time_epoch\":1435866142368.0,\"end_time_epoch\":1435866152374.0},{\"_id\":1435866162388.0,\"Total_Count\":7.0,\"start_time_epoch\":1435866152375.0,\"end_time_epoch\":1435866162388.0},{\"_id\":1435866172399.0,\"Total_Count\":14.0,\"start_time_epoch\":1435866162389.0,\"end_time_epoch\":1435866172399.0},{\"_id\":1435866182411.0,\"Total_Count\":9.0,\"start_time_epoch\":1435866172400.0,\"end_time_epoch\":1435866182411.0},{\"_id\":1435866192422.0,\"Total_Count\":7.0,\"start_time_epoch\":1435866182412.0,\"end_time_epoch\":1435866192422.0},{\"_id\":1435866202434.0,\"Total_Count\":15.0,\"start_time_epoch\":1435866192423.0,\"end_time_epoch\":1435866202434.0},{\"_id\":1435866212446.0,\"Total_Count\":9.0,\"start_time_epoch\":1435866202435.0,\"end_time_epoch\":1435866212446.0},{\"_id\":1435866222459.0,\"Total_Count\":13.0,\"start_time_epoch\":1435866212447.0,\"end_time_epoch\":1435866222459.0},{\"_id\":1435866232473.0,\"Total_Count\":19.0,\"start_time_epoch\":1435866222460.0,\"end_time_epoch\":1435866232473.0},{\"_id\":1435866242480.0,\"Total_Count\":9.0,\"start_time_epoch\":1435866232474.0,\"end_time_epoch\":1435866242480.0},{\"_id\":1435866252488.0,\"Total_Count\":10.0,\"start_time_epoch\":1435866242481.0,\"end_time_epoch\":1435866252488.0},{\"_id\":1435866262499.0,\"Total_Count\":12.0,\"start_time_epoch\":1435866252489.0,\"end_time_epoch\":1435866262499.0},{\"_id\":1435866272508.0,\"Total_Count\":10.0,\"start_time_epoch\":1435866262500.0,\"end_time_epoch\":1435866272508.0},{\"_id\":1435866282519.0,\"Total_Count\":6.0,\"start_time_epoch\":1435866272509.0,\"end_time_epoch\":1435866282519.0},{\"_id\":1435866292534.0,\"Total_Count\":3.0,\"start_time_epoch\":1435866282520.0,\"end_time_epoch\":1435866292534.0},{\"_id\":1435866302542.0,\"Total_Count\":14.0,\"start_time_epoch\":1435866292535.0,\"end_time_epoch\":1435866302542.0},{\"_id\":1435866312550.0,\"Total_Count\":10.0,\"start_time_epoch\":1435866302543.0,\"end_time_epoch\":1435866312550.0},{\"_id\":1435866322557.0,\"Total_Count\":8.0,\"start_time_epoch\":1435866312551.0,\"end_time_epoch\":1435866322557.0},{\"_id\":1435866332563.0,\"Total_Count\":15.0,\"start_time_epoch\":1435866322558.0,\"end_time_epoch\":1435866332563.0},{\"_id\":1435866342575.0,\"Total_Count\":6.0,\"start_time_epoch\":1435866332564.0,\"end_time_epoch\":1435866342575.0},{\"_id\":1435866352583.0,\"Total_Count\":6.0,\"start_time_epoch\":1435866342576.0,\"end_time_epoch\":1435866352583.0},{\"_id\":1435866362589.0,\"Total_Count\":17.0,\"start_time_epoch\":1435866352584.0,\"end_time_epoch\":1435866362589.0},{\"_id\":1435866372600.0,\"Total_Count\":11.0,\"start_time_epoch\":1435866362590.0,\"end_time_epoch\":1435866372600.0}]}";
}
