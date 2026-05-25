// web/src/features/auth/useMuteStatus.js
import { useEffect, useState, useCallback } from "react";
import { supabase } from "../../supabaseClient";

export function useMuteStatus(uid) {
  const [status, setStatus] = useState({
    isMuted:    false,
    muteUntil:  null,
    muteReason: null,
  });

  const check = useCallback(async () => {
    if (!uid) return;

    const { data, error } = await supabase
      .from("users")
      .select("is_muted, mute_until, mute_reason")
      .eq("id", uid)
      .single();

    if (error || !data) return;

    const expired =
      data.is_muted &&
      data.mute_until &&
      new Date(data.mute_until) <= new Date();

    // Auto-clear expired mute so the admin panel stays accurate
    if (expired) {
      await supabase
        .from("users")
        .update({ is_muted: false, mute_until: null, mute_reason: null })
        .eq("id", uid);
    }

    setStatus({
      isMuted:    data.is_muted && !expired,
      muteUntil:  data.mute_until,
      muteReason: data.mute_reason,
    });
  }, [uid]);

  useEffect(() => {
    check();
    const interval = setInterval(check, 60_000);
    return () => clearInterval(interval);
  }, [check]);

  return status;
}