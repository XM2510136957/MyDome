package com.example.xinmujun.xinmuhezi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.xinmujun.xinmuhezi.fragment.util.OkHttpUtil;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private WebView webView;
    private SwipeRefreshLayout swipeLayout;
    private NavigationView navigation_view;
    private ConnectivityManager cm;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0){
                String res = (String) msg.obj;
                String  ress= "<H1>防洪链接以生成!</H1><h3>复制黏贴即可!</h3><H2>"+res+"</H2>";

                webView.loadData(ress, "text/html; charset=UTF-8", null);
                progressDialog.dismiss();

            }else {
                Toast.makeText(MainActivity.this,"解析失败!", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }
    };
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findview();
        init();
        initwebview();

    }

    private void findview() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        webView = findViewById(R.id.main_webview);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        navigation_view = findViewById(R.id.navigation_view);
        navigation_view.setNavigationItemSelectedListener(this);

    }

    private void initwebview() {

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                //重新刷新页面
                webView.loadUrl(webView.getUrl());
            }
        });

        swipeLayout.setColorScheme(R.color.colorAccent,
                R.color.colorPrimary, R.color.colorPrimaryDark,
                R.color.colorAccent);


        // 设置WebView的客户端
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url == null) return false;
                try {
                    //其他自定义的scheme
                    if (url.startsWith("weixin://") || url.startsWith("alipays://") ||
                            url.startsWith("mailto://") || url.startsWith("tel://") || url.startsWith("mqqapi://")
                            || url.startsWith("mqqwpa://")
                            ) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } else if (url.startsWith("http:") || url.startsWith("https:")) {
                        return false;
                    }
                } catch (Exception e) { //防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                    return false;
                }

                //处理http和https开头的url
                view.loadUrl(url);

                return true;

            }

            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {

                return super.shouldOverrideKeyEvent(view, event);
            }
        });


        //设置进度条
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    //隐藏进度条
                    swipeLayout.setRefreshing(false);
                } else {
                    if (!swipeLayout.isRefreshing())
                        swipeLayout.setRefreshing(true);
                }

                super.onProgressChanged(view, newProgress);
            }
        });


        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        //添加javaScript支持
        webView.getSettings().setJavaScriptEnabled(true);
        //触摸焦点起作用
        webView.requestFocus();

        WebSettings webSettings = webView.getSettings();


        // 让WebView能够执行javaScript
        webSettings.setJavaScriptEnabled(true);
        // 让JavaScript可以自动打开windows
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置缓存
        webSettings.setAppCacheEnabled(true);
        // 设置缓存模式,一共有四种模式
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);//开启DOM缓存，关闭的话H5自身的一些操作是无效的
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info != null) {

            if (info.isAvailable()) {
                webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            } else {
                webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);//不使用网络，只加载缓存
            }
        }
//        if (upgrade.cacheControl > cacheControl) {
//            webView.clearCache(true);
//        //删除DOM缓存 VersionUtils.clearCache(mContext.getCacheDir());//删除APP缓存
//            try { getApplicationContext().deleteDatabase("webview.db");//删除数据库缓存
//                getApplicationContext().deleteDatabase("webviewCache.db");
//            } catch (Exception e) {} }

//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 设置缓存路径
//        webSettings.setAppCachePath("");
        // 支持缩放(适配到当前屏幕)
        webSettings.setSupportZoom(true);
        //设置可以访问文件
        webSettings.setAllowFileAccess(true);
        // 将图片调整到合适的大小
        webSettings.setUseWideViewPort(true);
        // 支持内容重新布局,一共有四种方式
        // 默认的是NARROW_COLUMNS
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 设置可以被显示的屏幕控制
        webSettings.setDisplayZoomControls(true);
        // 设置默认字体大小
        webSettings.setDefaultFontSize(13);

        webView.loadUrl("http://www.tob1.cn/");

    }


    private void init() {

        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("主界面");
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.mipmap.menu, R.mipmap.pause) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //打开
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //关闭
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }


    //点击返回键，返回上一个页面，而不是退出程序
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();// 返回前一个页面
            return true;

        } else {
            ExistDialog();
//                    Toast.makeText(MainActivity.this, "退出程序!", Toast.LENGTH_SHORT).show();
//                onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.option_1:
                webView.goBack();
//                Toast.makeText(MainActivity.this, "option_1", Toast.LENGTH_SHORT).show();
                break;
            case R.id.option_2:
                webView.reload();
//                Toast.makeText(MainActivity.this, "option_2", Toast.LENGTH_SHORT).show();
                break;
            case R.id.option_3:

                webView.clearCache(true);
                //删除DOM缓存 VersionUtils.clearCache(mContext.getCacheDir());//删除APP缓存
                try {
                    getApplicationContext().deleteDatabase("webview.db");//删除数据库缓存
                    getApplicationContext().deleteDatabase("webviewCache.db");
                } catch (Exception e) {
                }

                webView.reload();
//                Toast.makeText(MainActivity.this, "option_3", Toast.LENGTH_SHORT).show();
                break;
            case R.id.option_4:
                finish();
                break;
        }
        return true;

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawerLayout.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            case R.id.nav_shop:
                webView.loadUrl("http://www.bx.tob1.cn/");
                break;
            case R.id.nav_zhanzhang:
                webView.loadUrl("http://tob1.cn/admin/");
                break;
            case R.id.nav_fenzhan:
                webView.loadUrl("http://tob1.cn/user/login.php");
                break;
            case R.id.nav_jiaqun:
                joinQQGroup("MVFb0_IgeY89lLxXiqpCwwMbptDzxdND");
                break;
            case R.id.nav_fanghong:
                CustomDialog();
                break;
            case R.id.nav_update:

                try {
                    //第二种方式：可以跳转到添加好友，如果qq号是好友了，直接聊天
                    String  qqUrl = "mqqwpa://im/chat?chat_type=wpa&uin=" + "2879325451" + "&version=1";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(qqUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent); //跳转到制定QQ的聊天界面
                } catch (Exception e) {
                    e.printStackTrace();

                    Toast.makeText(MainActivity.this, "请检查是否安装QQ", Toast.LENGTH_SHORT).show();

                }

                break;

        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {

            // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
            // destory()
            ViewParent parent = webView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(webView);
            }

            webView.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearHistory();
            webView.clearView();
            webView.removeAllViews();
            webView.destroy();

        }
        super.onDestroy();
    }

    //进度框
    public void ProDialog(){

        //1.创建一个ProgressDialog的实例
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("防洪链接生成");//2.设置标题
        progressDialog.setMessage("正在生成中，请稍等....");//3.设置显示内容
        progressDialog.setCancelable(true);//4.设置可否用back键关闭对话框
        progressDialog.show();//5.将ProgessDialog显示出来

    }



    //退出弹窗
    public void ExistDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setIcon(R.mipmap.setting4);
        builder.setTitle("你确定要退出吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //这里添加点击确定后的逻辑
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //这里添加点击确定后的逻辑
                dialog.dismiss();
            }
        });
        builder.create().show();

    }

    //自定义弹窗
    public void CustomDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // 通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_dialog, null);
        //设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);
        //显示
        final AlertDialog alertDialog = builder.show();

        final EditText et_wangye = view.findViewById(R.id.ed_wangye);

        Button btn_cancle = view.findViewById(R.id.btn_cancle);
        Button btn_confirm = view.findViewById(R.id.btn_confirm);


        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new OkHttpUtil().getFhlj(et_wangye.getText().toString(),handler);
                ProDialog();
                alertDialog.dismiss();
            }
        });
    }

    /****************
     *
     * 发起添加群流程。群号：新木业务群(772933415) 的 key 为： MVFb0_IgeY89lLxXiqpCwwMbptDzxdND
     * 调用 joinQQGroup(MVFb0_IgeY89lLxXiqpCwwMbptDzxdND) 即可发起手Q客户端申请加群 新木业务群(772933415)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }
}
