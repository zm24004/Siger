export interface Reservacion {
    idReservacion:      number;
    codigoReservacion:  string;
    estado:             string;
    fechaReservacion:   Date;
    idEvento:           number;
    eventoNombre:       string;
    eventoFecha:        Date;
    eventoHoraInicio:   string;
    participanteNombre: string;
}
