package cn.edu.zafu.news.ui.search.adaper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.edu.zafu.news.R;
import cn.edu.zafu.news.model.SearchItem;

/**
 * Created by lizhangqu on 2015/5/15.
 */
public class SearchAdapter extends
        RecyclerView.Adapter<SearchAdapter.ViewHolder>{
    //数据集
    private List<SearchItem> list;
    private OnItemClickLitener mOnItemClickLitener;

    public SearchAdapter(List<SearchItem> list) {
        this.list = list;
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        //绑定布局
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_search, parent,false);
        //创建ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        //绑定数据
        viewHolder.title.setText(list.get(position).getTitle());
        viewHolder.time.setText(list.get(position).getTime());
        viewHolder.category.setText(list.get(position).getCategory());
        // 如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null)
        {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(viewHolder.itemView, pos);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView category;
        public TextView time;
        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            title = (TextView) itemLayoutView.findViewById(R.id.title);
            time = (TextView) itemLayoutView.findViewById(R.id.time);
            category = (TextView) itemLayoutView.findViewById(R.id.category);

        }
    }
}
