// src/utils/iconFactory.js
import L from "leaflet";

export function getSeverityColor(level) {
  if (level === "Critical") return "#7c3aed";
  if (level === "High")     return "#ef4444";
  if (level === "Medium")   return "#f97316";
  return "#3b82f6";
}

export function getSeverityStyle(level) {
  if (level === "Critical") return { bg: "#f3e8ff", text: "#7c3aed", border: "#d8b4fe" };
  if (level === "High")     return { bg: "#fee2e2", text: "#dc2626", border: "#fca5a5" };
  if (level === "Medium")   return { bg: "#ffedd5", text: "#ea580c", border: "#fdba74" };
  return                           { bg: "#dbeafe", text: "#2563eb", border: "#93c5fd" };
}

export function createMarkerIcon(severityLevel, options = {}) {
  const { isTemp = false } = options;
  const color = getSeverityColor(severityLevel);

  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="44" viewBox="0 0 32 44">
      <filter id="s"><feDropShadow dx="0" dy="2" stdDeviation="2" flood-opacity="0.25"/></filter>
      <path filter="url(#s)"
        d="M16 0C7.163 0 0 7.163 0 16c0 11.667 16 28 16 28S32 27.667 32 16C32 7.163 24.837 0 16 0z"
        fill="${color}" opacity="${isTemp ? 0.5 : 1}"/>
      <circle cx="16" cy="16" r="7" fill="white" opacity="0.9"/>
      ${isTemp
        ? `<line x1="16" y1="10" x2="16" y2="22" stroke="${color}" stroke-width="2.5"/>
           <line x1="10" y1="16" x2="22" y2="16" stroke="${color}" stroke-width="2.5"/>`
        : ""}
    </svg>`;

  return L.divIcon({
    html: svg,
    className: "",
    iconSize: [32, 44],
    iconAnchor: [16, 44],
    popupAnchor: [0, -46],
  });
}