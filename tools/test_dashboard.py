#!/usr/bin/env python3
"""Builds a local HTML dashboard from the JUnit results Gradle already writes.

  python3 tools/test_dashboard.py          # read the latest results, open the dashboard
  python3 tools/test_dashboard.py --run    # run the tests first (./gradlew), then do the above
  python3 tools/test_dashboard.py --serve  # live page at http://localhost:8765 with a Re-run tests button
  python3 tools/test_dashboard.py --no-open

Also shows: a stale-tests warning, classes with no test, and a robot checklist
(tools/robot_checklist.json; sign-offs are saved in build/test-dashboard/checklist.json, --serve only).

Output: build/test-dashboard/index.html (build/ is git-ignored). Run history for the
trend line and streak lives next to it in history.json.
"""
import argparse, glob, json, os, re, subprocess, sys, threading, webbrowser
from http.server import BaseHTTPRequestHandler, HTTPServer
import xml.etree.ElementTree as ET
from datetime import datetime

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "TeamCode/build/test-results/testDebugUnitTest")
OUT = os.path.join(ROOT, "build/test-dashboard")
PKG_PREFIX = "org.firstinspires.ftc.teamcode."


def describe(full_class):
    """{method: description}: the Javadoc or // comment right above @Test, else the name as a sentence."""
    path = os.path.join(SRC, "test/java", full_class.replace(".", "/") + ".java")
    try:
        src = open(path).read()
    except OSError:
        return {}
    out = {}
    for m in re.finditer(r"((?:[ \t]*(?://[^\n]*|/\*(?:(?!\*/).)*\*/)\s*)*)@Test[^\n]*\s+(?:@\w+[^\n]*\s+)*public\s+void\s+(\w+)", src, re.S):
        text = re.sub(r"^\s*(?:/\*+|\*/|\*|//)+ ?", "", m.group(1), flags=re.M)
        text = re.sub(r"\s*\*/", "", text)
        text = " ".join(l.strip() for l in text.splitlines() if l.strip() and not re.match(r"[-=]{3,}", l.strip()))
        out[m.group(2)] = text
    return out


def sentence(name):
    words = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", " ", name).lower()
    return words[:1].upper() + words[1:] + "."


def load():
    classes = []
    for path in sorted(glob.glob(os.path.join(RESULTS, "TEST-*.xml"))):
        suite = ET.parse(path).getroot()
        tests = []
        docs = describe(suite.get("name"))
        for tc in suite.findall("testcase"):
            bad = tc.find("failure") if tc.find("failure") is not None else tc.find("error")
            status = "fail" if bad is not None else "skip" if tc.find("skipped") is not None else "pass"
            msg = ""
            if bad is not None:
                msg = (bad.get("message") or "") + "\n" + (bad.text or "")
            tests.append({"name": tc.get("name"), "status": status,
                          "desc": docs.get(tc.get("name")) or sentence(tc.get("name")),
                          "time": float(tc.get("time") or 0), "msg": msg.strip()[:1500]})
        full = suite.get("name")
        short = full[len(PKG_PREFIX):] if full.startswith(PKG_PREFIX) else full
        parts = short.split(".")
        classes.append({"name": parts[-1], "area": ".".join(parts[:-1]) or "(root)", "tests": tests})
    return classes


def update_history(classes):
    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, "history.json")
    try:
        hist = json.load(open(path))
    except (OSError, ValueError):
        hist = []
    n = sum(len(c["tests"]) for c in classes)
    p = sum(t["status"] == "pass" for c in classes for t in c["tests"])
    hist.append({"at": datetime.now().strftime("%b %d %H:%M"), "total": n, "passed": p})
    hist = hist[-30:]
    json.dump(hist, open(path, "w"))
    return hist


SRC = os.path.join(ROOT, "TeamCode/src")
CHECKLIST = os.path.join(ROOT, "tools/robot_checklist.json")
CHECK_STATE = os.path.join(OUT, "checklist.json")
SKIP_DIRS = ("/test/", "/examples/", "/legacy/")


def java_files(sub):
    return glob.glob(os.path.join(SRC, sub, "**/*.java"), recursive=True)


def freshness():
    xmls = glob.glob(os.path.join(RESULTS, "TEST-*.xml"))
    ran = max(os.path.getmtime(f) for f in xmls)
    code = max(os.path.getmtime(f) for f in java_files("main") + java_files("test"))
    return {"stale": code > ran, "stale_min": int((code - ran) // 60) if code > ran else 0,
            "ran": datetime.fromtimestamp(ran).strftime("%b %d %H:%M")}


def coverage():
    # ponytail: "covered" = some test file mentions the class name; not line coverage (add JaCoCo if needed)
    tests = " ".join(open(f).read() for f in java_files("test"))
    out = []
    for f in java_files("main/java/org/firstinspires/ftc/teamcode/common"):
        name = os.path.basename(f)[:-5]
        if name == "package-info" or any(d in f for d in SKIP_DIRS):
            continue
        area = os.path.dirname(f).split("/common")[-1].strip("/") or "common"
        out.append({"name": name, "area": area, "covered": bool(re.search(r"\b%s\b" % name, tests))})
    return sorted(out, key=lambda c: (c["area"], c["name"]))


def read_state():
    try:
        return json.load(open(CHECK_STATE))
    except (OSError, ValueError):
        return {}


def checklist():
    return {"items": json.load(open(CHECKLIST)), "done": read_state()}


def save_check(body):
    """{"id","by"} signs an item off, {"id","clear":true} undoes it, {"reset":true} starts over."""
    state = read_state()
    if body.get("reset"):
        state = {}
    elif body.get("id") in {i["id"] for i in json.load(open(CHECKLIST))}:
        if body.get("clear"):
            state.pop(body["id"], None)
        else:
            state[body["id"]] = {"by": str(body.get("by") or "?")[:40], "at": datetime.now().strftime("%b %d %H:%M")}
    os.makedirs(OUT, exist_ok=True)
    json.dump(state, open(CHECK_STATE, "w"))


PAGE = """<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>BioBuzz Test Hive</title>
<style>
:root{--bg:#14110b;--card:#1f1a10;--line:#3a3220;--ink:#fff6dc;--dim:#b8a982;--gold:#ffc83d;--ok:#5fe08a;--bad:#ff5d5d;--skip:#8a8a8a}
*{box-sizing:border-box}body{margin:0;background:radial-gradient(circle at 20% -10%,#3a2c08 0,var(--bg) 55%);color:var(--ink);font:16px/1.4 ui-rounded,system-ui,sans-serif;min-height:100vh}
main{max-width:1100px;margin:0 auto;padding:24px 16px 64px}
h1{margin:0;font-size:clamp(28px,5vw,44px)}h1 span{color:var(--gold)}
.sub{color:var(--dim);margin:4px 0 24px}
.hero{display:flex;flex-wrap:wrap;gap:24px;align-items:center;background:var(--card);border:1px solid var(--line);border-radius:20px;padding:24px}
.ring{position:relative;width:180px;height:180px}.ring svg{transform:rotate(-90deg)}
.ring b{position:absolute;inset:0;display:grid;place-items:center;font-size:40px}
.stats{display:flex;flex-wrap:wrap;gap:12px;flex:1;min-width:260px}
.stat{flex:1 1 110px;background:#2a2314;border-radius:14px;padding:12px 16px}
.stat b{display:block;font-size:30px}.stat small{color:var(--dim)}
.banner{flex-basis:100%;font-size:20px;font-weight:700}
.xp{flex-basis:100%}.xp div{height:10px;background:#2a2314;border-radius:9px;overflow:hidden}.xp i{display:block;height:100%;background:linear-gradient(90deg,var(--gold),#ff9d2e);transition:width 1.2s}
.xp small{color:var(--dim)}
h2{margin:32px 0 12px;font-size:20px}
.bar{display:flex;gap:8px;flex-wrap:wrap;margin:24px 0 0}
.bar input{flex:1 1 200px;background:var(--card);border:1px solid var(--line);color:var(--ink);border-radius:12px;padding:10px 14px;font:inherit}
.bar button{background:var(--card);border:1px solid var(--line);color:var(--ink);border-radius:12px;padding:10px 14px;font:inherit;cursor:pointer;min-height:44px}
.bar button.on{background:var(--gold);color:#201800;border-color:var(--gold)}
#rerun{background:var(--ok)!important;color:#06240f!important;border-color:var(--ok)!important;font-weight:700}#rerun:disabled{opacity:.6;cursor:wait}
.warn{margin-top:16px;background:#4a3200;border:1px solid var(--gold);border-radius:14px;padding:12px 16px;font-weight:700}
.sec{background:var(--card);border:1px solid var(--line);border-radius:16px;padding:16px}.sec small{color:var(--dim)}
.chips{display:flex;flex-wrap:wrap;gap:8px}.chip{background:#2a2314;border:1px solid var(--bad);border-radius:20px;padding:4px 12px;font-size:14px}
.cov div{display:flex;align-items:center;gap:10px;margin:6px 0;font-size:14px}.cov span{flex:0 0 130px}.cov u{flex:1;height:10px;background:#2a2314;border-radius:6px;overflow:hidden}.cov i{display:block;height:100%;background:var(--ok)}
.chk h4{margin:14px 0 4px;color:var(--gold);font-size:14px}
.chk button.item{display:flex;gap:10px;align-items:center;width:100%;text-align:left;background:none;border:0;color:var(--ink);font:inherit;padding:8px 4px;min-height:44px;cursor:pointer;border-radius:10px}
.chk button.item:hover:not(:disabled){background:#2a2314}.chk button.item:disabled{cursor:default}
.chk b{flex:none;width:24px;height:24px;border-radius:7px;border:2px solid var(--dim);display:grid;place-items:center;color:#06240f}.chk .ok b{background:var(--ok);border-color:var(--ok)}
.chk em{margin-left:auto;color:var(--dim);font-size:12px;font-style:normal;padding-left:8px}
#chk-reset{background:var(--card);border:1px solid var(--line);color:var(--ink);border-radius:12px;padding:8px 14px;font:inherit;cursor:pointer;min-height:44px}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:12px}
.cls{min-width:0;background:var(--card);border:1px solid var(--line);border-radius:16px;padding:14px;cursor:pointer;transition:transform .15s}
.cls:hover{transform:translateY(-3px)}.cls.bad{border-color:var(--bad)}
.cls h3{margin:0;font-size:16px;display:flex;justify-content:space-between;gap:8px}
.cls .a{color:var(--dim);font-size:13px}
.dots{display:flex;flex-wrap:wrap;gap:4px;margin:10px 0}
.dots i{width:14px;height:14px;border-radius:50%;background:var(--ok);animation:pop .4s backwards}
.dots i.fail{background:var(--bad)}.dots i.skip{background:var(--skip)}
@keyframes pop{from{transform:scale(0)}}
.list{display:none;font-size:14px;border-top:1px solid var(--line);padding-top:8px}.cls.open .list{display:block}
.list div{display:flex;justify-content:space-between;gap:8px;padding:2px 0}.list div span:first-child{flex:1;min-width:0;overflow-wrap:anywhere}.list div span:last-child{flex:none;white-space:nowrap}.list .fail{color:var(--bad)}.list .skip{color:var(--skip)}
.list pre{white-space:pre-wrap;color:var(--bad);font-size:12px;margin:4px 0 8px}
.slow div{display:flex;align-items:center;gap:10px;margin:6px 0;font-size:14px}.slow span{flex:0 0 40%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.slow i{display:block;height:12px;border-radius:6px;background:linear-gradient(90deg,var(--gold),#ff9d2e)}
.spark{width:100%;height:80px}
canvas#fx{position:fixed;inset:0;pointer-events:none}
@media(prefers-reduced-motion:reduce){*{animation:none!important;transition:none!important}}
</style></head><body><main>
<h1>Bio<span>Buzz</span> Test Hive</h1>
<p class="sub" id="sub"></p>
<section class="hero">
 <div class="ring"><svg width="180" height="180"><circle cx="90" cy="90" r="78" fill="none" stroke="#2a2314" stroke-width="16"/><circle id="arc" cx="90" cy="90" r="78" fill="none" stroke="var(--ok)" stroke-width="16" stroke-linecap="round" stroke-dasharray="490" stroke-dashoffset="490" style="transition:stroke-dashoffset 1.2s"/></svg><b id="pct">0%</b></div>
 <div class="stats">
  <div class="banner" id="banner"></div>
  <div class="stat"><b id="n-pass">0</b><small>passing</small></div>
  <div class="stat"><b id="n-fail">0</b><small>failing</small></div>
  <div class="stat"><b id="n-skip">0</b><small>skipped</small></div>
  <div class="stat"><b id="n-streak">0</b><small>green runs in a row</small></div>
  <div class="xp"><div><i id="xp" style="width:0"></i></div><small id="lvl"></small></div>
 </div>
</section>
<div class="warn" id="stale" hidden></div>
<h2>Robot checklist</h2>
<div class="sec chk"><div id="chk-sum" style="font-weight:700"></div><div id="chk-body"></div>
 <p><small id="chk-note"></small> <button id="chk-reset" hidden>Start a new checklist</button></p></div>
<div class="bar"><input id="q" placeholder="Search tests..." aria-label="Search tests"><button data-f="all" class="on">All</button><button data-f="fail">Failing</button><button id="rerun" hidden>&#8635; Re-run tests</button></div>
<h2>Test classes <small style="color:var(--dim);font-weight:400">(click one to open it)</small></h2>
<div id="areas"></div>
<h2>Classes with no test</h2><div class="sec"><div id="gaps"></div><div class="cov" id="cov"></div></div>
<h2>Slowest tests</h2><div class="slow" id="slow"></div>
<h2>Last runs</h2><svg class="spark" id="spark" viewBox="0 0 300 80" preserveAspectRatio="none" role="img" aria-label="Pass rate over recent runs"></svg>
</main><canvas id="fx"></canvas>
<script>
const D=__DATA__, H=__HIST__, GEN="__GEN__", FR=__FRESH__, COV=__COV__;
let CK=__CHK__;
const LIVE=location.protocol.startsWith("http");
const all=D.flatMap(c=>c.tests.map(t=>({...t,cls:c.name})));
const cnt=s=>all.filter(t=>t.status===s).length, P=cnt("pass"), F=cnt("fail"), S=cnt("skip"), N=all.length;
const $=id=>document.getElementById(id), esc=s=>s.replace(/[&<>]/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;"}[c]));
function up(el,to){const t0=performance.now();(function f(t){const k=Math.min(1,(t-t0)/1000);el.textContent=Math.round(to*k);if(k<1)requestAnimationFrame(f)})(t0)}
$("sub").textContent=`${D.length} classes, ${N} tests. Dashboard built ${GEN}`;
const rate=N?P/N:0;
setTimeout(()=>{$("arc").style.strokeDashoffset=490*(1-rate);$("xp").style.width=(N%25)*4+"%"},50);
if(F)$("arc").style.stroke="var(--bad)";
up($("n-pass"),P);up($("n-fail"),F);up($("n-skip"),S);
let c=0;for(let i=H.length-1;i>=0&&H[i].passed===H[i].total;i--)c++;
$("n-streak").textContent=c;
$("pct").textContent=Math.round(rate*100)+"%";
$("lvl").textContent=`Hive level ${Math.floor(N/25)+1}: ${25-N%25} more tests to level up`;
$("banner").textContent=!N?"No results yet. Run the tests first!":F?`${F} test${F>1?"s":""} need${F>1?"":"s"} attention`:"All green. The hive is buzzing!";
let filter="all";
function render(){
 const q=$("q").value.toLowerCase(), areas={};
 D.forEach(cl=>{
  const ts=cl.tests.filter(t=>(filter==="all"||t.status==="fail")&&(!q||(cl.name+t.name).toLowerCase().includes(q)));
  if(!ts.length)return;(areas[cl.area]=areas[cl.area]||[]).push({cl,ts});});
 $("areas").innerHTML=Object.keys(areas).sort().map(a=>`<h2 style="font-size:16px;color:var(--dim)">${esc(a)}</h2><div class="grid">`+areas[a].map(({cl,ts})=>{
  const bad=ts.some(t=>t.status==="fail");
  return `<div class="cls ${bad?"bad open":""}" onclick="this.classList.toggle('open')"><h3>${esc(cl.name)}<span>${ts.filter(t=>t.status==="pass").length}/${ts.length}</span></h3>
  <div class="dots">${ts.map((t,i)=>`<i class="${t.status}" title="${esc(t.desc)}" style="animation-delay:${i*25}ms"></i>`).join("")}</div>
  <div class="list">${ts.map(t=>`<div class="${t.status}" title="${esc(t.desc)}"><span>${esc(t.name)}</span><span>${(t.time*1000).toFixed(0)} ms</span></div>${t.msg?`<pre>${esc(t.msg)}</pre>`:""}`).join("")}</div></div>`}).join("")+"</div>").join("")||"<p>Nothing matches.</p>";
}
$("q").oninput=render;
document.querySelectorAll(".bar button[data-f]").forEach(b=>b.onclick=()=>{filter=b.dataset.f;document.querySelectorAll(".bar button[data-f]").forEach(x=>x.classList.toggle("on",x===b));render()});
render();
if(LIVE){const b=$("rerun");b.hidden=false;b.onclick=async()=>{b.disabled=true;b.textContent="Running tests...";
 try{const r=await fetch("/rerun",{method:"POST"});if(!r.ok)throw 0;location.reload()}catch(e){b.disabled=false;b.textContent="Server is off. Start it: python3 tools/test_dashboard.py --serve"}}}
if(FR.stale){const s=$("stale");s.hidden=false;s.textContent=`Heads up: code changed ${FR.stale_min?FR.stale_min+" min":"a moment"} after the last test run (${FR.ran}). Re-run the tests!`}
const gaps=COV.filter(c=>!c.covered);
$("gaps").innerHTML=gaps.length?`<p><small>${gaps.length} of ${COV.length} classes are never mentioned in a test. Pick one and write its test!</small></p><div class="chips">${gaps.map(c=>`<span class="chip">${esc(c.area)}/${esc(c.name)}</span>`).join("")}</div>`:"<p>Every class has a test. Nice!</p>";
const byArea={};COV.forEach(c=>{(byArea[c.area]=byArea[c.area]||[]).push(c)});
$("cov").innerHTML="<p></p>"+Object.keys(byArea).map(a=>{const l=byArea[a],k=l.filter(c=>c.covered).length;return `<div><span>${esc(a)}</span><u><i style="width:${k/l.length*100}%"></i></u>${k}/${l.length}</div>`}).join("");
function post(body){return fetch("/check",{method:"POST",body:JSON.stringify(body)}).then(r=>{if(!r.ok)throw 0;return r.json()}).then(j=>{CK=j;drawChk()}).catch(()=>alert("Could not save. Start the server: python3 tools/test_dashboard.py --serve"))}
function drawChk(){
 const it=CK.items,d=CK.done,n=it.filter(i=>d[i.id]).length,groups={};
 it.forEach(i=>(groups[i.group]=groups[i.group]||[]).push(i));
 $("chk-sum").textContent=n===it.length?"Checklist complete. Robot is ready!":`${n} of ${it.length} checks done`;
 $("chk-body").innerHTML=Object.keys(groups).map(g=>`<h4>${esc(g)}</h4>`+groups[g].map(i=>`<button class="item ${d[i.id]?"ok":""}" data-id="${i.id}" ${LIVE?"":"disabled"}><b>${d[i.id]?"&#10003;":""}</b>${esc(i.text)}${d[i.id]?`<em>${esc(d[i.id].by)}, ${esc(d[i.id].at)}</em>`:""}</button>`).join("")).join("");
 let me="";try{me=localStorage.getItem("who")||""}catch(e){}
 $("chk-note").innerHTML=LIVE?`Tick a check once you have really tested it on the robot. Click again to undo. ${me?`Signing off as <span style="color:var(--gold)">${esc(me)}</span> <a href="#" id="chk-who" style="color:var(--gold)">(not you?)</a>`:"You will be asked your name once."}`:"Open with --serve to tick items off.";
 const w=$("chk-who");if(w)w.onclick=e=>{e.preventDefault();getWho(true);drawChk()};
 $("chk-reset").hidden=!LIVE||!n;
 $("chk-body").querySelectorAll("button").forEach(b=>b.onclick=()=>{
  const id=b.dataset.id;if(CK.done[id])return post({id,clear:true});
  const who=getWho();if(who)post({id,by:who})})}
function getWho(change){
 let w="";try{w=localStorage.getItem("who")||""}catch(e){}
 if(!w||change){w=(prompt("Your name?",w)||w||"").trim();try{localStorage.setItem("who",w)}catch(e){}}
 return w}
$("chk-reset").onclick=()=>{if(confirm("Clear every check and start a new checklist?"))post({reset:true})};
drawChk();
const slowest=[...all].sort((a,b)=>b.time-a.time).slice(0,6), mx=Math.max(...slowest.map(t=>t.time),0.001);
$("slow").innerHTML=slowest.map(t=>`<div><span>${esc(t.cls)}.${esc(t.name)}</span><i style="width:${Math.max(2,t.time/mx*50)}%"></i>${(t.time*1000).toFixed(0)} ms</div>`).join("");
if(H.length>1){const pts=H.map((h,i)=>`${i/(H.length-1)*300},${75-(h.total?h.passed/h.total:0)*70}`).join(" ");
 $("spark").innerHTML=`<polyline points="${pts}" fill="none" stroke="#ffc83d" stroke-width="2" vector-effect="non-scaling-stroke"/>`}
else $("spark").outerHTML="<p class='sub'>Run it again to start a trend line.</p>";
if(N&&!F&&!matchMedia("(prefers-reduced-motion:reduce)").matches){
 const cv=$("fx"),x=cv.getContext("2d");cv.width=innerWidth;cv.height=innerHeight;
 const ps=Array.from({length:140},()=>({x:Math.random()*cv.width,y:-20-Math.random()*cv.height*.5,v:2+Math.random()*3,r:3+Math.random()*4,h:35+Math.random()*30}));
 let fr=0;(function f(){x.clearRect(0,0,cv.width,cv.height);ps.forEach(p=>{p.y+=p.v;p.x+=Math.sin(p.y/30);x.fillStyle=`hsl(${p.h} 95% 60%)`;x.beginPath();x.arc(p.x,p.y,p.r,0,7);x.fill()});if(++fr<240)requestAnimationFrame(f);else x.clearRect(0,0,cv.width,cv.height)})();}
</script></body></html>"""


GRADLE = ["./gradlew", ":TeamCode:cleanTestDebugUnitTest", ":TeamCode:testDebugUnitTest", "--continue"]


def build():
    classes = load()
    if not classes:
        sys.exit("No results in %s. Run with --run (or run the Gradle test task) first." % RESULTS)
    hist = update_history(classes)
    page = (PAGE.replace("__DATA__", json.dumps(classes).replace("</", "<\\/"))
                .replace("__HIST__", json.dumps(hist))
                .replace("__FRESH__", json.dumps(freshness()))
                .replace("__COV__", json.dumps(coverage()))
                .replace("__CHK__", json.dumps(checklist()).replace("</", "<\\/"))
                .replace("__GEN__", datetime.now().strftime("%b %d, %H:%M")))
    out = os.path.join(OUT, "index.html")
    open(out, "w").write(page)
    return out


def serve(port):
    lock = threading.Lock()

    class Handler(BaseHTTPRequestHandler):
        def log_message(self, *a):
            pass

        def reply(self, code, body=b"", ctype="text/plain"):
            self.send_response(code)
            self.send_header("Content-Type", ctype)
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)

        def do_GET(self):
            if self.path.split("?")[0] not in ("/", "/index.html"):
                return self.reply(404)
            self.reply(200, open(build(), "rb").read(), "text/html; charset=utf-8")

        def do_POST(self):
            if self.path == "/check":
                try:
                    save_check(json.loads(self.rfile.read(int(self.headers.get("Content-Length", 0))) or b"{}"))
                except ValueError:
                    return self.reply(400)
                return self.reply(200, json.dumps(checklist()).encode(), "application/json")
            if self.path != "/rerun":
                return self.reply(404)
            with lock:  # one Gradle run at a time
                subprocess.run(GRADLE, cwd=ROOT)
            self.reply(200, b"ok")

    # 127.0.0.1 only: the Re-run endpoint must not be reachable from the network
    srv = HTTPServer(("127.0.0.1", port), Handler)
    url = "http://localhost:%d" % port
    print("Dashboard at %s (Ctrl+C to stop)" % url)
    webbrowser.open(url)
    try:
        srv.serve_forever()
    except KeyboardInterrupt:
        pass


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--run", action="store_true", help="run the unit tests first")
    ap.add_argument("--serve", action="store_true", help="serve a live page with a Re-run tests button")
    ap.add_argument("--port", type=int, default=8765)
    ap.add_argument("--no-open", action="store_true")
    a = ap.parse_args()
    if a.run:
        subprocess.run(GRADLE, cwd=ROOT)
    if a.serve:
        return serve(a.port)
    out = build()
    print("Wrote", out)
    if not a.no_open:
        webbrowser.open("file://" + out)


if __name__ == "__main__":
    main()
