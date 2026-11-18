import { useCallback, useEffect, useRef, useState } from 'react'


export const useTabScroll = (activeId: string) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const tabRefs = useRef<Map<string, HTMLElement>>(new Map());

  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  const updateScrollHints = useCallback(() => {
    const el = containerRef.current;
    if (!el) return;
    const { scrollLeft, scrollWidth, clientWidth } = el;
    setCanScrollLeft(scrollLeft > 4);
    setCanScrollRight(scrollLeft + clientWidth < scrollWidth - 4);
  }, []);

  // Auto-scroll al tab activo
  useEffect(() => {
    const tab = tabRefs.current.get(activeId);
    const el = containerRef.current;
    if (!tab || !el) return;

    const tabRect = tab.getBoundingClientRect();
    const containerRect = el.getBoundingClientRect();

    // Si el tab está fuera de la vista horizontal
    if (tabRect.left < containerRect.left) {
      el.scrollBy({ left: tabRect.left - containerRect.left - 16, behavior: "smooth" });
    } else if (tabRect.right > containerRect.right) {
      el.scrollBy({ left: tabRect.right - containerRect.right + 16, behavior: "smooth" });
    }
    updateScrollHints();
  }, [activeId, updateScrollHints]);

  // Actualiza hints al hacer scroll o resize
  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;

    updateScrollHints();
    el.addEventListener("scroll", updateScrollHints, { passive: true });

    const ro = new ResizeObserver(updateScrollHints);
    ro.observe(el);

    return () => {
      el.removeEventListener("scroll", updateScrollHints);
      ro.disconnect();
    };
  }, [updateScrollHints]);

  return {
    containerRef,
    tabRefs,
    canScrollLeft,
    canScrollRight,
    scrollBy: (delta: number) => {
      containerRef.current?.scrollBy({ left: delta, behavior: "smooth" });
    },
  };
}