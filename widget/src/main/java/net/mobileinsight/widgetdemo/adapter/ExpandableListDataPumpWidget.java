package net.mobileinsight.widgetdemo.adapter;


import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import net.mobileinsight.widgetdemo.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ExpandableListDataPumpWidget {
    public static LinkedHashMap<String, List<Spanned>> getData(Context context) {
        LinkedHashMap<String, List<Spanned>> expandableListDetail = new LinkedHashMap<String, List<Spanned>>();

        List<Spanned> lte_emm_wdiget = new ArrayList<Spanned>();
        List<Spanned> lte_esm_wdiget = new ArrayList<Spanned>();
        List<Spanned> lte_rrc_wdiget = new ArrayList<Spanned>();
        List<Spanned> wcdma_gmm_wdiget = new ArrayList<Spanned>();
        List<Spanned> wcdma_mm_wdiget = new ArrayList<Spanned>();
        List<Spanned> wcdma_rrc_wdiget = new ArrayList<Spanned>();
        List<Spanned> lte_phy_wdiget = new ArrayList<Spanned>();
        List<Spanned> mg_wdiget = new ArrayList<Spanned>();
        List<Spanned> table_wdiget = new ArrayList<Spanned>();

        if (Build.VERSION.SDK_INT >= 24) {
            lte_emm_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.lte_emm_widget_intro),Html.FROM_HTML_MODE_LEGACY));
            lte_esm_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.lte_esm_widget_intro),Html.FROM_HTML_MODE_LEGACY));
            lte_rrc_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.lte_rrc_widget_intro),Html.FROM_HTML_MODE_LEGACY));
            wcdma_gmm_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.wcdma_gmm_widget_intro),Html.FROM_HTML_MODE_LEGACY));
            wcdma_mm_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.wcdma_mm_widget_intro),Html.FROM_HTML_MODE_LEGACY));
            wcdma_rrc_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.wcdma_rrc_widget_intro),Html.FROM_HTML_MODE_LEGACY));
            lte_phy_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.phy_widget_intro),Html.FROM_HTML_MODE_LEGACY));
            mg_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.mg_widget_intro),Html.FROM_HTML_MODE_LEGACY));
            table_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.table_widget_intro),Html.FROM_HTML_MODE_LEGACY));
        } else {
            lte_emm_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.lte_emm_widget_intro)));
            lte_esm_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.lte_esm_widget_intro)));
            lte_rrc_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.lte_rrc_widget_intro)));
            wcdma_gmm_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.wcdma_gmm_widget_intro)));
            wcdma_mm_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.wcdma_mm_widget_intro)));
            wcdma_rrc_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.wcdma_rrc_widget_intro)));
            lte_phy_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.phy_widget_intro)));
            mg_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.mg_widget_intro)));
            table_wdiget.add(Html.fromHtml(context.getResources().getString(R.string.table_widget_intro)));
        }

        expandableListDetail.put("LteEmmState Widget", lte_emm_wdiget);
        expandableListDetail.put("LteEsmState Widget", lte_esm_wdiget);
        expandableListDetail.put("LteRrcState Widget", lte_rrc_wdiget);
        expandableListDetail.put("LtePhyBW Widget", lte_phy_wdiget);
        expandableListDetail.put("WcdmaGmmState Widget", wcdma_gmm_wdiget);
        expandableListDetail.put("WcdmaMmState Widget", wcdma_mm_wdiget);
        expandableListDetail.put("WcdmaRrcState Widget", wcdma_rrc_wdiget);
        expandableListDetail.put("LteWcdmaTable Widget", table_wdiget);
        expandableListDetail.put("MobilityGraph Widget", mg_wdiget);
        return expandableListDetail;
    }
}
