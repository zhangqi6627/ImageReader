package com.james.imagereader;

public class NativeApi {
    static {
        System.loadLibrary("imagereader");
    }

    public native String getPassword();
}
