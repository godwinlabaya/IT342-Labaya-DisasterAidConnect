import Sidebar from "../../components/Sidebar";
import "../Dashboard/Dashboard.css";

export default function MapPage() {
  return (
    <div className="dashboard">
      <Sidebar />
      <div className="main">
        <div className="header">
          <div className="header-left">
            <h1>Map</h1>
            <p>View disaster zones and aid distribution</p>
          </div>
        </div>
        <div className="requests">
          <p style={{ color: "#6b7280" }}>Map content coming soon.</p>
        </div>
      </div>
    </div>
  );
}