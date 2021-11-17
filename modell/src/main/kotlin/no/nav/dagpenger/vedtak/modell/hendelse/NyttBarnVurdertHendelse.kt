package no.nav.dagpenger.vedtak.modell.hendelse

// TODO: Får vi alltid sats, selv om resultat er false?
class NyttBarnVurdertHendelse(val resultat: Boolean, val sats: Double? = null) : Hendelse
