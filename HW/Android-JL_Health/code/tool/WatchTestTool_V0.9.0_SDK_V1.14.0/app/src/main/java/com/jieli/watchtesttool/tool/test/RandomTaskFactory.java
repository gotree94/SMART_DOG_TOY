package com.jieli.watchtesttool.tool.test;

import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.watchtesttool.tool.test.fattask.FatFileTestTask;
import com.jieli.watchtesttool.tool.test.filetask.ContactTestTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 4/25/21
 * @desc :
 */
public class RandomTaskFactory implements ITaskFactory {


    private SDCardBean sdCardBean;

    private int size;

    public RandomTaskFactory(SDCardBean sdCardBean, int size) {
        this.size = size;
        this.sdCardBean = sdCardBean;
    }

    @Override
    public ITestTask create() throws Exception {
        //sd卡设备
        List<SDCardBean> flashs = new ArrayList<>();
        for (SDCardBean sdCardBean : FileBrowseManager.getInstance().getOnlineDev()) {
            if (sdCardBean.getType() == SDCardBean.SD || sdCardBean.getType() == SDCardBean.FLASH_2) {
                flashs.add(0, sdCardBean);//倒序添加
            }
        }

        List<ITaskFactory> list = new ArrayList<>();
        list.add(new FatFileTestTask.Factory(false));
        list.add(new FatFileTestTask.Factory(true));

        if (!flashs.isEmpty()) {
            list.add(new ContactTestTask.Factory(flashs.get(0)));
        }

        TestTaskQueue queue = new TestTaskQueue(size);
        for (int i = 0; i < size; i++) {
            int random = (int) (Math.random() * list.size());
            random = Math.min(random, list.size() - 1);
            ITestTask task = list.get(random).create();
            queue.add(task);
        }
        return queue;
    }
}
