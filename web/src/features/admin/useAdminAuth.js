import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../../supabaseClient";

export function useAdminAuth() {
  const [username, setUsername] = useState("");
  const [loading,  setLoading]  = useState(true);
  const [authed,   setAuthed]   = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    supabase.auth.getSession().then(async ({ data: { session } }) => {
      try {
        if (!session) {
          navigate("/");
          return;
        }

        const { data: userData } = await supabase
          .from("users")
          .select("role, username")
          .eq("id", session.user.id)
          .single();

        if (userData?.role !== "admin") {
          navigate("/dashboard");
          return;
        }

        setUsername(userData.username ?? "Admin");
        setAuthed(true);
      } finally {
        // Always runs — prevents the component from being stuck
        // in loading state regardless of auth outcome
        setLoading(false);
      }
    });
  }, [navigate]);

  return { username, loading, authed };
}