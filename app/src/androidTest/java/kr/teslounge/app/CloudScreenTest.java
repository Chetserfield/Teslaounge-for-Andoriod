package kr.teslounge.app;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/** Exercises cloud screens without changing profiles, posting, or resetting user preferences. */
public class CloudScreenTest {
 private boolean contains(View view,String text){
  if(view instanceof TextView && ((TextView)view).getText().toString().contains(text))return true;
  if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)if(contains(((ViewGroup)view).getChildAt(i),text))return true;
  return false;
 }
 private void awaitText(ActivityScenario<MainActivity> app,String text) throws Exception {
  long deadline=System.currentTimeMillis()+20000;AtomicBoolean found=new AtomicBoolean();
  while(System.currentTimeMillis()<deadline){app.onActivity(a->found.set(contains(a.body,text)));if(found.get())return;Thread.sleep(100);}
  fail("Expected cloud screen text: "+text);
 }
 @Test public void communityAndCareLoadAndNavigationSurvivesRecreation() throws Exception {
  assumeTrue(BackendClient.configured());
  try(ActivityScenario<MainActivity> app=ActivityScenario.launch(MainActivity.class)){
   app.onActivity(a->a.go("커뮤니티"));awaitText(app,"회원들이 함께 보는 게시판");
   awaitText(app,"아직 이야기가 없어요");
   app.recreate();awaitText(app,"아직 이야기가 없어요");
   app.onActivity(a->a.go("케어"));awaitText(app,"내 차를 위한 케어");awaitText(app,"조건에 맞는 업체가 없어요");
   app.onActivity(a->a.go("MY"));awaitText(app,"게스트로 둘러보는 중");
  }
 }
}
