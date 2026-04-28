package com.jieli.healthaide.tool.http.model.response;

import java.util.List;

/**
 * @ClassName: TextToImageResponse
 * @Description: 文生图的回复
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/8 10:07
 */
public class TextToImageResponse {
    private Header header;
    private Payload payload;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public static class Header {
        private int code;
        private String message;
        private String sid;
        private int status;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    public static class Payload {
        private Choice choices;

        public Choice getChoices() {
            return choices;
        }

        public void setChoices(Choice choices) {
            this.choices = choices;
        }
    }

    public static class Choice {
        private int status;
        private int sep;
        private List<Text> text;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getSep() {
            return sep;
        }

        public void setSep(int sep) {
            this.sep = sep;
        }

        public List<Text> getText() {
            return text;
        }

        public void setText(List<Text> text) {
            this.text = text;
        }
    }

    public static class Text {
        private String content;
        private int index;
        private String role;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
