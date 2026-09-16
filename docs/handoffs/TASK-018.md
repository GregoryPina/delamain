# TASK-018 — handoff

Status: **implementado, aguardando teste do proprietário**.

Branch: `codex/task-018-personality` (base: `codex/task-017-mute-controls`).

## Entregue

### Preferências locais

- **Nome de tratamento** opcional (vazio por padrão; não inventa Gregory em todo aparelho).
- **Tom**: `WARM` (atual) e `DIRECT` (mais curto).
- Persistência no mesmo DataStore (`vexa_preferences`): `personality_display_name`, `personality_tone`.
- Reset de personalidade (**PADRÃO**) não apaga voz nem mute.

### Motor e frases

- `LocalPhraseBank` centraliza variantes por tom; fatos/placeholders permanecem tipados.
- `LocalCommandEngine.applyPersonality()` atualiza config **sem** recriar o motor nem zerar rotação de variantes.
- `PersonalityPreview` gera exemplo local (presença) **sem** chamar `ActionPort`.

### UI

- Botão **TOM** na tela principal → painel compacto: nome, chips de tom, SALVAR, PRÉVIA, PADRÃO.

## Defaults adotados

| Campo | Default |
| --- | --- |
| Nome | vazio (sem tratamento) |
| Tom | Atual (`WARM`) |
| Limite de nome | 40 caracteres (truncado) |

## Exemplos antes/depois (tom Direto, nome “Ana”)

| Situação | Atual (WARM) | Direto |
| --- | --- | --- |
| “Está aí?” | “Sempre a postos, Ana.” | “Aqui, Ana.” |
| “Vexa” | “À disposição.” | “Sim.” |
| Próxima música (despacho) | “Solicitei a próxima faixa.” | “Próxima faixa solicitada.” |
| Bateria 72% | “Bateria em 72%.” | “Bateria em 72%.” (fato igual) |

## Testes

`PersonalityConfigTest`, `PersonalityPreviewTest`, `LocalCommandEnginePersonalityTest`, store/coordinator personality.

Executados: `testDebugUnitTest` (108 testes), `assembleDebug`, `assembleRelease` — verdes.

## Roteiro de validação manual

1. Abrir **TOM** → salvar nome → “Está aí?” deve usar o nome.
2. Alternar para **Direto** → respostas mais curtas; comandos (volume, apps) inalterados.
3. **PRÉVIA** → mostra exemplo sem executar ação.
4. **PADRÃO** → nome vazio, tom Atual; voz e mute preservados.
5. Reiniciar app → preferências restauradas.

## Build

```powershell
$env:JAVA_HOME='C:\Users\Gregory\.jdks\jbr-21.0.11'
.\gradlew.bat assembleDebug
```
