import { supabase } from "../supabaseClient";

const disasterService = {
  async getAll() {
    const { data, error } = await supabase
      .from("disasters")
      .select("*")
      .order("created_at", { ascending: false });
    if (error) throw error;
    return data;
  },

  async getByUser(userId) {
    const { data, error } = await supabase
      .from("disasters")
      .select("*")
      .eq("created_by", userId)
      .order("created_at", { ascending: false });
    if (error) throw error;
    return data;
  },

  async create(payload) {
    const { data, error } = await supabase
      .from("disasters")
      .insert([payload])
      .select()
      .single();
    if (error) throw error;
    return data;
  },

  async update(id, updates) {
    const { data, error } = await supabase
      .from("disasters")
      .update(updates)
      .eq("id", id)
      .select()
      .single();
    if (error) throw error;
    return data;
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