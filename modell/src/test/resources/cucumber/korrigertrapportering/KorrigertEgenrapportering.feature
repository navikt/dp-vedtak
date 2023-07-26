# language: no

Egenskap: Korrigert egenrapportering

  Bakgrunn: Ordinære dagpenger er innvilget fra 12. desember
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | egenandel |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 12.12.2022    | 800     | 52             | 8                      | Ordinær           | 2400      |

  Scenario: Rapporterer først ingen arbeidstimer. Korrigerer deretter med arbeid = 41 timer, altså over terskel.
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
    Så skal gjenstående stønadsdager være 250 fra "25.12.2022"
    Så skal utbetalingen være 5600
    Så skal beregnet utbetaling være 5600 kr for "25.12.2022"
    Så skal bruker ha 2 vedtak

    # her kommer en korrigering av forrige egenrapportering
    # her jobbes det over terskel, altså 50%
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
    Så skal gjenstående egenandel være 2400 fra "25.12.2022"
    Så skal gjenstående stønadsdager være 260 fra "25.12.2022"
    Så skal utbetalingen være 0
    Så skal beregnet utbetaling være 0 kr for "25.12.2022"
    Så skal bruker ha 3 vedtak

  Scenario: Rapporterer først ingen arbeidstimer og ingen fravær. Korrigerer deretter med fravær på en arbeidsdag
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
    Så skal gjenstående stønadsdager være 250 fra "25.12.2022"
    Så skal utbetalingen være 5600
    Så skal beregnet utbetaling være 5600 kr for "25.12.2022"
    Så skal bruker ha 2 vedtak

    # her kommer en korrigering av forrige egenrapportering med en fraværsdag
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 12.12.2022 | true   | 0     |
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
    Så skal forbruket være 9 dager
    Så skal gjenstående egenandel være 0 fra "25.12.2022"
    Så skal gjenstående stønadsdager være 251 fra "25.12.2022"
    Så skal utbetalingen være 4800
    Så skal beregnet utbetaling være 4800 kr for "25.12.2022"
    Så skal bruker ha 3 vedtak

  Scenario: Korrigerer meldeperiode nummer 2 med arbeid over terskel
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
    Så skal gjenstående stønadsdager være 250 fra "25.12.2022"
    Så skal utbetalingen være 5600
    Så skal beregnet utbetaling være 5600 kr for "25.12.2022"
    Så skal bruker ha 2 vedtak

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
    Så skal gjenstående egenandel være 0 fra "08.01.2023"
    Så skal gjenstående stønadsdager være 240 fra "08.01.2023"
    Så skal utbetalingen være 8000
    Så skal beregnet utbetaling være 8000 kr for "08.01.2023"
    Så skal bruker ha 3 vedtak

    # Korrigering av andre meldeperiode, hvor det jobbes over terskel
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 26.12.2022 | false  | 8     |
      | 27.12.2022 | false  | 8     |
      | 28.12.2022 | false  | 8     |
      | 29.12.2022 | false  | 8     |
      | 30.12.2022 | false  | 8     |
      | 31.12.2022 | false  | 0     |
      | 01.01.2023 | false  | 0     |
      | 02.01.2023 | false  | 1     |
      | 03.01.2023 | false  | 0     |
      | 04.01.2023 | false  | 0     |
      | 05.01.2023 | false  | 0     |
      | 06.01.2023 | false  | 0     |
      | 07.01.2023 | false  | 0     |
      | 08.01.2023 | false  | 0     |
    Så skal forbruket være 0 dager
    Så skal gjenstående egenandel være 0 fra "25.12.2022"
    Så skal gjenstående egenandel være 0 fra "08.01.2023"
    Så skal gjenstående stønadsdager være 250 fra "25.12.2022"
    Så skal gjenstående stønadsdager være 250 fra "08.01.2023"
    Så skal utbetalingen være 0
    Så skal beregnet utbetaling være 5600 kr for "25.12.2022"
    Så skal beregnet utbetaling være 0 kr for "08.01.2023"
    Så skal bruker ha 4 vedtak

  @wip
  # @todo: Avrundingsproblem - se sak  https://favro.com/organization/98c34fb974ce445eac854de0/696529a0ddfa866861cfa6b6?card=NAV-13898
  Scenario: Korrigerer meldeperiode nummer 2 med fravær og arbeid
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
    Så skal gjenstående stønadsdager være 250 fra "25.12.2022"
    Så skal utbetalingen være 5600
    Så skal beregnet utbetaling være 5600 kr for "25.12.2022"
    Så skal bruker ha 2 vedtak

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
    Så skal gjenstående egenandel være 0 fra "25.12.2022"
    Så skal gjenstående egenandel være 0 fra "08.01.2023"
    Så skal gjenstående stønadsdager være 250 fra "25.12.2022"
    Så skal gjenstående stønadsdager være 240 fra "08.01.2023"
    Så skal utbetalingen være 8000
    Så skal beregnet utbetaling være 5600 kr for "25.12.2022"
    Så skal beregnet utbetaling være 8000 kr for "08.01.2023"
    Så skal bruker ha 3 vedtak

    # Korrigering av andre meldeperiode, hvor det er fravær og noe arbeid
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 26.12.2022 | false  | 0     |
      | 27.12.2022 | false  | 0     |
      | 28.12.2022 | false  | 0     |
      | 29.12.2022 | false  | 4     |
      | 30.12.2022 | false  | 4     |
      | 31.12.2022 | false  | 0     |
      | 01.01.2023 | false  | 0     |
      | 02.01.2023 | false  | 0     |
      | 03.01.2023 | false  | 0     |
      | 04.01.2023 | false  | 0     |
      | 05.01.2023 | true   | 0     |
      | 06.01.2023 | false  | 0     |
      | 07.01.2023 | false  | 0     |
      | 08.01.2023 | false  | 0     |
    Så skal forbruket være 9 dager
    Så skal gjenstående egenandel være 0 fra "08.01.2023"
    Så skal gjenstående stønadsdager være 241 fra "08.01.2023"
    Så skal utbetalingen være 6400
    Så skal beregnet utbetaling være 6400 kr for "08.01.2023"
    Så skal bruker ha 4 vedtak

  @wip
  # @todo: reberegning av etterfølgende meldeperioder
  Scenario: Leverer tre meldeperioder der egenandel trekkes på alle tre periodene. Korrigerer deretter første periode,
  med arbeid over terskel. Etterfølgende perioder reberegnes.
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 12.12.2022 | true   | 0     |
      | 13.12.2022 | true   | 0     |
      | 14.12.2022 | true   | 0     |
      | 15.12.2022 | true   | 0     |
      | 16.12.2022 | true   | 0     |
      | 17.12.2022 | false  | 0     |
      | 18.12.2022 | false  | 0     |
      | 19.12.2022 | true   | 0     |
      | 20.12.2022 | true   | 0     |
      | 21.12.2022 | true   | 0     |
      | 22.12.2022 | false  | 8     |
      | 23.12.2022 | false  | 0     |
      | 24.12.2022 | false  | 0     |
      | 25.12.2022 | false  | 0     |
    Så skal forbruket være 2 dager
    Så skal gjenstående egenandel være 1600 fra "25.12.2022"
    Så skal gjenstående stønadsdager være 258 fra "25.12.2022"
    Så skal utbetalingen være 0
    Så skal beregnet utbetaling være 0 kr for "25.12.2022"
    Så skal bruker ha 2 vedtak

    # Meldeperiode 2 har forbruk på 3 dager, men arbeid som avkorter
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 26.12.2022 | true   | 0     |
      | 27.12.2022 | true   | 0     |
      | 28.12.2022 | true   | 0     |
      | 29.12.2022 | true   | 0     |
      | 30.12.2022 | true   | 0     |
      | 31.12.2022 | false  | 0     |
      | 01.01.2023 | false  | 0     |
      | 02.01.2023 | true   | 0     |
      | 03.01.2023 | true   | 0     |
      | 04.01.2023 | false  | 8     |
      | 05.01.2023 | false  | 2     |
      | 06.01.2023 | false  | 0     |
      | 07.01.2023 | false  | 0     |
      | 08.01.2023 | false  | 0     |
    Så skal forbruket være 3 dager
    Så skal gjenstående egenandel være 200 fra "08.01.2023"
    Så skal gjenstående stønadsdager være 255 fra "08.01.2023"
    Så skal utbetalingen være 0
    Så skal beregnet utbetaling være 0 kr for "08.01.2023"
    Så skal bruker ha 3 vedtak

    # Meldeperiode 3 har full utbetaling minus egenandel
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 09.01.2023 | false  | 0     |
      | 10.01.2023 | false  | 0     |
      | 11.01.2023 | false  | 0     |
      | 12.01.2023 | false  | 0     |
      | 13.01.2023 | false  | 0     |
      | 14.01.2023 | false  | 0     |
      | 15.01.2023 | false  | 0     |
      | 16.01.2023 | false  | 0     |
      | 17.01.2023 | false  | 0     |
      | 18.01.2023 | false  | 0     |
      | 19.01.2023 | false  | 0     |
      | 20.01.2023 | false  | 0     |
      | 21.01.2023 | false  | 0     |
      | 22.01.2023 | false  | 0     |
    Så skal forbruket være 10 dager
    Så skal gjenstående egenandel være 0 fra "22.01.2023"
    Så skal gjenstående stønadsdager være 245 fra "22.01.2023"
    Så skal utbetalingen være 7800
    Så skal beregnet utbetaling være 7800 kr for "22.01.2023"
    Så skal bruker ha 4 vedtak

    ## Korrigerer første meldeperiode, slik at arbeid er over terskel.
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 12.12.2022 | true   | 0     |
      | 13.12.2022 | true   | 0     |
      | 14.12.2022 | true   | 0     |
      | 15.12.2022 | true   | 0     |
      | 16.12.2022 | true   | 0     |
      | 17.12.2022 | false  | 0     |
      | 18.12.2022 | false  | 0     |
      | 19.12.2022 | true   | 0     |
      | 20.12.2022 | true   | 0     |
      | 21.12.2022 | false  | 8     |
      | 22.12.2022 | false  | 8     |
      | 23.12.2022 | false  | 0     |
      | 24.12.2022 | false  | 0     |
      | 25.12.2022 | false  | 0     |
    Så skal forbruket være 0 dager
    Så skal gjenstående egenandel være 2400 fra "25.12.2022"
    Så skal gjenstående egenandel være 1000 fra "08.01.2023"
    Så skal gjenstående egenandel være 0 fra "22.01.2023"
    Så skal gjenstående stønadsdager være 260 fra "25.12.2022"
    Så skal gjenstående stønadsdager være 257 fra "08.01.2023"
    Så skal gjenstående stønadsdager være 247 fra "22.01.2023"
    Så skal utbetalingen være 0
    Så skal beregnet utbetaling være 0 kr for "25.12.2022"
    Så skal beregnet utbetaling være 0 kr for "08.01.2023"
    Så skal beregnet utbetaling være 7000 kr for "22.01.2023"
    Så skal bruker ha 5 vedtak