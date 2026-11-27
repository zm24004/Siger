import { useState, useEffect } from "react";
import { eventoService } from "../../api/eventos";
import { useToast } from "../../context/ToastContext";
import type { EventoDetalle } from "../../interfaces/api-interface/eventos.interface";
import { Loader } from "../../components/Loader";
import { EventSimpleList } from "../../components/ui/EvenSimpleList/EventSimpleList";
import NotFound from "../sistema/NotFound";

export const PopularEvents = () => {
    const toast = useToast();
    const [eventos, setEventos] = useState<EventoDetalle[] | null>([]);
    const [cargando, setCargando] = useState(true);

    useEffect(() => {
        let alive = true
        setCargando(true)
        eventoService.getPopularEvents()
            .then((data) => {
                if (!alive) return
                setEventos(data)
            })
            .catch((err) => {
                if (!alive) return
                toast.error(err.message || 'No se pudieron cargar los eventos.')
            })
            .finally(() => alive && setCargando(false))
        return () => { alive = false }
    }, [])

    if(cargando){
        return (
            <Loader label="Cargando los Eventos más Populares..."/>
        );
    }

    if(!cargando && !eventos){
        return(
            <NotFound/>
        );
    }

    return (
        <EventSimpleList eventList={eventos!}/>
    )
}
