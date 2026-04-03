import Sidebar from "../../components/Sidebar";
import "../Dashboard/Dashboard.css";

export default function RequestsPage() {
  return (
    <div className="dashboard">
      <Sidebar />
      <div className="main">
        <div className="header">
          <div className="header-left">
            <h1>Requests</h1>
            <p>Manage and track aid requests</p>
          </div>
        </div>
        <div className="requests">
          <p style={{ color: "#6b7280" }}>Requests content coming soon.</p>
        </div>
      </div>
    </div>
  );
}