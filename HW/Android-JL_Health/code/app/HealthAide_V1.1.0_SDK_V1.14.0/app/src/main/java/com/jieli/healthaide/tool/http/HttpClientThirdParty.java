package com.jieli.healthaide.tool.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jieli.healthaide.tool.http.api.TextToImageApi;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @ClassName: ThirdPartyHttpClient
 * @Description: 第三方Http请求客户端处理
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/8 9:40
 */
public class HttpClientThirdParty {
    public static String SPARK_URL = "https://spark-api.cn-huabei-1.xf-yun.com";

    private static Map<String, Retrofit> sRetrofitMap = new HashMap<>();

    public static TextToImageApi createSparkTextToImageApi() {
        return createRetrofit(SPARK_URL).create(TextToImageApi.class);
    }

    private static HttpLoggingInterceptor getLogger(HttpLoggingInterceptor.Level level) {
        HttpLoggingInterceptor loggerInterceptor = new HttpLoggingInterceptor();
        loggerInterceptor.setLevel(level);
        return loggerInterceptor;
    }

    private static Retrofit createRetrofit(String url) {
        Retrofit retrofit = sRetrofitMap.get(url);
        if (retrofit == null) {
            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .client(new OkHttpClient.Builder()
//                            .addInterceptor(new BaseUrlInterceptor()) //增加动态修改BaseUrl
//                            .addInterceptor(new TokenInterceptor())
                            //缓存使用检测
//                            .addInterceptor(new CacheStatusCheckInterceptor())
                            .addInterceptor(getLogger(HttpLoggingInterceptor.Level.BODY))
//                            .addInterceptor(new RewriteCacheInterceptor())
//                            .addNetworkInterceptor(new ResponseCacheInterceptor())
//                            .cache(cache)
                            .build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            sRetrofitMap.put(url, retrofit);
        }
        return retrofit;
    }
}
