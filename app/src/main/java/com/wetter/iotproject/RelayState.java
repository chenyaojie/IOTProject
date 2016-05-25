package com.wetter.iotproject;

import cn.bmob.v3.BmobObject;

/**
 * Created by Wetter on 2016/5/24.
 */
public class RelayState extends BmobObject{
    private Boolean currentState = false;
    private Boolean expectState = false;

    public Boolean getCurrentState() {
        return currentState;
    }

    public Boolean getExpectState() {
        return expectState;
    }

//    public void setCurrentState(Boolean currentState) {
//        this.currentState = currentState;
//    }

    public void setExpectState(Boolean expectState) {
        this.expectState = expectState;
    }
}
