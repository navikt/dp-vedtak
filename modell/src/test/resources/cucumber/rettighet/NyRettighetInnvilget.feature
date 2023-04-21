# language: no
Egenskap: Ny rettighet innvilget


  Scenario: Innvilget ordinære dagpenger i 104 uker
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | egenandel |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 14.12.2022    | 588     | 490921   | 104            | 8                      | Ordinær           | 1764      |
    Så skal bruker ha 1 vedtak
    Og vedtaket har dagpengerettighet "Ordinær"
    Og vedtaket har virkningsdato "14.12.2022"
    Og vedtaket har dagsats på 588 kroner
    Og vedtaket har grunnlag på 490921 kroner
    Og vedtaket har stønadsperiode på 104 uker
    Og vedtaket har vanlig arbeidstid per dag på 8 timer
    Og vedtaket har behandlingId lik "7E7A891C-E8E2-4641-A213-83E3A7841A57"


  Scenario: Innvilget dagpenger under permittering i 52 uker
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet | egenandel |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 14.12.2022    | 588     | 490921   | 52             | 8                      | Permittering      | 1764      |
    Så skal bruker ha 1 vedtak
    Og vedtaket har dagpengerettighet "Permittering"
    Og vedtaket har virkningsdato "14.12.2022"
    Og vedtaket har dagsats på 588 kroner
    Og vedtaket har grunnlag på 490921 kroner
    Og vedtaket har stønadsperiode på 52 uker
    Og vedtaket har vanlig arbeidstid per dag på 8 timer
    Og vedtaket har behandlingId lik "7E7A891C-E8E2-4641-A213-83E3A7841A57"