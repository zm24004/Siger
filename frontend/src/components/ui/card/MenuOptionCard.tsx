import { ArrowRight, type LucideIcon } from 'lucide-react';
import { Link } from 'react-router-dom';

interface MenuOptionCardProps {
    navigateTo: string;
    iconCard: LucideIcon;
    titleCard: string;
    descriptionCard: string;
}

export const MenuOptionCard = (props: MenuOptionCardProps) => {
    const { navigateTo, iconCard: MainIcon, titleCard, descriptionCard } = props;

    return (
        <Link
            to={navigateTo}
            className="group block w-full  rounded-2xl border border-(--color-borde) bg-white p-8 text-(--color-texto) shadow-sm transition-all duration-300 hover:-translate-y-0.5 hover:border-(--color-primario) hover:shadow-md focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-(--color-primario)"
        >
            <div className="flex items-start justify-between">
                <div className="rounded-xl bg-(--color-primario-claro) p-3 text-(--color-primario) transition-transform duration-300 group-hover:scale-105">
                    <MainIcon size={24} />
                </div>

                <span className="text-(--color-primario) transition-transform duration-300 group-hover:translate-x-1">
                    <ArrowRight size={20} />
                </span>
            </div>

            <div className="mt-4">
                <h3 className="text-lg font-bold text-(--color-texto) transition-colors group-hover:text-(--color-primario)">
                    {titleCard}
                </h3>
                <p className="mt-1 line-clamp-2 text-sm text-(--color-texto-secundario)">
                    {descriptionCard}
                </p>
            </div>
        </Link>
    );
};
