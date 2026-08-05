package com.james.imagereader;

import java.io.File;
import java.util.Comparator;

public enum AssetSortType {
    NAME_ASC,
    NAME_DESC,
    IMAGE_COUNT_ASC,
    IMAGE_COUNT_DESC,
    SIZE_ASC,
    SIZE_DESC;

    public enum SortGroup {
        NAME,
        IMAGE_COUNT,
        SIZE
    }

    public static SortGroup groupFromPosition(int position) {
        switch (position) {
            case 1:
                return SortGroup.IMAGE_COUNT;
            case 2:
                return SortGroup.SIZE;
            case 0:
            default:
                return SortGroup.NAME;
        }
    }

    public int getGroupPosition() {
        switch (getGroup()) {
            case IMAGE_COUNT:
                return 1;
            case SIZE:
                return 2;
            case NAME:
            default:
                return 0;
        }
    }

    public static AssetSortType fromPosition(int position) {
        AssetSortType[] values = values();
        if (position < 0 || position >= values.length) {
            return NAME_ASC;
        }
        return values[position];
    }

    public static AssetSortType getDefaultType(SortGroup group) {
        switch (group) {
            case IMAGE_COUNT:
                return IMAGE_COUNT_DESC;
            case SIZE:
                return SIZE_DESC;
            case NAME:
            default:
                return NAME_ASC;
        }
    }

    public SortGroup getGroup() {
        switch (this) {
            case IMAGE_COUNT_ASC:
            case IMAGE_COUNT_DESC:
                return SortGroup.IMAGE_COUNT;
            case SIZE_ASC:
            case SIZE_DESC:
                return SortGroup.SIZE;
            case NAME_ASC:
            case NAME_DESC:
            default:
                return SortGroup.NAME;
        }
    }

    public AssetSortType toggleDirection() {
        switch (this) {
            case NAME_ASC:
                return NAME_DESC;
            case NAME_DESC:
                return NAME_ASC;
            case IMAGE_COUNT_ASC:
                return IMAGE_COUNT_DESC;
            case IMAGE_COUNT_DESC:
                return IMAGE_COUNT_ASC;
            case SIZE_ASC:
                return SIZE_DESC;
            case SIZE_DESC:
            default:
                return SIZE_ASC;
        }
    }

    public boolean isAscending() {
        switch (this) {
            case NAME_ASC:
            case IMAGE_COUNT_ASC:
            case SIZE_ASC:
                return true;
            case NAME_DESC:
            case IMAGE_COUNT_DESC:
            case SIZE_DESC:
            default:
                return false;
        }
    }

    public Comparator<AssetInfo> getComparator() {
        final Comparator<AssetInfo> baseComparator;
        switch (this) {
            case NAME_DESC:
                baseComparator = new Comparator<AssetInfo>() {
                    @Override
                    public int compare(AssetInfo left, AssetInfo right) {
                        return getDisplayFileName(right).compareToIgnoreCase(getDisplayFileName(left));
                    }
                };
                break;
            case IMAGE_COUNT_ASC:
                baseComparator = new Comparator<AssetInfo>() {
                    @Override
                    public int compare(AssetInfo left, AssetInfo right) {
                        return compareWithName(Integer.compare(left.getImageCount(), right.getImageCount()), left, right);
                    }
                };
                break;
            case IMAGE_COUNT_DESC:
                baseComparator = new Comparator<AssetInfo>() {
                    @Override
                    public int compare(AssetInfo left, AssetInfo right) {
                        return compareWithName(Integer.compare(right.getImageCount(), left.getImageCount()), left, right);
                    }
                };
                break;
            case SIZE_ASC:
                baseComparator = new Comparator<AssetInfo>() {
                    @Override
                    public int compare(AssetInfo left, AssetInfo right) {
                        return compareWithName(Long.compare(left.getPackageSize(), right.getPackageSize()), left, right);
                    }
                };
                break;
            case SIZE_DESC:
                baseComparator = new Comparator<AssetInfo>() {
                    @Override
                    public int compare(AssetInfo left, AssetInfo right) {
                        return compareWithName(Long.compare(right.getPackageSize(), left.getPackageSize()), left, right);
                    }
                };
                break;
            case NAME_ASC:
            default:
                baseComparator = new Comparator<AssetInfo>() {
                    @Override
                    public int compare(AssetInfo left, AssetInfo right) {
                        return getDisplayFileName(left).compareToIgnoreCase(getDisplayFileName(right));
                    }
                };
                break;
        }

        return new Comparator<AssetInfo>() {
            @Override
            public int compare(AssetInfo left, AssetInfo right) {
                int recentResult = compareRecentFirst(left, right);
                if (recentResult != 0) {
                    return recentResult;
                }
                return baseComparator.compare(left, right);
            }
        };
    }

    private static int compareRecentFirst(AssetInfo left, AssetInfo right) {
        long leftTime = left.getLastReadTime();
        long rightTime = right.getLastReadTime();
        if (leftTime == rightTime) {
            return 0;
        }
        return leftTime > rightTime ? -1 : 1;
    }

    private static int compareWithName(int result, AssetInfo left, AssetInfo right) {
        if (result != 0) {
            return result;
        }
        return getDisplayFileName(left).compareToIgnoreCase(getDisplayFileName(right));
    }

    private static String getDisplayFileName(AssetInfo assetInfo) {
        String displayName = assetInfo.getDisplayName();
        if (displayName == null) {
            return "";
        }
        return new File(displayName).getName().replace(".apk", "");
    }
}
