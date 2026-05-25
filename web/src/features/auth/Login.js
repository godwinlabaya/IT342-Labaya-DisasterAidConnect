import { useState } from "react";
import { supabase } from "../../supabaseClient";
import "./Login.css";
import { useNavigate, Link } from "react-router-dom";

function Toast({ message, type }) {
  if (!message) return null;
  return (
    <div className={`toast toast-${type}`}>
      <i className={`ti ${type === "success" ? "ti-circle-check" : "ti-alert-circle"} toast-icon`} aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}

// ── Password input with eye toggle ──────────────────────────────────────────
function PasswordInput({ name, placeholder, onChange }) {
  const [show, setShow] = useState(false);
  return (
    <div className="password-wrap">
      <input
        name={name}
        type={show ? "text" : "password"}
        placeholder={placeholder}
        onChange={onChange}
        required
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

// ── Forgot Password modal ────────────────────────────────────────────────────
function ForgotPasswordModal({ onClose }) {
  const [step,     setStep]     = useState(1);
  const [email,    setEmail]    = useState("");
  const [question, setQuestion] = useState("");
  const [answer,   setAnswer]   = useState("");
  const [newPass,  setNewPass]  = useState("");
  const [showPass, setShowPass] = useState(false);
  const [userId,   setUserId]   = useState(null);
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState("");

  const handleEmailSubmit = async () => {
    if (!email.trim()) { setError("Please enter your email."); return; }
    setLoading(true); setError("");
    const { data, error: err } = await supabase
      .from("users")
      .select("id, security_question")
      .eq("email", email.trim().toLowerCase())
      .single();
    if (err || !data) { setError("No account found with that email."); setLoading(false); return; }
    setUserId(data.id);
    setQuestion(data.security_question ?? "");
    setStep(2);
    setLoading(false);
  };

  const handleAnswerSubmit = async () => {
    if (!answer.trim()) { setError("Please enter your answer."); return; }
    setLoading(true); setError("");
    const { data, error: err } = await supabase
      .from("users")
      .select("security_answer")
      .eq("id", userId)
      .single();
    if (err || !data) { setError("Something went wrong."); setLoading(false); return; }
    if (data.security_answer?.toLowerCase() !== answer.trim().toLowerCase()) {
      setError("Incorrect answer. Please try again.");
      setLoading(false); return;
    }
    setStep(3);
    setLoading(false);
  };

  const handlePasswordReset = async () => {
    const passwordRegex = /^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/;
    if (!passwordRegex.test(newPass)) {
      setError("Password must be 8+ characters with an uppercase letter and a special character.");
      return;
    }
    setLoading(true); setError("");
    const { error: rpcErr } = await supabase.rpc("update_user_password", {
      p_user_id: userId,
      p_new_password: newPass,
    });
    if (rpcErr) {
      setError("Failed to reset password. Please contact support.");
      setLoading(false); return;
    }
    setStep(4);
    setLoading(false);
  };

  return (
    <div className="fp-backdrop" onClick={onClose}>
      <div className="fp-modal" onClick={(e) => e.stopPropagation()}>

        <div className="fp-header">
          <div className="fp-icon">
            <i className="ti ti-lock" aria-hidden="true" />
          </div>
          <h3>Reset Password</h3>
          {step < 4 && (
            <p className="fp-sub">
              {step === 1 && "Enter your email address to get started."}
              {step === 2 && "Answer your security question to verify your identity."}
              {step === 3 && "Choose a new password for your account."}
            </p>
          )}
        </div>

        {step < 4 && (
          <div className="fp-steps">
            {[1, 2, 3].map((s) => (
              <div key={s} className={`fp-step ${step >= s ? "fp-step-active" : ""}`}>
                {step > s ? <i className="ti ti-check" aria-hidden="true" /> : s}
              </div>
            ))}
          </div>
        )}

        {step === 1 && (
          <div className="fp-body">
            <div className="fp-field">
              <label>Email Address</label>
              <input
                type="email"
                placeholder="you@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleEmailSubmit()}
              />
            </div>
            {error && <p className="fp-error">{error}</p>}
            <button className="fp-btn" onClick={handleEmailSubmit} disabled={loading}>
              {loading ? "Checking…" : "Continue"}
              <i className="ti ti-arrow-right" aria-hidden="true" />
            </button>
          </div>
        )}

        {step === 2 && (
          <div className="fp-body">
            <div className="fp-question-box">
              <p className="fp-question-label">Your security question</p>
              <p className="fp-question-text">{question || "No security question set."}</p>
            </div>
            <div className="fp-field">
              <label>Your Answer</label>
              <input
                type="text"
                placeholder="Enter your answer"
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleAnswerSubmit()}
              />
            </div>
            {error && <p className="fp-error">{error}</p>}
            <button className="fp-btn" onClick={handleAnswerSubmit} disabled={loading}>
              {loading ? "Verifying…" : "Verify Answer"}
              <i className="ti ti-arrow-right" aria-hidden="true" />
            </button>
            <button className="fp-back" onClick={() => { setStep(1); setError(""); }}>
              <i className="ti ti-arrow-left" aria-hidden="true" /> Back
            </button>
          </div>
        )}

        {step === 3 && (
          <div className="fp-body">
            <div className="fp-field">
              <label>New Password</label>
              <div className="password-wrap">
                <input
                  type={showPass ? "text" : "password"}
                  placeholder="••••••••••••"
                  value={newPass}
                  onChange={(e) => setNewPass(e.target.value)}
                />
                <button
                  type="button"
                  className="password-eye"
                  onClick={() => setShowPass((v) => !v)}
                >
                  <i className={`ti ${showPass ? "ti-eye-off" : "ti-eye"}`} aria-hidden="true" />
                </button>
              </div>
              <p className="fp-hint">8+ characters, one uppercase, one special character</p>
            </div>
            {error && <p className="fp-error">{error}</p>}
            <button className="fp-btn" onClick={handlePasswordReset} disabled={loading}>
              {loading ? "Resetting…" : "Reset Password"}
              <i className="ti ti-check" aria-hidden="true" />
            </button>
            <button className="fp-back" onClick={() => { setStep(2); setError(""); }}>
              <i className="ti ti-arrow-left" aria-hidden="true" /> Back
            </button>
          </div>
        )}

        {step === 4 && (
          <div className="fp-body fp-success">
            <div className="fp-success-icon">
              <i className="ti ti-circle-check" aria-hidden="true" />
            </div>
            <p className="fp-success-title">Password reset!</p>
            <p className="fp-success-sub">
              Your password has been successfully updated. You can now sign in with your new password.
            </p>
            <button className="fp-btn" onClick={onClose}>Back to Login</button>
          </div>
        )}

        <button className="fp-close" onClick={onClose} aria-label="Close">
          <i className="ti ti-x" aria-hidden="true" />
        </button>
      </div>
    </div>
  );
}

// ── Main Login ───────────────────────────────────────────────────────────────
export default function Login() {
  const [form,       setForm]       = useState({ email: "", password: "" });
  const [toast,      setToast]      = useState({ message: "", type: "" });
  const [showForgot, setShowForgot] = useState(false);
  const navigate = useNavigate();

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "" }), 3000);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const { data, error } = await supabase.auth.signInWithPassword({
      email: form.email,
      password: form.password,
    });
    if (error) { showToast(error.message, "error"); return; }

    const { data: userData } = await supabase
      .from("users")
      .select("role")
      .eq("id", data.user.id)
      .single();

    const role = userData?.role ?? "user";
    if (role === "admin") {
      showToast("Welcome back, Admin! Redirecting…", "success");
      setTimeout(() => navigate("/admin/dashboard"), 1500);
    } else {
      showToast("Successfully logged in! Redirecting…", "success");
      setTimeout(() => navigate("/dashboard"), 1500);
    }
  };

  return (
    <div className="login-container">
      <Toast message={toast.message} type={toast.type} />

      {/* LEFT SIDE */}
      <div className="login-left">
        <div className="brand">
          <div className="logo-box"></div>
          <h1><span className="blue">DISASTER</span>AIDCONNECT</h1>
        </div>
        <h2>Transform Crisis Into Coordinated Action</h2>
        <p className="description">
          Connects communities, volunteers, and aid organizations in real time.
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

      {/* RIGHT SIDE */}
      <div className="login-right">
        <div className="login-card">
          <h2>Welcome back</h2>
          <p className="subtitle">Sign in to your account to continue</p>
          <form onSubmit={handleSubmit}>
            <div>
              <label>Email Address</label>
              <input name="email" type="email" placeholder="you@email.com" onChange={handleChange} required />
            </div>
            <div>
              <label>Password</label>
              <PasswordInput name="password" placeholder="••••••••••••" onChange={handleChange} />
            </div>
            <div className="login-options">
              <label><input type="checkbox" /> Remember Me</label>
              <button type="button" className="forgot-link" onClick={() => setShowForgot(true)}>
                Forgot Password?
              </button>
            </div>
            <button type="submit">SIGN IN</button>
          </form>
          <div className="divider"><span>OR</span></div>
          <p className="register-text">
            Don't have an account? <Link to="/register">Sign up</Link>
          </p>
        </div>
      </div>

      {showForgot && <ForgotPasswordModal onClose={() => setShowForgot(false)} />}
    </div>
  );
}