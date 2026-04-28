package com.jieli.healthaide.tool.http.model.param;

/**
 * @ClassName: TTIParameter
 * @Description: 文生图参数
 * @Author: ZhangHuanMing
 * @CreateDate: 2023/10/8 10:23
 */
public class TTIParameter {
    private Chat chat = new Chat();

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public static class Chat {
        private String domain = "general";

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }
}
