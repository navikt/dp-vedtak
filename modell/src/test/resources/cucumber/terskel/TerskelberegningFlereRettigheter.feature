# language: no

  Egenskap: Terskel

    Bakgrunn: Dagpenger under permittering fra fiskeindustrien innvilget fra 12. desember. Endres til ordinære dagpenger 15. desember. Gir ulike terskler i meldeperioden.
      Gitt en ny hendelse om innvilget søknad
        | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet              | egenandel |
        | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 12.12.2022    | 588     | 490921   | 52             | 8                      | PermitteringFraFiskeindustrien | 0         |


      Gitt et endringsvedtak
        | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | egenandel |
        | 12345678901   | CA1BD250-8A03-45AA-B9A3-5BC5A49B7076 | true   | 15.12.2022    | 588     | 490921   | 52             | 8                      | Ordinær           | 0         |

      Scenario: Rapporterer arbeidstimer eksakt lik gjennomsnittsterskel
        Når rapporteringshendelse mottas
          | dato       | fravær | timer |
          | 12.12.2022 | false  | 0     |
          | 13.12.2022 | false  | 8     |
          | 14.12.2022 | false  | 8     |
          | 15.12.2022 | false  | 2.4   |
          | 16.12.2022 | false  | 8     |
          | 17.12.2022 | false  | 0     |
          | 18.12.2022 | false  | 0     |
          | 19.12.2022 | false  | 0     |
          | 20.12.2022 | false  | 4     |
          | 21.12.2022 | false  | 4     |
          | 22.12.2022 | false  | 4     |
          | 23.12.2022 | false  | 4     |
          | 24.12.2022 | false  | 0     |
          | 25.12.2022 | false  | 0     |
        Så skal forbruket være 10 dager
        Så skal bruker ha 3 vedtak

    Scenario: Rapporterer arbeidstimer 0,1 time over gjennomsnittsterskel
      Når rapporteringshendelse mottas
        | dato       | fravær | timer |
        | 12.12.2022 | false  | 0     |
        | 13.12.2022 | false  | 8     |
        | 14.12.2022 | false  | 8     |
        | 15.12.2022 | false  | 2.5   |
        | 16.12.2022 | false  | 8     |
        | 17.12.2022 | false  | 0     |
        | 18.12.2022 | false  | 0     |
        | 19.12.2022 | false  | 0     |
        | 20.12.2022 | false  | 4     |
        | 21.12.2022 | false  | 4     |
        | 22.12.2022 | false  | 4     |
        | 23.12.2022 | false  | 4     |
        | 24.12.2022 | false  | 0     |
        | 25.12.2022 | false  | 0     |
      Så skal forbruket være 0 dager
      Så skal bruker ha 3 vedtak
