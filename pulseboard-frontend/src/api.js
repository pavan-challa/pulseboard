// Thin wrapper around the PulseBoard REST API. Base URL is injected at build/run
// time via VITE_API_BASE_URL (see .env.example) so the same code works against
// localhost:8080 in dev and the deployed Railway URL in production.
const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  if (!res.ok) {
    let message = `Request failed: ${res.status}`;
    try {
      const body = await res.json();
      if (body?.message) message = body.message;
    } catch {
      // response wasn't JSON - keep the generic message
    }
    throw new Error(message);
  }

  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  listEndpoints: () => request("/api/endpoints"),

  registerEndpoint: (name, url) =>
    request("/api/endpoints", {
      method: "POST",
      body: JSON.stringify({ name, url }),
    }),

  getChecks: (id, hours = 24) => request(`/api/endpoints/${id}/checks?hours=${hours}`),

  getStats: (id, hours = 24) => request(`/api/endpoints/${id}/stats?hours=${hours}`),

  getPublicStatus: () => request("/api/public/status"),
};
