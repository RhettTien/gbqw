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
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    private WebView webView;
    private SharedPreferences preferences;
    private static final String PREF_NAME = "WebViewPrefs";
    private static final String URL_KEY = "web_url";
    private static final String JUMP_URL_KEY = "jump_url";
    private static WebView staticWebView; // 静态保存WebView实例

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View root = inflater.inflate(R.layout.fragment_home, container, false);

            if (!isAdded() || getContext() == null) {
                return root;
            }

            preferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

            setupWebView(root);
            setupButtons(root);

            return root;
        } catch (Exception e) {
            e.printStackTrace();
            View root = inflater.inflate(R.layout.fragment_home, container, false);
            if (getContext() != null) {
                Toast.makeText(getContext(), "页面初始化失败，请重试", Toast.LENGTH_SHORT).show();
            }
            return root;
        }
    }

    private void setupWebView(View root) {
        ViewGroup webViewContainer = root.findViewById(R.id.webView);

        if (staticWebView == null) {
            // 首次创建WebView
            webView = new WebView(requireContext());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    String defaultJumpUrl = getString(R.string.default_jump_url);
                    String customJumpUrl = preferences.getString(JUMP_URL_KEY, defaultJumpUrl);

                    if (url.startsWith(customJumpUrl)) {
                        // 创建 Bundle 来传递 URL
                        Bundle bundle = new Bundle();
                        bundle.putString("url", url);

                        // 使用 Navigation 组件导航到查看页面
                        if (getActivity() != null) {
                            androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                            // 先弹出当前页面
                            navController.popBackStack();
                            // 然后导航到查看页面
                            navController.navigate(R.id.navigation_dashboard, bundle);
                            return true;
                        }
                    }
                    // 其他 URL 正常加载
                    view.loadUrl(url);
                    return true;
                }
            });
            webView.setWebChromeClient(new WebChromeClient());

            // 首次加载默认URL
            String defaultUrl = preferences.getString(URL_KEY, getString(R.string.default_web_url));
            webView.loadUrl(defaultUrl);

            staticWebView = webView;
        } else {
            // 使用已存在的WebView
            webView = staticWebView;
            // 从原来的父视图中移除
            if (webView.getParent() != null) {
                ((ViewGroup) webView.getParent()).removeView(webView);
            }
        }

        // 设置WebView的布局参数
        webView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // 将WebView添加到容器中
        webViewContainer.addView(webView);
    }

    private void setupButtons(View root) {
        try {
            // 设置按钮
            ImageButton settingsButton = root.findViewById(R.id.settingsButton);
            if (settingsButton != null) {
                settingsButton.setOnClickListener(v -> showSettingsDialog());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSettingsDialog() {
        String[] options = {"设置网页地址", "设置跳转网址", "清除应用数据", "关于"};

        new AlertDialog.Builder(requireContext())
                .setTitle("设置")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showUrlDialog();
                    } else if (which == 1) {
                        showJumpUrlDialog();
                    } else if (which == 2) {
                        showClearDataConfirmDialog();
                    } else if (which == 3) {
                        showAboutInfoDialog();
                    }
                })
                .show();
    }

    private void showUrlDialog() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setText(preferences.getString(URL_KEY, getString(R.string.default_web_url)));

        new AlertDialog.Builder(requireContext())
                .setTitle("设置主页网址")
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    String url = input.getText().toString();
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    preferences.edit().putString(URL_KEY, url).apply();
                    webView.loadUrl(url);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showJumpUrlDialog() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setText(preferences.getString(JUMP_URL_KEY, getString(R.string.default_jump_url)));

        new AlertDialog.Builder(requireContext())
                .setTitle("设置跳转网址")
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    String url = input.getText().toString();
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }
                    preferences.edit().putString(JUMP_URL_KEY, url).apply();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showClearDataConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("清除应用数据")
                .setMessage("确定要清除所有应用数据吗？这将清除浏览记录、缓存和设置。")
                .setPositiveButton("确定", (dialog, which) -> clearAppData())
                .setNegativeButton("取消", null)
                .show();
    }

    private void showAboutInfoDialog() {
        String message = String.format(
                "<html>" +
                        "<body style='text-align:left;'>" +
                        "国标全文公开系统便携浏览器<br><br>" +
                        "版本：0.1.0<br><br>" +
                        "功能：<br>" +
                        "• 浏览国家标准全文<br>" +
                        "• 支持标准收藏功能<br>" +
                        "• 支持标准查看记录<br><br>" +
                        "我的：<a href='https://github.com/RhettTien'>Github</a><br><br>" +
                        "© 2024 All Rights Reserved<br>" +
                        "Power by Claude<br>" +
                        "</body>" +
                        "</html>");

        androidx.appcompat.app.AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("关于")
                .setMessage(android.text.Html.fromHtml(message))
                .setPositiveButton("确定", null)
                .create();

        dialog.show();

        // 使超链接可点击并设置文本格式
        android.widget.TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
            messageView.setLineSpacing(0, 1.2f);  // 设置行间距
            messageView.setPadding(50, 30, 50, 30);  // 设置内边距
        }
    }

    private void clearAppData() {
        // 保存当前的网址参数
        String currentUrl = preferences.getString(URL_KEY, getString(R.string.default_web_url));
        String currentJumpUrl = preferences.getString(JUMP_URL_KEY, getString(R.string.default_jump_url));

        // 清除所有数据
        preferences.edit().clear().apply();

        // 恢复网址参数
        preferences.edit()
                .putString(URL_KEY, currentUrl)
                .putString(JUMP_URL_KEY, currentJumpUrl)
                .apply();

        if (webView != null) {
            webView.clearCache(true);
            webView.clearHistory();
            webView.clearFormData();
            // 重新加载当前页面
            webView.loadUrl(currentUrl);
        }
        Toast.makeText(requireContext(), "应用数据已清除", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        // 不销毁WebView，只移除它
        if (webView != null && webView.getParent() != null) {
            ((ViewGroup) webView.getParent()).removeView(webView);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 只在Fragment真正销毁时（比如应用退出）才清理WebView
        if (isRemoving() || requireActivity().isFinishing()) {
            if (staticWebView != null) {
                staticWebView.destroy();
                staticWebView = null;
            }
        }
    }
}