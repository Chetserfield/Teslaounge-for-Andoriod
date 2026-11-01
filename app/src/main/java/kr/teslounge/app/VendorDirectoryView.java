package kr.teslounge.app;

import android.view.View;
import android.widget.*;
import android.text.*;

/** Directory entry UI. No fictional vendors, prices, reviews or paid placements. */
final class VendorDirectoryView {
    private final MainActivity activity;
    private final String[] categories={"전체 서비스","틴팅","PPF","신차패키지","세차","사고수리"};
    private EditText search;
    private Spinner category;
    private TextView resultTitle,resultDescription;
    private LinearLayout directory,advertising;
    private org.json.JSONArray providers=new org.json.JSONArray();
    private boolean loaded=false;
    private String loadError=null;
    VendorDirectoryView(MainActivity activity){this.activity=activity;}
    void attach(LinearLayout body){
        activity.heading(body,"내 차를 위한 케어");
        body.addView(activity.text("내 차에 필요한 시공과 관리",14,activity.muted));
        search=activity.input("업체명 또는 지역 검색",80,false);body.addView(search);
        body.addView(activity.text("서비스 선택",12,activity.muted));
        category=new Spinner(activity);category.setContentDescription("업체 서비스 종류");
        category.setAdapter(new ArrayAdapter<>(activity,android.R.layout.simple_spinner_dropdown_item,categories));
        category.setMinimumHeight(activity.dp(48));body.addView(category);
        LinearLayout ad=activity.card(body);advertising=ad;
        ad.addView(activity.text("광고 · 업체 소식",12,activity.muted));
        ad.addView(activity.text("새로운 업체 소식을 기다려요",17,activity.ink));
        ad.addView(activity.text("현재 등록된 광고가 없어요. 광고는 일반 검색 결과와 구분해 표시합니다.",13,activity.muted));
        activity.heading(body,"업체 목록");
        LinearLayout results=activity.card(body);directory=activity.column();body.addView(directory);
        resultTitle=activity.text("",17,activity.ink);results.addView(resultTitle);
        resultDescription=activity.text("",13,activity.muted);results.addView(resultDescription);
        body.addView(activity.button("검색 조건 초기화",()->{search.setText("");category.setSelection(0);refresh();}));
        body.addView(activity.text(BackendClient.configured()?"현재 예약과 견적 요청은 접수되지 않습니다.":"업체 정보 연결 준비 중 · 현재 예약과 견적 요청은 접수되지 않습니다.",12,activity.muted));
        body.addView(activity.button("인도 준비 체크리스트",()->activity.go("구매·인도")));
        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){refresh();}
            public void afterTextChanged(Editable e){}
        });
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(AdapterView<?> parent){}
            public void onItemSelected(AdapterView<?> parent,View view,int position,long id){refresh();}
        });
        refresh();
        if(BackendClient.configured()){
            body.addView(activity.button("업체 새로고침",()->activity.go("케어")));
            BackendClient api=BackendClient.get(activity);
            api.async(()->new org.json.JSONObject()
                .put("providers",api.rows("providers?select=id,name,region,category,description&order=name.asc,id.asc&limit=200"))
                .put("ads",api.rows("advertisements?select=title,description,providers(name,region)&order=starts_at.desc&limit=10")),(response,error)->{
                if(activity.isDestroyed()||activity.body!=body)return;
                loaded=true;loadError=error;advertising.removeAllViews();advertising.addView(activity.text("광고 · 업체 소식",12,activity.muted));
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
    private void refresh(){
        String query=search.getText().toString().trim();
        int selected=category.getSelectedItemPosition();
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
