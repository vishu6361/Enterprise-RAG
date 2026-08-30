import React, { useState } from "react";
import { Link, useLocation, Outlet } from "react-router-dom";
import {
  LayoutDashboard,
  Files,
  Users,
  User as UserIcon,
  LogOut,
  Building2,
  Menu,
  X,
  Bot,
  Layers,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { Badge } from "@/components/ui/Badge";
import { cn } from "@/lib/utils";

export const AppLayout: React.FC = () => {
  const { user, organization, logout } = useAuth();
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const navItems = [
    { label: "Dashboard", href: "/", icon: LayoutDashboard },
    { label: "Documents", href: "/documents", icon: Files },
    { label: "Organization & Team", href: "/team", icon: Users },
    { label: "My Profile", href: "/profile", icon: UserIcon },
  ];

  return (
    <div className="flex min-h-screen bg-muted/20">
      {/* Mobile Drawer Overlay */}
      {isMobileMenuOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm lg:hidden"
          onClick={() => setIsMobileMenuOpen(false)}
        />
      )}

      {/* Sidebar Navigation */}
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-50 flex w-72 flex-col border-r bg-card shadow-sm transition-transform duration-200 ease-in-out lg:static lg:translate-x-0",
          isMobileMenuOpen ? "translate-x-0" : "-translate-x-full"
        )}
      >
        {/* Brand & Organization Header */}
        <div className="flex flex-col border-b p-5">
          <div className="flex items-center justify-between">
            <Link to="/" className="flex items-center gap-2.5">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-md shadow-primary/20">
                <Bot className="h-6 w-6" />
              </div>
              <div>
                <span className="text-base font-bold tracking-tight">Enterprise RAG</span>
                <span className="block text-xs font-medium text-muted-foreground">AI Knowledge Hub</span>
              </div>
            </Link>
            <button
              onClick={() => setIsMobileMenuOpen(false)}
              className="rounded-lg p-1 text-muted-foreground hover:bg-accent lg:hidden"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Active Tenant / Organization Badge */}
          <div className="mt-4 flex items-center justify-between rounded-lg bg-muted/60 p-2.5 border">
            <div className="flex items-center gap-2 overflow-hidden">
              <Building2 className="h-4 w-4 shrink-0 text-primary" />
              <div className="truncate">
                <p className="truncate text-xs font-semibold leading-none">
                  {organization?.name || "Loading tenant..."}
                </p>
                <p className="mt-0.5 truncate text-[11px] text-muted-foreground">
                  ID: #{user?.organizationId}
                </p>
              </div>
            </div>
            <Badge variant="outline" className="text-[10px] uppercase font-bold shrink-0 bg-card">
              Tenant
            </Badge>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 space-y-1.5 p-4 overflow-y-auto">
          <div className="px-3 pb-2 text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
            Platform
          </div>
          {navItems.map((item) => {
            const isActive =
              item.href === "/"
                ? location.pathname === "/"
                : location.pathname.startsWith(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                to={item.href}
                onClick={() => setIsMobileMenuOpen(false)}
                className={cn(
                  "flex items-center gap-3 rounded-lg px-3.5 py-2.5 text-sm font-medium transition-colors",
                  isActive
                    ? "bg-primary text-primary-foreground shadow-sm font-semibold"
                    : "text-muted-foreground hover:bg-accent hover:text-foreground"
                )}
              >
                <Icon className="h-4 w-4 shrink-0" />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* User Card & Logout */}
        <div className="border-t p-4 bg-muted/10">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3 overflow-hidden">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary font-bold text-sm border border-primary/20">
                {user?.name ? user.name.charAt(0).toUpperCase() : "U"}
              </div>
              <div className="truncate">
                <p className="truncate text-xs font-semibold leading-none">{user?.name}</p>
                <p className="mt-1 truncate text-[11px] text-muted-foreground">{user?.email}</p>
              </div>
            </div>
            <button
              onClick={logout}
              title="Logout"
              className="rounded-lg p-2 text-muted-foreground hover:bg-destructive/10 hover:text-destructive transition-colors"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
          <div className="mt-2.5 flex items-center justify-between pt-2 border-t text-[11px]">
            <span className="text-muted-foreground font-medium">Role</span>
            <Badge
              variant={user?.designation === "ADMIN" ? "default" : "secondary"}
              className="text-[10px] font-semibold"
            >
              {user?.designation}
            </Badge>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* Mobile Header Bar */}
        <header className="flex h-16 items-center justify-between border-b bg-card px-4 lg:hidden">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setIsMobileMenuOpen(true)}
              className="rounded-lg p-2 text-muted-foreground hover:bg-accent hover:text-foreground"
            >
              <Menu className="h-5 w-5" />
            </button>
            <div className="flex items-center gap-2">
              <Layers className="h-5 w-5 text-primary" />
              <span className="font-bold text-sm tracking-tight">Enterprise RAG</span>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="outline" className="text-xs font-medium">
              {organization?.name || "Enterprise"}
            </Badge>
          </div>
        </header>

        {/* Page Content Viewport */}
        <main className="flex-1 overflow-y-auto p-4 md:p-8">
          <div className="mx-auto max-w-7xl">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};
