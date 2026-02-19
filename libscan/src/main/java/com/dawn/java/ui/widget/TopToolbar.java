package com.dawn.java.ui.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;

import com.dawn.decoderapijni.R;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TopToolbar extends Toolbar implements View.OnClickListener {

    private ImageButton btn_left;
    private ImageView iv_left_red_point;
    private ImageView iv_left_state_icon;

    private TextView tv_title;
    private ImageButton btn_reght;
    private MenuToolBarListener menuToolBarListener;

    public TopToolbar(Context context) {
        this(context, null);
    }

    public TopToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.layout_top_toolbar, this);
        btn_left = findViewById(R.id.btn_htb_left);
        tv_title = findViewById(R.id.tv_htb_title);
        btn_reght = findViewById(R.id.btn_htb_right);
        iv_left_red_point = findViewById(R.id.iv_left_red_point);
        iv_left_state_icon = findViewById(R.id.iv_left_state_icon);

        btn_left.setOnClickListener(this);
        btn_reght.setOnClickListener(this);

        TypedArray array = null;
        try {
            //获得属性集合，并从属性集合中获得对应属性的资源
            array = getContext().obtainStyledAttributes(attrs, R.styleable.TopToolbar);

            String title=array.getString(R.styleable.TopToolbar_mainTitle);
            tv_title.setText(title);
            Drawable srcRightIcon = array.getDrawable(R.styleable.TopToolbar_srcRightIcon);
            if (srcRightIcon != null) {
                btn_reght.setImageDrawable(srcRightIcon);
            }
            Drawable srcLeftIcon = array.getDrawable(R.styleable.TopToolbar_srcLeftIcon);
            if (srcLeftIcon != null) {
                btn_left.setImageDrawable(srcLeftIcon);
            }
        } finally {
            if (array != null) {
                array.recycle();
            }
        }

    }

    //设置中间title的内容
    public void setMainTitle(String text) {
        this.setTitle(" ");
        tv_title.setText(text);
    }

    //设置中间title的内容文字的颜色
    public void setMainTitleColor(int color) {
        tv_title.setTextColor(color);
    }

    //设置title左边小红点图标
    public void setLeftRedPointDrawable(int id) {
        iv_left_red_point.setImageResource(id);
    }

    //设置title左边小红点图标是否可见
    public void setRedPointVisibility(int visible) {
        iv_left_red_point.setVisibility(visible);
    }

    //设置title左边下方图标
    public void setLeftStateDrawable(int id) {
        iv_left_state_icon.setImageResource(id);
    }

    //设置title左边下方图标是否可见
    public void setLeftStateVisibility(int visible) {
        iv_left_state_icon.setVisibility(visible);
    }

    //设置title左边图标
    public void setLeftTitleDrawable(int id) {
        btn_left.setImageResource(id);
    }

    //设置title左边图标是否可见
    public void setLeftTitleVisibility(int visible) {
        btn_left.setVisibility(visible);
    }

    //设置title右边图标
    public void setRightTitleDrawable(int id) {
        btn_reght.setImageResource(id);
    }

    //设置title右边图标是否可见
    public void setRightTitleVisibility(int visible) {
        btn_reght.setVisibility(visible);
    }

    @Override
    public void onClick(View v) {

        if (menuToolBarListener == null) return;

        int id = v.getId();
        if (id == R.id.btn_htb_left) {
            menuToolBarListener.onToolBarClickLeft(v);
        } else if (id == R.id.btn_htb_right) {
            menuToolBarListener.onToolBarClickRight(v);
        }
    }

    public void setMenuToolBarListener(MenuToolBarListener listener) {
        menuToolBarListener = listener;
    }

    public interface MenuToolBarListener {
        public void onToolBarClickLeft(View v);

        public void onToolBarClickRight(View v);
    }

}
