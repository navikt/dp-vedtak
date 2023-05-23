# language: no
@wip
Egenskap: Korrigert egenrapportering på slutten av stønadsperioden

  Bakgrunn: 2 arbeidsuker igjen av ordinære dagpenger
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | egenandel |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 12.12.2022    | 800     | 333333   | 2              | 8                      | Ordinær           | 0         |

  Scenario: Rapporterer først ingen arbeidstimer, slik at dagpengeperioden brukes opp. Korrigerer deretter med arbeid over terskel,
  slik at to uker likevel gjenstår og ny rapportering for påfølgende uker leveres.

    # Leverer egenrapportering som medfører at dagpengeperioden brukes opp.
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
    Så skal utbetalingen være 8000
    Så skal gjenstående stønadsdager være 0
    Så skal bruker ha 2 vedtak

    # Korrigerer forrige egenrapportering med arbeid over terskel, slik at forbruk og utbetaling skal nullstilles.
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 12.12.2022 | false  | 8     |
      | 13.12.2022 | false  | 8     |
      | 14.12.2022 | false  | 8     |
      | 15.12.2022 | false  | 8     |
      | 16.12.2022 | false  | 8     |
      | 17.12.2022 | false  | 0     |
      | 18.12.2022 | false  | 0     |
      | 19.12.2022 | false  | 1     |
      | 20.12.2022 | false  | 0     |
      | 21.12.2022 | false  | 0     |
      | 22.12.2022 | false  | 0     |
      | 23.12.2022 | false  | 0     |
      | 24.12.2022 | false  | 0     |
      | 25.12.2022 | false  | 0     |
    Så skal forbruket være 0 dager
    Så skal utbetalingen være 0
    Så skal gjenstående stønadsdager være 10
    Så skal bruker ha 3 vedtak

    # Leverer påfølgende egenrapportering, som igjen medfører at dagpengeperioden er oppbrukt.
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
      | 05.01.2023 | false  | 0     |
      | 06.01.2023 | false  | 0     |
      | 07.01.2023 | false  | 0     |
      | 08.01.2023 | false  | 0     |
    Så skal forbruket være 10 dager
    Så skal utbetalingen være 8000
    Så skal gjenstående stønadsdager være 0
    Så skal bruker ha 4 vedtak

  Scenario: Rapporterer først fravær en dag, slik at 1 dag gjenstår av dagpengeperioden. Rapporterer påfølgende uke, slik at dagpengeperioden er oppbrukt.
  Korrigerer deretter første rapportering, slik at dagpengeperioden brukes opp tidligere.

    # Leverer egenrapportering som medfører at dagpengeperioden brukes opp.
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 12.12.2022 | false  | 0     |
      | 13.12.2022 | false  | 0     |
      | 14.12.2022 | true   | 0     |
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
    Så skal forbruket være 9 dager
    Så skal utbetalingen være 7200
    Så skal gjenstående stønadsdager være 1
    Så skal bruker ha 2 vedtak

    # Leverer påfølgende egenrapportering, som medfører at dagpengeperioden er oppbrukt.
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
      | 05.01.2023 | false  | 0     |
      | 06.01.2023 | false  | 0     |
      | 07.01.2023 | false  | 0     |
      | 08.01.2023 | false  | 0     |
    Så skal forbruket være 1 dager
    Så skal utbetalingen være 800
    Så skal gjenstående stønadsdager være 0
    Så skal bruker ha 3 vedtak

    # Korrigerer første egenrapportering og fjerner fraværet, slik at dagpengeperioden blir brukt opp
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
    Så skal utbetalingen være 8000
    Så skal gjenstående stønadsdager være 0
    Så skal bruker ha 4 vedtak