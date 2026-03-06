import { createClient } from "@supabase/supabase-js";

const supabaseUrl = "https://wvwvxwkbjnvsrvnmrxsr.supabase.co";
const supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind2d3Z4d2tiam52c3J2bm1yeHNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI3OTc3NjgsImV4cCI6MjA4ODM3Mzc2OH0.HOpufPceHZLuH-Lxoa1RoP1oZXmw9CA_rOBXFKqECpg";

export const supabase = createClient(supabaseUrl, supabaseAnonKey);