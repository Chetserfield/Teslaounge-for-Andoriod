package kr.teslounge.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.webkit.*;
import android.widget.Toast;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/** Isolated display-only widget: no native bridge, credentials or local files. */
final class StockChartView extends WebView {
    private static final String PROVIDER="https://www.tradingview.com/symbols/NASDAQ-TSLA/";
    @SuppressLint("SetJavaScriptEnabled")
    StockChartView(Context context){
        super(context);
        setBackgroundColor(0xFF090A0C);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setAllowFileAccess(false);
        getSettings().setAllowContentAccess(false);
        getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        getSettings().setSupportMultipleWindows(true);
        setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
                if(!request.isForMainFrame())return !"https".equals(request.getUrl().getScheme());
                openProvider();return true;
            }
            @Override public void onReceivedError(WebView view,WebResourceRequest request,WebResourceError error){
                if(request.isForMainFrame())showFailure();
            }
        });
        setWebChromeClient(new WebChromeClient(){
            @Override public boolean onCreateWindow(WebView view,boolean dialog,boolean gesture,android.os.Message result){
                if(gesture)openProvider();return false;
            }
        });
        loadChart();
    }
    void loadChart(){
        try(InputStream input=getContext().getAssets().open("tsla-chart.html")){
            ByteArrayOutputStream output=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;
            while((n=input.read(buffer))!=-1)output.write(buffer,0,n);
            loadDataWithBaseURL("https://appassets.androidplatform.net/",output.toString(StandardCharsets.UTF_8.name()),"text/html","UTF-8",null);
        }catch(java.io.IOException error){showFailure();}
    }
    private void showFailure(){loadData("<html><meta charset='utf-8'><body style='background:#090a0c;color:#a6adb8;font-family:sans-serif'>차트를 불러오지 못했어요. 연결을 확인하고 아래에서 다시 불러와주세요.</body></html>","text/html","UTF-8");}
    void openProvider(){
        try{getContext().startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(PROVIDER)));}
        catch(ActivityNotFoundException error){Toast.makeText(getContext(),"링크를 열 브라우저가 없어요",Toast.LENGTH_SHORT).show();}
    }
    void dispose(){stopLoading();if(getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)getParent()).removeView(this);removeAllViews();destroy();}
}
