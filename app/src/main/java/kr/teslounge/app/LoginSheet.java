package kr.teslounge.app;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Entry UI only. Never creates a session or pretends that Kakao authenticated. */
final class LoginSheet {
    static void show(MainActivity activity, Runnable localPreview) {
        LinearLayout content=activity.column();
        content.setPadding(activity.dp(24),activity.dp(28),activity.dp(24),activity.dp(24));
        content.setBackground(activity.bg(activity.surface));
        content.addView(activity.text("TESLOUNGE",12,activity.accent));
        TextView title=activity.text("테슬라 이야기,\n함께 시작해요",28,activity.ink);
        title.setTypeface(null,Typeface.BOLD);content.addView(title);
        content.addView(activity.text("둘러보기는 자유롭게.\n나의 이야기는 가입하고 함께 나눠요.",15,activity.muted));
        LinearLayout benefits=activity.card(content);
        benefits.addView(activity.text("나만의 닉네임으로 참여",16,activity.ink));
        benefits.addView(activity.text("관심자부터 오너까지, 함께하는 라운지",13,activity.muted));
        benefits.addView(activity.text("Tesla 차량 연결은 별도로 진행해요",13,activity.muted));
        Button kakao=activity.button("카카오로 시작하기",()->new AlertDialog.Builder(activity)
            .setTitle("아직 카카오 계정에 연결되지 않았어요")
            .setMessage("카카오 가입 기능을 준비하고 있어요. 지금은 둘러보거나 기기 체험을 이용할 수 있습니다. 회원 계정은 생성되지 않습니다.")
            .setPositiveButton("확인",null).show());
        kakao.setTextColor(0xFF191919);kakao.setBackground(activity.bg(0xFFFEE500));content.addView(kakao);
        content.addView(activity.text("카카오 가입 연결 준비 중",12,activity.muted));
        ScrollView scroll=new ScrollView(activity);scroll.addView(content);
        AlertDialog dialog=new AlertDialog.Builder(activity).setView(scroll).create();
        content.addView(activity.button("먼저 둘러보기",dialog::dismiss));
        content.addView(activity.text("개발 체험",12,activity.accent));
        content.addView(activity.text("체험한 글·댓글은 이 기기에만 저장돼요.\n실제 회원가입이나 다른 회원과의 공유는 되지 않아요.",12,activity.muted));
        content.addView(activity.button("기기에만 저장하며 체험하기",()->{dialog.dismiss();localPreview.run();}));
        dialog.show();
        Window window=dialog.getWindow();
        if(window!=null){window.setBackgroundDrawableResource(android.R.color.transparent);window.setLayout(-1,-2);}
    }
}
