import { Button } from "../ui/Button";
import { RefreshCw } from "lucide-react";

const DocumentHeader = ({fetchDocuments}: {fetchDocuments: () => void}) => {
    return (
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
    );
};

export default DocumentHeader;