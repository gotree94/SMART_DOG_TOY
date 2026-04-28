package com.jieli.healthaide.tool.http.api;

import com.jieli.healthaide.tool.http.model.param.TextToImageParam;
import com.jieli.healthaide.tool.http.model.response.TextToImageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @ClassName: TextToImageApi
 * @Description: 文生图 的api(科大讯飞的星火大模型)
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/8 9:47
 */
public interface TextToImageApi {
    @POST("/v2.1/tti")
    Call<TextToImageResponse> textToImage(@Query("authorization") String authorization, @Query("date") String date, @Query("host") String host,@Body TextToImageParam textToImageParam);

}
