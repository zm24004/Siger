import { Link } from 'react-router-dom';
import imageLogo from '../assets/favicon.svg';

interface BrandProps {
  className?: string;
  tone?: 'light' | 'dark';
}

export default function Brand({ className = '', tone = 'light' }: BrandProps) {
  return (
    <Link
      to="/"
      aria-label="SIGER - Inicio"
      className={`inline-flex w-fit items-center gap-2 rounded-md focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-blue-700 ${className}`}
    >
      <img className="h-9 w-9" src={imageLogo} alt="" />
      <span className={`text-xl font-bold ${tone === 'dark' ? 'text-white' : 'text-(--color-primario-oscuro)'}`}>SIGER</span>
    </Link>
  );
}
