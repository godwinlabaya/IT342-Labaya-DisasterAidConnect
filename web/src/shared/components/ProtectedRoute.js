import { Navigate } from "react-router-dom";
import { supabase } from "../../supabaseClient";
import { useEffect, useState } from "react";

export default function ProtectedRoute({ children }) {
  const [session, setSession] = useState(undefined); 
  // undefined = still loading

  useEffect(() => {
    const getSession = async () => {
      const { data: { session } } = await supabase.auth.getSession();
      setSession(session);
    };

    getSession();
  }, []);

  // ⏳ While checking session
  if (session === undefined) {
    return null; // or loading spinner
  }

  // ❌ Not logged in
  if (!session) {
    return <Navigate to="/" />;
  }

  // ✅ Logged in
  return children;
}