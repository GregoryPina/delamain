# TASK-019 — coexistência com áudio e matriz de rotas

Status: IMPLEMENTADA em `codex/task-019-audio-focus` (base: TASK-018). Aguardando validação no aparelho. Handoff: `docs/handoffs/TASK-019.md`; matriz: `docs/handoffs/TASK-019-AUDIO-MATRIX.md`.

## Problema e escopo

Fala e escuta já têm ciclo de vida, mas comportamento junto a música, chamadas e Bluetooth não foi comprovado. Tratar foco de áudio durante interações explícitas e registrar as rotas que realmente funcionam. Não prometer integração Bluetooth só porque o som sai em alto-falante externo.

## Primeiro coletar evidência

Matriz mínima: alto-falante interno; fone disponível; Bluetooth disponível; sem player; player tocando; perda de foco; app vai ao fundo; rota desconecta durante fala/escuta. Campos NÃO DISPONÍVEL/NÃO TESTADO são válidos. Não exigir compra de hardware nem simular testes físicos em relatório.

## Implementação proposta

- Pequena porta de foco de áudio, adapter Android oficial e integração no coordenador. Consultar regras atuais para targetSdk 35 e versão do aparelho antes de escolher solicitação de foco.
- Adquirir somente durante operação que precisa de foco; liberar em sucesso, falha, cancelamento, timeout e shutdown. Resposta curta não justifica sessão de áudio permanente.
- Política inicial a revisar: fala usa interrupção transitória apropriada; perda de foco cancela fala/captura ativa e não retoma automaticamente. Não pausar/retomar player alheio com toggle para imitar ducking.
- Se foco for negado, informar indisponibilidade de áudio e manter texto. Não afirmar áudio reproduzido sem callback, nem captura iniciada sem readiness.
- Mudança/desconexão de rota não dispara comando, não executa novamente e não muda silenciosamente para outra captura em background.
- Não forçar SCO/A2DP, adicionar permissões Bluetooth ou serviço em foreground só para “fazer funcionar”. Se isso for necessário, registrar nova proposta com requisito observado, não ampliar esta tarefa.

## Testes

Fake de foco: concedido/negado/perda durante fala/perda durante escuta/callback atrasado após cancelamento; cada aquisição é liberada uma vez; falha de aquisição impede áudio; cancelamento não reinicia música alheia. Proprietário confirma matriz real e efeito no player. Áudio de chamada não deve ser gravado ou enviado para análise.

## Entrega e aceite

PR com política escolhida, motivo, versões consultadas e matriz por aparelho/rota. Zero reprodução/captura persistindo por engano após perda de foco ou fechamento. Não classificar Bluetooth não testado como suportado. Manter retorno textual e comandos locais existentes. Se diagnóstico não exigir código, registrar evidência e não adicionar abstração sem função.

Fonte primária para consulta na execução: [Android — foco de áudio](https://developer.android.com/media/optimize/audio-focus). API/regra deve ser verificada na data da implementação; este documento não fixa receita de compatibilidade universal.
