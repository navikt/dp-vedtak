# language: no

  Egenskap: VurderTerskelForTaptArbeidstid

    Bakgrunn: Dagpenger under permittering fra fiskeindustrien innvilges fra 12. desember
      Gitt en ny hendelse om innvilget søknad
        | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet              |
        | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 12.12.2022    | 588     | 52             | 8                      | PermitteringFraFiskeindustrien |

      Scenario: Rapporterer arbeidstimer (48 t) eksakt lik terskel, som er 40% for fiskeindustrien
        Når rapporteringshendelse mottas
          | dato       | fravær | timer |
          | 12.12.2022 | false  | 8     |
          | 13.12.2022 | false  | 8     |
          | 14.12.2022 | false  | 4     |
          | 15.12.2022 | false  | 4     |
          | 16.12.2022 | false  | 4     |
          | 17.12.2022 | false  | 0     |
          | 18.12.2022 | false  | 0     |
          | 19.12.2022 | false  | 4     |
          | 20.12.2022 | false  | 4     |
          | 21.12.2022 | false  | 4     |
          | 22.12.2022 | false  | 4     |
          | 23.12.2022 | false  | 4     |
          | 24.12.2022 | false  | 0     |
          | 25.12.2022 | false  | 0     |
        Så skal forbruket være 10 dager
        Så skal bruker ha 2 vedtak

      Scenario: Rapporterer arbeidstimer over terskel
        Når rapporteringshendelse mottas
          | dato       | fravær | timer |
          | 12.12.2022 | false  | 8     |
          | 13.12.2022 | false  | 8     |
          | 14.12.2022 | false  | 4     |
          | 15.12.2022 | false  | 4     |
          | 16.12.2022 | false  | 4     |
          | 17.12.2022 | false  | 0     |
          | 18.12.2022 | false  | 0     |
          | 19.12.2022 | false  | 4     |
          | 20.12.2022 | false  | 4     |
          | 21.12.2022 | false  | 4     |
          | 22.12.2022 | false  | 4     |
          | 23.12.2022 | false  | 4.5   |
          | 24.12.2022 | false  | 0     |
          | 25.12.2022 | false  | 0     |
          Så skal forbruket være 0 dager
          Så skal bruker ha 2 vedtak
