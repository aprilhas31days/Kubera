const T = {
  bg: "#080808",
  card: "#0e0e0e",
  border: "#1e1e1e",
  textPrimary: "#f0ebe3",
  textSecondary: "#555",
  textMuted: "#383838",
  font: "system-ui",
};

const SmsIcon = () => (
  <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke={T.textMuted} strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
    <line x1="9" y1="10" x2="15" y2="10"/>
    <line x1="9" y1="14" x2="12" y2="14"/>
  </svg>
);

export default function NoPermission() {
  return (
    <div style={{
      background: T.bg, minHeight: "100vh", fontFamily: T.font,
      display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      padding: "40px 32px", textAlign: "center",
    }}>

      {/* Icon */}
      <div style={{
        width: 80, height: 80, borderRadius: "50%",
        background: T.card, border: `1px solid ${T.border}`,
        display: "flex", alignItems: "center", justifyContent: "center",
        marginBottom: 28,
      }}>
        <SmsIcon />
      </div>

      {/* Text */}
      <div style={{ color: T.textPrimary, fontSize: 18, fontWeight: 600, marginBottom: 12, letterSpacing: -0.5 }}>
        SMS access needed
      </div>
      <div style={{ color: T.textSecondary, fontSize: 13, lineHeight: 1.7, marginBottom: 40, maxWidth: 280 }}>
        This app reads your bank SMSes to track transactions automatically. Without this permission, nothing can be tracked.
      </div>

      {/* Primary CTA */}
      <div onClick={() => {}} style={{
        width: "100%", maxWidth: 320,
        padding: "14px", borderRadius: 12,
        background: T.textPrimary, color: T.bg,
        fontSize: 13, fontWeight: 700, textAlign: "center",
        cursor: "pointer", marginBottom: 12,
      }}>
        Open Settings
      </div>

      {/* Secondary */}
      <div style={{ color: T.textSecondary, fontSize: 12, marginTop: 4 }}>
        Settings → Apps → Paisa → Permissions → SMS
      </div>

    </div>
  );
}
