package se.goransson.mqttexample;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

	ArrayList<Fragment> pages;

	public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> pages) {
		super(fm);
		this.pages = pages;
	}

	@Override
	public Fragment getItem(int pos) {
		return pages.get(pos);
	}

	@Override
	public int getCount() {
		return pages.size();
	}

}
