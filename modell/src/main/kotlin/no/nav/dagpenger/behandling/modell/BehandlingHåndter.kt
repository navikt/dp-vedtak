package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.AvklaringIkkeRelevantHendelse
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
import no.nav.dagpenger.behandling.modell.hendelser.NyOpplysningHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PåminnelseHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse

interface PersonHåndter : BehandlingHåndter

interface BehandlingHåndter {
    fun håndter(hendelse: SøknadInnsendtHendelse)

    fun håndter(hendelse: NyOpplysningHendelse)

    fun håndter(hendelse: AvbrytBehandlingHendelse)

    fun håndter(hendelse: ForslagGodkjentHendelse)

    fun håndter(hendelse: AvklaringIkkeRelevantHendelse)

    fun håndter(hendelse: PåminnelseHendelse)
}
