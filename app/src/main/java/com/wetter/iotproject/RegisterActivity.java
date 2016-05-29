package com.wetter.iotproject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout etEmail, etNick;
    private MyUser myUser;
    private BmobFile userAvatar;
    private ImageView ivNFC;

    // 邮箱地址验证
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;

    // 摄像机请求码
    private static int CAMERA_REQUEST_CODE = 1;
    // 图库请求码
    private static int GALLERY_REQUEST_CODE = 2;
    // 裁剪请求码
    private static int CROP_REQUEST_CODE = 3;

    // NFC相关定义
    private String nfcUID = "";
    private NfcAdapter mNfcAdapter = null;
    private PendingIntent mPendingIntent = null;
    private String[][] mTechList = null;
    private IntentFilter[] mIntentFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_register);
        initNfc();
        initView();
    }

    private void initView() {
        etEmail = (TextInputLayout) findViewById(R.id.et_email);
        etNick = (TextInputLayout) findViewById(R.id.et_nickname);
        ivNFC = (ImageView) findViewById(R.id.register_nfc_iv);
        myUser = new MyUser();
    }

    // 初始化NFC适配器，意图过滤器
    private void initNfc() {
        NfcCheck();
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter intentFilterTECH = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            intentFilterTECH.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        mIntentFilter = new IntentFilter[]{intentFilterTECH};
        mTechList = new String[][]{new String[]{MifareClassic.class.getName()}};
    }

    private void NfcCheck() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "您的手机不支持NFC", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, "尚未开启NFC功能", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
            }
        }
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] byteArr = tag.getId();
            try {
                nfcUID = getHexString(byteArr);
                Log.i("Log_NFC", "tagUID: " + nfcUID);
                identifyUID();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // 在获取UID后进行注册或登入
    private void identifyUID() {
        ivNFC.setImageResource(R.drawable.ic_check_box_black_24dp);
    }

    // Convert a byte array to a Hex string
    public static String getHexString(byte[] b){
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilter, mTechList);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
    }

    /**
     * 隐藏小键盘
     */
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Email地址验证
     *
     * @param email
     * @return
     */
    public boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }


    public void register(View view) {
        String uName = etEmail.getEditText().getText().toString();
        String uNick = etNick.getEditText().getText().toString();

        // 隐藏小键盘
        hideKeyboard();

        if (!validateEmail(uName)) {
            etEmail.setError("非法的邮箱地址");
        } else if (uNick.isEmpty()) {
            etNick.setError("呢称不得为空");
        } else if (nfcUID.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "NFC卡片未完成识别", Toast.LENGTH_SHORT).show();
        } else {
            etEmail.setErrorEnabled(false);
            doLogin(uName, nfcUID, uNick);
        }

    }

    /**
     * 用户注册方法
     *
     * @param uName
     * @param uPassword
     */
    private void doLogin(String uName, String uPassword, String uNick) {

        myUser.setUsername(uName);
        myUser.setPassword(uPassword);
        myUser.setEmail(uName);
        myUser.setNickName(uNick);
        myUser.setNfcUID(uPassword);
        if (userAvatar != null) {
            myUser.setAvatar(userAvatar);
        }

        myUser.signUp(this, new SaveListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(RegisterActivity.this, "注册成功!", Toast.LENGTH_SHORT).show();
                MainActivity.isIdentify = true;
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                Log.i("Log_RegisterActivity","注册失败：" + s);
                Toast.makeText(RegisterActivity.this, "注册失败：" + s, Toast.LENGTH_SHORT).show();

            }
        });

    }

    public void galleryPic(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    public void cameraPic(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    private Uri saveBitmap(Bitmap bm) {
        File tmpDir = new File(Environment.getExternalStorageDirectory() + "/com.wetter.iot");
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        File img = new File(tmpDir.getAbsolutePath() + "avater.png");
        try {
            FileOutputStream fos = new FileOutputStream(img);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return Uri.fromFile(img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Uri convertUri(Uri uri) {
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();

            return saveBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startImageZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (data == null) {
                return;
            } else {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap bm = extras.getParcelable("data");
                    Uri uri = saveBitmap(bm);
                    startImageZoom(uri);
                }
            }
        } else if (requestCode == GALLERY_REQUEST_CODE) {
            if (data == null) {
                return;
            }
            Uri uri;
            uri = data.getData();
            Log.i("Log_RegisterActivity", "Uri: " + uri);
            Uri fileUri = convertUri(uri);
            Log.i("Log_RegisterActivity", "fileUri: " + fileUri);
            startImageZoom(fileUri);
        } else if (requestCode == CROP_REQUEST_CODE) {
            Log.i("Log_RegisterActivity", "图片裁剪完毕");
            if (data == null) {
                return;
            }
            Bundle extras = data.getExtras();
            if (extras == null) {
                return;
            }
            Bitmap bm = extras.getParcelable("data");
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(bm);

            Uri bmUrl = saveBitmap(bm);
            Log.i("Log_RegisterActivity", "bmUrl: " + bmUrl.getPath());
            userAvatar = new BmobFile(new File(bmUrl.getPath()));

            userAvatar.uploadblock(this, new UploadFileListener() {
                @Override
                public void onSuccess() {
                    Log.i("Log_RegisterActivity", userAvatar.getFileUrl(RegisterActivity.this));
                    Toast.makeText(RegisterActivity.this, "头像上传成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i, String s) {
                    Log.i("Log_RegisterActivity", "头像上传失败" + s);
                    Toast.makeText(RegisterActivity.this, "头像上传失败" + s, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(Integer value) {
                    super.onProgress(value);
                    Log.i("Log_RegisterActivity", "onProgress" + value);
                }
            });
        }
    }

}
