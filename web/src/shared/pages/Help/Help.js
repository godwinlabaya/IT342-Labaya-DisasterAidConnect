import Sidebar from "../../components/Sidebar";
import "../../../features/dashboard/Dashboard.css";
import "./Help.css";

const QUICK_LINKS = [
  { icon: "ti-map-2",        category: "Map",       desc: "Add disaster points" },
  { icon: "ti-clipboard-list", category: "Requests", desc: "Manage aid requests" },
  { icon: "ti-heart",        category: "Donations",  desc: "Send or track donations" },
  { icon: "ti-user",         category: "Account",    desc: "Profile & security" },
];

const FAQS = [
  {
    q: "How do I add a disaster point on the map?",
    a: 'Go to the Map page, click the "Add Point" button in the top bar, then click anywhere on the map to place a pin. Fill in the title, description, severity, and status, then click Save.',
  },
  {
    q: "Who can see the disaster points I add?",
    a: "All disaster points are visible to every user on the platform. Only you (the creator) can edit or delete your own points.",
  },
  {
    q: "How do I report or delete a point I didn't create?",
    a: "Currently only the creator of a point can delete it. If you believe a point is inaccurate or harmful, contact the platform administrator.",
  },
  {
    q: "How do I make a donation?",
    a: "Navigate to the Donations page from the sidebar. Select a disaster, enter your donation amount, choose a payment method, and complete the transaction.",
  },
  {
    q: "How do I change my password?",
    a: 'Password recovery is handled through the login screen using your security question. Click "Forgot Password?" on the login page and follow the prompts.',
  },
  {
    q: "Can I sign in with Google?",
    a: 'Yes. On the login page, click "Sign in with Google" and authorize the app. Your account will be linked automatically.',
  },
];

export default function HelpPage() {
  return (
    <div className="dashboard">
      <Sidebar />

      <div className="main">

        <div className="header">
          <div className="header-left">
            <h1>Help &amp; Support</h1>
            <p>Find answers and get assistance</p>
          </div>
        </div>

        <div className="help-body">

          {/* ── Quick links ── */}
          <div className="help-quick-grid">
            {QUICK_LINKS.map((l) => (
              <div key={l.category} className="help-quick-card">
                <div className="help-quick-icon">
                  <i className={`ti ${l.icon}`} aria-hidden="true" />
                </div>
                <div>
                  <p className="help-quick-category">{l.category}</p>
                  <p className="help-quick-desc">{l.desc}</p>
                </div>
              </div>
            ))}
          </div>

          {/* ── FAQ ── */}
          <div className="help-card">
            <p className="help-card-label">Frequently asked questions</p>
            <div className="help-faq-list">
              {FAQS.map((item, i) => (
                <div
                  key={i}
                  className={`help-faq-item ${i < FAQS.length - 1 ? "help-faq-border" : ""}`}
                >
                  <p className="help-faq-q">{item.q}</p>
                  <p className="help-faq-a">{item.a}</p>
                </div>
              ))}
            </div>
          </div>

          {/* ── Contact ── */}
          <div className="help-card">
            <p className="help-card-label">Contact &amp; support</p>
            <div className="help-contact-row">
              <div className="help-contact-avatar">GL</div>
              <div className="help-contact-info">
                <p className="help-contact-name">Godwin Labaya</p>
                <p className="help-contact-role">Developer &amp; platform maintainer</p>
              </div>
              <div className="help-contact-details">
                <div className="help-contact-detail">
                  <div className="help-contact-detail-icon">
                    <i className="ti ti-mail" aria-hidden="true" />
                  </div>
                  <span className="help-contact-email">godwin@email.com</span>
                </div>
                <div className="help-contact-detail">
                  <div className="help-contact-detail-icon">
                    <i className="ti ti-school" aria-hidden="true" />
                  </div>
                  <span>CIT University, Cebu — IT342</span>
                </div>
              </div>
            </div>
          </div>

          {/* ── Notice ── */}
          <div className="help-notice">
            <div className="help-notice-icon">
              <i className="ti ti-info-circle" aria-hidden="true" />
            </div>
            <p className="help-notice-text">
              Disaster Aid Connect is a capstone project built for IT342. For urgent issues,
              contact the developer directly via email.
            </p>
          </div>

        </div>
      </div>
    </div>
  );
}