import { SummaryUsers } from "./summary/SummaryUsers";
import { SummaryOrganizers } from "./summary/SummaryOrganizers";
import { SummaryParticipants } from "./summary/SummaryParticipants";
import { SummaryEvents } from "./summary/SummaryEvents";
import { MenuOptionCard } from "../../components/ui/card/MenuOptionCard";
import { Calendars, Users } from "lucide-react";
import { ComingSoonCard } from "../../components/ui/card/ComingSoonCard";
import { HeaderAdmin } from "./HeaderAdmin";

export default function PanelAdmin() {


  return (
    <>
      <div className="panel-wrapper">
        <HeaderAdmin/>
        <section className="panel-stats mt-12" aria-label="Resumen del sistema">
          <SummaryUsers />
          <SummaryOrganizers />
          <SummaryParticipants />
          <SummaryEvents />
        </section>
      </div>

      <div className="grid grid-cols-12 lg:gap-6 md:gap-2 mt-12 w-full">

        <div className="col-span-6">
          <MenuOptionCard
            navigateTo="/admin/usuarios"
            iconCard={Users}
            titleCard="Gestionar Usuarios"
            descriptionCard="Administrar y supervisar todos los usuarios del sistema."
          />
        </div>
        <div className="col-span-6 flex justify-center">

          <MenuOptionCard
            navigateTo="/admin/eventos"
            iconCard={Calendars}
            titleCard="Gestionar Eventos"
            descriptionCard="Administrar y gestionar todos los eventos"
          />
        </div>

        <div className="col-span-12 flex justify-center">
          <ComingSoonCard title="Próximamente Reportes" icon={Calendars} colorVariant='purple'/>
        </div>
      </div>
    </>
  );
}
