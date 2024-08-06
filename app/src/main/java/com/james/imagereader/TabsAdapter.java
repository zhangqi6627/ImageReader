package com.james.imagereader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.Map;

public class TabsAdapter extends FragmentStatePagerAdapter {
    private Context mContext;
    private Map<String, Integer> tabTypes;
    private String[] tabTypesArray;

    public TabsAdapter(Context context, FragmentManager fm, Map<String, Integer> tabTypes) {
        super(fm);
        mContext = context;
        setTabs(tabTypes);
    }

    @Override
    public Fragment getItem(int i) {
        AssetsFragment assetsFragment = new AssetsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", tabTypesArray[i]);
        assetsFragment.setArguments(bundle);
        return assetsFragment;
    }

    public void setTabs(Map<String, Integer> tabTypes) {
        this.tabTypes = tabTypes;
        tabTypesArray = tabTypes.keySet().toArray(new String[0]);
        for (String tabType : tabTypesArray) {
            int tabTypeCount = ((BaseActivity)mContext).getDBHelper().getTypeCount(tabType);
            tabTypes.put(tabType, tabTypeCount);
        }
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

        Log.e("zq8888", "nums: " + tabTypes.get(tabType));
        return tabType + "\n(" + tabTypes.get(tabType) + ")";
    }
}
