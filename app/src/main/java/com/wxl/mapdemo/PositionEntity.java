package com.wxl.mapdemo;

import java.io.Serializable;

/**
 * ClassName:PositionEntity <br/>
 * Function: 封装的关于位置的实体 搜索 <br/>
 * Date: 2015年4月3日 上午9:50:28 <br/>
 *
 * @author yiyi.qi
 * @version
 * @since JDK 1.6
 * @see
 */
public class PositionEntity implements Serializable {
    public double latitue;

    public double longitude;

    public String address;

    public String city;

    public PositionEntity() {
    }

    public PositionEntity(double latitude, double longtitude, String address, String city) {
        this.latitue = latitude;
        this.longitude = longtitude;
        this.address = address;
    }
}
