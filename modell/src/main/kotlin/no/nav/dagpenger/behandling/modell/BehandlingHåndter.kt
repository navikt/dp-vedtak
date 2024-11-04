package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.AvklaringIkkeRelevantHendelse
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
import no.nav.dagpenger.behandling.modell.hendelser.LåsHendelse
import no.nav.dagpenger.behandling.modell.hendelser.LåsOppHendelse
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PåminnelseHendelse
import no.nav.dagpenger.behandling.modell.hendelser.StartHendelse

interface PersonHåndter : BehandlingHåndter

interface BehandlingHåndter {
    fun håndter(hendelse: StartHendelse)

    fun håndter(hendelse: OpplysningSvarHendelse)

    fun håndter(hendelse: AvbrytBehandlingHendelse)

    fun håndter(hendelse: ForslagGodkjentHendelse)

    fun håndter(hendelse: LåsHendelse)

    fun håndter(hendelse: LåsOppHendelse)

    fun håndter(hendelse: AvklaringIkkeRelevantHendelse)

    fun håndter(hendelse: PåminnelseHendelse)
}
