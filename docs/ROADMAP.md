# Roadmap

## Fase 0 — Fundação

Situação em 16 de setembro de 2026: TASK-006–009 integradas e aceitas pelo proprietário; apps, mídia próxima/anterior, bateria, variantes e TTS DEV disponíveis. TASK-010 e TASK-011 implementadas em branches dependentes, aguardando validação conjunta. Evidências em PROJECT_STATUS.md; não houve novos testes nesta auditoria.

- [x] Criar repositório
- [x] Documentar objetivo e princípios
- [x] Documentar arquitetura inicial
- [x] Especificar personalidade, reflexos locais e contrato de integração com IA
- [x] Fundação Android presente e interface funcional segundo o proprietário
- [x] Reproduzir build neste checkout pelos agentes
- [x] Primeiro build em dispositivo

## V0.1 — Interface
- [x] Fullscreen horizontal
- [x] Tela de boot
- [x] Rosto/personagem com estética cyberpunk presente
- [x] Glitch/flicker/scanlines
- [x] Estados IDLE, LISTENING, THINKING, SPEAKING/ERROR
- [x] Transição de boot para tela principal
- [ ] Confirmar origem/licença dos assets visuais

Conclusão funcional baseada no relato do proprietário em 15 de setembro de 2026. Ela não representa reprodução do build por um agente nem certificação da licença dos assets.

## V0.2 — Comandos locais
- [x] **V0.2-A:** núcleo Kotlin local para chamada, saudação, agradecimento e hora
- [x] **V0.2-A:** testes do roteamento e das variantes sem repetição imediata
- [x] **V0.2-A:** integrar entrada de debug e compilar o fluxo local
- [x] **V0.2-A:** validar funcionamento digitado pelo proprietário
- [ ] Validar explicitamente cenários de layout com teclado e ausência de DEV na release
- [x] **V0.2-B:** reconhecer presença informal (“tá aí?”, “ta ai?”, “vc tá aí?”)
- [x] Catálogo inicial de frases com personalidade e variação sem repetição imediata
- [x] Reflexos sociais por gatilho: chamada, saudação e agradecimento
- [x] Entrada de desenvolvimento para testar roteamento sem microfone
- [x] Motor inicial de intents com reconhecimento de frases completas
- [ ] Ambiguidade, negação e resultados de ação sem confirmação falsa
- [x] Aumento/redução de volume de mídia implementados com resultados tipados e testes
- [x] Proprietário confirmou funcionamento de volume (TASK-006)
- [ ] Detalhar limites e rotas de áudio no aparelho
- [x] Contribuição remota de mídia, abertura de apps e volume percentual preservada no código
- [x] Revisar contribuição remota (TASK-003) e corrigir parser (TASK-004)
- [ ] Consolidar/remover legado quando necessário; motor remoto continua inativo
- [x] Integrar faixa anterior/próxima no fluxo local (TASK-007)
- [ ] Validar controle de mídia completo (play/pause futuro; fora do escopo TASK-007)
- [x] Integrar abertura de aplicativos no fluxo local (TASK-006)
- [x] Validar abertura de aplicativos no aparelho (proprietário: teste ok)
- [x] Consulta de hora local
- [x] Status do sistema (bateria; TASK-008)
- [ ] Navegação/câmera como intents

## V0.3 — Voz
- [x] TTS (baseline no painel DEV; voz do sistema — personalizar depois)
- [ ] TASK-010: TTS, callbacks, ciclo de vida e interrupção implementados em branch; aguardando testes e integração
- [ ] TASK-011: STT local por botão implementado em branch dependente; aguardando testes e fechamento da TASK-010
- [ ] Personalização de voz em recorte separado
- [ ] Wake word
- [ ] Caminho de baixa latência
- [ ] Tratamento de erro/permissões
- [ ] Interrupção de fala, cancelamento e modo silencioso
- [ ] Validar STT/TTS offline e medir latência por etapa no dispositivo

## V0.4 — IA
- [ ] Provider abstrato
- [ ] Integração com provider escolhido
- [ ] Histórico de conversa
- [ ] Personalidade consistente com respostas locais e retenção opcional de histórico
- [ ] Timeout e descarte de respostas após cancelamento
- [ ] Resposta por voz
- [ ] Ações estruturadas com allowlist

## V0.5 — Automotivo
- [ ] Bluetooth/mídia
- [ ] GPS
- [ ] Câmera
- [ ] Inicialização automática quando apropriado
- [ ] Modo de condução / UI simplificada
- [ ] Eventos de sessão com limite de repetição e falas espontâneas opcionais

Ideias adicionais estão em [IDEAS.md](IDEAS.md), sem compromisso de implementação nesta sequência.

## V1.0 — Hardware
- [ ] Escolher método de saída de vídeo
- [ ] Testar monitor automotivo 7"
- [ ] Integrar tela externa
- [ ] Otimizar consumo e estabilidade

## Wave 2 — concluída

- [x] Receber revisão estática TASK-003, sem alterações de código
- [x] TASK-004: corrigir falsos positivos no roteador remoto, sem ativá-lo
- [x] TASK-005: aplicar nome VEXA em textos e chamada digitada
- [x] Proprietário validar Wave 2: relato “teste ok” para HEAD 39d9fce; PR #1 integrado
