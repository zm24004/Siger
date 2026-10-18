/**
 * Lista de países (con código telefónico, ISO 3166-1 alpha-2, nombre y bandera emoji).
 * El Salvador es el país ancla del proyecto, por eso aparece primero y se usa
 * como valor por defecto cuando no se puede detectar el del navegador.
 */
export const PAISES = [
  { codigo: '+503', iso: 'SV', nombre: 'El Salvador', bandera: '🇸🇻' },
  { codigo: '+502', iso: 'GT', nombre: 'Guatemala', bandera: '🇬🇹' },
  { codigo: '+501', iso: 'BZ', nombre: 'Belice', bandera: '🇧🇿' },
  { codigo: '+504', iso: 'HN', nombre: 'Honduras', bandera: '🇭🇳' },
  { codigo: '+505', iso: 'NI', nombre: 'Nicaragua', bandera: '🇳🇮' },
  { codigo: '+506', iso: 'CR', nombre: 'Costa Rica', bandera: '🇨🇷' },
  { codigo: '+507', iso: 'PA', nombre: 'Panamá', bandera: '🇵🇦' },
  { codigo: '+52',  iso: 'MX', nombre: 'México', bandera: '🇲🇽' },
  { codigo: '+1',   iso: 'US', nombre: 'Estados Unidos', bandera: '🇺🇸' },
  { codigo: '+34',  iso: 'ES', nombre: 'España', bandera: '🇪🇸' },
  { codigo: '+57',  iso: 'CO', nombre: 'Colombia', bandera: '🇨🇴' },
  { codigo: '+58',  iso: 'VE', nombre: 'Venezuela', bandera: '🇻🇪' },
  { codigo: '+51',  iso: 'PE', nombre: 'Perú', bandera: '🇵🇪' },
  { codigo: '+56',  iso: 'CL', nombre: 'Chile', bandera: '🇨🇱' },
  { codigo: '+54',  iso: 'AR', nombre: 'Argentina', bandera: '🇦🇷' },
  { codigo: '+598', iso: 'UY', nombre: 'Uruguay', bandera: '🇺🇾' },
  { codigo: '+591', iso: 'BO', nombre: 'Bolivia', bandera: '🇧🇴' },
  { codigo: '+593', iso: 'EC', nombre: 'Ecuador', bandera: '🇪🇨' },
  { codigo: '+595', iso: 'PY', nombre: 'Paraguay', bandera: '🇵🇾' },
  { codigo: '+55',  iso: 'BR', nombre: 'Brasil', bandera: '🇧🇷' }
]

/**
 * Detecta el país del navegador usando navigator.language.
 * Ejemplos: "es-SV" → SV → El Salvador, "es-MX" → MX → México, "en-US" → US.
 * Si no puede determinarse, cae al primero de la lista (El Salvador).
 */
export function detectarPais() {
  try {
    const lang = navigator.language || navigator.languages?.[0] || ''
    const partes = lang.split('-')
    // navigator.language puede venir como "es-SV", "es-419", "en-US", "es" (sin región)…
    // Buscamos la parte de la región (la última parte con 2 letras ASCII).
    const iso = partes.length > 1 ? partes[partes.length - 1].toUpperCase() : null
    if (iso && /^[A-Z]{2}$/.test(iso)) {
      const found = PAISES.find((p) => p.iso === iso)
      if (found) return found
    }
  } catch {
    // Navegadores antiguos o sin permisos: cae al default.
  }
  return PAISES[0]
}

export default PAISES
