package net.mobileinsight.widgetdemo;

import android.os.Bundle;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class UmtsCmStateWidget extends AppWidgetProvider {
    static public String umts_cm_state = "";
    static public boolean running = false;
    static public boolean isonline = true;

    MyAsynctask task = null;

    static public Queue<String> state_lst = new LinkedList<String>();
    static public Queue<String> time_lst = new LinkedBlockingDeque<>();
    static private String lastState = "";

    public final static String BROADCAST_COUNTER_ACTION = "MobileInsight.UmtsCm.COUNTER_ACTION";
    //    public final static String TABLE_BROADCAST_COUNTER_ACTION = "MobileInsight.Table.COUNTER_ACTION";
    private final static String LOG_TAG = "Caster-CM";
    public class MyAsynctask extends AsyncTask<Integer, Integer, Integer> {

        //        private Queue<String> time_lst;
        private Context context;
        String time_before = "";
        String state_before = "";

        @Override
        protected Integer doInBackground(Integer... vals) {

            while (running) {
                Log.i(LOG_TAG, "not stopped");
                time_before = time_lst.peek();
                state_before = state_lst.peek();
                Log.i(LOG_TAG, "State Before: " + state_before);
                if (time_lst.size() > 0) {
                    time_lst.remove();
                    state_lst.remove();
                }
                Log.i(LOG_TAG, "Current State: " + state_lst.peek());
                Log.i(LOG_TAG, "Num of remianed elements in time_lst: " + String.valueOf(time_lst.size()));

                if (time_before != null & time_lst.peek() != null) {
                    publishProgress(vals);
                    java.util.Date time1=new java.util.Date((long)Double.parseDouble(time_lst.peek())*1000-(long)Double.parseDouble(time_before)*1000);
                    Long time_sleep =time1.getTime(); //add 5000L FOR DEBUGGING
                    Log.i(LOG_TAG, "Time interval: " + String.valueOf(time_sleep));
                    try {
                        if(time_sleep>60000){
                            Thread.sleep(5000);
                        }else if( time_sleep < 0){
                            Thread.sleep(1000);
                        }else{
                            Thread.sleep(time_sleep);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return 1;
        }

        public MyAsynctask(Context context) {
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
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), UmtsCmStateWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        try {

            if (intent.getAction().equals("android.appwidget.action.APPWIDGET_DISABLED")) {
                running = false;
                if (task != null) {
                    task.cancel(true);
                }
                umts_cm_state = "Disabled";
                state_lst.clear();
                time_lst.clear();
                Log.i(LOG_TAG, "disabled fom receiver");
            }

            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals(BROADCAST_COUNTER_ACTION)) {

                umts_cm_state = intent.getStringExtra("last_state");
                for (int appWidgetId : appWidgetIds) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }
            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.UmtsNasAnalyzer.CM_STATE")) {

                //Log.i(LOG_TAG, "started " + intent.getAction());
                Bundle bundle = intent.getExtras();
                String state_tmp = bundle.getString("state");    //NEED TO CHANGE
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        Object value = bundle.get(key);
                        Log.d(LOG_TAG, String.format("%s %s (%s)", key,
                                value.toString(), value.getClass().getName()));
                    }
                }

                Log.d(LOG_TAG, "UmtsCmStateWidget" + " New broadcast message received ".concat(state_tmp));

                if (!isonline) {
                    String time_tmp = intent.getStringExtra("timestamp");
                    Log.d(LOG_TAG, "UmtsCmStateWidget" + "Timestamp message received ".concat(time_tmp));
                    if (state_tmp != null && time_tmp != null) {
                        state_lst.offer(state_tmp);
                        time_lst.offer(time_tmp);
                    }
                } else {
                    umts_cm_state = "state_tmp";
                }

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }

            } else if (intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED") || intent.getAction().equals("MobileInsight.OnlineMonitor.STARTED")) {

                Log.i(LOG_TAG, "started " + intent.getAction());

                if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED")) {
                    isonline = false;
                    if (task != null) {
                        task.cancel(true);
                    }
                    task = new MyAsynctask(context);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    running = true;
                    Log.i(LOG_TAG, "started " + String.valueOf(task));
                } else {
                    isonline = true;
                    running = false;
                    if (task != null) {
                        task.cancel(true);
                    }
                }
                umts_cm_state = "Starting";
                state_lst.clear();
                time_lst.clear();

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        super.onReceive(context, intent);

    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.umts_cm_state_widget);
        Log.d(LOG_TAG, "Update Message umts cm state " + lastState);
        Log.d(LOG_TAG, "Update Message umts cm state " + umts_cm_state);
        int tx_id = -1;
        switch (umts_cm_state) {
            case "CM_ALERTING":
                tx_id = R.drawable.cm_alert;  //DRAWABLES *************

                break;
            case "CM_CALL_PROCEEDING":
                tx_id = R.drawable.cm_call;
                break;
            case "CM_CONNECT":
                tx_id = R.drawable.cm_connect;
                break;
            case "CM_CONNECT_ACK":
                tx_id = R.drawable.cm_connect;
                break;
            case "CM_DISCONNET": {
                switch (lastState) {
                    case "CM_CALL_PROCEEDING":
                        tx_id = R.drawable.cm_call_disconnect;
                        break;
                    case "CM_ALERTING":
                        tx_id = R.drawable.cm_alert_disconnect;
                        break;
                    case "CM_CONNECT":
                        tx_id = R.drawable.cm_connect_disconnect;
                        break;
                    case "CM_CONNECT_ACK":
                        tx_id = R.drawable.cm_connect_disconnect;
                        break;
                }
                break;
            }
            case "CM_IDLE": {
                switch (lastState) {
                    case "CM_SETUP":
                        tx_id = R.drawable.cm_setup_idle;
                        break;
                    case "CM_SERVICE_REQUEST":
                        tx_id = R.drawable.cm_setup_idle;
                        break;
                    case "CM_RELEASE":
                        tx_id = R.drawable.cm_idle;
                        break;
                }
                break;
            }

            case "CM_SETUP":
                tx_id = R.drawable.cm_setup;
                break;
            case "CM_SERVICE_REQUEST":
                tx_id = R.drawable.cm_setup;
                break;
            case "CM_RELEASE": {
                switch (lastState) {
                    case "CM_CONNECT":
                        tx_id = R.drawable.cm_connect_release;
                        break;
                    case "CM_DISCONNET":
                        tx_id = R.drawable.cm_disconnect_release;
                        break;
                }
                break;
            }

            default:
                tx_id = R.drawable.cm_idle;
                views.setTextViewText(R.id.textView1, "");
                break;

        }
        lastState = umts_cm_state;
        if(tx_id!=-1){
            views.setImageViewResource(R.id.imageView_UMTS_CM, tx_id);
        }
        else{
            views.setImageViewResource(R.id.imageView_UMTS_CM, R.drawable.cm_idle);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}