type TipoUbicacion = 'Fisica' | 'Virtual';

export interface Ubicacion {
    idUbicacion:   number;
    nombre:        string;
    tipo:          TipoUbicacion;
    direccion:     string;
    enlaceVirtual: string;
}


export interface CrearUbicacionPayload {
    nombre:        string;
    direccion:     string;
    tipo:          TipoUbicacion;
    enlaceVirtual: string;
}

