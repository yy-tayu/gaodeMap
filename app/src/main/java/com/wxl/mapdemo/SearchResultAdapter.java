package com.wxl.mapdemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：添加类的描述
 *
 * @author Created by wxl
 * @e-mail mmwxl666@163.com
 * @time Created on 2018/5/23
 */
public class SearchResultAdapter extends BaseAdapter {

    private List<PoiItem> data = new ArrayList<>();
    private int selectedPostion = 0;

    public void setData(List<PoiItem> pData) {
        if (pData ==null) {
            data.clear();
        } else {
            this.data = pData;
        }
        notifyDataSetChanged();
    }

    public int getSelectedPostion() {
        return selectedPostion;
    }

    public void setSelectedPostion(int pSelectedPostion) {
        selectedPostion = pSelectedPostion;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PoiItem poiItem = data.get(position);
        ViewHolder h;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poiitem, null);
            h = new ViewHolder(convertView);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }
        h.mTvName.setText(poiItem.toString());
        return convertView;
    }


}

  class ViewHolder {
    public View rootView;
    public TextView mTvName;

    public ViewHolder(View rootView) {
        this.rootView = rootView;
        this.mTvName = rootView.findViewById(R.id.tv_name);
    }

}

