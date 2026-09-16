# TASK-021 — primeiro provider real de IA

Status: PLANEJADA, CONDICIONADA A DECISÕES; NÃO DESPACHADA. Depende de TASK-020 aceita. BASE COMMIT somente após decisões abaixo. Aplicar [protocolo](NEXT_TASKS.md).

## Decisões necessárias antes de código de rede

Coordenador consolida com o proprietário: provider/modelo, custo máximo aceitável, origem das credenciais, acesso direto de uso pessoal ou backend intermediário, política de contexto/retensão e onde a configuração será fornecida. Não escolher serviço pago ou criar infraestrutura/conta por inferência. Pesquisar documentação/preços oficiais atuais no momento dessa escolha; não congelar valores/modelos nesta TASK.

Se faltar decisão, preparar comparação curta e contrato de integração; status BLOQUEADA_POR_DECISÃO, sem implementar provider imaginário. Reutilizar credencial existente só se explicitamente destinada a esse uso. Nunca ler/exibir segredos para “descobrir qual chave usar”.

## Caminho de implementação após decisão

Um adapter implementa contrato da 020. Adicionar apenas cliente HTTP/dependência necessária e justificada, respeitando versões Android/Gradle. Sem migrar projeto inteiro para SDK proprietário. Endpoint/modelo são configuração validada; HTTPS obrigatório, sem desabilitar verificação de certificado.

Nenhum segredo no código, recursos, BuildConfig, arquivo versionado ou descrição de PR. Chave dentro de APK não é segredo protegido. Se acesso pessoal direto for escolhido, explicitar armazenamento/ameaças e não prometer proteção absoluta; Keystore protege em repouso, não elimina exposição em dispositivo comprometido. Não criar backend como trabalho oculto desta tarefa.

## Comportamento

Ação explícita envia texto/contexto mínimo da 020; controle ligado/desligado, feedback de conectando/erro e botão cancelar. Nada de fallback de Unknown, áudio, GPS, contatos ou lista de apps para enriquecer pedido. Contexto só em memória inicialmente; limite de turnos/tamanho e “limpar conversa”. Cancelar/timeout não equivale a estorno de custo já incorrido.

Mapear autenticação, quota/rate limit, rede, timeout, resposta vazia/inválida e erro do serviço. Sem retry automático nesta primeira integração; nova tentativa exige usuário e novo ID. Limitar entrada/saída conforme contrato e parâmetros suportados; registrar contagem de uso quando fornecida sem registrar conteúdo/segredo. Não afirmar teto monetário estrito com base só em limite de caracteres.

Não habilitar ações de IA: saída continua texto, nunca executada no engine local. Voz usa o TTS existente e sua política de silêncio/cancelamento. App funciona localmente com provider desligado/sem rede.

## Testes e aceite

Fixtures HTTP sanitizadas/fakes: códigos de erro, body inválido, desconexão, cancelamento, resposta após timeout, limite de tamanho e zero vazamento em logs. Testes automatizados não fazem chamada paga. Proprietário faz um smoke test real explicitamente, dentro do limite acordado, e relata HEAD/resultado sem enviar chave.

Manifesto INTERNET somente quando adapter real existir. Revisar dados de backup e configuração para evitar cópia de credenciais. Entrega inclui instruções de configurar/remover acesso, limites conhecidos, fonte oficial consultada, teste real pendente/feito e modo de desligar. Sem publicação do app, compra ou implantação externa implícita.
