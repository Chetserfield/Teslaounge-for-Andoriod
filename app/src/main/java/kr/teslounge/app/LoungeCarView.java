package kr.teslounge.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original, decorative electric-car illustration. No vehicle data is implied. */
final class LoungeCarView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    LoungeCarView(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    protected void onDraw(Canvas c){
        super.onDraw(c);c.save();float scale=Math.min(getWidth()/320f,getHeight()/112f);c.translate((getWidth()-320*scale)/2,(getHeight()-112*scale)/2);c.scale(scale,scale);
        p.setColor(0xFFF2F4F7);c.drawOval(33,8,290,107,p);
        p.setShader(new RadialGradient(165,91,125,new int[]{0x22202D40,0x00202D40},null,Shader.TileMode.CLAMP));c.drawOval(26,80,306,105,p);p.setShader(null);
        Path body=new Path();body.moveTo(30,66);body.cubicTo(36,57,58,55,76,52);body.cubicTo(99,20,134,20,166,24);body.cubicTo(195,26,214,43,231,51);body.cubicTo(254,54,280,57,289,68);body.lineTo(291,84);body.quadTo(272,91,247,89);body.lineTo(67,89);body.quadTo(36,89,28,81);body.close();
        p.setShader(new LinearGradient(0,29,0,90,new int[]{0xFFEDF1F5,0xFFB5BFCB,0xFF748294},null,Shader.TileMode.CLAMP));c.drawPath(body,p);p.setShader(null);
        Path glass=new Path();glass.moveTo(87,51);glass.cubicTo(108,27,135,27,163,31);glass.cubicTo(184,33,200,43,212,52);glass.close();p.setColor(0xFF364254);c.drawPath(glass,p);
        p.setColor(0xFFCED7E1);p.setStrokeWidth(3);c.drawLine(151,30,155,52,p);
        p.setColor(0x55FFFFFF);p.setStrokeWidth(2);c.drawLine(73,58,241,61,p);
        for(int x:new int[]{78,242}){p.setColor(0xFF202833);c.drawCircle(x,84,17,p);p.setColor(0xFF99A5B5);c.drawCircle(x,84,10,p);p.setColor(0xFFDCE3EA);c.drawCircle(x,84,5,p);}
        p.setColor(0xFFE5484D);c.drawRoundRect(30,64,43,68,2,2,p);p.setColor(0xFFF5F8FF);c.drawRoundRect(271,64,286,68,2,2,p);
        p.setColor(0xFF738195);c.drawRoundRect(162,59,174,62,1,1,p);c.restore();
    }
}
