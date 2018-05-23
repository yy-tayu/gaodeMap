package com.wxl.mapdemo;

import android.content.Context;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lenovo on 2018/4/16.
 */

public class InputTipTask implements Inputtips.InputtipsListener {
    private static InputTipTask mInputTipTask;

    private Inputtips mInputTips;

    private RecomandAdapter mAdapter;

    public static InputTipTask getInstance(RecomandAdapter adapter) {
        if (mInputTipTask == null) {
            mInputTipTask = new InputTipTask();
        }
        //单例情况，多次进入DestinationActivity传进来的RecomandAdapter对象会不是同一个
        mInputTipTask.setRecommandAdapter(adapter);
        return mInputTipTask;
    }

    public void setRecommandAdapter(RecomandAdapter adapter) {
        mAdapter = adapter;
    }

    private InputTipTask() {


    }

    public void searchTips(Context context, String keyWord, String city) {

        InputtipsQuery query = new InputtipsQuery(keyWord, city);
        query.setCityLimit(true);//将获取到的结果进行城市限制筛选
        mInputTips = new Inputtips(context, query);
        mInputTips.setInputtipsListener(this);
        mInputTips.requestInputtipsAsyn();


    }

    @Override
    public void onGetInputtips(List<Tip> tips, int resultCode) {

        if (resultCode == AMapException.CODE_AMAP_SUCCESS && tips != null) {
            ArrayList<PositionEntity> positions = new ArrayList<PositionEntity>();
            for (Tip tip : tips) {
                if (tip.getPoint() != null) {

                    positions.add(new PositionEntity(tip.getPoint().getLatitude(), tip.getPoint().getLongitude(), tip.getName(), tip.getAdcode()));
                } else {
                    positions.add(new PositionEntity(31.209139, 121.611367, tip.getName(), tip.getAdcode()));
                }

            }
            mAdapter.setPositionEntities(positions);
            mAdapter.notifyDataSetChanged();
        }else {
            mAdapter.setPositionEntities( Arrays.asList(new PositionEntity[] {
                    new PositionEntity(39.908722, 116.397496, "天安门","010"),
                    new PositionEntity(39.91141, 116.411306, "王府井","010"),
                    new PositionEntity(39.908342, 116.375121, "西单","010"),
                    new PositionEntity(39.990949, 116.481090, "方恒国际中心","010"),
                    new PositionEntity(39.914529, 116.316648, "玉渊潭公园","010"),
                    new PositionEntity(39.999093, 116.273945, "颐和园","010"),
                    new PositionEntity(39.999022, 116.324698, "清华大学","010"),
                    new PositionEntity(39.982940, 116.319802, "中关村","010"),
                    new PositionEntity(39.933708, 116.454185, "三里屯","010"),
                    new PositionEntity(39.941627, 116.435584, "东直门","010") }));
            mAdapter.notifyDataSetChanged();
        }


    }

}
