import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../supabaseClient";

export function useAuth({ redirectIfUnauthenticated = true } = {}) {
  const [session,  setSession]  = useState(null);
  const [userId,   setUserId]   = useState(null);
  const [username, setUsername] = useState("");
  const [loading,  setLoading]  = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      if (!session && redirectIfUnauthenticated) {
        navigate("/");
        return;
      }
      setSession(session);
      setUserId(session?.user?.id ?? null);
      setLoading(false);
    });

    const { data: { subscription } } = supabase.auth.onAuthStateChange(
      (_event, session) => {
        if (!session && redirectIfUnauthenticated) {
          navigate("/");
          return;
        }
        setSession(session);
        setUserId(session?.user?.id ?? null);
      }
    );

    return () => subscription.unsubscribe();
  }, [navigate, redirectIfUnauthenticated]);

  useEffect(() => {
    if (!userId) return;
    supabase
      .from("users")
      .select("username")
      .eq("id", userId)
      .single()
      .then(({ data }) => { if (data) setUsername(data.username); });
  }, [userId]);

  return { session, userId, username, loading };
}