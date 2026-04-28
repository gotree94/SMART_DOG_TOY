#pragma bss_seg(".play_file.data.bss")
#pragma data_seg(".play_file.data")
#pragma const_seg(".play_file.text.const")
#pragma code_seg(".play_file.text")
#pragma str_literal_override(".play_file.text.const")

#include "play_file.h"
#include "music_play.h"
#include "music_device.h"
#include "break_point.h"
#include "device_mge.h"
#include "vfs.h"
#include "vfs_fat.h"
#include "ui_api.h"
#include "app.h"
#include "msg.h"
#include "vm_api.h"
#include "dac_api.h"


#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[play_file]"
#include "log.h"

dec_obj *decoder_by_sclust(void *pvfs, play_control *ppctl)
{
    u32 err, tmp_index;
    void *obj;
    dp_buff *p_dp = ppctl->pdp;

    /* log_info("dec_by_sclust!\n"); */

    /* decoder_stop(ppctl->p_dec_obj, NO_WAIT, ppctl->pdp); */
    get_dp(ppctl->p_dec_obj, ppctl->pdp);
    decoder_stop(ppctl->p_dec_obj, NO_WAIT);


    /* err = vfs_openbyclust(pvfs, &ppctl->pfile, p_dp->sclust, (void *)fsn_music); */
    char *fs_name = vfs_type_name(ppctl->pfs);
    if (0 == memcmp(fs_name, "fat", sizeof("fat"))) {
        err = vfs_select(pvfs, &ppctl->pfile, fsn_music, FSEL_BY_SCLUST, p_dp->sclust);
    } else {
        err = vfs_openbyclust(pvfs, &ppctl->pfile, p_dp->sclust);
    }
    if (0 != err) {
        log_info("openbysclust:0x%x err:0x%x dev:%d\n", p_dp->sclust, err, ppctl->dev_index);
        ppctl->flag &= ~BIT_FDEC_DP;
        clear_dp_buff(ppctl->pdp);
        /* ppctl->findex = 1; */
        return NULL;
    }

    /* 解码前读取文件信息 */
    /* struct vfs_attr attr; */
    /* fs_get_attrs(ppctl->pfile, (void *)&attr); */
    /* tmp_sclust = attr.sclust; */
    if (INNER_FLASH_RO == ppctl->dev_index) {
        vfs_ioctl(ppctl->pfile, FS_IOCTL_FILE_INDEX, (int)&tmp_index);
    } else {
        tmp_index = fsn_music->file_counter;
    }

    log_info("by_index dev:0x%x findex:%d/%d sclust:0x%x\n", ppctl->dev_index, tmp_index, \
             ppctl->ftotal, p_dp->sclust);

    obj = decoder_io(ppctl->pfile, ppctl->dec_type, p_dp, 0);
    if (NULL != obj) {
        dac_fade_in_api();
        /* 启动解码成功清除断点，并将簇号存入断点 */
        ppctl->flag &= ~BIT_FDEC_DP;
        /* clear_dp_buff(ppctl->pdp); */
        clear_dp(ppctl->pdp);
        ppctl->findex = tmp_index;
    }

    return obj;
}

dec_obj *decoder_by_index(void *pvfs, play_control *ppctl)
{
    u32 err, tmp_sclust;
    void *obj;

    /* log_info("dec_by_index!\n"); */

    get_dp(ppctl->p_dec_obj, ppctl->pdp);
    decoder_stop(ppctl->p_dec_obj, NO_WAIT);
    /* err = vfs_openbyindex(pvfs, &ppctl->pfile, ppctl->findex, (void *)fsn_music); */

    char *fs_name = vfs_type_name(ppctl->pfs);
    if (0 == memcmp(fs_name, "fat", sizeof("fat"))) {
        err = vfs_select(pvfs, &ppctl->pfile, fsn_music, FSEL_BY_NUMBER, ppctl->findex);
    } else {
        err = vfs_openbyindex(pvfs, &ppctl->pfile, ppctl->findex);
    }
    if (0 != err) {
        log_info("openbyindex:0x%x err:0x%x dev:%d\n", ppctl->findex, err, ppctl->dev_index);
        return NULL;
    }

    /* 解码前读取文件信息 */
    struct vfs_attr attr;
    vfs_get_attrs(ppctl->pfile, (void *)&attr);
    tmp_sclust = attr.sclust;

    obj = decoder_io(ppctl->pfile, ppctl->dec_type, NULL, 0);
    if (NULL != obj) {
        dac_fade_in_api();
        /* 启动解码成功清除断点，并将文件序号存入断点 */
        dp_buff *p_dp = ppctl->pdp;
        if (NULL != p_dp) {
            clear_dp_buff(ppctl->pdp);
            p_dp->sclust = tmp_sclust;
            save_music_break_point(ppctl, 0);

            /* 解码成功后清除VM中的有效断点 */
            clear_music_break_point(ppctl->dev_index);
        }
    }

    log_info("dev:0x%x findex:%d/%d sclust:0x%x\n", ppctl->dev_index, ppctl->findex, \
             ppctl->ftotal, tmp_sclust);

    return obj;
}

static u32 mbox_check_device(play_control *ppctl, u32 *p_index, s32 sdev)
{
    if (0 != sdev) {
        *p_index = ppctl->dev_index;
    }
    u32 err = device_switch(p_index, sdev);
    if (E_DEV_ALLOFF == err) {
        log_info("no active dev!\n");
        post_msg(1, MSG_NEXT_WORKMODE);
        return err;
    }

    if (*p_index == ppctl->dev_index) {
        log_info("curr_device is same,0x%x", *p_index);
        return E_DEV_NOTCHANGE;
    }
    return err;
}

static u32 mbox_select_logic_device(play_control *ppctl, MBOX_MUSIC_CMD dev_cmd, u32 *p_sel_index)
{
    u32 err;

    switch (dev_cmd) {
    /* 切换指定设备 */
    case DEV_CMD_SEL_NEW_DEV:
        /* log_info("001"); */
        err = mbox_check_device(ppctl, p_sel_index, 0);
        break;

    /* 设备强制切换 */
    case DEV_CMD_PREV:
        /* log_info("002"); */
        err = mbox_check_device(ppctl, p_sel_index, -1);
        break;
    case DEV_CMD_NEXT:
        /* log_info("003"); */
        err = mbox_check_device(ppctl, p_sel_index, 1);
        break;

    /* 设备自动切换,外部不可调用 */
    case DEV_CMD_AUTO_PREV:
        /* log_info("004"); */
        err = mbox_check_device(ppctl, p_sel_index, -1);
        goto __dev_logic_auto_next;
    case DEV_CMD_AUTO_NEXT:
        /* log_info("005"); */
        err = mbox_check_device(ppctl, p_sel_index, 1);
__dev_logic_auto_next:
        if (E_DEV_NOTCHANGE == err) {
            if (err_device > MAX_DEVICE) {
                err = E_DEV_ALLOFF;
            } else {
                err = E_DEV_ONLINE;
            }
        }
        break;
    default:
        return E_DEV_NOCMD;
    }

    return err;
}

static SEL_FILE_MODE mbox_select_logic_file(play_control *ppctl, MBOX_MUSIC_CMD file_cmd, u32 findex, MBOX_MUSIC_CMD *p_dev_cmd)
{
    u16 play_mode = ppctl->play_mode;
    /* log_info("play_mode:%d file_cmd:0x%x\n", play_mode, file_cmd); */

#ifdef FOLDER_PLAY_EN
    if ((play_mode == REPEAT_FOLDER) && (file_cmd == FILE_CMD_PLAY_BY_INDEX)) {
        /* log_info("clear play dec_stop_wait!\n"); */
        play_mode = ppctl->play_mode = REPEAT_ALL;
    }
#endif

    switch (play_mode) {
    case REPEAT_ALL:
__sel_file_repeat_all:
        if (file_cmd == FILE_CMD_PLAY_BY_INDEX) {
            if ((findex >= 1) && (findex <= ppctl->ftotal)) {
                ppctl->findex = findex;
            } else {
                ppctl->findex = 1;
            }
        } else if ((file_cmd == FILE_CMD_PREV) || (file_cmd == FILE_CMD_AUTO_PREV)) {
            ppctl->findex--;
            if (ppctl->findex == 0) {
                ppctl->findex = ppctl->ftotal;
                *p_dev_cmd = DEV_CMD_AUTO_PREV;
                return SEL_LAST_FILE;
            }
        } else {
            ppctl->findex++;
            if (ppctl->findex > ppctl->ftotal) {
                ppctl->findex = 1;
                *p_dev_cmd = DEV_CMD_AUTO_NEXT;
                return SEL_FIRST_FILE;
            }
        }
        break;
#ifdef FOLDER_PLAY_EN
    case REPEAT_FOLDER: {
        if (INNER_FLASH_RO == ppctl->dev_index) {
            break;
        }
        int start_num, end_num;
        vfs_get_folderinfo(ppctl->pfile, fsn_music, &start_num, &end_num);
        log_info("REPEAT_FOLDER : %d-%d\n", start_num, end_num);
        if (file_cmd == FILE_CMD_PREV) {
            ppctl->findex--;
            if (ppctl->findex < start_num) {
                ppctl->findex = end_num;
            }
        } else {
            ppctl->findex++;
            if (ppctl->findex > end_num) {
                ppctl->findex = start_num;
            }
        }
    }
    break;
#endif
#ifdef RANDOM_PLAY_EN
    case REPEAT_RANDOM:
        log_info("REPEAT_RANDOM!\n");
        if (file_cmd == FILE_CMD_PLAY_BY_INDEX) {
            goto __sel_file_repeat_all;
        }
        ppctl->findex = (JL_RAND->R64L) % ppctl->ftotal + 1;
        if (ppctl->findex > ppctl->ftotal) {
            ppctl->findex = 1;
        }
        break;
#endif
    case REPEAT_ONE:
        log_info("REPEAT_ONE!\n");
        if (file_cmd != FILE_CMD_AUTO_NEXT) {
            goto __sel_file_repeat_all;
        }
        break;
    default:
        break;
    }

    return 0;
}

bool music_play_control(play_control *ppctl, MBOX_MUSIC_CMD cmd, u32 index, DEC_STOP_WAIT dec_stop_wait)
{
    u32 err, dev_cmd, file_cmd, dindex, findex;
    u8 err_flag = 0;
    if (cmd < 0x80) {
        /* 设备操作 */
        dev_cmd = cmd;
        dindex = index;
        file_cmd = findex = 0;
    } else {
        /* 文件操作 */
        file_cmd = cmd;
        findex = index;
        dev_cmd = dindex = 0;
    }

    SEL_FILE_MODE file_mode = 0;

    log_info("<----music_play_control\n");
    if (DEV_CMD_NULL == dev_cmd) {
        goto __mpc_file_control_part;
    }

    /*--------------- 逻辑设备选择 ---------------*/
__mpc_device_control_part:
    err = mbox_select_logic_device(ppctl, dev_cmd, &dindex);
    if (E_DEV_ONLINE == err) {
        /* 只有一个设备时,头尾文件边界切换走该分支 */
        goto __mpc_play_file;
    } else if ((E_DEV_NOTCHANGE == err) && (err_flag == 0)) {
        return 0;
    } else if (0 != err) {
        /* 无有效设备处理分支 */
        goto __mpc_no_effetive_dev_deal;
    }

    /* decoder_stop(pctl[0].p_dec_obj, dec_stop_wait, pctl[0].pdp); */
    get_dp(ppctl->p_dec_obj, ppctl->pdp);
    decoder_stop(ppctl->p_dec_obj, dec_stop_wait);
    dac_fade_out_api();
    fs_dev_close(ppctl);
    if (err_device > MAX_DEVICE) {
__mpc_no_effetive_dev_deal:
        log_info("err_device_break!\n");
        ppctl->dev_index = NO_DEVICE;
        SET_UI_MAIN(MENU_IDLE);
        UI_menu(MENU_IDLE, 0);//无设备在线时显示IDLE
        return 0;
    }

    log_info("cur_dev:%d, next_dev:%d\n", ppctl->dev_index, dindex);

    /*--------------- 加载设备断点 ---------------*/
    if ((DEV_CMD_AUTO_PREV == dev_cmd) || (DEV_CMD_AUTO_NEXT == dev_cmd)) {
        /* log_info("no dp in"); */
        clear_dp(ppctl->pdp);
        /* 保存上一设备断点 */
        save_music_break_point(ppctl, 1);
        ppctl->dev_index = dindex;
        ppctl->findex = 1;
    } else {
        /* log_info("dp in"); */
        if (ppctl->dev_index < MAX_DEVICE) {
            /* 保存上一设备断点 */
            save_music_break_point(ppctl, 1);
        }
        clear_dp_buff(ppctl->pdp);
        ppctl->dev_index = dindex;
        /* 加载下一设备断点 */
        load_music_break_point(ppctl, 1);
        /*--------------- 检查设备断点 ---------------*/
        if (0 != check_dp(ppctl->pdp)) {
            ppctl->flag |= BIT_FDEC_DP;
            log_info("check dp succ!\n");
        } else {
            if (false == load_music_break_point(ppctl, 0)) {
                ppctl->findex = 1;
            }
            log_info("check dp fail!\n");
        }
    }

    /*--------------- 挂载物理设备 ---------------*/
__mpc_pick_one:
    /* UI_menu(MENU_WAIT, 0);//物理操作选择设备时显示WAIT */
    err = device_mount(ppctl, &dev_scan_info[ppctl->dev_index]);
    if (err) {
        log_info("pick_dev err:0x%x\n", err);
        err_flag = 1;
        if ((0 == dev_cmd) || (DEV_CMD_SEL_NEW_DEV == dev_cmd)) {
            dev_cmd = DEV_CMD_NEXT;
        }
        goto __mpc_device_control_part;
    }

    if (SEL_LAST_FILE == file_mode) {
        ppctl->findex = ppctl->ftotal;
    } else if (SEL_FIRST_FILE == file_mode) {
        ppctl->findex = 1;
    }

    goto __mpc_play_file;

    /*--------------- 文件选择操作 ---------------*/
__mpc_file_control_part:
    /* decoder_stop(pctl[0].p_dec_obj, dec_stop_wait, pctl[0].pdp); */
    get_dp(ppctl->p_dec_obj, ppctl->pdp);
    decoder_stop(ppctl->p_dec_obj, dec_stop_wait);
    dac_fade_out_api();

    err = device_status(ppctl->dev_index, 0);
    if (0 != err) {
        log_info("file_dev offline!\n");
        fs_dev_close(ppctl);
        goto __mpc_pick_one;
    }

    file_mode = mbox_select_logic_file(ppctl, file_cmd, findex, &dev_cmd);
    if (file_mode) {
        goto __mpc_device_control_part;
    }

    /*--------------- 播放选中文件 ---------------*/
__mpc_play_file:
    if (NULL == ppctl->pfs) {
        vfs_file_close(&ppctl->pfile);
        goto __mpc_pick_one;
    }

    music_vol_update();
    vm_pre_erase();

    SET_UI_MAIN(MENU_MUSIC_MAIN);
    UI_menu(MENU_FILENUM, (int)ppctl);//解码前显示文件号

    ppctl->p_dec_obj = NULL;
    if (BIT_FDEC_DP & ppctl->flag) {
        ppctl->p_dec_obj = decoder_by_sclust(ppctl->pfs, ppctl);
        if (NULL == ppctl->p_dec_obj) {
            ppctl->findex = 1;
        }
    }
    if (NULL == ppctl->p_dec_obj) {
        ppctl->p_dec_obj = decoder_by_index(ppctl->pfs, ppctl);
    }

    if (NULL != ppctl->p_dec_obj) {
        if (ppctl->p_dec_obj->eq != NULL) {
            err = ppctl->p_dec_obj->eq(ppctl->p_dec_obj, dec_eq_mode);
            log_info("cur dec_eq dec_stop_wait:%d \n", err);
        }
        /* log_info("DEOCDE SUCCC\n"); */
        err_device = 0;
        /* 启动解码成功记录活跃设备 */
        vm_write(VM_INDEX_ACTIVE_DEV, &ppctl->dev_index, sizeof(ppctl->dev_index));
    } else {
        /* log_info("DECODE FAIL\n"); */
        if (file_cmd != FILE_CMD_PREV) {
            file_cmd = FILE_CMD_NEXT;
        }
        goto __mpc_file_control_part;
    }
    return 1;
}

