import React, { useState, useEffect } from "react";
import { userService, CreateUserPayload } from "@/services/userService";
import { useAuth } from "@/context/AuthContext";
import { User, UserDesignation } from "@/types";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Modal } from "@/components/ui/Modal";
import { Alert } from "@/components/ui/Alert";
import { Users, Mail, RefreshCw, UserPlus, Trash2, Search, Lock, Shield } from "lucide-react";

export const Team: React.FC = () => {
  const { user: currentUser, organization } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  // Add Member Modal State
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [designation, setDesignation] = useState<UserDesignation>("EMPLOYEE");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [modalFeedback, setModalFeedback] = useState<{ type: "success" | "destructive"; message: string } | null>(null);

  const isAdminOrManager = currentUser?.designation === "ADMIN" || currentUser?.designation === "MANAGER";

  const fetchUsers = async () => {
    setIsLoading(true);
    try {
      const res = await userService.getUsers();
      if (res.flag && res.data) {
        setUsers(res.data);
      }
    } catch (e) {
      console.error("Failed to load organization users", e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !email.trim() || !password.trim()) {
      setModalFeedback({ type: "destructive", message: "Please fill in all required fields." });
      return;
    }

    if (password.length < 6) {
      setModalFeedback({ type: "destructive", message: "Password must be at least 6 characters long." });
      return;
    }

    setIsSubmitting(true);
    setModalFeedback(null);

    const payload: CreateUserPayload = {
      name: name.trim(),
      email: email.trim(),
      password,
      designation,
      organizationId: currentUser?.organizationId,
    };

    try {
      const res = await userService.createUser(payload);
      if (res.flag && res.data) {
        setModalFeedback({ type: "success", message: `Team member "${res.data.name}" added successfully!` });
        setName("");
        setEmail("");
        setPassword("");
        setDesignation("EMPLOYEE");
        fetchUsers();
        setTimeout(() => {
          setIsAddModalOpen(false);
          setModalFeedback(null);
        }, 1200);
      } else {
        setModalFeedback({ type: "destructive", message: res.message || "Failed to add team member" });
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || "Error adding team member";
      setModalFeedback({ type: "destructive", message: msg });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteUser = async (userId: number, userName: string) => {
    if (userId === currentUser?.id) {
      alert("You cannot deactivate your own account.");
      return;
    }
    if (!window.confirm(`Are you sure you want to deactivate "${userName}"? Any owned documents will be reassigned.`)) {
      return;
    }
    try {
      const res = await userService.deleteUser(userId);
      if (res.flag) {
        setUsers((prev) => prev.filter((u) => u.id !== userId));
      } else {
        alert(res.message || "Failed to deactivate user");
      }
    } catch (err: any) {
      alert(err.response?.data?.message || "Failed to deactivate user");
    }
  };

  const filteredUsers = users.filter(
    (u) =>
      u.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.designation.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Organization & Team</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Manage members and role permissions under {organization?.name || "your organization"}
          </p>
        </div>
        <div className="flex items-center gap-3 self-start sm:self-auto">
          <Button onClick={fetchUsers} variant="outline" size="sm" className="gap-2">
            <RefreshCw className="h-4 w-4" />
            Refresh
          </Button>
          {isAdminOrManager && (
            <Button onClick={() => setIsAddModalOpen(true)} size="sm" className="gap-2 font-semibold shadow-sm">
              <UserPlus className="h-4 w-4" />
              Add Member
            </Button>
          )}
        </div>
      </div>

      <Card>
        <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <Users className="h-5 w-5 text-primary" />
              <CardTitle>Team Members ({users.length})</CardTitle>
            </div>
            <CardDescription>
              Active accounts authorized to access documents and ask RAG questions
            </CardDescription>
          </div>
          <div className="relative w-full sm:w-72">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
            <input
              type="text"
              placeholder="Search by name, email, or role..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full rounded-lg border border-input bg-background pl-9 pr-4 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </div>
        </CardHeader>

        <CardContent>
          {isLoading ? (
            <div className="py-12 text-center text-sm text-muted-foreground">Loading members...</div>
          ) : filteredUsers.length === 0 ? (
            <div className="py-12 text-center text-sm text-muted-foreground">
              {searchQuery ? "No members match your search criteria." : "No members found in this organization."}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="border-b text-xs font-semibold uppercase tracking-wider text-muted-foreground bg-muted/30">
                  <tr>
                    <th className="py-3 px-4">Member</th>
                    <th className="py-3 px-4">Email</th>
                    <th className="py-3 px-4">Role / Designation</th>
                    <th className="py-3 px-4">User ID</th>
                    {currentUser?.designation === "ADMIN" && (
                      <th className="py-3 px-4 text-right">Actions</th>
                    )}
                  </tr>
                </thead>
                <tbody className="divide-y">
                  {filteredUsers.map((u) => (
                    <tr key={u.id} className="hover:bg-muted/40 transition-colors">
                      <td className="py-3.5 px-4 font-medium flex items-center gap-3">
                        <div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 text-primary font-bold text-sm border border-primary/20">
                          {u.name ? u.name.charAt(0).toUpperCase() : "U"}
                        </div>
                        <div>
                          <p className="font-semibold text-foreground flex items-center gap-2">
                            {u.name}
                            {u.id === currentUser?.id && (
                              <span className="text-[10px] bg-primary/10 text-primary px-1.5 py-0.5 rounded font-bold">
                                You
                              </span>
                            )}
                          </p>
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-muted-foreground">
                        <div className="flex items-center gap-1.5">
                          <Mail className="h-3.5 w-3.5" />
                          <span>{u.email}</span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4">
                        <Badge
                          variant={u.designation === "ADMIN" ? "default" : "secondary"}
                          className="font-semibold"
                        >
                          {u.designation}
                        </Badge>
                      </td>
                      <td className="py-3.5 px-4 font-mono text-xs text-muted-foreground">
                        #{u.id}
                      </td>
                      {currentUser?.designation === "ADMIN" && (
                        <td className="py-3.5 px-4 text-right">
                          {u.id !== currentUser?.id && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleDeleteUser(u.id, u.name)}
                              title="Deactivate Member"
                              className="h-8 w-8 p-0 text-destructive/80 hover:text-destructive hover:bg-destructive/10"
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          )}
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Add Team Member Modal */}
      <Modal
        isOpen={isAddModalOpen}
        onClose={() => {
          setIsAddModalOpen(false);
          setModalFeedback(null);
        }}
        title="Add Team Member"
        description={`Provision a new user account under ${organization?.name || "your organization"}`}
      >
        <form onSubmit={handleAddMember} className="space-y-4">
          {modalFeedback && (
            <Alert variant={modalFeedback.type}>{modalFeedback.message}</Alert>
          )}

          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Full Name *
            </label>
            <Input
              type="text"
              placeholder="e.g. Sarah Connor"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>

          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Work Email *
            </label>
            <Input
              type="email"
              placeholder="sarah@company.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Initial Password * (min 6 chars)
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                type="password"
                placeholder="••••••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="pl-9"
                required
                minLength={6}
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Role & Permissions
            </label>
            <select
              value={designation}
              onChange={(e) => setDesignation(e.target.value as UserDesignation)}
              className="w-full rounded-lg border border-input bg-background p-2.5 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <option value="EMPLOYEE">EMPLOYEE (Document Viewer & Chat)</option>
              <option value="MANAGER">MANAGER (Document Editor & Uploader)</option>
              <option value="ADMIN">ADMIN (Full Workspace Management)</option>
            </select>
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t">
            <Button
              type="button"
              variant="outline"
              onClick={() => {
                setIsAddModalOpen(false);
                setModalFeedback(null);
              }}
            >
              Cancel
            </Button>
            <Button type="submit" isLoading={isSubmitting} className="gap-2">
              <Shield className="h-4 w-4" />
              Add Member
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
