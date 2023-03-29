# language: no
Egenskap: Ventedager

  Bakgrunn: Alt er fastsatt
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | ventetid |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 12.12.2022    | 588     | 490921   | 52             | 8                      | Ordinær           | 3        |

  Scenario: Rapporterer arbeidstimer eksakt lik terskel
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 12.12.2022 | false  | 0     |
      | 13.12.2022 | false  | 0     |
      | 14.12.2022 | false  | 0     |
      | 15.12.2022 | false  | 8     |
      | 16.12.2022 | false  | 8     |
      | 17.12.2022 | false  | 0     |
      | 18.12.2022 | false  | 0     |
      | 19.12.2022 | false  | 4     |
      | 20.12.2022 | false  | 4     |
      | 21.12.2022 | false  | 4     |
      | 22.12.2022 | false  | 4     |
      | 23.12.2022 | false  | 4     |
      | 24.12.2022 | false  | 0     |
      | 25.12.2022 | false  | 0     |
    Så skal forbruket være 7 dager
    Så skal ventedager være avspasert, altså 0 timer
    Så skal bruker ha 2 vedtak