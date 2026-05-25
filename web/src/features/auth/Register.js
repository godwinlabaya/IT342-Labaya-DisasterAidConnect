import { useState } from "react";
import "./Register.css";
import { supabase } from "../../supabaseClient";
import { Link, useNavigate } from "react-router-dom";

function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <i className={`ti ${type === "success" ? "ti-circle-check" : "ti-alert-circle"} toast-icon`} aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}

// ── Password input with eye toggle ────────────────────────────────────────────
function PasswordInput({ name, placeholder, onChange, minLength }) {
  const [show, setShow] = useState(false);
  return (
    <div className="password-wrap">
      <input
        name={name}
        type={show ? "text" : "password"}
        placeholder={placeholder}
        onChange={onChange}
        required
        minLength={minLength}
      />
      <button
        type="button"
        className="password-eye"
        onClick={() => setShow((v) => !v)}
        aria-label={show ? "Hide password" : "Show password"}
      >
        <i className={`ti ${show ? "ti-eye-off" : "ti-eye"}`} aria-hidden="true" />
      </button>
    </div>
  );
}

export default function Register() {
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    securityQuestion: "",
    securityAnswer: ""
  });
  const [toast, setToast] = useState({ message: "", type: "" });
  const navigate = useNavigate();

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "" }), 3000);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (form.password !== form.confirmPassword) {
      showToast("Passwords do not match.", "error"); return;
    }

    const passwordRegex = /^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/;
    if (!passwordRegex.test(form.password)) {
      showToast("Password must be 8+ characters with an uppercase letter and special character.", "error");
      return;
    }

    if (!form.securityQuestion) {
      showToast("Please select a security question.", "error"); return;
    }

    const { data, error } = await supabase.auth.signUp({
      email: form.email,
      password: form.password,
      options: { data: { username: form.username } },
    });

    if (error) { showToast(error.message, "error"); return; }

    if (data.user) {
      const { error: insertError } = await supabase.from("users").insert([{
        id: data.user.id,
        username: form.username,
        email: form.email,
        security_question: form.securityQuestion,
        security_answer: form.securityAnswer,
        role: "user"
      }]);

      if (insertError) { showToast(insertError.message, "error"); return; }

      showToast("Account created successfully! Redirecting to login…", "success");
      setTimeout(() => navigate("/"), 2000);
    }
  };

  return (
    <div className="register-container">
      <Toast message={toast.message} type={toast.type} />

      {/* LEFT PANEL */}
      <div className="register-left">
        <div className="brand">
          <div className="logo-box"></div>
          <h1><span className="blue">DISASTER</span>AIDCONNECT</h1>
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
              <h4>Connect Volunteers &amp; Organizations</h4>
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

      {/* RIGHT PANEL */}
      <div className="register-right">
        <div className="register-card">
          <h2>Create Account</h2>
          <p className="subtitle">Join DisasterAidConnect and start your journey</p>

          <form onSubmit={handleSubmit}>
            <div>
              <label>Username</label>
              <input name="username" placeholder="Your name" onChange={handleChange} required />
            </div>
            <div>
              <label>Email</label>
              <input name="email" type="email" placeholder="you@email.com" onChange={handleChange} required />
            </div>
            <div>
              <label>Password</label>
              <PasswordInput name="password" placeholder="••••••••••••" onChange={handleChange} minLength={8} />
            </div>
            <div>
              <label>Confirm Password</label>
              <PasswordInput name="confirmPassword" placeholder="••••••••••••" onChange={handleChange} />
            </div>
            <div>
              <label>Security Question (for password recovery)</label>
              <select name="securityQuestion" onChange={handleChange} required>
                <option value="">— Select a question —</option>
                <option>What is your mother's maiden name?</option>
                <option>What is your childhood nickname?</option>
                <option>What is your first pet's name?</option>
                <option>What is your favorite color?</option>
                <option>What city were you born in?</option>
                <option>What is the name of your elementary school?</option>
                <option>What was the make of your first car?</option>
              </select>
            </div>
            <div>
              <label>Answer to Security Question</label>
              <input name="securityAnswer" placeholder="Your answer (remember this)" onChange={handleChange} required />
            </div>
            <button type="submit">SIGN UP</button>
          </form>

          <div className="divider"><span>OR</span></div>
          <p className="login-text">
            Already have an account? <Link to="/">Log in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}