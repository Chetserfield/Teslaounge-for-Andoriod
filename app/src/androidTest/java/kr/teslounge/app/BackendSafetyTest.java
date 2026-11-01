package kr.teslounge.app;

import android.content.Context;
import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class BackendSafetyTest {
    @Test public void sessionIsEncryptedAndCanBeRemoved() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();SessionVault vault=new SessionVault(context);
        String marker="test-token-never-store-plaintext";
        try {
            vault.write("test_only",new JSONObject().put("access_token",marker));
            assertEquals(marker,vault.read("test_only").getString("access_token"));
            assertFalse(context.getSharedPreferences("teslounge_auth",Context.MODE_PRIVATE).getString("test_only","").contains(marker));
            vault.remove("test_only");assertFalse(vault.read("test_only").has("access_token"));
        } finally {vault.remove("test_only");}
    }
    @Test public void callbackMustMatchExactAppRouteWithoutTokenFragment() {
        assertTrue(BackendClient.isCallback(Uri.parse("kr.teslounge.app://auth/callback?code=example")));
        assertFalse(BackendClient.isCallback(Uri.parse("https://attacker.example/callback?code=example")));
        assertFalse(BackendClient.isCallback(Uri.parse("kr.teslounge.app://auth/other?code=example")));
        assertFalse(BackendClient.isCallback(Uri.parse("kr.teslounge.app://auth/callback#access_token=example")));
    }
    @Test public void callbackWithoutPendingLoginCannotCreateSession() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        BackendClient api=BackendClient.get(context);
        assertThrows(BackendClient.ApiError.class,()->api.completeLogin(Uri.parse("kr.teslounge.app://auth/callback?code=unrequested")));
    }
}
