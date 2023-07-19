# language: no

  Egenskap: VurderTerskelForTaptArbeidstid

    Bakgrunn: Ordinære dagpenger er innvileget fra 1. mai (uten egenandel). Fra 8. mai endres vanligArbeidstidPerDag fra 8 til 4 timer. VA for meldeperioden blir da 60 timer.
      Gitt en ny hendelse om innvilget søknad
        | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | egenandel |
        | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 01.05.2023    | 800     | 333333   | 104            | 8                      | Ordinær           | 0         |

      Gitt et endringsvedtak
        | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | egenandel |
        | 12345678901   | CA1BD250-8A03-45AA-B9A3-5BC5A49B7076 | true   | 08.05.2023    | 800     | 333333   | 104            | 4                      | Ordinær           | 0         |

      Scenario: Rapporterer arbeidstimer eksakt lik terskel
        Når rapporteringshendelse mottas
          | dato       | fravær | timer |
          | 01.05.2023 | false  | 5     |
          | 02.05.2023 | false  | 5     |
          | 03.05.2023 | false  | 5     |
          | 04.05.2023 | false  | 0     |
          | 05.05.2023 | false  | 0     |
          | 06.05.2023 | false  | 0     |
          | 07.05.2023 | false  | 0     |
          | 08.05.2023 | false  | 5     |
          | 09.05.2023 | false  | 5     |
          | 10.05.2023 | false  | 5     |
          | 11.05.2023 | false  | 0     |
          | 12.05.2023 | false  | 0     |
          | 13.05.2023 | false  | 0     |
          | 14.05.2023 | false  | 0     |
        Så skal forbruket være 10 dager
        Så skal bruker ha 3 vedtak

    Scenario: Rapporterer arbeidstimer eksakt lik terskel
      Når rapporteringshendelse mottas
        | dato       | fravær | timer |
        | 01.05.2023 | false  | 5.5   |
        | 02.05.2023 | false  | 5     |
        | 03.05.2023 | false  | 5     |
        | 04.05.2023 | false  | 0     |
        | 05.05.2023 | false  | 0     |
        | 06.05.2023 | false  | 0     |
        | 07.05.2023 | false  | 0     |
        | 08.05.2023 | false  | 5     |
        | 09.05.2023 | false  | 5     |
        | 10.05.2023 | false  | 5     |
        | 11.05.2023 | false  | 0     |
        | 12.05.2023 | false  | 0     |
        | 13.05.2023 | false  | 0     |
        | 14.05.2023 | false  | 0     |
      Så skal forbruket være 0 dager
      Så skal bruker ha 3 vedtak

