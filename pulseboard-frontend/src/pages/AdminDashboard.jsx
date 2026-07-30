import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Activity, ArrowUpRight } from "lucide-react";
import { api } from "../api";
import EndpointForm from "../components/EndpointForm";
import EndpointCard from "../components/EndpointCard";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

const POLL_INTERVAL_MS = 30_000;

export default function AdminDashboard() {
  const [endpoints, setEndpoints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const refresh = useCallback(() => {
    return api
      .listEndpoints()
      .then((data) => {
        setEndpoints(data);
        setError(null);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    refresh();
    const interval = setInterval(refresh, POLL_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [refresh]);

  async function handleRegister(name, url) {
    await api.registerEndpoint(name, url);
    await refresh();
  }

  const summary = useMemo(() => {
    const up = endpoints.filter((e) => e.currentStatus === "UP").length;
    const down = endpoints.filter((e) => e.currentStatus === "DOWN").length;
    return { total: endpoints.length, up, down };
  }, [endpoints]);

  return (
    <div className="mx-auto max-w-4xl px-5 py-10">
      <header className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/15 text-primary">
            <Activity className="h-5 w-5" />
          </div>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">PulseBoard</h1>
            <p className="text-sm text-muted-foreground">Admin dashboard — private view</p>
          </div>
        </div>
        <Link
          to="/status"
          className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
        >
          View public status page
          <ArrowUpRight className="h-3.5 w-3.5" />
        </Link>
      </header>

      {!loading && endpoints.length > 0 && (
        <div className="mb-6 grid grid-cols-3 gap-3">
          <SummaryCard label="Monitored" value={summary.total} />
          <SummaryCard label="Up" value={summary.up} tone="success" />
          <SummaryCard label="Down" value={summary.down} tone="destructive" />
        </div>
      )}

      <EndpointForm onRegister={handleRegister} />

      {error && (
        <p className="mb-4 rounded-md bg-destructive/10 px-4 py-2 text-sm text-destructive">{error}</p>
      )}

      {loading ? (
        <div className="space-y-3">
          <Skeleton className="h-20 w-full" />
          <Skeleton className="h-20 w-full" />
        </div>
      ) : endpoints.length === 0 ? (
        <Card>
          <CardContent className="py-10 text-center text-sm text-muted-foreground">
            No endpoints registered yet. Add one above — the scheduler checks it every 60 seconds.
          </CardContent>
        </Card>
      ) : (
        <div className="flex flex-col gap-3">
          {endpoints.map((e) => (
            <EndpointCard key={e.id} endpoint={e} />
          ))}
        </div>
      )}
    </div>
  );
}

function SummaryCard({ label, value, tone }) {
  const toneClass =
    tone === "success" ? "text-success" : tone === "destructive" ? "text-destructive" : "text-foreground";
  return (
    <Card>
      <CardContent className="py-4">
        <div className={`text-2xl font-bold ${toneClass}`}>{value}</div>
        <div className="text-xs text-muted-foreground">{label}</div>
      </CardContent>
    </Card>
  );
}
