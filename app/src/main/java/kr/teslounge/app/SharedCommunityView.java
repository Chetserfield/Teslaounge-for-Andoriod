package kr.teslounge.app;

import android.app.AlertDialog;
import android.widget.*;
import org.json.*;
import java.util.UUID;

/** Server-only community. Never reads or uploads the offline demonstration posts. */
final class SharedCommunityView {
    private final MainActivity a;
    private final BackendClient api;
    private final LinearLayout body;
    private int generation=0;
    SharedCommunityView(MainActivity activity,LinearLayout body){a=activity;api=BackendClient.get(a);this.body=body;}
    private boolean active(){return !a.isFinishing()&&!a.isDestroyed()&&a.body==body;}
    private void error(String message){if(active())new AlertDialog.Builder(a).setMessage(message).setPositiveButton("확인",null).show();}
    void show(){
        final int load=++generation;body.removeAllViews();a.heading(body,"함께 나누는 이야기");
        body.addView(a.text("회원들이 함께 보는 게시판",12,a.muted));
        body.addView(a.primary("＋ 이야기 쓰기",this::write));
        body.addView(a.button("새로고침",this::show));
        LinearLayout feed=a.column();body.addView(feed);feed.addView(a.text("이야기를 불러오는 중이에요",14,a.muted));
        api.async(()->api.rows("posts?select=id,author_id,title,body,category,created_at,profiles:profiles!posts_author_id_fkey(nickname)&order=created_at.desc,id.desc&limit=100"),(rows,message)->{
            if(!active()||generation!=load)return;feed.removeAllViews();
            if(message!=null){feed.addView(a.text(message,14,a.muted));return;}
            if(rows.length()==0){feed.addView(a.text("아직 이야기가 없어요. 첫 글을 남겨보세요.",14,a.muted));return;}
            feed.addView(a.text("최근 이야기 · 최대 100개",12,a.muted));
            for(int i=0;i<rows.length();i++){
                JSONObject p=rows.optJSONObject(i);LinearLayout card=a.card(feed);
                card.addView(a.text(p.optString("category"),12,a.muted));card.addView(a.text(p.optString("title"),18,a.ink));
                TextView excerpt=a.text(p.optString("body"),14,a.muted);excerpt.setMaxLines(2);excerpt.setEllipsize(android.text.TextUtils.TruncateAt.END);card.addView(excerpt);
                card.addView(a.text(author(p),12,a.muted));card.setFocusable(true);card.setContentDescription(p.optString("title")+", 글 열기");card.setOnClickListener(v->detail(p));
            }
        });
    }
    private String author(JSONObject p){JSONObject profile=p.optJSONObject("profiles");return profile==null?"회원":profile.optString("nickname","회원");}
    private boolean login(){if(api.userId().isEmpty()){a.startKakaoLogin();return false;}return true;}
    private void withProfile(Runnable action){
        if(!login())return;
        final int load=++generation;
        api.async(()->api.rows("profiles?id=eq."+api.userId()+"&select=id"),(rows,message)->{
            if(!active()||generation!=load)return;
            if(message!=null){error(message);return;}
            if(rows.length()>0){action.run();return;}
            LinearLayout form=a.column();form.setPadding(a.dp(20),a.dp(8),a.dp(20),a.dp(8));
            form.addView(a.text("게시글에 표시할 닉네임을 정해주세요. 카카오 실명은 자동 공개하지 않아요.",14,a.muted));
            EditText nickname=a.input("공개 닉네임 (20자)",20,false);form.addView(nickname);
            AlertDialog dialog=new AlertDialog.Builder(a).setTitle("라운지 프로필").setView(form).setNegativeButton("취소",null).setPositiveButton("저장",null).create();
            dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{
                String name=nickname.getText().toString().trim();if(name.isEmpty()){nickname.setError("닉네임을 입력해주세요");return;}
                dialog.getButton(-1).setEnabled(false);
                api.async(()->api.insert("profiles",new JSONObject().put("id",api.userId()).put("nickname",name).put("audience",a.group())),(profile,err)->{
                    if(!active())return;dialog.getButton(-1).setEnabled(true);if(err!=null){error(err);return;}dialog.dismiss();action.run();
                });
            }));dialog.show();
        });
    }
    private void write(){withProfile(()->{
        LinearLayout form=a.column();form.setPadding(a.dp(20),a.dp(8),a.dp(20),a.dp(8));
        EditText title=a.input("제목 (80자 이내)",80,false),content=a.input("함께 나누고 싶은 이야기 (3,000자)",3000,true);
        Spinner category=new Spinner(a);category.setAdapter(new ArrayAdapter<>(a,android.R.layout.simple_spinner_dropdown_item,new String[]{a.categories[1],a.categories[2],a.categories[3]}));
        form.addView(category);form.addView(title);form.addView(content);form.addView(a.text("등록한 글은 다른 회원과 방문자에게 공개됩니다.",12,a.muted));
        String id=UUID.randomUUID().toString();
        AlertDialog dialog=new AlertDialog.Builder(a).setTitle("새로운 이야기").setView(form).setNegativeButton("취소",null).setPositiveButton("게시하기",null).create();
        dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{
            String t=title.getText().toString().trim(),b=content.getText().toString().trim();
            if(t.isEmpty()){title.setError("제목을 입력해주세요");return;}if(b.isEmpty()){content.setError("내용을 입력해주세요");return;}
            dialog.getButton(-1).setEnabled(false);
            api.async(()->api.insert("posts",new JSONObject().put("id",id).put("title",t).put("body",b).put("category",category.getSelectedItem().toString())),(p,message)->{
                if(!active())return;dialog.getButton(-1).setEnabled(true);if(message!=null){error(message);return;}dialog.dismiss();show();
            });
        }));dialog.show();
    });}
    private void detail(JSONObject p){
        final int load=++generation;body.removeAllViews();String id=p.optString("id");
        body.addView(a.button("← 목록으로",this::show));a.heading(body,p.optString("title"));body.addView(a.text(author(p),12,a.muted));body.addView(a.text(p.optString("body"),16,a.ink));
        LinearLayout comments=a.column();
        TextView count=a.text("공감을 불러오는 중",12,a.muted);body.addView(count);
        Button like=a.button("공감하기",()->{});like.setEnabled(false);body.addView(like);
        api.async(()->{
            JSONObject result=new JSONObject().put("count",api.reactionCount(id));
            return result.put("liked",!api.userId().isEmpty()&&api.rows("reactions?post_id=eq."+id+"&user_id=eq."+api.userId()).length()>0);
        },(result,message)->{
            if(!active()||generation!=load)return;
            if(message!=null){count.setText(message);return;}
            count.setText("공감 "+result.optLong("count"));boolean liked=result.optBoolean("liked");like.setText(liked?"공감 취소":"공감하기");like.setEnabled(true);
            like.setOnClickListener(v->withProfile(()->{like.setEnabled(false);api.async(()->{
                if(liked)api.delete("reactions?post_id=eq."+id+"&user_id=eq."+api.userId());else api.insert("reactions",new JSONObject().put("post_id",id));return true;
            },(ok,err)->{if(!active())return;if(err!=null){like.setEnabled(true);error(err);}else detail(p);});}));
        });
        a.heading(body,"댓글");body.addView(comments);comments.addView(a.text("댓글을 불러오는 중",14,a.muted));
        api.async(()->api.rows("comments?post_id=eq."+id+"&select=id,body,author_id,created_at,profiles(nickname)&order=created_at.asc,id.asc&limit=100"),(rows,message)->{
            if(!active()||generation!=load)return;comments.removeAllViews();
            if(message!=null){comments.addView(a.text(message,14,a.muted));return;}
            if(rows.length()==0)comments.addView(a.text("첫 댓글을 남겨보세요",14,a.muted));
            for(int i=0;i<rows.length();i++){JSONObject c=rows.optJSONObject(i);LinearLayout card=a.card(comments);card.addView(a.text(author(c),12,a.muted));card.addView(a.text(c.optString("body"),14,a.ink));
                if(c.optString("author_id").equals(api.userId()))card.addView(a.button("내 댓글 삭제",()->remove("comments?id=eq."+c.optString("id"),()->detail(p))));
            }
        });
        EditText comment=a.input("따뜻한 댓글을 남겨주세요",500,true);body.addView(comment);String commentId=UUID.randomUUID().toString();
        Button send=a.button("댓글 게시",()->{});body.addView(send);
        send.setOnClickListener(v->{String value=comment.getText().toString().trim();if(value.isEmpty()){comment.setError("댓글을 입력해주세요");return;}withProfile(()->{
            send.setEnabled(false);api.async(()->api.insert("comments",new JSONObject().put("id",commentId).put("post_id",id).put("body",value)),(c,message)->{
                if(!active())return;send.setEnabled(true);if(message!=null)error(message);else detail(p);
            });
        });});
        if(p.optString("author_id").equals(api.userId()))body.addView(a.button("내 글 삭제",()->remove("posts?id=eq."+id,this::show)));
    }
    private void remove(String query,Runnable done){new AlertDialog.Builder(a).setMessage("서버에 저장된 내용을 삭제할까요?").setNegativeButton("취소",null).setPositiveButton("삭제",(d,w)->api.async(()->{api.delete(query);return true;},(ok,message)->{if(!active())return;if(message!=null)error(message);else done.run();})).show();}
}
