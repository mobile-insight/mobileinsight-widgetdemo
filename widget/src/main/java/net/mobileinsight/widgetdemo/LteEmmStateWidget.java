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
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class LteEmmStateWidget extends AppWidgetProvider {
    static public String lte_emm_state = "";
    static public String substate = "";

    static public boolean running = false;
    static public boolean isonline = true;

    MyAsynctask task = null;

    static public Queue<String> state_lst = new LinkedList<String>();
    static public Queue<String> time_lst = new LinkedBlockingDeque<>();

    public final static String BROADCAST_COUNTER_ACTION = "MobileInsight.LteEmm.COUNTER_ACTION";
    private final static String LOG_TAG = "Caster-LTEEMM";
    public class MyAsynctask extends AsyncTask<Integer, Integer, Integer> {

        private Context context;
        String time_before = "";
        String state_before = "";

        @Override
        protected Integer doInBackground(Integer... vals) {

            while (running) {
                time_before = time_lst.peek();
                state_before = state_lst.peek();
                if (time_lst.size() > 0) {
                    time_lst.remove();
                    state_lst.remove();
                }
                Log.i(LOG_TAG, "Num of remaining elements in time_lst: " + String.valueOf(time_lst.size()));
                if (time_before != null & time_lst.peek() != null) {
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

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), LteEmmStateWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if(intent.getAction().equals("android.appwidget.action.APPWIDGET_DISABLED")){
            running = false;
            if(task != null)
            {
                task.cancel(true);
            }
            lte_emm_state = "";
            substate = "";
            state_lst.clear();
            time_lst.clear();
            Log.i(LOG_TAG, "disabled fom receiver");
        }

        if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals(BROADCAST_COUNTER_ACTION)){
            try{
                lte_emm_state = intent.getStringExtra("last_state").split("\t")[0];
                substate = intent.getStringExtra("last_state").split("\t")[1];

                for (int appWidgetId : appWidgetIds) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.LteNasAnalyzer.EMM_STATE")) {

            String state_tmp = intent.getStringExtra("conn state");

            String substate_tmp = intent.getStringExtra("conn substate");
            Log.d(LOG_TAG, "EmmStateWidget" + " New broadcast message received ".concat(state_tmp));


            if (!isonline) {
                String time_tmp = intent.getStringExtra("timestamp");
                Log.d(LOG_TAG, "EmmStateWidget" + "New broadcast message received ".concat(time_tmp));
                if (state_tmp != null & substate_tmp != null &time_tmp != null) {

                    state_lst.offer(state_tmp + "\t" +substate_tmp);
                    Log.i(LOG_TAG, String.valueOf(time_lst.size()));
                    time_lst.offer(time_tmp);
                }
            }
            else{
                lte_emm_state = state_tmp;
                substate = substate_tmp;
            }


            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }

        }

        else if (intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED") || intent.getAction().equals("MobileInsight.OnlineMonitor.STARTED")) {

            Log.i(LOG_TAG, "started " + intent.getAction());

            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.OfflineReplayer.STARTED")) {
                isonline = false;
                if (task != null) {
                    task.cancel(true);
                }
                task = new MyAsynctask(context);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                running = true;

            }
            else{
                isonline = true;
                running = false;
                if (task != null) {
                    task.cancel(true);
                }
            }
            lte_emm_state = "";
            substate = "";
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

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.lte_emm_state_widget);
        Log.d(LOG_TAG, "Update Message " + String.valueOf(lte_emm_state));
        Log.d(LOG_TAG, "Update Message " + String.valueOf(substate));
        views.setTextViewText(R.id.textView2, substate);

        LteWcdmaTableWidget.lte_emm_state = lte_emm_state;

        int tx_id = -1;
        switch (lte_emm_state) {
            case "EMM_REGISTERED" :
                tx_id = R.drawable.lte_emm_registered;
                break;
            case "EMM_DEREGISTERED":
                tx_id = R.drawable.lte_emm_deregistered;
                break;
            default:
                tx_id = R.drawable.lte_emm_basis;
                break;
        }
        if(tx_id != -1){
            views.setImageViewResource(R.id.imageView_LTE_EMM, tx_id);
        }
        else{
            views.setImageViewResource(R.id.imageView_LTE_EMM, R.drawable.basis);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
