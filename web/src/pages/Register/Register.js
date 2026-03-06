import { useState } from "react";
import "./Register.css";  // ← Make sure this path is correct
import { supabase } from "../../supabaseClient";
import { Link, useNavigate } from "react-router-dom";

export default function Register() {
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    securityQuestion: "",
    securityAnswer: ""
  });

  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (form.password !== form.confirmPassword) {
      alert("Passwords do not match");
      return;
    }

    const passwordRegex =
    /^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/

    if (!passwordRegex.test(form.password)) {
      alert("Password must be at least 8 characters long and contain an uppercase letter and a special character.");
      return;
    }

    const { data, error } = await supabase.auth.signUp({
      email: form.email,
      password: form.password,
      options: {
        data: {
          username: form.username,
        },
      },
    });
    
    if (error) {
      alert(error.message);
      return;
    }

    if (data.user) {
      const { error: insertError } = await supabase.from("users").insert([
        {
          id: data.user.id,
          username: form.username,
          email: form.email,
          security_question: form.securityQuestion,
          security_answer: form.securityAnswer,
          role: "user"
        }
      ]);

      if (insertError) {
        alert(insertError.message);
        return;
      }

      alert("Succesfully registered!");
      navigate("/");
    }
  };

  return (
    <div className="register-container">

      {/* LEFT PANEL */}
      <div className="register-left">

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

      {/* RIGHT PANEL */}
      <div className="register-right">

        <div className="register-card">

          <h2>Create Account</h2>
          <p className="subtitle">Join DisasterAidConnect and start your journey</p>

          <form onSubmit={handleSubmit}>

            <div>
              <label>Username</label>
              <input
                name="username"
                placeholder="Your name"
                onChange={handleChange}
                required
              />
            </div>

            <div>
              <label>Email</label>
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
                minLength="8"
              />
            </div>

            <div>
              <label>Confirm Password</label>
              <input
                name="confirmPassword"
                type="password"
                placeholder="••••••••••••"
                onChange={handleChange}
                required
              />
            </div>

            <div>
              <label>Security Question (for password recovery)</label>
              <input
                name="securityQuestion"
                placeholder="What is your mother's maiden name?"
                onChange={handleChange}
                required
              />
            </div>

            <div>
              <label>Answer to Security Question</label>
              <input
                name="securityAnswer"
                placeholder="Your answer (remember this)"
                onChange={handleChange}
                required
              />
            </div>

            <button type="submit">SIGN UP</button>

          </form>

          <div className="divider">
            <span>OR</span>
          </div>

          <p className="login-text">
            Already have an account? <Link to="/">Log in</Link>
          </p>

        </div>

      </div>

    </div>
  );
}