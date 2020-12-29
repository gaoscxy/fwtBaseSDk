package com.fx.device.listener;

import com.fx.device.entity.BaseEntity;

public interface ApiListener {
    void onResponse(BaseEntity result);

    void onError(String error);
}
