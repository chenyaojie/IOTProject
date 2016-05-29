package com.wetter.iotproject;

import cn.bmob.v3.BmobObject;

/**
 * Created by Wetter on 2016/5/24.
 */
public class RelayState extends BmobObject{
    private Integer currentState = 0;
    private Integer expectState = 0;

    public Integer getCurrentState() {
        return currentState;
    }

    public Integer getExpectState() {
        return expectState;
    }

    public void setExpectState(Integer expectState) {
        this.expectState = expectState;
    }
}
