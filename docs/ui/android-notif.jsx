import { useState } from "react";

const T = {
  bg: "#080808", card: "#0e0e0e", border: "#1e1e1e", subtle: "#141414",
  textPrimary: "#f0ebe3", textSecondary: "#555", textMuted: "#2e2e2e",
  green: "#4caf7d", red: "#e05c3a", font: "system-ui",
};

// ── Android system chrome colours ───────────────────────────────────────────
const Android = {
  wallpaper: "#1a1a2e",
  notifBg: "#1e1e1e",
  notifBgExpanded: "#191919",
  notifAction: "#2a2a2a",
  notifText: "#e8e8e8",
  notifSub: "#999",
  notifDivider: "#2e2e2e",
  appIcon: T.red,
  statusBar: "#111",
};

// Small app icon
function AppIcon({size=18}){
  return (
    <div style={{width:size,height:size,borderRadius:size*0.28,background:Android.appIcon,display:"flex",alignItems:"center",justifyContent:"center",flexShrink:0}}>
      <span style={{color:"#fff",fontSize:size*0.55,fontWeight:700,lineHeight:1}}>₹</span>
    </div>
  );
}

// Status bar
function StatusBar(){
  return (
    <div style={{background:Android.statusBar,padding:"10px 16px 6px",display:"flex",justifyContent:"space-between",alignItems:"center"}}>
      <span style={{color:Android.notifSub,fontSize:11}}>9:41</span>
      <div style={{display:"flex",gap:6,alignItems:"center"}}>
        <svg width="14" height="10" viewBox="0 0 14 10"><rect x="0" y="3" width="2" height="7" rx="1" fill="#999"/><rect x="3" y="2" width="2" height="8" rx="1" fill="#999"/><rect x="6" y="1" width="2" height="9" rx="1" fill="#ccc"/><rect x="9" y="0" width="2" height="10" rx="1" fill="#ccc"/><rect x="12" y="0" width="2" height="10" rx="1" fill="#ccc"/></svg>
        <svg width="12" height="10" viewBox="0 0 24 18"><path d="M12 4C8 4 4.5 5.8 2 8.5L0 6.5C3 3.2 7.3 1 12 1s9 2.2 12 5.5l-2 2C19.5 5.8 16 4 12 4z" fill="#ccc"/><path d="M12 8c-2.5 0-4.8 1-6.5 2.7L3.5 8.7C5.7 6.4 8.7 5 12 5s6.3 1.4 8.5 3.7l-2 2C16.8 9 14.5 8 12 8z" fill="#ccc"/><circle cx="12" cy="15" r="3" fill="#ccc"/></svg>
        <div style={{display:"flex",gap:1,alignItems:"center"}}>
          <div style={{width:20,height:10,border:"1.5px solid #999",borderRadius:2,padding:"1px 1px",display:"flex",alignItems:"center"}}>
            <div style={{width:"75%",height:"100%",background:"#ccc",borderRadius:1}}/>
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Notification A: Collapsed (default) ─────────────────────────────────────
function NotifCollapsed(){
  const [dismissed,setDismissed]=useState(false);
  if(dismissed) return <div style={{color:T.textSecondary,fontSize:12,textAlign:"center",padding:"12px 0"}}>Dismissed</div>;
  return (
    <div style={{background:Android.notifBg,borderRadius:16,overflow:"hidden",marginBottom:2}}>
      <div style={{padding:"12px 14px",display:"flex",gap:10,alignItems:"flex-start"}}>
        <AppIcon/>
        <div style={{flex:1,minWidth:0}}>
          <div style={{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:2}}>
            <span style={{color:Android.notifSub,fontSize:11,letterSpacing:0.3}}>Paisa · now</span>
            <div onClick={()=>setDismissed(true)} style={{cursor:"pointer",color:Android.notifSub,fontSize:16,lineHeight:1,padding:"0 2px"}}>×</div>
          </div>
          <div style={{color:Android.notifText,fontSize:13,fontWeight:500,marginBottom:2}}>3 new transactions</div>
          <div style={{color:Android.notifSub,fontSize:12}}>2 auto-categorised · 1 needs review</div>
        </div>
      </div>
      {/* Action buttons */}
      <div style={{display:"flex",borderTop:`1px solid ${Android.notifDivider}`}}>
        <div style={{flex:1,padding:"10px 14px",color:Android.notifSub,fontSize:12,cursor:"pointer",textAlign:"center"}}>Dismiss</div>
        <div style={{width:1,background:Android.notifDivider}}/>
        <div style={{flex:1,padding:"10px 14px",color:T.red,fontSize:12,fontWeight:600,cursor:"pointer",textAlign:"center"}}>Review</div>
      </div>
    </div>
  );
}

// ── Notification B: Expanded (long-press / pull down) ───────────────────────
function NotifExpanded(){
  const txns=[
    {merchant:"Blinkit", amount:640, suggested:"Food"},
    {merchant:"PhonePe", amount:200, suggested:null},
  ];
  const [dismissed,setDismissed]=useState(false);
  if(dismissed) return <div style={{color:T.textSecondary,fontSize:12,textAlign:"center",padding:"12px 0"}}>Dismissed</div>;
  return (
    <div style={{background:Android.notifBgExpanded,borderRadius:16,overflow:"hidden",marginBottom:2}}>
      <div style={{padding:"12px 14px 10px",display:"flex",gap:10,alignItems:"flex-start"}}>
        <AppIcon/>
        <div style={{flex:1,minWidth:0}}>
          <div style={{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:2}}>
            <span style={{color:Android.notifSub,fontSize:11}}>Paisa · now</span>
            <div onClick={()=>setDismissed(true)} style={{cursor:"pointer",color:Android.notifSub,fontSize:16,lineHeight:1,padding:"0 2px"}}>×</div>
          </div>
          <div style={{color:Android.notifText,fontSize:13,fontWeight:500,marginBottom:1}}>3 new transactions</div>
          <div style={{color:Android.notifSub,fontSize:12}}>1 needs your attention</div>
        </div>
      </div>
      {/* Expanded content */}
      <div style={{margin:"0 14px 10px",background:Android.notifAction,borderRadius:10,overflow:"hidden"}}>
        {txns.map((t,i)=>(
          <div key={t.merchant}>
            <div style={{padding:"9px 12px",display:"flex",alignItems:"center",justifyContent:"space-between"}}>
              <div>
                <div style={{color:Android.notifText,fontSize:12,fontWeight:500}}>{t.merchant}</div>
                <div style={{color:Android.notifSub,fontSize:11,marginTop:1}}>₹{t.amount} · {t.suggested?`Suggested: ${t.suggested}`:"Needs review"}</div>
              </div>
              {t.suggested
                ? <div style={{color:T.green,fontSize:10,border:`1px solid ${T.green}44`,background:`${T.green}11`,padding:"3px 8px",borderRadius:10}}>✓ {t.suggested}</div>
                : <div style={{color:T.red,fontSize:10,border:`1px solid ${T.red}44`,background:`${T.red}11`,padding:"3px 8px",borderRadius:10}}>? Review</div>
              }
            </div>
            {i<txns.length-1&&<div style={{height:1,background:Android.notifDivider,margin:"0 12px"}}/>}
          </div>
        ))}
      </div>
      {/* Actions */}
      <div style={{display:"flex",borderTop:`1px solid ${Android.notifDivider}`}}>
        <div style={{flex:1,padding:"10px 14px",color:Android.notifSub,fontSize:12,cursor:"pointer",textAlign:"center"}}>Dismiss</div>
        <div style={{width:1,background:Android.notifDivider}}/>
        <div style={{flex:1,padding:"10px 14px",color:T.red,fontSize:12,fontWeight:600,cursor:"pointer",textAlign:"center"}}>Review</div>
      </div>
    </div>
  );
}

// ── Notification C: All categorised (no action needed) ──────────────────────
function NotifAllGood(){
  const [dismissed,setDismissed]=useState(false);
  if(dismissed) return <div style={{color:T.textSecondary,fontSize:12,textAlign:"center",padding:"12px 0"}}>Dismissed</div>;
  return (
    <div style={{background:Android.notifBg,borderRadius:16,overflow:"hidden",marginBottom:2}}>
      <div style={{padding:"12px 14px",display:"flex",gap:10,alignItems:"center"}}>
        <AppIcon/>
        <div style={{flex:1,minWidth:0}}>
          <div style={{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:2}}>
            <span style={{color:Android.notifSub,fontSize:11}}>Paisa · now</span>
            <div onClick={()=>setDismissed(true)} style={{cursor:"pointer",color:Android.notifSub,fontSize:16,lineHeight:1,padding:"0 2px"}}>×</div>
          </div>
          <div style={{color:Android.notifText,fontSize:13,fontWeight:500,marginBottom:2}}>4 transactions added</div>
          <div style={{color:T.green,fontSize:12}}>✓ All auto-categorised</div>
        </div>
      </div>
    </div>
  );
}

// ── Root ────────────────────────────────────────────────────────────────────
export default function App(){
  const SL=({c})=><div style={{color:T.textSecondary,fontSize:10,letterSpacing:3,marginBottom:10}}>{c}</div>;
  return (
    <div style={{background:Android.wallpaper,minHeight:"100vh",fontFamily:"system-ui",maxWidth:390,margin:"0 auto"}}>
      <StatusBar/>
      <div style={{padding:"20px 12px",display:"flex",flexDirection:"column",gap:24}}>
        <div>
          <SL c="COLLAPSED · DEFAULT VIEW"/>
          <NotifCollapsed/>
        </div>
        <div>
          <SL c="EXPANDED · LONG PRESS OR PULL DOWN"/>
          <NotifExpanded/>
        </div>
        <div>
          <SL c="ALL CATEGORISED · NO ACTION NEEDED"/>
          <NotifAllGood/>
        </div>
      </div>
    </div>
  );
}
