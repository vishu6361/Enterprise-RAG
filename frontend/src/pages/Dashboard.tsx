import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { documentService } from "@/services/documentService";
import { DocumentItem } from "@/types";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { formatDate } from "@/lib/utils";
import {
  Files,
  FileText,
  Clock,
  CheckCircle2,
  UploadCloud,
  Users,
  Shield,
  ArrowRight,
  Sparkles,
} from "lucide-react";

export const Dashboard: React.FC = () => {
  const { user, organization } = useAuth();
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        const res = await documentService.getDocuments();
        if (res.flag && res.data) {
          setDocuments(res.data);
        }
      } catch (e) {
        console.error("Failed to fetch documents for dashboard", e);
      } finally {
        setIsLoading(false);
      }
    };
    loadDashboardData();
  }, []);

  const processingCount = documents.filter((d) => d.status === "PROCESSING").length;
  const completedCount = documents.filter((d) => d.status === "COMPLETED").length;
  const recentDocs = documents.slice(0, 5);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return <Badge variant="success">Completed</Badge>;
      case "PROCESSING":
        return <Badge variant="warning">Processing</Badge>;
      case "FAILED":
        return <Badge variant="destructive">Failed</Badge>;
      default:
        return <Badge variant="secondary">{status}</Badge>;
    }
  };

  return (
    <div className="space-y-8">
      {/* Welcome Banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-primary to-indigo-600 p-6 sm:p-8 text-white shadow-xl">
        <div className="relative z-10 flex flex-col md:flex-row md:items-center md:justify-between gap-6">
          <div className="space-y-2 max-w-2xl">
            <div className="inline-flex items-center gap-2 rounded-full bg-white/15 px-3 py-1 text-xs font-semibold backdrop-blur-md">
              <Sparkles className="h-3.5 w-3.5" />
              <span>Enterprise AI Workspace</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
              Welcome back, {user?.name}!
            </h1>
            <p className="text-sm text-primary-foreground/90 leading-relaxed">
              Your organization's documents are indexed and ready for contextual retrieval and semantic search.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <Link to="/documents">
              <Button variant="secondary" className="font-semibold shadow-md gap-2">
                <UploadCloud className="h-4 w-4" />
                Upload Documents
              </Button>
            </Link>
          </div>
        </div>
      </div>

      {/* Metrics Overview Grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Total Documents
            </CardTitle>
            <Files className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{isLoading ? "..." : documents.length}</div>
            <p className="text-xs text-muted-foreground mt-1">Scoped to {organization?.name}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Processing Pipeline
            </CardTitle>
            <Clock className="h-4 w-4 text-amber-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{isLoading ? "..." : processingCount}</div>
            <p className="text-xs text-muted-foreground mt-1">Asynchronous ingestion</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Indexed & Ready
            </CardTitle>
            <CheckCircle2 className="h-4 w-4 text-emerald-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{isLoading ? "..." : completedCount}</div>
            <p className="text-xs text-muted-foreground mt-1">Available for RAG retrieval</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Active Tenant
            </CardTitle>
            <Shield className="h-4 w-4 text-indigo-500" />
          </CardHeader>
          <CardContent>
            <div className="truncate text-lg font-bold">{organization?.name || "Enterprise"}</div>
            <p className="text-xs text-muted-foreground mt-1">Role: {user?.designation}</p>
          </CardContent>
        </Card>
      </div>

      {/* Main Sections: Recent Documents & Quick Operations */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Recent Documents Table (2 Cols) */}
        <Card className="lg:col-span-2">
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle>Recent Documents</CardTitle>
              <CardDescription>Latest knowledge assets uploaded in your organization</CardDescription>
            </div>
            <Link to="/documents">
              <Button variant="ghost" size="sm" className="gap-1 text-xs font-semibold text-primary">
                View All <ArrowRight className="h-3.5 w-3.5" />
              </Button>
            </Link>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="py-8 text-center text-sm text-muted-foreground">Loading documents...</div>
            ) : recentDocs.length === 0 ? (
              <div className="flex flex-col items-center justify-center rounded-xl border border-dashed p-8 text-center">
                <FileText className="h-10 w-10 text-muted-foreground/50 mb-3" />
                <h3 className="text-sm font-semibold">No documents uploaded yet</h3>
                <p className="text-xs text-muted-foreground mt-1 max-w-sm">
                  Upload PDF, Word, TXT, or JSON files to start semantic search and contextual AI generation.
                </p>
                <Link to="/documents" className="mt-4">
                  <Button size="sm" className="gap-2">
                    <UploadCloud className="h-4 w-4" />
                    Upload File
                  </Button>
                </Link>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="border-b text-xs font-semibold text-muted-foreground bg-muted/30">
                    <tr>
                      <th className="py-2.5 px-3">Document Name</th>
                      <th className="py-2.5 px-3">Status</th>
                      <th className="py-2.5 px-3">Owner</th>
                      <th className="py-2.5 px-3">Uploaded</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y">
                    {recentDocs.map((doc) => (
                      <tr key={doc.id} className="hover:bg-muted/40 transition-colors">
                        <td className="py-3 px-3 font-medium flex items-center gap-2 truncate max-w-[200px]">
                          <FileText className="h-4 w-4 text-primary shrink-0" />
                          <span className="truncate">{doc.documentName}</span>
                        </td>
                        <td className="py-3 px-3">{getStatusBadge(doc.status)}</td>
                        <td className="py-3 px-3 text-xs text-muted-foreground">
                          {doc.ownerName || `User #${doc.ownerId}`}
                        </td>
                        <td className="py-3 px-3 text-xs text-muted-foreground">
                          {formatDate(doc.createdAt)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Quick Operations & Architecture Info (1 Col) */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Quick Shortcuts</CardTitle>
              <CardDescription>Key governance actions</CardDescription>
            </CardHeader>
            <CardContent className="space-y-2.5">
              <Link to="/documents" className="block">
                <div className="flex items-center justify-between rounded-lg border p-3 hover:bg-muted/50 transition-colors">
                  <div className="flex items-center gap-3">
                    <UploadCloud className="h-5 w-5 text-primary" />
                    <div>
                      <p className="text-sm font-semibold">Upload Knowledge</p>
                      <p className="text-xs text-muted-foreground">Ingest new PDFs or files</p>
                    </div>
                  </div>
                  <ArrowRight className="h-4 w-4 text-muted-foreground" />
                </div>
              </Link>

              <Link to="/team" className="block">
                <div className="flex items-center justify-between rounded-lg border p-3 hover:bg-muted/50 transition-colors">
                  <div className="flex items-center gap-3">
                    <Users className="h-5 w-5 text-indigo-500" />
                    <div>
                      <p className="text-sm font-semibold">Team & RBAC</p>
                      <p className="text-xs text-muted-foreground">View organization members</p>
                    </div>
                  </div>
                  <ArrowRight className="h-4 w-4 text-muted-foreground" />
                </div>
              </Link>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 text-white border-slate-800">
            <CardHeader>
              <div className="flex items-center gap-2">
                <Shield className="h-5 w-5 text-emerald-400" />
                <CardTitle className="text-base text-white">System Security Health</CardTitle>
              </div>
            </CardHeader>
            <CardContent className="space-y-2.5 text-xs text-slate-300">
              <div className="flex justify-between border-b border-slate-800 pb-2">
                <span>Tenant Isolation:</span>
                <span className="font-semibold text-emerald-400">Strict (JWT Scoped)</span>
              </div>
              <div className="flex justify-between border-b border-slate-800 pb-2">
                <span>SHA-256 Deduplication:</span>
                <span className="font-semibold text-emerald-400">Active</span>
              </div>
              <div className="flex justify-between">
                <span>Audit Logs:</span>
                <span className="font-semibold text-sky-400">Enabled</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};
