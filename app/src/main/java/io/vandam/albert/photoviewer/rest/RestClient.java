package io.vandam.albert.photoviewer.rest;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class RestClient {
    private static RestClient instance;
    private static AsyncHttpClient client;

    public static RestClient getInstance() {
        if (instance == null) {
            instance = new RestClient();
            client = new AsyncHttpClient();
        }

        return instance;
    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }
}