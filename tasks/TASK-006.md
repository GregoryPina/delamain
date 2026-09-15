# TASK-006 — abertura de aplicativo no fluxo local ativo

## Objective

Adicionar a primeira ação local parametrizada além do volume: abrir um aplicativo permitido via `LocalCommandEngine`, com resultado tipado, validação de disponibilidade e falha explícita — sem conectar o roteador remoto à UI.

## Current behavior

O fluxo ativo reconhece reflexos, hora e volume. `LocalActionPort` aceita apenas `VOLUME_UP` e `VOLUME_DOWN`. O painel DEV injeta `AndroidMediaVolumeActionPort`.

Existe código remoto em `domain/command/CommandRouter` e `platform/AndroidCommandExecutor` com catálogo e `openApp()`, mas permanece **inativo** e fora do fluxo canônico. A revisão TASK-003 registrou que o executor remoto não oferece resultados tipados adequados para abertura de apps.

## Desired behavior

Frases cadastradas como `abra o youtube`, `abrir spotify` ou `Vexa, abra o maps` (após normalização) reconhecem `LocalIntent.OPEN_APP`, executam `LocalAction.OpenApp(packageName, displayName)` e retornam resposta coerente com o resultado observado:

- app instalado e intent de lançamento disponível → confirma abertura;
- pacote ausente ou sem intent de lançamento → resultado `NotInstalled`, sem alegar sucesso;
- porta ausente → `Unavailable`;
- erro de plataforma ao iniciar a activity → `Failure`.

Negações, pedidos compostos, apps fora da allowlist e texto extra continuam retornando `Unknown` sem executar ação.

## Relevant files/modules

**Domínio (alterar):**

- `app/src/main/java/com/gregorypina/delamain/domain/LocalActionPort.kt`
- `app/src/main/java/com/gregorypina/delamain/domain/LocalIntent.kt`
- `app/src/main/java/com/gregorypina/delamain/domain/LocalCommandEngine.kt`
- `app/src/test/java/com/gregorypina/delamain/domain/LocalCommandEngineTest.kt`

**Integração Android (criar/alterar):**

- `app/src/main/java/com/gregorypina/delamain/integration/apps/AndroidLaunchAppActionPort.kt` (novo)
- `app/src/main/java/com/gregorypina/delamain/integration/CompositeLocalActionPort.kt` (novo, ou equivalente que delegue por tipo de ação)
- `app/src/debug/java/com/gregorypina/delamain/ui/DebugCommandPanel.kt` (injetar porta composta)

**Documentação (criar pelo executor):**

- `docs/handoffs/TASK-006.md`

**Não alterar:**

- `domain/command/CommandRouter.kt`
- `platform/AndroidCommandExecutor.kt`
- `ui/DelamainApp.kt` (layout/efeitos)
- identificadores técnicos, manifesto além do estritamente necessário, assets, Gradle

## Requirements

1. Ler `AGENTS.md`, `docs/DECISIONS.md`, `docs/ARCHITECTURE.md`, `docs/INTERACTIONS.md` e `docs/handoffs/TASK-003.md` na baseline do despacho.
2. Evoluir `LocalAction` de enum simples para modelo que suporte ações parametrizadas (ex.: `sealed interface` com `VolumeUp`, `VolumeDown`, `OpenApp(packageName, displayName)`). Preservar compatibilidade dos testes e adapters de volume existentes.
3. Adicionar variantes tipadas em `LocalActionResult` para abertura de app (mínimo: `Launched`, `NotInstalled`, reutilizando `Unavailable`, `Failure` quando aplicável). Não confirmar abertura sem `getLaunchIntentForPackage` retornar intent válido.
4. Catálogo fechado em `LocalCommandEngine`, com normalização existente (acentos, caixa, pontuação, prefixo opcional `vexa`). Correspondência por **frase inteira** após normalização.
5. Allowlist inicial de apps (referência do catálogo remoto, adaptada ao estilo local):

| Frase normalizada (exemplos) | packageName | displayName |
| --- | --- | --- |
| `abrir youtube`, `abra youtube`, `abre youtube`, `abrir o youtube`, … | `com.google.android.youtube` | YouTube |
| `abrir chrome`, `abra chrome`, … | `com.android.chrome` | Chrome |
| `abrir maps`, `abra maps`, `abrir google maps`, … | `com.google.android.apps.maps` | Google Maps |
| `abrir spotify`, `abra spotify`, … | `com.spotify.music` | Spotify |
| `abrir whatsapp`, `abra whatsapp`, … | `com.whatsapp` | WhatsApp |

   Verbos aceitos: `abrir`, `abra`, `abre`. Incluir formas com artigo `o/a` quando fizer sentido (`abrir o youtube`). Documentar o catálogo positivo no handoff.

6. `AndroidLaunchAppActionPort` deve usar `applicationContext`, `PackageManager.getLaunchIntentForPackage`, `Intent.FLAG_ACTIVITY_NEW_TASK` e capturar falhas de `startActivity`. Não adicionar permissões novas sem justificativa documentada.
7. `DebugCommandPanel` injeta porta composta (volume + apps). Variante `release` permanece inalterada.
8. Respostas de personalidade coerentes com cada resultado (ex.: “Abrindo YouTube.”, “YouTube não está instalado.”, “Não consegui abrir o aplicativo.”). Não inventar sucesso.

## Constraints

- Não conectar `CommandRouter` ou `AndroidCommandExecutor` ao fluxo ativo.
- Não implementar mídia, volume percentual, voz, STT, TTS ou IA.
- Não usar matching parcial, `contains` ou reconhecimento aproximado de nomes de apps.
- Não expandir a allowlist além dos cinco apps acima sem aprovação do Tech Lead.
- Não reescrever `TECH_LEAD_HANDOFF.md`, `docs/PROJECT_STATUS.md` ou outros documentos centrais do coordenador.
- Preservar cobertura existente de volume, VEXA, negações e compostos.

## Dependencies

- Wave 2 concluída e baseline `85d249df33d5722cec74532bcee8dd6a58d2b3af` publicada.
- Nenhuma outra TASK em andamento.

## Acceptance criteria

- [ ] Frases allowlisted abrem o app correspondente quando instalado (validação no aparelho pelo proprietário).
- [ ] App ausente retorna `NotInstalled` e mensagem adequada, sem `startActivity`.
- [ ] `não abrir spotify`, `abrir spotify e youtube`, `abrir netflix`, `abrir` sozinho → `Unknown`.
- [ ] Prefixo `Vexa` funciona (`Vexa, abra o spotify`).
- [ ] Testes unitários cobrem reconhecimento, negação, composto, allowlist positiva e mapeamento de resultados tipados (com porta fake).
- [ ] Volume e reflexos existentes continuam passando nos testes.
- [ ] Roteador remoto e executor remoto permanecem inativos e intocados.

## Validation

O proprietário executará `assembleDebug`, `testDebugUnitTest` e `assembleRelease`. No aparelho, testará pelo painel DEV:

1. `Vexa, abra o YouTube` (instalado) → app abre e resposta confirma.
2. `abra o spotify` com app ausente → mensagem de não instalado, sem crash.
3. `não abrir whatsapp` → não reconhecido.
4. Comandos de volume e hora continuam funcionando.

Registrar “aguardando teste do proprietário” até confirmação. Não alegar testes não executados.

## Expected output

PR em branch própria ou unified diff aplicável à baseline. Incluir:

- resumo das mudanças;
- lista de arquivos;
- catálogo de frases documentado;
- riscos (ex.: Chrome ausente em alguns aparelhos, pacotes alternativos);
- roteiro de validação para o proprietário.

Não fazer push em `main` nem merge.
