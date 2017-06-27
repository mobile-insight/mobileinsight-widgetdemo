package net.mobileinsight.widgetdemo;
 
/**
 * Created by Danrui
 */
 
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

public class WcdmaMmStateWidget extends AppWidgetProvider {
    static public String nas_mm_state = "";
    static public String substate = "";
    static public String update_state = "";
    static public boolean running = false;
    static public boolean isonline = true;
    static public boolean play = true;

    MyAsynctask task = null;

    static public Queue<String> state_lst = new LinkedList<String>();
    static public Queue<String> time_lst = new LinkedBlockingDeque<>();

    public final static String BROADCAST_COUNTER_ACTION = "MobileInsight.WcdmaMm.COUNTER_ACTION";
    private final static String LOG_TAG = "Caster-MM";
    public class MyAsynctask extends AsyncTask<Integer, Integer, Integer> {

        private Context context;
        String time_before = "";
        String state_before = "";

        @Override
        protected Integer doInBackground(Integer... vals) {

            while (running) {
                if (play) {
                    time_before = time_lst.peek();
                    state_before = state_lst.peek();
                    if (time_lst.size() > 0) {
                        time_lst.remove();
                        state_lst.remove();
                    }
                    Log.i(LOG_TAG, "Num of remaining elements in time_lst: " + String.valueOf(time_lst.size()));
                    while (state_lst.peek() != null && "CONNECTING".equals(state_lst.peek())) {
                        time_lst.remove();
                        state_lst.remove();
                    }
                    if (time_before != null & time_lst.peek() != null) {
                        publishProgress(vals);

                        Long time_sleep = Timestamp.valueOf(time_lst.peek()).getTime() - Timestamp.valueOf(time_before).getTime();
                        Log.i(LOG_TAG,"Time inter in milisec: " + String.valueOf(time_sleep));
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
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }
 
    @Override
    public void onReceive(Context context, Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), WcdmaMmStateWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        try {

            if (intent.getAction().equals("android.appwidget.action.APPWIDGET_DISABLED")) {
                running = false;
                if (task != null) {
                    task.cancel(true);
                }

                Log.i(LOG_TAG, "disabled fom receiver");
            }

            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals(BROADCAST_COUNTER_ACTION)) {

                if (intent.getStringExtra("last_state").split("\t") != null) {
                    nas_mm_state = intent.getStringExtra("last_state").split("\t")[0];
                    substate = intent.getStringExtra("last_state").split("\t")[1];
                    update_state = intent.getStringExtra("last_state").split("\t")[2];
                }

                final int N = appWidgetIds.length;
                for (int i = 0; i < N; i++) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }

            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.UmtsNasAnalyzer.MM_STATE")) {

                String state_tmp = intent.getStringExtra("conn state");

                String substate_tmp = intent.getStringExtra("conn substate");
                String update_state_tmp = intent.getStringExtra("update state");

                Log.d(LOG_TAG, "NASmmStateWidget" + "New broadcast message received ".concat(nas_mm_state));
                Log.d(LOG_TAG, "NASmmStateWidget" + "New broadcast message received ".concat(substate));
                Log.d(LOG_TAG, "NASmmStateWidget" + "New broadcast message received ".concat(update_state));
                Log.d(LOG_TAG, "isonline: " + String.valueOf(isonline));

                if (!isonline) {
                    String time_tmp = intent.getStringExtra("timestamp");
                    if (state_tmp != null & substate_tmp != null & time_tmp != null & update_state_tmp != null) {
                        state_lst.offer(state_tmp + "\t" + substate_tmp + "\t" + update_state_tmp);
                        Log.i(LOG_TAG, String.valueOf(time_lst.size()));
                        time_lst.offer(time_tmp);
                    }
                } else {
                    nas_mm_state = state_tmp;
                    substate = substate_tmp;
                    update_state = update_state_tmp;
                }
                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }

            } else if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED") || intent.getAction().equals("MobileInsight.OnlineMonitor.STARTED")) {

                Log.i(LOG_TAG, "started " + intent.getAction());

                if (intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED")) {
                    isonline = false;
                    if (task != null) {
                        task.cancel(true);
                    }
                    task = new MyAsynctask(context);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    running = true;
                    state_lst.clear();
                    time_lst.clear();
                } else {
                    if (task != null) {
                        task.cancel(true);
                    }
                }
                nas_mm_state = "";
                play = true;

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
 
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        //Todo...
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.wcdma_mm_state_widget);
        Log.d(LOG_TAG, "Update Message " + String.valueOf(nas_mm_state));
        Log.d(LOG_TAG, "Update Message " + String.valueOf(substate));
        Log.d(LOG_TAG, "Update Message " + String.valueOf(update_state));


        String substate_text = substate == null || ("").equals(substate)  ? "Unavailable" : substate;
        String update_state_text = update_state == null || ("").equals(update_state) ? "Unavailable" : update_state;

        views.setTextViewText(R.id.substate, "Substate: " + substate_text);
        views.setTextViewText(R.id.updateState, "Update Status: " + update_state_text);


        int tx_id = -1;
        switch (nas_mm_state) {
            case "MM_WAIT_FOR_NETWORK_COMMAND" :
                tx_id = R.drawable.wcdma_mm_wait_for_network_command;
                break;
            case "MM_IDLE":
                tx_id = R.drawable.wcdma_mm_idle;
                break;
            case "MM_WAIT_FOR_OUTGOING_MM_CONNECTION" :
                tx_id = R.drawable.wcdma_mm_wait_for_outgoing_mm_connection;
                break;
            case "MM_CONNECTION_ACTIVE":
                tx_id = R.drawable.wcdma_mm_connection_active;
                break;
            default:
                tx_id = R.drawable.wcdma_mm_basis;
                break;
        }
        if(tx_id!=-1){
            views.setImageViewResource(R.id.imageView_NAS_MM, tx_id);
        }
        else{
            views.setImageViewResource(R.id.imageView_NAS_MM, R.drawable.basis);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}