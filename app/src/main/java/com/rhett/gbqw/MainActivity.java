package com.rhett.gbqw;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AlertDialog;

/**
 * 主活动类，负责初始化应用的主要UI组件和导航
 */
public class MainActivity extends AppCompatActivity {
    // 用于实现双击返回键退出功能
    private long lastBackPressTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2000; // 2秒内双击返回键退出

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置状态栏颜色为应用主题色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(0xFF2266A7);
        }
        
        setContentView(R.layout.activity_main);

        // 初始化底部导航栏
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        
        // 将底部导航栏与导航控制器关联
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public void onBackPressed() {
        // 获取当前Fragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // 如果当前不在首页，则正常返回上一页
        if (navController.getCurrentDestination().getId() != R.id.navigation_home) {
            super.onBackPressed();
            return;
        }

        // 如果在首页，显示退出确认对话框
        new AlertDialog.Builder(this)
                .setTitle("退出确认")
                .setMessage("确定要退出应用吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }
} 