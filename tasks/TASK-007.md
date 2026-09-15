# TASK-007 — faixa anterior e próxima no fluxo local ativo

## Objective

Adicionar controle local de faixa anterior e próxima via `LocalCommandEngine`, com resultados tipados e adapter Android — sem conectar o roteador remoto e **sem** play/pause como toggle nesta etapa (ver TASK-003).

## Current behavior

Volume e abertura de apps funcionam no fluxo local via `CompositeLocalActionPort`. O código remoto em `CommandRouter`/`AndroidCommandExecutor` reconhece mídia, mas permanece inativo e o executor remoto não confirma efeitos de forma robusta.

## Desired behavior

Frases fechadas em português (ex.: `próxima música`, `faixa anterior`) reconhecem `LocalIntent.MEDIA_NEXT` / `MEDIA_PREVIOUS`, executam ações tipadas e retornam resposta coerente com o resultado observado (ex.: `Dispatched`, `Unavailable`, `Failure`). Não alegar troca de faixa sem evidência mínima aceitável pelo contrato do adapter.

## Relevant files

- `domain/LocalActionPort.kt`, `LocalIntent.kt`, `LocalCommandEngine.kt`
- `integration/audio/` — novo adapter de teclas de mídia ou extensão do composite
- `integration/CompositeLocalActionPort.kt`
- `ui/DebugCommandPanel.kt` (debug)
- `domain/LocalCommandEngineTest.kt`
- `docs/handoffs/TASK-007.md`

Não alterar `CommandRouter`, `AndroidCommandExecutor` nem ativar play/pause.

## Requirements

1. Evoluir `LocalAction` com `MediaNext` e `MediaPrevious` (ou equivalente).
2. Catálogo fechado PT, frase inteira após normalização; prefixo `vexa` opcional.
3. Referência de frases do remoto, adaptadas ao estilo local (sem inglês solto, salvo se explicitamente aprovado).
4. Adapter usa `AudioManager.dispatchMediaKeyEvent` com `KEYCODE_MEDIA_NEXT` / `KEYCODE_MEDIA_PREVIOUS`; capturar falhas.
5. Resultados tipados distintos de volume e de apps; respostas sem sucesso falso.
6. Testes unitários: reconhecimento, negação, composto, resultados tipados com porta fake.
7. Preservar volume, apps e reflexos existentes.

## Constraints

- Não implementar play/pause/toggle nesta TASK.
- Não volume percentual, voz, STT, TTS ou IA.
- Não conectar contribuição remota à UI.

## Dependencies

- TASK-006 integrada em `main` (baseline publicada após merge do PR).

## Acceptance criteria

- [ ] Frases allowlisted disparam tecla de mídia correta no aparelho (validação pelo proprietário).
- [ ] Negações e compostos retornam `Unknown`.
- [ ] Testes unitários verdes; volume/apps/reflexos intactos.
- [ ] Roteador remoto intocado.

## Validation

Proprietário: `testDebugUnitTest`, `assembleDebug`, testar no painel DEV com app de mídia ativo (ex.: Spotify/YouTube Music em reprodução).

## Expected output

PR ou unified diff contra baseline pós-merge TASK-006. Não push em `main` nem merge.
