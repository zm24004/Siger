export interface IAsistencia {
    idAsistencia:       number;
    estado:             string;
    fechaAsistencia:    Date;
    horaAsistencia:     string;
    idEvento:           number;
    eventoNombre:       string;
    codigoReservacion:  string;
    participanteNombre: string;
}
