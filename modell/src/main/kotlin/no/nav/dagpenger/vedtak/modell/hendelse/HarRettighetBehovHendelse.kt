package no.nav.dagpenger.vedtak.modell.hendelse

class HarRettighetBehovHendelse(val fnr: String) : Hendelse

class NyRettighetHendelse(val søknad_uuid: String) : Hendelse
