package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Regelverk
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag
import no.nav.dagpenger.regel.fastsetting.DagpengensStørrelse
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode
import no.nav.dagpenger.regel.fastsetting.Egenandel

val RegelverkDagpenger =
    Regelverk(
        Alderskrav.regelsett,
        Dagpengegrunnlag.regelsett,
        DagpengensStørrelse.regelsett,
        Dagpengeperiode.regelsett,
        Egenandel.regelsett,
        KravPåDagpenger.regelsett,
        Medlemskap.regelsett,
        Meldeplikt.regelsett,
        Minsteinntekt.regelsett,
        Opptjeningstid.regelsett,
        ReellArbeidssøker.regelsett,
        Rettighetstype.regelsett,
        StreikOgLockout.regelsett,
        Søknadstidspunkt.regelsett,
        Utdanning.regelsett,
        Utestengning.regelsett,
        Verneplikt.regelsett,
        Virkningstidspunkt.regelsett,
    )
