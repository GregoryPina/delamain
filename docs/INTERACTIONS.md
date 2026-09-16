# Roteamento, reflexos e conversa

Contrato alvo para V0.2–V0.4. Hoje o painel DEV recebe texto e responde com reflexos, hora, volume, apps permitidos, próxima/anterior e bateria; TTS lê a resposta. Unknown permanece local. TASK-011 integrada: escuta por botão envia apenas resultado final ao mesmo motor. Esclarecimento conversacional e IA são futuros. Cancelamento por botão e frases de controle prioritário estão integrados pela TASK-017. A sequência abaixo descreve o contrato alvo, não funcionalidades todas concluídas.

## Caminho de uma entrada

1. Receber texto final reconhecido, entrada manual de desenvolvimento ou evento tipado autorizado.
2. Priorizar interrupção/cancelamento de fala ou solicitação pendente.
3. Normalizar texto e procurar intenção local com regras explícitas.
4. Se houver ambiguidade, pedir esclarecimento local; não executar nem enviar à IA automaticamente.
5. Validar parâmetros, permissão e disponibilidade da ação conhecida.
6. Executar a ação ou obter o dado; selecionar frase de personalidade compatível com o resultado.
7. Se for conversa aberta, consultar o provider apenas se a integração estiver habilitada para esse uso.
8. Publicar resposta e estado visual; em falha, usar resposta local e retornar a IDLE.

Um comando conhecido que falha não deve virar tentativa pela IA. “Não apagar” não pode casar com “apagar”; correspondência por palavra solta é insuficiente. Nomes de aplicativos e destinos precisam de resolução inequívoca.

## Catálogo inicial proposto

| Gatilho | Tipo | Fonte/ação | Etapa |
| --- | --- | --- | --- |
| “Está aí?”, “bom dia”, “obrigado” | Reflexo social | Banco local de frases | V0.2 |
| “Que horas são?” | Consulta local | Relógio do aparelho | V0.2 |
| “Aumente o volume” | Comando local | Ação permitida e resultado do adapter | V0.2 |
| “Próxima música” / “música anterior” | Comando local | Despacho de tecla de mídia | Implementado TASK-007 |
| “Abra Spotify” | Comando local | Lista permitida de apps | Implementado TASK-006 |
| “Bateria” | Consulta local | Nível e carga | Implementado TASK-008 |
| “Pause a música” | Comando local | Semântica de pausa a definir | Futuro |
| “Pare de falar” | Controle prioritário | Cancelar TTS e resposta pendente | TASK-017 |
| “Modo mute” / “ativar voz” | Preferência local | Suprimir TTS automático; respostas permanecem na tela | TASK-017 |
| “Modo silencioso” (espontâneos) | Preferência local | Suprimir comentários espontâneos futuros | V0.5+ |
| “Me explique…” | Conversa aberta | API de IA habilitada | V0.4 |
| Início de sessão de viagem | Evento | Saudação local opcional, uma vez por sessão | V0.5 |

Saudações, presença, agradecimento e hora foram implementados na V0.2-A; volume de mídia e formas “tá aí?”, “vc tá aí?” e “cê tá aí?” na V0.2-B. Prefixo “Vexa”, caixa, acentos e pontuação são normalizados. Apenas frases cadastradas são aceitas, sem reconhecimento aproximado para ações. “Delamain” não é mais gatilho de chamada; permanece apenas em identificadores técnicos e registros históricos. Apps, próxima/anterior e bateria foram integrados pelas TASK-006–008 através das portas locais. O executor remoto permanece inativo. As demais capacidades são planejadas.

## Modelo mínimo

- `Input`: texto ou evento, origem e identificador da solicitação.
- `Intent`: intenção reconhecida, parâmetros e necessidade de esclarecimento.
- `ActionResult`: sucesso confirmado, solicitação encaminhada, indisponível, permissão negada ou falha.
- `Response`: texto, origem local/IA, prioridade e estado visual.
- `Phrase`: identificador, intenção/evento, variantes, condições e intervalo mínimo para eventos automáticos.

Começar com catálogo em Kotlin e componentes separados por responsabilidade dentro do app. Não criar vários módulos Gradle, banco ou um editor de regras nesta etapa.

## Concorrência e latência

Uma fala por vez. Cancelamento invalida a solicitação em andamento, interrompe o TTS e descarta respostas atrasadas da API. Não reproduzir uma resposta antiga depois de um novo pedido. Definir timeout configurável para IA e evitar repetição automática de ações com efeitos.

Meta inicial a medir: roteamento e escolha de frase local em até 100 ms no percentil 95, depois de receber o texto final, no aparelho alvo. Essa meta não inclui captura, STT, execução Android ou início de TTS, que terão medições separadas. Não é desempenho já comprovado.

## Voz e privacidade

Roteamento local não garante reconhecimento de voz offline. Selecionar e testar o caminho de STT/TTS sem rede; não trocar silenciosamente para reconhecimento remoto. Na primeira integração de IA, preferir enviar texto e contexto mínimo. Áudio remoto só com ação explícita para um recurso que o necessite. Acordar o assistente não autoriza envio contínuo de áudio.

## Ações propostas pela IA

O provider devolve texto e, quando suportado, uma proposta estruturada. O domínio aplica a mesma lista de ações permitidas dos comandos locais, valida parâmetros e pede confirmação para ações que exigirem isso. Texto livre nunca vira código, comando de sistema ou execução irrestrita. Não executar propostas incompletas ou recebidas depois de cancelamento.

## Cenários de aceite futuros

- Saudação e hora respondem sem rede e sem chamar o provider.
- Variação mantém o significado e evita repetição imediata quando possível.
- Frase de sucesso só aparece após resultado que a sustente.
- Entrada ambígua, negada ou indisponível não executa nem chama IA como fallback.
- API desabilitada, timeout ou resposta inválida preservam comandos locais e retorno a IDLE.
- Cancelamento impede fala tardia e ação ainda não iniciada; não promete desfazer ação concluída.
- Evento repetido respeita intervalo, limite de sessão e modo silencioso.
- Ação fora da lista permitida é recusada, mesmo se proposta pela IA.
