import { useEffect, useState } from 'react'
import { usuarioService } from '../../../api/usuarios';
import { ContentSummaryCard } from '../../../components/ui/card/ContentSummaryCard';

export const SummaryParticipants = () => {
    const [countParticipants, setCountParticipants] = useState<number>(0)

    useEffect(() => {
        usuarioService.countParticipantUsers().then(counter => {
            setCountParticipants(counter);
        });
    }, [])

    return (
        <>
            <ContentSummaryCard
                title="Participantes"
                countSummary={`${countParticipants}`}
                resumeDescription='Reservan lugares'
                customColorLabelClass='stat-card-exito'
            />
        </>
    )
}
