# Evolução scriptada antes da IA

Revisão em 2026-09-17. Incrementos pequenos, sem rede ou novas permissões. Não renumerar TASK-020–022 existentes.

## Implementado nesta rodada: ajuda local

“ajuda”, “comandos”, “o que você sabe fazer?”, “o que você pode fazer?”, “quais comandos”, “quais são os comandos”, prefixo Vexa opcional. Três variantes por tom; somente exemplos já suportados. Entrada negada/composta continua Unknown. Texto de resposta nunca passa novamente pelo motor. Teste criado, execução pendente.

## Recorte seguinte recomendado: identidade e repertório social

Após validar o conjunto atual, ampliar frases explícitas para “quem é você?”, despedida e brincadeira solicitada. Arquivos: LocalIntent, LocalCommandEngine, LocalPhraseBank e testes. Sem fala espontânea, sensores inventados ou afirmação de entender conversa livre. Três variantes por intenção/tom; respeitar mute; zero chamadas a portas de ação. Não adicionar aliases por substring. Aprovar catálogo de frases no despacho e fixar base publicada.

## Recorte posterior: repetir a última resposta

Repetir apenas texto da resposta anterior, sem reenviar comando ao motor (repetir “abri Spotify” nunca abre app novamente). Memória só da sessão, apagada no descarte; excluir mensagens transitórias de erro e dados sensíveis futuros. Definir se pedido explícito fala em mute antes de implementar. Testar zero ações e ausência de resposta anterior.

## Medir antes de ampliar reconhecimento

Coletar voluntariamente exemplos de frases que falharam durante os testes. Adicionar aliases explícitos ao catálogo com casos negativos de negação/compostos. Sem logging persistente de transcrições, fuzzy matching de ações ou fallback IA automático.

## Adiar

Play/pause exige contrato de resultado; volume percentual exige validação tipada. Saudação espontânea e sessão de viagem precisam opt-in/limite/cancelamento. Navegação e trânsito exigem fontes reais. Não são bloqueadores da IA e não devem ampliar a rodada atual.

Ordem sugerida: estabilizar → testar → repertório social opcional → repetir opcional → TASK-020 (simulação sem rede) → TASK-021 (provider real após decisão). Ajuda e catálogo social dão personalidade sem custo por chamada.
