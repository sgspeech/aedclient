package com.example.client;

/**
 * Created by speech on 2018-02-19.
 */

public interface Listener {

    public void onStart();
    public void onError(final int e);
    public void onResult(final String s);
}
