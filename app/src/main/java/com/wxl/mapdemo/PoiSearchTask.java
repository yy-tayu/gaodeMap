package com.wxl.mapdemo;

import android.content.Context;
import android.util.Log;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;


import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:PoiSearchTask <br/>
 * Function: 简单封装了poi搜索的功能，搜索结果配合RecommendAdapter进行使用显示 <br/>
 * Date: 2015年4月7日 上午11:25:07 <br/>
 *
 * @author yiyi.qi
 * @version
 * @since JDK 1.6
 * @see
 */
public class PoiSearchTask {

    private Context mContext;


    private PoiSearch.Query query;// Poi查询条件类


    private LatLonPoint lp;


    private PoiSearch poiSearch;


    private OnZhouBianListener zhouBianListener;

    private OnGuanJianZiListener guanJianZiListener;

    public PoiSearchTask(Context context) {
        this.mContext = context;
    }

    public void setZhouBianListener(OnZhouBianListener zhouBianListener) {
        this.zhouBianListener = zhouBianListener;
    }

    public void setGuanJianZiListener(OnGuanJianZiListener guanJianZiListener) {
        this.guanJianZiListener = guanJianZiListener;
    }

    public void ZhouBianSearch(String keyWord, String city, double latitude, double longitude, int cp, int ps, int scope) {
        int currentPage = cp;
        int pageSize=ps;
        query = new PoiSearch.Query("", keyWord, city);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(pageSize);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        lp = new LatLonPoint(latitude, longitude);
        poiSearch = new PoiSearch(mContext, query);
        poiSearch.setOnPoiSearchListener(new OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int  resultCode) {
                if (zhouBianListener != null) {
                    zhouBianListener.result(poiResult, resultCode);
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
        poiSearch.setBound(new PoiSearch.SearchBound(lp, scope, true));
        // 设置搜索区域为以lp点为圆心，其周围2000米范围
        poiSearch.searchPOIAsyn();// 异步搜索

    }
    public void search(String keyWord,String city) {
        Log.i("MY","search");
        Query query = new PoiSearch.Query(keyWord, "", city);
        query.setPageSize(20);
        query.setPageNum(0);

        PoiSearch poiSearch = new PoiSearch(mContext, query);
        poiSearch.setOnPoiSearchListener(new OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int resultCode) {
                if (guanJianZiListener != null) {
                    guanJianZiListener.result(poiResult, resultCode);
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
        poiSearch.searchPOIAsyn();
    }



//    @Override
//    public void onPoiSearched(PoiResult poiResult, int resultCode) {
//        if (resultCode == AMapException.CODE_AMAP_SUCCESS && poiResult != null) {
//            ArrayList<PoiItem> pois=poiResult.getPois();
//            if(pois==null){
//                return;
//            }
//            List<PositionEntity>entities=new ArrayList<PositionEntity>();
//            for(PoiItem poiItem:pois){
//                PositionEntity entity=new PositionEntity(poiItem.getLatLonPoint().getLatitude(),
//                        poiItem.getLatLonPoint().getLongitude(),poiItem.getTitle()
//                        ,poiItem.getCityName());
//                entities.add(entity);
//            }
//            mRecommandAdapter.setPositionEntities(entities);
//            mRecommandAdapter.notifyDataSetChanged();
//        }else {
//            RxToast.error("搜索失败，没有找到您要查询的内容");
//        }
//
//    }

//    @Override
//    public void onPoiItemSearched(PoiItem poiItem, int i) {
//
//    }
    public interface OnZhouBianListener{
        void result(PoiResult poiResult,int resultCode);
    }
    public interface OnGuanJianZiListener{
        void result(PoiResult poiResult,int resultCode);
    }
}
