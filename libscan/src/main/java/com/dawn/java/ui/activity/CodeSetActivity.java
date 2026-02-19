package com.dawn.java.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.dawn.decoderapijni.ServiceTools;
import com.dawn.decoderapijni.SoftEngine;
import com.dawn.decoderapijni.bean.CodeEnableBean;
import com.dawn.java.ScanApplication;
import com.dawn.java.ui.activity.base.DawnActivity;
import com.dawn.java.ui.widget.TopToolbar;
import com.dawn.decoderapijni.R;

import java.util.ArrayList;
import java.util.List;

public class CodeSetActivity extends DawnActivity {

    private TopToolbar topBar;
    private ViewPager viewPager;
    private RadioGroup rg_code_type;
    private RadioButton rd_code_1d;
    private RadioButton rd_code_2d;
    private RadioButton rd_code_ocr;
    private RadioButton rd_code_other;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_code_set;
    }

    private void initView() {
        rg_code_type = findViewById(R.id.rg_code_type);
        rd_code_1d = findViewById(R.id.rd_code_1d);
        rd_code_2d = findViewById(R.id.rd_code_2d);
        rd_code_ocr = findViewById(R.id.rd_code_ocr);
        rd_code_other = findViewById(R.id.rd_code_other);

        topBar = findViewById(R.id.topBar);
        topBar.setMenuToolBarListener(new TopToolbar.MenuToolBarListener() {
            @Override
            public void onToolBarClickLeft(View v) {
                finish();
            }

            @Override
            public void onToolBarClickRight(View v) {

            }
        });


        viewPager = findViewById(R.id.pager);
        CodeSetPagerAdapter codeSetPagerAdapter = new CodeSetPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(codeSetPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        rd_code_1d.setChecked(true);
                        break;
                    case 1:
                        rd_code_2d.setChecked(true);
                        break;
                    case 2:
                        rd_code_ocr.setChecked(true);
                        break;
                    case 3:
                        rd_code_other.setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        rd_code_1d.setChecked(true);
        rg_code_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.rd_code_1d) {
                    viewPager.setCurrentItem(0);
                } else if (checkedId == R.id.rd_code_2d) {
                    viewPager.setCurrentItem(1);
                } else if (checkedId == R.id.rd_code_ocr) {
                    viewPager.setCurrentItem(2);
                } else if (checkedId == R.id.rd_code_other) {
                    viewPager.setCurrentItem(3);
                }
            }
        });
    }



    private class CodeSetPagerAdapter extends FragmentStatePagerAdapter {

        private static final int POS_1D = 0;
        private static final int POS_2D = 1;
        private static final int POS_OCR = 2;
        private static final int POS_OTHER = 3;

        private List<String> pageTitleList = new ArrayList<>();

        CodeSetPagerAdapter(FragmentManager fm) {
            super(fm);
            pageTitleList.add(POS_1D, "1D");
            pageTitleList.add(POS_2D, "2D");
            pageTitleList.add(POS_OCR, "OCR");
            pageTitleList.add(POS_OTHER, "OTHER");
        }

        @Override
        public Fragment getItem(int position) {
//            Log.v(TAG, "Fragment getItem" + position);
            Fragment fragment = new CodeSetPagerFragment();
            Bundle args = new Bundle();
            args.putInt(ScanApplication.BUNDLE_KEY_CUR_FRAGMENT, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return pageTitleList.size();
        }

        //
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitleList.get(position);
        }
    }

    public static class CodeSetPagerFragment extends Fragment {

        private ListView lv_code_item;
        private CodeSetItemAdapter mCodeSetItemAdapter;
        private List<CodeEnableBean> codeSetItemSet;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_code_set, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            lv_code_item = view.findViewById(R.id.lv_code_item);

            Bundle argsBundle = getArguments();
            if (argsBundle != null) {
                int curFragment = argsBundle.getInt(ScanApplication.BUNDLE_KEY_CUR_FRAGMENT);
                codeSetItemSet = new ArrayList<>();
                switch (curFragment) {
                    case 0:
                        codeSetItemSet= ServiceTools.getInstance().get1DCodeEnableList();
                        break;
                    case 1:
                        codeSetItemSet= ServiceTools.getInstance().get2DCodeEnableList();
                        break;
                    case 2:
                        codeSetItemSet= ServiceTools.getInstance().getOCREnableList();
                        break;
                    case 3:
                        codeSetItemSet= ServiceTools.getInstance().getOtherCodeEnableList();
                        break;
                    default:
                        break;
                }

                mCodeSetItemAdapter = new CodeSetItemAdapter(view.getContext().getApplicationContext(),
                        codeSetItemSet, null);
            }
            lv_code_item.setAdapter(mCodeSetItemAdapter);

            lv_code_item.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    CodeEnableBean item = (CodeEnableBean) mCodeSetItemAdapter.getItem(i);
                    Intent intent = new Intent(getActivity(), ParamSetActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(ScanApplication.BUNDLE_KEY_CODE_NAME, item.getCodeName());
                    intent.putExtras(bundle);
                    startActivityForResult(intent, ScanApplication.CODE_SETTING_REQUEST_CODE);
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            Log.v(ScanApplication.TAG, "onActivityResult" + requestCode + " " + resultCode);
            if (requestCode == ScanApplication.CODE_SETTING_REQUEST_CODE && resultCode == ScanApplication.CODE_SETTING_SUCCESS_RESULT_CODE) {
                String codeName = data.getStringExtra(ScanApplication.BUNDLE_KEY_CODE_NAME);
                Log.v(ScanApplication.TAG, "codeName return :" + codeName);
                String enable = SoftEngine.getInstance().ScanGet(codeName, "Enable");

                for (CodeEnableBean item : codeSetItemSet) {
                    if(item.getCodeName().equals(codeName)){
                        item.setEnableValue(enable);
                        mCodeSetItemAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }

        private class CodeSetItemAdapter extends BaseAdapter {

            private LayoutInflater mInflater;
            private List<CodeEnableBean> mList;

            CodeSetItemAdapter(Context context, List<CodeEnableBean> mList,
                               View.OnClickListener detailsListener) {
                this.mList = mList;
                mInflater = LayoutInflater.from(context);
            }

            @Override
            public int getCount() {
                return mList.size();
            }

            @Override
            public Object getItem(int position) {
                if (this.mList == null)
                    return null;
                return this.mList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (position >= this.mList.size())
                    return convertView;
                ViewHolder holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.layout_code_enable_item, null, false);

                    holder = new ViewHolder();
                    holder.tv_code_name = convertView.findViewById(R.id.tv_code_name);
                    holder.tv_enable = convertView.findViewById(R.id.tv_enable);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                CodeEnableBean item = mList.get(position);
                if (item.getEnableValue() != null && item.getEnableValue().equals("1")) {
                    holder.tv_enable.setText(R.string.common_open);
                    holder.tv_enable.setTextColor(getResources().getColor(R.color.app_main_blue));
                } else {
                    holder.tv_enable.setText(R.string.common_close);
                    holder.tv_enable.setTextColor(Color.RED);
                }
                holder.tv_code_name.setText(item.getFullCodeName());
                return convertView;
            }

            private class ViewHolder {
                private TextView tv_code_name;
                private TextView tv_enable;

            }
        }

    }

}
