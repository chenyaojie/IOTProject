package com.wetter.iotproject;

import cn.bmob.v3.BmobObject;

/**
 * Created by Wetter on 2016/5/27.
 */
public class CurrentData extends BmobObject {
    private Integer temperature = 0;
    private Integer humidity = 0;
    private Integer illuminant = 0;
    private String sensorDate = "2016-01-01 00:00:00";

    public Integer getHumidity() {
        return this.humidity;
    }

    public Integer getIlluminant() {
        return this.illuminant;
    }

    public Integer getTemperature() {
        return this.temperature;
    }

    public String getSensorDate() {
        return this.sensorDate;
    }
}
