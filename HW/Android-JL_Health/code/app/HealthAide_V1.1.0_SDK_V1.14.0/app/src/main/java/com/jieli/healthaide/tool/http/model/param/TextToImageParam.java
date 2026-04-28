package com.jieli.healthaide.tool.http.model.param;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: TextToImageParam
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/10 11:11
 */
public class TextToImageParam {
    private Header header;
    private Payload payload;
    private Parameter parameter;

    public TextToImageParam(String appid, String content) {
        this.header = new Header();
        this.header.appid = appid;
        this.parameter = new Parameter();
        this.payload = new Payload();
        Text text = new Text();
        text.role = "user";
        text.content = content;
        Message message = new Message();
        List<Text> textArrayList = new ArrayList<>();
        textArrayList.add(text);
        message.setText(textArrayList);
        this.payload.message = message;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    class Header {
        @SerializedName("app_id")
        private String appid;

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }
    }

    class Parameter {
        private Chat chat = new Chat();

        public Chat getChat() {
            return chat;
        }

        public void setChat(Chat chat) {
            this.chat = chat;
        }
    }

    class Chat {
        private String domain = "general";

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }

    class Payload {
        private Message message;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }

    class Message {
        private List<Text> text;

        public List<Text> getText() {
            return text;
        }

        public void setText(List<Text> text) {
            this.text = text;
        }
    }

    class Text {
        private String role;
        private String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
