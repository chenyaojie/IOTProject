package com.wetter.iotproject;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rey.material.widget.ProgressView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private CurrentData mData;
    private RelayState mState;
    private final int ITEM_NUMBER = 4;
    private final int IS_NORMAL = 0;
    private final int IS_DELAY = 1;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    public RecyclerAdapter(CurrentData sensorData, RelayState relayState,Context context) {
        this.mData = sensorData;
        this.mState = relayState;
        this.mContext = context;
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
                if (mData.getTemperature() <= 10) {
                    holder.mImageView.setImageResource(R.drawable.ic_temperature_1);
                } else if (mData.getTemperature() <= 20) {
                    holder.mImageView.setImageResource(R.drawable.ic_temperature_2);
                } else if (mData.getTemperature() <= 30) {
                    holder.mImageView.setImageResource(R.drawable.ic_temperature_3);
                } else {
                    holder.mImageView.setImageResource(R.drawable.ic_temperature_4);
                }
                holder.mTextView.setText(mData.getTemperature() + "℃");
                holder.mTextDate.setVisibility(View.VISIBLE);
                displayTime(holder.mTextDate, mData.getSensorDate());
                break;
            case 1:
                if (mData.getHumidity() <= 25) {
                    holder.mImageView.setImageResource(R.drawable.ic_raindrops_1);
                } else if (mData.getHumidity() <= 50) {
                    holder.mImageView.setImageResource(R.drawable.ic_raindrops_2);
                }else if (mData.getHumidity() <= 75) {
                    holder.mImageView.setImageResource(R.drawable.ic_raindrops_3);
                }else{
                    holder.mImageView.setImageResource(R.drawable.ic_raindrops_4);
                }
                holder.mTextView.setText(mData.getHumidity() + "%");
                break;
            case 2:
                if (mData.getIlluminant() <= 200) {
                    holder.mImageView.setImageResource(R.drawable.ic_sunski_1);
                } else if (mData.getIlluminant() <= 500) {
                    holder.mImageView.setImageResource(R.drawable.ic_sunski_2);
                }else if (mData.getIlluminant() <= 800) {
                    holder.mImageView.setImageResource(R.drawable.ic_sunski_3);
                }else{
                    holder.mImageView.setImageResource(R.drawable.ic_sunski_4);
                }

                holder.mTextView.setText(mData.getIlluminant() + "Lux");
                break;
            case 3:
                if (mState.getCurrentState()== 0){
                    holder.mImageView.setImageResource(R.drawable.ic_power_0);
                } else if (mState.getCurrentState()<= 25) {
                    holder.mImageView.setImageResource(R.drawable.ic_power_1);
                } else if (mState.getCurrentState() <= 50) {
                    holder.mImageView.setImageResource(R.drawable.ic_power_2);
                }else if (mState.getCurrentState() <= 75) {
                    holder.mImageView.setImageResource(R.drawable.ic_power_3);
                }else{
                    holder.mImageView.setImageResource(R.drawable.ic_power_4);
                }
                holder.mSeekBar.setProgress(mState.getCurrentState());
                holder.mTextView.setText(mState.getCurrentState()+"%");
                break;
        }

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null) {
            if (position != 3) {
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
            } else {
                holder.mSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
                    @Override
                    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(final DiscreteSeekBar seekBar) {
                        final int nowProgress = seekBar.getProgress();
                        holder.mProgressView.setVisibility(View.VISIBLE);
                        holder.mSeekBar.setVisibility(View.GONE);
                        mState.setExpectState(nowProgress);
                        mState.update(mContext);
                        long nowTime = System.currentTimeMillis();
                        new RelayAsyncTask(mContext){
                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                super.onPostExecute(aBoolean);
                                holder.mProgressView.setVisibility(View.GONE);
                                holder.mSeekBar.setVisibility(View.VISIBLE);
                                if (aBoolean) {
                                    Snackbar.make(MainActivity.mCoordinatorLayout,
                                            "继电器操作成功", Snackbar.LENGTH_SHORT).show();
                                    holder.mTextView.setText(nowProgress+"%");
                                    if (nowProgress== 0){
                                        holder.mImageView.setImageResource(R.drawable.ic_power_0);
                                    } else if (nowProgress<= 25) {
                                        holder.mImageView.setImageResource(R.drawable.ic_power_1);
                                    } else if (nowProgress<= 50) {
                                        holder.mImageView.setImageResource(R.drawable.ic_power_2);
                                    }else if (nowProgress<= 75) {
                                        holder.mImageView.setImageResource(R.drawable.ic_power_3);
                                    }else{
                                        holder.mImageView.setImageResource(R.drawable.ic_power_4);
                                    }
                                } else {
                                    Snackbar.make(MainActivity.mCoordinatorLayout,
                                            "继电器操作失败", Snackbar.LENGTH_LONG)
                                            .setAction("OK",null).show();
                                    holder.mSeekBar.setProgress(mState.getCurrentState());
                                }
                            }
                        }.execute((long)nowProgress);
                    }
                });
            }

        }

    }

    // 获取数据的数量
    @Override
    public int getItemCount() {
        if(MainActivity.isIdentify){
            return ITEM_NUMBER;
        }else return ITEM_NUMBER-1;
    }

    // 自定义的ViewHolder，持有每个Item的所有界面元素
    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private TextView mTextView, mTextDate;
        private CardView mCardView;

        private ProgressView mProgressView;
        private DiscreteSeekBar mSeekBar;
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
                mProgressView = (ProgressView) itemView.findViewById(R.id.card_delay_progress);
                mSeekBar = (DiscreteSeekBar) itemView.findViewById(R.id.card_delay_seek_bar);
                mTextView = (TextView) itemView.findViewById(R.id.card_delay_tv);
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

    public void refreshData(CurrentData data,RelayState state) {
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
