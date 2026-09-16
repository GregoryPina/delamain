# TASK-020 — contrato de conversa com provider falso

Status: PLANEJADA, NÃO DESPACHADA. Depende de TASK-015/017/018/019 integradas. BASE COMMIT a definir; aplicar [protocolo](NEXT_TASKS.md), arquitetura, personalidade e decisões.

## Objetivo

Preparar conversa aberta sem rede, credenciais ou custo. Um provider falso permite provar cancelamento, prioridade dos comandos locais e comportamento da UI. Não anunciar “IA conectada” nem apresentar texto fake como resposta de um serviço real.

## Ativação e roteamento

Proposta inicial: ação explícita “Perguntar à IA” envia texto revisável pelo usuário, separada de ENVIAR/OUVIR para comandos. Unknown comum continua local. Não enviar automaticamente pergunta fora do catálogo nem tentar rede quando uma ação conhecida falhar.

Se quiser reaproveitar entrada de voz, só o resultado textual final de uma sessão explicitamente destinada à conversa pode compor o pedido; não enviar áudio. UI identifica o destino antes de capturar/enviar. Nesta tarefa todo destino é fake claramente rotulado no DEV; controles de IA real continuam desabilitados.

## Contrato mínimo proposto

Pedido: requestId, texto, configuração de personalidade necessária e contexto temporário limitado. Resposta: texto ou erro tipado. Cancelamento deve invalidar ID e cancelar trabalho subjacente quando possível. Erros: indisponível, timeout, cancelado, autenticação/limite futuros e falha; não expor stack trace ao usuário.

Uma solicitação por vez, sem fila ilimitada. Novo envio cancela/invalida anterior. Coordenador mantém controle local de PARAR/CANCELAR independentemente do provider. Não introduzir streaming inicialmente; resposta completa é mais simples de validar. Limites propostos: entrada até 2.000 caracteres, saída exibida até 4.000, timeout 20s; revalidar no despacho. Truncamento/limite deve ser explicado, não cortar comando e executá-lo como outro.

Fake configurável: sucesso imediato/tardio, erro, timeout, resposta depois de cancelamento e texto vazio. Relógio/scheduler injetável. Não usar sleeps em testes nem adicionar cliente HTTP nesta tarefa.

## Fronteira de ações

Provider retorna somente texto. Nada de tool calling, JSON executável, shell, intents livres ou passagem da resposta pelo LocalCommandEngine. Mesmo se o texto contiver “abra Spotify”, ele não deve executar. Esse isolamento precisa de teste com ActionPort registrando zero chamadas.

## Testes e aceite

Comando local e Unknown geram zero chamadas ao provider. Envio explícito gera uma. Cancelar, sair, novo pedido e timeout impedem resposta/TTS antigos. Erro preserva comandos locais e não cria retry automático. Respostas falsas aparecem identificadas como simulação; modo mute da 017 suprime TTS automático. Personalidade é dado delimitado; nome configurado não vira instrução privilegiada.

Arquivos: interface/provider fake, coordenador, UI DEV de simulação, testes e docs. Sem INTERNET, SDK de fornecedor ou armazenamento de histórico. Handoff define schema, limites, cancelamento e ponto exato de substituição por adapter real na 021.
