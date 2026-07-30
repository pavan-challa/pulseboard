import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

function formatTick(iso) {
  const d = new Date(iso);
  return d.toLocaleString(undefined, {
    month: "numeric",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
}

export default function ResponseTimeChart({ checks }) {
  if (!checks || checks.length === 0) {
    return <p className="py-8 text-center text-sm text-muted-foreground">No checks recorded in this window yet.</p>;
  }

  const data = checks.map((c) => ({
    checkedAt: c.checkedAt,
    responseTimeMs: c.responseTimeMs,
    status: c.status,
  }));

  return (
    <ResponsiveContainer width="100%" height={220}>
      <LineChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#232733" />
        <XAxis
          dataKey="checkedAt"
          tickFormatter={formatTick}
          minTickGap={40}
          stroke="#8b93a7"
          fontSize={12}
        />
        <YAxis
          stroke="#8b93a7"
          fontSize={12}
          label={{ value: "ms", angle: -90, position: "insideLeft", fill: "#8b93a7" }}
        />
        <Tooltip
          labelFormatter={formatTick}
          formatter={(value, key, entry) => [`${value} ms`, entry.payload.status]}
          contentStyle={{ background: "#12151c", border: "1px solid #232733", borderRadius: 8 }}
        />
        <Line
          type="monotone"
          dataKey="responseTimeMs"
          stroke="#3b82f6"
          dot={false}
          strokeWidth={2}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
