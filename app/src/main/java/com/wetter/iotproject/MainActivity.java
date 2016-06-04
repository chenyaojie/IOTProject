package com.wetter.iotproject;

import android.animation.Animator;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.LogInListener;
import uk.co.imallan.jellyrefresh.JellyRefreshLayout;

public class MainActivity extends AppCompatActivity {

    // UI相关控件
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private JellyRefreshLayout mRefreshLayout;
    private FrameLayout mFrameLayout;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigation;
    public static CoordinatorLayout mCoordinatorLayout;
    public static List<SensorData> sDataList = new ArrayList<>();

    // Table类定义
    private CurrentData mSensorData;
    private RelayState mRelayState;

    // NFC相关定义
    private NfcAdapter mNfcAdapter = null;
    private PendingIntent mPendingIntent = null;
    private String[][] mTechList = null;
    private IntentFilter[] mIntentFilter = null;
    private String tagUID = "";
    private ImageView headerAvatar;
    private TextView headerNickname;
    private TextView headerEmail;
    private Button headerChange;

    // 用户管理
    public static boolean isIdentify = false;
    private static final String TIME_FORMAT = "yyyy-MM-dd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
        Bmob.initialize(this, "34f2ac9c0adfcf9d8f5af8682bbc621a");

        initView();
        initData();
        initNfc();
    }

    private void initView() {
        mFrameLayout = (FrameLayout) findViewById(R.id.main_frame);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator);
        mRefreshLayout = (JellyRefreshLayout) findViewById(R.id.main_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        mSensorData = new CurrentData();
        mRelayState = new RelayState();

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.my_drawer_layout);
        mNavigation = (NavigationView) findViewById(R.id.nav_view);

        headerAvatar = (ImageView) (mNavigation != null ? mNavigation.getHeaderView(0).findViewById(R.id.header_image) : null);
        headerNickname = (TextView) (mNavigation != null ? mNavigation.getHeaderView(0).findViewById(R.id.tv_header_nickname) : null);
        headerEmail = (TextView) (mNavigation != null ? mNavigation.getHeaderView(0).findViewById(R.id.tv_header_email) : null);
        headerChange = (Button) (mNavigation != null ? mNavigation.getHeaderView(0).findViewById(R.id.btn_header_changeUser) : null);
        headerChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
        // 添加Toolbar
        setSupportActionBar(mToolbar);

        // 添加侧边栏按钮与其监听器
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mToolbar.setTitle("我的账户");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mToolbar.setTitle("我的山云居");
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mAdapter = new RecyclerAdapter(mSensorData, mRelayState,this);
        mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.i("Log_Main", "onItemClick" + position);
                if (position != 3) {
                    Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                    intent.putExtra("pos", position);
                    startActivity(intent);
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
                        refreshSensorData();
                    }
                });

    }

    private void initData() {
        refreshSensorData();
        BmobQuery<SensorData> query = new BmobQuery<>();
        query.order("-sensorDate");
        query.setLimit(1000);
        query.addWhereGreaterThanOrEqualTo("sensorDate", getLimitDay());
        query.findObjects(this, new FindListener<SensorData>() {
            @Override
            public void onSuccess(List<SensorData> list) {
                sDataList = list;
                Log.i("Log_Main", "sensorDate_size: " + list.size());
                mFrameLayout.animate()
                        .alpha(0.0f)
                        .setDuration(1000)
                        .scaleX(2.0f)
                        .scaleY(2.0f)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                mFrameLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });
            }

            @Override
            public void onError(int i, String s) {
                Log.i("Log_Main", i + " load_data_error: " + s);
            }

        });
    }

    private void refreshSensorData() {
        BmobQuery<CurrentData> queryCurrent = new BmobQuery<>();
        queryCurrent.getObject(MainActivity.this, "ngGbQQQY", new GetListener<CurrentData>() {

            @Override
            public void onSuccess(CurrentData currentData) {
                mSensorData = currentData;
                mAdapter.refreshData(mSensorData, mRelayState);
            }

            @Override
            public void onFailure(int i, String s) {
                Log.i("Log_Main", i + " refresh_error: " + s);
            }
        });


        BmobQuery<RelayState> stateBmobQuery = new BmobQuery<>();
        stateBmobQuery.getObject(MainActivity.this, "hnm5666B", new GetListener<RelayState>() {
            @Override
            public void onSuccess(RelayState state) {
                Snackbar.make(mCoordinatorLayout, "感测数据已更新", Snackbar.LENGTH_SHORT).show();
                mRelayState = state;
                mAdapter.refreshData(mSensorData, mRelayState);
                mRefreshLayout.finishRefreshing();
            }

            @Override
            public void onFailure(int i, String s) {
                Log.i("Log_Main", i + " load_relay_error: " + s);
                mRefreshLayout.finishRefreshing();
            }
        });
    }

    // 初始化NFC适配器，意图过滤器
    private void initNfc() {
        NfcCheck();
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter intentFilterTECH = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            intentFilterTECH.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        mIntentFilter = new IntentFilter[]{intentFilterTECH};
        mTechList = new String[][]{new String[]{MifareClassic.class.getName()}};
    }

    private String getLimitDay() {
        SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);
        Date currentDate = new Date(System.currentTimeMillis());
        Date limitDate = new Date(currentDate.getTime() - (long) 7 * 24 * 60 * 60 * 1000);
        // Log.i("Log_Main", tempDate);
        return formatter.format(limitDate) + " 00:00:00";
    }

    private void NfcCheck() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(MainActivity.this, "您的手机不支持NFC", Toast.LENGTH_SHORT).show();
        } else {
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(MainActivity.this, "尚未开启NFC功能", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
            }
        }
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] byteArr = tag.getId();
            try {
                tagUID = getHexString(byteArr);
                Log.i("Log_NFC", "tagUID: " + tagUID);
                identifyUID();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void userChange() {
        //查找UID是否存在
        BmobQuery<MyUser> queryUser = new BmobQuery<>();
        queryUser.addWhereEqualTo("nfcUID", tagUID);
        queryUser.findObjects(MainActivity.this, new FindListener<MyUser>() {
            @Override
            public void onSuccess(List<MyUser> list) {// 用户存在
                if (list.size() != 0) {// 用户切换
                    MyUser user = list.get(0);
                    BmobUser.loginByAccount(MainActivity.this, user.getUsername(), tagUID,
                            new LogInListener<MyUser>() {
                                @Override
                                public void done(MyUser myUser, BmobException e) {
                                    if (myUser != null) {
                                        Snackbar.make(mCoordinatorLayout, "用户"+myUser.getNickName()+"登入成功", Snackbar.LENGTH_SHORT).show();
                                        isIdentify = true;
                                        mAdapter.notifyDataSetChanged();
                                        MyUser currentUser = BmobUser.getCurrentUser(MainActivity.this, MyUser.class);
                                        if (currentUser != null) {
                                            // 允许用户使用应用
                                            if (currentUser.getAvatar() != null) {
                                                String avatarUrl = currentUser.getAvatar().getFileUrl(MainActivity.this) + "";
                                                if (!avatarUrl.isEmpty()) {
                                                    ImageLoader.getInstance().displayImage(avatarUrl, headerAvatar);
                                                }
                                            }
                                            headerNickname.setText(currentUser.getNickName());
                                            headerEmail.setText(currentUser.getUsername());
                                            headerChange.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                }

                            });
                } else {
                    Snackbar.make(mCoordinatorLayout, "未注册的NFC卡片", Snackbar.LENGTH_LONG)
                            .setAction("注册", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                                }
                            }).show();
                }
            }

            @Override
            public void onError(int i, String s) {
                Snackbar.make(mCoordinatorLayout, "未注册的NFC卡片", Snackbar.LENGTH_LONG)
                        .setAction("注册", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                            }
                        }).show();
            }
        });
    }

    // 在获取UID后进行判断
    private void identifyUID() {
        MyUser current = BmobUser.getCurrentUser(this, MyUser.class);
        if (current != null) {// 存在当前用户

            if (isIdentify) {// 已经获得继电器权限

                if (current.getNfcUID().equals(tagUID)) {
                    Snackbar.make(mCoordinatorLayout, "再刷我要报警了！", Snackbar.LENGTH_SHORT).show();
                } else {
                    userChange();
                }
            } else {// 未获得继电器权限
                if (current.getNfcUID().equals(tagUID)) {
                    isIdentify = true;
                    mAdapter.notifyDataSetChanged();
                } else {
                    userChange();
                }

            }
        } else { // 不存在当前用户
            userChange();
        }

    }

    // Convert a byte array to a Hex string
    public static String getHexString(byte[] b) {
        String result = "";
        for (byte aB : b) {
            result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilter, mTechList);
        }
        MyUser currentUser = BmobUser.getCurrentUser(this, MyUser.class);
        if (currentUser != null) {
            // 允许用户使用应用
            if (currentUser.getAvatar() != null) {
                String avatarUrl = currentUser.getAvatar().getFileUrl(this) + "";
                if (!avatarUrl.isEmpty()) {
                    ImageLoader.getInstance().displayImage(avatarUrl, headerAvatar);
                }
            }else{
                headerAvatar.setImageResource(R.drawable.ic_default_image);
            }
            headerNickname.setText(currentUser.getNickName());
            headerEmail.setText(currentUser.getUsername());
            headerChange.setVisibility(View.INVISIBLE);
        } else {
            // 引导用户注册
            headerChange.setText("注册");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
    }
}
