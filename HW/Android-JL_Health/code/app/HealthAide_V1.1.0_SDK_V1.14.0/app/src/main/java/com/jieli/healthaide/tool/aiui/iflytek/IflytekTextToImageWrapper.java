package com.jieli.healthaide.tool.aiui.iflytek;

import android.content.Context;

import androidx.core.util.Consumer;

import com.iflytek.sparkchain.core.LLM;
import com.iflytek.sparkchain.core.LLMCallbacks;
import com.iflytek.sparkchain.core.LLMConfig;
import com.iflytek.sparkchain.core.LLMError;
import com.iflytek.sparkchain.core.LLMEvent;
import com.iflytek.sparkchain.core.LLMFactory;
import com.iflytek.sparkchain.core.LLMResult;
import com.iflytek.sparkchain.utils.constants.ErrorCode;
import com.jieli.healthaide.tool.aiui.model.OpResult;
import com.jieli.healthaide.tool.aiui.model.StateResult;
import com.jieli.jl_rcsp.util.JL_Log;

/**
 * @ClassName: TextToImageManager
 * @Description: 文生图(科大讯飞)
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/9 9:34
 */
public class IflytekTextToImageWrapper extends BasicWrapper<String, LLMResult> {

    /**
     * 文生图控制器
     */
    private final LLM mLLM;

    public IflytekTextToImageWrapper(Context context) throws RuntimeException {
        super(context);
        LLMConfig config = LLMConfig.builder()
                .maxToken(1024);
        mLLM = LLMFactory.imageGeneration(512, 512, config);
        mLLM.registerLLMCallbacks(new LLMCallbacks() {
            @Override
            public void onLLMResult(LLMResult llmResult, Object userTag) {
                if (!isSameTag(userTag)) return;
                handleLLMResult(llmResult);
            }

            @Override
            public void onLLMEvent(LLMEvent llmEvent, Object userTag) {
                if (!isSameTag(userTag) || null == llmEvent) return;
                int eventId = llmEvent.getEventID();//获取事件ID
                String eventMsg = llmEvent.getEventMsg();//获取事件信息
                String sid = llmEvent.getSid();//本次交互的sid
                JL_Log.d(tag, "onLLMEvent", "EventID : " + eventId + ", " + eventMsg);
            }

            @Override
            public void onLLMError(LLMError llmError, Object userTag) {
                if (!isSameTag(userTag)) return;
                handleLLMError(llmError);
            }
        });
    }

    @Override
    public void destroy() {
        stop();
        mLLM.registerLLMCallbacks(null);
        super.destroy();
    }

    @Override
    public int getType() {
        return FUNCTION_AI_DIAL;
    }

    @Override
    public boolean isRunning() {
        return mStatus == STATUS_WORKING;
    }

    @Override
    public void execute(String input, Consumer<StateResult<LLMResult>> callback) {
        if (isRunning()) { //如果还在交互中，结束这次交互
            stop();
        }
        setCallback(callback);
        int userTag = autoIncUserTag();
        JL_Log.d(tag, "execute", "Input : " + input + ", userTag : " + userTag);
        callbackStart();
        int ret = mLLM.arun(input, userTag);
        if (ret != 0) {
            callbackFinish("execute", ret, "Operation failed. code : " + ret, null);
        }
    }

    @Override
    public void stop() {
        if (!isRunning()) return;
        mLLM.stop();
        callbackFinish("stop", OpResult.ERR_NONE, "User cancels operation.", null);
    }

    @Override
    public void cancel() {
        stop();
    }

    private void handleLLMResult(LLMResult result) {
        if (!isRunning() || null == result) return;
        //解析获取的交互结果，示例展示所有结果获取，开发者可根据自身需要，选择获取。
        byte[] bytes = result.getImage();//一次性返回完整结果，因此不需要获取status去判断结果是否返回完成
        String role = result.getRole();//获取角色信息
        String sid = result.getSid();//本次交互的sid
        int completionTokens = result.getCompletionTokens();//获取回答的Token大小
        int promptTokens = result.getPromptTokens();//包含历史问题的总Tokens大小
        int totalTokens = result.getTotalTokens();//promptTokens和completionTokens的和，也是本次交互计费的Tokens大小

        callbackFinish("handleLLMResult", OpResult.ERR_NONE, "", result);
    }

    private void handleLLMError(LLMError error) {
        if (!isRunning() || null == error) return;
        int errCode = error.getErrCode();//返回错误码
        String errMsg = error.getErrMsg();//获取错误信息
        String sid = error.getSid();//本次交互的sid

        callbackFinish("handleLLMError", errCode, errMsg, null);
    }

    /**
     * 开始对话
     */
    /*public void startChat(String usrInputText, TextToImageCallback callback) throws Exception {
        String appid = BuildConfig.IFLYTEK_APP_ID;
        String apiSecret = BuildConfig.IFLYTEK_API_SECRET;
        String apiKey = BuildConfig.IFLYTEK_API_KEY;
        URL url = new URL("https://spark-api.cn-huabei-1.xf-yun.com/v2.1/tti");
        String authorization;
        String date;
        String host;
        {
            // 时间
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = format.format(new Date());
        }
        {
            // host
            host = url.getHost();
        }
        {
            // authorization
            String preStr = "host: " + url.getHost() + "\n" +
                    "date: " + date + "\n" +
                    "POST " + url.getPath() + " HTTP/1.1";
            // SHA256加密
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
            // Base64加密
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            // 拼接
            authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
            authorization = Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8));
        }

        TextToImageParam textToImageParam = new TextToImageParam(appid, usrInputText);
        HttpClientThirdParty.createSparkTextToImageApi().textToImage(authorization, date, host, textToImageParam).enqueue(new Callback<TextToImageResponse>() {
            @Override
            public void onResponse(@NonNull Call<TextToImageResponse> call, @NonNull retrofit2.Response<TextToImageResponse> responseCall) {
                TextToImageResponse response = responseCall.body();
                Log.d("TextToImageManager", "startChat: " + responseCall.body());
                Log.d("TextToImageManager", "test:code  : " + responseCall.code());
                Log.d("TextToImageManager", "test:message : " + responseCall.message());
                if (response != null) {
                    TextToImageResponse.Payload payload = response.getPayload();
                    if (payload != null) {
                        TextToImageResponse.Choice choice = payload.getChoices();
                        if (choice != null) {
                            List<TextToImageResponse.Text> textList = choice.getText();
                            if (textList != null && !textList.isEmpty()) {
                                TextToImageResponse.Text text = textList.get(0);
                                if (callback != null) {
                                    callback.onChatOutput(text.getContent());
                                }
                                return;
                            }
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onChatError(responseCall.code(), responseCall.message());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TextToImageResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                if (callback != null) {
                    callback.onChatError(ERR_HTTP_EXCEPTION, t.getMessage());
                }
            }
        });
    }*/
}
