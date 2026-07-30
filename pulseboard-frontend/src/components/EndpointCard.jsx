import { useEffect, useState } from "react";
import { ChevronDown, ExternalLink } from "lucide-react";
import { api } from "../api";
import StatusBadge from "./StatusBadge";
import ResponseTimeChart from "./ResponseTimeChart";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

const WINDOWS = [
  { label: "24h", hours: 24 },
  { label: "7d", hours: 168 },
];

export default function EndpointCard({ endpoint }) {
  const [expanded, setExpanded] = useState(false);
  const [hours, setHours] = useState(24);
  const [checks, setChecks] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!expanded) return;

    let cancelled = false;
    setLoading(true);

    Promise.all([api.getChecks(endpoint.id, hours), api.getStats(endpoint.id, hours)])
      .then(([checksData, statsData]) => {
        if (cancelled) return;
        setChecks(checksData);
        setStats(statsData);
      })
      .catch(() => {
        if (!cancelled) {
          setChecks([]);
          setStats(null);
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [expanded, hours, endpoint.id]);

  return (
    <Card className="overflow-hidden py-0">
      <button
        type="button"
        className="flex w-full flex-wrap items-center justify-between gap-3 p-5 text-left hover:bg-accent/40"
        onClick={() => setExpanded((v) => !v)}
      >
        <div className="min-w-0">
          <h3 className="font-semibold">{endpoint.name}</h3>
          <a href={endpoint.url} target="_blank" rel="noreferrer" onClick={(e) => e.stopPropagation()} className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
            {endpoint.url}
            <ExternalLink className="h-3 w-3 shrink-0" />
          </a>
        </div>
        <div className="flex items-center gap-3">
          <StatusBadge status={endpoint.currentStatus || "UNKNOWN"} />
          {endpoint.lastResponseTimeMs != null && (
            <span className="text-sm text-muted-foreground">{endpoint.lastResponseTimeMs} ms</span>
          )}
          <ChevronDown className={cn("h-4 w-4 text-muted-foreground transition-transform", expanded && "rotate-180")} />
        </div>
      </button>

      {expanded && (
        <div className="border-t border-border p-5">
          <div className="mb-4 flex gap-1.5">
            {WINDOWS.map((w) => (
              <Button
                key={w.hours}
                size="sm"
                variant={hours === w.hours ? "default" : "outline"}
                onClick={() => setHours(w.hours)}
              >
                {w.label}
              </Button>
            ))}
          </div>

          {loading ? (
            <div className="space-y-3">
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-48 w-full" />
            </div>
          ) : (
            <>
              {stats && stats.sampleCount > 0 && (
                <div className="mb-4 flex flex-wrap gap-6">
                  <Stat value={`${stats.uptimePercentage}%`} label="Uptime" />
                  <Stat value={`${stats.p50ResponseTimeMs} ms`} label="p50" />
                  <Stat value={`${stats.p95ResponseTimeMs} ms`} label="p95" />
                  <Stat value={stats.sampleCount} label="Checks" />
                </div>
              )}
              <ResponseTimeChart checks={checks} />
            </>
          )}
        </div>
      )}
    </Card>
  );
}

function Stat({ value, label }) {
  return (
    <div className="flex flex-col">
      <span className="text-xl font-bold">{value}</span>
      <span className="text-xs text-muted-foreground">{label}</span>
    </div>
  );
}