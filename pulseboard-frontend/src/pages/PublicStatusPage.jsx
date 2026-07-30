import { useEffect, useState } from "react";
import { CheckCircle2, AlertTriangle, XCircle, HelpCircle } from "lucide-react";
import { api } from "../api";
import StatusBadge from "../components/StatusBadge";
import { Card } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

const POLL_INTERVAL_MS = 30_000;

const OVERALL = {
  RED: { message: "Some systems are experiencing issues", className: "bg-destructive/10 text-destructive", icon: XCircle },
  YELLOW: { message: "Some systems are degraded", className: "bg-warning/10 text-warning", icon: AlertTriangle },
  GREEN: { message: "All systems operational", className: "bg-success/10 text-success", icon: CheckCircle2 },
  UNKNOWN: { message: "Status unavailable", className: "bg-muted text-muted-foreground", icon: HelpCircle },
};

function overallStatus(services) {
  if (services.some((s) => s.status === "RED")) return "RED";
  if (services.some((s) => s.status === "YELLOW")) return "YELLOW";
  if (services.length === 0) return "UNKNOWN";
  return "GREEN";
}

export default function PublicStatusPage() {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    function load() {
      api
        .getPublicStatus()
        .then((data) => {
          setServices(data);
          setError(null);
        })
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false));
    }

    load();
    const interval = setInterval(load, POLL_INTERVAL_MS);
    return () => clearInterval(interval);
  }, []);

  const overall = overallStatus(services);
  const { message, className, icon: Icon } = OVERALL[overall];

  return (
    <div className="mx-auto max-w-xl px-5 py-14">
      <header className="mb-8 text-center">
        <h1 className="mb-4 text-2xl font-bold tracking-tight">System Status</h1>
        <span className={cn("inline-flex items-center gap-2 rounded-full px-4 py-1.5 text-sm font-medium", className)}>
          <Icon className="h-4 w-4" />
          {message}
        </span>
      </header>

      {error && (
        <p className="mb-4 rounded-md bg-destructive/10 px-4 py-2 text-center text-sm text-destructive">{error}</p>
      )}

      {loading ? (
        <div className="space-y-2">
          <Skeleton className="h-16 w-full" />
          <Skeleton className="h-16 w-full" />
        </div>
      ) : services.length === 0 ? (
        <Card>
          <p className="py-10 text-center text-sm text-muted-foreground">No services published yet.</p>
        </Card>
      ) : (
        <Card className="divide-y divide-border overflow-hidden py-0">
          {services.map((s) => (
            <div key={s.id} className="flex items-center justify-between px-5 py-4">
              <span className="font-medium">{s.name}</span>
              <StatusBadge status={s.status} />
            </div>
          ))}
        </Card>
      )}

      <footer className="mt-8 text-center text-xs text-muted-foreground">Powered by PulseBoard</footer>
    </div>
  );
}
