export interface EventoDetalle {
    idEvento:          number;
    nombre:            string;
    fecha:             Date;
    horaInicio:        string;
    capacidadMaxima:   number;
    estado:            string;
    cuposDisponibles:  number;
    descripcion:       string;
    horaFin:           string;
    idUbicacion:       number;
    ubicacionNombre:   string;
    idOrganizador:     number;
    organizadorNombre: string;
}


export interface CrearEventoPayload {
    nombre:          string;
    fecha:           Date | string;
    horaInicio:      string;
    capacidadMaxima: number;
    idUbicacion:     number;
    descripcion:     string;
    horaFin:         string;
}
