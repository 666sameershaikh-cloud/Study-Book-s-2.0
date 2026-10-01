const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const APK_PATH = '/app/applet/.build-outputs/app-debug.apk';

const htmlContent = `<!DOCTYPE html>
<html lang="hi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>World Books • Direct APK Install</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
    body { background: #0B0F19; color: #F8FAFC; min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px 16px; }
    .card { background: #131B2E; border: 1px solid rgba(255,255,255,0.1); border-radius: 24px; max-width: 480px; width: 100%; padding: 32px 24px; text-align: center; box-shadow: 0 20px 40px rgba(0,0,0,0.5); }
    .logo { width: 80px; height: 80px; background: linear-gradient(135deg, #06B6D4, #4F46E5); border-radius: 20px; display: flex; align-items: center; justify-content: center; font-size: 38px; margin: 0 auto 20px auto; box-shadow: 0 10px 25px rgba(6,182,212,0.3); }
    h1 { font-size: 26px; font-weight: 800; color: #FFFFFF; margin-bottom: 8px; letter-spacing: -0.5px; }
    .badge { display: inline-block; background: rgba(6,182,212,0.15); color: #06B6D4; border: 1px solid rgba(6,182,212,0.3); padding: 4px 12px; border-radius: 999px; font-size: 13px; font-weight: 600; margin-bottom: 20px; }
    p.desc { font-size: 15px; color: #94A3B8; line-height: 1.5; margin-bottom: 28px; }
    .btn-download { display: flex; align-items: center; justify-content: center; gap: 10px; width: 100%; background: linear-gradient(135deg, #06B6D4, #2563EB); color: #FFFFFF; font-size: 17px; font-weight: 700; text-decoration: none; padding: 16px; border-radius: 16px; box-shadow: 0 10px 25px rgba(6,182,212,0.4); transition: transform 0.15s ease; }
    .btn-download:active { transform: scale(0.98); }
    .steps { text-align: left; background: #0B0F19; border: 1px solid rgba(255,255,255,0.06); border-radius: 16px; padding: 20px; margin-top: 28px; }
    .steps h3 { font-size: 15px; color: #06B6D4; margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
    .step-item { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 10px; font-size: 13px; color: #CBD5E1; line-height: 1.4; }
    .step-item:last-child { margin-bottom: 0; }
    .step-num { background: #1E293B; color: #38BDF8; width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; flex-shrink: 0; }
    .features { display: flex; justify-content: space-around; margin-top: 24px; padding-top: 20px; border-top: 1px solid rgba(255,255,255,0.06); font-size: 12px; color: #64748B; }
    .feat { display: flex; flex-direction: column; align-items: center; gap: 4px; }
    .feat span { font-size: 18px; }
  </style>
</head>
<body>
  <div class="card">
    <div class="logo">📚</div>
    <h1>World Books</h1>
    <div class="badge">Official Android APK • v1.0</div>
    <p class="desc">World Books application direct download & install karein bina kisi error ke.</p>

    <a href="/app-debug.apk" download="worldbooks.apk" class="btn-download">
      <span>📥</span>
      <span>Download World Books APK</span>
    </a>

    <div class="steps">
      <h3>📲 Install Karne Ke Steps</h3>
      <div class="step-item">
        <div class="step-num">1</div>
        <div>Upar <b>Download APK</b> button dabayein.</div>
      </div>
      <div class="step-item">
        <div class="step-num">2</div>
        <div>Download hone ke baad Notification ya Downloads folder me <b>worldbooks.apk</b> par tap karein.</div>
      </div>
      <div class="step-item">
        <div class="step-num">3</div>
        <div><b>"Install"</b> dabayein (agar prompt aaye to <i>"Allow from this source"</i> enable karein).</div>
      </div>
      <div class="step-item">
        <div class="step-num">4</div>
        <div>App open karein aur Books, Secret Chat aur Camera photo upload enjoy karein! 🎉</div>
      </div>
    </div>

    <div class="features">
      <div class="feat"><span>📖</span>Reader</div>
      <div class="feat"><span>🔒</span>Secret Chat</div>
      <div class="feat"><span>📸</span>Camera</div>
      <div class="feat"><span>⚡</span>Realtime</div>
    </div>
  </div>
</body>
</html>`;

const server = http.createServer((req, res) => {
  const url = req.url.split('?')[0];

  if (url === '/app-debug.apk' || url === '/worldbooks.apk' || url === '/download') {
    if (fs.existsSync(APK_PATH)) {
      const stat = fs.statSync(APK_PATH);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Length': stat.size,
        'Content-Disposition': 'attachment; filename="worldbooks.apk"',
        'Cache-Control': 'no-cache'
      });
      const readStream = fs.createReadStream(APK_PATH);
      readStream.pipe(res);
    } else {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('APK file is compiling. Please try again in a few seconds.');
    }
  } else {
    res.writeHead(200, {
      'Content-Type': 'text/html; charset=utf-8',
      'Cache-Control': 'no-cache'
    });
    res.end(htmlContent);
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Direct install server listening on port ${PORT}`);
});
