package com.rhett.gbqw.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rhett.gbqw.R;
import com.rhett.gbqw.model.FavoriteItem;
import java.util.ArrayList;
import java.util.List;

/**
 * 收藏列表的适配器，负责显示收藏项
 */
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
    private List<FavoriteItem> favorites = new ArrayList<>();
    private OnItemClickListener listener;

    /**
     * 列表项点击事件监听器接口
     */
    public interface OnItemClickListener {
        void onItemClick(FavoriteItem item);
        void onDeleteClick(FavoriteItem item);
    }

    /**
     * 设置点击事件监听器
     * @param listener 监听器实例
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 添加新的收藏项
     * @param newItems 要添加的收藏项列表
     */
    public void addItems(List<FavoriteItem> newItems) {
        int startPosition = favorites.size();
        favorites.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }

    /**
     * 清空所有收藏项
     */
    public void clearItems() {
        favorites.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteItem item = favorites.get(position);
        holder.titleText.setText(item.getTitle());
        holder.standardNoText.setText(item.getStandardNo());
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    /**
     * ViewHolder类，用于缓存列表项的视图
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView standardNoText;
        View deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            standardNoText = itemView.findViewById(R.id.standardNoText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
} 