import { useState, useMemo, useRef } from "react";

const rupee = "₹";

const T = {
  bg: "#080808", card: "#0e0e0e", border: "#1e1e1e", subtle: "#141414",
  textPrimary: "#f0ebe3", textSecondary: "#555", textMuted: "#2e2e2e",
  green: "#4caf7d", red: "#e05c3a", font: "system-ui",
};

const catColors = { Food:"#f97316", Transport:"#3b82f6", Bills:"#06b6d4", Shopping:"#ec4899", Cash:"#a855f7", Income:"#4caf7d" };
const ALL_CATEGORIES = ["Food","Transport","Bills","Shopping","Cash","Income"];
const ALL_TYPES = ["UPI","NEFT","ATM","IMPS","Credit Card","Cash"];
const ALL_BANKS = ["HDFC","SBI"];
const MONTHS = ["January","February","March","April","May","June","July","August","September","October","November","December"];
const DAYS = ["Su","Mo","Tu","We","Th","Fr","Sa"];

const allTxns = [
  {id:1, merchant:"Zomato",        category:"Food",      amount:-340,  date:"2026-06-06", time:"8:42 PM",  type:"UPI",  bank:"HDFC"},
  {id:13,merchant:"Blinkit",       category:null,        amount:-640,  date:"2026-06-06", time:"11:20 AM", type:"UPI",  bank:"HDFC"},
  {id:14,merchant:"PhonePe",       category:null,        amount:-200,  date:"2026-06-06", time:"9:05 AM",  type:"UPI",  bank:"SBI"},
  {id:2, merchant:"Salary Credit", category:"Income",    amount:60000, date:"2026-06-01", time:"9:00 AM",  type:"NEFT", bank:"HDFC"},
  {id:3, merchant:"Uber",          category:"Transport", amount:-180,  date:"2026-06-05", time:"6:15 PM",  type:"UPI",  bank:"SBI"},
  {id:4, merchant:"Swiggy",        category:"Food",      amount:-520,  date:"2026-06-04", time:"1:30 PM",  type:"UPI",  bank:"HDFC"},
  {id:5, merchant:"ATM Withdrawal",category:"Cash",      amount:-2000, date:"2026-06-03", time:"11:00 AM", type:"ATM",  bank:"SBI"},
  {id:6, merchant:"Amazon",        category:"Shopping",  amount:-1299, date:"2026-06-02", time:"3:20 PM",  type:"UPI",  bank:"HDFC"},
  {id:7, merchant:"Airtel",        category:"Bills",     amount:-499,  date:"2026-05-28", time:"10:00 AM", type:"UPI",  bank:"HDFC"},
  {id:8, merchant:"Swiggy",        category:"Food",      amount:-380,  date:"2026-05-25", time:"8:00 PM",  type:"UPI",  bank:"HDFC"},
  {id:9, merchant:"Salary Credit", category:"Income",    amount:60000, date:"2026-05-01", time:"9:00 AM",  type:"NEFT", bank:"HDFC"},
  {id:10,merchant:"Ola",           category:"Transport", amount:-220,  date:"2026-05-15", time:"7:30 PM",  type:"UPI",  bank:"SBI"},
  {id:11,merchant:"BigBasket",     category:"Shopping",  amount:-1840, date:"2026-05-10", time:"2:10 PM",  type:"UPI",  bank:"HDFC"},
  {id:12,merchant:"Netflix",       category:"Bills",     amount:-649,  date:"2026-05-05", time:"12:00 PM", type:"UPI",  bank:"HDFC"},
];

const circlesData = [
  {id:1, name:"Rishank",        upiIds:["rishank0911@okaxis","rishank@ybl"],       txnIds:[1,4,8]},
  {id:2, name:"Priya",          upiIds:["priya.sharma@okicici"],                   txnIds:[3,10]},
  {id:3, name:"College Friends",upiIds:["rahul99@okhdfc","neha.k@ybl"],            txnIds:[6,11]},
];

const initRules = [
  {id:1,pattern:"zomato",category:"Food"},{id:2,pattern:"swiggy",category:"Food"},
  {id:3,pattern:"uber",category:"Transport"},{id:4,pattern:"ola",category:"Transport"},
  {id:5,pattern:"airtel",category:"Bills"},{id:6,pattern:"netflix",category:"Bills"},
  {id:7,pattern:"amazon",category:"Shopping"},
];

const fmtDate = (d) => new Date(d).toLocaleDateString("en-IN",{day:"numeric",month:"short"});
const fmtDateLong = (d) => d ? d.toLocaleDateString("en-IN",{day:"numeric",month:"short",year:"numeric"}) : null;

// Icons
const Ico = ({d,w=20,h=20,stroke,sw=1.8}) => <svg width={w} height={h} viewBox="0 0 24 24" fill="none" stroke={stroke||T.textPrimary} strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round"><path d={d}/></svg>;
const BackIcon = () => <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={T.textPrimary} strokeWidth="1.8" strokeLinecap="round"><polyline points="15 18 9 12 15 6"/></svg>;
const ChevronRight = () => <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke={T.textSecondary} strokeWidth="1.8" strokeLinecap="round"><polyline points="9 18 15 12 9 6"/></svg>;
const CloseIcon = () => <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke={T.textPrimary} strokeWidth="1.8" strokeLinecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>;
const SearchIcon = () => <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke={T.textSecondary} strokeWidth="2" strokeLinecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>;
const TrashIcon = () => <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke={T.red} strokeWidth="1.8" strokeLinecap="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>;
const FilterIcon = ({active}) => <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={active?T.textPrimary:T.textSecondary} strokeWidth="1.8" strokeLinecap="round"><line x1="4" y1="6" x2="20" y2="6"/><line x1="8" y1="12" x2="16" y2="12"/><line x1="11" y1="18" x2="13" y2="18"/></svg>;
const PlusIconBtn = ({color}) => <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke={color||T.textPrimary} strokeWidth="1.8" strokeLinecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>;
const SmsIcon = () => <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke={T.textMuted} strokeWidth="1.2" strokeLinecap="round"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/><line x1="9" y1="10" x2="15" y2="10"/><line x1="9" y1="14" x2="12" y2="14"/></svg>;
const IconHome = ({a}) => <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={a?T.textPrimary:T.textSecondary} strokeWidth="1.8" strokeLinecap="round"><path d="M3 9.5L12 3l9 6.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z"/><path d="M9 21V12h6v9"/></svg>;
const IconAnalytics = ({a}) => <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={a?T.textPrimary:T.textSecondary} strokeWidth="1.8" strokeLinecap="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>;
const IconCircles = ({a}) => <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={a?T.textPrimary:T.textSecondary} strokeWidth="1.8" strokeLinecap="round"><circle cx="9" cy="12" r="4"/><circle cx="15" cy="12" r="4"/></svg>;
const IconAutopay = ({a}) => <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={a?T.textPrimary:T.textSecondary} strokeWidth="1.8" strokeLinecap="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/></svg>;
const IconSettings = ({a}) => <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={a?T.textPrimary:T.textSecondary} strokeWidth="1.8" strokeLinecap="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/></svg>;

// Shared UI
const SL = ({c}) => <div style={{color:T.textSecondary,fontSize:10,letterSpacing:3,marginBottom:14}}>{c}</div>;
const FL = ({c}) => <div style={{color:T.textSecondary,fontSize:10,letterSpacing:2,marginBottom:8}}>{c}</div>;
const Divider = () => <div style={{height:1,background:T.border,margin:"24px 0"}}/>;

const Chip = ({label,selected,onToggle,color}) => (
  <div onClick={onToggle} style={{padding:"7px 14px",borderRadius:20,cursor:"pointer",border:`1px solid ${selected?T.textPrimary:T.border}`,color:selected?T.textPrimary:T.textSecondary,fontSize:12,fontWeight:selected?600:400,display:"flex",alignItems:"center",gap:6}}>
    {color && <div style={{width:6,height:6,borderRadius:"50%",background:color}}/>}
    {label}
  </div>
);

const TxtIn = ({value,onChange,placeholder,mono,type}) => (
  <input value={value} onChange={e=>onChange(e.target.value)} placeholder={placeholder} type={type||"text"}
    style={{width:"100%",background:T.bg,border:`1px solid ${T.border}`,borderRadius:12,padding:"12px 14px",color:T.textPrimary,fontSize:13,fontFamily:mono?"'Courier New',monospace":T.font,outline:"none",boxSizing:"border-box"}}/>
);

const SaveBtn = ({label,onPress,enabled}) => (
  <div onClick={enabled!==false?onPress:undefined} style={{padding:"14px",borderRadius:12,background:enabled!==false?T.textPrimary:T.subtle,color:enabled!==false?T.bg:T.textSecondary,fontSize:13,fontWeight:700,textAlign:"center",cursor:enabled!==false?"pointer":"default"}}>
    {label||"Save"}
  </div>
);

function Sheet({onClose,title,children}){
  return (
    <div style={{position:"fixed",inset:0,zIndex:200,display:"flex",flexDirection:"column",justifyContent:"flex-end"}}>
      <div onClick={onClose} style={{position:"absolute",inset:0,background:"#000000cc"}}/>
      <div style={{position:"relative",background:T.card,borderRadius:"20px 20px 0 0",padding:"24px 20px 40px",fontFamily:T.font,maxHeight:"90vh",overflowY:"auto"}}>
        <div style={{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:24}}>
          <div style={{color:T.textPrimary,fontSize:16,fontWeight:600}}>{title}</div>
          <div onClick={onClose} style={{cursor:"pointer"}}><CloseIcon/></div>
        </div>
        {children}
      </div>
    </div>
  );
}

// Transaction Row
function TxnRow({t,showDate,last,onPress,onDelete}){
  const ts = showDate ? `${fmtDate(t.date)} · ${t.time}` : t.time;
  const [offsetX,setOffsetX]=useState(0);
  const [dragging,setDragging]=useState(false);
  const startX=useRef(null);
  const THRESHOLD=72;

  const onTouchStart=e=>{startX.current=e.touches[0].clientX;setDragging(true);};
  const onTouchMove=e=>{
    if(startX.current===null)return;
    const dx=e.touches[0].clientX-startX.current;
    if(dx<0)setOffsetX(Math.max(dx,-THRESHOLD-10));
  };
  const onTouchEnd=()=>{
    if(offsetX<-THRESHOLD*0.6){setOffsetX(-THRESHOLD);}
    else{setOffsetX(0);}
    setDragging(false);
    startX.current=null;
  };

  const revealed=offsetX<=-THRESHOLD;

  return (
    <div style={{position:"relative",overflow:"hidden",borderBottom:last?"none":`1px solid ${T.subtle}`}}>
      {onDelete&&<div onClick={()=>{setOffsetX(0);onDelete(t.id);}} style={{position:"absolute",right:0,top:0,bottom:0,width:THRESHOLD,background:T.red,display:"flex",alignItems:"center",justifyContent:"center",cursor:"pointer"}}>
        <TrashIcon/>
      </div>}
      <div
        onTouchStart={onDelete?onTouchStart:undefined}
        onTouchMove={onDelete?onTouchMove:undefined}
        onTouchEnd={onDelete?onTouchEnd:undefined}
        onClick={()=>{if(revealed){setOffsetX(0);}else if(onPress){onPress(t);}}}
        style={{display:"flex",alignItems:"center",gap:14,paddingTop:13,paddingBottom:13,cursor:onPress?"pointer":"default",background:T.bg,transform:`translateX(${offsetX}px)`,transition:dragging?"none":"transform 0.2s ease"}}
      >
        <div style={{width:8,height:8,borderRadius:"50%",background:catColors[t.category]||T.textSecondary,flexShrink:0}}/>
        <div style={{flex:1,minWidth:0}}>
          <div style={{color:T.textPrimary,fontSize:13,fontWeight:600,whiteSpace:"nowrap",overflow:"hidden",textOverflow:"ellipsis",marginBottom:4}}>{t.merchant}</div>
          <div style={{color:T.textSecondary,fontSize:11}}>{t.category}<span style={{color:T.border,margin:"0 4px"}}>·</span>{t.type}<span style={{color:T.border,margin:"0 4px"}}>·</span>{t.bank}</div>
        </div>
        <div style={{textAlign:"right",flexShrink:0}}>
          <div style={{color:t.amount>0?T.green:T.red,fontSize:14,fontWeight:700,letterSpacing:-0.5,marginBottom:3}}>₹{Math.abs(t.amount).toLocaleString("en-IN")}</div>
          <div style={{color:T.textSecondary,fontSize:10}}>{ts}</div>
        </div>
      </div>
    </div>
  );
}

// Date Picker
function getDIM(y,m){return new Date(y,m+1,0).getDate();}
function getFD(y,m){return new Date(y,m,1).getDay();}
function sameDay(a,b){return a&&b&&a.getFullYear()===b.getFullYear()&&a.getMonth()===b.getMonth()&&a.getDate()===b.getDate();}
function inRange(d,f,t){return f&&t&&d>f&&d<t;}

function Calendar({year,month,from,to,onSelect}){
  const days=getDIM(year,month), first=getFD(year,month);
  const cells=[...Array(first).fill(null),...Array.from({length:days},(_,i)=>new Date(year,month,i+1))];
  const circ=2*Math.PI*60;
  return (
    <div>
      <div style={{display:"grid",gridTemplateColumns:"repeat(7,1fr)",marginBottom:8}}>
        {DAYS.map(d=><div key={d} style={{textAlign:"center",color:T.textSecondary,fontSize:11,padding:"4px 0"}}>{d}</div>)}
      </div>
      <div style={{display:"grid",gridTemplateColumns:"repeat(7,1fr)",gap:"2px 0"}}>
        {cells.map((date,i)=>{
          if(!date)return <div key={`e${i}`}/>;
          const isF=sameDay(date,from),isT=sameDay(date,to),iR=inRange(date,from,to),isE=isF||isT;
          return (
            <div key={i} onClick={()=>onSelect(date)} style={{position:"relative",textAlign:"center",padding:"8px 0",cursor:"pointer"}}>
              {iR&&<div style={{position:"absolute",inset:"4px 0",background:"#f0ebe315"}}/>}
              {isF&&to&&<div style={{position:"absolute",inset:"4px 0",background:"#f0ebe315",left:"50%"}}/>}
              {isT&&from&&<div style={{position:"absolute",inset:"4px 0",background:"#f0ebe315",right:"50%"}}/>}
              <div style={{position:"relative",zIndex:1,width:32,height:32,borderRadius:"50%",background:isE?T.textPrimary:"transparent",display:"flex",alignItems:"center",justifyContent:"center",margin:"0 auto"}}>
                <span style={{fontSize:13,color:isE?T.bg:iR?T.textPrimary:T.textSecondary,fontWeight:isE?700:400}}>{date.getDate()}</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function DatePickerSheet({onClose,onApply,initFrom,initTo}){
  const [vy,setVy]=useState(2026),[vm,setVm]=useState(5);
  const [from,setFrom]=useState(initFrom||null),[to,setTo]=useState(initTo||null);
  const [sel,setSel]=useState("from");
  const prev=()=>{if(vm===0){setVm(11);setVy(y=>y-1);}else setVm(m=>m-1);};
  const next=()=>{if(vm===11){setVm(0);setVy(y=>y+1);}else setVm(m=>m+1);};
  const handle=(date)=>{
    if(sel==="from"){setFrom(date);setTo(null);setSel("to");}
    else{if(from&&date<from){setFrom(date);setTo(null);setSel("to");}else{setTo(date);setSel("from");}}
  };
  return (
    <Sheet onClose={onClose} title="Date Range">
      <div style={{display:"flex",gap:10,marginBottom:20}}>
        {[["From",from,"from"],["To",to,"to"]].map(([label,date,key])=>(
          <div key={key} onClick={()=>setSel(key)} style={{flex:1,padding:"12px 14px",borderRadius:12,background:T.bg,border:`1px solid ${sel===key?T.textPrimary:T.border}`,cursor:"pointer"}}>
            <div style={{color:T.textSecondary,fontSize:10,letterSpacing:2,marginBottom:4}}>{label.toUpperCase()}</div>
            <div style={{color:date?T.textPrimary:T.textSecondary,fontSize:13,fontWeight:date?600:400}}>{date?fmtDateLong(date):"Select"}</div>
          </div>
        ))}
      </div>
      <div style={{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:16}}>
        <div onClick={prev} style={{cursor:"pointer",padding:4}}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={T.textPrimary} strokeWidth="1.8" strokeLinecap="round"><polyline points="15 18 9 12 15 6"/></svg></div>
        <div style={{color:T.textPrimary,fontSize:14,fontWeight:600}}>{MONTHS[vm]} {vy}</div>
        <div onClick={next} style={{cursor:"pointer",padding:4}}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={T.textPrimary} strokeWidth="1.8" strokeLinecap="round"><polyline points="9 18 15 12 9 6"/></svg></div>
      </div>
      <Calendar year={vy} month={vm} from={from} to={to} onSelect={handle}/>
      <div style={{color:T.textSecondary,fontSize:11,textAlign:"center",marginTop:14}}>{!from?"Select start date":!to?"Select end date":"Range selected"}</div>
      <div style={{display:"flex",gap:10,marginTop:20}}>
        <div onClick={()=>{setFrom(null);setTo(null);}} style={{flex:1,padding:"13px",borderRadius:12,border:`1px solid ${T.border}`,color:T.textSecondary,fontSize:13,textAlign:"center",cursor:"pointer"}}>Clear</div>
        <div onClick={()=>{if(from){onApply(from,to);onClose();}}} style={{flex:2,padding:"13px",borderRadius:12,background:from?T.textPrimary:T.subtle,color:from?T.bg:T.textSecondary,fontSize:13,fontWeight:700,textAlign:"center",cursor:from?"pointer":"default"}}>Apply</div>
      </div>
    </Sheet>
  );
}

// Filter Sheet
function FilterSheet({filters,setFilters,onClose,categories}){
  const [local,setLocal]=useState(filters);
  const [showDP,setShowDP]=useState(false);
  const toggle=(key,val)=>setLocal(f=>({...f,[key]:f[key].includes(val)?f[key].filter(x=>x!==val):[...f[key],val]}));
  const ac=(local.categories.length?1:0)+(local.types.length?1:0)+(local.banks.length?1:0)+(local.flow!=="all"?1:0)+(local.dateFrom||local.dateTo?1:0);
  return (
    <>
      <Sheet onClose={onClose} title="Filters">
        <div style={{marginBottom:20}}>
          <FL c="DATE RANGE"/>
          <div onClick={()=>setShowDP(true)} style={{padding:"12px 14px",borderRadius:12,background:T.bg,border:`1px solid ${local.dateFrom||local.dateTo?T.textPrimary:T.border}`,cursor:"pointer"}}>
            <div style={{color:local.dateFrom||local.dateTo?T.textPrimary:T.textSecondary,fontSize:13}}>{local.dateFrom||local.dateTo?`${local.dateFrom?fmtDateLong(local.dateFrom):"—"} → ${local.dateTo?fmtDateLong(local.dateTo):"—"}`:"Select range"}</div>
          </div>
        </div>
        <div style={{marginBottom:20}}><FL c="DEBIT / CREDIT"/><div style={{display:"flex",gap:8}}>{["all","debit","credit"].map(v=><Chip key={v} label={v.charAt(0).toUpperCase()+v.slice(1)} selected={local.flow===v} onToggle={()=>setLocal(f=>({...f,flow:v}))}/>)}</div></div>
        <div style={{marginBottom:20}}><FL c="CATEGORY"/><div style={{display:"flex",flexWrap:"wrap",gap:8}}>{categories.map(c=><Chip key={c} label={c} selected={local.categories.includes(c)} onToggle={()=>toggle("categories",c)} color={catColors[c]}/>)}</div></div>
        <div style={{marginBottom:20}}><FL c="PAYMENT TYPE"/><div style={{display:"flex",flexWrap:"wrap",gap:8}}>{ALL_TYPES.map(t=><Chip key={t} label={t} selected={local.types.includes(t)} onToggle={()=>toggle("types",t)}/>)}</div></div>
        <div style={{marginBottom:24}}><FL c="BANK"/><div style={{display:"flex",gap:8}}>{ALL_BANKS.map(b=><Chip key={b} label={b} selected={local.banks.includes(b)} onToggle={()=>toggle("banks",b)}/>)}</div></div>
        <div style={{display:"flex",gap:10}}>
          <div onClick={()=>setLocal({categories:[],types:[],banks:[],flow:"all",dateFrom:null,dateTo:null})} style={{flex:1,padding:"13px",borderRadius:12,border:`1px solid ${T.border}`,color:T.textSecondary,fontSize:13,textAlign:"center",cursor:"pointer"}}>Clear all</div>
          <div onClick={()=>{setFilters(local);onClose();}} style={{flex:2,padding:"13px",borderRadius:12,background:T.textPrimary,color:T.bg,fontSize:13,fontWeight:700,textAlign:"center",cursor:"pointer"}}>Apply{ac>0?` (${ac})`:""}</div>
        </div>
      </Sheet>
      {showDP&&<DatePickerSheet onClose={()=>setShowDP(false)} initFrom={local.dateFrom} initTo={local.dateTo} onApply={(f,t)=>setLocal(l=>({...l,dateFrom:f,dateTo:t}))}/>}
    </>
  );
}

// Inline category picker for uncategorised transactions
function UncatRow({t,onCategorise,onPress}){
  const [expanded,setExpanded]=useState(false);
  const [chosen,setChosen]=useState(null);
  const [stage,setStage]=useState("pick"); // "pick" | "rule"
  const handleConfirm=()=>{if(!chosen)return;setStage("rule");};
  const handleRule=(addRule)=>{onCategorise(t.id,chosen,addRule);setExpanded(false);setStage("pick");setChosen(null);};
  return (
    <div style={{borderBottom:`1px solid ${T.subtle}`,background:`${T.red}11`,margin:"0 -20px",padding:"0 20px"}}>
      <div style={{display:"flex",alignItems:"center",gap:14,paddingTop:13,paddingBottom:13,cursor:"pointer"}} onClick={()=>{if(stage==="pick")setExpanded(e=>!e);}}>
        <div style={{width:8,height:8,borderRadius:"50%",border:`1.5px dashed ${T.textSecondary}`,flexShrink:0}}/>
        <div style={{flex:1,minWidth:0}}>
          <div style={{color:T.textPrimary,fontSize:13,fontWeight:600,whiteSpace:"nowrap",overflow:"hidden",textOverflow:"ellipsis",marginBottom:4}}>{t.merchant}</div>
          <div style={{color:T.textSecondary,fontSize:11}}>{t.type}<span style={{color:T.border}}>·</span>{t.bank}</div>
        </div>
        <div style={{textAlign:"right",flexShrink:0}}>
          <div style={{color:t.amount>0?T.green:T.red,fontSize:14,fontWeight:700,letterSpacing:-0.5,marginBottom:3}}>₹{Math.abs(t.amount).toLocaleString("en-IN")}</div>
          <div style={{color:T.textSecondary,fontSize:10}}>{t.time}</div>
        </div>
      </div>
      {expanded&&stage==="pick"&&(
        <div style={{paddingBottom:14}}>
          <div style={{display:"flex",flexWrap:"wrap",gap:8,marginBottom:10}}>
            {ALL_CATEGORIES.map(cat=>(
              <div key={cat} onClick={()=>setChosen(cat)} style={{padding:"5px 12px",borderRadius:20,fontSize:11,cursor:"pointer",background:chosen===cat?catColors[cat]+"33":T.subtle,border:`1px solid ${chosen===cat?catColors[cat]:T.border}`,color:chosen===cat?catColors[cat]:T.textSecondary,display:"flex",alignItems:"center",gap:5}}>
                <div style={{width:5,height:5,borderRadius:"50%",background:catColors[cat]}}/>
                {cat}
              </div>
            ))}
          </div>
          <div style={{display:"flex",gap:8}}>
            <div onClick={()=>{setExpanded(false);setChosen(null);}} style={{flex:1,padding:"9px",borderRadius:10,border:`1px solid ${T.border}`,color:T.textSecondary,fontSize:12,textAlign:"center",cursor:"pointer"}}>Cancel</div>
            <div onClick={handleConfirm} style={{flex:2,padding:"9px",borderRadius:10,background:chosen?T.textPrimary:T.subtle,color:chosen?T.bg:T.textSecondary,fontSize:12,fontWeight:600,textAlign:"center",cursor:chosen?"pointer":"default",transition:"background 0.15s"}}>
              {chosen?`Confirm as ${chosen}`:"Pick a category"}
            </div>
          </div>
        </div>
      )}
      {expanded&&stage==="rule"&&(
        <div style={{paddingBottom:14}}>
          <div style={{color:T.textPrimary,fontSize:13,fontWeight:600,marginBottom:4}}>Always categorise {t.merchant} as {chosen}?</div>
          <div style={{color:T.textSecondary,fontSize:11,marginBottom:12}}>This will add a rule in Settings. Future transactions will be auto-categorised.</div>
          <div style={{display:"flex",gap:8}}>
            <div onClick={()=>handleRule(false)} style={{flex:1,padding:"9px",borderRadius:10,border:`1px solid ${T.border}`,color:T.textSecondary,fontSize:12,textAlign:"center",cursor:"pointer"}}>Just this once</div>
            <div onClick={()=>handleRule(true)} style={{flex:2,padding:"9px",borderRadius:10,background:T.textPrimary,color:T.bg,fontSize:12,fontWeight:600,textAlign:"center",cursor:"pointer"}}>Yes, add rule</div>
          </div>
        </div>
      )}
    </div>
  );
}

// Home
function HomeScreen({navigate,setTab}){
  const now=new Date("2026-06-06");
  const todayStr=now.toISOString().split("T")[0];
  const [vy,setVy]=useState(now.getFullYear());
  const [vm,setVm]=useState(now.getMonth());
  const [cats,setCats]=useState({});
  const prevMonth=()=>{if(vm===0){setVm(11);setVy(y=>y-1);}else setVm(m=>m-1);};
  const nextMonth=()=>{if(vm===11){setVm(0);setVy(y=>y+1);}else setVm(m=>m+1);};
  const isCurrentMonth=vy===now.getFullYear()&&vm===now.getMonth();
  const monthTxns=useMemo(()=>allTxns.filter(t=>{const d=new Date(t.date);return d.getFullYear()===vy&&d.getMonth()===vm;}),[vy,vm]);
  const credited=monthTxns.filter(t=>t.amount>0).reduce((s,t)=>s+t.amount,0);
  const debited=monthTxns.filter(t=>t.amount<0).reduce((s,t)=>s+Math.abs(t.amount),0);

  const todayTxns=useMemo(()=>allTxns.filter(t=>t.date===todayStr),[]);
  const recentGroups=useMemo(()=>{
    if(todayTxns.length>0) return [{label:"TODAY",txns:todayTxns}];
    const recent=[...allTxns].sort((a,b)=>new Date(b.date)-new Date(a.date));
    const seen=new Set();const grouped=[];
    for(const t of recent){
      if(!seen.has(t.date)){seen.add(t.date);grouped.push({label:new Date(t.date).toLocaleDateString("en-IN",{day:"numeric",month:"short"}).toUpperCase(),txns:[]});}
      grouped[grouped.length-1].txns.push(t);
      if(grouped.reduce((s,g)=>s+g.txns.length,0)>=7)break;
    }
    return grouped;
  },[]);
  const uncatCount=recentGroups.flatMap(g=>g.txns).filter(t=>!t.category&&!cats[t.id]).length;

  return (
    <div style={{flex:1,overflowY:"auto",padding:"24px 20px 16px"}}>
      <div style={{display:"flex",justifyContent:"space-between",alignItems:"baseline",marginBottom:28}}>
        <div style={{color:T.textPrimary,fontSize:18,fontWeight:600,letterSpacing:-0.5}}>Hey, Anuj</div>
        <div style={{display:"flex",alignItems:"center",gap:14}}>
          <div style={{display:"flex",alignItems:"center",gap:10}}>
            <div onClick={prevMonth} style={{cursor:"pointer",padding:"2px 4px",color:T.textSecondary}}>‹</div>
            <div style={{color:T.textSecondary,fontSize:11,letterSpacing:2,minWidth:72,textAlign:"center"}}>{MONTHS[vm].slice(0,3).toUpperCase()} {vy}</div>
            <div onClick={nextMonth} style={{cursor:"pointer",padding:"2px 4px",color:isCurrentMonth?T.textMuted:T.textSecondary,pointerEvents:isCurrentMonth?"none":"auto"}}>›</div>
          </div>
          <div onClick={()=>setTab("settings")} style={{cursor:"pointer",display:"flex",alignItems:"center"}}><IconSettings a={false}/></div>
        </div>
      </div>
      <div style={{textAlign:"center",marginBottom:28}}>
        <div style={{color:T.textSecondary,fontSize:10,letterSpacing:3,textTransform:"uppercase",marginBottom:8}}>Total Expenditure</div>
        <div style={{color:T.textPrimary,fontSize:46,fontWeight:300,letterSpacing:-2,lineHeight:1}}>₹{debited.toLocaleString("en-IN")}</div>
      </div>
      <div style={{display:"flex",gap:16,marginBottom:28}}>
        {[{label:"Credited",value:credited,color:T.green,sign:"↑"},{label:"Debited",value:debited,color:T.red,sign:"↓"}].map(({label,value,color,sign})=>(
          <div key={label} style={{flex:1,borderTop:`2px solid ${color}`,paddingTop:12}}>
            <div style={{color:T.textSecondary,fontSize:10,letterSpacing:2,marginBottom:5}}>{sign} {label.toUpperCase()}</div>
            <div style={{color:T.textPrimary,fontSize:18,fontWeight:600,letterSpacing:-0.5}}>₹{value.toLocaleString("en-IN")}</div>
          </div>
        ))}
      </div>
      <Divider/>
      <div style={{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:16}}>
        <div style={{display:"flex",alignItems:"center",gap:10}}>
          <div style={{color:T.textSecondary,fontSize:10,letterSpacing:3}}>{recentGroups[0]?.label||"RECENT"}</div>
          {uncatCount>0&&<div style={{fontSize:10,color:T.red,border:`1px solid ${T.red}33`,background:`${T.red}11`,padding:"2px 7px",borderRadius:4,letterSpacing:0.5}}>{uncatCount} to review</div>}
        </div>
        <span onClick={()=>navigate("transactions")} style={{color:T.textPrimary,fontSize:11,cursor:"pointer",opacity:0.5}}>Show all</span>
      </div>
      {recentGroups.length===0
        ?<div style={{color:T.textSecondary,fontSize:13,textAlign:"center",marginTop:40}}>No transactions yet</div>
        :recentGroups.map(({label,txns},gi)=>(
          <div key={label}>
            {gi>0&&<div style={{color:T.textSecondary,fontSize:10,letterSpacing:3,marginBottom:10,marginTop:16}}>{label}</div>}
            {txns.map((t,i)=>{
              const resolved={...t,category:cats[t.id]||t.category};
              return !resolved.category
                ?<UncatRow key={t.id} t={t} onCategorise={(id,cat)=>setCats(c=>({...c,[id]:cat}))} onPress={()=>navigate("txnDetail",t)}/>
                :<TxnRow key={t.id} t={resolved} showDate={false} last={i===txns.length-1} onPress={()=>navigate("txnDetail",t)}/>;
            })}
          </div>
        ))
      }
    </div>
  );
}

// Transactions
function TransactionsScreen({navigate,onBack,initFilters,categories}){
  const [txns,setTxns]=useState(allTxns);
  const deleteTxn=id=>setTxns(ts=>ts.filter(t=>t.id!==id));
  const [search,setSearch]=useState("");
  const [sortBy,setSortBy]=useState("date");
  const [sortDir,setSortDir]=useState("desc");
  const [showFilter,setShowFilter]=useState(false);
  const [filters,setFilters]=useState(initFilters||{categories:[],types:[],banks:[],flow:"all",dateFrom:null,dateTo:null});
  const handleSort=(key)=>{if(sortBy===key)setSortDir(d=>d==="desc"?"asc":"desc");else{setSortBy(key);setSortDir("desc");}};
  const afc=(filters.categories.length?1:0)+(filters.types.length?1:0)+(filters.banks.length?1:0)+(filters.flow!=="all"?1:0)+(filters.dateFrom||filters.dateTo?1:0);
  const processed=useMemo(()=>{
    let list=txns.filter(t=>{
      if(search&&!t.merchant.toLowerCase().includes(search.toLowerCase())&&!t.category.toLowerCase().includes(search.toLowerCase()))return false;
      if(filters.categories.length&&!filters.categories.includes(t.category))return false;
      if(filters.types.length&&!filters.types.includes(t.type))return false;
      if(filters.banks.length&&!filters.banks.includes(t.bank))return false;
      if(filters.flow==="debit"&&t.amount>=0)return false;
      if(filters.flow==="credit"&&t.amount<0)return false;
      if(filters.dateFrom&&new Date(t.date)<filters.dateFrom)return false;
      if(filters.dateTo&&new Date(t.date)>filters.dateTo)return false;
      return true;
    });
    list.sort((a,b)=>{const diff=sortBy==="date"?new Date(a.date)-new Date(b.date):Math.abs(a.amount)-Math.abs(b.amount);return sortDir==="desc"?-diff:diff;});
    return list;
  },[search,filters,sortBy,sortDir,txns]);
  const grouped=useMemo(()=>{
    if(sortBy!=="date")return null;
    const groups={};
    processed.forEach(t=>{const key=new Date(t.date).toLocaleString("en-IN",{month:"long",year:"numeric"});if(!groups[key])groups[key]=[];groups[key].push(t);});
    return Object.entries(groups);
  },[processed,sortBy]);
  const SortBtn=({id,label})=>{const a=sortBy===id;return <div onClick={()=>handleSort(id)} style={{padding:"6px 14px",borderRadius:20,cursor:"pointer",border:`1px solid ${a?T.textPrimary:T.border}`,color:a?T.textPrimary:T.textSecondary,fontSize:11,fontWeight:a?600:400}}>{label}{a?(sortDir==="desc"?" ↓":" ↑"):""}</div>;};
  return (
    <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
      <div style={{padding:"24px 20px 0"}}>
        <div style={{display:"flex",alignItems:"center",gap:14,marginBottom:20}}>
          <div onClick={onBack} style={{cursor:"pointer"}}><BackIcon/></div>
          <div style={{color:T.textPrimary,fontSize:16,fontWeight:600,flex:1}}>Transactions</div>
          <div onClick={()=>setShowFilter(true)} style={{cursor:"pointer",position:"relative"}}>
            <FilterIcon active={afc>0}/>
            {afc>0&&<div style={{position:"absolute",top:-4,right:-4,width:14,height:14,borderRadius:"50%",background:T.textPrimary,display:"flex",alignItems:"center",justifyContent:"center"}}><span style={{color:T.bg,fontSize:8,fontWeight:700}}>{afc}</span></div>}
          </div>
        </div>
        <div style={{display:"flex",alignItems:"center",gap:10,background:T.card,borderRadius:12,padding:"11px 14px",border:`1px solid ${T.border}`,marginBottom:14}}>
          <SearchIcon/>
          <input value={search} onChange={e=>setSearch(e.target.value)} placeholder="Search transactions..." style={{background:"none",border:"none",outline:"none",color:T.textPrimary,fontSize:13,flex:1,fontFamily:T.font}}/>
          {search&&<div onClick={()=>setSearch("")} style={{cursor:"pointer",display:"flex",alignItems:"center"}}><CloseIcon/></div>}
        </div>
        <div style={{display:"flex",gap:8,marginBottom:4}}><SortBtn id="date" label="Date"/><SortBtn id="amount" label="Amount"/></div>
      </div>
      <div style={{flex:1,overflowY:"auto",padding:"0 20px 32px"}}>
        {processed.length===0?<div style={{color:T.textSecondary,fontSize:13,textAlign:"center",marginTop:60}}>No transactions found</div>
        :grouped?grouped.map(([month,txns])=>(
          <div key={month}>
            <div style={{display:"flex",justifyContent:"space-between",alignItems:"baseline",paddingTop:20,paddingBottom:10}}>
              <div style={{color:T.textSecondary,fontSize:10,letterSpacing:3}}>{month.toUpperCase()}</div>
              <div style={{color:T.red,fontSize:12,fontWeight:600}}>₹{txns.filter(t=>t.amount<0).reduce((s,t)=>s+Math.abs(t.amount),0).toLocaleString("en-IN")}</div>
            </div>
            {txns.map((t,i)=><TxnRow key={t.id} t={t} showDate={false} last={i===txns.length-1} onPress={()=>navigate("txnDetail",t)} onDelete={deleteTxn}/>)}
          </div>
        )):processed.map((t,i)=><TxnRow key={t.id} t={t} showDate={true} last={i===processed.length-1} onPress={()=>navigate("txnDetail",t)} onDelete={deleteTxn}/>)}
      </div>
      {showFilter&&<FilterSheet filters={filters} setFilters={setFilters} onClose={()=>setShowFilter(false)} categories={categories}/>}
    </div>
  );
}

// Txn Detail
function TxnDetail({txn,onBack,navigate}){
  const rows=[
    {label:"Amount",value:`₹${Math.abs(txn.amount).toLocaleString("en-IN")}`,color:txn.amount>0?T.green:T.red},
    {label:"Type",value:txn.amount>0?"Credit":"Debit"},
    {label:"Category",value:txn.category,dot:catColors[txn.category]},
    {label:"Payment",value:txn.type},
    {label:"Bank",value:txn.bank},
    {label:"Date",value:fmtDate(txn.date)},
    {label:"Time",value:txn.time},
  ];
  return (
    <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
      <div style={{padding:"24px 20px 0"}}>
        <div style={{display:"flex",alignItems:"center",gap:14,marginBottom:24}}>
          <div onClick={onBack} style={{cursor:"pointer"}}><BackIcon/></div>
          <div style={{color:T.textPrimary,fontSize:16,fontWeight:600,flex:1}}>{txn.merchant}</div>
          <div onClick={()=>navigate("editTxn",txn)} style={{color:T.textSecondary,fontSize:12,cursor:"pointer"}}>Edit</div>
        </div>
      </div>
      <div style={{flex:1,overflowY:"auto",padding:"0 20px 40px"}}>
        <div style={{background:T.card,borderRadius:16,border:`1px solid ${T.border}`,overflow:"hidden"}}>
          {rows.map(({label,value,color,dot},i)=>(
            <div key={label} style={{display:"flex",justifyContent:"space-between",alignItems:"center",padding:"14px 16px",borderBottom:i<rows.length-1?`1px solid ${T.subtle}`:"none"}}>
              <div style={{color:T.textSecondary,fontSize:12}}>{label}</div>
              <div style={{display:"flex",alignItems:"center",gap:6}}>
                {dot&&<div style={{width:6,height:6,borderRadius:"50%",background:dot}}/>}
                <div style={{color:color||T.textPrimary,fontSize:13,fontWeight:600}}>{value}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// Add/Edit Transaction
function EditTxnScreen({txn,onBack,categories,setCategories}){
  const isAdd=!txn;
  const [form,setForm]=useState(txn?{...txn,amount:Math.abs(txn.amount),type:txn.amount>=0?"credit":"debit"}:{merchant:"",amount:"",type:"debit",category:categories[0],paymentType:"UPI",date:"2026-06-06",time:"20:00",notes:""});
  const [showAddCat,setShowAddCat]=useState(false);
  const [newCat,setNewCat]=useState("");
  const set=k=>v=>setForm(f=>({...f,[k]:v}));
  const addCat=()=>{const t=newCat.trim();if(!t||categories.includes(t))return;setCategories(c=>[...c,t]);setForm(f=>({...f,category:t}));setNewCat("");setShowAddCat(false);};
  return (
    <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
      <div style={{padding:"24px 20px 0",marginBottom:8}}>
        <div style={{display:"flex",alignItems:"center",gap:14}}>
          <div onClick={onBack} style={{cursor:"pointer"}}><BackIcon/></div>
          <div style={{color:T.textPrimary,fontSize:16,fontWeight:600}}>{isAdd?"Add Transaction":"Edit Transaction"}</div>
        </div>
      </div>
      <div style={{flex:1,overflowY:"auto",padding:"16px 20px 120px"}}>
        <div style={{marginBottom:24}}><FL c="MERCHANT"/><TxtIn value={form.merchant} onChange={set("merchant")} placeholder="Merchant name"/></div>
        <div style={{marginBottom:24}}>
          <FL c="AMOUNT"/>
          <div style={{display:"flex",gap:10}}>
            <div style={{position:"relative",flex:1}}>
              <div style={{position:"absolute",left:14,top:"50%",transform:"translateY(-50%)",color:T.textSecondary,fontSize:13}}>₹</div>
              <input type="number" value={form.amount} onChange={e=>set("amount")(e.target.value)} style={{width:"100%",background:T.bg,border:`1px solid ${T.border}`,borderRadius:12,padding:"12px 14px 12px 28px",color:T.textPrimary,fontSize:13,fontFamily:T.font,outline:"none",boxSizing:"border-box"}}/>
            </div>
            <div style={{display:"flex",gap:8}}>
              {["debit","credit"].map(v=>(
                <div key={v} onClick={()=>set("type")(v)} style={{padding:"12px 14px",borderRadius:12,cursor:"pointer",border:`1px solid ${form.type===v?(v==="debit"?T.red:T.green):T.border}`,color:form.type===v?(v==="debit"?T.red:T.green):T.textSecondary,fontSize:12,fontWeight:form.type===v?600:400}}>
                  {v.charAt(0).toUpperCase()+v.slice(1)}
                </div>
              ))}
            </div>
          </div>
        </div>
        <div style={{marginBottom:24}}>
          <FL c="CATEGORY"/>
          <div style={{display:"flex",flexWrap:"wrap",gap:8}}>
            {categories.map(cat=><Chip key={cat} label={cat} selected={form.category===cat} onToggle={()=>set("category")(cat)} color={catColors[cat]}/>)}
            <div onClick={()=>setShowAddCat(true)} style={{padding:"7px 14px",borderRadius:20,cursor:"pointer",border:`1px dashed ${T.border}`,color:T.textSecondary,fontSize:12,display:"flex",alignItems:"center",gap:5}}>
              <span style={{fontSize:14,lineHeight:1}}>+</span><span>New</span>
            </div>
          </div>
        </div>
        <div style={{marginBottom:24}}><FL c="PAYMENT TYPE"/><div style={{display:"flex",flexWrap:"wrap",gap:8}}>{ALL_TYPES.map(pt=><Chip key={pt} label={pt} selected={form.paymentType===pt} onToggle={()=>set("paymentType")(pt)}/>)}</div></div>
        <div style={{marginBottom:24}}><FL c="DATE & TIME"/><div style={{display:"flex",gap:10}}><input type="date" value={form.date} onChange={e=>set("date")(e.target.value)} style={{flex:1,background:T.bg,border:`1px solid ${T.border}`,borderRadius:12,padding:"12px 14px",color:T.textPrimary,fontSize:13,fontFamily:T.font,outline:"none",boxSizing:"border-box",colorScheme:"dark"}}/><input type="time" value={form.time} onChange={e=>set("time")(e.target.value)} style={{flex:1,background:T.bg,border:`1px solid ${T.border}`,borderRadius:12,padding:"12px 14px",color:T.textPrimary,fontSize:13,fontFamily:T.font,outline:"none",boxSizing:"border-box",colorScheme:"dark"}}/></div></div>
        <div style={{marginBottom:24}}><FL c="NOTES"/><textarea value={form.notes||""} onChange={e=>set("notes")(e.target.value)} placeholder="Add a note..." rows={3} style={{width:"100%",background:T.bg,border:`1px solid ${T.border}`,borderRadius:12,padding:"12px 14px",color:T.textPrimary,fontSize:13,fontFamily:T.font,outline:"none",resize:"none",boxSizing:"border-box",lineHeight:1.6}}/></div>
      </div>
      <div style={{position:"fixed",bottom:0,left:0,right:0,padding:"16px 20px 36px",background:T.bg,borderTop:`1px solid ${T.border}`}}>
        <SaveBtn label={isAdd?"Add Transaction":"Save Changes"} onPress={()=>onBack()} enabled={!!form.merchant&&!!form.amount}/>
      </div>
      {showAddCat&&<Sheet onClose={()=>setShowAddCat(false)} title="New Category"><div style={{marginBottom:24}}><FL c="NAME"/><TxtIn value={newCat} onChange={setNewCat} placeholder="e.g. Chai & Snacks"/></div><SaveBtn label="Add Category" onPress={addCat} enabled={!!newCat.trim()}/></Sheet>}
    </div>
  );
}

// Analytics
function AnalyticsScreen({onBack,navigate}){
  const [activeCat,setActiveCat]=useState(null);
  const [showFilter,setShowFilter]=useState(false);
  const now=new Date();
  const monthStart=new Date(now.getFullYear(),now.getMonth(),1);
  const [filters,setFilters]=useState({categories:[],types:[],banks:[],flow:"all",dateFrom:monthStart,dateTo:null});

  const isCurrentMonthDefault=f=>!f.dateTo&&f.categories.length===0&&f.types.length===0&&f.banks.length===0&&f.flow==="all"&&f.dateFrom&&f.dateFrom.getFullYear()===now.getFullYear()&&f.dateFrom.getMonth()===now.getMonth()&&f.dateFrom.getDate()===1;
  const isDefault=isCurrentMonthDefault(filters);
  const afc=(filters.categories.length?1:0)+(filters.types.length?1:0)+(filters.banks.length?1:0)+(filters.flow!=="all"?1:0)+(filters.dateFrom||filters.dateTo?1:0);

  const filtered=useMemo(()=>allTxns.filter(t=>{
    if(filters.categories.length&&!filters.categories.includes(t.category))return false;
    if(filters.types.length&&!filters.types.includes(t.type))return false;
    if(filters.banks.length&&!filters.banks.includes(t.bank))return false;
    if(filters.flow==="debit"&&t.amount>=0)return false;
    if(filters.flow==="credit"&&t.amount<0)return false;
    if(filters.dateFrom&&new Date(t.date)<filters.dateFrom)return false;
    if(filters.dateTo&&new Date(t.date)>filters.dateTo)return false;
    return true;
  }),[filters]);

  const spent=filtered.filter(t=>t.amount<0);
  const credited=filtered.filter(t=>t.amount>0);
  const totalSpent=spent.reduce((s,t)=>s+Math.abs(t.amount),0);
  const totalCredited=credited.reduce((s,t)=>s+t.amount,0);

  const catTotals={};
  spent.forEach(t=>{catTotals[t.category]=(catTotals[t.category]||0)+Math.abs(t.amount);});
  const catList=Object.entries(catTotals).sort((a,b)=>b[1]-a[1]);

  const merchantTotals={};
  spent.forEach(t=>{merchantTotals[t.merchant]=(merchantTotals[t.merchant]||0)+Math.abs(t.amount);});
  const topM=Object.entries(merchantTotals).sort((a,b)=>b[1]-a[1]).slice(0,5);

  const today=new Date("2026-06-06");
  const barEnd=filters.dateTo||today;
  const barStart=new Date(barEnd);barStart.setDate(barEnd.getDate()-14);
  const last15=Array.from({length:15},(_,i)=>{const d=new Date(barStart);d.setDate(barStart.getDate()+i);const ds=d.toISOString().split("T")[0];return{ds,total:spent.filter(t=>t.date===ds).reduce((s,t)=>s+Math.abs(t.amount),0),label:d.getDate()};});
  const maxDay=Math.max(...last15.map(d=>d.total),1);
  const todayStr=today.toISOString().split("T")[0];

  const circ=2*Math.PI*60;
  let offset=0;
  const slices=catList.map(([cat,val])=>{const pct=val/totalSpent;const dash=circ*pct-3;const s={cat,dash,offset};offset+=circ*pct;return s;});

  const barLabel=`${barStart.getDate()} ${MONTHS[barStart.getMonth()].slice(0,3).toUpperCase()} – ${barEnd.getDate()} ${MONTHS[barEnd.getMonth()].slice(0,3).toUpperCase()}`;

  const filterLabel=isDefault
    ? "THIS MONTH"
    : afc===0
      ? "ALL TIME"
      : filters.dateFrom||filters.dateTo
        ? `${filters.dateFrom?fmtDateLong(filters.dateFrom):"—"} → ${filters.dateTo?fmtDateLong(filters.dateTo):"—"}`
        : `${afc} FILTER${afc>1?"S":""}`;

  return (
    <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
      <div style={{padding:"24px 20px 0"}}>
        <div style={{display:"flex",alignItems:"center",gap:14,marginBottom:24}}>
          <div onClick={onBack} style={{cursor:"pointer"}}><BackIcon/></div>
          <div style={{color:T.textPrimary,fontSize:16,fontWeight:600,flex:1}}>Analytics</div>
          <div style={{display:"flex",alignItems:"center",gap:10}}>
            <div style={{color:T.textSecondary,fontSize:10,letterSpacing:1,maxWidth:160,textAlign:"right",lineHeight:1.3}}>{filterLabel}</div>
            <div onClick={()=>setShowFilter(true)} style={{cursor:"pointer",position:"relative"}}>
              <FilterIcon active={!isDefault&&afc>0}/>
              {!isDefault&&afc>0&&<div style={{position:"absolute",top:-4,right:-4,width:14,height:14,borderRadius:"50%",background:T.textPrimary,display:"flex",alignItems:"center",justifyContent:"center"}}><span style={{color:T.bg,fontSize:8,fontWeight:700}}>{afc}</span></div>}
            </div>
          </div>
        </div>
      </div>
      <div style={{flex:1,overflowY:"auto",padding:"0 20px 40px"}}>
        {filtered.length===0?(
          <div style={{color:T.textSecondary,fontSize:13,textAlign:"center",marginTop:60}}>No transactions match these filters</div>
        ):(
          <>
            <div style={{display:"flex",gap:16,marginBottom:32}}>
              {[{label:"SPENT",value:totalSpent,color:T.red},{label:"CREDITED",value:totalCredited,color:T.green}].map(({label,value,color})=>(
                <div key={label} style={{flex:1,borderTop:`2px solid ${color}`,paddingTop:12}}>
                  <div style={{color:T.textSecondary,fontSize:10,letterSpacing:2,marginBottom:5}}>{label}</div>
                  <div style={{color:T.textPrimary,fontSize:20,fontWeight:600,letterSpacing:-0.5}}>₹{value.toLocaleString("en-IN")}</div>
                </div>
              ))}
            </div>
            {catList.length>0&&(
              <div style={{marginBottom:32}}>
                <SL c="CATEGORIES"/>
                <div style={{display:"flex",gap:20,alignItems:"center"}}>
                  <div style={{position:"relative",flexShrink:0}}>
                    <svg width="160" height="160" style={{transform:"rotate(-90deg)"}}>
                      {slices.map(({cat,dash,offset})=><circle key={cat} cx="80" cy="80" r="60" fill="none" stroke={catColors[cat]||"#888"} strokeWidth="22" strokeDasharray={`${dash} ${circ-dash}`} strokeDashoffset={-offset} strokeLinecap="butt"/>)}
                    </svg>
                    <div style={{position:"absolute",inset:0,display:"flex",flexDirection:"column",alignItems:"center",justifyContent:"center"}}>
                      <div style={{color:T.textSecondary,fontSize:9,letterSpacing:2}}>TOTAL</div>
                      <div style={{color:T.textPrimary,fontSize:14,fontWeight:700,letterSpacing:-0.5}}>₹{totalSpent.toLocaleString("en-IN")}</div>
                    </div>
                  </div>
                  <div style={{flex:1}}>
                    {catList.map(([cat,val])=>(
                      <div key={cat} onClick={()=>{setActiveCat(activeCat===cat?null:cat);navigate("transactions",{categories:[cat]});}} style={{display:"flex",alignItems:"center",justifyContent:"space-between",marginBottom:14,cursor:"pointer",opacity:activeCat&&activeCat!==cat?0.4:1,transition:"opacity 0.2s"}}>
                        <div style={{display:"flex",alignItems:"center",gap:8}}><div style={{width:8,height:8,borderRadius:"50%",background:catColors[cat]||"#888"}}/><span style={{color:T.textSecondary,fontSize:12}}>{cat}</span></div>
                        <div style={{textAlign:"right"}}><div style={{color:T.textPrimary,fontSize:12,fontWeight:600}}>₹{val.toLocaleString("en-IN")}</div><div style={{color:T.textSecondary,fontSize:10}}>{Math.round(val/totalSpent*100)}%</div></div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
            <div style={{marginBottom:32}}>
              <SL c={barLabel}/>
              <div style={{display:"flex",alignItems:"flex-end",gap:4,height:80}}>
                {last15.map(({ds,total,label})=>{
                  const h=(total/maxDay)*56,isT=ds===todayStr;
                  return <div key={ds} style={{flex:1,display:"flex",flexDirection:"column",alignItems:"center",gap:5}}><div style={{width:"100%",height:total>0?h:3,background:isT?T.textPrimary:total>0?"#4a4a4a":"#1e1e1e",borderRadius:3,minHeight:3}}/><div style={{color:isT?T.green:T.textSecondary,fontSize:9}}>{label}</div></div>;
                })}
              </div>
            </div>
            {topM.length>0&&(
              <div>
                <SL c="TOP MERCHANTS"/>
                {topM.map(([merchant,val],i)=>(
                  <div key={merchant} style={{display:"flex",alignItems:"center",gap:12,paddingTop:12,paddingBottom:12}}>
                    <div style={{color:T.textSecondary,fontSize:11,width:14,textAlign:"right",flexShrink:0}}>{i+1}</div>
                    <div style={{flex:1,minWidth:0}}>
                      <div style={{display:"flex",justifyContent:"space-between",marginBottom:5}}><span style={{color:T.textPrimary,fontSize:13,fontWeight:600}}>{merchant}</span><span style={{color:T.textPrimary,fontSize:13,fontWeight:600}}>₹{val.toLocaleString("en-IN")}</span></div>
                      <div style={{height:2,background:T.subtle,borderRadius:2}}><div style={{width:`${(val/topM[0][1])*100}%`,height:"100%",background:"#4a4a4a",borderRadius:2}}/></div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </div>
      {showFilter&&<FilterSheet filters={filters} setFilters={setFilters} onClose={()=>setShowFilter(false)} categories={ALL_CATEGORIES}/>}
    </div>
  );
}

// Circles
function CirclesScreen({onBack,navigate}){
  const [selected,setSelected]=useState(null);
  const getTxns=c=>allTxns.filter(t=>c.txnIds.includes(t.id));
  if(selected){
    const txns=getTxns(selected);
    const paid=txns.filter(t=>t.amount<0).reduce((s,t)=>s+Math.abs(t.amount),0);
    const recv=txns.filter(t=>t.amount>0).reduce((s,t)=>s+t.amount,0);
    const net=recv-paid;
    const groups={};
    [...txns].sort((a,b)=>new Date(b.date)-new Date(a.date)).forEach(t=>{const key=new Date(t.date).toLocaleString("en-IN",{month:"long",year:"numeric"});if(!groups[key])groups[key]=[];groups[key].push(t);});
    return (
      <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
        <div style={{padding:"24px 20px 0"}}>
          <div style={{display:"flex",alignItems:"center",gap:14,marginBottom:24}}>
            <div onClick={()=>setSelected(null)} style={{cursor:"pointer"}}><BackIcon/></div>
            <div style={{color:T.textPrimary,fontSize:16,fontWeight:600,flex:1}}>{selected.name}</div>
          </div>
          <div style={{display:"flex",gap:16,marginBottom:16}}>
            {[{label:"YOU PAID",value:paid,color:T.red},{label:"RECEIVED",value:recv,color:T.green}].map(({label,value,color})=>(
              <div key={label} style={{flex:1,borderTop:`2px solid ${color}`,paddingTop:12}}>
                <div style={{color:T.textSecondary,fontSize:10,letterSpacing:2,marginBottom:5}}>{label}</div>
                <div style={{color:T.textPrimary,fontSize:18,fontWeight:600,letterSpacing:-0.5}}>₹{value.toLocaleString("en-IN")}</div>
              </div>
            ))}
          </div>
          <div style={{background:T.card,borderRadius:12,padding:"12px 16px",border:`1px solid ${T.border}`,marginBottom:20,display:"flex",justifyContent:"space-between",alignItems:"center"}}>
            <span style={{color:T.textSecondary,fontSize:11,letterSpacing:2}}>NET</span>
            <span style={{color:net>=0?T.green:T.red,fontSize:16,fontWeight:700,letterSpacing:-0.5}}>₹{Math.abs(net).toLocaleString("en-IN")}</span>
          </div>
          <div style={{marginBottom:20}}>
            <SL c="LINKED UPI IDs"/>
            <div style={{display:"flex",flexDirection:"column",gap:6}}>
              {selected.upiIds.map(id=><div key={id} style={{padding:"8px 12px",borderRadius:10,border:`1px solid ${T.border}`,color:T.textSecondary,fontSize:12,fontFamily:"'Courier New',monospace"}}>{id}</div>)}
            </div>
          </div>
          <div style={{height:1,background:T.border}}/>
        </div>
        <div style={{flex:1,overflowY:"auto",padding:"0 20px 40px"}}>
          {Object.entries(groups).length===0
            ?<div style={{color:T.textSecondary,fontSize:13,textAlign:"center",marginTop:60}}>No transactions linked to this circle yet</div>
            :Object.entries(groups).map(([month,txns])=>(
            <div key={month}>
              <div style={{color:T.textSecondary,fontSize:10,letterSpacing:3,paddingTop:20,paddingBottom:10}}>{month.toUpperCase()}</div>
              {txns.map((t,i)=><TxnRow key={t.id} t={t} showDate={false} last={i===txns.length-1} onPress={()=>navigate("txnDetail",t)}/>)}
            </div>
          ))}
        </div>
      </div>
    );
  }
  return (
    <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
      <div style={{padding:"24px 20px 0"}}>
        <div style={{display:"flex",alignItems:"center",gap:14,marginBottom:24}}>
          <div onClick={onBack} style={{cursor:"pointer"}}><BackIcon/></div>
          <div style={{color:T.textPrimary,fontSize:16,fontWeight:600,flex:1}}>Circles</div>
        </div>
      </div>
      <div style={{flex:1,overflowY:"auto",padding:"0 20px 40px"}}>
        {circlesData.map((circle,i)=>{
          const txns=getTxns(circle);
          const net=txns.filter(t=>t.amount>0).reduce((s,t)=>s+t.amount,0)-txns.filter(t=>t.amount<0).reduce((s,t)=>s+Math.abs(t.amount),0);
          const last=[...txns].sort((a,b)=>new Date(b.date)-new Date(a.date))[0];
          return (
            <div key={circle.id} onClick={()=>setSelected(circle)} style={{paddingTop:16,paddingBottom:16,cursor:"pointer",borderBottom:i<circlesData.length-1?`1px solid ${T.subtle}`:"none",display:"flex",alignItems:"center",gap:14}}>
              <div style={{width:40,height:40,borderRadius:"50%",background:T.card,border:`1px solid ${T.border}`,display:"flex",alignItems:"center",justifyContent:"center",color:T.textSecondary,fontSize:14,fontWeight:600,flexShrink:0}}>{circle.name.charAt(0)}</div>
              <div style={{flex:1,minWidth:0}}>
                <div style={{color:T.textPrimary,fontSize:13,fontWeight:600,marginBottom:4}}>{circle.name}</div>
                <div style={{color:T.textSecondary,fontSize:11}}>{circle.upiIds.length} {circle.upiIds.length===1?"UPI ID":"UPI IDs"}<span style={{color:T.textMuted,margin:"0 4px"}}>·</span>Last {last?fmtDate(last.date):"—"}</div>
              </div>
              <div style={{display:"flex",alignItems:"center",gap:8,flexShrink:0}}>
                <div style={{color:net>=0?T.green:T.red,fontSize:14,fontWeight:700,letterSpacing:-0.5}}>₹{Math.abs(net).toLocaleString("en-IN")}</div>
                <ChevronRight/>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

// Settings
function SettingsScreen({onBack,categories,setCategories}){
  const [screen,setScreen]=useState(null);
  const [rules,setRules]=useState(initRules);
  const [sCircles,setSCircles]=useState(circlesData);
  const [selCircle,setSelCircle]=useState(null);
  const [showAdd,setShowAdd]=useState(false);
  const [addPattern,setAddPattern]=useState("");
  const [addCatFor,setAddCatFor]=useState(categories[0]);
  const [newCatVal,setNewCatVal]=useState("");
  const [newCircleName,setNewCircleName]=useState("");
  const [newUpi,setNewUpi]=useState("");

  const resetAdd=()=>{setShowAdd(false);setAddPattern("");setNewCatVal("");setNewCircleName("");setNewUpi("");};

  if(screen==="rules") return (
    <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
      <div style={{padding:"24px 20px 0"}}>
        <div style={{display:"flex",alignItems:"center",gap:14,marginBottom:12}}>
          <div onClick={()=>{setScreen(null);resetAdd();}} style={{cursor:"pointer"}}><BackIcon/></div>
          <div style={{color:T.textPrimary,fontSize:16,fontWeight:600,flex:1}}>Rules</div>
          <div onClick={()=>setShowAdd(true)} style={{cursor:"pointer"}}><PlusIconBtn/></div>
        </div>
        <div style={{color:T.textSecondary,fontSize:12,marginBottom:24,lineHeight:1.6}}>When a merchant name contains the pattern, it is automatically assigned that category.</div>
      </div>
      <div style={{flex:1,overflowY:"auto",padding:"0 20px 40px"}}>
        <SL c="ALL RULES"/>
        {rules.map((rule,i)=>(
          <div key={rule.id} style={{display:"flex",alignItems:"center",gap:12,paddingTop:14,paddingBottom:14,borderBottom:i<rules.length-1?`1px solid ${T.subtle}`:"none"}}>
            <div style={{flex:1}}>
              <div style={{color:T.textPrimary,fontSize:13,fontWeight:600,marginBottom:4,fontFamily:"'Courier New',monospace"}}>{rule.pattern}</div>
              <div style={{display:"flex",alignItems:"center",gap:6}}><div style={{width:6,height:6,borderRadius:"50%",background:catColors[rule.category]||T.textSecondary}}/><span style={{color:T.textSecondary,fontSize:11}}>{rule.category}</span></div>
            </div>
            <div onClick={()=>setRules(r=>r.filter(x=>x.id!==rule.id))} style={{cursor:"pointer",padding:4}}><TrashIcon/></div>
          </div>
        ))}
      </div>
      {showAdd&&<Sheet onClose={resetAdd} title="New Rule">
        <div style={{marginBottom:20}}><FL c="MERCHANT CONTAINS"/><TxtIn value={addPattern} onChange={setAddPattern} placeholder="e.g. zomato" mono/></div>
        <div style={{marginBottom:24}}><FL c="ASSIGN CATEGORY"/><div style={{display:"flex",flexWrap:"wrap",gap:8}}>{categories.map(cat=><Chip key={cat} label={cat} selected={addCatFor===cat} onToggle={()=>setAddCatFor(cat)} color={catColors[cat]}/>)}</div></div>
        <SaveBtn label="Add Rule" onPress={()=>{if(addPattern.trim()){setRules(r=>[...r,{id:Date.now(),pattern:addPattern.trim().toLowerCase(),category:addCatFor}]);resetAdd();}}} enabled={!!addPattern.trim()}/>
      </Sheet>}
    </div>
  );

  if(screen==="categories") return (
    <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
      <div style={{padding:"24px 20px 0"}}>
        <div style={{display:"flex",alignItems:"center",gap:14,marginBottom:24}}>
          <div onClick={()=>{setScreen(null);resetAdd();}} style={{cursor:"pointer"}}><BackIcon/></div>
          <div style={{color:T.textPrimary,fontSize:16,fontWeight:600,flex:1}}>Categories</div>
          <div onClick={()=>setShowAdd(true)} style={{cursor:"pointer"}}><PlusIconBtn/></div>
        </div>
      </div>
      <div style={{flex:1,overflowY:"auto",padding:"0 20px 40px"}}>
        <SL c="ALL CATEGORIES"/>
        {categories.map((cat,i)=>(
          <div key={cat} style={{display:"flex",alignItems:"center",gap:12,paddingTop:14,paddingBottom:14,borderBottom:i<categories.length-1?`1px solid ${T.subtle}`:"none"}}>
            <div style={{width:10,height:10,borderRadius:"50%",background:catColors[cat]||T.textSecondary,flexShrink:0}}/>
            <div style={{flex:1,color:T.textPrimary,fontSize:13,fontWeight:600}}>{cat}</div>
            {!ALL_CATEGORIES.includes(cat)?<div onClick={()=>setCategories(c=>c.filter(x=>x!==cat))} style={{cursor:"pointer",padding:4}}><TrashIcon/></div>:<div style={{color:T.textMuted,fontSize:10,letterSpacing:1}}>DEFAULT</div>}
          </div>
        ))}
      </div>
      {showAdd&&<Sheet onClose={resetAdd} title="New Category"><div style={{marginBottom:24}}><FL c="NAME"/><TxtIn value={newCatVal} onChange={setNewCatVal} placeholder="e.g. Chai & Snacks"/></div><SaveBtn label="Add Category" onPress={()=>{const t=newCatVal.trim();if(t&&!categories.includes(t)){setCategories(c=>[...c,t]);resetAdd();}}} enabled={!!newCatVal.trim()}/></Sheet>}
    </div>
  );

  if(screen==="circles"){
    if(selCircle) return (
      <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
        <div style={{padding:"24px 20px 0"}}>
          <div style={{display:"flex",alignItems:"center",gap:14,marginBottom:24}}>
            <div onClick={()=>{setSelCircle(null);resetAdd();}} style={{cursor:"pointer"}}><BackIcon/></div>
            <div style={{color:T.textPrimary,fontSize:16,fontWeight:600,flex:1}}>{selCircle.name}</div>
            <div onClick={()=>setShowAdd(true)} style={{cursor:"pointer"}}><PlusIconBtn/></div>
          </div>
        </div>
        <div style={{flex:1,overflowY:"auto",padding:"0 20px 40px"}}>
          <SL c="LINKED UPI IDs"/>
          {selCircle.upiIds.map((id,i)=>(
            <div key={id} style={{display:"flex",alignItems:"center",gap:12,paddingTop:14,paddingBottom:14,borderBottom:i<selCircle.upiIds.length-1?`1px solid ${T.subtle}`:"none"}}>
              <div style={{flex:1,color:T.textPrimary,fontSize:13,fontFamily:"'Courier New',monospace"}}>{id}</div>
              <div onClick={()=>{const u={...selCircle,upiIds:selCircle.upiIds.filter(x=>x!==id)};setSCircles(cs=>cs.map(c=>c.id===u.id?u:c));setSelCircle(u);}} style={{cursor:"pointer",padding:4}}><TrashIcon/></div>
            </div>
          ))}
        </div>
        {showAdd&&<Sheet onClose={resetAdd} title="Add UPI ID"><div style={{marginBottom:24}}><FL c="UPI ID"/><TxtIn value={newUpi} onChange={setNewUpi} placeholder="e.g. name@okaxis" mono/></div><SaveBtn label="Add" onPress={()=>{const t=newUpi.trim();if(t){const u={...selCircle,upiIds:[...selCircle.upiIds,t]};setSCircles(cs=>cs.map(c=>c.id===u.id?u:c));setSelCircle(u);resetAdd();}}} enabled={!!newUpi.trim()}/></Sheet>}
      </div>
    );
    return (
      <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
        <div style={{padding:"24px 20px 0"}}>
          <div style={{display:"flex",alignItems:"center",gap:14,marginBottom:24}}>
            <div onClick={()=>{setScreen(null);resetAdd();}} style={{cursor:"pointer"}}><BackIcon/></div>
            <div style={{color:T.textPrimary,fontSize:16,fontWeight:600,flex:1}}>Circles</div>
            <div onClick={()=>setShowAdd(true)} style={{cursor:"pointer"}}><PlusIconBtn/></div>
          </div>
        </div>
        <div style={{flex:1,overflowY:"auto",padding:"0 20px 40px"}}>
          <SL c="ALL CIRCLES"/>
          {sCircles.map((circle,i)=>(
            <div key={circle.id} onClick={()=>setSelCircle(circle)} style={{display:"flex",alignItems:"center",gap:14,paddingTop:14,paddingBottom:14,cursor:"pointer",borderBottom:i<sCircles.length-1?`1px solid ${T.subtle}`:"none"}}>
              <div style={{width:36,height:36,borderRadius:"50%",background:T.card,border:`1px solid ${T.border}`,display:"flex",alignItems:"center",justifyContent:"center",color:T.textSecondary,fontSize:13,fontWeight:600,flexShrink:0}}>{circle.name.charAt(0)}</div>
              <div style={{flex:1}}><div style={{color:T.textPrimary,fontSize:13,fontWeight:600,marginBottom:4}}>{circle.name}</div><div style={{color:T.textSecondary,fontSize:11}}>{circle.upiIds.length} UPI {circle.upiIds.length===1?"ID":"IDs"}</div></div>
              <ChevronRight/>
            </div>
          ))}
        </div>
        {showAdd&&<Sheet onClose={resetAdd} title="New Circle"><div style={{marginBottom:24}}><FL c="NAME"/><TxtIn value={newCircleName} onChange={setNewCircleName} placeholder="e.g. Rahul"/></div><SaveBtn label="Create Circle" onPress={()=>{const t=newCircleName.trim();if(t){setSCircles(cs=>[...cs,{id:Date.now(),name:t,upiIds:[],txnIds:[]}]);resetAdd();}}} enabled={!!newCircleName.trim()}/></Sheet>}
      </div>
    );
  }

  const items=[{id:"rules",label:"Rules",desc:`${rules.length} active rules`},{id:"categories",label:"Categories",desc:`${categories.length} categories`},{id:"circles",label:"Circles",desc:`${sCircles.length} circles`}];
  return (
    <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
      <div style={{padding:"24px 20px 0"}}>
        <div style={{display:"flex",alignItems:"center",gap:14,marginBottom:24}}>
          <div onClick={onBack} style={{cursor:"pointer"}}><BackIcon/></div>
          <div style={{color:T.textPrimary,fontSize:16,fontWeight:600}}>Settings</div>
        </div>
      </div>
      <div style={{padding:"0 20px"}}>
        {items.map((item,i)=>(
          <div key={item.id} onClick={()=>{setScreen(item.id);resetAdd();setAddCatFor(categories[0]);}} style={{display:"flex",alignItems:"center",gap:14,paddingTop:16,paddingBottom:16,cursor:"pointer",borderBottom:i<items.length-1?`1px solid ${T.subtle}`:"none"}}>
            <div style={{flex:1}}><div style={{color:T.textPrimary,fontSize:13,fontWeight:600,marginBottom:4}}>{item.label}</div><div style={{color:T.textSecondary,fontSize:11}}>{item.desc}</div></div>
            <ChevronRight/>
          </div>
        ))}
      </div>
    </div>
  );
}

// No Permission
function NoPermission(){
  return (
    <div style={{flex:1,display:"flex",flexDirection:"column",alignItems:"center",justifyContent:"center",padding:"40px 32px",textAlign:"center"}}>
      <div style={{width:80,height:80,borderRadius:"50%",background:T.card,border:`1px solid ${T.border}`,display:"flex",alignItems:"center",justifyContent:"center",marginBottom:28}}><SmsIcon/></div>
      <div style={{color:T.textPrimary,fontSize:18,fontWeight:600,marginBottom:12,letterSpacing:-0.5}}>SMS access needed</div>
      <div style={{color:T.textSecondary,fontSize:13,lineHeight:1.7,marginBottom:40,maxWidth:280}}>This app reads your bank SMSes to track transactions automatically. Without this permission, nothing can be tracked.</div>
      <div style={{width:"100%",maxWidth:320,padding:"14px",borderRadius:12,background:T.textPrimary,color:T.bg,fontSize:13,fontWeight:700,textAlign:"center",cursor:"pointer",marginBottom:12}}>Open Settings</div>
      <div style={{color:T.textSecondary,fontSize:12,marginTop:4}}>Settings → Apps → Kubera → Permissions → SMS</div>
    </div>
  );
}

// Autopay Screen
function AutopayScreen(){
  const upcoming=[
    {id:1,name:"Netflix",amount:649,date:"5 Jul",type:"UPI",bank:"HDFC",color:"#ec4899"},
    {id:2,name:"Airtel",amount:499,date:"10 Jul",type:"UPI",bank:"HDFC",color:"#06b6d4"},
    {id:3,name:"Amazon Prime",amount:1499,date:"15 Jul",type:"Credit Card",bank:"HDFC",color:"#f97316"},
  ];
  return (
    <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>
      <div style={{padding:"24px 20px 0",marginBottom:8}}>
        <div style={{color:T.textPrimary,fontSize:18,fontWeight:600,letterSpacing:-0.5,marginBottom:4}}>Autopay</div>
        <div style={{color:T.textSecondary,fontSize:12}}>Recurring payments & subscriptions</div>
      </div>
      <div style={{flex:1,overflowY:"auto",padding:"16px 20px 40px"}}>
        <SL c="UPCOMING"/>
        {upcoming.map((item,i)=>(
          <div key={item.id} style={{display:"flex",alignItems:"center",gap:14,paddingTop:16,paddingBottom:16,borderBottom:i<upcoming.length-1?`1px solid ${T.subtle}`:"none"}}>
            <div style={{width:40,height:40,borderRadius:12,background:item.color+"22",border:`1px solid ${item.color}44`,display:"flex",alignItems:"center",justifyContent:"center",flexShrink:0}}>
              <div style={{width:8,height:8,borderRadius:"50%",background:item.color}}/>
            </div>
            <div style={{flex:1,minWidth:0}}>
              <div style={{color:T.textPrimary,fontSize:13,fontWeight:600,marginBottom:4}}>{item.name}</div>
              <div style={{color:T.textSecondary,fontSize:11}}>{item.type}<span style={{color:T.border,margin:"0 4px"}}>·</span>{item.bank}</div>
            </div>
            <div style={{textAlign:"right",flexShrink:0}}>
              <div style={{color:T.red,fontSize:14,fontWeight:700,letterSpacing:-0.5,marginBottom:3}}>₹{item.amount.toLocaleString("en-IN")}</div>
              <div style={{color:T.textSecondary,fontSize:10}}>{item.date}</div>
            </div>
          </div>
        ))}
        <div style={{marginTop:40,padding:"20px",borderRadius:16,border:`1px dashed ${T.border}`,textAlign:"center"}}>
          <div style={{color:T.textSecondary,fontSize:12,lineHeight:1.7,marginBottom:16}}>Autopay detection reads your bank SMSes and groups recurring charges automatically.</div>
          <div style={{color:T.textMuted,fontSize:11,letterSpacing:1}}>COMING SOON</div>
        </div>
      </div>
    </div>
  );
}

// Bottom Nav
const navItems=[
  {id:"home",label:"Home",Icon:({a})=><IconHome a={a}/>},
  {id:"analytics",label:"Analytics",Icon:({a})=><IconAnalytics a={a}/>},
  {id:"add",label:"",Icon:null},
  {id:"circles",label:"Circles",Icon:({a})=><IconCircles a={a}/>},
  {id:"autopay",label:"Autopay",Icon:({a})=><IconAutopay a={a}/>},
];

function BottomNav({active,setActive}){
  return (
    <div style={{background:T.card,borderTop:`1px solid ${T.border}`,padding:"10px 8px 24px",display:"flex",alignItems:"center",justifyContent:"space-around",flexShrink:0}}>
      {navItems.map(item=>{
        if(item.id==="add") return <div key="add" onClick={()=>setActive("add")} style={{width:50,height:50,borderRadius:"50%",background:T.textPrimary,display:"flex",alignItems:"center",justifyContent:"center",cursor:"pointer",marginTop:-20,boxShadow:`0 0 0 6px ${T.bg}`,flexShrink:0}}><PlusIconBtn color="#080808"/></div>;
        const a=active===item.id;
        return <div key={item.id} onClick={()=>setActive(item.id)} style={{display:"flex",flexDirection:"column",alignItems:"center",gap:4,cursor:"pointer",minWidth:44}}><item.Icon a={a}/><div style={{fontSize:10,color:a?T.textPrimary:T.textSecondary,fontWeight:a?600:400,letterSpacing:0.3}}>{item.label}</div></div>;
      })}
    </div>
  );
}

// App Root
export default function App(){
  const [hasPermission]=useState(true);
  const [activeTab,setActiveTab]=useState("home");
  const [screen,setScreen]=useState(null);
  const [categories,setCategories]=useState([...ALL_CATEGORIES]);

  const navigate=(name,data)=>setScreen({name,data});
  const goBack=()=>setScreen(null);
  const handleTab=(tab)=>{setScreen(null);setActiveTab(tab);};

  if(!hasPermission) return <div style={{background:T.bg,height:"100vh",fontFamily:T.font,display:"flex",flexDirection:"column"}}><NoPermission/></div>;

  const renderContent=()=>{
    if(screen?.name==="transactions") return <TransactionsScreen navigate={navigate} onBack={goBack} initFilters={screen.data?{categories:screen.data.categories||[],types:[],banks:[],flow:"all",dateFrom:null,dateTo:null}:undefined} categories={categories}/>;
    if(screen?.name==="txnDetail")    return <TxnDetail txn={screen.data} onBack={goBack} navigate={navigate}/>;
    if(screen?.name==="editTxn")      return <EditTxnScreen txn={screen.data} onBack={goBack} categories={categories} setCategories={setCategories}/>;
    switch(activeTab){
      case "home":      return <HomeScreen navigate={navigate} setTab={handleTab} categories={categories}/>;
      case "analytics": return <AnalyticsScreen onBack={()=>{}} navigate={(name,data)=>{setScreen({name,data});}}/>;
      case "circles":   return <CirclesScreen onBack={()=>{}} navigate={navigate}/>;
      case "autopay":   return <AutopayScreen/>;
      case "settings":  return <SettingsScreen onBack={()=>handleTab("home")} categories={categories} setCategories={setCategories}/>;
      case "add":       return <EditTxnScreen txn={null} onBack={()=>setActiveTab("home")} categories={categories} setCategories={setCategories}/>;
      default:          return <HomeScreen navigate={navigate} categories={categories}/>;
    }
  };

  return (
    <div style={{background:T.bg,height:"100vh",fontFamily:T.font,display:"flex",flexDirection:"column",maxWidth:430,margin:"0 auto",colorScheme:"dark"}}>
      <div style={{flex:1,display:"flex",flexDirection:"column",overflow:"hidden"}}>{renderContent()}</div>
      {activeTab!=="add"&&screen?.name!=="editTxn"&&<BottomNav active={screen?null:activeTab} setActive={handleTab}/>}
    </div>
  );
}
