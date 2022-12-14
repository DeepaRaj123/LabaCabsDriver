package com.laba.partner.data.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.laba.partner.BuildConfig;
import com.laba.partner.MvpApplication;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

//import com.facebook.stetho.okhttp3.StethoInterceptor;

public class APIClient {
    private static Retrofit retrofit = null;

    public static ApiInterface getAPIClient() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit
                    .Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(getHttpClient())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(ApiInterface.class);
    }

    private static OkHttpClient getHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient().newBuilder()
                .cache(new Cache(MvpApplication.getInstance().getCacheDir(), 10 * 1024 * 1024)) // 10 MB
                .connectTimeout(10, TimeUnit.MINUTES)
                .addNetworkInterceptor(new AddHeaderInterceptor())
                .hostnameVerifier((hostname, sslSession) -> true)
//                .addNetworkInterceptor(new StethoInterceptor())
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .addInterceptor(interceptor)
                .build();
    }

    public static Retrofit getFcmRetrofit() {
        OkHttpClient client = getHttpClient();
        return new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    private static class AddHeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request.Builder builder = chain.request().newBuilder();
            builder.addHeader("X-Requested-With", "XMLHttpRequest");
            builder.addHeader("Authorization", "Bearer " + SharedHelper.getKey(MvpApplication.getInstance(),
                    Constants.SharedPref.access_token));


            Log.e("token", SharedHelper.getKey(MvpApplication.getInstance(),
                    Constants.SharedPref.access_token));
            return chain.proceed(builder.build());

        }
    }

}
