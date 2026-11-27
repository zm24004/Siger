import { useState, useEffect } from "react";
import { ContentSummaryCard } from "../../../components/ui/card/ContentSummaryCard";
import { eventoService } from "../../../api/eventos";

export const SummaryEvents = () => {
    const [countEvents, setCountEvents] = useState<number>(0)

    useEffect(() => {
        eventoService
        eventoService.countEventosActivosPaginado().then(counter => {
            setCountEvents(counter);
        });
    }, [])
    return (
        <>
            <ContentSummaryCard
                title="Eventos"
                countSummary={`${countEvents}`}
                navigationTitle="Ver catálogo"
                navigateToPath="/eventos"
                customColorLabelClass="stat-card-purple"
            />
        </>
    )
}
