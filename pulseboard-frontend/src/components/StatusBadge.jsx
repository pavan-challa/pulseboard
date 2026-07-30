import { CheckCircle2, XCircle, AlertTriangle, HelpCircle } from "lucide-react";
import { Badge } from "@/components/ui/badge";

const CONFIG = {
  GREEN: { variant: "success", label: "Operational", icon: CheckCircle2 },
  UP: { variant: "success", label: "Up", icon: CheckCircle2 },
  YELLOW: { variant: "warning", label: "Degraded", icon: AlertTriangle },
  RED: { variant: "destructive", label: "Down", icon: XCircle },
  DOWN: { variant: "destructive", label: "Down", icon: XCircle },
  UNKNOWN: { variant: "outline", label: "No data yet", icon: HelpCircle },
};

export default function StatusBadge({ status }) {
  const { variant, label, icon: Icon } = CONFIG[status] || CONFIG.UNKNOWN;
  return (
    <Badge variant={variant}>
      <Icon className="h-3.5 w-3.5" />
      {label}
    </Badge>
  );
}
