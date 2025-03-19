package com.rhett.gbqw;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rhett.gbqw.adapter.FavoriteAdapter;
import com.rhett.gbqw.db.FavoriteDao;
import com.rhett.gbqw.model.FavoriteItem;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 收藏列表页面Fragment，负责显示和管理收藏的标准
 */
public class NotificationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView emptyView;
    private View loadingView;
    private FavoriteAdapter adapter;
    private FavoriteDao favoriteDao;
    private boolean isLoading = false;      // 是否正在加载数据
    private boolean hasMoreData = true;     // 是否还有更多数据
    private int currentPage = 0;            // 当前页码
    private static final int PAGE_SIZE = 20; // 每页加载数量
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        // 初始化UI组件
        recyclerView = root.findViewById(R.id.recyclerView);
        emptyView = root.findViewById(R.id.emptyView);
        loadingView = root.findViewById(R.id.loadingView);

        setupRecyclerView();
        setupDatabase();
        loadData(true); // 首次加载数据

        return root;
    }

    /**
     * 设置RecyclerView及其相关组件
     */
    private void setupRecyclerView() {
        adapter = new FavoriteAdapter();
        adapter.setOnItemClickListener(new FavoriteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FavoriteItem item) {
                // 创建 Bundle 来传递 URL
                Bundle bundle = new Bundle();
                bundle.putString("url", item.getUrl());

                // 使用 Navigation 组件导航到查看页面
                if (getActivity() != null) {
                    androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    // 先弹出当前页面
                    navController.popBackStack();
                    // 然后导航到查看页面
                    navController.navigate(R.id.navigation_dashboard, bundle);
                }
            }

            @Override
            public void onDeleteClick(FavoriteItem item) {
                showDeleteConfirmDialog(item);
            }
        });

        // 设置RecyclerView的布局管理器和适配器
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 添加滚动监听，实现分页加载
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (!isLoading && hasMoreData) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // 当滚动到底部时加载更多数据
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadData(false);
                    }
                }
            }
        });
    }

    /**
     * 初始化数据库连接
     */
    private void setupDatabase() {
        favoriteDao = new FavoriteDao(requireContext());
        favoriteDao.open();
    }

    /**
     * 加载收藏数据
     * @param isRefresh 是否是刷新操作
     */
    private void loadData(boolean isRefresh) {
        if (isLoading) return;
        isLoading = true;

        if (isRefresh) {
            currentPage = 0;
            adapter.clearItems();
            hasMoreData = true;
        }

        loadingView.setVisibility(View.VISIBLE);

        // 在后台线程中加载数据
        executor.execute(() -> {
            List<FavoriteItem> items = favoriteDao.getFavorites(currentPage * PAGE_SIZE, PAGE_SIZE);
            hasMoreData = items.size() == PAGE_SIZE;
            currentPage++;

            // 在主线程中更新UI
            mainHandler.post(() -> {
                loadingView.setVisibility(View.GONE);
                if (items.isEmpty() && currentPage == 1) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.addItems(items);
                }
                isLoading = false;
            });
        });
    }

    /**
     * 显示删除确认对话框
     * @param item 要删除的收藏项
     */
    private void showDeleteConfirmDialog(FavoriteItem item) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("删除确认")
                .setMessage("确定要删除这条收藏吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    deleteFavorite(item);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 删除收藏项
     * @param item 要删除的收藏项
     */
    private void deleteFavorite(FavoriteItem item) {
        executor.execute(() -> {
            favoriteDao.deleteFavorite(item.getId());
            mainHandler.post(() -> {
                loadData(true); // 重新加载数据
                Toast.makeText(requireContext(), "已删除", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        favoriteDao.close(); // 关闭数据库连接
    }
} 