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

/**
 * Created by hjfang on 16/11/20.
 */

//TODO: add track of states from other widgets. For example, add track for lte_rrc_state. If LTE
//TODO: widget is working, update from the remote widget.

public class LteWcdmaTableWidget extends AppWidgetProvider {
    static public String lte_esm_state = "XXX";
    static public String lte_emm_state = "XXX";
    static public String lte_rrc_state = "XXX";
    static public String wcdma_mm_state = "XXX";
    static public String wcdma_gmm_state = "XXX";
    static public String wcdma_rrc_state = "XXX";

    static public String [] states = {"XXX", "XXX", "XXX", "XXX", "XXX", "XXX"}; //LTEESM,LTEEMM,  LTERRC, WCDMAMM, WCDMAGMM, WCDMARRC

    static public boolean isonline = true;
    static public boolean running = false;

    static public Queue<String[]> state_lst = new LinkedList<>();
    static public Queue<String> time_lst = new LinkedBlockingDeque<>();

    MyAsynctask task = null;

    private final static String LOG_TAG = "Caster-TABLE";
    public final static String TABLE_BROADCAST_COUNTER_ACTION = "MobileInsight.Table.COUNTER_ACTION";

    public class MyAsynctask extends AsyncTask<Integer, Integer, Integer> {

        private Context context;
        String time_before = "";
        String [] state_before = {"XXX", "XXX", "XXX", "XXX", "XXX", "XXX"};

        @Override
        protected Integer doInBackground(Integer... vals) {
            while (running) {
                time_before = time_lst.peek();
                state_before = state_lst.peek();
                if (time_lst.size() > 0) {
                    time_lst.remove();
                    state_lst.remove();
                }
                Log.i(LOG_TAG, "Num of remianed elements in time_lst: " + String.valueOf(time_lst.size()));
                if (time_before != null & time_lst.peek() != null) {
                    publishProgress(vals);

                    Log.i(LOG_TAG, "update once");
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
            Log.i(LOG_TAG, states[0] + "\t" + states[1] + "\t" + states[2] + "\t" + states[3] + "\t" + states[4] + "\t" + states[5]);
            super.onProgressUpdate(values);
            Intent intent = new Intent();
            intent.setAction(TABLE_BROADCAST_COUNTER_ACTION);
            intent.putExtra("time_show", time_before);
            intent.putExtra("last_state_0", state_before[0]);
            intent.putExtra("last_state_1", state_before[1]);
            intent.putExtra("last_state_2", state_before[2]);
            intent.putExtra("last_state_3", state_before[3]);
            intent.putExtra("last_state_4", state_before[4]);
            intent.putExtra("last_state_5", state_before[5]);
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
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), LteWcdmaTableWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        try {

            if (intent.getAction().equals("android.appwidget.action.APPWIDGET_DISABLED")) {
                running = false;
                if (task != null) {
                    task.cancel(true);
                }
                lte_esm_state = "XXX";
                lte_emm_state = "XXX";
                lte_rrc_state = "XXX";
                wcdma_mm_state = "XXX";
                wcdma_gmm_state = "XXX";
                wcdma_rrc_state = "XXX";
                state_lst.clear();
                time_lst.clear();

                Log.i(LOG_TAG, "disabled fom receiver");
            }

            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals(TABLE_BROADCAST_COUNTER_ACTION)) {

                lte_esm_state = intent.getStringExtra("last_state_0");
                lte_emm_state = intent.getStringExtra("last_state_1");
                lte_rrc_state = intent.getStringExtra("last_state_2");
                wcdma_mm_state = intent.getStringExtra("last_state_3");
                wcdma_gmm_state = intent.getStringExtra("last_state_4");
                wcdma_rrc_state = intent.getStringExtra("last_state_5");

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
                } else {
                    running = false;
                    if (task != null) {
                        task.cancel(true);
                    }
                }
                lte_esm_state = "XXX";
                lte_emm_state = "XXX";
                lte_rrc_state = "XXX";
                wcdma_mm_state = "XXX";
                wcdma_gmm_state = "XXX";
                wcdma_rrc_state = "XXX";
                state_lst.clear();
                time_lst.clear();

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }

            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.LteNasAnalyzer.ESM_STATE")) {

                String state_tmp = intent.getStringExtra("conn state");

                if (!isonline) {
                    String time_tmp = intent.getStringExtra("timestamp");
                    if (state_tmp != null & time_tmp != null) {
                        states[0] = state_tmp;
                        String[] newStates = new String[states.length];
                        System.arraycopy(states, 0, newStates, 0, states.length);
                        state_lst.offer(newStates);
                        Log.i(LOG_TAG, String.valueOf(time_lst.size()));
                        time_lst.offer(time_tmp);
                    }
                } else {
                    lte_esm_state = state_tmp;
                }

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }

            }
            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.LteNasAnalyzer.EMM_STATE")) {

                String state_tmp = intent.getStringExtra("conn state");


                if (!isonline) {
                    String time_tmp = intent.getStringExtra("timestamp");
                    if (state_tmp != null & time_tmp != null) {
                        states[1] = state_tmp;
                        String[] newStates = new String[states.length];
                        System.arraycopy(states, 0, newStates, 0, states.length);
                        state_lst.offer(newStates);
                        Log.i(LOG_TAG, String.valueOf(time_lst.size()));
                        time_lst.offer(time_tmp);
                    }
                } else {
                    lte_emm_state = state_tmp;
                }

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }

            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.UmtsNasAnalyzer.MM_STATE")) {

                String state_tmp = intent.getStringExtra("conn state");

                if (!isonline) {
                    String time_tmp = intent.getStringExtra("timestamp");
                    if (state_tmp != null & time_tmp != null) {
                        states[3] = state_tmp;
                        String[] newStates = new String[states.length];
                        System.arraycopy(states, 0, newStates, 0, states.length);
                        state_lst.offer(newStates);
                        Log.i(LOG_TAG, String.valueOf(time_lst.size()));
                        time_lst.offer(time_tmp);
                    }
                } else {
                    wcdma_mm_state = state_tmp;
                }

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }

            }
            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.UmtsNasAnalyzer.GMM_STATE")) {

                String state_tmp = intent.getStringExtra("conn state");

                if (!isonline) {
                    String time_tmp = intent.getStringExtra("timestamp");
                    if (state_tmp != null & time_tmp != null) {
                        states[4] = state_tmp;
                        String[] newStates = new String[states.length];
                        System.arraycopy(states, 0, newStates, 0, states.length);
                        state_lst.offer(newStates);
                        Log.i(LOG_TAG, String.valueOf(time_lst.size()));
                        time_lst.offer(time_tmp);
                    }
                } else {
                    wcdma_gmm_state = state_tmp;
                }

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }
            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.LteRrcAnalyzer.DRX")) {

                String state_tmp = intent.getStringExtra("DRX state");
                String time_tmp = intent.getStringExtra("Timestamp");

                if (!isonline) {
                    if (state_tmp != null & time_tmp != null) {
                        states[2] = state_tmp;
                        String[] newStates = new String[states.length];
                        System.arraycopy(states, 0, newStates, 0, states.length);
                        state_lst.offer(newStates);
                        Log.i(LOG_TAG, String.valueOf(time_lst.size()));
                        time_lst.offer(time_tmp);
                    }
                } else {
                    lte_rrc_state = state_tmp;
                }

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }

            }
            if (appWidgetIds != null && appWidgetIds.length > 0 && intent.getAction().equals("MobileInsight.WcdmaRrcAnalyzer.RRC_STATE")) {
                String state_tmp = intent.getStringExtra("RRC State");
                String time_tmp = intent.getStringExtra("Timestamp");

                if (!isonline) {
                    if (state_tmp != null & time_tmp != null) {
                        states[5] = state_tmp;
                        String[] newStates = new String[states.length];
                        System.arraycopy(states, 0, newStates, 0, states.length);
                        state_lst.offer(newStates);
                        Log.i(LOG_TAG, String.valueOf(time_lst.size()));
                        time_lst.offer(time_tmp);
                    }
                } else {
                    wcdma_rrc_state = state_tmp;
                }

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

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.lte_wcdma_table_widget);

        views.setTextViewText(R.id.text_4G_EMM_content, ("XXX").equals(lte_emm_state) ? "Unavailable" : lte_emm_state);
        views.setTextViewText(R.id.text_4G_ESM_content, ("XXX").equals(lte_esm_state) ? "Unavailable" : lte_esm_state);
        views.setTextViewText(R.id.text_4G_RRC_content, ("XXX").equals(lte_rrc_state) ? "Unavailable" : lte_rrc_state);
        views.setTextViewText(R.id.text_3G_MM_content,  ("XXX").equals(wcdma_mm_state)? "Unavailable" : wcdma_mm_state);
        views.setTextViewText(R.id.text_3G_GMM_content, ("XXX").equals(wcdma_gmm_state) ? "Unavailable" : wcdma_gmm_state);
        views.setTextViewText(R.id.text_3G_RRC_content, ("XXX").equals(wcdma_rrc_state) ? "Unavailable" : wcdma_rrc_state);

        appWidgetManager.updateAppWidget(appWidgetId, views);

    }
}