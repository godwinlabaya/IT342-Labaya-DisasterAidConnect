import { supabase } from "../../supabaseClient";

const disasterService = {
  async getAll() {
    const { data, error } = await supabase
      .from("disasters")
      .select("*, users(username)")
      .order("created_at", { ascending: false });
    if (error) throw error;
    return (data || []).map((d) => ({
      ...d,
      creator_username: d.users?.username ?? "Unknown",
    }));
  },

  async getByUser(userId) {
    const { data, error } = await supabase
      .from("disasters")
      .select("*, users(username)")
      .eq("created_by", userId)
      .order("created_at", { ascending: false });
    if (error) throw error;
    return (data || []).map((d) => ({
      ...d,
      creator_username: d.users?.username ?? "Unknown",
    }));
  },

  async create(payload) {
    const { data, error } = await supabase
      .from("disasters")
      .insert([payload])
      .select("*, users(username)")
      .single();
    if (error) throw error;
    return { ...data, creator_username: data.users?.username ?? "Unknown" };
  },

  async update(id, updates) {
    const { data, error } = await supabase
      .from("disasters")
      .update(updates)
      .eq("id", id)
      .select("*, users(username)")
      .single();
    if (error) throw error;
    return { ...data, creator_username: data.users?.username ?? "Unknown" };
  },

  async remove(id) {
    const { error } = await supabase
      .from("disasters")
      .delete()
      .eq("id", id);
    if (error) throw error;
  },
};

export default disasterService;