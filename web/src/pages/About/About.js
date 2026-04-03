import Sidebar from "../../components/Sidebar";
import "../Dashboard/Dashboard.css";

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
        <div className="requests">
          <p style={{ color: "#6b7280" }}>About Us content coming soon.</p>
        </div>
      </div>
    </div>
  );
}