package net.mobileinsight.widgetdemo;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Implementation of App Widget functionality.
 */
public class LtePhyWidget extends AppWidgetProvider {

    static public String test="0";
    static public String modulation0="";
//    static public String modulation1=""; // Currently unused
    static public String statistics_qpsk="0";
    static public String statistics_16qam="0";
    static public String statistics_64qam="0";
    static Number[] series1Numbers = new Number[50];

    private final static String LOG_TAG = "Caster-Phy";
    public final static String BROADCAST_COUNTER_ACTION = "MobileInsight.LtePhy.COUNTER_ACTION";

    static Queue<String> test_lst = new LinkedList<String>();
    static Queue<String[]> modulation_lst = new LinkedList<String[]>();
    static Queue<String[]> statistics_lst = new LinkedList<String[]>();

    MyAsynctask task = null;

    static public boolean running = false;
    static float number = 0;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        if (series1Numbers[0] == null)
            for (int i = 1; i < series1Numbers.length; i++)
                series1Numbers[i]=0;

        if (test_lst.peek() != null) {
            System.arraycopy(series1Numbers, 1, series1Numbers, 0, series1Numbers.length - 1);

            test = test_lst.poll();

            String [] md_tmp = modulation_lst.poll();
            modulation0 = md_tmp[0];
//            modulation1 = md_tmp[1];

            String [] st_tmp = statistics_lst.poll();
            statistics_qpsk = st_tmp[0];
            statistics_16qam = st_tmp[1];
            statistics_64qam = st_tmp[2];

            number = Float.valueOf(test);
            series1Numbers[series1Numbers.length-1] = number;
        }

            for (int widgetId : appWidgetIds) {
                XYPlot plot = new XYPlot(context, "");

                plot.layout(0,0, 300,300);
                plot.setDrawingCacheEnabled(true);
                // Turn the above arrays into XYSeries':
                XYSeries series1 = new SimpleXYSeries(
                        Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                        "Series1");                             // Set the display title of the series
                // Create a formatter to use for drawing a series using LineAndPointRenderer:
                LineAndPointFormatter series1Format = new LineAndPointFormatter();
                series1Format.getLinePaint().setColor(Color.rgb(100,149,237));
                series1Format.getLinePaint().setStrokeWidth(3);
                series1Format.getVertexPaint().setColor(Color.rgb(100,149,237));
                series1Format.getVertexPaint().setStrokeWidth(0);
                series1Format.getFillPaint().setColor(Color.TRANSPARENT);
                plot.setBackgroundColor(Color.BLACK);


                plot.getBackgroundPaint().setColor(Color.TRANSPARENT);

                plot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
                plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
                plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
                plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
                plot.getGraphWidget().getDomainSubGridLinePaint().setColor(Color.TRANSPARENT);
                plot.getGraphWidget().getRangeSubGridLinePaint().setColor(Color.TRANSPARENT);

                plot.setGridPadding(0, 0, 5, 0);
                plot.setPlotMargins(0, 0, 0, 0);
                plot.getGraphWidget().getDomainOriginLinePaint().setStrokeWidth(4);
                plot.getGraphWidget().getRangeOriginLinePaint().setStrokeWidth(2);
                plot.getLayoutManager().getPaddingPaint().setTextSize(5);
                plot.setTitle("LTE DL Bandwidth");
                plot.getTitleWidget().getLabelPaint().setTextSize(25);
                plot.setRangeBoundaries(0, 50, BoundaryMode.FIXED);
                plot.setRangeStepValue(10);
                plot.getLayoutManager().remove(plot.getLegendWidget());
                plot.getLayoutManager().remove(plot.getDomainLabelWidget());
                plot.getLayoutManager().remove(plot.getRangeLabelWidget());
                plot.getLayoutManager().remove(plot.getBorderPaint());

                plot.addSeries(series1, series1Format);

                // reduce the number of range labels
                plot.setTicksPerRangeLabel(2);
                plot.getGraphWidget().setMarginLeft(0);
                plot.getGraphWidget().setPaddingLeft(0);
                // by default, AndroidPlot displays developer guides to aid in laying out your plot.
                // To get rid of them call disableAllMarkup():

                Bitmap bmp = plot.getDrawingCache();

                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.lte_phy_widget);
                rv.setTextViewText(R.id.textView1, String.valueOf(number).concat(" Mbps"));
                rv.setTextColor(R.id.textView1, Color.WHITE);

                rv.setTextViewText(R.id.textView2, "Current modulation:".concat(modulation0));
                float qpsk = Float.parseFloat(statistics_qpsk);
                float qam16 = Float.parseFloat(statistics_16qam);
                float qam64 = Float.parseFloat(statistics_64qam);
                float total = qpsk + qam16 + qam64;
                if(total!=0 && number != 0.0){
                    qpsk = 100*qpsk/total;
                    qam16 = 100*qam16/total;
                    qam64 = 100*qam64/total;
                }
                else{
                    qpsk = 0;
                    qam16 = 0;
                    qam64 = 0;
                }
                rv.setTextViewText(R.id.textView3,"QPSK (% in last 1s): "+String.format("%.2f",qpsk)+"%");
                rv.setTextViewText(R.id.textView4,"16QAM (% in last 1s): "+String.format("%.2f",qam16)+"%");
                rv.setTextViewText(R.id.textView5,"64QAM (% in last 1s): "+String.format("%.2f",qam64)+"%");

                rv.setBitmap(R.id.imageView, "setImageBitmap", bmp);

                appWidgetManager.updateAppWidget(widgetId, rv);
            }


    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        /*
         Get PHY bandwidth
         */
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), LtePhyWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);


        if(intent.getAction().equals("android.appwidget.action.APPWIDGET_ENABLED")){
            test="0";
            modulation0="";
//            modulation1="";
            statistics_qpsk="0";
            statistics_16qam="0";
            statistics_64qam="0";
            test_lst.clear();
            modulation_lst.clear();
            statistics_lst.clear();
            Log.i(LOG_TAG, "enabled fom receiver");
        }
        if(intent.getAction().equals("android.appwidget.action.APPWIDGET_DISABLED")){
            running = false;
            if(task != null)
            {
                task.cancel(true);
            }
            Log.i(LOG_TAG, "disabled fom receiver");
        }

        if(intent.getAction().equals(BROADCAST_COUNTER_ACTION)){

            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }

        if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.LtePhyAnalyzer.LTE_DL_BW")){

            String test_tmp = intent.getStringExtra("Bandwidth (Mbps)");
            test_lst.offer(test_tmp);

            String mod0 = intent.getStringExtra("Modulation 0");
            String mod1 = intent.getStringExtra("Modulation 1");
            String [] mod_tmp = {mod0, mod1};
            modulation_lst.offer(mod_tmp);

            String statistics_qpsk_tmp = intent.getStringExtra("Modulation-QPSK");
            String statistics_16qam_tmp = intent.getStringExtra("Modulation-16QAM");
            String statistics_64qam_tmp = intent.getStringExtra("Modulation-64QAM");
            String [] stat_tmp = {statistics_qpsk_tmp, statistics_16qam_tmp, statistics_64qam_tmp};
            statistics_lst.offer(stat_tmp);
//            Log.i(LOG_TAG,statistics_qpsk_tmp+" "+statistics_16qam_tmp+" "+statistics_64qam_tmp);
        }

        else if (intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED") || intent.getAction().equals("MobileInsight.OnlineMonitor.STARTED")) {
            Log.i(LOG_TAG, "started");
            if (appWidgetIds != null && appWidgetIds.length > 0 && task == null) {
                task = new MyAsynctask(context);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                running = true;
            }

            test="0";
            modulation0="";
//            modulation1="";
            statistics_qpsk="0";
            statistics_16qam="0";
            statistics_64qam="0";
            test_lst.clear();
            modulation_lst.clear();
            statistics_lst.clear();

            for (int i = 0; i < series1Numbers.length; i++) {
                series1Numbers[i] = 0;
            }
            number = 0;
        }
        super.onReceive(context, intent);
    }


    public class MyAsynctask extends AsyncTask<Integer, Integer, Integer> {

        private Context context;
        @Override
        protected Integer doInBackground(Integer... vals) {

            while (running) {
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return 1;
        }

        public MyAsynctask(Context context){
            this.context = context;
            Log.i(LOG_TAG, "myasynctask inited");
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Intent intent = new Intent();
            intent.setAction(BROADCAST_COUNTER_ACTION);
            this.context.sendBroadcast(intent);
        }

        @Override
        protected void onPreExecute() {
            Log.i(LOG_TAG, "progress starting");
        }

        @Override
        protected void onPostExecute(Integer val) {
            Log.i(LOG_TAG, "progress ending");
        }

        @Override
        protected void onCancelled(){
            running = false;
            Log.i(LOG_TAG, "task is cancelled");
        }
    }
}
