
import { Rocket, type LucideIcon } from 'lucide-react';

type colorVariant = 'indigo' | 'amber' | 'emerald' | 'purple';
interface ComingSoonProps {
  title?: string;
  description?: string;
  icon?: LucideIcon;
  colorVariant?: colorVariant;
}

export function ComingSoonCard({
  title = "Próximamente",
  description = "Estamos construyendo esta funcionalidad para mejorar tu experiencia. ¡Estará lista muy pronto!",
  icon: Icon = Rocket,
  colorVariant = 'indigo'
}: ComingSoonProps) {
  
  const colorStyles = {
    indigo: "bg-(--color-primario-claro) text-(--color-primario) ring-(--color-primario-claro)",
    amber: "bg-(--color-advertencia-claro) text-(--color-advertencia) ring-(--color-advertencia-claro)",
    emerald: "bg-(--color-exito-claro) text-(--color-exito) ring-(--color-exito-claro)",
    purple: "bg-(--color-info-claro) text-(--color-info) ring-(--color-info-claro)",
  };

  return (
    <div className="relative flex w-full flex-col items-center justify-center overflow-hidden rounded-2xl border border-(--color-borde) bg-white p-8 text-center shadow-sm transition-shadow duration-300 hover:shadow-md select-none">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(var(--color-primario-claro)_1px,transparent_1px)] [background-size:18px_18px] opacity-60" />

      <div className={`relative z-10 mb-6 rounded-full p-4 ring-4 ${colorStyles[colorVariant]}`}>
        <Icon size={32} strokeWidth={1.75} />
      </div>

      <div className="relative z-10 max-w-sm">
        <h3 className="text-xl font-extrabold tracking-tight text-(--color-texto) py-2">
          {title}
        </h3>
        <p className="mt-2.5 text-sm leading-relaxed text-(--color-texto-secundario)">
          {description}
        </p>
      </div>

      <div className="mt-6 relative z-10">
        <span className="inline-flex items-center gap-1.5 rounded-full border border-(--color-advertencia-claro) bg-(--color-advertencia-claro) px-3 py-1 text-xs font-semibold text-(--color-advertencia)">
          <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-(--color-advertencia)" />
          En desarrollo
        </span>
      </div>
    </div>
  );
}
