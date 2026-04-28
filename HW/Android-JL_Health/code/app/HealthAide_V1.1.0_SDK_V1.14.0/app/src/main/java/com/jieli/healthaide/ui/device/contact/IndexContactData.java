package com.jieli.healthaide.ui.device.contact;

import com.chad.library.adapter.base.entity.MultiItemEntity;

class IndexContactData implements MultiItemEntity {

        public int type;
        public Contact contact;
        public String index;
        public int bgRes;
        @Override
        public int getItemType() {
            return type;
        }
    }