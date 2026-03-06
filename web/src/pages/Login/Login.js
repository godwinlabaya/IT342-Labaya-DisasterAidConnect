import { useState } from "react";
import { supabase } from "../../supabaseClient";
import "./Login.css";
import { useNavigate, Link } from "react-router-dom";

export default function Login() {
  const [form, setForm] = useState({ email: "", password: "" });
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const { error } = await supabase.auth.signInWithPassword({
      email: form.email,
      password: form.password,
    });

    if (error) {
      alert(error.message);
    } else {
      navigate("/dashboard");
    }
  };

  return (
    <div className="login-container">
      
      {/* LEFT SIDE */}
      <div className="login-left">

        <div className="brand">
          <div className="logo-box"></div>
          <h1>
            <span className="blue">DISASTER</span>AIDCONNECT
          </h1>
        </div>

        <h2>Transform Crisis Into Coordinated Action</h2>

        <p className="description">
          Connect communities, volunteers, and aid organizations in real time.
          Disaster Aid Connect helps streamline relief efforts, allocate resources
          efficiently, and support those affected when it matters most.
        </p>

        <div className="features">

          <div className="feature">
            <div className="icon-box"></div>
            <div>
              <h4>Coordinate Relief Efforts</h4>
              <p>Manage requests, track aid distribution, and monitor response progress in one unified platform.</p>
            </div>
          </div>

          <div className="feature">
            <div className="icon-box"></div>
            <div>
              <h4>Connect Volunteers & Organizations</h4>
              <p>Bring together certified responders, NGOs, and local volunteers to work seamlessly during emergencies.</p>
            </div>
          </div>

          <div className="feature">
            <div className="icon-box"></div>
            <div>
              <h4>Deliver Critical Resources</h4>
              <p>Match supplies, shelter, and medical assistance with communities in urgent need.</p>
            </div>
          </div>

        </div>
      </div>

      {/* RIGHT SIDE */}
      <div className="login-right">

        <div className="login-card">
          <h2>Welcome back</h2>
          <p className="subtitle">Sign in to your account to continue</p>

          <form onSubmit={handleSubmit}>
            <div>
              <label>Email Address</label>
              <input
                name="email"
                type="email"
                placeholder="you@email.com"
                onChange={handleChange}
                required
              />
            </div>

            <div>
              <label>Password</label>
              <input
                name="password"
                type="password"
                placeholder="••••••••••••"
                onChange={handleChange}
                required
              />
            </div>

            <div className="login-options">
              <label>
                <input type="checkbox" />
                Remember Me
              </label>

              <a href="#">Forgot Password?</a>
            </div>

            <button type="submit">SIGN IN</button>
          </form>

          <div className="divider">
            <span>OR</span>
          </div>

          <p className="register-text">
            Don't have an account? <Link to="/register">Sign up</Link>
          </p>

        </div>

      </div>
    </div>
  );
}