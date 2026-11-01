#!/usr/bin/env python3
"""Real deployed REST test. Uses two existing, confirmed TEST accounts, never admin keys."""
import os,json,uuid,urllib.request,urllib.error
url=os.environ['SUPABASE_URL'].rstrip('/')
key=os.environ['SUPABASE_PUBLISHABLE_KEY']
assert url.startswith('https://') and key.startswith('sb_publishable_')
def call(method,path,body=None,token=None):
    headers={'apikey':key,'Content-Type':'application/json','Prefer':'return=representation'}
    if token: headers['Authorization']='Bearer '+token
    request=urllib.request.Request(url+path,data=None if body is None else json.dumps(body).encode(),headers=headers,method=method)
    try:
        with urllib.request.urlopen(request,timeout=20) as response:
            value=response.read();return response.status,json.loads(value) if value else []
    except urllib.error.HTTPError as error:
        return error.code,None  # Do not log server responses containing account information.
def login(prefix):
    status,result=call('POST','/auth/v1/token?grant_type=password',{'email':os.environ[prefix+'_EMAIL'],'password':os.environ[prefix+'_PASSWORD']})
    assert status==200, 'Test-account sign in failed'
    return result['access_token'],result['user']['id']
a,aid=login('TEST_A');b,bid=login('TEST_B');assert aid!=bid
post=str(uuid.uuid4())
try:
    for token,uid in [(a,aid),(b,bid)]:
        status,rows=call('GET','/rest/v1/profiles?id=eq.'+uid,token=token);assert status==200
        if not rows:
            status,_=call('POST','/rest/v1/profiles',{'id':uid,'nickname':'서버 검증 계정'},token);assert status==201
    status,_=call('POST','/rest/v1/posts',{'id':post,'title':'공유 서버 검증','body':'자동 검증 후 삭제됩니다.','category':'오너 생활'},a);assert status==201
    status,rows=call('GET','/rest/v1/posts?id=eq.'+post,token=b);assert status==200 and len(rows)==1
    status,rows=call('PATCH','/rest/v1/posts?id=eq.'+post,{'title':'권한 없는 변경'},b);assert status==200 and rows==[]
    status,_=call('POST','/rest/v1/comments',{'post_id':post,'body':'다른 계정의 댓글'},b);assert status==201
    status,rows=call('GET','/rest/v1/comments?post_id=eq.'+post,token=a);assert status==200 and len(rows)==1
    status,_=call('POST','/rest/v1/comments',{'post_id':post,'body':'게스트 쓰기'});assert status in (401,403)
    status,_=call('PATCH','/rest/v1/posts?id=eq.'+post,{'hidden':True},a);assert status in (401,403)
    status,rows=call('DELETE','/rest/v1/posts?id=eq.'+post,token=b);assert status==200 and rows==[]
    status,rows=call('PATCH','/rest/v1/profiles?id=eq.'+aid,{'nickname':'타인 변경'},b);assert status==200 and rows==[]
    status,rows=call('GET','/rest/v1/comments?post_id=eq.'+post,token=a);assert status==200 and len(rows)==1
    comment=rows[0]['id']
    status,rows=call('PATCH','/rest/v1/comments?id=eq.'+comment,{'body':'타인 변경'},a);assert status==200 and rows==[]
    status,_=call('POST','/rest/v1/reactions',{'post_id':post},b);assert status==201
    status,_=call('POST','/rest/v1/reactions',{'post_id':post},b);assert status==409
    status,count=call('POST','/rest/v1/rpc/reaction_count',{'target':post},a);assert status==200 and count==1
    status,rows=call('GET','/rest/v1/reactions?post_id=eq.'+post,token=a);assert status==200 and rows==[]
    status,_=call('DELETE','/rest/v1/reactions?post_id=eq.'+post+'&user_id=eq.'+bid,token=b);assert status==200
    status,count=call('POST','/rest/v1/rpc/reaction_count',{'target':post},a);assert status==200 and count==0
    status,_=call('DELETE','/rest/v1/comments?id=eq.'+comment,token=b);assert status==200
    print('PASS: actual Auth login for two users, shared posts/comments, own deletes, cross-user edits/deletes blocked, guest writes blocked, moderation blocked, unique reactions and aggregate, reactor privacy.')
finally:
    status,_=call('DELETE','/rest/v1/posts?id=eq.'+post,token=a)
    if status not in (200,204):print('Cleanup failed: delete the test post in the dashboard.')
    call('POST','/auth/v1/logout?scope=local',token=a);call('POST','/auth/v1/logout?scope=local',token=b)
