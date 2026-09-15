# TASK-004 — corrigir falsos positivos no roteador remoto

## Objective / current behavior

Implementar a primeira correção da revisão TASK-003. CommandRouter usa contains/find e pode aceitar negações ou pedidos compostos. Continua fora do fluxo ativo.

## Desired behavior

Reconhecimento determinístico por frase completa. Preservar catálogo remoto legítimo e recusar texto extra, negações, múltiplos destinos ou ações compostas. Percentuais devem casar com a frase inteira e estar entre 0 e 100; não truncar valores inválidos.

## Files

- app/src/main/java/com/gregorypina/delamain/domain/command/CommandRouter.kt
- novo app/src/test/java/com/gregorypina/delamain/domain/command/CommandRouterTest.kt
- docs/handoffs/TASK-004.md

## Requirements / constraints

- Ler AGENTS.md, docs/DECISIONS.md e docs/handoffs/TASK-003.md no BASE COMMIT do despacho.
- Remover matching parcial de mídia, hora, abertura de apps e volume percentual; documentar catálogo positivo e testar equivalência entre caixa/acentos/pontuação terminal.
- Testar Unknown para “não aumenta o volume”, “não pausa a música”, “pausa a música e aumenta o volume”, “você sabe que horas são amanhã?”, “abrir spotify e depois youtube”, “volume 101”, “volume -1”, “volume 20.5” e texto antes/depois do percentual.
- Não ligar roteador remoto à UI, alterar executor Android, ativar mídia/apps/percentual ou reescrever LocalCommandEngine. Problemas do executor/toggle ficam registrados e inativos.
- Não adicionar dependências ou novos recursos. Não apagar contribuição remota.

## Acceptance / validation

Código e testes de regressão entregues, sem efeitos no fluxo ativo. O proprietário executará assembleDebug, testDebugUnitTest e assembleRelease. A entrega fica aguardando teste até o relato dele; não alegar testes não executados.

## Expected output

Obrigatório: PR em branch própria ou unified diff aplicável à baseline. Uma análise sem código não conclui esta TASK. Incluir resumo, arquivos, riscos e roteiro. Não fazer push em main nem merge.
