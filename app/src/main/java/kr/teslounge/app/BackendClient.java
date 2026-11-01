package kr.teslounge.app;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.concurrent.*;

/** Serial I/O prevents competing refresh-token rotations. No credentials are logged. */
final class BackendClient {
    static final String CALLBACK="kr.teslounge.app://auth/callback";
    private static BackendClient instance;
    private final SessionVault vault;
    private final ExecutorService queue=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    interface Job<T>{T run() throws Exception;}
    interface Result<T>{void done(T value,String error);}
    static synchronized BackendClient get(Context context){if(instance==null)instance=new BackendClient(context.getApplicationContext());return instance;}
    private BackendClient(Context context){vault=new SessionVault(context);}
    static boolean configured(){return !BuildConfig.SUPABASE_URL.isEmpty()&&!BuildConfig.SUPABASE_KEY.isEmpty();}
    String userId(){return session().optString("user_id");}
    private JSONObject session(){JSONObject s=vault.read("session");return BuildConfig.SUPABASE_URL.equals(s.optString("project"))?s:new JSONObject();}
    <T> void async(Job<T> job,Result<T> result){queue.execute(()->{T value=null;String error=null;try{value=job.run();}catch(Exception e){error=e instanceof ApiError?e.getMessage():"연결하지 못했어요. 잠시 후 다시 시도해주세요.";}final T v=value;final String message=error;main.post(()->result.done(v,message));});}
    static final class ApiError extends IOException{ApiError(String message){super(message);}}
    String beginLogin() throws Exception {
        JSONObject settings=new JSONObject(request("GET","/auth/v1/settings",null,null));
        JSONObject providers=settings.optJSONObject("external");
        if(providers==null||!providers.optBoolean("kakao"))throw new ApiError("서버는 연결됐지만 카카오 가입은 아직 준비 중이에요.");
        byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);
        String verifier=Base64.encodeToString(bytes,Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);
        String challenge=Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)),Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);
        JSONObject pending=new JSONObject().put("verifier",verifier).put("created",System.currentTimeMillis()).put("project",BuildConfig.SUPABASE_URL);
        vault.write("pending",pending);
        return Uri.parse(BuildConfig.SUPABASE_URL+"/auth/v1/authorize").buildUpon().appendQueryParameter("provider","kakao")
            .appendQueryParameter("redirect_to",CALLBACK).appendQueryParameter("code_challenge",challenge).appendQueryParameter("code_challenge_method","s256").build().toString();
    }
    static boolean isCallback(Uri uri){return uri!=null&&"kr.teslounge.app".equals(uri.getScheme())&&"auth".equals(uri.getHost())&&"/callback".equals(uri.getPath())&&uri.getFragment()==null;}
    void completeLogin(Uri uri) throws Exception {
        JSONObject pending=vault.read("pending");vault.remove("pending");
        if(!isCallback(uri)||!BuildConfig.SUPABASE_URL.equals(pending.optString("project"))||pending.optString("verifier").isEmpty()||System.currentTimeMillis()-pending.optLong("created")>600000)
            throw new ApiError("로그인을 다시 시작해주세요.");
        String code=uri.getQueryParameter("code");if(code==null||code.isEmpty())throw new ApiError("카카오 로그인이 취소되었거나 완료되지 않았어요.");
        JSONObject response=new JSONObject(request("POST","/auth/v1/token?grant_type=pkce",new JSONObject().put("auth_code",code).put("code_verifier",pending.getString("verifier")),null));
        storeSession(response);
    }
    private void storeSession(JSONObject response) throws Exception {
        JSONObject safe=new JSONObject().put("access_token",response.getString("access_token")).put("refresh_token",response.getString("refresh_token"))
            .put("user_id",response.getJSONObject("user").getString("id")).put("expires",System.currentTimeMillis()+response.getLong("expires_in")*1000)
            .put("project",BuildConfig.SUPABASE_URL);
        vault.write("session",safe);
    }
    private String token() throws Exception {
        JSONObject s=session();if(s.optString("access_token").isEmpty())return null;
        if(s.optLong("expires")<System.currentTimeMillis()+60000){
            try{storeSession(new JSONObject(request("POST","/auth/v1/token?grant_type=refresh_token",new JSONObject().put("refresh_token",s.getString("refresh_token")),null)));}
            catch(ApiError error){vault.remove("session");throw new ApiError("로그인이 만료됐어요. 다시 로그인해주세요.");}
        }
        return session().optString("access_token");
    }
    void logout() throws Exception {
        try{String t=token();if(t!=null)request("POST","/auth/v1/logout?scope=local",null,t);}
        finally{vault.remove("session");vault.remove("pending");}
    }
    JSONArray rows(String tableQuery) throws Exception {return new JSONArray(request("GET","/rest/v1/"+tableQuery,null,token()));}
    JSONObject insert(String table,JSONObject value) throws Exception {
        String t=token();if(t==null)throw new ApiError("로그인이 필요해요.");
        JSONArray result=new JSONArray(request("POST","/rest/v1/"+table,value,t));return result.length()==0?new JSONObject():result.getJSONObject(0);
    }
    void delete(String query) throws Exception {String t=token();if(t==null)throw new ApiError("로그인이 필요해요.");request("DELETE","/rest/v1/"+query,null,t);}
    long reactionCount(String post) throws Exception {return Long.parseLong(request("POST","/rest/v1/rpc/reaction_count",new JSONObject().put("target",post),token()).trim());}
    private String request(String method,String path,JSONObject body,String token) throws Exception {
        if(!configured())throw new ApiError("서버 설정이 필요해요.");
        HttpURLConnection connection=(HttpURLConnection)new URL(BuildConfig.SUPABASE_URL+path).openConnection();
        connection.setInstanceFollowRedirects(false);connection.setConnectTimeout(12000);connection.setReadTimeout(15000);
        connection.setRequestMethod(method);connection.setRequestProperty("apikey",BuildConfig.SUPABASE_KEY);
        if(token!=null)connection.setRequestProperty("Authorization","Bearer "+token);
        connection.setRequestProperty("Content-Type","application/json");connection.setRequestProperty("Prefer","return=representation");
        try{
            if(body!=null){connection.setDoOutput(true);try(OutputStream out=connection.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
            int status=connection.getResponseCode();
            if(status<200||status>=300)throw new ApiError(status==401?"로그인이 만료됐어요. 다시 로그인해주세요.":status==403?"이 작업을 할 권한이 없어요.":status==409?"이미 처리됐거나 중복된 요청이에요. 새로고침해주세요.":status==429?"요청이 많아요. 잠시 후 다시 시도해주세요.":"요청을 처리하지 못했어요. 서버 설정과 입력 내용을 확인해주세요.");
            try(InputStream in=connection.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1){if(out.size()+n>2097152)throw new IOException("Response too large");out.write(buffer,0,n);}return out.size()==0?"[]":out.toString("UTF-8");
            }
        }finally{connection.disconnect();}
    }
}
