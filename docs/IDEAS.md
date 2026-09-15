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

O proprietário validou funcionalmente a interface V0.1 em 15 de setembro de 2026. A **V0.2-A** já possui núcleo local, testes e entrada de debug compilados. O próximo passo é validar o painel no aparelho e então avançar para ações locais permitidas antes de adicionar microfone ou custos de API.

O wrapper foi restaurado e os builds debug/release passaram pelos agentes. O relato original da V0.1 não identifica o aparelho utilizado; a validação manual do novo painel está registrada como pendência em PROJECT_STATUS.md.

## Decisões em aberto

- Tom final: formalidade, uso do nome e intensidade de humor, partindo de [PERSONALITY.md](PERSONALITY.md).
- Provider de IA e orçamento, avaliados na V0.4.
- Voz e disponibilidade offline no Redmi 13, avaliadas na V0.3.
- Retenção de histórico, desativada por padrão até existir controle explícito.
