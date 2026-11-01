import { Link } from "react-router-dom";

interface ContentSummaryCard {
    title: string;
    countSummary: string;
    resumeDescription?: string;
    navigationTitle?: string;
    navigateToPath?: string;
    customColorLabelClass?: string;
}

export const ContentSummaryCard = (props: ContentSummaryCard) => {
    const { title, countSummary, resumeDescription, navigationTitle, navigateToPath, customColorLabelClass } = props;
    return (
        <>
            <div className={`stat-card ${customColorLabelClass ? customColorLabelClass : ''}`}>

                <span className="stat-label">{title}</span>
                <span className="stat-valor">{countSummary}</span>
                <span className="texto-secundario stat-sub">{resumeDescription}</span>
                {
                    (navigateToPath && navigationTitle) &&
                    <Link to={`${navigateToPath}`} className="stat-link">
                        {navigationTitle} →
                    </Link>
                }
            </div>
        </>
    )
}
