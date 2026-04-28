package com.jieli.healthaide.ui.device.file;

import com.jieli.jl_filebrowse.FileBrowseManager;
import com.jieli.jl_filebrowse.bean.FileStruct;
import com.jieli.jl_filebrowse.bean.Folder;
import com.jieli.jl_filebrowse.bean.SDCardBean;
import com.jieli.jl_rcsp.util.JL_Log;

import java.util.ArrayList;
import java.util.List;

public class Util {
    /**
     * 过滤非音乐文件
     *
     * @param list 需要过滤的数据
     * @return
     */
    public static List<FileStruct> filter(List<FileStruct> list) {
        List<FileStruct> result = new ArrayList<>();
        for (FileStruct struct : list) {
            if (!struct.isFile()) {
                continue;
            }
            String name = struct.getName().toLowerCase();
            if (name.endsWith(".mp3")
                    || name.endsWith(".wav")
                    || name.endsWith(".wave")
                    || name.endsWith(".aac")
                    || name.endsWith(".ogg")
                    || name.endsWith(".amr")
                    || name.endsWith(".wma")
                    || name.endsWith(".flac")
                    || name.endsWith(".ape")
            ) {
                result.add(struct);
            }
        }
        return result;
    }


    public static FileStruct getDownloadDir(List<FileStruct> list) {
        for (FileStruct struct : list) {
            String name = struct.getName();
            if (!struct.isFile() && name.equalsIgnoreCase(DeviceFileViewModel.DOWNLOAD_DIR)
            ) {
                return struct;
            }
        }
        return null;
    }

    public static void cleanDownloadDir(SDCardBean sdCardBean) {
        if (sdCardBean == null) return;

        Folder root = FileBrowseManager.getInstance().getCurrentReadFile(sdCardBean);
        while (root.getParent() != null) {
            root = (Folder) root.getParent();
        }

        FileStruct fileStruct = getDownloadDir(root.getChildFileStructs());
        if (fileStruct == null) {
            //没有download目录的时候，把root目录设置位未读完状态
            JL_Log.w("Util", "cleanDownloadDir", "无download fileStruct");
            root.setLoadFinished(false);
            return;
        }


        Folder downloadDir = (Folder) root.getChildFile(fileStruct.getCluster());
        if (downloadDir == null) {
            JL_Log.w("Util", "cleanDownloadDir", "无download folder");
            return;
        }
        downloadDir.clean();
    }
}
