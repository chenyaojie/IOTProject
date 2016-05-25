package com.wetter.iotproject;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private SensorData mData;
    private RelayState mState;
    private final int ITEM_NUMBER = 4;
    private final int IS_NORMAL = 0;
    private final int IS_DELAY = 1;
    private OnItemClickListener mOnItemClickListener;

    public RecyclerAdapter(SensorData sensorData, RelayState relayState) {
        this.mData = sensorData;
        this.mState = relayState;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 3) {
            return IS_DELAY;
        } else {
            return IS_NORMAL;
        }
    }

    // 创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == IS_NORMAL) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_recycler_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_recycler_delay, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    // 将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Log.i("RecA", "onBindViewHolder");
        switch (position) {
            case 0:
                holder.mImageView.setImageResource(R.drawable.ic_temperature_128px);
                holder.mTextView.setText(mData.getTemperature() + "℃");
                holder.mTextDate.setVisibility(View.VISIBLE);
                displayTime(holder.mTextDate, mData.getSensorDate());
                break;
            case 1:
                holder.mImageView.setImageResource(R.drawable.ic_raindrap_128px);
                holder.mTextView.setText(mData.getHumidity() + "%");
                break;
            case 2:
                holder.mImageView.setImageResource(R.drawable.ic_lighting_128px);
                holder.mTextView.setText(mData.getIlluminant() + "Lux");
                break;
            case 3:
                holder.mImageView.setImageResource(R.drawable.ic_delay_128px);
                holder.mTextView.setText(mState.getCurrentState() + "");
                break;
        }

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null) {
            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);
                }
            });

            holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }

    }

    // 获取数据的数量
    @Override
    public int getItemCount() {
        return ITEM_NUMBER;
    }

    // 自定义的ViewHolder，持有每个Item的所有界面元素
    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private TextView mTextView, mTextDate;
        private CardView mCardView;
        public ViewHolder(View itemView, int viewType) {
            super(itemView);

            if (viewType == IS_NORMAL) {
                mCardView = (CardView) itemView.findViewById(R.id.card_view);
                mImageView = (ImageView) itemView.findViewById(R.id.card_image);
                mTextView = (TextView) itemView.findViewById(R.id.card_text);
                mTextDate = (TextView) itemView.findViewById(R.id.card_date);
            } else {
                mCardView = (CardView) itemView.findViewById(R.id.card_view);
                mImageView = (ImageView) itemView.findViewById(R.id.card_delay_image);
                mTextView = (TextView) itemView.findViewById(R.id.card_delay_text);
            }

        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void refreshData(SensorData data,RelayState state) {
        this.mData = data;
        this.mState = state;
        notifyDataSetChanged();
    }

    // 计算并显示时间差
    private void displayTime(TextView tv, String sensorDate) {
        Date dateC, dateS = null;
        String str = "最近更新时间：";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateC = new Date(System.currentTimeMillis());
        try {
            dateS = formatter.parse(sensorDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = dateC.getTime() - dateS.getTime();
        long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        long diffHours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long diffDays = TimeUnit.MILLISECONDS.toDays(diff);
        if (diffDays != 0) {
            str += diffDays + "天";
        } else if (diffHours != 0) {
            str += diffHours + "小时";
        } else if (diffMinutes != 0) {
            str += diffMinutes + "分钟";
        } else {
            str += diffSeconds + "秒";
        }
        str += "前";
        tv.setText(str);
    }
}
