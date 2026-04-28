package com.jieli.watchtesttool.tool.logcat;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 6/4/21
 * @desc :
 */
public class LogcatBuilder {

    private int pid = -1; //进程pid
    private String expr = "";//正则表达式
    private int count;//日志数
    private int size;//日志文件大小 kb
    private String path; //日志输出路径


    public LogcatBuilder expr(String expr) {
        this.expr = expr;
        return this;
    }


    public LogcatBuilder count(int count) {
        this.count = count;
        return this;
    }

    public LogcatBuilder fileSize(int fileSize) {
        this.size = fileSize;
        return this;
    }


    public LogcatBuilder outPath(String path) {
        this.path = path;
        return this;
    }

    public LogcatBuilder pid(int pid) {
        this.pid = pid;
        return this;
    }


    public String toString() {

        StringBuilder sb = new StringBuilder("logcat");

        if (pid > 0) {
            sb.append(" --pid ").append(pid);
        }


        if (count > 0) {
            sb.append(" -n ").append(count);
        }

        if (size > 0) {

            sb.append(" -r ").append(size);
        }

        if (expr != null && expr.length() > 0) {
            sb.append(" -e ").append("\"").append(expr).append("\"");
        }


        if (path == null || path.length() < 1) {
            throw new RuntimeException("输出路径不能为空");
        }

        sb.append(" -f ").append(path);


        return sb.toString();
    }


}
