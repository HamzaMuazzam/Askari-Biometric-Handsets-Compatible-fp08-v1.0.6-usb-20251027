package com.dawn.java.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dawn.decoderapijni.R;


public class AutoTestItemView extends LinearLayout {
    public static final int ITEM_STATE_WAIT = 0;
    public static final int ITEM_STATE_PROCESSING = 1;
    public static final int ITEM_STATE_COMPLETE = 2;
    public static final int ITEM_STATE_ERROR = 3;

    private int mTotalCount = 0;
    private int mDelay = 0;
    private int mCurCount = 0;
    private int mState = 0;
    private int mErrorCount = 0;
    private Context mContext;
    private LinearLayout ll_test_item;
    private TextView tv_test_delay;
    private TextView tv_cur_count;
    private TextView tv_total_count;
    private ImageView iv_test_state;
    private LinearLayout ll_error;
    private TextView tv_error_count;


    public AutoTestItemView(Context context, int delay, int totalCount) {
        super(context);
        mContext = context;
        mDelay = delay;
        mTotalCount = totalCount;
        initView(context);
    }

    public AutoTestItemView(Context context, AttributeSet attrs, int delay, int totalCount) {
        super(context, attrs);
        mContext = context;
        mDelay = delay;
        mTotalCount = totalCount;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_auto_testing_item, this, true);
        ll_test_item = findViewById(R.id.ll_test_item);
        tv_test_delay = findViewById(R.id.tv_test_delay);
        tv_cur_count = findViewById(R.id.tv_cur_count);
        tv_total_count = findViewById(R.id.tv_total_count);
        iv_test_state = findViewById(R.id.iv_test_state);
        ll_error = findViewById(R.id.ll_error);
        tv_error_count = findViewById(R.id.tv_error_count);

        tv_test_delay.setText(mDelay + "");
        tv_cur_count.setText(mCurCount + "");
        tv_total_count.setText(mTotalCount + "");
        setItemState(ITEM_STATE_WAIT);

    }

    public void setLongClickListener(OnLongClickListener listener) {
        ll_test_item.setOnLongClickListener(listener);
    }

    public void setCurCount(int curCount) {
        mCurCount = curCount;
        tv_cur_count.setText(mCurCount + "");
    }

    public void setErrorCount(int errorCount) {
        mErrorCount = errorCount;
        if (mErrorCount > 0) {
            ll_error.setVisibility(VISIBLE);
            tv_error_count.setText(mErrorCount + "");
        } else {
            ll_error.setVisibility(INVISIBLE);
        }
    }

    public void setItemState(int state) {
        mState = state;
        switch (state) {
            case ITEM_STATE_WAIT:
                iv_test_state.setImageDrawable(this.getResources().getDrawable(R.drawable.icon_waiting, mContext.getTheme()));
                break;
            case ITEM_STATE_PROCESSING:
                iv_test_state.setImageDrawable(this.getResources().getDrawable(R.drawable.icon_process, mContext.getTheme()));
                break;
            case ITEM_STATE_COMPLETE:
                iv_test_state.setImageDrawable(this.getResources().getDrawable(R.drawable.icon_complete, mContext.getTheme()));
                break;
            case ITEM_STATE_ERROR:
                iv_test_state.setImageDrawable(this.getResources().getDrawable(R.drawable.icon_warning, mContext.getTheme()));
                break;
        }
    }

    public int getCurCount() {
        return mCurCount;
    }

    public int getDelay() {
        return mDelay;
    }

    public int getTotalCount() {
        return mTotalCount;
    }

    public int getState() {
        return mState;
    }

    public int getErrorCount() {
        return mErrorCount;
    }
}
