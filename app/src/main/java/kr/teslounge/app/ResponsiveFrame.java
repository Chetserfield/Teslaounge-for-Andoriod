package kr.teslounge.app;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

/** Bounds the reading column using actual window space, including split-screen. */
final class ResponsiveFrame extends FrameLayout {
    ResponsiveFrame(Context context){super(context);}
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        int available=View.MeasureSpec.getSize(widthSpec);
        int maximum=Math.round(640*getResources().getDisplayMetrics().density);
        if(getChildCount()>0){
            FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)getChildAt(0).getLayoutParams();
            params.width=Math.min(available,maximum);
            params.gravity=Gravity.CENTER_HORIZONTAL;
        }
        super.onMeasure(widthSpec,heightSpec);
    }
}
