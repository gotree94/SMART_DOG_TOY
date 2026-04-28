package com.jieli.healthaide.tool.http.model.param;

/**
 * @ClassName: TTIPayload
 * @Description: 文生图附带参数
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/8 10:26
 */
public class TTIPayload {
    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public static class Message {
        private Text[] text;

        public Text[] getText() {
            return text;
        }

        public void setText(Text[] text) {
            this.text = text;
        }
    }

    public static class Text {
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
