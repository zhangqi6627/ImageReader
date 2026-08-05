package com.james.imagereader;

public enum AssetDisplayMode {
    LIST,
    GRID;

    public static AssetDisplayMode fromPosition(int position) {
        AssetDisplayMode[] values = values();
        if (position < 0 || position >= values.length) {
            return LIST;
        }
        return values[position];
    }

    public AssetDisplayMode toggle() {
        return this == LIST ? GRID : LIST;
    }
}
