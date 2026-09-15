# TASK-006 — abertura de aplicativo no fluxo local ativo

Status: validado pelo proprietário em aparelho (abertura do YouTube confirmada após correção de `<queries>` no manifesto).
Base: `85d249df33d5722cec74532bcee8dd6a58d2b3af`.
Branch: `codex/open-app-local`.
Testes automáticos: `testDebugUnitTest` — 32 testes, 0 falhas (após correção do `RecordingActionPort` em `aba7a35`). Validação manual: relato “funcionou” para YouTube.

## Resumo

`LocalAction` evoluiu para `sealed interface` com `VolumeUp`, `VolumeDown` e `OpenApp(packageName, displayName)`. O motor local reconhece frases fechadas de abertura de apps e executa via porta tipada. `AndroidLaunchAppActionPort` valida `getLaunchIntentForPackage` antes de `startActivity`. O painel DEV injeta `CompositeLocalActionPort` (volume + apps). Roteador e executor remotos permanecem inativos.

## Catálogo positivo (após normalização)

Verbos: `abrir`, `abra`, `abre`. Cada app aceita com e sem artigo `o`:

| App | packageName | Exemplos |
| --- | --- | --- |
| YouTube | `com.google.android.youtube` | `abra o youtube`, `abrir youtube` |
| Chrome | `com.android.chrome` | `abra o chrome`, `abrir chrome` |
| Google Maps | `com.google.android.apps.maps` | `abrir google maps`, `abra maps` |
| Spotify | `com.spotify.music` | `abra o spotify`, `abrir spotify` |
| WhatsApp | `com.whatsapp` | `abrir whatsapp`, `abra o whatsapp` |

Prefixo opcional `vexa` suportado (`Vexa, abra o spotify`).

## Resultados tipados

- `Launched` → "Abrindo {nome}."
- `NotInstalled` → "{nome} não está instalado."
- `Unavailable` → "Abertura de aplicativos indisponível."
- `Failure` → "Não consegui abrir o aplicativo."

## Arquivos alterados

- `domain/LocalActionPort.kt`
- `domain/LocalIntent.kt`
- `domain/LocalCommandEngine.kt`
- `integration/audio/AndroidMediaVolumeActionPort.kt`
- `integration/apps/AndroidLaunchAppActionPort.kt` (novo)
- `integration/CompositeLocalActionPort.kt` (novo)
- `ui/DebugCommandPanel.kt` (debug)
- `domain/LocalCommandEngineTest.kt`

## Riscos

- Chrome pode estar ausente em alguns aparelhos (variantes de navegador).
- Pacotes de apps podem variar por região/fabricante; allowlist fixa por enquanto.
- `startActivity` a partir do `applicationContext` requer `FLAG_ACTIVITY_NEW_TASK`.

## Correção — visibilidade de pacotes (Android 11+)

Com `targetSdk` 35, `PackageManager.getLaunchIntentForPackage` retorna `null` para apps não declarados em `<queries>`, mesmo instalados. Foi adicionado bloco `<queries>` no manifesto com os cinco pacotes da allowlist. Sem isso, o adapter reportava `NotInstalled` incorretamente.

## Roteiro para o proprietário

```powershell
cd C:\Users\Gregory\StudioProjects\delamain
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleRelease
```

No painel DEV:

1. `Vexa, abra o YouTube` (instalado) → app abre, resposta confirma.
2. `abra o spotify` sem app instalado → mensagem de não instalado, sem crash.
3. `não abrir whatsapp` → não reconhecido.
4. `aumente o volume` e `que horas são?` → continuam funcionando.
