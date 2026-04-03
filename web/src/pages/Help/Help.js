import Sidebar from "../../components/Sidebar";
import "../Dashboard/Dashboard.css";

export default function HelpPage() {
  return (
    <div className="dashboard">
      <Sidebar />
      <div className="main">
        <div className="header">
          <div className="header-left">
            <h1>Help & Support</h1>
            <p>Find answers and get assistance</p>
          </div>
        </div>
        <div className="requests">
          <p style={{ color: "#6b7280" }}>Help & Support content coming soon.</p>
        </div>
      </div>
    </div>
  );
}