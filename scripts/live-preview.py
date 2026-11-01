"""Local, live view and touch bridge for the dedicated TESLounge emulator."""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import json
import secrets
import subprocess
import threading
import time

ROOT = Path(__file__).resolve().parents[2]
ADB = str(ROOT / '.android-tools/sdk/platform-tools/adb')
TOKEN = secrets.token_urlsafe(24)
PREFIX = '/' + TOKEN
PORT = 8765
lock = threading.Lock()
frame = b''
updated = 0

def adb(*args):
    return subprocess.run([ADB, '-s', 'emulator-5554', *args], capture_output=True, timeout=15, check=True).stdout

def capture():
    global frame, updated
    while True:
        try:
            with lock:
                data = adb('exec-out', 'screencap', '-p')
            if data.startswith(b'\x89PNG'):
                frame, updated = data, time.time()
        except (subprocess.SubprocessError, OSError):
            pass
        time.sleep(1)

HTML = '''<!doctype html><html lang="ko"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>TESLounge · 실시간 앱</title><style>
*{box-sizing:border-box}body{margin:0;background:#101114;color:#f5f5f7;font:14px system-ui,sans-serif}header{padding:16px 20px;border-bottom:1px solid #303239}h1{font-size:18px;margin:0 0 6px}#state{color:#b7bcc6;font-size:12px}main{display:flex;flex-direction:column;align-items:center;padding:12px;gap:10px}img{display:block;max-width:100%;height:calc(100dvh - 175px);object-fit:contain;cursor:pointer;touch-action:none;border-radius:12px;background:#222}nav{display:flex;gap:8px;flex-wrap:wrap;justify-content:center}button{background:#282b33;border:1px solid #424650;color:white;border-radius:8px;padding:10px 16px;cursor:pointer}p{font-size:12px;color:#b7bcc6;margin:0;text-align:center}
</style><header><h1>TESLounge <span style="color:#f46b60">LIVE</span></h1><div id="state">에뮬레이터 화면 연결 중…</div></header>
<main><img id="screen" alt="실행 중인 Android 앱 화면"><nav><button data-action="back">← 뒤로</button><button data-action="up">위로 스크롤</button><button data-action="down">아래로 스크롤</button></nav><p>화면을 클릭해 조작하세요 · 약 1~2초 간격 갱신<br>실제 Android 실행 화면 · 현재 앱은 오프라인 체험판</p></main>
<script>
const base=location.pathname.replace(/\\/$/,''), screen=document.querySelector('#screen'), state=document.querySelector('#state');
let pending=false;
async function refresh(){try{const r=await fetch(base+'/frame?t='+Date.now());if(!r.ok)throw Error();const b=await r.blob();const u=URL.createObjectURL(b);const old=screen.src;screen.src=u;if(old.startsWith('blob:'))URL.revokeObjectURL(old);state.textContent='● 연결됨 · 마지막 화면 '+new Date(Number(r.headers.get('X-Frame-Time'))*1000).toLocaleTimeString('ko-KR');}catch{state.textContent='연결 대기 중 · 에뮬레이터를 확인하고 있습니다';}finally{setTimeout(refresh,1200);}}
async function act(payload){if(pending)return;pending=true;try{const r=await fetch(base+'/action',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});if(!r.ok)state.textContent='조작을 처리하지 못했습니다';}finally{pending=false;}}
screen.onclick=e=>{const r=screen.getBoundingClientRect(), scale=Math.min(r.width/screen.naturalWidth,r.height/screen.naturalHeight),w=screen.naturalWidth*scale,h=screen.naturalHeight*scale;const x=(e.clientX-r.left-(r.width-w)/2)/scale,y=(e.clientY-r.top-(r.height-h)/2)/scale;if(x>=0&&y>=0&&x<screen.naturalWidth&&y<screen.naturalHeight)act({action:'tap',x:Math.round(x),y:Math.round(y)});};
document.querySelectorAll('button').forEach(b=>b.onclick=()=>act({action:b.dataset.action}));refresh();
</script></html>'''

class Handler(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def send(self, status, content, mime):
        self.send_response(status)
        self.send_header('Content-Type', mime)
        self.send_header('Content-Length', str(len(content)))
        self.send_header('Cache-Control', 'no-store')
        self.send_header('X-Frame-Time', str(updated))
        self.send_header('X-Content-Type-Options', 'nosniff')
        self.end_headers()
        self.wfile.write(content)

    def do_GET(self):
        path = self.path.split('?')[0].rstrip('/')
        if path == PREFIX:
            self.send(200, HTML.encode(), 'text/html; charset=utf-8')
        elif path == PREFIX + '/frame' and frame and time.time() - updated < 10:
            self.send(200, frame, 'image/png')
        else:
            self.send(404, b'Not ready or not found', 'text/plain')

    def do_POST(self):
        if (self.path != PREFIX + '/action'
            or self.headers.get('Origin') != f'http://127.0.0.1:{PORT}'
            or self.headers.get('Content-Type') != 'application/json'):
            self.send(403, b'Forbidden', 'text/plain'); return
        try:
            length = int(self.headers.get('Content-Length', '0'))
            if not 0 < length <= 512:
                raise ValueError()
            data = json.loads(self.rfile.read(length))
            action = data.get('action')
            with lock:
                if action == 'tap':
                    x, y = int(data['x']), int(data['y'])
                    if not 0 <= x < 1080 or not 0 <= y < 2400:
                        raise ValueError()
                    adb('shell', 'input', 'tap', str(x), str(y))
                elif action == 'back':
                    adb('shell', 'input', 'keyevent', '4')
                elif action in ('up', 'down'):
                    start, end = ('1800', '650') if action == 'down' else ('650', '1800')
                    adb('shell', 'input', 'swipe', '540', start, '540', end, '350')
                else:
                    raise ValueError()
            self.send(200, b'{}', 'application/json')
        except (ValueError, KeyError, TypeError, subprocess.SubprocessError):
            self.send(400, b'Invalid or unavailable action', 'text/plain')

if __name__ == '__main__':
    server = ThreadingHTTPServer(('127.0.0.1', PORT), Handler)
    threading.Thread(target=capture, daemon=True).start()
    url = f'http://127.0.0.1:{PORT}{PREFIX}'
    (ROOT / '.android-tools/live-preview-url.txt').write_text(url)
    print(url, flush=True)
    server.serve_forever()
