package net.mobileinsight.widgetdemo.adapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import net.mobileinsight.widgetdemo.PluginIntroFragment;
import net.mobileinsight.widgetdemo.UsageGuideFragment;
import net.mobileinsight.widgetdemo.WidgetIntroFragment;

public class PageAdapter extends FragmentStatePagerAdapter {
	int mNumOfTabs;

	public PageAdapter(FragmentManager fm, int NumOfTabs) {
		super(fm);
		this.mNumOfTabs = NumOfTabs;
	}

	@Override
	public Fragment getItem(int position) {

		switch (position) {
			case 0:
				UsageGuideFragment tab1 = new UsageGuideFragment();
				return tab1;
			case 1:
				WidgetIntroFragment tab2 = new WidgetIntroFragment();
				return tab2;
			case 2:
				PluginIntroFragment tab3 = new PluginIntroFragment();
				return tab3;
			default:
				return null;
		}
	}

	@Override
	public int getCount() {
		return mNumOfTabs;
	}
}