# TASK-017 — interrupção prioritária e modo mute

Status: IMPLEMENTADA em `codex/task-017-mute-controls` (base: TASK-016). Aguardando validação do proprietário. Handoff: `docs/handoffs/TASK-017.md`.

Decisão do proprietário (2026-09-16): **não** haverá “modo somente texto” como produto. O campo TEXTO da 016 é apenas **entrada alternativa**, não um modo de operação. O que faz sentido é **modo mute**: VEXA continua ouvindo, executando comandos e mostrando respostas na tela, mas **não fala** automaticamente.

## Objetivo e semântica

Separar quatro intenções distintas:

| Conceito | Efeito |
| --- | --- |
| **PARAR** (botão ou “pare de falar”) | Interrompe a fala **atual**; não altera preferência |
| **CANCELAR** (“cancelar”, “cancela”) | Invalida interação pendente (STT/TTS); não desfaz ação Android já concluída |
| **Modo mute** | Suprime TTS automático das respostas; texto na tela permanece |
| **Ativar voz** | Reabilita TTS automático; não repete histórico |

Modo mute **não** é volume zero do aparelho, **não** desliga microfone e **não** remove a entrada TEXTO. Outras apps/músicas não são afetadas.

### Frases propostas (catálogo fechado)

| Entrada completa proposta | Efeito |
| --- | --- |
| “pare de falar”, “parar de falar” | Parar TTS atual; feedback visual, sem nova fala de confirmação |
| “cancelar”, “cancela” | Invalidar interação pendente |
| “modo mute”, “silenciar voz”, “desativar voz” | Ativar mute (parar fala atual + suprimir próximas) |
| “ativar voz”, “sair do mute”, “respostas por voz” | Desativar mute |

Aliases são proposta fechada; aceitar prefixo Vexa/normalização existente. “Não pare de falar”, “explique a expressão pare de falar” e “cancele e abra Spotify” não executam interrupção por substring.

## Integração

Controle prioritário antes de consultar engine de ações/IA. Reusar normalização existente. Definir intenção tipada de controle no coordenador/sessão; não misturar parar TTS com `LocalAction.MediaPrevious`/volume. Botões e voz devem compartilhar o mesmo comando interno.

- Toggle **mute** na UI principal (além de frases), persistido no store da 014 junto com preferência de voz.
- Valor inicial: voz ligada (comportamento atual).
- Com mute ativo: OUVIR e TEXTO funcionam; resposta aparece na tela; TTS automático não dispara.
- **TESTAR VOZ** (DEV) e prévia explícita podem falar mesmo em mute, rotuladas como tal, sem alterar a preferência.

A escuta atual exige botão: “pare de falar” com microfone desligado não é interrupção hands-free (usuário usa PARAR ou OUVIR, que já para TTS antes de escutar).

## Arquivos e testes

Coordenador/sessão, store existente, UI normal/DEV, testes e docs. Sem APIs remotas, mídia nova ou mudanças em permissões.

Testes: stop idempotente; resposta assíncrona atrasada não fala após cancelar/mute; mute durante fala; reativar voz não repete histórico; negações; preferência restaurada; botões/voz/frases produzem mesma transição.

## Aceite

Interrupção nunca produz outra fala automática de confirmação. Mute não altera volume global. Mensagens distinguem cancelamento de reversão e mute de “só digitar”. Proprietário testa com TTS/escuta/TEXTO e reinício. Handoff documenta precedência, aliases, persistência e diferença entre resposta automática e prévia explícita.
