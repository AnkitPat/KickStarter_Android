package com.example.ankitpatidar.democheck.interfaces;

import com.example.ankitpatidar.democheck.model.ListItemHolder;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;


/**
 * Created by ankitpatidar on 28/10/17.
 *
 * Interface to deal with Retrofit callback when data is fetched from server.
 * it used to pass data after fetching to main Activity
 */

public interface CallingInterface {

    @GET("kickstarter")
    Call<List<ListItemHolder>> getListItemModel();
}
