import { supabase } from "../../supabaseClient";

const donationService = {

  // Create checkout session via Spring Boot backend → PayMongo
  async createCheckout({ userId, disasterId, amount }) {
    const response = await fetch("http://localhost:8080/api/payments/create", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, disasterId, amount }),
    });
    if (!response.ok) {
      const err = await response.json();
      throw new Error(err.error ?? "Payment failed");
    }
    return response.json(); // { checkoutUrl, donationId }
  },

  // Get all donations by user with disaster info
  async getByUser(userId) {
    const { data, error } = await supabase
      .from("donations")
      .select("*, disasters(id, title, latitude, longitude, severity_level, status)")
      .eq("user_id", userId)
      .order("donated_at", { ascending: false });
    if (error) throw error;
    return data;
  },

  // Get total donated by user (completed only)
  async getTotalDonated(userId) {
    const { data, error } = await supabase
      .from("donations")
      .select("amount")
      .eq("user_id", userId)
      .eq("status", "Completed");
    if (error) throw error;
    return data.reduce((sum, d) => sum + parseFloat(d.amount ?? 0), 0);
  },
};

export default donationService;