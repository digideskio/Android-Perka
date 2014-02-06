package com.michaelbarany.perka;

import retrofit.RestAdapter;

public class Api {
    private static final String API_URL = "https://getperka.com/api/2";
    private static RestAdapter sRestAdapter;

    public static RestAdapter getRestAdapter() {
        if (null == sRestAdapter) {
            sRestAdapter = new RestAdapter.Builder()
                .setServer(API_URL)
                .build();
        }
        return sRestAdapter;
    }
}
