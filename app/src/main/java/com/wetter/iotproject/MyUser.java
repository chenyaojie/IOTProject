package com.wetter.iotproject;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by Wetter on 2016/4/10.
 */
public class MyUser extends BmobUser {

    private BmobFile avatar;
    private String nickName;
    private String nfcUID;

    public void setAvatar(BmobFile avatar) {
        this.avatar = avatar;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public BmobFile getAvatar() {
        return avatar;
    }

    public void setNfcUID(String nfcUID) {
        this.nfcUID = nfcUID;
    }

    public String getNfcUID() {
        return nfcUID;
    }

}
