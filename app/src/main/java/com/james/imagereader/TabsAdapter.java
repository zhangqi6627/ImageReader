package com.james.imagereader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Set;

public class TabsAdapter extends FragmentStatePagerAdapter {
    private Set<String> tabs;
    private String[] tabsArray;
    public TabsAdapter(FragmentManager fm, Set<String> tabs) {
        super(fm);
        setTabs(tabs);
    }

    @Override
    public Fragment getItem(int i) {
        BaseFragment baseFragment = new BaseFragment();
        Bundle bundle = new Bundle();
        String tabType = (String) getPageTitle(i);
        bundle.putString("type", tabType);
        baseFragment.setArguments(bundle);
        return baseFragment;
    }

    public void setTabs(Set<String> tabs) {
        this.tabs = tabs;
        tabsArray = tabs.toArray(new String[]{});
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabsArray[position];
    }
}
