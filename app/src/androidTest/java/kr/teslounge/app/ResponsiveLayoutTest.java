package kr.teslounge.app;

import android.view.View;
import android.widget.FrameLayout;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResponsiveLayoutTest {
    @Test public void columnFitsNarrowAndUnfoldedWindows(){
        try(ActivityScenario<MainActivity> app=ActivityScenario.launch(MainActivity.class)){
            app.onActivity(a->{
                ResponsiveFrame frame=new ResponsiveFrame(a);
                View child=new View(a);frame.addView(child,new FrameLayout.LayoutParams(-1,-1));
                for(int width:new int[]{320,420,840,1100}){
                    frame.measure(View.MeasureSpec.makeMeasureSpec(a.dp(width),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(a.dp(700),View.MeasureSpec.EXACTLY));
                    frame.layout(0,0,a.dp(width),a.dp(700));
                    assertEquals(a.dp(Math.min(width,640)),child.getWidth());
                    assertTrue(child.getLeft()>=0);assertTrue(child.getRight()<=frame.getWidth());
                    assertTrue(Math.abs(child.getLeft()-(frame.getWidth()-child.getRight()))<=1);
                }
                a.go("케어");
            });
            app.recreate();app.onActivity(a->assertEquals("케어",a.tab));
        }
    }
    @Test public void liveDraftSurvivesWindowResize() throws Exception {
        android.app.Instrumentation instrumentation=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation();
        String original=shell(instrumentation,"wm size");
        String restore=original.contains("Override size:")?original.split("Override size:")[1].trim().split("\\s+")[0]:"reset";
        java.util.concurrent.atomic.AtomicReference<MainActivity> initial=new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<android.app.AlertDialog> dialog=new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<android.widget.EditText> draft=new java.util.concurrent.atomic.AtomicReference<>();
        try(ActivityScenario<MainActivity> app=ActivityScenario.launch(MainActivity.class)){
            try {
                app.onActivity(a->{initial.set(a);a.go("커뮤니티");android.widget.EditText edit=a.input("draft",80,false);edit.setText("접어도 유지되는 이야기");draft.set(edit);dialog.set(new android.app.AlertDialog.Builder(a).setView(edit).show());});
                for(String size:new String[]{"2208x1840","1080x2400"}){
                    shell(instrumentation,"wm size "+size);Thread.sleep(1200);instrumentation.waitForIdleSync();
                    app.onActivity(a->{assertSame(initial.get(),a);assertEquals("커뮤니티",a.tab);assertTrue(dialog.get().isShowing());assertEquals("접어도 유지되는 이야기",draft.get().getText().toString());});
                }
            } finally {app.onActivity(a->{if(dialog.get()!=null && dialog.get().getWindow().getDecorView().isAttachedToWindow())dialog.get().dismiss();});shell(instrumentation,"wm size "+restore);}
        }
    }
    private String shell(android.app.Instrumentation instrumentation,String command) throws Exception {
        try(android.os.ParcelFileDescriptor descriptor=instrumentation.getUiAutomation().executeShellCommand(command);
            java.io.FileInputStream input=new java.io.FileInputStream(descriptor.getFileDescriptor());
            java.io.ByteArrayOutputStream output=new java.io.ByteArrayOutputStream()){
            byte[] buffer=new byte[1024];int n;while((n=input.read(buffer))!=-1)output.write(buffer,0,n);
            return output.toString("UTF-8");
        }
    }
}
