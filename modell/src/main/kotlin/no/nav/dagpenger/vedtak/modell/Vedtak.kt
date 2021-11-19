package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelse.Hendelse

abstract class Vedtak(hendelse: Hendelse, val avtale: Avtale?)

class Hovedvedtak(hendelse: Hendelse, avtale: Avtale?) : Vedtak(hendelse, avtale) {
    constructor(hendelse: Hendelse) : this(hendelse, null)
}

class AvslåttEndringsvedtak(hendelse: Hendelse, avtale: Avtale?) : Vedtak(hendelse, avtale){}
class InnvilgetEndringsvedtak(hendelse: Hendelse, avtale: Avtale?) : Vedtak(hendelse, avtale){}


//Endringsønske som ender med nei. F.eks. avslag på en ønsket endring, f.eks. mer dagpenger i form av barnetillegg
//Vi har vurdert henvendelsen din, men vi har kommet til at det ikke endrer din situasjon.
//Dette er fortsatt et vedtak, fordi vi er bedt om å endre en rett, men vi gjør det ikke. Typisk klagesituasjon.
//Veldig viktig mtp. historikk for brukeren.

class Stansvedtak(hendelse: Hendelse, avtale: Avtale?) : Vedtak(hendelse, avtale)
