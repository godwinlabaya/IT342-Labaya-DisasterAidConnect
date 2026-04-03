import Sidebar from "../../components/Sidebar";
import "../Dashboard/Dashboard.css";

export default function DonationsPage() {
  return (
    <div className="dashboard">
      <Sidebar />
      <div className="main">
        <div className="header">
          <div className="header-left">
            <h1>Donations</h1>
            <p>Track and manage donations</p>
          </div>
        </div>
        <div className="requests">
          <p style={{ color: "#6b7280" }}>Donations content coming soon.</p>
        </div>
      </div>
    </div>
  );
}