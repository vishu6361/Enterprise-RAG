import React from "react";
import { useAuth } from "@/context/AuthContext";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Building2, ShieldCheck, Mail, Key, LogOut } from "lucide-react";

export const Profile: React.FC = () => {
  const { user, organization, logout } = useAuth();

  return (
    <div className="space-y-8 max-w-4xl">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Account & Organization</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Review your enterprise identity, tenant assignment, and security privileges
        </p>
      </div>

      {/* User Card */}
      <Card className="overflow-hidden border">
        <div className="h-28 bg-gradient-to-r from-primary to-indigo-600 p-6 flex items-end">
          <div className="flex items-center gap-4 translate-y-8">
            <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-card border-4 border-background text-primary font-bold text-2xl shadow-xl">
              {user?.name ? user.name.charAt(0).toUpperCase() : "U"}
            </div>
          </div>
        </div>

        <CardContent className="pt-12 pb-6 px-6 space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h2 className="text-xl font-bold">{user?.name}</h2>
              <p className="text-sm text-muted-foreground flex items-center gap-1.5 mt-0.5">
                <Mail className="h-3.5 w-3.5" />
                {user?.email}
              </p>
            </div>
            <div className="flex items-center gap-2">
              <Badge
                variant={user?.designation === "ADMIN" ? "default" : "secondary"}
                className="text-xs px-3 py-1 font-semibold"
              >
                {user?.designation} Role
              </Badge>
              <Badge variant="outline" className="text-xs px-3 py-1">
                User #{user?.id}
              </Badge>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Organization Info Card */}
        <Card>
          <CardHeader className="pb-3">
            <div className="flex items-center gap-2 text-primary">
              <Building2 className="h-5 w-5" />
              <CardTitle className="text-base">Organization Details</CardTitle>
            </div>
            <CardDescription>Multi-tenant organization boundary</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3.5 text-sm">
            <div className="flex justify-between border-b pb-2">
              <span className="text-muted-foreground">Organization Name:</span>
              <span className="font-semibold">{organization?.name || "Loading..."}</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="text-muted-foreground">Organization ID:</span>
              <span className="font-mono font-medium">#{user?.organizationId}</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="text-muted-foreground">Contact Email:</span>
              <span>{organization?.contactEmail || "N/A"}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Contact Phone:</span>
              <span>{organization?.contactPhone || "N/A"}</span>
            </div>
          </CardContent>
        </Card>

        {/* Security & Authentication Privileges */}
        <Card>
          <CardHeader className="pb-3">
            <div className="flex items-center gap-2 text-emerald-600 dark:text-emerald-400">
              <ShieldCheck className="h-5 w-5" />
              <CardTitle className="text-base">Security & Authentication</CardTitle>
            </div>
            <CardDescription>JWT cryptographic credentials</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3.5 text-sm">
            <div className="flex justify-between border-b pb-2">
              <span className="text-muted-foreground">Password Hashing:</span>
              <span className="font-semibold text-emerald-600 dark:text-emerald-400">BCrypt (Salted)</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="text-muted-foreground">Token Protocol:</span>
              <span className="font-medium">HMAC-SHA256 JWT</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="text-muted-foreground">Session Expiration:</span>
              <span>24 Hours (Stateless)</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Access Scope:</span>
              <span className="font-semibold">Tenant-Isolated</span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Session Management Card */}
      <Card className="border-destructive/30">
        <CardHeader>
          <div className="flex items-center gap-2 text-destructive">
            <Key className="h-5 w-5" />
            <CardTitle className="text-base">Session Termination</CardTitle>
          </div>
          <CardDescription>Log out of this browser session</CardDescription>
        </CardHeader>
        <CardContent className="flex items-center justify-between">
          <p className="text-xs text-muted-foreground max-w-md">
            Logging out will clear your local JWT token and terminate the active browser session.
          </p>
          <Button variant="destructive" onClick={logout} className="gap-2 shrink-0">
            <LogOut className="h-4 w-4" />
            Logout
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};
