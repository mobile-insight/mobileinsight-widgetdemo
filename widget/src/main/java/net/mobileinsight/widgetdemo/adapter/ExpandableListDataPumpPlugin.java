package net.mobileinsight.widgetdemo.adapter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPumpPlugin {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> phy_online_plugin = new ArrayList<String>();
        phy_online_plugin.add("Demo-PHY-online plugin works with LtePhyBW widget online.");

        List<String> phy_offline_plugin = new ArrayList<String>();
        phy_offline_plugin.add("Demo-PHY-offline plugin works with LtePhyBW widget offline");

        List<String> mg_online_plugin = new ArrayList<String>();
        mg_online_plugin.add("Demo-Mobilgraph-online plugin works with MobilityGraph widget online");

        List<String> mg_offline_plugin = new ArrayList<String>();
        mg_offline_plugin.add("Demo-Mobilgraph-offline plugin works with MobilityGraph widget offline");

        List<String> lterrc_offline_plugin = new ArrayList<String>();
        lterrc_offline_plugin.add("Demo-RRC-4G-offline plugin works with LteRrcState widget offline");

        List<String> wcdmarrc_offline_plugin = new ArrayList<String>();
        wcdmarrc_offline_plugin.add("Demo-RRC-3G-offline plugin works with WcdmaRrcState widget offline");

        List<String> nas_offline_plugin = new ArrayList<String>();
        nas_offline_plugin.add("Demo-NAS-offline plugin works with NAS and UTMS related Widget offline. It could work with LteEsmState, LteEmmState, WcdmaGmmState, WcdmaMmState widgets.");

//      TODO: add plugin introduction for NasAnalysis (work with NAS realted Widget online)
//      TODO: and RrcAnalysis (work with RRC related Widget online)

        expandableListDetail.put("Demo-PHY-online Plugin", phy_online_plugin);
        expandableListDetail.put("Demo-PHY-offline Plugin", phy_offline_plugin);
        expandableListDetail.put("Demo-Mobilgraph-online Plugin", mg_online_plugin);
        expandableListDetail.put("Demo-Mobilgraph-offline Plugin", mg_offline_plugin);
        expandableListDetail.put("Demo-RRC-4G-offline Plugin", lterrc_offline_plugin);
        expandableListDetail.put("Demo-RRC-3G-offline Plugin", wcdmarrc_offline_plugin);
        expandableListDetail.put("Demo-NAS-offline Plugin", nas_offline_plugin);
        return expandableListDetail;
    }
}
