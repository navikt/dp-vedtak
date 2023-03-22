# language: no
Egenskap: Ny rettighet innvilget


  Scenario: Innvilget ordinære dagpenger i 104 uker
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 14.12.2022    | 588     | 490921   | 104            | 8                      | Ordinær           |
    Så skal bruker ha 1 vedtak
    Og dagpengerettighet er "Ordinær"
    Og vedtaket har virkningsdato "14.12.2022"
    Og vedtaket har dagsats på 588, grunnlag 490921, behandlingId er "7E7A891C-E8E2-4641-A213-83E3A7841A57", stønadsperiode på 104 uker og vanlig arbeidstid per dag er 8 timer


  Scenario: Innvilget dagpenger under permittering i 52 uker
    Gitt en ny hendelse om innvilget søknad
      | fødselsnummer | behandlingId                         | utfall | virkningsdato | dagsats | grunnlag | stønadsperiode | vanligArbeidstidPerDag | dagpengerettighet |
      | 12345678901   | 7E7A891C-E8E2-4641-A213-83E3A7841A57 | true   | 14.12.2022    | 588     | 490921   | 52             | 8                      | Permittering      |
    Så skal bruker ha 1 vedtak
    Og dagpengerettighet er "Permittering"
    Og vedtaket har virkningsdato "14.12.2022"
    Og vedtaket har dagsats på 588, grunnlag 490921, behandlingId er "7E7A891C-E8E2-4641-A213-83E3A7841A57", stønadsperiode på 52 uker og vanlig arbeidstid per dag er 8 timer