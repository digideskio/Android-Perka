package com.michaelbarany.perka;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface ApplyService {
    @POST("/apply")
    void index(@Body ApplicationForm form, Callback<String> callback);
}
