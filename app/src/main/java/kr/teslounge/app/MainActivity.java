package kr.teslounge.app;

import android.app.*;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.view.*;
import android.widget.*;
import android.text.*;
import org.json.*;
import java.util.Locale;

/** Offline prototype. No network or account credentials are collected. */
public class MainActivity extends Activity {
    final int ink=Color.rgb(25,31,40), muted=Color.rgb(107,118,132), paper=Color.rgb(242,244,246), accent=Color.rgb(229,72,77);
    final String[] groups={"관심자", "예비 오너", "오너"};
    final String[] categories={"전체","구매·인도 Q&A","오너 생활","지역 모임"};
    final String[] models={"예비 오너","Model Y","Model 3","Model S","Model X","Cybertruck"};
    SharedPreferences prefs;
    JSONArray posts;
    LinearLayout root, body, list;
    String tab="홈", filter="전체", query="";
    int detail=-1;
    int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle b){
        super.onCreate(b); prefs=getSharedPreferences("teslounge_v1",MODE_PRIVATE);
        try { posts=new JSONArray(prefs.getString("posts","[]")); } catch(JSONException e){posts=new JSONArray();}
        if(!prefs.getBoolean("seeded",false)){
            seed("첫 드라이브, 어디로 떠나고 싶으세요?","차를 받으면 가장 먼저 가보고 싶은 곳을 나눠주세요. 이 글은 체험용 예시입니다.","오너 생활");
            seed("인도 전 준비, 함께 체크해요","홈의 인도 준비 카드에서 개인 체크리스트를 사용해보세요. 구체적인 일정과 서류는 본인의 공식 안내를 확인해주세요.","구매·인도 Q&A");
            seed("우리 동네 오너들과 커피 한 잔","지역 모임 게시판의 예시 글입니다. 실제 모집 중인 모임은 아닙니다.","지역 모임");
            save(); prefs.edit().putBoolean("seeded",true).apply();
        }
        if(b!=null){tab=b.getString("tab","홈"); detail=b.getInt("detail",-1);filter=b.getString("filter","전체");query=b.getString("query","");}
        render();
    }
    @Override public void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b); b.putString("tab",tab); b.putInt("detail",detail); b.putString("filter",filter);b.putString("query",query);}
    void seed(String title,String text,String category){posts.put(post(title,text,category,true));}
    JSONObject post(String title,String text,String category,boolean sample){
        JSONObject p=new JSONObject();
        try{p.put("title",title);p.put("text",text);p.put("category",category);p.put("author",sample?"라운지 가이드":prefs.getString("nickname","새로운 오너"));p.put("sample",sample);p.put("liked",false);p.put("comments",new JSONArray());}catch(JSONException ignored){}
        return p;
    }
    void save(){prefs.edit().putString("posts",posts.toString()).apply();}
    GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable(); d.setColor(color);d.setCornerRadius(dp(24));return d;}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("kern");t.setLineSpacing(dp(3),1);t.setPadding(0,dp(5),0,dp(5));return t;}
    void heading(LinearLayout target,String value){TextView t=text(value,23,ink);t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));t.setPadding(0,dp(14),0,dp(12));target.addView(t);}
    Button button(String label,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(15);b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));b.setTextColor(ink);b.setMinHeight(dp(52));b.setPadding(dp(16),dp(12),dp(16),dp(12));b.setStateListAnimator(null);b.setElevation(0);b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x14000000),bg(0xFFF2F4F6),null));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(6),0,dp(6));b.setLayoutParams(lp);b.setOnClickListener(v->action.run());return b;}
    Button primary(String label,Runnable action){Button b=button(label,action);b.setTextColor(Color.WHITE);b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22FFFFFF),bg(accent),null));return b;}
    LinearLayout card(LinearLayout parent){LinearLayout l=column();l.setPadding(dp(22),dp(20),dp(22),dp(20));l.setBackground(bg(Color.WHITE));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(6),0,dp(8));parent.addView(l,lp);return l;}
    EditText input(String hint,int max,boolean multi){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(0xFF8B95A1);e.setBackground(bg(0xFFF2F4F6));e.setTextColor(ink);e.setTextSize(16);e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)});e.setSingleLine(!multi);e.setPadding(dp(16),dp(16),dp(16),dp(16));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(8),0,dp(8));e.setLayoutParams(lp);if(multi){e.setMinLines(4);e.setGravity(Gravity.TOP);e.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);}return e;}
    void go(String name){tab=name;detail=-1;render();}
    int group(){return Math.max(0,Math.min(2,prefs.getInt("group",0)));}
    String[] tabs(){
        if(group()==0)return new String[]{"홈","뉴스","주식","커뮤니티","MY"};
        return new String[]{"홈","뉴스·주식","커뮤니티","서비스",group()==2?"내 차":"MY"};
    }
    void render(){
        root=column();root.setBackgroundColor(paper);setContentView(root);
        root.setOnApplyWindowInsetsListener((v,i)->{root.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());return i;});root.requestApplyInsets();
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(24),dp(6),dp(24),dp(8));
        TextView logo=text("teslounge",23,ink);logo.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));logo.setLetterSpacing(-0.04f);header.addView(logo,new LinearLayout.LayoutParams(0,-2,1));
        Button profile=button("MY",()->go("MY"));profile.setTextSize(11);profile.setMinWidth(0);profile.setMinimumWidth(0);profile.setPadding(0,0,0,0);profile.setMinHeight(0);profile.setMinimumHeight(0);profile.setBackground(bg(Color.WHITE));header.addView(profile,new LinearLayout.LayoutParams(dp(44),dp(44)));root.addView(header);
        ScrollView scroll=new ScrollView(this);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);body=column();body.setPadding(dp(20),dp(4),dp(20),dp(28));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if(detail>=0 && detail<posts.length())showPost(detail);
        else if(tab.equals("홈"))home();else if(tab.equals("커뮤니티"))community();else if(tab.equals("구매·인도"))guide();
        else if(tab.equals("뉴스")||tab.equals("뉴스·주식"))news();else if(tab.equals("주식"))stock();else if(tab.equals("서비스"))services();else if(tab.equals("내 차"))vehicle();else garage();
        LinearLayout nav=new LinearLayout(this);nav.setBackgroundColor(Color.WHITE);nav.setPadding(dp(8),dp(8),dp(8),dp(4));
        for(String name:tabs()){boolean selected=tab.equals(name)||(tab.equals("주식")&&name.equals("뉴스·주식"));Button b=button(name,()->go(name));b.setTextSize(10);b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(2),dp(6),dp(2),dp(6));b.setTextColor(selected?ink:0xFF8B95A1);b.setBackground(bg(Color.WHITE));LoungeIcon icon=new LoungeIcon(name,selected?ink:0xFFADB5BD);icon.setBounds(0,0,dp(23),dp(23));b.setCompoundDrawables(null,icon,null,null);b.setCompoundDrawablePadding(dp(5));nav.addView(b,new LinearLayout.LayoutParams(0,dp(60),1));}root.addView(nav);
    }
    void home(){
        TextView greeting=text("반가워요, "+prefs.getString("nickname","새로운 오너")+"님",14,muted);greeting.setPadding(dp(4),dp(12),0,dp(4));body.addView(greeting);
        TextView title=text("더 즐거운 테슬라 라이프",26,ink);title.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));title.setPadding(dp(4),0,0,dp(18));body.addView(title);
        LinearLayout welcome=card(body);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView label=text(groups[group()]+"의 라운지",14,muted);top.addView(label,new LinearLayout.LayoutParams(0,-2,1));
        TextView badge=text("함께하는 일상",10,accent);badge.setPadding(dp(10),dp(5),dp(10),dp(5));badge.setBackground(bg(0xFFFFF0F0));top.addView(badge);welcome.addView(top);
        TextView headline=text(group()==2?"내 차와 나, 더 가까이":group()==1?"첫 만남을 기다리는 설렘":"테슬라가 궁금한 모든 순간",22,ink);headline.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));welcome.addView(headline);
        welcome.addView(new LoungeCarView(this),new LinearLayout.LayoutParams(-1,dp(112)));
        if(group()==2){welcome.addView(text("차량 연결은 준비 중이에요",14,muted));welcome.addView(primary("내 차 보기",()->go("내 차")));}
        else if(group()==1){welcome.addView(text("인도 준비부터 차근차근 함께해요",14,muted));welcome.addView(primary("출고 준비하기",()->go("구매·인도")));}
        else{welcome.addView(text("나에게 맞는 이야기부터 만나보세요",14,muted));welcome.addView(primary("관심 설정하기",()->go("MY")));}
        LinearLayout shortcuts=new LinearLayout(this);shortcuts.setPadding(0,dp(8),0,dp(12));
        String[] names={"새 소식","이야기","인도 준비"};String[] targets={group()==0?"뉴스":"뉴스·주식","커뮤니티","구매·인도"};String[] icons={"뉴스","커뮤니티","서비스"};
        for(int i=0;i<names.length;i++){final String target=targets[i];Button quick=button(names[i],()->go(target));quick.setTextSize(13);quick.setMinWidth(0);quick.setMinimumWidth(0);quick.setPadding(dp(4),dp(15),dp(4),dp(15));quick.setBackground(bg(Color.WHITE));LoungeIcon icon=new LoungeIcon(icons[i],i==0?0xFF5794E8:i==1?0xFF8B7AE6:0xFF49A896);icon.setBounds(0,0,dp(27),dp(27));quick.setCompoundDrawables(null,icon,null,null);quick.setCompoundDrawablePadding(dp(9));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(90),1);lp.setMargins(i==0?0:dp(8),0,0,0);shortcuts.addView(quick,lp);}body.addView(shortcuts);
        LinearLayout g=card(body);g.addView(text("첫 드라이브 준비",13,muted));TextView guideTitle=text("인도 준비, 하나씩",20,ink);guideTitle.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));g.addView(guideTitle);g.addView(text("준비한 만큼, 첫날이 더 편해져요",14,muted));g.addView(button("내 체크리스트 열기",()->go("구매·인도")));
        heading(body,"라운지의 새로운 이야기");for(int i=posts.length()-1;i>=Math.max(0,posts.length()-3);i--)postCard(body,i);
        body.addView(text("오프라인 체험판 · 작성 내용은 이 기기에만 저장돼요\nTesla와 독립적으로 운영되는 커뮤니티입니다",11,muted));
    }
    void postCard(LinearLayout parent,int id){JSONObject p=posts.optJSONObject(id);if(p==null)return;LinearLayout c=card(parent);c.addView(text(p.optString("category")+(p.optBoolean("sample")?"  ·  예시":""),12,accent));TextView title=text(p.optString("title"),19,ink);title.setTypeface(null,Typeface.BOLD);c.addView(title);TextView excerpt=text(p.optString("text"),14,muted);excerpt.setMaxLines(2);excerpt.setEllipsize(TextUtils.TruncateAt.END);c.addView(excerpt);JSONArray comments=p.optJSONArray("comments");c.addView(text(p.optString("author")+"   ·   공감 "+(p.optBoolean("liked")?1:0)+"   댓글 "+(comments==null?0:comments.length()),12,muted));c.setContentDescription(p.optString("title")+", 글 열기");c.setFocusable(true);c.setOnClickListener(v->{detail=id;render();});}
    void community(){
        heading(body,"함께 나누는 이야기");body.addView(primary("＋ 이야기 쓰기",this::compose));
        if(group()!=0)body.addView(button("출고 센터 · 인도 체크리스트",()->go("구매·인도")));
        EditText search=input("제목이나 내용 검색",100,false);search.setText(query);body.addView(search);
        Spinner category=new Spinner(this);category.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,categories));for(int i=0;i<categories.length;i++)if(categories[i].equals(filter))category.setSelection(i);body.addView(category);
        list=column();body.addView(list);refreshPosts();
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int before,int count){query=s.toString();refreshPosts();}public void afterTextChanged(Editable e){}});
        category.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int pos,long id){filter=categories[pos];refreshPosts();}});
    }
    void refreshPosts(){list.removeAllViews();for(int i=posts.length()-1;i>=0;i--){JSONObject p=posts.optJSONObject(i);if(p==null)continue;if(!filter.equals("전체")&&!filter.equals(p.optString("category")))continue;if(!(p.optString("title")+p.optString("text")).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))continue;postCard(list,i);}if(list.getChildCount()==0)list.addView(text("아직 이야기가 없어요. 첫 글을 남겨보세요.",16,muted));}
    void compose(){
        LinearLayout form=column();form.setPadding(dp(20),dp(8),dp(20),dp(8));EditText title=input("제목 (80자 이내)",80,false),content=input("함께 나누고 싶은 이야기 (3,000자)",3000,true);Spinner cat=new Spinner(this);cat.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{categories[1],categories[2],categories[3]}));form.addView(cat);form.addView(title);form.addView(content);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("새로운 이야기").setView(form).setNegativeButton("취소",null).setPositiveButton("기기에 저장",null).create();dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{if(title.getText().toString().trim().isEmpty()){title.setError("제목을 입력해주세요");return;}if(content.getText().toString().trim().isEmpty()){content.setError("내용을 입력해주세요");return;}posts.put(post(title.getText().toString().trim(),content.getText().toString().trim(),cat.getSelectedItem().toString(),false));save();dialog.dismiss();detail=posts.length()-1;render();}));dialog.show();
    }
    void showPost(int id){
        JSONObject p=posts.optJSONObject(id);body.addView(button("← 목록으로",()->{detail=-1;render();}));body.addView(text(p.optString("category")+(p.optBoolean("sample")?" · 예시":""),13,accent));heading(body,p.optString("title"));body.addView(text(p.optString("author"),13,muted));body.addView(text(p.optString("text"),18,ink));
        body.addView(button(p.optBoolean("liked")?"♥ 공감 취소":"♡ 공감하기",()->{try{p.put("liked",!p.optBoolean("liked"));save();render();}catch(JSONException ignored){}}));
        JSONArray comments=p.optJSONArray("comments");heading(body,"댓글 "+(comments==null?0:comments.length()));if(comments!=null)for(int i=0;i<comments.length();i++){JSONObject c=comments.optJSONObject(i);if(c!=null){LinearLayout row=card(body);row.addView(text(c.optString("author"),12,muted));row.addView(text(c.optString("text"),16,ink));}}
        EditText comment=input("따뜻한 댓글을 남겨주세요",500,true);body.addView(comment);body.addView(button("댓글 저장",()->{String value=comment.getText().toString().trim();if(value.isEmpty()){comment.setError("댓글을 입력해주세요");return;}try{JSONArray a=p.optJSONArray("comments");if(a==null)a=new JSONArray();JSONObject c=new JSONObject();c.put("author",prefs.getString("nickname","새로운 오너"));c.put("text",value);a.put(c);p.put("comments",a);save();render();}catch(JSONException ignored){}}));
        if(!p.optBoolean("sample"))body.addView(button("내 글 삭제",()->new AlertDialog.Builder(this).setMessage("이 기기에 저장된 글을 삭제할까요?").setNegativeButton("취소",null).setPositiveButton("삭제",(d,w)->{posts.remove(id);save();detail=-1;render();}).show()));
    }
    void guide(){
        heading(body,"첫 만남을 준비해요");body.addView(text("나만의 인도 체크리스트",17,muted));
        String[] items={"공식 안내에서 인도 일정과 장소 확인","결제 및 등록 진행 상황 확인","보험 가입과 적용 시점 확인","안내받은 준비 서류 확인","인도장 이동 계획 세우기","차량 외관·실내 함께 확인","앱 연결과 기본 사용법 확인"};
        int complete=0;for(int i=0;i<items.length;i++)if(prefs.getBoolean("check"+i,false))complete++;
        TextView progress=text(complete+" / "+items.length+" 완료",22,accent);body.addView(progress);
        for(int i=0;i<items.length;i++){final int index=i;CheckBox box=new CheckBox(this);box.setText(items[i]);box.setTextSize(16);box.setTextColor(ink);box.setMinHeight(dp(60));box.setChecked(prefs.getBoolean("check"+i,false));box.setOnCheckedChangeListener((b,checked)->{prefs.edit().putBoolean("check"+index,checked).apply();int n=0;for(int j=0;j<items.length;j++)if(prefs.getBoolean("check"+j,false))n++;progress.setText(n+" / "+items.length+" 완료");});body.addView(box);}
        body.addView(text("개인 준비를 돕는 참고 목록입니다. 실제 일정·결제·서류는 본인에게 전달된 Tesla 공식 안내를 확인해주세요.",13,muted));
    }
    void garage(){
        heading(body,"MY");body.addView(text("당신의 테슬라 이야기를 소개해주세요.",16,muted));
        LinearLayout audience=card(body);audience.addView(text("나는 지금",13,muted));Spinner selectedGroup=new Spinner(this);selectedGroup.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,groups));selectedGroup.setSelection(group());audience.addView(selectedGroup);
        audience.addView(text("유형에 따라 홈과 하단 메뉴가 바뀝니다. 오너 선택은 차량 인증을 의미하지 않습니다.",13,muted));
        LinearLayout c=card(body);c.addView(text("닉네임",13,muted));EditText nick=input("닉네임 (20자)",20,false);nick.setText(prefs.getString("nickname","새로운 오너"));c.addView(nick);c.addView(text("관심 모델",13,muted));Spinner model=new Spinner(this);model.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,models));model.setSelection(prefs.getInt("model",0));c.addView(model);c.addView(primary("프로필 저장",()->{String value=nick.getText().toString().trim();if(value.isEmpty()){nick.setError("닉네임을 입력해주세요");return;}prefs.edit().putString("nickname",value).putInt("model",model.getSelectedItemPosition()).putInt("group",selectedGroup.getSelectedItemPosition()).apply();Toast.makeText(this,"프로필을 저장했어요",Toast.LENGTH_SHORT).show();go("홈");}));
        body.addView(text("테슬라운지 0.2 · 오프라인 체험판\n회원가입과 차량 연결 기능은 아직 포함되지 않았습니다.",13,muted));
    }
    void unavailable(String title,String description){LinearLayout c=card(body);c.addView(text("연동 준비 중",12,accent));heading(c,title);c.addView(text(description,16,muted));}
    void news(){
        heading(body,"오늘의 테슬라");body.addView(text("뉴스 · 데이터 뉴스 · FSD 모아보기",15,muted));
        unavailable("새로운 소식을 기다려요","뉴스 공급원이 아직 연결되지 않았습니다. 원문 출처와 기준일을 확인할 수 있는 뉴스 피드를 준비합니다.");
        if(tab.equals("뉴스·주식"))body.addView(button("TSLA 주식 보기",()->go("주식")));
    }
    void stock(){
        heading(body,"TSLA");unavailable("시세 미연결","현재 가격과 차트는 제공하지 않습니다. 데이터 공급원과 표시 권한을 확보한 뒤 시세 기준 시각과 함께 제공합니다.");
        if(group()!=0)body.addView(button("뉴스로 돌아가기",()->go("뉴스·주식")));
    }
    void services(){
        heading(body,"내 차를 위한 서비스");
        body.addView(text("틴팅 · PPF · 신차패키지 · 세차 · 사고수리",16,muted));
        unavailable("아직 등록된 업체가 없어요","업체 목록과 예약 서버는 준비 중입니다. 현재 앱에서 예약이나 견적 요청은 접수되지 않습니다.");
        body.addView(button("인도 준비 체크리스트",()->go("구매·인도")));
    }
    void vehicle(){
        heading(body,"내 차");unavailable("연결된 차량이 없어요","Tesla 계정 연결은 아직 지원하지 않습니다. Fleet API 연동 후 차량 상태를 표시합니다. 현재 차량 제어 명령은 전송하지 않습니다.");
        body.addView(button("내 프로필 설정",()->go("MY")));
    }
    @Override public void onBackPressed(){if(detail>=0){detail=-1;render();}else if(!tab.equals("홈"))go("홈");else super.onBackPressed();}
}
