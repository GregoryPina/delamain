# Ideias para evolução

Backlog exploratório: estas ideias não são compromisso de implementação nem ampliam a V0.1.

| Ideia | Valor para a viagem | Dependências e limites |
| --- | --- | --- |
| Ritual de partida | Saudação curta com identidade | Sessão explícita; não afirmar diagnóstico do carro |
| Humor e tratamento ajustáveis | Companion com o tom do proprietário | Preferências locais, humor discreto por padrão |
| “Só me responda quando eu chamar” | Menos interrupções | Política de fala e modo silencioso |
| Resumo de viagem a pedido | Duração e informações úteis | Sessão e dados reais; localização opcional |
| Lembrete de pausa configurado | Apoiar viagens longas | Temporizador; não alegar detectar fadiga |
| Preferências lembradas | Nome, idioma e tom consistentes | Armazenamento local com edição e exclusão |
| Conversa de estrada | Histórias, explicações e planejamento por voz | API opcional, cancelamento e respostas curtas |
| Ritual ao conectar ao carro | Facilitar início da sessão | Bluetooth e comportamento em background validados no dispositivo |

## Próximo incremento recomendado

TASK-010–012 integradas após aceite funcional, consolidação TASK-013 encerrada. Próximo recorte: persistir preferência de voz (TASK-014). O estado atual está em PROJECT_STATUS.md; [tarefas 013–022](../tasks/NEXT_TASKS.md) detalham a continuação. Ideias desta página não autorizam implementação fora dessas tarefas.

## Decisões em aberto

- Tom final: formalidade, uso do nome e intensidade de humor, partindo de [PERSONALITY.md](PERSONALITY.md).
- Provider de IA e orçamento, avaliados na V0.4.
- Voz e disponibilidade offline no Redmi 13, avaliadas na V0.3.
- Retenção de histórico, desativada por padrão até existir controle explícito.
