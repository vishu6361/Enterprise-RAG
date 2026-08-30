import { UploadCloud } from "lucide-react";
import { Alert } from "../ui/Alert";
import { Button } from "../ui/Button";
import { Card, CardContent } from "../ui/Card";

const UploadZone = ({handleDrag, handleDrop, dragActive, uploadFeedback, fileInputRef, handleFileUpload, isUploading}: {
    handleDrag: (e: React.DragEvent) => void,
    handleDrop: (e: React.DragEvent) => void,
    dragActive: boolean,
    uploadFeedback: { type: "success" | "destructive"; message: string } | null,
    fileInputRef: React.RefObject<HTMLInputElement>,
    handleFileUpload: (file: File) => void,
    isUploading: boolean
}) => {
    return (
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
    )
}

export default UploadZone;