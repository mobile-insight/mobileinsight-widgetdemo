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
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class LteRrcStateWidget extends AppWidgetProvider {

    static public String lte_rrc_state = "";
    static public String next_state = "";

    static public String timeinfo = "";
    static public String time_init = "";
    static public long[] time_for_state = {0, 0, 0, 0}; //0-LTE_RRC_IDLE; 1-CRX; 2-Short_DRX; 3-Long_DRX
    static public long time_all = 0;
    static public long time_last = 0;
    static public long time_cur = 0;

    static public BlockingDeque<String> state_lst = new LinkedBlockingDeque<String>();
    static public BlockingDeque<String> time_lst = new LinkedBlockingDeque<String>();

    private final static String LOG_TAG = "Caster-LTERRC";
    public final static String BROADCAST_COUNTER_ACTION = "MobileInsight.LteRrc.COUNTER_ACTION";

    static public boolean play = true;
    static public boolean running = false;
    static public boolean isonline = true;

    MyAsynctask task = null;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }


    @Override
    public void onEnabled(Context context) {
    }

    public class MyAsynctask extends AsyncTask<Integer, Integer, Integer> {

        private Context context;
        String time_before = "";
        String state_before = "";
        String time_now = "";
        String state_now = "";
        @Override
        protected Integer doInBackground(Integer... vals) {

            while (running) {
                time_before = time_lst.peek();
                if (time_lst.size() > 0 )
                {
                    state_before = state_lst.peek();
                    time_lst.remove();
                    state_lst.remove();
                }
                while (time_before != null && time_lst.peek() != null && time_before.equals(time_lst.peek())){
                    state_before = state_lst.peek();
                    time_lst.remove();
                    state_lst.remove();
                }
                Log.i(LOG_TAG, "Num of remianed elements in time_lst: "+String.valueOf(time_lst.size()));
                if (time_before != null && time_lst.peek()!= null) {
                    time_now = time_lst.peek();
                    state_now = state_lst.peek();
                    if (time_lst.size() > 0 )
                    {
                        time_lst.remove();
                        state_lst.remove();
                    }
                    while (time_now != null && time_lst.peek() != null && time_now.equals(time_lst.peek())){
                        state_now = state_lst.peek();
                        time_lst.remove();
                        state_lst.remove();
                    }
                    try {
                        time_lst.putFirst(time_now);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        state_lst.putFirst(state_now);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
            return 1;
        }

        public MyAsynctask(Context context){
            this.context = context;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Intent intent = new Intent();
            intent.setAction(BROADCAST_COUNTER_ACTION);
            intent.putExtra("time_show", time_before);
            intent.putExtra("last_state", state_before);
            intent.putExtra("cur_time", time_now);
            intent.putExtra("cur_state", state_now);
            this.context.sendBroadcast(intent);
        }

        @Override
        protected void onPostExecute(Integer val) {
            Log.i(LOG_TAG, "progress ending");
        }

        @Override
        protected void onPreExecute() {
            Log.i(LOG_TAG, "progress starting");
        }

        @Override
        protected void onCancelled(){
            Log.i(LOG_TAG, "task is cancelled");
        }
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), LteRrcStateWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if(intent.getAction().equals("android.appwidget.action.APPWIDGET_ENABLED")){
//            if (task != null) {
//                task.cancel(true);
//            }
//            task = new MyAsynctask(context);
//            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            running = true;
            Log.i(LOG_TAG, "enabled fom receiver");
        }

        if(intent.getAction().equals("android.appwidget.action.APPWIDGET_DISABLED")){
            running = false;
            if(task != null)
            {
                task.cancel(true);
            }

            lte_rrc_state = "";
            next_state = ""; // next state to show in the figure
            timeinfo = ""; // current time infomation
            time_init = "";
            for (int i = 0; i < time_for_state.length; i++) {
                time_for_state[i] = 0;
            }
            time_all = 0;
            time_last = 0;
            time_cur = 0;

            state_lst.clear();
            time_lst.clear();

            Log.i(LOG_TAG, "disabled fom receiver");
        }

        if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals(BROADCAST_COUNTER_ACTION)) {

            if (intent.getStringExtra("cur_state")!=null && intent.getStringExtra("cur_time")!=null
                    && intent.getStringExtra("time_show")!=null && intent.getStringExtra("last_state") != null) {
                next_state = intent.getStringExtra("cur_state");
                timeinfo = intent.getStringExtra("cur_time");
                time_last = Timestamp.valueOf(intent.getStringExtra("time_show")).getTime();
                lte_rrc_state = intent.getStringExtra("last_state");
            }
            if (next_state != null && timeinfo!= null && !timeinfo .equals("") ){
                time_cur = Timestamp.valueOf(timeinfo).getTime();
            }

            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }

        if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.LteRrcAnalyzer.DRX")){

            String received_state = intent.getStringExtra("DRX state");
            String received_time = intent.getStringExtra("Timestamp");
            Log.i(LOG_TAG, String.valueOf(isonline));
            if (received_state != null & received_time != null) {
                if (isonline) {
                    lte_rrc_state = next_state;
                    next_state = intent.getStringExtra("DRX state");
                    timeinfo = intent.getStringExtra("Timestamp");
                    time_last = time_cur;
                    time_cur = Timestamp.valueOf(timeinfo).getTime();
                    Log.i(LOG_TAG, "Received Time: " + received_time);
                    Log.i(LOG_TAG, "Received State: " + received_state);

                    if (appWidgetIds != null && appWidgetIds.length > 0) {
                        onUpdate(context, appWidgetManager, appWidgetIds);
                    }
                }
                else{
                    state_lst.offer(received_state);
                    time_lst.offer(received_time);
                    Log.i(LOG_TAG, "Received Time: " + received_time);
                    Log.i(LOG_TAG, "Received State: " + received_state);
                }
            }
        }

        else if (intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED") || intent.getAction().equals("MobileInsight.OnlineMonitor.STARTED")) {
            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED")) {
                isonline = false;
                if (task != null) {
                    task.cancel(true);
                }
                task = new MyAsynctask(context);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                running = true;
                Log.i(LOG_TAG, "enabled fom receiver");

            }
            else{
                running = false;
                if (task != null) {
                    task.cancel(true);
                }
            }

            Log.i(LOG_TAG, "started");

            lte_rrc_state = "";
            next_state = ""; // next state to show in the figure
            timeinfo = ""; // current time infomation
            time_init = "";
            for (int i = 0; i < time_for_state.length; i++) {
                time_for_state[i] = 0;
            }
            time_all = 0;
            time_last = 0;
            time_cur = 0;

            state_lst.clear();
            time_lst.clear();

            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
        super.onReceive(context, intent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lte_rrc_state_widget);

        if (time_init.equals("")) {
            time_init = timeinfo;
            time_last = time_cur;
        }

        if(lte_rrc_state.equals("")){
            lte_rrc_state = next_state;
            views.setImageViewResource(R.id.imageView_LTE, R.drawable.lte_rrc_basis);

            views.setTextViewText(R.id.textView0, "IDLE: 0.00(0%)");
            views.setTextViewText(R.id.textView2, "ShortDRX: 0.00(0%)");
            views.setTextViewText(R.id.textView3, "LongDRX: 0.00(0%)");
            views.setTextViewText(R.id.textView1, "CRX: 0.00(0%)");

        }
        else{
            int tx_id = -1;

            long time_inter = time_cur - time_last;
            if (time_inter < 0){
                time_inter = 0;
            }
            time_all += time_inter;

            switch (lte_rrc_state){
                case "IDLE":{
                    switch (next_state){
                        case "CRX":
                            tx_id = R.drawable.lte_rrc_idle_crx;
                            break;
                        case "IDLE":
                            break;
                        default:
                            Log.e("LteRrcStateWidget","Invalid transition ".concat(lte_rrc_state).concat("->").concat(next_state));
                            break;
                    }
                    time_for_state[0] += time_inter;
                    break;
                }
                case "CRX":{
                    switch (next_state){
                        case "CRX":
                            break;
                        case "IDLE":
                            tx_id = R.drawable.lte_rrc_crx_idle;
                            break;
                        case "LONG_DRX":
                            tx_id = R.drawable.lte_rrc_crx_long;
                            break;
                        case "SHORT_DRX":
                            tx_id = R.drawable.lte_rrc_crx_short;
                            break;
                        default:
                            Log.e("LteRrcStateWidget","Invalid transition ".concat(lte_rrc_state).concat("->").concat(next_state));
                            break;
                    }
                    time_for_state[1] += time_inter;
                    break;
                }
                case "SHORT_DRX":{
                    switch (next_state){
                        case "CRX":
                            tx_id = R.drawable.lte_rrc_short_crx;
                            break;
                        case "LONG_DRX":
                            tx_id = R.drawable.lte_rrc_short_long;
                            break;
                        case "IDLE":
                            tx_id = R.drawable.lte_rrc_crx_idle;
                            break;
                        case "SHORT_DRX":
                            break;
                        default:
                            Log.e("LteRrcStateWidget","Invalid transition ".concat(lte_rrc_state).concat("->").concat(next_state));
                            break;
                    }
                    time_for_state[2] += time_inter;
                    break;
                }
                case "LONG_DRX":{
                    switch (next_state){

                        case "CRX":
                            tx_id = R.drawable.lte_rrc_long_crx;
                            Log.i("Debugging","LONG->CRX");
                            break;
                        case "IDLE":
                            tx_id = R.drawable.lte_rrc_crx_idle;
                            break;
                        case "LONG_DRX":
                            break;
                        default:
                            Log.e("LteRrcStateWidget","Invalid transition ".concat(lte_rrc_state).concat("->").concat(next_state));
                            break;
                    }
                    time_for_state[3] += time_inter;
                    break;
                }
                default:{
                    Log.e("LteRrcStateWidget","Invalid transition ".concat(lte_rrc_state).concat("->").concat(next_state));
                    break;
                }


            }

            if (time_all == 0) {
                views.setTextViewText(R.id.textView0, "IDLE: 0.00(0%)");
                views.setTextViewText(R.id.textView2, "ShortDRX: 0.00(0%)");
                views.setTextViewText(R.id.textView3, "LongDRX: 0.00(0%)");
                views.setTextViewText(R.id.textView1, "CRX: 0.00(0%)");
                views.setImageViewResource(R.id.imageView_LTE, R.drawable.lte_rrc_basis);
            }


            else {
                views.setTextViewText(R.id.textView0, "IDLE: ".concat(new DecimalFormat("#.##").format(Double.valueOf((double) time_for_state[0] / 1000))).concat("s(").
                        concat(new DecimalFormat("#.##%").format(Double.valueOf((double) time_for_state[0] / time_all))).concat(")"));

                views.setTextViewText(R.id.textView2, "ShortDRX: ".concat(new DecimalFormat("#.##").format(new Double((double) time_for_state[2] / 1000))).concat("s(").
                        concat(new DecimalFormat("#.##%").format(new Double((double) time_for_state[2] / time_all))).concat(")"));
                views.setTextViewText(R.id.textView3, "LongDRX: ".concat(new DecimalFormat("#.##").format(new Double((double) time_for_state[3] / 1000))).concat("s(").
                        concat(new DecimalFormat("#.##%").format(new Double((double) time_for_state[3] / time_all))).concat(")"));
                views.setTextViewText(R.id.textView1, "CRX: ".concat(new DecimalFormat("#.##").format(new Double((double) time_for_state[1] / 1000))).concat("s(").
                        concat(new DecimalFormat("#.##%").format(new Double((double) time_for_state[1] / time_all))).concat(")"));

                if(tx_id!=-1){
                    views.setImageViewResource(R.id.imageView_LTE, tx_id);
                }
            }

        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

