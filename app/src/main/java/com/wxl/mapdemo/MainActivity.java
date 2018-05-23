package com.wxl.mapdemo;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextWatcher, AMap.OnMapLoadedListener, OnLocationGetListener, AMap.OnMyLocationChangeListener, GeocodeSearch.OnGeocodeSearchListener, AMap.OnPOIClickListener {
    private AMap mMap;
    private MapView mapView;

    private LocationTask mLocationTask;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

//    private RecomandAdapter mRecomandAdapter;

    private double lat;
    private double lon;

    private Marker mPositionMark;


    private AutoCompleteTextView et_search;
    private SlidingDrawer slidingdrawer;
    private ListView loc_list;
    private SearchResultAdapter adapter;//抽屉中listview的适配器

    private RecomandAdapter mRecomandAdapter;//搜索的适配器


    private PoiSearchTask poiSearchTask;


    private GeocodeSearch geocoderSearch;// 逆地理编码


    private RegeocodeQuery regeocodeQuery;//逆地理编码查询


    private boolean isfirstinput = true;//是否首次输入搜索


    private String inputSearchKey;//输入搜索内容


    private List<Tip> autoTips;


    List<PoiItem> resultData;//返回地理信息的list


    private  MarkerOptions markerOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        if (mMap == null) {
            mMap = mapView.getMap();
        }
        //调用此方法定位设置
        setUpMap();
//        Location();
        //管理缩放控件
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //设置地图的缩放级别
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16f));

////        // 定位工具初始化
//        mLocationTask = LocationTask.getInstance(getApplicationContext());
//        mLocationTask.setOnLocationGetListener(this);
        et_search.addTextChangedListener(this);
        et_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (autoTips != null && autoTips.size() > position) {
                    Tip tip = autoTips.get(position);
                    searchPoi(tip);
                }
            }
        });
        mMap.setOnMapLoadedListener(this);//地图加载完成回调函数
        mMap.setOnMyLocationChangeListener(this);//地图定位圆点成功的回调
        mMap.setOnPOIClickListener(this);//地图POI点击回调
        mMap.setInfoWindowAdapter(new InfoWinAdapter());
//        poiSearchTask=new PoiSearchTask(this);
//        poiSearchTask.setZhouBianListener(new PoiSearchTask.OnZhouBianListener() {
//            @Override
//            public void result(PoiResult poiResult, int resultCode) {
//
//            }
//        });
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        loc_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiItem poiItem= (PoiItem) adapter.getItem(position);
                //相当于定位 移动回中心点
                CameraUpdate cameraUpate = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(poiItem.getLatLonPoint().getLatitude(),poiItem.getLatLonPoint().getLongitude()), mMap.getCameraPosition().zoom);
                mMap.animateCamera(cameraUpate);
                //再次搜索周边获取数据
                // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
                regeocodeQuery  = new RegeocodeQuery(new LatLonPoint(poiItem.getLatLonPoint().getLatitude(),poiItem.getLatLonPoint().getLongitude()), 1000,GeocodeSearch.AMAP);

                geocoderSearch.getFromLocationAsyn(regeocodeQuery);

            }
        });

    }

    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        //  myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//定位一次，且将视角移动到地图中心点。android:color/transparent
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.color.tr));
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));//圆圈的颜色,设为透明的时候就可以去掉园区区域了
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 自定义精度范围的圆形边框颜色
        mMap.setMyLocationStyle(myLocationStyle);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }

    private void initView() {
        et_search = findViewById(R.id.et_search);
        slidingdrawer = findViewById(R.id.slidingdrawer);//抽屉
        loc_list=findViewById(R.id.loc_list);
        adapter=new SearchResultAdapter();
        loc_list.setAdapter(adapter);
        slidingdrawer.close();

    }
    private void searchPoi(Tip result) {
        inputSearchKey = result.getName();//getAddress(); // + result.getRegeocodeAddress().getCity() + result.getRegeocodeAddress().getDistrict() + result.getRegeocodeAddress().getTownship();
        LatLonPoint searchLatlonPoint = result.getPoint();
        PoiItem firstItem = new PoiItem("tip", searchLatlonPoint, inputSearchKey, result.getAddress());
        firstItem.setCityName(result.getDistrict());
        firstItem.setAdName("");
        resultData.clear();
        adapter.notifyDataSetChanged();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(searchLatlonPoint.getLatitude(), searchLatlonPoint.getLongitude()), 16f));

        hideSoftKey(et_search);
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        regeocodeQuery  = new RegeocodeQuery(new LatLonPoint(searchLatlonPoint.getLatitude(), searchLatlonPoint.getLongitude()), 1000,GeocodeSearch.AMAP);

        geocoderSearch.getFromLocationAsyn(regeocodeQuery);
    }

    private void hideSoftKey(AutoCompleteTextView view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
//    public void Location() {
//
//        try {
//            locationClient = new AMapLocationClient(this);
//            locationOption = new AMapLocationClientOption();
//            // 设置定位模式为低功耗模式
//            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
//            // 设置定位监听
//            locationClient.setLocationListener(this);
//            locationOption.setOnceLocation(true);//设置为单次定位
//            locationClient.setLocationOption(locationOption);// 设置定位参数
//            // 启动定位
//            locationClient.startLocation();
//        } catch (Exception e) {
//            Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_LONG).show();
//        }
//    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        if (newText.length() > 0) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, "");
            Inputtips inputTips = new Inputtips(MainActivity.this, inputquery);
            inputquery.setCityLimit(true);
            inputTips.setInputtipsListener(inputtipsListener);
            inputTips.requestInputtipsAsyn();
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }



    @Override
    public void onMapLoaded() {



//        if (mLocationTask != null) {
//            mLocationTask.startSingleLocate();
//        }
    }


    @Override
    public void onLocationGet(PositionEntity entity) {

    }

    @Override
    public void onRegecodeGet(PositionEntity entity) {

    }

    @Override
    public void onMyLocationChange(Location location) {
        if (location != null) {
            // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
            regeocodeQuery  = new RegeocodeQuery(new LatLonPoint(location.getLatitude(), location.getLongitude()), 1000,GeocodeSearch.AMAP);

            geocoderSearch.getFromLocationAsyn(regeocodeQuery);

        }
    }
//    //获取有数据的数组
//    private PoiItem[] getZhouBianArray(List<PoiItem> pois){
//        PoiItem arr[] = new PoiItem[pois.size()];
//        for (int i = 0; i < pois.size(); i++){
//            arr[i]=pois.get(i);
//        }
//           return arr;
//    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int resultcode) {
        if(resultcode == 1000)
        {
            if(regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
                    && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null)
            {
                if (mPositionMark!=null){
                    mPositionMark.remove();
                }
                markerOptions = new MarkerOptions();


                LatLng latLng=new LatLng(regeocodeResult.getRegeocodeQuery().getPoint().getLatitude()
                        , regeocodeResult.getRegeocodeQuery().getPoint().getLongitude());
                markerOptions.setFlat(true);
                markerOptions.anchor(0.5f, 0.5f);
                markerOptions.position(latLng);
                markerOptions
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),
                                        R.drawable.marker_avatar)));
                mPositionMark = mMap.addMarker(markerOptions);
                mPositionMark.setPosition(latLng);
                mPositionMark.setTitle(regeocodeResult.getRegeocodeAddress().getFormatAddress());//获得目的地名称
                mPositionMark.showInfoWindow();


                //显示定位marker周边数据
                int ps=20;//设置每页显示的数量

                 resultData=regeocodeResult.getRegeocodeAddress().getPois();
                 adapter.setData(resultData);
                 adapter.notifyDataSetChanged();

            }
        }

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onPOIClick(Poi poi) {
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(poi.getCoordinate().latitude, poi.getCoordinate().longitude), 16f));
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        regeocodeQuery  = new RegeocodeQuery(new LatLonPoint(poi.getCoordinate().latitude, poi.getCoordinate().longitude), 1000,GeocodeSearch.AMAP);

        geocoderSearch.getFromLocationAsyn(regeocodeQuery);
    }

    class InfoWinAdapter implements AMap.InfoWindowAdapter, View.OnClickListener {
        private LatLng latLng;
        private String agentName;
        private String snippet;
        private TextView nameTV;
        @Override
        public void onClick(View v) {

        }

        @Override
        public View getInfoWindow(Marker marker) {
            initData(marker);
            View view = initView();
            return view;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        private void initData(Marker marker) {
            this.latLng = marker.getPosition();
            this.snippet = marker.getSnippet();
            this.agentName = marker.getTitle();
        }

        @NonNull
        private View initView() {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_infowindow, null);
            nameTV =  view.findViewById(R.id.name);
            nameTV.setText(agentName);
            return view;
        }
    }

    Inputtips.InputtipsListener inputtipsListener = new Inputtips.InputtipsListener() {
        @Override
        public void onGetInputtips(List<Tip> list, int rCode) {
            if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
                autoTips = list;
                List<String> listString = new ArrayList<String>();
                for (int i = 0; i < list.size(); i++) {
                    listString.add(list.get(i).getName());
                }
                ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                        getApplicationContext(),
                        android.R.layout.simple_list_item_1, listString);
                et_search.setAdapter(aAdapter);
                aAdapter.notifyDataSetChanged();
                if (isfirstinput) {
                    isfirstinput = false;
                    et_search.showDropDown();
                }
            } else {
                Toast.makeText(MainActivity.this, "erroCode " + rCode, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
