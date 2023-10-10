# language: no

  Egenskap: VurderRettTilDagpenger

    Bakgrunn: Ordinære dagpenger er innvileget fra 26. desember
      Gitt en ny hendelse om innvilget søknad
        | fødselsnummer | behandlingId                         | utfall      | virkningsdato | dagsats | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet |
        | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | Innvilgelse | 05.01.2023    | 800     | 104            | 8                      | Ordinær           |

      Scenario: Rapporteing for meldeperiode som i helhet er før virkningsdato for innvilgelse av dagpenger, skal gi utfall false
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
        Så skal utfall være "false"
        Så skal bruker ha 2 vedtak

    Scenario: Rapporteing med fravær på alle dagene etter virkningsdato for innvilgelse av dagpenger, skal gi utfall false
        Når rapporteringshendelse mottas
          | dato       | fravær | timer |
          | 26.12.2022 | false  | 0     |
          | 27.12.2022 | false  | 0     |
          | 28.12.2022 | false  | 0     |
          | 29.12.2022 | false  | 0     |
          | 30.12.2022 | false  | 0     |
          | 31.12.2022 | false  | 0     |
          | 01.01.2023 | false  | 0     |
          | 02.01.2023 | false  | 0     |
          | 03.01.2023 | false  | 0     |
          | 04.01.2023 | false  | 0     |
          | 05.01.2023 | true   | 0     |
          | 06.01.2023 | true   | 0     |
          | 07.01.2023 | false  | 0     |
          | 08.01.2023 | false  | 0     |
        Så skal utfall være "false"
        Så skal bruker ha 2 vedtak

    Scenario: Rapporteing med minst én tellende dag etter virkningsdato for innvilgelse av dagpenger, skal gi utfall true
      Når rapporteringshendelse mottas
        | dato       | fravær | timer |
        | 26.12.2022 | false  | 0     |
        | 27.12.2022 | false  | 0     |
        | 28.12.2022 | false  | 0     |
        | 29.12.2022 | false  | 0     |
        | 30.12.2022 | false  | 0     |
        | 31.12.2022 | false  | 0     |
        | 01.01.2023 | false  | 0     |
        | 02.01.2023 | false  | 0     |
        | 03.01.2023 | false  | 0     |
        | 04.01.2023 | false  | 0     |
        | 05.01.2023 | true   | 0     |
        | 06.01.2023 | false  | 0     |
        | 07.01.2023 | false  | 0     |
        | 08.01.2023 | false  | 0     |
      Så skal utfall være "true"
      Så skal bruker ha 2 vedtak
