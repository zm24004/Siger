import { useEffect, useState } from "react"
import { usuarioService } from "../../../api/usuarios";
import { ContentSummaryCard } from "../../../components/ui/card/ContentSummaryCard";

interface SummaryUsers {
    active: number;
    inActive: number;
}

export const SummaryUsers = () => {

    const [countUsers, setCountUsers] = useState<SummaryUsers>({ active: 0, inActive: 0 });

    useEffect(() => {
        Promise.all([
            usuarioService.countActiveUsers().catch(() => 0),
            usuarioService.countInActiveUsers().catch(() => 0),
        ])
            .then(([countActive, countInactive]) => {
                setCountUsers({ active: countActive, inActive: countInactive })
            })
    }, [])


    return (
        <>
            <ContentSummaryCard
                title="Usuarios totales"
                countSummary={`${countUsers.active + countUsers.inActive}`}
                resumeDescription={`${countUsers.active} activos · ${countUsers.inActive} inactivos`}
                customColorLabelClass=""
            />
        </>
    )
}
