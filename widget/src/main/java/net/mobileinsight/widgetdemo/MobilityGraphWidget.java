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

import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.sql.Timestamp;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 * Implementation of App Widget functionality.
 */
public class MobilityGraphWidget extends AppWidgetProvider {
    static Number[] series1Numbers = {5.5, 5.5, 5.5, 5.5, 5.5, 5.5};
    static int count3g = 4;
    static int count4g = 4;
    static int number = 0;
    static int value = 0;
    static int[] dd3g = {0, 0, 0, 0, 0};
    static int[] dd4g = {0, 0, 0, 0, 0};
    static String bs3g4g = "";
    static int new_row_to_place = 0;
    static String[] bs3g = {"", "", "", "", ""};
    static String[] bs4g = {"", "", "", "", ""};
    static String newbs = "";
    static String label_onUpdate = "";
    static String label_onClick = "";
    static int show_label = -1;
    static String[] label_save = {"", "", "" ,""};
    static String[] Mngt_Type = {"", "", "", ""};
    static long[] time_cur = {0, 0, 0, 0};

    static String timeinfo = "";
    static long time_start = 0;
    static long time_latest = 0;
    static String mtype = "";

    static public boolean running = false;

    static Queue<String[]> msg_lst = new LinkedList<String[]>();
    static Queue<String> time_lst = new LinkedList<String>();
    static Queue<String> MType_lst = new LinkedList<String>();

    private final static String LOG_TAG = "Caster-MG";
    public final static String BROADCAST_COUNTER_ACTION = "MobileInsight.MobilityGraph.COUNTER_ACTION";

    MyAsynctask task = null;


    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        if (show_label == -1) {
            System.arraycopy(series1Numbers, 1, series1Numbers, 0, series1Numbers.length - 1);
            value = 11;

            switch (bs3g4g) {
                case "WCDMA":
                    if (count3g >= 0) {
                        for (int i = 0; i < 5; i++) {
                            if (bs3g[i].equals(newbs)) {
                                value = i;
                            }
                        }
                        if (value == 11) {
                            bs3g[count3g] = newbs;
                            number = count3g + 1;
                            dd3g[count3g] = 5;
                        } else {
                            number = value + 1;
                            dd3g[value] = 5;
                        }
                        count3g -= 1;
                        for (int i = 0; i < 5; i++) {
                            dd3g[i] -= 1;
                        }
                    } else {
                        for (int i = 0; i < 5; i++) {
                            if (bs3g[i].equals(newbs)) {
                                value = i;
                            }
                        }
                        if (value == 11) {
                            for (int i = 0; i < 5; i++) {
                                if (dd3g[i] <= value) {
                                    value = dd3g[i];
                                    new_row_to_place = i;
                                }
                            }
                            dd3g[new_row_to_place] = 5;
                            number = new_row_to_place + 1;
                            bs3g[new_row_to_place] = newbs;
                        } else {
                            number = value + 1;
                            dd3g[value] = 5;
                        }
                        count3g -= 1;
                        for (int i = 0; i < 5; i++) {
                            dd3g[i] -= 1;
                        }
                    }
                    series1Numbers[series1Numbers.length - 1] = number;
                    series1Numbers[series1Numbers.length - 2] = number;
                    break;
                case "LTE":
                    if (count4g >= 0) {
                        for (int i = 0; i < 5; i++) {
                            if (bs4g[i].equals(newbs)) {
                                value = i;
                            }
                        }
                        if (value == 11) {
                            bs4g[count4g] = newbs;
                            number = count4g + 6;
                            dd4g[count4g] = 5;
                        } else {
                            number = value + 6;
                            dd4g[value] = 5;
                        }
                        count4g -= 1;
                        for (int i = 0; i < 5; i++) {
                            dd4g[i] -= 1;
                        }
                    } else {
                        for (int i = 0; i < 5; i++) {
                            if (bs4g[i].equals(newbs)) {
                                value = i;
                            }
                        }
                        if (value == 11) {
                            for (int i = 0; i < 5; i++) {
                                if (dd4g[i] <= value) {
                                    value = dd4g[i];
                                    new_row_to_place = i;
                                }
                            }
                            dd4g[new_row_to_place] = 5;
                            number = new_row_to_place + 6;
                            bs4g[new_row_to_place] = newbs;
                        } else {
                            number = value + 6;
                            dd4g[value] = 5;
                        }
                        count4g -= 1;
                        for (int i = 0; i < 5; i++) {
                            dd4g[i] -= 1;
                        }
                    }
                    series1Numbers[series1Numbers.length - 1] = number;
                    series1Numbers[series1Numbers.length - 2] = number;
                    break;
                default:
                    series1Numbers[series1Numbers.length - 1] = series1Numbers[series1Numbers.length - 2];
                    break;
            }
        }

        for (int widgetId : appWidgetIds) {

            final XYPlot plot = new XYPlot(context, "");
            plot.layout(0, 0, 400, 400);
            plot.setDrawingCacheEnabled(true);

            XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Mobility events");

            StepFormatter series1Format = new StepFormatter();
            series1Format.getLinePaint().setStrokeWidth(3);
            series1Format.getLinePaint().setColor(Color.rgb(100, 149, 237));
            series1Format.getVertexPaint().setStrokeWidth(5);
            series1Format.getVertexPaint().setColor(Color.TRANSPARENT);
            series1Format.getFillPaint().setColor(Color.TRANSPARENT);

            StepFormatter formatter_Point_HANDOFF = new StepFormatter();
            formatter_Point_HANDOFF.getLinePaint().setColor(Color.TRANSPARENT);
            formatter_Point_HANDOFF.getVertexPaint().setStrokeWidth(10);
            formatter_Point_HANDOFF.getVertexPaint().setColor(Color.RED);
            formatter_Point_HANDOFF.getFillPaint().setColor(Color.TRANSPARENT);

            StepFormatter formatter_Point_MEAS_CTRL = new StepFormatter();
            formatter_Point_MEAS_CTRL.getLinePaint().setColor(Color.TRANSPARENT);
            formatter_Point_MEAS_CTRL.getVertexPaint().setStrokeWidth(10);
            formatter_Point_MEAS_CTRL.getVertexPaint().setColor(Color.GREEN);
            formatter_Point_MEAS_CTRL.getFillPaint().setColor(Color.TRANSPARENT);

            StepFormatter formatter_Point_MEAS_REPORT = new StepFormatter();
            formatter_Point_MEAS_REPORT.getLinePaint().setColor(Color.TRANSPARENT);
            formatter_Point_MEAS_REPORT.getVertexPaint().setStrokeWidth(10);
            formatter_Point_MEAS_REPORT.getVertexPaint().setColor(Color.YELLOW);
            formatter_Point_MEAS_REPORT.getFillPaint().setColor(Color.TRANSPARENT);


            SimpleXYSeries Point_HANDOFF = new SimpleXYSeries("Handoff cmd");
            SimpleXYSeries Point_MEAS_CTRL = new SimpleXYSeries("Meas control");
            SimpleXYSeries Point_MEAS_REPORT = new SimpleXYSeries("Meas report");

            for (int i  = 0; i < Mngt_Type.length; i++) {
                if (Mngt_Type[i].equals("HANDOFF")) {
                    Point_HANDOFF.addFirst(i+1,series1Numbers[i]);
                }
                if (Mngt_Type[i].equals("MEAS_CTRL")) {
                    Point_MEAS_CTRL.addFirst(i+1,series1Numbers[i]);
                }
                if (Mngt_Type[i].equals("MEAS_REPORT")) {
                    Point_MEAS_REPORT.addFirst(i+1,series1Numbers[i]);
                }
            }

            plot.addSeries(Point_HANDOFF, formatter_Point_HANDOFF);
            plot.addSeries(Point_MEAS_CTRL, formatter_Point_MEAS_CTRL);
            plot.addSeries(Point_MEAS_REPORT, formatter_Point_MEAS_REPORT);
            plot.addSeries(series1, series1Format);

            plot.setDomainValueFormat(new Format() {
                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    Number num = (Number) obj;
                    if (num.intValue() > 4) {
                        toAppendTo.append("");
                    }
                    else if (num.intValue() == 4) {
                        toAppendTo.append("time/ms");
                    }

                    else if (num.intValue() < 3 && time_cur[num.intValue() + 1] == 0) {
                        toAppendTo.append("");
                    }

                    else {
                        toAppendTo.append((new Long(time_cur[num.intValue()])).toString());
                    }
                    return toAppendTo;
                }

                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;
                }
                                      }
            );

            plot.setRangeValueFormat(new Format() {
                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    Number num = (Number) obj;
                    switch (num.intValue()) {
                        case 1:
                            toAppendTo.append(bs3g[0]);
                            break;
                        case 2:
                            toAppendTo.append(bs3g[1]);
                            break;
                        case 3:
                            toAppendTo.append(bs3g[2]);
                            break;
                        case 4:
                            toAppendTo.append(bs3g[3]);
                            break;
                        case 5:
                            toAppendTo.append(bs3g[4]);
                            break;
                        case 6:
                            toAppendTo.append(bs4g[0]);
                            break;
                        case 7:
                            toAppendTo.append(bs4g[1]);
                            break;
                        case 8:
                            toAppendTo.append(bs4g[2]);
                            break;
                        case 9:
                            toAppendTo.append(bs4g[3]);
                            break;
                        case 10:
                            toAppendTo.append(bs4g[4]);
                            break;
                    }
                    return toAppendTo;
                }

                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;
                }
            });

            plot.setBackgroundColor(Color.BLACK);
            plot.getBackgroundPaint().setColor(Color.TRANSPARENT);

            plot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
            plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
            plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
            plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
            plot.getGraphWidget().getDomainSubGridLinePaint().setColor(Color.TRANSPARENT);
            plot.getGraphWidget().getRangeSubGridLinePaint().setColor(Color.TRANSPARENT);

            plot.setGridPadding(50, 10, 15, 10);
            plot.getGraphWidget().getDomainOriginLinePaint().setStrokeWidth(1);
            plot.getGraphWidget().getRangeOriginLinePaint().setStrokeWidth(1);

            plot.setRangeLabel("Cell Info (Radio-Type, Cell-ID)");

            plot.setTitle("Mobility Graph Widget");
            plot.getTitleWidget().getLabelPaint().setColor(Color.WHITE);

            plot.getGraphWidget().setRangeAxisLeft(true);
            plot.getGraphWidget().setShowRangeLabels(true);
            plot.getGraphWidget().setShowDomainLabels(true);
            plot.getTitleWidget().getLabelPaint().setTextSize(25);
            plot.getGraphWidget().getRangeTickLabelPaint().setTextSize(11);
            plot.getGraphWidget().getRangeTickLabelPaint().setColor(Color.WHITE);
            plot.getGraphWidget().getDomainTickLabelPaint().setTextSize(11);
            plot.getGraphWidget().getDomainTickLabelPaint().setColor(Color.WHITE);
            plot.getGraphWidget().getRangeOriginTickLabelPaint().setTextSize(11);
            plot.getGraphWidget().getRangeOriginTickLabelPaint().setColor(Color.WHITE);

            plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
            plot.setTicksPerRangeLabel(1);
            plot.setDomainStep(XYStepMode.SUBDIVIDE, 6);

            plot.setRangeBoundaries(1, 10, BoundaryMode.FIXED);

            plot.getLegendWidget().getTextPaint().setColor(Color.WHITE);
            plot.getLegendWidget().getTextPaint().setTextSize(12);
            plot.getLegendWidget().setSize(new Size(15, SizeLayoutType.ABSOLUTE, 200, SizeLayoutType.ABSOLUTE));
            plot.getLegendWidget().setTableModel(new DynamicTableModel(2, 2));

            plot.getLayoutManager().setMarkupEnabled(false);

            Bitmap bmp = plot.getDrawingCache();

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.mobility_graph_widget);

            if (show_label == -1) {
                rv.setTextViewText(R.id.label, label_onUpdate);
            }
            else {
                rv.setTextViewText(R.id.label, label_save[show_label]);
            }
            rv.setBitmap(R.id.imageView, "setImageBitmap", bmp);

            appWidgetManager.updateAppWidget(widgetId, rv);
        }
    }

    @Override
    public void onEnabled(final Context context) {
        Log.i(LOG_TAG, "onEnabled");
    }

    public class MyAsynctask extends AsyncTask<Integer, Integer, Integer> {

        private Context context;
        String time_before = "";
        String [] msg_before;
        String mtype_before = "";
        @Override
        protected Integer doInBackground(Integer... vals) {

            while (running) {
                time_before = time_lst.peek();
                msg_before = msg_lst.peek();
                mtype_before = MType_lst.peek();
                if (time_lst.size() > 0 )
                {
                    time_lst.remove();
                    msg_lst.remove();
                    MType_lst.remove();
                }
                Log.i(LOG_TAG, "Num of remianed elements in time_lst: "+String.valueOf(time_lst.size()));
                if (time_before != null & time_lst.peek()!= null) {
                    publishProgress(vals);

                    Long time_sleep = Timestamp.valueOf(time_lst.peek()).getTime() - Timestamp.valueOf(time_before).getTime();
                    Log.i(LOG_TAG,"Time inter in milisec: " + String.valueOf(time_sleep));
                    try {
                        if(time_sleep>60000){
                            Thread.sleep(500);
                        }else if( time_sleep < 0){
                            Thread.sleep(500);
                        }else{
                            Thread.sleep(time_sleep);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
            intent.putExtra("time_show", time_before);
            intent.putExtra("last_msg_0", msg_before[0]);
            intent.putExtra("last_msg_1", msg_before[1]);
            intent.putExtra("last_mtype", mtype_before);
            this.context.sendBroadcast(intent);
        }

        @Override
        protected void onPostExecute(Integer val) {
            Log.i(LOG_TAG, "progress ending");
        }
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        if(intent.getAction().equals("android.appwidget.action.APPWIDGET_ENABLED")){
            Log.i(LOG_TAG, "enabled fom receiver");
        }

        if(intent.getAction().equals("android.appwidget.action.APPWIDGET_DISABLED")){
            running = false;
            if(task != null)
            {
                task.cancel(true);
            }
            clearData();
            Log.i(LOG_TAG, "disabled fom receiver");
        }
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), MobilityGraphWidget.class);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals(BROADCAST_COUNTER_ACTION)){

            String msg0 = "";
            String msg1 = "";
            if (intent.getStringExtra("time_show")!=null && intent.getStringExtra("last_msg_0")!=null
                    && intent.getStringExtra("last_msg_1")!=null && intent.getStringExtra("last_mtype")!=null){
                timeinfo = intent.getStringExtra("time_show");
                msg0 = intent.getStringExtra("last_msg_0");
                msg1 = intent.getStringExtra("last_msg_1");
                mtype = intent.getStringExtra("last_mtype");
            }

            System.arraycopy(Mngt_Type, 1, Mngt_Type, 0, Mngt_Type.length - 1);
            Mngt_Type[Mngt_Type.length - 1] = mtype;
            Log.i("mtype", Mngt_Type[0]+","+Mngt_Type[1]+","+Mngt_Type[2]+","+Mngt_Type[3]);

            switch (mtype) {
                case "HANDOFF":
                    label_onClick = "Handoff to " + msg1;
                    label_onUpdate = label_onClick;
                    break;
                case "MEAS_CTRL":
                    label_onClick = msg0;
                    label_onUpdate = "Measurement Control";
                    break;
                case "MEAS_REPORT":
                    label_onClick = msg0 + " " + msg1;
                    label_onUpdate = label_onClick;
                    break;
            }

            System.arraycopy(label_save, 1, label_save,0, label_save.length - 1);
            label_save[3] = label_onClick;

            bs3g4g = msg0;
            newbs = msg1;

            if (!Objects.equals(timeinfo, "")) {
                time_latest = Timestamp.valueOf(timeinfo).getTime();
            }

            if (time_start == 0) {
                time_start = time_latest;
            }

            System.arraycopy(time_cur, 1, time_cur, 0, time_cur.length - 1);
            time_cur[time_cur.length - 1] = time_latest - time_start;


            for (int appWidgetId : appWidgetIds) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }

        else if (intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED") || intent.getAction().equals("MobileInsight.OnlineMonitor.STARTED")) {
            // If there are widgets on the home screen, cancel the running task and run a new one
            if (task != null) {
                task.cancel(true);
            }
            if (appWidgetIds != null && appWidgetIds.length > 0){

                task = new MyAsynctask(context);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                running = true;
            }

            msg_lst.clear();
            time_lst.clear();
            MType_lst.clear();

            for (int i=0; i < series1Numbers.length; i++) {
                series1Numbers[i] = 5.5;
            }

            clearData();
        }

        if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.MobilityMngt.HANDOFF")){
            // A Handoff command that specifies target 3G/4G and cell
            // 3g->WCDMA 4g->LTE
            String bs3g4g_tmp = intent.getStringExtra("Target Radio");

            String newbs_tmp = intent.getStringExtra("Target Freq");

            //Get timestamp of message
            String time_tmp = intent.getStringExtra("Timestamp");
            time_lst.offer(time_tmp);


            if (bs3g4g_tmp.equals("WCDMA")){
                newbs_tmp = "(3G, "+newbs_tmp+")";

            }
            else{
                newbs_tmp = "(4G, "+newbs_tmp+")";
            }

            String[] msg_tmp = {bs3g4g_tmp, newbs_tmp};
            msg_lst.offer(msg_tmp);
            MType_lst.offer("HANDOFF");
        }
        else if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.MobilityMngt.MEAS_CTRL")){
            // A measurement control command
            // TODO: please help show the message below (control_info) on the widget (either popup or a fixed textview)
            String control_info = intent.getStringExtra("Control info");
            MType_lst.offer("MEAS_CTRL");

            String time_tmp = intent.getStringExtra("Timestamp");

            String[] msg_tmp = {control_info, ""};
            time_lst.offer(time_tmp);
            msg_lst.offer(msg_tmp);
        }
        else if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.MobilityMngt.MEAS_REPORT")){
            // A measurement report
            // TODO: please help show the message below (report_event and rss) on the widget (either popup or a fixed textview)
            String report_event = intent.getStringExtra("event");
            String rss = intent.getStringExtra("rss");

            MType_lst.offer("MEAS_REPORT");

            String time_tmp = intent.getStringExtra("Timestamp");
            time_lst.offer(time_tmp);

            String[] msg_tmp = {report_event, rss};
            msg_lst.offer(msg_tmp);
        }

        show_label = -1;

        if (appWidgetIds != null && appWidgetIds.length > 0) {
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    public void clearData(){
        count3g = 4;
        count4g = 4;
        number = 0;
        value = 0;

        for (int i = 0; i < dd3g.length; i++) {
            dd3g[i] = 0;
            dd4g[i] = 0;
        }

        bs3g4g = "";
        new_row_to_place = 0;

        for (int i = 0; i < bs3g.length; i++) {
            bs3g[i] = "";
            bs4g[i] = "";
        }

        newbs = "";
        label_onUpdate = "";
        label_onClick = "";
        show_label = -1;

        for (int i = 0; i < label_save.length; i++) {
            label_save[i] = "";
            Mngt_Type[i] = "";
        }

        for (int i = 0; i < time_cur.length; i++) {
            time_cur[i] = 0;
        }

        timeinfo = "";
        time_start = 0;
        time_latest = 0;
        mtype = "";
    }
}