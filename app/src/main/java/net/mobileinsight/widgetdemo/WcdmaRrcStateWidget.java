package net.mobileinsight.widgetdemo;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Implementation of App Widget functionality.
 */
public class WcdmaRrcStateWidget extends AppWidgetProvider {

    static public String wcdma_rrc_state = ""; //to receive state
    static public String next_state = ""; // next state to show in the figure
    static public String timeinfo = ""; // current time infomation
    static public String time_init = ""; // init time
    static public String state_before = "";
    static public long[] time_for_state = {0, 0, 0, 0, 0}; //0-DISCONNECTED; 1-CELL_DCH; 2-URA_PCH;
                                                            //3-CELL_FACH; 4-CELL_PCH
    static public long time_all = 0;
    static public long time_last = 0;
    static public long time_cur = 0;

    private final static String LOG_TAG = "Caster-WCDMARRC";
    public final static String BROADCAST_COUNTER_ACTION = "MobileInsight.WcdmaRrc.COUNTER_ACTION";

    static public Queue<String> state_lst = new LinkedList<String>();
    static public Queue<String> time_lst = new LinkedBlockingDeque<>();

    MyAsynctask task = null;

    static public boolean play = true;
    static public boolean running = false;
    static public boolean isonline = true;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(final Context context) {
        Log.i(LOG_TAG, "onEnabled");

    }

    public class MyAsynctask extends AsyncTask<Integer, Integer, Integer>  {

        private Context context;
        String time_before = "";
        String state_before = "";
        @Override
        protected Integer doInBackground(Integer... vals) {

            while (!isCancelled() && running) {
                if (play) {
                    time_before = time_lst.peek();
                    state_before = state_lst.peek();
                    if (time_lst.size() > 0 )
                    {
                        time_lst.remove();
                        state_lst.remove();
                    }
                    Log.i(LOG_TAG, "Num of remianed elements in time_lst: "+String.valueOf(time_lst.size()));
                    while (state_lst.peek() != null && "CONNECTING".equals(state_lst.peek())){
                        time_lst.remove();
                        state_lst.remove();
                    }
                    if (time_before != null & time_lst.peek()!= null) {
                        publishProgress(vals);

                        Long time_sleep = Timestamp.valueOf(time_lst.peek()).getTime() - Timestamp.valueOf(time_before).getTime();
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
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return 1;
        }

        public MyAsynctask(Context context){
            this.context = context;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.i(LOG_TAG, "sent intent");
            super.onProgressUpdate(values);
            Intent intent = new Intent();
            intent.setAction(BROADCAST_COUNTER_ACTION);
            intent.putExtra("time_show", time_before);
            intent.putExtra("last_state", state_before);
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

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("android.appwidget.action.APPWIDGET_ENABLED")){
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

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), WcdmaRrcStateWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals(BROADCAST_COUNTER_ACTION)){

            String tmp = state_lst.peek();
            String time_show = intent.getStringExtra("time_show");
            state_before = intent.getStringExtra("last_state");
            if (tmp != null && time_show != null) {
                timeinfo = time_show;
                next_state = tmp;
                if (timeinfo.equals("")) {
                    time_cur = 0;
                }
                time_cur = Timestamp.valueOf(timeinfo).getTime();
            }

            final int N = appWidgetIds.length;
            for (int i = 0; i < N; i++) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
        if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.WcdmaRrcAnalyzer.RRC_STATE")){

            String state_tmp = intent.getStringExtra("RRC State");
            String time_tmp = intent.getStringExtra("Timestamp");
            Log.i("WcdmaRrcStateWidget", state_tmp);

            if (!isonline) {

                if (state_tmp != null) {
                    state_lst.offer(state_tmp);
                }
                if (time_tmp != null) {
                    time_lst.offer(time_tmp);
                }
            }
            else{
                state_before = next_state;
                time_cur = Timestamp.valueOf(time_tmp).getTime();
                next_state = state_tmp;
            }
        }

        else if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED") || intent.getAction().equals("MobileInsight.OnlineMonitor.STARTED")) {

            Log.i(LOG_TAG, "started");

            if (intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED")) {
                isonline = false;
                Log.i(LOG_TAG, String.valueOf(task));
                if (task != null) {

                    task.cancel(true);
                }
                task = new MyAsynctask(context);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Log.i(LOG_TAG, String.valueOf(task));
                running = true;
                state_lst.clear();
                time_lst.clear();
            }
            else{
                if (task != null) {
                    task.cancel(true);
                }
            }
            wcdma_rrc_state = ""; //to receive state
            next_state = ""; // next state to show in the figure
            timeinfo = ""; // current time infomation
            time_init = ""; // init time
            state_before = "";

            //0-DISCONNECTED; 1-CELL_DCH; 2-URA_PCH; 3-CELL_FACH; 4-CELL_PCH
            for (int i = 0; i < time_for_state.length; i++) {
                time_for_state[i] = 0;
            }
            time_all = 0;
            time_last = 0;
            time_cur = 0;

            play = true;

            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
        super.onReceive(context, intent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wcdma_rrc_state_widget);

        if (time_init.equals("")) {
            time_init = timeinfo;
            time_last = time_cur;
        }
        if(wcdma_rrc_state.equals("")){
            wcdma_rrc_state = next_state;
            views.setImageViewResource(R.id.imageView_WCDMA, R.drawable.basis);

            views.setTextViewText(R.id.textView1, "CELL_DCH: 0.00(0%)");
            views.setTextViewText(R.id.textView2, "URA_PCH: 0.00(0%)");
            views.setTextViewText(R.id.textView3, "CELL_FACH: 0.00(0%)");
            views.setTextViewText(R.id.textView4, "CELL_PCH: 0.00(0%)");
            views.setTextViewText(R.id.textView0, "DISCONN: 0.00(0%)");
        }

        else{

            if(next_state.equals("CONNECTING")){
                return;
            }
            int tx_id = -1;
            long time_inter = time_cur - time_last;

            switch (state_before){
                case "DISCONNECTED":{ //STATE: 0
                    switch (next_state){
                        case "CELL_DCH": //STATE: 1
                            tx_id = R.drawable.disconn_dch;
                            break;
                        case "CELL_FACH":
                            tx_id = R.drawable.disconn_fach; //STATE: 3
                            break;
                        default:
                            Log.e("WcdmaRrcStateWidget","Invalid transition: "+state_before+"->"+next_state);
                            break;
                    }
                    if (play) {
                        time_for_state[0] += time_inter;
                        time_all += time_inter;
                    }
                    break;
                }
                case "CELL_DCH":{ //state: 1
                    switch (next_state){
                        case "DISCONNECTED":
                            tx_id = R.drawable.dch_disconn;
                            break;
                        case "CELL_FACH":
                            tx_id = R.drawable.dch_fach;
                            break;
                        case "CELL_PCH":
                            tx_id = R.drawable.dch_pch;
                            break;
                        case "URA_PCH":
                            tx_id = R.drawable.dch_ura;
                            break;
                        default:
                            Log.e("WcdmaRrcStateWidget","Invalid transition: "+state_before+"->"+next_state);
                            break;
                    }
                    if (play) {
                        time_for_state[1] += time_inter;
                        time_all += time_inter;
                    }
                    break;
                }
                case "CELL_FACH":{
                    switch (next_state){
                        case "DISCONNECTED":
                            tx_id = R.drawable.fach_disconn;
                            break;
                        case "CELL_DCH":
                            tx_id = R.drawable.fach_dch;
                            break;
                        case "CELL_PCH":
                            tx_id = R.drawable.fach_pch;
                            break;
                        case "URA_PCH":
                            tx_id = R.drawable.fach_ura;
                            break;
                        default:
                            Log.e("WcdmaRrcStateWidget","Invalid transition: "+state_before+"->"+next_state);
                            break;
                    }
                    if (play) {
                        time_for_state[3] += time_inter;
                        time_all += time_inter;
                    }
                    break;
                }
                case "CELL_PCH":{
                    switch (next_state){

                        case "CELL_FACH":
                            tx_id = R.drawable.pch_fach;
                            break;
                        case "DISCONNECTED":
                            tx_id = R.drawable.pch_disconn;
                            break;
                        default:
                            Log.e("WcdmaRrcStateWidget","Invalid transition: "+state_before+"->"+next_state);
                            break;
                    }
                    if (play) {
                        time_for_state[4] += time_inter;
                        time_all += time_inter;
                    }
                    break;
                }
                case "URA_PCH":{
                    switch (next_state){
                        case "CELL_FACH":
                            tx_id = R.drawable.ura_fach;
                            break;
                        case "DISCONNECTED":
                            tx_id = R.drawable.ura_disconn;
                            break;
                        default:
                            Log.e("WcdmaRrcStateWidget","Invalid transition: "+state_before+"->"+next_state);
                            break;
                    }
                    if (play) {
                        time_for_state[2] += time_inter;
                        time_all += time_inter;
                    }
                    break;
                }
                default:{
                    Log.e("WcdmaRrcStateWidget","Invalid transition: "+state_before+"->"+next_state);
                    break;
                }
            }

            if (time_all == 0) {
                views.setTextViewText(R.id.textView1, "CELL_DCH: 0.00(0%)");
                views.setTextViewText(R.id.textView2, "URA_PCH: 0.00(0%)");
                views.setTextViewText(R.id.textView3, "CELL_FACH: 0.00(0%)");
                views.setTextViewText(R.id.textView4, "CELL_PCH: 0.00(0%)");
                views.setTextViewText(R.id.textView0, "DISCONN: 0.00(0%)");
            }

            if (tx_id != -1) {
                if (play) {
                    views.setImageViewResource(R.id.imageView_WCDMA, tx_id);

                    if (time_all > 0) {
                        views.setTextViewText(R.id.textView1, "CELL_DCH: ".concat(new DecimalFormat("#.##").format(new Double((double) time_for_state[1] / 1000)).toString()).concat("s(").
                                concat(new DecimalFormat("#.##%").format(new Double((double) time_for_state[1] / time_all)).toString()).concat(")"));
                        views.setTextViewText(R.id.textView2, "URA_PCH: ".concat(new DecimalFormat("#.##").format(new Double((double) time_for_state[2] / 1000)).toString()).concat("s(").
                                concat(new DecimalFormat("#.##%").format(new Double((double) time_for_state[2] / time_all)).toString()).concat(")"));
                        views.setTextViewText(R.id.textView3, "CELL_FACH: ".concat(new DecimalFormat("#.##").format(new Double((double) time_for_state[3] / 1000)).toString()).concat("s(").
                                concat(new DecimalFormat("#.##%").format(new Double((double) time_for_state[3] / time_all)).toString()).concat(")"));
                        views.setTextViewText(R.id.textView4, "CELL_PCH: ".concat(new DecimalFormat("#.##").format(new Double((double) time_for_state[4] / 1000)).toString()).concat("s(").
                                concat(new DecimalFormat("#.##%").format(new Double((double) time_for_state[4] / time_all)).toString()).concat(")"));
                        views.setTextViewText(R.id.textView0, "DISCONN: ".concat(new DecimalFormat("#.##").format(new Double((double) time_for_state[0] / 1000)).toString()).concat("s(").
                                concat(new DecimalFormat("#.##%").format(new Double((double) time_for_state[0] / time_all)).toString()).concat(")"));
                    }
                }
            }

            else {
                views.setImageViewResource(R.id.imageView_WCDMA, R.drawable.basis);
            }

            if (play) {
                time_last = time_cur;
            }

        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

