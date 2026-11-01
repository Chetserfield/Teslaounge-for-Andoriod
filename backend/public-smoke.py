#!/usr/bin/env python3
"""Public API and guest-permission smoke test; no privileged keys or real accounts."""
import json,uuid,urllib.request,urllib.error
from pathlib import Path
config=dict(x.split('=',1) for x in (Path(__file__).resolve().parents[1]/'backend.local.properties').read_text().splitlines() if '=' in x and not x.startswith('#'))
base=config['SUPABASE_URL'];key=config['SUPABASE_PUBLISHABLE_KEY']
def call(method,path,body=None):
    request=urllib.request.Request(base+path,data=None if body is None else json.dumps(body).encode(),headers={'apikey':key,'Content-Type':'application/json'},method=method)
    try:
        with urllib.request.urlopen(request,timeout=20) as response:
            raw=response.read();return response.status,json.loads(raw) if raw else None
    except urllib.error.HTTPError as error:
        return error.code,json.loads(error.read())
checks=[]
for query in ['posts?select=id,title,profiles:profiles!posts_author_id_fkey(nickname)&limit=1','comments?select=id,profiles(nickname)&limit=1','providers?select=id,name&limit=1','advertisements?select=id,providers(name)&limit=1']:
    status,rows=call('GET','/rest/v1/'+query);assert status==200 and isinstance(rows,list),(query,status);checks.append({'check':query,'status':status})
status,response=call('POST','/rest/v1/posts',{'id':str(uuid.uuid4()),'title':'권한 차단 검증','body':'게스트 요청은 거부되어야 합니다.','category':'오너 생활'})
assert status in (401,403) and response.get('code')=='42501',('Guest write unexpectedly permitted or wrong error',status)
checks.append({'check':'guest_post_denied','status':status,'code':response['code']})
status,response=call('GET','/rest/v1/reactions?limit=1');assert status in (401,403) and response.get('code')=='42501'
checks.append({'check':'guest_reactor_id_read_denied','status':status})
status,response=call('POST','/rest/v1/rpc/reaction_count',{'target':str(uuid.uuid4())});assert status==200 and response==0
checks.append({'check':'unknown_post_aggregate','status':status,'count':response})
status,response=call('GET','/auth/v1/settings');assert status==200
checks.append({'check':'kakao_configuration','enabled':response.get('external',{}).get('kakao')})
print(json.dumps({'project':base,'checks':checks},ensure_ascii=False,indent=2))
