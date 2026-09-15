# TASK-004 — roteador remoto com correspondência integral

Status: implementado, aguardando teste do proprietário.
Base: `d3e341a9d684f1fd2fb6d9490c8c6d554fbf502c`.

## Implementação

`CommandRouter` continua fora do fluxo ativo e agora reconhece somente frases completas após normalização de caixa, acentos, espaços e pontuação terminal. Foram removidos `contains()` e `Regex.find()` do reconhecimento de mídia, hora, abertura de apps e volume percentual.

O catálogo remoto preservado inclui:

- volume incremental: `aumentar volume`, `aumenta o volume`, `volume mais`, `mais volume`, `diminuir volume`, `diminui o volume`, `volume menos`, `menos volume`;
- mídia: `pausar musica`, `pausa musica`, `pausar`, `pause`, `play pause`, `proxima musica`, `proxima faixa`, `proxima`, `next`, `next song`, `musica anterior`, `faixa anterior`, `anterior`, `previous`, `previous song`;
- hora: `que horas`, `que horas sao`, `horas sao`, `hora`, `what time is it`;
- apps: verbos `abrir`, `abra`, `open`, `abre` seguidos exatamente por `youtube`, `chrome`, `google maps`, `maps`, `spotify` ou `whatsapp`;
- percentual: frase inteira com `volume` ou `som`, preposição opcional `para`/`em`/`de`, inteiro de 0 a 100 e `%`/`por cento` opcional.

Valores fora de 0..100, decimais, negativos, texto antes/depois, negações, pedidos compostos e múltiplos destinos retornam `Unknown`.

## Testes adicionados

`CommandRouterTest` cobre o catálogo positivo, normalização de caixa/acentos/espaços/pontuação terminal, percentuais válidos, abertura exata de app e regressões para negações, compostos, valores inválidos e texto extra especificados na TASK.

Nenhum teste foi executado por este executor. O proprietário deve executar:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease
```

Resultado esperado: testes unitários verdes e builds debug/release concluídos sem conectar `CommandRouter` à UI.

## Limites preservados

- `CommandRouter` permanece inativo em relação ao painel/UI.
- `AndroidCommandExecutor` não foi alterado.
- mídia, abertura de apps e volume percentual não foram ativados.
- o problema semântico de pausa versus toggle continua registrado para uma etapa futura.

## Fechamento

Proprietário confirmou teste ok no HEAD 39d9fce03943760eb722bd31b557596653a15798. PR #1 integrado em 85d249df33d5722cec74532bcee8dd6a58d2b3af. Relato sem logs detalhados; testes não repetidos por Astra. Status atual: concluída e integrada.
