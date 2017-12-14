package com.example.ankitpatidar.democheck.presentor;

import com.example.ankitpatidar.democheck.interfaces.CallingInterface;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by BBIM1041 on 14/12/17.
 */

public class RetrofitUtil {

    public CallingInterface getRetrofitService(String url){
        Retrofit retrofit1 = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();

        return retrofit1.create(CallingInterface.class);
    }
}
