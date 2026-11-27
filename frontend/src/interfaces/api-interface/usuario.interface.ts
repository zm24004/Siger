export interface UsuariosPaginadosParams{
    rol: string;
    estado: boolean | null;
    page: number;
    limit: number;
}

export interface Usuario {
  idUsuario:     number;
  nombre:        string;
  apellido:      string;
  email:         string;
  telefono:      string;
  codigoPais:    string;
  estado:        boolean;
  rol:           string;
  fechaRegistro: Date;
}


export type USER_FILER_TYPE = 'ADM' | 'PAR' | 'ORG' | null | undefined
export interface UsuarioPaginadoParams{
  page: number;
  limit: number;
  estado?: boolean | null | undefined;
  rol: USER_FILER_TYPE
}