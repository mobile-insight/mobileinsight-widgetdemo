package net.mobileinsight.widgetdemo.adapter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPumpWidget {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> lte_emm_wdiget = new ArrayList<String>();
        lte_emm_wdiget.add("Show state transition for LTE EMM");

        List<String> lte_esm_wdiget = new ArrayList<String>();
        lte_esm_wdiget.add("Show state transition for LTE ESM");

        List<String> lte_phy_wdiget = new ArrayList<String>();
        lte_phy_wdiget.add("Show physical bandwidth and coding scheme in last one second");

        List<String> lte_rrc_wdiget = new ArrayList<String>();
        lte_rrc_wdiget.add("Show state transition for LTE RRC");

        List<String> table_wdiget = new ArrayList<String>();
        table_wdiget.add("Show state transition in NAS and RRC for both LTE and WCDMA");

        List<String> mg_wdiget = new ArrayList<String>();
        mg_wdiget.add("Show mobility information, including handoff comands, mesuarement control information and measurement reports.");

        List<String> wcdma_gmm_wdiget = new ArrayList<String>();
        wcdma_gmm_wdiget.add("Show state transition for WCDMA GMM");

        List<String> wcdma_mm_wdiget = new ArrayList<String>();
        wcdma_mm_wdiget.add("Show state transition for WCDMA MM");

        List<String> wcdma_rrc_wdiget = new ArrayList<String>();
        wcdma_rrc_wdiget.add("Show state transition for WCDMA RRC");

        expandableListDetail.put("LteEmmState Widget", lte_emm_wdiget);
        expandableListDetail.put("LteEsmState Widget", lte_esm_wdiget);
        expandableListDetail.put("LtePhyBW Widget", lte_phy_wdiget);
        expandableListDetail.put("LteRrcState Widget", lte_rrc_wdiget);
        expandableListDetail.put("LteWcdmaTable Widget", table_wdiget);
        expandableListDetail.put("MobilityGraph Widget", mg_wdiget);
        expandableListDetail.put("WcdmaGmmState Widget", wcdma_gmm_wdiget);
        expandableListDetail.put("WcdmaMmState Widget", wcdma_mm_wdiget);
        expandableListDetail.put("WcdmaRrcState Widget", wcdma_rrc_wdiget);
        return expandableListDetail;
    }
}
