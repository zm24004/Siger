import { useEffect, useState } from 'react'
import { usuarioService } from '../../../api/usuarios'
import { ContentSummaryCard } from '../../../components/ui/card/ContentSummaryCard'

export const SummaryOrganizers = () => {
    const [countOrganizers, setCountOrganizers] = useState<number>(0)

    useEffect(() => {
        usuarioService.countOrganizerUsers().then(counter => {
            setCountOrganizers(counter);
        });
    }, [])

    return (
        <>
            <ContentSummaryCard
                title="Organizadores"
                countSummary={`${countOrganizers}`}
                resumeDescription='Publican eventos'
                customColorLabelClass='stat-card-rol'
            />
        </>
    )
}
