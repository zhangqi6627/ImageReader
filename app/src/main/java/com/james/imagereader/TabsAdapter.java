package com.james.imagereader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TabsAdapter extends FragmentStatePagerAdapter {
    private Map<String, Integer> tabTypes;
    private String[] tabTypesArray;

    public TabsAdapter(FragmentManager fm, Map<String, Integer> tabTypes) {
        super(fm);
        setTabs(tabTypes);
    }

    @Override
    public Fragment getItem(int i) {
        BaseFragment baseFragment = new BaseFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", tabTypesArray[i]);
        baseFragment.setArguments(bundle);
        return baseFragment;
    }

    public void setTabs(Map<String, Integer> tabTypes) {
        this.tabTypes = tabTypes;
        tabTypesArray = tabTypes.keySet().toArray(new String[0]);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tabTypesArray.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String tabType = tabTypesArray[position];
        return tabType + "\n(" + tabTypes.get(tabType) + ")";
    }
}
