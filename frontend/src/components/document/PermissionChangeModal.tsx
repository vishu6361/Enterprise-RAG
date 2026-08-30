import { DocumentItem, DocumentPermissionType } from "@/types";
import { Shield } from "lucide-react";
import { Alert } from "../ui/Alert";
import { Button } from "../ui/Button";
import { Modal } from "../ui/Modal";

const PermissionChangeModal = ({
    isPermissionModalOpen,
    setIsPermissionModalOpen,
    selectedDoc,
    permissionFeedback,
    targetUserId,
    setTargetUserId,
    permissionType,
    setPermissionType,
    teamMembers,
    handleSavePermission,
    isUpdatingPermission
}: {
    isPermissionModalOpen: boolean;
    setIsPermissionModalOpen: (isOpen: boolean) => void;
    selectedDoc: DocumentItem | null;
    permissionFeedback: string | null;
    targetUserId: string | number;
    setTargetUserId: (targetUserId: string | number) => void;
    permissionType: DocumentPermissionType;
    setPermissionType: (permissionType: DocumentPermissionType) => void;
    teamMembers: any[];
    handleSavePermission: () => void;
    isUpdatingPermission: boolean;
}) => {
    return (
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
    )
}

export default PermissionChangeModal;