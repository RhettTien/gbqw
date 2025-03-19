package com.rhett.gbqw;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rhett.gbqw.db.FavoriteDao;
import com.rhett.gbqw.model.FavoriteItem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 标准查看页面Fragment，负责显示标准内容和收藏功能
 */
public class DashboardFragment extends Fragment {
    private WebView webView;
    private TextView titleTextView;
    private static final String PREF_NAME = "WebViewPrefs"; // SharedPreferences存储键名
    private FavoriteDao favoriteDao;
    private ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private FloatingActionButton favoriteButton;
    private String currentTitle = "";      // 当前查看的标准标题
    private String currentStandardNo = ""; // 当前查看的标准编号

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 创建单线程执行器用于数据库操作
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // 初始化UI组件
        titleTextView = root.findViewById(R.id.titleTextView);
        titleTextView.setText("加载中...");

        webView = root.findViewById(R.id.webView);
        setupWebView();

        // 检查是否有传入的URL参数
        Bundle args = getArguments();
        if (args != null && args.containsKey("url")) {
            String url = args.getString("url");
            webView.loadUrl(url);

            // 保存为最后访问的标准URL
            if (getActivity() != null) {
                SharedPreferences preferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                preferences.edit().putString("last_standard_url", url).apply();
            }
        } else {
            // 从SharedPreferences获取保存的标准URL
            SharedPreferences preferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String standardUrl = preferences.getString("last_standard_url", null);

            if (standardUrl != null) {
                webView.loadUrl(standardUrl);
            }
        }

        setupDatabase();
        setupFavoriteButton(root);

        return root;
    }

    private void setupDatabase() {
        try {
            if (favoriteDao != null) {
                favoriteDao.close();
            }
            favoriteDao = new FavoriteDao(requireContext());
            favoriteDao.open();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "数据库初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

        // 设置User-Agent为Chrome浏览器
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        webView.getSettings().setUserAgentString(userAgent);

        // 启用缩放
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        // 自适应屏幕宽度
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // 设置默认缩放
        settings.setTextZoom(100);

        // 设置 WebChromeClient 来获取网页标题
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                currentTitle = title;
                // 从标题中提取标准号
                extractStandardNo(title);
                updateFavoriteButtonState(view.getUrl());
                if (titleTextView != null && title != null) {
                    titleTextView.setText(title);
                }
            }
        });

        // 设置 WebViewClient 来处理页面加载完成事件
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 如果标题为空或者是URL，尝试从页面内容中获取标题
                if (titleTextView.getText().toString().equals("加载中...") ||
                        titleTextView.getText().toString().contains("http")) {
                    view.evaluateJavascript(
                            "(function() { return document.title; })();",
                            value -> {
                                if (value != null && !value.equals("null") && !value.isEmpty()) {
                                    String title = value.replaceAll("^\"|\"$", "");
                                    if (titleTextView != null) {
                                        titleTextView.post(() -> titleTextView.setText(title));
                                    }
                                }
                            }
                    );
                }
            }
        });
    }

    private void extractStandardNo(String title) {
        if (title == null) return;

        // 匹配标准号的正则表达式
        Pattern pattern = Pattern.compile("GB/?T?\\s*\\d+(\\.\\d+)?-?\\d*");
        Matcher matcher = pattern.matcher(title);

        if (matcher.find()) {
            currentStandardNo = matcher.group().trim();
        } else {
            currentStandardNo = "";
        }
    }

    private void setupFavoriteButton(View root) {
        try {
            favoriteButton = new FloatingActionButton(requireContext());
            favoriteButton.setId(View.generateViewId());
            favoriteButton.setImageResource(android.R.drawable.btn_star);
            favoriteButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2266A7));
            favoriteButton.setOnClickListener(v -> toggleFavorite());

            // 设置按钮大小
            int buttonSize = (int) (48 * getResources().getDisplayMetrics().density); // 标准FAB大小是48dp
            favoriteButton.setCustomSize(buttonSize);

            // 获取根布局（ConstraintLayout）
            androidx.constraintlayout.widget.ConstraintLayout rootView =
                    (androidx.constraintlayout.widget.ConstraintLayout) root;
            rootView.addView(favoriteButton);

            // 使用ConstraintLayout的布局参数
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                            buttonSize,
                            buttonSize
                    );

            // 设置按钮位置约束
            params.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;

            // 设置外边距
            int margin = (int) (16 * getResources().getDisplayMetrics().density); // 16dp margin
            params.setMargins(margin, margin, margin, margin);

            favoriteButton.setLayoutParams(params);

            // 设置内边距使图标居中且大小合适
            int padding = (int) (16 * getResources().getDisplayMetrics().density); // 16dp padding
            favoriteButton.setPadding(padding, padding, padding, padding);

            // 设置阴影
            favoriteButton.setElevation(6 * getResources().getDisplayMetrics().density); // 6dp elevation
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "收藏按钮初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新收藏按钮状态
     * @param url 当前页面的URL
     */
    private void updateFavoriteButtonState(String url) {
        if (url == null || !isAdded()) return;
        executor.execute(() -> {
            try {
                boolean isFavorited = favoriteDao.isFavorited(url);
                mainHandler.post(() -> {
                    if (favoriteButton != null && isAdded()) {
                        favoriteButton.setImageResource(
                                isFavorited ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
                        );
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 切换收藏状态
     */
    private void toggleFavorite() {
        if (!isAdded() || webView == null) return;

        String url = webView.getUrl();
        if (url == null) return;

        executor.execute(() -> {
            try {
                boolean isFavorited = favoriteDao.isFavorited(url);
                if (isFavorited) {
                    // 取消收藏
                    favoriteDao.deleteFavoriteByUrl(url);
                    showToast("已取消收藏");
                } else {
                    // 添加收藏
                    FavoriteItem item = new FavoriteItem(currentTitle, currentStandardNo, url);
                    favoriteDao.addFavorite(item);
                    showToast("已添加收藏");
                }
                updateFavoriteButtonState(url);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("操作失败，请稍后重试");
            }
        });
    }

    private void showToast(String message) {
        mainHandler.post(() -> {
            if (isAdded()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次页面恢复时检查是否有新的URL需要加载
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String standardUrl = preferences.getString("last_standard_url", null);
        if (standardUrl != null && webView != null) {
            webView.loadUrl(standardUrl);
        }
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
        if (favoriteDao != null) {
            favoriteDao.close();
            favoriteDao = null;
        }
        if (favoriteButton != null && favoriteButton.getParent() != null) {
            ((ViewGroup) favoriteButton.getParent()).removeView(favoriteButton);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }
} 