import { useState } from "react";
import { toast } from "sonner";
import { Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

export default function EndpointForm({ onRegister }) {
  const [name, setName] = useState("");
  const [url, setUrl] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    if (!name.trim() || !url.trim()) return;

    setSubmitting(true);
    try {
      await onRegister(name.trim(), url.trim());
      toast.success(`${name.trim()} added`, {
        description: "The scheduler will check it within 60 seconds.",
      });
      setName("");
      setUrl("");
    } catch (err) {
      toast.error("Couldn't add endpoint", { description: err.message });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Card className="mb-6">
      <CardContent className="pt-5">
        <form className="flex flex-col gap-3 sm:flex-row" onSubmit={handleSubmit}>
          <Input
            type="text"
            placeholder="Service name (e.g. Mini E-Commerce Store)"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            className="sm:flex-1"
          />
          <Input
            type="url"
            placeholder="https://your-app.vercel.app"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            required
            className="sm:flex-1"
          />
          <Button type="submit" disabled={submitting}>
            <Plus className="h-4 w-4" />
            {submitting ? "Adding…" : "Add endpoint"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
