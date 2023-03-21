# language: no
Egenskap: Ny rettighet innvilget


  Scenario: Innvilget vedtak fra 14.12.2022
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 14.12.2022    | 588     | 490921   | 52             | 8                      |
    Så skal bruker ha 1 vedtak
    Og vedtaket har virkningsdato "14.12.2022"
    Og vedtaket har dagsats på 588, grunnlag 490921, stønadsperiode på 52 uker og vanlig arbeidstid per dag er 8 timer
