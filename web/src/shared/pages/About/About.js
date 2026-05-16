import Sidebar from "../../components/Sidebar";
import "../../../features/dashboard/Dashboard.css";
import "./About.css";

// import myPhoto from "../../assets/godwin.jpg";
const myPhoto = null;

const ROLES = [
  "Full-stack developer",
  "UI/UX designer",
  "Database architect",
  "DevOps",
  "Project manager",
];

const STACK = [
  "React",
  "Spring Boot",
  "Supabase",
  "PostgreSQL",
  "Java",
  "JavaScript",
];

const FEATURES = [
  {
    icon: "ti-map-2",
    title: "Disaster mapping",
    description: "Report and track disaster events on an interactive live map with severity indicators.",
  },
  {
    icon: "ti-clipboard-list",
    title: "Aid requests",
    description: "Submit and manage requests for food, water, medical assistance, and shelter.",
  },
  {
    icon: "ti-heart",
    title: "Donations",
    description: "Send financial aid directly to affected communities through a secure payment system.",
  },
  {
    icon: "ti-users",
    title: "Community network",
    description: "Connect volunteers, NGOs, and local responders under one unified platform.",
  },
];

const PROJECT_INFO = [
  { label: "Course",      value: "IT342 — Web Systems & Technologies" },
  { label: "Institution", value: "CIT University, Cebu" },
  { label: "Tech stack",  value: "React · Spring Boot · Supabase" },
  { label: "Version",     value: "1.0.0 — 2026" },
];

export default function AboutPage() {
  return (
    <div className="dashboard">
      <Sidebar />

      <div className="main">

        <div className="header">
          <div className="header-left">
            <h1>About Us</h1>
            <p>Learn more about Disaster Aid Connect</p>
          </div>
        </div>

        <div className="about-body">

          {/* ── Hero ── */}
          <div className="about-hero">
            <div className="about-hero-icon">
              <i className="ti ti-map-2" aria-hidden="true" />
            </div>
            <div>
              <p className="about-platform-label">About the platform</p>
              <h2 className="about-platform-title">Disaster Aid Connect</h2>
            </div>
          </div>

          <p className="about-tagline">
            A real-time coordination platform that bridges communities, volunteers, and aid
            organizations during disasters — making relief efforts faster, smarter, and more
            effective.
          </p>

          {/* ── Stats ── */}
          <div className="about-stats">
            <div className="about-stat">
              <p className="about-stat-label">Disaster reports</p>
              <p className="about-stat-value">Active</p>
            </div>
            <div className="about-stat">
              <p className="about-stat-label">Aid coordination</p>
              <p className="about-stat-value">Real-time</p>
            </div>
            <div className="about-stat">
              <p className="about-stat-label">Platform status</p>
              <p className="about-stat-value about-stat-green">Operational</p>
            </div>
          </div>

          {/* ── Feature cards ── */}
          <div className="about-features">
            {FEATURES.map((f) => (
              <div key={f.title} className="about-feature-card">
                <div className="about-feature-icon">
                  <i className={`ti ${f.icon}`} aria-hidden="true" />
                </div>
                <p className="about-feature-title">{f.title}</p>
                <p className="about-feature-desc">{f.description}</p>
              </div>
            ))}
          </div>

          {/* ── Developer card ── */}
          <div className="about-card about-dev-card">
            <p className="about-card-label">The developer</p>

            <div className="about-dev-inner">
              <div className="about-dev-photo-col">
                <div className="about-dev-avatar">
                  {myPhoto
                    ? <img src={myPhoto} alt="Godwin Labaya" className="about-dev-img" />
                    : <span className="about-dev-initials">GL</span>
                  }
                </div>
                <p className="about-dev-name">Godwin Labaya</p>
                <p className="about-dev-sub">Solo developer</p>
              </div>

              <div className="about-dev-info">
                <p className="about-dev-section-label">Roles &amp; responsibilities</p>
                <div className="about-pills">
                  {ROLES.map((r) => (
                    <span key={r} className="about-pill about-pill-blue">{r}</span>
                  ))}
                </div>

                <p className="about-dev-section-label" style={{ marginTop: "1rem" }}>Tech stack</p>
                <div className="about-pills">
                  {STACK.map((t) => (
                    <span key={t} className="about-pill about-pill-gray">{t}</span>
                  ))}
                </div>

                <p className="about-dev-section-label" style={{ marginTop: "1rem" }}>About</p>
                <p className="about-dev-bio">
                  IT342 student at CIT University, Cebu. Built Disaster Aid Connect as a solo
                  capstone project — designing, developing, and deploying the entire platform
                  from scratch.
                </p>
              </div>
            </div>
          </div>

          {/* ── Project info ── */}
          <div className="about-card">
            <p className="about-card-label">Project info</p>
            <div className="about-info-list">
              {PROJECT_INFO.map((item, i) => (
                <div
                  key={item.label}
                  className={`about-info-row ${i < PROJECT_INFO.length - 1 ? "about-info-row-border" : ""}`}
                >
                  <span className="about-info-label">{item.label}</span>
                  <span className="about-info-value">{item.value}</span>
                </div>
              ))}
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}