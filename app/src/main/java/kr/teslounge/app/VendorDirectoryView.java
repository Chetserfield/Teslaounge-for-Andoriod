package kr.teslounge.app;

import android.view.View;
import android.widget.*;
import android.text.*;

/** Directory entry UI. No fictional vendors, prices, reviews or paid placements. */
final class VendorDirectoryView {
    private final MainActivity activity;
    private final String[] categories={"전체 서비스","틴팅","PPF","신차패키지","세차","사고수리"};
    private EditText search;
    private int selectedCategory=0;
    private LinearLayout categoryButtons;
    private TextView resultTitle,resultDescription;
    private LinearLayout directory,advertising;
    private org.json.JSONArray providers=new org.json.JSONArray();
    private boolean loaded=false;
    private String loadError=null;
    VendorDirectoryView(MainActivity activity){this.activity=activity;}
    void attach(LinearLayout body){
        activity.heading(body,"내 차를 위한 케어");
        body.addView(activity.text("오래 아끼고 싶은 내 차를 위해.",14,activity.muted));
        search=activity.input("파트너 이름 또는 지역 검색",80,false);body.addView(search);
        HorizontalScrollView categoriesScroll=new HorizontalScrollView(activity);categoriesScroll.setHorizontalScrollBarEnabled(false);
        categoryButtons=new LinearLayout(activity);categoriesScroll.addView(categoryButtons);body.addView(categoriesScroll);
        for(int i=0;i<categories.length;i++){final int index=i;Button chip=activity.quiet(categories[i],()->{selectedCategory=index;updateCategories();refresh();});chip.setTextSize(11);LoungeIcon icon=new LoungeIcon("케어",activity.accent);icon.setBounds(0,0,activity.dp(22),activity.dp(22));chip.setCompoundDrawables(null,icon,null,null);chip.setCompoundDrawablePadding(activity.dp(8));categoryButtons.addView(chip,new LinearLayout.LayoutParams(-2,-2));}
        updateCategories();
        LinearLayout ad=activity.card(body);advertising=ad;
        ad.setBackground(activity.bg(0xFF252E25));ad.addView(activity.text("PARTNER SPOTLIGHT · 광고",10,activity.accent));
        ad.addView(activity.text("내 차를 아끼는 또 하나의 방법",17,activity.ink));
        ad.addView(activity.text("현재 등록된 광고가 없어요. 광고는 일반 검색 결과와 구분해 표시합니다.",13,activity.muted));
        activity.heading(body,"케어 파트너 찾기");
        LinearLayout results=activity.card(body);directory=activity.column();body.addView(directory);
        resultTitle=activity.text("",17,activity.ink);results.addView(resultTitle);
        resultDescription=activity.text("",13,activity.muted);results.addView(resultDescription);
        body.addView(activity.quiet("검색 조건 초기화",()->{search.setText("");selectedCategory=0;updateCategories();refresh();}));
        body.addView(activity.text(BackendClient.configured()?"현재 예약과 견적 요청은 접수되지 않습니다.":"업체 정보 연결 준비 중 · 현재 예약과 견적 요청은 접수되지 않습니다.",12,activity.muted));
        body.addView(activity.quiet("인도 준비 체크리스트",()->activity.go("구매·인도")));
        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){refresh();}
            public void afterTextChanged(Editable e){}
        });
        refresh();
        if(BackendClient.configured()){
            body.addView(activity.quiet("파트너 새로고침",()->activity.go("케어")));
            BackendClient api=BackendClient.get(activity);
            api.async(()->new org.json.JSONObject()
                .put("providers",api.rows("providers?select=id,name,region,category,description&order=name.asc,id.asc&limit=200"))
                .put("ads",api.rows("advertisements?select=title,description,providers(name,region)&order=starts_at.desc&limit=10")),(response,error)->{
                if(activity.isDestroyed()||activity.body!=body)return;
                loaded=true;loadError=error;advertising.removeAllViews();advertising.addView(activity.text("PARTNER SPOTLIGHT · 광고",12,activity.muted));
                if(error!=null){advertising.addView(activity.text(error,13,activity.muted));refresh();return;}
                providers=response.optJSONArray("providers");org.json.JSONArray ads=response.optJSONArray("ads");
                if(ads.length()==0)advertising.addView(activity.text("현재 등록된 광고가 없어요",14,activity.muted));
                for(int i=0;i<ads.length();i++){
                    org.json.JSONObject adItem=ads.optJSONObject(i);advertising.addView(activity.text(adItem.optString("title"),17,activity.ink));
                    advertising.addView(activity.text(adItem.optString("description"),13,activity.muted));
                    org.json.JSONObject vendor=adItem.optJSONObject("providers");if(vendor!=null)advertising.addView(activity.text(vendor.optString("name")+" · "+vendor.optString("region"),12,activity.muted));
                }
                refresh();
            });
        }
    }
    private void updateCategories(){
        for(int i=0;i<categoryButtons.getChildCount();i++){Button b=(Button)categoryButtons.getChildAt(i);b.setTextColor(i==selectedCategory?activity.accent:activity.muted);b.setBackground(activity.bg(i==selectedCategory?0xFF293226:activity.paper));b.setSelected(i==selectedCategory);}
    }
    private void refresh(){
        String query=search.getText().toString().trim();
        int selected=selectedCategory;
        if(BackendClient.configured()){
            directory.removeAllViews();
            if(!loaded){resultTitle.setText("업체를 불러오는 중이에요");resultDescription.setText("");return;}
            if(loadError!=null){resultTitle.setText("업체 정보를 불러오지 못했어요");resultDescription.setText(loadError);return;}
            int count=0;String term=query.toLowerCase(java.util.Locale.ROOT);
            for(int i=0;i<providers.length();i++){
                org.json.JSONObject p=providers.optJSONObject(i);
                if(selected>0&&!categories[selected].equals(p.optString("category")))continue;
                if(!(p.optString("name")+" "+p.optString("region")).toLowerCase(java.util.Locale.ROOT).contains(term))continue;
                count++;LinearLayout card=activity.card(directory);card.addView(activity.text(p.optString("name"),17,activity.ink));
                card.addView(activity.text(p.optString("category")+" · "+p.optString("region"),12,activity.muted));
                card.addView(activity.text(p.optString("description"),14,activity.muted));
            }
            resultTitle.setText(count==0?"조건에 맞는 업체가 없어요":"찾은 업체 "+count+"곳");
            resultDescription.setText("등록된 업체 중 최대 200곳에서 검색합니다.");return;
        }
        resultTitle.setText("아직 등록된 업체가 없어요");
        String condition=(selected>0?categories[selected]:"전체 서비스")+(query.isEmpty()?"":" · “"+query+"”");
        resultDescription.setText(condition+"\n업체가 등록되면 이 조건에 맞는 목록을 보여드릴게요.");
    }
}
