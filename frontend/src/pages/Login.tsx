import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/Card";
import { Alert } from "@/components/ui/Alert";
import { Bot, ShieldCheck, Database, Zap, Lock, Mail } from "lucide-react";

export const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password.trim()) {
      setError("Please provide both email and password.");
      return;
    }

    setError(null);
    setIsLoading(true);
    const result = await login({ email: email.trim(), password });
    setIsLoading(false);

    if (result.success) {
      navigate("/");
    } else {
      setError(result.message);
    }
  };

  return (
    <div className="flex min-h-screen">
      {/* Left Feature Showcase Banner (Desktop) */}
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between bg-slate-900 p-12 text-white relative overflow-hidden">
        {/* Ambient Glows */}
        <div className="absolute -top-24 -left-24 h-96 w-96 rounded-full bg-primary/20 blur-3xl" />
        <div className="absolute -bottom-24 -right-24 h-96 w-96 rounded-full bg-indigo-500/20 blur-3xl" />

        <div className="relative z-10">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary text-white shadow-lg shadow-primary/30">
              <Bot className="h-7 w-7" />
            </div>
            <div>
              <span className="text-xl font-bold tracking-tight">Enterprise RAG</span>
              <span className="block text-xs font-medium text-slate-400">Knowledge Retrieval Platform</span>
            </div>
          </div>
        </div>

        <div className="relative z-10 space-y-6 max-w-lg">
          <h1 className="text-3xl font-extrabold tracking-tight leading-tight sm:text-4xl">
            Secure, Multi-Tenant AI Knowledge Architecture.
          </h1>
          <p className="text-sm text-slate-300 leading-relaxed">
            Scalable document ingestion, cryptographically isolated multi-tenancy, granular RBAC permissions, and asynchronous vector processing.
          </p>

          <div className="grid grid-cols-1 gap-4 pt-4">
            <div className="flex items-start gap-3 rounded-lg bg-slate-800/60 p-3.5 border border-slate-700/60 backdrop-blur-sm">
              <ShieldCheck className="h-5 w-5 text-emerald-400 shrink-0 mt-0.5" />
              <div>
                <h4 className="text-sm font-semibold">Strict Tenant Isolation</h4>
                <p className="text-xs text-slate-400">Cryptographically bound JWT claims ensuring zero data leakage.</p>
              </div>
            </div>
            <div className="flex items-start gap-3 rounded-lg bg-slate-800/60 p-3.5 border border-slate-700/60 backdrop-blur-sm">
              <Database className="h-5 w-5 text-sky-400 shrink-0 mt-0.5" />
              <div>
                <h4 className="text-sm font-semibold">Deduplication & Audit Trail</h4>
                <p className="text-xs text-slate-400">SHA-256 duplicate content detection with immutable audit records.</p>
              </div>
            </div>
            <div className="flex items-start gap-3 rounded-lg bg-slate-800/60 p-3.5 border border-slate-700/60 backdrop-blur-sm">
              <Zap className="h-5 w-5 text-amber-400 shrink-0 mt-0.5" />
              <div>
                <h4 className="text-sm font-semibold">Async Vector Pipeline</h4>
                <p className="text-xs text-slate-400">High-throughput document parsing, chunking, and semantic search.</p>
              </div>
            </div>
          </div>
        </div>

        <div className="relative z-10 text-xs text-slate-500">
          © 2026 Enterprise RAG Platform. Production Ready SDE-2 Architecture.
        </div>
      </div>

      {/* Right Login Form */}
      <div className="flex w-full lg:w-1/2 items-center justify-center p-6 sm:p-12 bg-background">
        <div className="w-full max-w-md space-y-6">
          {/* Mobile Header */}
          <div className="flex flex-col items-center text-center lg:hidden">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-md">
              <Bot className="h-6 w-6" />
            </div>
            <h2 className="mt-4 text-2xl font-bold tracking-tight">Enterprise RAG</h2>
            <p className="text-xs text-muted-foreground">Sign in to your organization account</p>
          </div>

          <Card className="shadow-lg border">
            <CardHeader className="space-y-1 pb-4">
              <CardTitle className="text-2xl font-bold tracking-tight">Sign In</CardTitle>
              <CardDescription>
                Enter your enterprise credentials to access your knowledge repository
              </CardDescription>
            </CardHeader>

            <form onSubmit={handleSubmit}>
              <CardContent className="space-y-4">
                {error && (
                  <Alert variant="destructive" title="Authentication Error">
                    {error}
                  </Alert>
                )}

                <div className="space-y-2">
                  <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    Work Email
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                    <Input
                      type="email"
                      placeholder="name@enterprise.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="pl-9"
                      required
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                      Password
                    </label>
                  </div>
                  <div className="relative">
                    <Lock className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                    <Input
                      type="password"
                      placeholder="••••••••••••"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="pl-9"
                      required
                    />
                  </div>
                </div>
              </CardContent>

              <CardFooter className="flex flex-col gap-3 pt-2">
                <Button type="submit" className="w-full font-semibold" isLoading={isLoading}>
                  Authenticate Session
                </Button>

                <div className="text-center text-xs text-muted-foreground">
                  Don't have an organization workspace?{" "}
                  <Link to="/signup" className="text-primary font-semibold hover:underline">
                    Create one here
                  </Link>
                </div>

                <div className="rounded-lg bg-muted/50 p-3 text-center text-xs text-muted-foreground border">
                  <span>🔒 Multi-tenant encrypted session using BCrypt & JWT</span>
                </div>
              </CardFooter>
            </form>
          </Card>
        </div>
      </div>
    </div>
  );
};
