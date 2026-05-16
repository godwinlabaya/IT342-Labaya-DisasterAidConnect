import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../../supabaseClient";

export function useAdminAuth() {
  const [username, setUsername] = useState("");
  const [loading,  setLoading]  = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    supabase.auth.getSession().then(async ({ data: { session } }) => {
      if (!session) {
        navigate("/");
        return;
      }

      // ── Check role — redirect non-admins away ──────────────────────────
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
      setLoading(false);
    });
  }, [navigate]);

  return { username, loading };
}