import React, { useState, useEffect, useRef } from "react";
import { documentService } from "@/services/documentService";
import { userService } from "@/services/userService";
import { DocumentItem, User, DocumentPermissionType } from "@/types";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Modal } from "@/components/ui/Modal";
import { Alert } from "@/components/ui/Alert";
import { formatDate } from "@/lib/utils";
import {
  UploadCloud,
  FileText,
  Trash2,
  Share2,
  RefreshCw,
  Search,
  FileCode,
  FileSpreadsheet,
  File,
  Shield,
} from "lucide-react";

export const Documents: React.FC = () => {
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [teamMembers, setTeamMembers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  // Upload States
  const [isUploading, setIsUploading] = useState(false);
  const [uploadFeedback, setUploadFeedback] = useState<{ type: "success" | "destructive"; message: string } | null>(null);
  const [dragActive, setDragActive] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Permission Modal States
  const [selectedDoc, setSelectedDoc] = useState<DocumentItem | null>(null);
  const [isPermissionModalOpen, setIsPermissionModalOpen] = useState(false);
  const [targetUserId, setTargetUserId] = useState<number | "">("");
  const [permissionType, setPermissionType] = useState<DocumentPermissionType>("VIEWER");
  const [isUpdatingPermission, setIsUpdatingPermission] = useState(false);
  const [permissionFeedback, setPermissionFeedback] = useState<string | null>(null);

  const fetchDocuments = async () => {
    setIsLoading(true);
    try {
      const res = await documentService.getDocuments();
      if (res.flag && res.data) {
        setDocuments(res.data);
      }
    } catch (e) {
      console.error("Failed to load documents", e);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchTeam = async () => {
    try {
      const res = await userService.getUsers();
      if (res.flag && res.data) {
        setTeamMembers(res.data);
      }
    } catch (e) {
      console.error("Failed to load team members", e);
    }
  };

  useEffect(() => {
    fetchDocuments();
    fetchTeam();
  }, []);

  const handleFileUpload = async (file: File) => {
    setUploadFeedback(null);
    setIsUploading(true);
    try {
      const res = await documentService.uploadDocument(file);
      if (res.flag) {
        setUploadFeedback({
          type: "success",
          message: `Document "${file.name}" uploaded successfully and queued for AI ingestion!`,
        });
        fetchDocuments();
      } else {
        setUploadFeedback({
          type: "destructive",
          message: res.message || "Failed to upload document",
        });
      }
    } catch (err: any) {
      const msg =
        err.response?.status === 409
          ? "Duplicate file detected: A document with identical SHA-256 hash already exists in your organization."
          : err.response?.data?.message || err.message || "Error uploading document";
      setUploadFeedback({ type: "destructive", message: msg });
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileUpload(e.dataTransfer.files[0]);
    }
  };

  const handleDelete = async (id: number, name: string) => {
    if (!window.confirm(`Are you sure you want to delete "${name}"?`)) return;
    try {
      const res = await documentService.deleteDocument(id);
      if (res.flag) {
        setDocuments((prev) => prev.filter((d) => d.id !== id));
      } else {
        alert(res.message || "Failed to delete document");
      }
    } catch (e: any) {
      alert(e.response?.data?.message || "Failed to delete document");
    }
  };

  const handleSavePermission = async () => {
    if (!selectedDoc || !targetUserId) return;
    setIsUpdatingPermission(true);
    setPermissionFeedback(null);
    try {
      const res = await documentService.updatePermission({
        documentId: selectedDoc.id,
        userId: Number(targetUserId),
        permissionType,
      });
      if (res.flag) {
        setPermissionFeedback("Permission updated successfully!");
        setTimeout(() => {
          setIsPermissionModalOpen(false);
          setPermissionFeedback(null);
          setTargetUserId("");
        }, 1200);
      } else {
        setPermissionFeedback(res.message || "Failed to update permission");
      }
    } catch (e: any) {
      setPermissionFeedback(e.response?.data?.message || "Error updating permission");
    } finally {
      setIsUpdatingPermission(false);
    }
  };

  const getFileIcon = (filename: string) => {
    const ext = filename.split(".").pop()?.toLowerCase();
    if (ext === "pdf" || ext === "txt" || ext === "md") return <FileText className="h-5 w-5 text-red-500 shrink-0" />;
    if (ext === "csv" || ext === "xlsx") return <FileSpreadsheet className="h-5 w-5 text-emerald-500 shrink-0" />;
    if (ext === "json" || ext === "xml" || ext === "yaml") return <FileCode className="h-5 w-5 text-amber-500 shrink-0" />;
    return <File className="h-5 w-5 text-primary shrink-0" />;
  };

  const filteredDocs = documents.filter((doc) =>
    doc.documentName.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Document Hub</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Manage, upload, and govern enterprise knowledge assets with SHA-256 deduplication
          </p>
        </div>
        <Button onClick={fetchDocuments} variant="outline" size="sm" className="gap-2 self-start">
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>

      {/* Drag & Drop Upload Zone */}
      <Card className="border-2 border-dashed transition-all duration-200">
        <CardContent className="p-6">
          <div
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
            className={`flex flex-col items-center justify-center rounded-xl p-8 text-center transition-colors ${
              dragActive ? "bg-primary/10 border-primary" : "bg-muted/30"
            }`}
          >
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10 text-primary shadow-inner mb-4">
              <UploadCloud className="h-8 w-8" />
            </div>

            <h3 className="text-base font-semibold">
              {dragActive ? "Drop your file here" : "Drag and drop your document here"}
            </h3>
            <p className="text-xs text-muted-foreground mt-1 max-w-md">
              Supports PDF, DOCX, TXT, CSV, MD, and JSON files. File is retained for retries and audited.
            </p>

            <input
              ref={fileInputRef}
              type="file"
              onChange={(e) => {
                if (e.target.files && e.target.files[0]) {
                  handleFileUpload(e.target.files[0]);
                }
              }}
              className="hidden"
              accept=".pdf,.txt,.docx,.csv,.json,.md"
            />

            <div className="mt-5 flex gap-3">
              <Button
                onClick={() => fileInputRef.current?.click()}
                isLoading={isUploading}
                className="gap-2 font-medium"
              >
                <UploadCloud className="h-4 w-4" />
                Browse Files
              </Button>
            </div>
          </div>

          {uploadFeedback && (
            <div className="mt-4">
              <Alert variant={uploadFeedback.type}>{uploadFeedback.message}</Alert>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Document Library Table */}
      <Card>
        <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <CardTitle>Organization Repository</CardTitle>
            <CardDescription>All documents available within your tenant boundary</CardDescription>
          </div>
          <div className="relative w-full sm:w-72">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
            <input
              type="text"
              placeholder="Search documents..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full rounded-lg border border-input bg-background pl-9 pr-4 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </div>
        </CardHeader>

        <CardContent>
          {isLoading ? (
            <div className="py-12 text-center text-sm text-muted-foreground">Loading documents...</div>
          ) : filteredDocs.length === 0 ? (
            <div className="py-12 text-center text-sm text-muted-foreground">
              {searchQuery ? "No documents match your search query." : "No documents uploaded yet."}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="border-b text-xs font-semibold uppercase tracking-wider text-muted-foreground bg-muted/30">
                  <tr>
                    <th className="py-3 px-4">Document</th>
                    <th className="py-3 px-4">Content Hash (SHA-256)</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4">Owner</th>
                    <th className="py-3 px-4">Uploaded Date</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y">
                  {filteredDocs.map((doc) => (
                    <tr key={doc.id} className="hover:bg-muted/40 transition-colors">
                      <td className="py-3.5 px-4 font-medium flex items-center gap-3">
                        {getFileIcon(doc.documentName)}
                        <div>
                          <p className="font-semibold text-foreground truncate max-w-xs">{doc.documentName}</p>
                          <span className="text-[11px] text-muted-foreground">ID: #{doc.id}</span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4 font-mono text-xs text-muted-foreground">
                        <span className="rounded bg-muted px-2 py-1" title={doc.contentHash}>
                          {doc.contentHash ? `${doc.contentHash.substring(0, 12)}...` : "N/A"}
                        </span>
                      </td>
                      <td className="py-3.5 px-4">
                        {doc.status === "COMPLETED" && <Badge variant="success">Completed</Badge>}
                        {doc.status === "PROCESSING" && <Badge variant="warning">Processing</Badge>}
                        {doc.status === "FAILED" && <Badge variant="destructive">Failed</Badge>}
                      </td>
                      <td className="py-3.5 px-4 text-xs font-medium text-muted-foreground">
                        {doc.ownerName || `User #${doc.ownerId}`}
                      </td>
                      <td className="py-3.5 px-4 text-xs text-muted-foreground">
                        {formatDate(doc.createdAt)}
                      </td>
                      <td className="py-3.5 px-4 text-right space-x-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedDoc(doc);
                            setIsPermissionModalOpen(true);
                          }}
                          title="Manage Permissions"
                          className="h-8 w-8 p-0"
                        >
                          <Share2 className="h-4 w-4 text-muted-foreground hover:text-foreground" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(doc.id, doc.documentName)}
                          title="Delete Document"
                          className="h-8 w-8 p-0 text-destructive/80 hover:text-destructive hover:bg-destructive/10"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Permission Management Modal */}
      <Modal
        isOpen={isPermissionModalOpen}
        onClose={() => setIsPermissionModalOpen(false)}
        title="Document Permissions"
        description={`Manage access rights for "${selectedDoc?.documentName}"`}
      >
        <div className="space-y-4">
          {permissionFeedback && (
            <Alert variant={permissionFeedback.includes("success") ? "success" : "destructive"}>
              {permissionFeedback}
            </Alert>
          )}

          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Select Team Member
            </label>
            <select
              value={targetUserId}
              onChange={(e) => setTargetUserId(e.target.value ? Number(e.target.value) : "")}
              className="w-full rounded-lg border border-input bg-background p-2.5 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <option value="">-- Choose User --</option>
              {teamMembers.map((member) => (
                <option key={member.id} value={member.id}>
                  {member.name} ({member.email}) — {member.designation}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Access Permission Level
            </label>
            <select
              value={permissionType}
              onChange={(e) => setPermissionType(e.target.value as DocumentPermissionType)}
              className="w-full rounded-lg border border-input bg-background p-2.5 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <option value="VIEWER">VIEWER (Read-only / RAG query)</option>
              <option value="EDITOR">EDITOR (Modify / Re-process)</option>
              <option value="OWNER">OWNER (Full control / Manage rights)</option>
            </select>
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t">
            <Button variant="outline" onClick={() => setIsPermissionModalOpen(false)}>
              Cancel
            </Button>
            <Button
              onClick={handleSavePermission}
              isLoading={isUpdatingPermission}
              disabled={!targetUserId}
              className="gap-2"
            >
              <Shield className="h-4 w-4" />
              Save Permissions
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
