package kr.teslounge.app;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
/** Read-only tests against the configured project; no test accounts or posts created. */
public class ServerConnectionTest {
 @Test public void publicCommunityAndCareEndpointsRespond() throws Exception {
  assumeTrue(BackendClient.configured());
  BackendClient api=BackendClient.get(InstrumentationRegistry.getInstrumentation().getTargetContext());
  assertNotNull(api.rows("posts?select=id,title,profiles:profiles!posts_author_id_fkey(nickname)&limit=1"));
  assertNotNull(api.rows("comments?select=id,profiles(nickname)&limit=1"));
  assertNotNull(api.rows("providers?select=id,name&limit=1"));
  assertNotNull(api.rows("advertisements?select=id,providers(name)&limit=1"));
 }
}
