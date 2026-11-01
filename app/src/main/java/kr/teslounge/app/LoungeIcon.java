package kr.teslounge.app;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Small original line icons, rendered crisply at the device density. */
final class LoungeIcon extends Drawable {
    private final String name;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    LoungeIcon(String name, int color) { this.name=name;paint.setColor(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.5f);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND); }
    public void draw(Canvas c) {
        c.save();c.translate(getBounds().left,getBounds().top);c.scale(getBounds().width()/24f,getBounds().height()/24f);
        Path p=new Path();
        if(name.equals("홈")){p.moveTo(3,10);p.lineTo(12,3);p.lineTo(21,10);p.moveTo(5,9);p.lineTo(5,21);p.lineTo(10,21);p.lineTo(10,15);p.lineTo(14,15);p.lineTo(14,21);p.lineTo(19,21);p.lineTo(19,9);c.drawPath(p,paint);}
        else if(name.equals("커뮤니티")){c.drawRoundRect(3,4,21,18,5,5,paint);p.moveTo(7,18);p.lineTo(6,22);p.lineTo(12,18);c.drawPath(p,paint);c.drawLine(8,10,16,10,paint);c.drawLine(8,14,13,14,paint);}
        else if(name.equals("주식")){c.drawLine(4,4,4,21,paint);c.drawLine(4,21,21,21,paint);p.moveTo(7,16);p.lineTo(12,11);p.lineTo(15,14);p.lineTo(21,6);c.drawPath(p,paint);}
        else if((name.equals("서비스")||name.equals("케어"))){for(int x=3;x<18;x+=11)for(int y=3;y<18;y+=11)c.drawRoundRect(x,y,x+7,y+7,2,2,paint);}
        else if(name.equals("MY")){c.drawCircle(12,7,4,paint);c.drawArc(4,13,20,28,180,180,false,paint);}
        else if(name.equals("내 차")){c.drawRoundRect(3,9,21,19,3,3,paint);p.moveTo(5,9);p.lineTo(8,4);p.lineTo(16,4);p.lineTo(19,9);c.drawPath(p,paint);c.drawLine(6,19,6,21,paint);c.drawLine(18,19,18,21,paint);c.drawLine(6,13,8,13,paint);c.drawLine(16,13,18,13,paint);}
        else{c.drawRoundRect(4,3,20,21,3,3,paint);c.drawLine(8,8,16,8,paint);c.drawLine(8,12,16,12,paint);c.drawLine(8,16,13,16,paint);}
        c.restore();
    }
    public void setAlpha(int alpha){paint.setAlpha(alpha);}
    public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);}
    public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
