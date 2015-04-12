package com.montserrat.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.montserrat.lib.ViewPager.PagerAdapter;
import com.montserrat.lib.ViewPager.ViewPager;


public class DetailActivity extends ActionBarActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//        viewPagerAdapter adapter = new viewPagerAdapter(this);
//        ViewPager viewPager;
//
//        viewPager = (ViewPager)findViewById(R.id.viewPager);
//        viewPager.setAdapter(adapter);
//        viewPager.setCurrentItem(0);
//
//        setContentView(R.layout.activity_main);
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    private ViewPager mPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mPager = (ViewPager)findViewById(R.id.viewPager);
        mPager.setAdapter(new PagerAdapterClass(getApplicationContext()));
        mPager.setPageMargin(15);
        mPager.enableCenterLockOfChilds();
        mPager.setCurrentItemInCenter(0);
    }

    private void setCurrentInflateItem(int type){
        if(type==0){
            mPager.setCurrentItemInCenter(0);
        }else if(type==1){
            mPager.setCurrentItemInCenter(1);
        }else{
            mPager.setCurrentItemInCenter(2);
        }
    }

    /**
     * Layout
     */

    private View.OnClickListener mPagerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String text = ((Button)v).getText().toString();
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * PagerAdapter
     */
    private class PagerAdapterClass extends PagerAdapter{

        private LayoutInflater mInflater;

        public PagerAdapterClass(Context c){
            super();
            mInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public Object instantiateItem(View pager, int position) {
            View v = null;
            if(position==0){
                v = mInflater.inflate(R.layout.page1, null);
                v.findViewById(R.id.iv_one);
            }else if(position==1){
                v = mInflater.inflate(R.layout.page1, null);
                v.findViewById(R.id.iv_one);
            }else if(position==2){
                v = mInflater.inflate(R.layout.page1, null);
                v.findViewById(R.id.iv_one);
            }else if(position==3){
                v = mInflater.inflate(R.layout.page1, null);
                v.findViewById(R.id.iv_one);
            }else if(position==4){
                v = mInflater.inflate(R.layout.page1, null);
                v.findViewById(R.id.iv_one);
            }else{
                v = mInflater.inflate(R.layout.page1, null);
                v.findViewById(R.id.iv_one);
            }

            ((ViewPager)pager).addView(v, 0);

            return v;
        }
        public float getPageWidth(int position)
        {
//            if (position == 0 || position == getCount()-1)
//            {
//                return 0.9f;
//            }
            return 0.9f;
        }
        @Override
        public void destroyItem(View pager, int position, Object view) {
            ((ViewPager)pager).removeView((View)view);
        }

        @Override
        public boolean isViewFromObject(View pager, Object obj) {
            return pager == obj;
        }

        @Override public void restoreState(Parcelable arg0, ClassLoader arg1) {}
        @Override public Parcelable saveState() { return null; }
        @Override public void startUpdate(View arg0) {}
        @Override public void finishUpdate(View arg0) {}
    }
}
