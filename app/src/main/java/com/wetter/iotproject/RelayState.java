package com.wetter.iotproject;

import cn.bmob.v3.BmobObject;

/**
 * Created by Wetter on 2016/5/24.
 */
public class RelayState extends BmobObject {
    private Integer currentState = 0;
    private Integer expectState = 0;
    private Integer currentState_1 = 0;
    private Integer expectState_1 = 0;
    private Integer currentState_2 = 0;
    private Integer expectState_2 = 0;
    private Integer currentState_3 = 0;
    private Integer expectState_3 = 0;

    public Integer getCurrentState() {
        return currentState;
    }

    public Integer getExpectState() {
        return expectState;
    }

    public void setExpectState(Integer expectState) {
        this.expectState = expectState;
    }

    public Integer getCurrentState_1() {
        return currentState_1;
    }

    public void setCurrentState_1(Integer currentState_1) {
        this.currentState_1 = currentState_1;
    }

    public Integer getCurrentState_2() {
        return currentState_2;
    }

    public void setCurrentState_2(Integer currentState_2) {
        this.currentState_2 = currentState_2;
    }

    public Integer getCurrentState_3() {
        return currentState_3;
    }

    public void setCurrentState_3(Integer currentState_3) {
        this.currentState_3 = currentState_3;
    }

    public Integer getExpectState_1() {
        return expectState_1;
    }

    public Integer getExpectState_2() {
        return expectState_2;
    }

    public Integer getExpectState_3() {
        return expectState_3;
    }

    public void setExpectState_1(Integer expectState_1) {
        this.expectState_1 = expectState_1;
    }

    public void setExpectState_2(Integer expectState_2) {
        this.expectState_2 = expectState_2;
    }

    public void setExpectState_3(Integer expectState_3) {
        this.expectState_3 = expectState_3;
    }

}
