# language: no
Egenskap: Egenandel

  Bakgrunn: Alt er fastsatt
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | egenandel |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 12.12.2022    | 800     | 333333   | 104            | 8                      | Ordinær           | 2400      |

  Scenario: Rapporterer arbeidstimer under terskel og skal betale all egenandel på de tre første dagene
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
    Så skal forbruket være 10 dager
    Så skal gjenstående egenandel være 0 fra "25.12.2022"
    Så skal bruker ha 2 vedtak
    #Så skal utbetalingen være 4000

  Scenario: Rapporterer arbeidstimer over terskel og skal dermed ikke betale noe egenandel
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 12.12.2022 | false  | 0     |
      | 13.12.2022 | false  | 0     |
      | 14.12.2022 | false  | 0     |
      | 15.12.2022 | false  | 8     |
      | 16.12.2022 | false  | 8     |
      | 17.12.2022 | false  | 0     |
      | 18.12.2022 | false  | 0     |
      | 19.12.2022 | false  | 8     |
      | 20.12.2022 | false  | 8     |
      | 21.12.2022 | false  | 8     |
      | 22.12.2022 | false  | 8     |
      | 23.12.2022 | false  | 8     |
      | 24.12.2022 | false  | 0     |
      | 25.12.2022 | false  | 0     |
    Så skal forbruket være 0 dager
    Så skal gjenstående egenandel være 2400 fra "25.12.2022"
    Så skal bruker ha 2 vedtak


  Scenario: Vedtak starter midt i meldeperioden og rapporterer slik at ikke all egenandel trekkes
    Når rapporteringshendelse mottas
      | dato       | fravær | timer |
      | 05.12.2022 | false  | 0     |
      | 06.12.2022 | false  | 0     |
      | 07.12.2022 | false  | 0     |
      | 08.12.2022 | false  | 0     |
      | 09.12.2022 | false  | 0     |
      | 10.12.2022 | false  | 0     |
      | 11.12.2022 | false  | 0     |
      | 12.12.2022 | false  | 8     |
      | 13.12.2022 | false  | 8     |
      | 14.12.2022 | false  | 4     |
      | 15.12.2022 | false  | 0     |
      | 16.12.2022 | false  | 0     |
      | 17.12.2022 | false  | 0     |
      | 18.12.2022 | false  | 0     |
    Så skal forbruket være 5 dager
    Så skal gjenstående egenandel være 400 fra "18.12.2022"
    Så skal bruker ha 2 vedtak