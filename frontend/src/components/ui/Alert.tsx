import * as React from "react";
import { AlertCircle, CheckCircle2, Info, AlertTriangle } from "lucide-react";
import { cn } from "@/lib/utils";

interface AlertProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: "info" | "success" | "warning" | "destructive";
  title?: string;
}

export const Alert: React.FC<AlertProps> = ({
  variant = "info",
  title,
  children,
  className,
  ...props
}) => {
  const icons = {
    info: <Info className="h-5 w-5 text-sky-600 dark:text-sky-400" />,
    success: <CheckCircle2 className="h-5 w-5 text-emerald-600 dark:text-emerald-400" />,
    warning: <AlertTriangle className="h-5 w-5 text-amber-600 dark:text-amber-400" />,
    destructive: <AlertCircle className="h-5 w-5 text-destructive" />,
  };

  const variants = {
    info: "bg-sky-50 border-sky-200 text-sky-900 dark:bg-sky-950/40 dark:border-sky-800 dark:text-sky-200",
    success: "bg-emerald-50 border-emerald-200 text-emerald-900 dark:bg-emerald-950/40 dark:border-emerald-800 dark:text-emerald-200",
    warning: "bg-amber-50 border-amber-200 text-amber-900 dark:bg-amber-950/40 dark:border-amber-800 dark:text-amber-200",
    destructive: "bg-destructive/10 border-destructive/20 text-destructive dark:bg-destructive/20",
  };

  return (
    <div
      className={cn(
        "flex gap-3 rounded-lg border p-4 text-sm items-start",
        variants[variant],
        className
      )}
      role="alert"
      {...props}
    >
      <div className="shrink-0 mt-0.5">{icons[variant]}</div>
      <div className="flex-1">
        {title && <h5 className="font-semibold leading-none tracking-tight mb-1">{title}</h5>}
        <div className="text-sm opacity-90">{children}</div>
      </div>
    </div>
  );
};
