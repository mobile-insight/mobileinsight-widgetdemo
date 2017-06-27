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

public class LteEsmStateWidget extends AppWidgetProvider {
    static public String lte_esm_state = "";
    static public boolean running = false;
    static public boolean isonline = true;

    MyAsynctask task = null;

    static public Queue<String> state_lst = new LinkedList<String>();
    static public Queue<String> time_lst = new LinkedBlockingDeque<>();

    public final static String BROADCAST_COUNTER_ACTION = "MobileInsight.LteEsm.COUNTER_ACTION";
    private final static String LOG_TAG = "Caster-LTEESM";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    public class MyAsynctask extends AsyncTask<Integer, Integer, Integer> {

        private Context context;
        String time_before = "";
        String state_before = "";
        @Override
        protected Integer doInBackground(Integer... vals) {

            while (running) {
                time_before = time_lst.peek();
                state_before = state_lst.peek();
                if (time_lst.size() > 0 )
                {
                    time_lst.remove();
                    state_lst.remove();
                }
                Log.i(LOG_TAG, "Num of remained elements in time_lst: "+String.valueOf(time_lst.size()));
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
    public void onReceive(Context context, Intent intent) {
 
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), LteEsmStateWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if(intent.getAction().equals("android.appwidget.action.APPWIDGET_DISABLED")){
            lte_esm_state = "";
            state_lst.clear();
            time_lst.clear();
            running = false;
            if(task != null)
            {
                task.cancel(true);
            }
            Log.i(LOG_TAG, "disabled fom receiver");
        }

        if(intent.getAction().equals(BROADCAST_COUNTER_ACTION)){
            try {
                lte_esm_state = intent.getStringExtra("last_state");

                for (int appWidgetId : appWidgetIds) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
 
        if(appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.LteNasAnalyzer.ESM_STATE")) {
 
            String state_tmp = intent.getStringExtra("conn state");

            Log.d(LOG_TAG, "EsmStateWidget "+"New broadcast message received ".concat(lte_esm_state));
            Log.i(LOG_TAG, "isOnline " + String.valueOf(isonline));

            if (!isonline) {
                String time_tmp = intent.getStringExtra("timestamp");
                if (state_tmp != null) {
                    state_lst.offer(state_tmp);
                }
                if (time_tmp != null) {
                    Log.i(LOG_TAG, String.valueOf(time_lst.size()));
                    time_lst.offer(time_tmp);
                }
            }
            else{
                lte_esm_state = state_tmp;
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
                running = false;
                if (task != null) {
                    task.cancel(true);
                }
            }
            lte_esm_state = "";
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
 
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.lte_esm_state_widget);
        Log.d(LOG_TAG, "Message "+String.valueOf(lte_esm_state));
        int tx_id;
        switch (lte_esm_state) {
            case "connected" :
                tx_id = R.drawable.lte_esm_connected;
                break;
            case "disconnected":
                tx_id = R.drawable.lte_esm_disconnected;
                break;
            default:
                tx_id = R.drawable.lte_esm_basis;
                break;
        }
        if(tx_id != -1){
            views.setImageViewResource(R.id.imageView_LTE_ESM, tx_id);
        }
        else{
            views.setImageViewResource(R.id.imageView_LTE_ESM, R.drawable.basis);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }
}