# language: no
Egenskap: Ny rettighet innvilget


  Scenario: Innvilget ordinære dagpenger i 104 uker
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 14.12.2022    | 588     | 104            | 8                      | Ordinær           |
    Så skal bruker ha 1 vedtak
    Og vedtaket har dagpengerettighet "Ordinær"
    Og vedtaket har virkningsdato "14.12.2022"
    Og vedtaket har dagsats på 588 kroner
    Og vedtaket har stønadsperiode på 104 uker
    Og vedtaket har vanlig arbeidstid per dag på 8 timer
    Og vedtaket har behandlingId lik "7E7A891C-E8E2-4641-A213-83E3A7841A57"


  Scenario: Innvilget dagpenger under permittering i 52 uker
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats  | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 14.12.2022    | 588      | 52             | 8                      | Permittering      |
    Så skal bruker ha 1 vedtak
    Og vedtaket har dagpengerettighet "Permittering"
    Og vedtaket har virkningsdato "14.12.2022"
    Og vedtaket har dagsats på 588 kroner
    Og vedtaket har stønadsperiode på 52 uker
    Og vedtaket har vanlig arbeidstid per dag på 8 timer
    Og vedtaket har behandlingId lik "7E7A891C-E8E2-4641-A213-83E3A7841A57"