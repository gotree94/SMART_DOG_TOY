package com.jieli.watchtesttool.tool.test.contacts;

import android.content.Context;

import com.jieli.jl_rcsp.impl.RcspOpImpl;
import com.jieli.jl_rcsp.task.SimpleTaskListener;
import com.jieli.jl_rcsp.task.contacts.DeviceContacts;
import com.jieli.jl_rcsp.task.contacts.ReadContactsTask;
import com.jieli.jl_rcsp.task.contacts.UpdateContactsTask;
import com.jieli.jl_rcsp.util.JL_Log;
import com.jieli.watchtesttool.R;
import com.jieli.watchtesttool.tool.test.AbstractTestTask;
import com.jieli.watchtesttool.tool.test.ITaskFactory;
import com.jieli.watchtesttool.tool.test.ITestTask;
import com.jieli.watchtesttool.tool.test.TestError;
import com.jieli.watchtesttool.tool.watch.WatchManager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 2021/10/25
 * @desc :
 */
public class NewContestTestTask extends AbstractTestTask {
    private final static int MAX_COUNT = 10;
    private final Context context;
    private final RcspOpImpl rcspOp;


    private NewContestTestTask(Context context, RcspOpImpl rcspOp) {
        this.context = context;
        this.rcspOp = rcspOp;
    }


    @Override
    public void startTest() {
        onTestLog("----开始联系人传输-------- ");
        if (rcspOp.getDeviceInfo() == null) {
             next(new TestError(TestError.ERR_FAILED,   "联系人测试失败:设备已断开"));
            return;
        }
        ReadContactsTask readContactsTask = new ReadContactsTask(rcspOp, context);
        readContactsTask.setListener(new SimpleTaskListener() {
            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                msg = String.format(Locale.getDefault(), "读取联系人错误:%d, 描述:%s", code, msg);
                next(new TestError(code, msg));
            }

            @Override
            public void onFinish() {
                super.onFinish();
                updateContacts(readContactsTask.getContacts());

            }
        });
        readContactsTask.start();
    }

    @Override
    public void stopTest() {
//        onTestLog("该任务不能取消，请等待任务结束");
    }

    //更新联系人数据
    private void updateContacts(final List<DeviceContacts> list) {
        onTestLog("开始更新联系人");
        int tail = (int) (Math.random() * 20000);
        if (list.size() < 1) {
            String name = "联系人" + tail;
            String mobile = "12345678" + tail;
            DeviceContacts deviceContacts = new DeviceContacts((short) 0, name, mobile);
            list.add(deviceContacts);
        } else if (list.size() >= MAX_COUNT) {
            int index = (int) (Math.random() * list.size());
            list.remove(Math.min(list.size() - 1, index));
        } else {
            int random = (int) (Math.random() * list.size());
            if (random % 2 == 0) {
                String name = "联系人" + tail;
                String mobile = "12345678" + tail;
                DeviceContacts deviceContacts = new DeviceContacts(list.get(0).getFileId(), name, mobile);
                list.add(deviceContacts);
            } else {
                int index = (int) (Math.random() * list.size());
                list.remove(Math.min(index, list.size() - 1));
            }
        }

        UpdateContactsTask updateContactsTask = new UpdateContactsTask(rcspOp, context, list);
        updateContactsTask.setListener(new SimpleTaskListener() {
            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                msg = String.format(Locale.getDefault(), "更新联系人错误:%d, 描述:%s", code, msg);
                next(new TestError(code, msg));
            }

            @Override
            public void onFinish() {
                super.onFinish();
                compareContacts(list);
            }
        });
        updateContactsTask.start();
    }

    //对比更新后的联系人数据
    private void compareContacts(List<DeviceContacts> source) {
        onTestLog("开始比对联系人流程+" + source.size());
        ReadContactsTask readContactsTask = new ReadContactsTask(rcspOp, context);
        readContactsTask.setListener(new SimpleTaskListener() {
            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                msg = String.format(Locale.getDefault(), "比对联系人错误:%d, 描述:%s", code, msg);
                next(new TestError(code, msg));
            }

            @Override
            public void onFinish() {
                super.onFinish();
                List<DeviceContacts> list = readContactsTask.getContacts();

                JL_Log.d(tag, "source---->" + DeviceContacts.toString(source));
                JL_Log.d(tag, "list---->" + DeviceContacts.toString(list));
                byte[] lastData = DeviceContacts.toData(source);
                byte[] nextData = DeviceContacts.toData(list);
                if (Arrays.equals(lastData, nextData)) {
                    next(new TestError(TestError.ERR_SUCCESS, "联系人传输结束"));
                } else {
                    next(new TestError(TestError.ERR_FAILED, "联系人比对失败"));
                }


            }
        });

        readContactsTask.start();
    }

    @Override
    public String getName() {
        return context.getString(R.string.func_contacts);
    }


    public static class Factory implements ITaskFactory {
        private final Context context;


        public Factory(Context context) {
            this.context = context;
        }

        @Override
        public ITestTask create() throws Exception {
            return new NewContestTestTask(context, WatchManager.getInstance());
        }
    }

}
