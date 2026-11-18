import type { ReactNode } from "react";

export interface AdminTabItem {
  id: string;
  label: string;
  icon?: ReactNode;
  badge?: string | number;
  badgeVariant?: "neutral" | "success" | "warning" | "error" | "brand";
  disabled?: boolean;
  content: ReactNode;
}

export type TabsVariant = "underline" | "pill" | "enclosed";
export type TabsSize = "sm" | "md" | "lg";

export interface AdminTabsProps {
  items: AdminTabItem[];
  activeId?: string;
  defaultActiveId?: string;
  onChange?: (id: string) => void;
  variant?: TabsVariant;
  size?: TabsSize;
  orientation?: "horizontal" | "vertical";
  showPanels?: boolean;
  className?: string;
  panelClassName?: string;
  unmountInactive?: boolean;
  iconOnlyOnMobile?: boolean;
  hideLabelsOnMobile?: boolean;
  mobileVariant?: TabsVariant;
  verticalFrom?: "md" | "lg" | "never";
}