package com.wetter.iotproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import uk.co.imallan.jellyrefresh.JellyRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private SensorData mSensorData;
    private RelayState mRelayState;
    private JellyRefreshLayout mRefreshLayout;
    private FrameLayout mFrameLayout;
    public static List<SensorData> sDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
        Bmob.initialize(this, "34f2ac9c0adfcf9d8f5af8682bbc621a");
        initView();
    }

    private void initView() {
        mFrameLayout = (FrameLayout) findViewById(R.id.main_frame);

        mRefreshLayout = (JellyRefreshLayout) findViewById(R.id.main_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        mSensorData = new SensorData();
        mRelayState = new RelayState();

        mAdapter = new RecyclerAdapter(mSensorData,mRelayState);
        mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.i("Log_Main", "onItemClick" + position);
                if (position != 3) {
                    Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                    intent.putExtra("pos", position);
                    startActivity(intent);
                } else {
                    // 点击了继电器
                    mRelayState.setExpectState(!mRelayState.getExpectState());
                    mRelayState.update(MainActivity.this);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        mRefreshLayout.setRefreshListener(
                new JellyRefreshLayout.JellyRefreshListener() {
                    @Override
                    public void onRefresh(JellyRefreshLayout jellyRefreshLayout) {
                        BmobQuery<SensorData> query = new BmobQuery<SensorData>();
                        query.order("-sensorDate");
                        query.setLimit(1000);
                        query.addWhereGreaterThanOrEqualTo("sensorDate", getLimitDay());
                        query.findObjects(MainActivity.this, new FindListener<SensorData>() {
                            @Override
                            public void onSuccess(List<SensorData> list) {
                                if (!list.get(0).getObjectId().equals(mSensorData.getObjectId())) {
                                    Toast.makeText(MainActivity.this, "感测数据已更新", Toast.LENGTH_SHORT).show();
                                    mSensorData = list.get(0);
                                    sDataList = list;
                                    mAdapter.refreshData(mSensorData,mRelayState);
                                } else {
                                    Toast.makeText(MainActivity.this, "已经是最新", Toast.LENGTH_SHORT).show();
                                    mAdapter.notifyDataSetChanged();
                                }
                                mRefreshLayout.finishRefreshing();
                            }

                            @Override
                            public void onError(int i, String s) {
                                mRefreshLayout.finishRefreshing();
                                Log.i("Log_Main", i + " refresh_error: " + s);
                            }
                        });

                        BmobQuery<RelayState> stateBmobQuery = new BmobQuery<>();
                        stateBmobQuery.getObject(MainActivity.this, "hnm5666B", new GetListener<RelayState>() {
                            @Override
                            public void onSuccess(RelayState state) {
                                mRelayState = state;
                                mAdapter.refreshData(mSensorData,mRelayState);
                                Toast.makeText(MainActivity.this, "继电器状态已更新", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                Log.i("Log_Main", i + " load_relay_error: " + s);
                            }
                        });
                    }
                });

        BmobQuery<SensorData> query = new BmobQuery<>();
        query.order("-sensorDate");
        query.setLimit(1000);
        query.addWhereGreaterThanOrEqualTo("sensorDate", getLimitDay());
        query.findObjects(this, new FindListener<SensorData>() {
            @Override
            public void onSuccess(List<SensorData> list) {
                mSensorData = list.get(0);
                mAdapter.refreshData(mSensorData,mRelayState);
                sDataList = list;
                Log.i("Log_Main", "sensorDate_size: "+list.size());
            }

            @Override
            public void onError(int i, String s) {
                Log.i("Log_Main", i + " load_data_error: " + s);
            }

        });

        BmobQuery<RelayState> stateBmobQuery = new BmobQuery<>();
        stateBmobQuery.getObject(this, "hnm5666B", new GetListener<RelayState>() {
            @Override
            public void onSuccess(RelayState state) {
                mRelayState = state;
                mAdapter.refreshData(mSensorData,mRelayState);
                mFrameLayout.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int i, String s) {
                Log.i("Log_Main", i + " load_relay_error: " + s);
            }
        });

    }

    private String getLimitDay() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date(System.currentTimeMillis());
        Date limitDate = new Date(currentDate.getTime() - (long)7 * 24 * 60 * 60 * 1000);
        String tempDate = formatter.format(limitDate) + " 00:00:00";
        // Log.i("Log_Main", tempDate);
        return tempDate;
    }

}
