export interface Notificacion {
    idNotificacion: number;
    mensaje:        string;
    tipo:           string;
    leido:          boolean;
    fechaEnvio:     Date;
}

export interface NotificacionCursor {
    fechaEnvio: string;
    idNotificacion: number;
}

export interface NotificacionPagina {
    content: Notificacion[];
    nextCursor: NotificacionCursor | null;
    hasMore: boolean;
}

