package com.wetter.iotproject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;

/**
 * Created by Wetter on 2016/5/28.
 */
public class RelayAsyncTask extends AsyncTask<Integer, Void, Boolean> {
    private Context mContext;

    public RelayAsyncTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Boolean doInBackground(final Integer... ints) {
        final boolean[] isEqual = {false};
        int count = 0;

        while (count < 20) {
            Log.i("Log_Time", "passTime: " + count);
            BmobQuery<RelayState> stateBmobQuery = new BmobQuery<>();
            stateBmobQuery.getObject(mContext, "hnm5666B", new GetListener<RelayState>() {
                @Override
                public void onSuccess(RelayState state) {
                    int current = -1;
                    switch (ints[1]){
                        case 0: current = state.getCurrentState();break;
                        case 1: current = state.getCurrentState_1();break;
                        case 2: current = state.getCurrentState_2();break;
                        case 3: current = state.getCurrentState_3();break;
                    }
                    if (current == ints[0]) {
                        isEqual[0] = true;
                    }
                }

                @Override
                public void onFailure(int i, String s) {

                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isEqual[0]) {
                return true;
            }
            count++;
        }

        return isEqual[0];
    }

}
