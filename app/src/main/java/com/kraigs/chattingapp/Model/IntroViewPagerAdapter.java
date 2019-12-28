package com.kraigs.chattingapp.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;


import com.airbnb.lottie.LottieAnimationView;
import com.kraigs.chattingapp.R;

import java.util.List;

public class IntroViewPagerAdapter extends PagerAdapter {

    Context mContext;
    List<ScreenItem> mListScreen;

    public IntroViewPagerAdapter(Context context,List<ScreenItem> mListScreen){
        this.mContext = context;
        this.mListScreen = mListScreen;
    }

    @Override
    public int getCount() {
        return mListScreen.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutScreen = inflater.inflate(R.layout.zunit_intro , null);

        LottieAnimationView animationView = layoutScreen.findViewById(R.id.loginAnim);
        animationView.setAnimation(mListScreen.get(position).getAsset());
        TextView title = layoutScreen.findViewById(R.id.intro_title);
        TextView description = layoutScreen.findViewById(R.id.intro_description);
        LinearLayout introLl = layoutScreen.findViewById(R.id.introLl);

        title.setText(mListScreen.get(position).getTitle());
        description.setText(mListScreen.get(position).getDescription());
        introLl.setBackgroundResource(mListScreen.get(position).getBackground());

        container.addView(layoutScreen);

        return layoutScreen;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}
