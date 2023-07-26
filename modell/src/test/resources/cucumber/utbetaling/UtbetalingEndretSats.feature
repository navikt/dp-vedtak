# language: no
Egenskap: Utbetaling

  Bakgrunn: Ordinære dagpenger er innvilget fra 12. desember med egenandel på tre dagsatser og dagsats på 800 kroner. Dagsats endres pga. barnetillegg fra søndag i uke 1.
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | egenandel |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 12.12.2022    | 800     | 52             | 8                      | Ordinær           | 2400      |

    Gitt et endringsvedtak
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | egenandel |
      | 12345678901   | CA1BD250-8A03-45AA-B9A3-5BC5A49B7076 | true   | 18.12.2022    | 835     | 52             | 8                      | Ordinær           | 2400      |

  Scenario: Rapporterer ingen arbeidstimer
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 12.12.2022 | false  | 0     |
      | 13.12.2022 | false  | 0     |
      | 14.12.2022 | false  | 0     |
      | 15.12.2022 | false  | 0     |
      | 16.12.2022 | false  | 0     |
      | 17.12.2022 | false  | 0     |
      | 18.12.2022 | false  | 0     |
      | 19.12.2022 | false  | 0     |
      | 20.12.2022 | false  | 0     |
      | 21.12.2022 | false  | 0     |
      | 22.12.2022 | false  | 0     |
      | 23.12.2022 | false  | 0     |
      | 24.12.2022 | false  | 0     |
      | 25.12.2022 | false  | 0     |
    Så skal forbruket være 10 dager
    Så skal gjenstående egenandel være 0 fra "25.12.2022"
    Så skal utbetalingen være 5775
    Så skal beregnet utbetaling være 5775 kr for "25.12.2022"
    Så skal bruker ha 3 vedtak

  Scenario: Rapporterer arbeid i helg uke 2 tilsvarende to hele arbeidsdager
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 12.12.2022 | false  | 0     |
      | 13.12.2022 | false  | 0     |
      | 14.12.2022 | false  | 0     |
      | 15.12.2022 | false  | 0     |
      | 16.12.2022 | false  | 0     |
      | 17.12.2022 | false  | 0     |
      | 18.12.2022 | false  | 0     |
      | 19.12.2022 | false  | 0     |
      | 20.12.2022 | false  | 0     |
      | 21.12.2022 | false  | 0     |
      | 22.12.2022 | false  | 0     |
      | 23.12.2022 | false  | 0     |
      | 24.12.2022 | false  | 8     |
      | 25.12.2022 | false  | 8     |
    Så skal forbruket være 10 dager
    Så skal gjenstående egenandel være 0 fra "25.12.2022"
    Så skal utbetalingen være 4140
    Så skal beregnet utbetaling være 4140 kr for "25.12.2022"
    Så skal bruker ha 3 vedtak

  Scenario: Rapporterer arbeid i helg uke 1 tilsvarende to hele arbeidsdager. Gir ulik sats lørdag og søndag.
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 12.12.2022 | false  | 0     |
      | 13.12.2022 | false  | 0     |
      | 14.12.2022 | false  | 0     |
      | 15.12.2022 | false  | 0     |
      | 16.12.2022 | false  | 0     |
      | 17.12.2022 | false  | 8     |
      | 18.12.2022 | false  | 8     |
      | 19.12.2022 | false  | 0     |
      | 20.12.2022 | false  | 0     |
      | 21.12.2022 | false  | 0     |
      | 22.12.2022 | false  | 0     |
      | 23.12.2022 | false  | 0     |
      | 24.12.2022 | false  | 0     |
      | 25.12.2022 | false  | 0     |
    Så skal forbruket være 10 dager
    Så skal gjenstående egenandel være 0 fra "25.12.2022"
    Så skal utbetalingen være 4140
    Så skal beregnet utbetaling være 4140 kr for "25.12.2022"
    Så skal bruker ha 3 vedtak
