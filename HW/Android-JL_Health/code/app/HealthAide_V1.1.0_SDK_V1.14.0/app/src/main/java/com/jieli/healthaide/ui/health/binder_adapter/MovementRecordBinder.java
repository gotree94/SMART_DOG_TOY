package com.jieli.healthaide.ui.health.binder_adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter.base.binder.BaseItemBinder;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jieli.healthaide.R;
import com.jieli.healthaide.data.entity.SportRecord;
import com.jieli.healthaide.ui.health.entity.MovementRecordEntity;
import com.jieli.healthaide.util.CalendarUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName: BloodOxygenBinder
 * @Description: java类作用描述
 * @Author: ZhangHuanMing
 * @CreateDate: 2021/2/19 16:37
 */
public class MovementRecordBinder extends BaseItemBinder<MovementRecordEntity, BaseViewHolder> {
    @Override
    public void convert(@NotNull BaseViewHolder baseViewHolder, MovementRecordEntity movementRecordEntity) {

        String dateString = movementRecordEntity.dateTag;
        String moveTypeString = "";
        switch (movementRecordEntity.movementType) {
            case SportRecord.TYPE_OUTDOOR:
                moveTypeString = getContext().getString(R.string.sport_outdoor_running);
                break;
            case SportRecord.TYPE_INDOOR:
                moveTypeString = getContext().getString(R.string.sport_indoor_running);
                break;
            default:
                moveTypeString = getContext().getString(R.string.sport_other);

        }
        String typeAndDateString;
        if (movementRecordEntity.dateTag != null) {
            typeAndDateString = CalendarUtil.formatString("%s %s", dateString, moveTypeString);
        } else {
            typeAndDateString = getContext().getString(R.string.empty_date);
        }
        baseViewHolder.setText(R.id.tv_health_date, typeAndDateString);
        baseViewHolder.setText(R.id.tv_distance, CalendarUtil.formatString("%.2f",movementRecordEntity.distance));
        baseViewHolder.setVisible(R.id.tv_health_empty, movementRecordEntity.dateTag == null);
        baseViewHolder.setVisible(R.id.tv_distance, movementRecordEntity.dateTag != null);
        baseViewHolder.setVisible(R.id.tv_distance_unit, movementRecordEntity.dateTag != null);
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        return new BaseViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_health_movement_record, viewGroup, false));
    }
}
