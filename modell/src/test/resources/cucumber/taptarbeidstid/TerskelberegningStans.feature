# language: no

  Egenskap: Stans av dagpengerettighet

    Bakgrunn: Ordinære dagpenger er innvilget fra 12. desember. Stanses fra 19. desember.
      Gitt en ny hendelse om innvilget søknad
        | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet |
        | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 12.12.2022    | 800     | 104            | 8                      | Ordinær           |

      # @todo: Vurder hvordan stans-hendelse skal se ut og håndteres
      Og en ny hendelse om stans
        | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagpengerettighet |
        | 12345678901   | 2D085CE8-F5E3-47B5-8C3B-873FE99E4EF4 | false  | 19.12.2022    | Ordinær           |

      Scenario: Har ikke vedtak hele perioden fordi dagpengevedtaket er stanset fra og med uke 2. Rapporterer arbeidstimer eksakt lik terskel i uke 1, over terskel i uke 2.
      Når rapporteringshendelse mottas
        | dato       | fravær | timer |
        | 12.12.2022 | false  | 4     |
        | 13.12.2022 | false  | 4     |
        | 14.12.2022 | false  | 4     |
        | 15.12.2022 | false  | 4     |
        | 16.12.2022 | false  | 4     |
        | 17.12.2022 | false  | 0     |
        | 18.12.2022 | false  | 0     |
        | 19.12.2022 | false  | 8     |
        | 20.12.2022 | false  | 8     |
        | 21.12.2022 | false  | 8     |
        | 22.12.2022 | false  | 8     |
        | 23.12.2022 | false  | 8     |
        | 24.12.2022 | false  | 0     |
        | 25.12.2022 | false  | 0     |
      Så skal forbruket være 5 dager
      Så skal bruker ha 3 vedtak
      Så skal utbetalingen være 2000

    Scenario: Har ikke vedtak hele perioden fordi dagpengevedtaket er stanset fra og med uke 2. Rapporterer arbeidstimer over terskel i uke 1, under terskel i uke 2.
      Når rapporteringshendelse mottas
        | dato       | fravær | timer |
        | 12.12.2022 | false  | 4.5   |
        | 13.12.2022 | false  | 4     |
        | 14.12.2022 | false  | 4     |
        | 15.12.2022 | false  | 4     |
        | 16.12.2022 | false  | 4     |
        | 17.12.2022 | false  | 0     |
        | 18.12.2022 | false  | 0     |
        | 19.12.2022 | false  | 0     |
        | 20.12.2022 | false  | 0     |
        | 21.12.2022 | false  | 0     |
        | 22.12.2022 | false  | 0     |
        | 23.12.2022 | false  | 0     |
        | 24.12.2022 | false  | 0     |
        | 25.12.2022 | false  | 0     |
      Så skal forbruket være 0 dager
      Så skal bruker ha 3 vedtak
      Så skal utbetalingen være 0
