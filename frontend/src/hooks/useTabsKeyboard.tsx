import { useCallback } from "react";

interface UseTabsKeyboardOptions {
  tabsCount: number;
  currentIndex: number;
  onIndexChange: (index: number) => void;
  orientation?: "horizontal" | "vertical";
  isDisabled: (index: number) => boolean;
}

export const useTabsKeyboard =({
  tabsCount,
  currentIndex,
  onIndexChange,
  orientation = "horizontal",
  isDisabled,
}: UseTabsKeyboardOptions) => {

  const findNextEnabled = useCallback(
    (start: number, direction: 1 | -1) => {
      let i = start;
      for (let step = 0; step < tabsCount; step++) {
        i = (i + direction + tabsCount) % tabsCount;
        if (!isDisabled(i)) return i;
      }
      return start;
    },
    [tabsCount, isDisabled]
  );

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      const prevKey = orientation === "horizontal" ? "ArrowLeft" : "ArrowUp";
      const nextKey = orientation === "horizontal" ? "ArrowRight" : "ArrowDown";

      let next: number | null = null;

      if (e.key === nextKey) next = findNextEnabled(currentIndex, 1);
      else if (e.key === prevKey) next = findNextEnabled(currentIndex, -1);
      else if (e.key === "Home") next = findNextEnabled(-1, 1);
      else if (e.key === "End") next = findNextEnabled(0, -1);

      if (next !== null) {
        e.preventDefault();
        onIndexChange(next);
      }
    },
    [currentIndex, findNextEnabled, onIndexChange, orientation]
  );

  return { handleKeyDown };
}