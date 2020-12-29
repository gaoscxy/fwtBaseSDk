package com.fx.device.api;

import android.content.Context;
import android.util.Log;

import com.fx.device.GlobalConstant;
import com.fx.device.utils.NetWorkUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    private final String TAG = "HTTP-REQUEST";

    public Retrofit retrofit;

    public ApiServer apiServer;
    private Context mContext;

    private static Api mInstance;

    private Api(Context context) {
        this.mContext = context;
        createApiService();
    }

    public static Api getInstance(Context context) {
        if (mInstance == null) {
            synchronized (Api.class) {
                if (mInstance == null) {
                    mInstance = new Api(context);
                }
            }
        }
        return mInstance;
    }

    private void createApiService() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)
                .addNetworkInterceptor(new HttpCacheInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GlobalConstant.URLContact.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        apiServer = retrofit.create(ApiServer.class);
    }

    private static Interceptor headerInterceptor = chain -> {
        Request build = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .build();
        return chain.proceed(build);
    };

    class HttpCacheInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!NetWorkUtil.isNetConnected(mContext.getApplicationContext())) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                Log.e(TAG, "no network");
            }

            Response originalResponse = chain.proceed(request);
            if (originalResponse.body() != null) {
                Log.e(TAG, originalResponse.request().toString());
            }
//            MediaType contentType = null;
//            String bodyString = null;
//            ResponseBody body = originalResponse.body();
//            if (originalResponse.body() != null) {
//                contentType = originalResponse.body().contentType();
//                bodyString = originalResponse.body().string();
//                body = ResponseBody.create(contentType, bodyString);
//                Log.i("HTTP-LOG-FAXUAN-REQUEST",originalResponse.request().toString());
//                Log.i("HTTP-LOG-FAXUAN",bodyString);
//            }
            if (NetWorkUtil.isNetConnected(mContext.getApplicationContext())) {
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma")
                        .build();
            } else {
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
                        .build();
            }
        }
    }
}
