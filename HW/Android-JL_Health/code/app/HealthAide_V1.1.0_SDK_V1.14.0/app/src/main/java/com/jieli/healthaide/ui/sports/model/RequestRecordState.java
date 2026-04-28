package com.jieli.healthaide.ui.sports.model;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/11/4
 * @desc :
 */
public class RequestRecordState {
    public static final int REQUEST_RECORD_STATE_NO = 0;
    public static final int REQUEST_RECORD_STATE_START = 1;
    public static final int REQUEST_RECORD_STATE_SUCCESS = 2;
    public static final int REQUEST_RECORD_STATE_FAILED= 3;



    public static final RequestRecordState NO_STATE = new RequestRecordState(REQUEST_RECORD_STATE_NO,0l,   0x00);
    public static final RequestRecordState FAILED_STATE = new RequestRecordState(REQUEST_RECORD_STATE_FAILED,0l,   0x00);


    public int status;
    public long startTime;
    public int type;




    public RequestRecordState(int status, long startTime,int type) {
        this.status = status;
        this.startTime = startTime;
        this.type = type;
    }

    @Override
    public String toString() {
        return "RequestRecordState{" +
                "status=" + status +
                ", startTime=" + startTime +
                '}';
    }
}
