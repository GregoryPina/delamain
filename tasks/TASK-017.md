# TASK-017 — interrupção prioritária e respostas em texto

Status: PLANEJADA, NÃO DESPACHADA. Depende de TASK-016. BASE COMMIT a definir no despacho; aplicar [protocolo](NEXT_TASKS.md).

## Objetivo e semântica

Separar três intenções: parar fala atual, cancelar interação em andamento e desativar respostas faladas. Não usar volume zero do aparelho para simular silêncio; outras músicas/apps não devem ser alterados.

| Entrada completa proposta | Efeito |
| --- | --- |
| “pare de falar”, “parar de falar” | Parar TTS; feedback visual, sem nova fala de confirmação |
| “cancelar”, “cancela” | Invalidar interação pendente, cancelar STT e TTS; não desfazer ação Android já concluída |
| “respostas em texto”, “desativar voz” | Parar TTS e suprimir próximas falas automáticas de respostas |
| “ativar voz”, “respostas por voz” | Reabilitar próximas respostas; não reproduzir respostas antigas |

Aliases são proposta fechada; aceitar prefixo Vexa/normalização existente. “Não pare de falar”, “explique a expressão pare de falar” e “cancele e abra Spotify” não executam interrupção por substring. Não fazer classificação aproximada de comandos de ação.

## Integração

Controle prioritário antes de consultar engine de ações/IA. Reusar normalização existente sem criar terceiro roteador de linguagem geral. Definir intenção tipada de controle no coordenador; não misturar parar TTS com LocalAction.MediaPrevious/volume. Botões e texto devem compartilhar o mesmo comando interno.

A escuta atual exige botão: dizer “pare de falar” enquanto o mic está desligado não pode ser prometido como interrupção hands-free. Usuário pode tocar OUVIR (já para TTS) ou PARAR. Não implementar captura simultânea/wake word como atalho.

Preferência de respostas faladas persiste no store da 014, com valor inicial ligado para preservar comportamento. Quando desativada, entrada de voz ainda pode funcionar; o resultado fica em texto. TESTAR VOZ é ação explícita de prévia e pode emitir áudio mesmo nesse modo: rotular claramente, sem alterar a preferência. Não chamar isso de “modo silencioso total”.

## Arquivos e testes

Coordenador/contratos de controles, store existente, UI normal/DEV, testes e docs. Sem APIs remotas, mídia nova ou mudanças em permissões.

Testes: stop sem active id é idempotente; resposta assíncrona atrasada não fala após cancelar; desativar durante fala; reativar não repete histórico; negações/texto extra; preferência restaurada; cancelamento não alega desfazer ação já executada; botões/texto produzem mesma transição. Regressões de comandos anteriores preservadas.

## Aceite

Uma interrupção nunca produz outra fala automática; preferência não mexe em volume global; mensagens distinguem cancelamento de reversão. Proprietário testa com TTS/escuta/texto e reinício. Handoff documenta precedência, aliases exatos, persistência e diferença entre resposta automática e prévia explícita.
