#pragma bss_seg(".music_device.data.bss")
#pragma data_seg(".music_device.data")
#pragma const_seg(".music_device.text.const")
#pragma code_seg(".music_device.text")
#pragma str_literal_override(".music_device.text.const")

#include "music_device.h"
#include "music_play.h"
#include "play_file.h"
#include "break_point.h"
#include "device.h"
#include "vm_api.h"

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[music_device]"
#include "log.h"

static const char scan_parm_music[] = "-t"
                                      "MP1MP2MP3WAV"
                                      " -sn -r"
                                      ;
/* 复用变量 -->start */
struct vfscan *fsn_music AT(.mode_music_overlay_data);//设备扫描句柄
struct vfscan_reset_info dev_scan_info[MAX_DEVICE] AT(.mode_music_overlay_data);
/* 复用变量 -->end */

void fs_dev_close(void *ppctl)
{
    play_control *tmp_pctl = (play_control *)ppctl;
    vfs_file_close(&tmp_pctl->pfile);
    char *fs_name = vfs_type_name(tmp_pctl->pfs);
    if (fs_name == NULL) {
        return;
    }
    if (0 == memcmp(fs_name, "fat", sizeof("fat"))) {
        vfs_fscan_release(tmp_pctl->pfs, fsn_music);
        fsn_music = NULL;
    }
    vfs_fs_close(&tmp_pctl->pfs);
    device_close(tmp_pctl->dev_index);
}

u32 device_switch(u32 *p_index, s32 dir)
{
    u32 cnt = 0;
    s32 index = *p_index;
    do {
        index += dir;

        if (index >= MAX_DEVICE) {
            index = 0;
        } else if (index < 0) {
            index = MAX_DEVICE - 1;
        }

        if (E_DEV_OFFLINE != device_status(index, 1)) {
            *p_index = index;
            return 0;
        }

        cnt++;
        err_device++;
        if (cnt >= MAX_DEVICE) {
            return E_DEV_ALLOFF;
        }
    } while (dir);

    return E_DEV_OFFLINE;
}

bool music_new_disk_in(play_control *ppctl, u32 index)
{
    log_info("dev in:0x%x", index);
    if (index == ppctl->dev_index) {
        log_info("curr_device is same,0x%x", index);
        return 0;
    }
    /* if (decoder_stop(pctl[0].p_dec_obj, NEED_WAIT, pctl[0].pdp)) { */
    if (NULL != ppctl->pdp) {
        if (true == get_dp(ppctl->p_dec_obj, ppctl->pdp)) {
            save_music_break_point(ppctl, 1);
        }
    }
    decoder_stop(ppctl->p_dec_obj, NEED_WAIT);
    /* log_info("dev in a", index); */
    fs_dev_close(ppctl);
    ppctl->dev_index = index;
    return 1;
}

bool decoder_disk_out(play_control *ppctl, u32 disconnect_index)
{
    if (disconnect_index == ppctl->dev_index) {
        /* decoder_stop(pctl[0].p_dec_obj, NO_WAIT, pctl[0].pdp); */
        if (NULL != ppctl->pdp) {
            if (true == get_dp(ppctl->p_dec_obj, ppctl->pdp)) {
                save_music_break_point(ppctl, 1);
            }
        }
        decoder_stop(ppctl->p_dec_obj, NO_WAIT);
        fs_dev_close(ppctl);
        return 1;
    }
    device_close(disconnect_index);
    return 0;
}

u32 device_mount(play_control *ppctl, void *scan_info)
{
    u32 err;
    void *p_device;
    u32 dev_index = ppctl->dev_index;

    if (E_DEV_OFFLINE == device_status(dev_index, 1)) {
        log_debug("device:%d offline\n", dev_index);
        return E_DEV_OFFLINE;
    }

    p_device = device_open(dev_index);
    if (INNER_FLASH_RO == dev_index) {
        err = vfs_mount(&ppctl->pfs, p_device, (void *)"sydfile");
        if (0 != err) {
            log_debug("vfs_mount err:0x%x\n", err);
            return err;
        }
        char *fs_name = vfs_type_name(ppctl->pfs);
        if (NULL != fs_name) {
            log_info("fs name: %s", fs_name);
            if (0 == memcmp(fs_name, "sydfile", sizeof("sydfile"))) {
                /* err = fs_openbypath(ppctl->pfs, &ppctl->pfile, ppctl->pdir[ppctl->dir_index]); */
                log_info("path : %s\n", ((const char **)ppctl->pdir)[ppctl->dir_index]);
                err = vfs_openbypath(ppctl->pfs, &ppctl->pfile, ((const char **)ppctl->pdir)[ppctl->dir_index]);
                if (0 != err) {
                    log_info("openbypath err :0x%x", err);
                    return err;
                }
                vfs_ioctl(ppctl->pfile, FS_IOCTL_DIR_FILE_TOTAL, (int)&ppctl->ftotal);
                goto __inner_flash_mount_exit;
            }
        }
    }
    if (NULL == p_device) {
        log_debug("device:%d open null!\n", dev_index);
        return E_DEV_NULL;
    }

    log_info("device:%d open succ!\n", dev_index);

    err = vfs_mount(&ppctl->pfs, p_device, "fat");
    if (0 != err) {
        log_debug("vfs_mount err:0x%x\n", err);
        device_close(dev_index);
        return err;
    }
    log_info("vfs_mount succ\n");

    if (NULL == scan_info) {
        goto __fsn_music_err_exit;
    }
    struct vfscan_reset_info *info = (struct vfscan_reset_info *)scan_info;
    fsn_music = vfs_fscan_new(ppctl->pfs, "/", scan_parm_music, 9, NULL, fsn_music, info);
    if (NULL == fsn_music) {
__fsn_music_err_exit:
        log_debug("vfs_fscan err\n");
        vfs_fs_close(&ppctl->pfs);
        device_close(dev_index);
        return E_FSCAN_ERR;
    }
    /* 获取文件总数 */
    ppctl->ftotal = fsn_music->file_number;
    /* log_info("scan file_total %d\n", ppctl->ftotal); */
__inner_flash_mount_exit:
    log_info("scan file_total %d\n", ppctl->ftotal);

    if ((ppctl->ftotal == 0) || (ppctl->ftotal == (-1))) {
        log_debug("no file in dev!\n");
        vfs_fs_close(&ppctl->pfs);
        device_close(dev_index);
        return E_DIR_NULL;
    }

    if (NULL != scan_info) {
        info->active = 1;
    }

    log_info("find_file SUCC\n");
    return 0;
}

