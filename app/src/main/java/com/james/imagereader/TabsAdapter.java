package com.james.imagereader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TabsAdapter extends FragmentStatePagerAdapter {
    private Context mContext;
    private Map<String, Integer> tabTypes;
    private String[] tabTypesArray;
    private Map<String, AssetsFragment> fragmentMap = new HashMap<>();
    private AssetSortType sortType = AssetSortType.NAME_ASC;
    private AssetDisplayMode displayMode = AssetDisplayMode.LIST;

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
        bundle.putInt("sortType", sortType.ordinal());
        bundle.putInt("displayMode", displayMode.ordinal());
        assetsFragment.setArguments(bundle);
        fragmentMap.put(tabTypesArray[i], assetsFragment);
        return assetsFragment;
    }

    public void setTabs(Map<String, Integer> tabTypes) {
        this.tabTypes = tabTypes;
        tabTypesArray = tabTypes.keySet().toArray(new String[0]);
        fragmentMap.clear();
        notifyDataSetChanged();
    }

    public Collection<AssetsFragment> getFragments() {
        return fragmentMap.values();
    }

    public void setSortType(AssetSortType sortType) {
        this.sortType = sortType;
        fragmentMap.clear();
    }

    public void setDisplayMode(AssetDisplayMode displayMode) {
        this.displayMode = displayMode;
        fragmentMap.clear();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
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
