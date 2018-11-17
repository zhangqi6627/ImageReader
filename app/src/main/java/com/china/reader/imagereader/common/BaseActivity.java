package com.china.reader.imagereader.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.reader.imagereader.R;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;


public class BaseActivity extends AppCompatActivity {
    protected Context mContext = BaseActivity.this;
    private String TAG = "BaseActivity";
    private Toast mToast;
    private ProgressDialog progressDialog;
    protected final static int MSG_GET_IMAGE_TITLE_LIST = 0x601;
    protected final static int MSG_GET_IMAGE_LIST = 0x602;
    // intent extras key
    public final static String KEY_WEB_INDEX = "webIndex";
    public final static String KEY_WEB_ISFAV = "isFavorite";
    public final static String KEY_IMAGE_PAGE = "imagePage";

    //http://www.youzi4.cc/mm/%d/%d_%d.html
    public final static int WEB_INDEX_1 = 1;
    //http://zp2006.com/img_%d.html
    public final static int WEB_INDEX_2 = 2;
    //http://www.17786.com/%d_%d.html
    public final static int WEB_INDEX_3 = 3;
    //http://www.mmjpg.com/mm/%d/%d
    public final static int WEB_INDEX_4 = 4;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseActivity.this.handleMessage(msg);
        }
    };

    protected void handleMessage(Message msg) {
    }
    protected void sendMessage(Message msg){
        mHandler.sendMessage(msg);
    }
    protected void sendEmptyMessage(int what){
        mHandler.sendEmptyMessage(what);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*显示toast*/
    protected void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        }
        mToast.setText(msg);
        mToast.show();
    }

    /*显示带图片的toast*/
    protected void showImageToast(String msg, int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        }
        //TextView
        TextView textView = new TextView(this);
        textView.setText(msg);
        textView.setTextSize(20);
        //textView.setTextColor(getResources().getColor(R.color.fbutton_color_orange));
        //ImageView
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.mipmap.ic_launcher);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);//设置LinearLayout垂直
        layout.setGravity(Gravity.CENTER);//设置LinearLayout里面内容中心分布
        layout.setBackgroundColor(Color.parseColor("#99000000"));
        layout.setPadding(20, 20, 20, 20);
        layout.addView(imageView);//先添加image
        layout.addView(textView);//再添加text
        mToast.setView(layout);//只需要把layout设置进入Toast
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.show();
    }

    protected void showSuperToast(String msg) {
        SuperActivityToast.create(this, new Style(), Style.TYPE_BUTTON)
                .setButtonText("UNDO")
                .setButtonIconResource(R.drawable.btn_star_normal)
                .setOnButtonClickListener("good_tag_name", null, new SuperActivityToast.OnButtonClickListener() {
                    @Override
                    public void onClick(View view, Parcelable token) {
                        LogUtils.e("BaseActivity->showSuperToast()");
                    }
                })
                .setProgressBarColor(Color.WHITE)
                .setText("Email deleted")
                .setDuration(Style.DURATION_LONG)
                .setFrame(Style.FRAME_LOLLIPOP)
                .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_PURPLE))
                .setAnimations(Style.ANIMATIONS_POP).show();
    }

    /*显示toast*/
    protected void showToast(int msgId) {
        showToast(getString(msgId));
    }

    /*显示进度条对话框*/
    protected void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext);
        }
        progressDialog.setMessage("Please wait a while...");
        progressDialog.show();
    }

    /*隐藏进度条对话框*/
    protected void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /* 保存到 SharedPreference */
    protected void saveToPrefs(String key, String value) {
        getSharedPref().edit().putString(key, value).commit();
    }

    /* 从SharedPreference中读取 */
    protected String getPrefsVal(String key) {
        return getSharedPref().getString(key, "");
    }

    protected SharedPreferences getSharedPref() {
        return getSharedPreferences("imagereader", Context.MODE_PRIVATE);
    }

    private final static boolean LOGGING = true;

}
