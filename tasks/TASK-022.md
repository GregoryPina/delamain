# TASK-022 — viabilidade de wake word local

Status: PLANEJADA, NÃO DESPACHADA. Tipo: pesquisa com proposta, não implementação de escuta contínua.
Depende de TASK-013/019 e evidências no aparelho; não depende de provider IA escolhido. BASE COMMIT fornecido no despacho. Aplicar [protocolo](NEXT_TASKS.md).

## Pergunta a responder

É viável detectar “Vexa” localmente, com baixa latência e consumo aceitável no aparelho real, sem prejudicar música/TTS e sem transmitir áudio? Só recomendar tecnologia depois de levantar restrições de Android, licença e modelo de palavra personalizada. Não tratar SpeechRecognizer contínuo como detector de wake word.

## Pesquisa delimitada

Comparar no máximo três opções maduras de detecção local, incluindo manter push-to-talk como alternativa válida. Fontes primárias oficiais, data, licença para uso pessoal/distribuição futura, obrigação de chave/ativação/rede, suporte pt-BR/palavra personalizada, tamanho/modelo, APIs/ABI, custo e manutenção. Não reproduzir marketing como medição. Se não houver solução adequada, recomendar adiar.

Não baixar modelo sem conhecer licença, instalar dependências no app ou cadastrar conta paga apenas para comparar. Um protótipo isolado exige tarefa posterior com escopo e permissão para usar microfone. Esta entrega pode conter documento e pequeno pseudocódigo de fluxo, não daemon funcional.

## Projeto de sessão a propor

1. Wake word local detecta candidato, com limiar/cooldown configuráveis.
2. Pausar detector antes de iniciar STT; mic tem um proprietário de cada vez.
3. Sessão STT única usa pipeline existente e cancela em timeout/erro.
4. TTS não pode acordar a própria VEXA; detector permanece suspenso durante fala e por janela justificada após término.
5. Retomar detector somente em modo explicitamente ligado e compatível com lifecycle. Desligar realmente libera microfone/serviço.

Definir atuação com tela apagada/background como decisão separada. Verificar regras atuais de microfone, foreground service, notificações e restrições do Android/HyperOS; não presumir que autostart/bateria ignorada sejam permissões triviais. Não pedir para desativar proteções do aparelho como solução padrão.

## Plano de medição, sem fabricar resultados

Propor ensaio com proprietário: acertos por número de chamadas, falsos positivos por tempo observado, falsos negativos, latência da palavra até escuta, bateria/temperatura em intervalos definidos e efeito de música/TTS. Separar ambiente quieto de carro parado com ruído; não pedir manipulação do app dirigindo. Identificar aparelho, versão, modelo/configuração e duração do ensaio. Não enviar gravações a fornecedor ou manter áudio pessoal sem decisão explícita.

Critérios numéricos devem ser acordados antes do ensaio; não usar percentuais arbitrários como prova de qualidade. Comparar com custo/latência do botão já funcional.

## Entrega e próximo passo

Documento `docs/handoffs/TASK-022.md` com matriz, recomendação ou adiamento, decisões pendentes e proposta de TASK-023 apenas se justificável. Status PESQUISA_ENTREGUE não significa wake word implementada. Não reservar 023 como implementação automática: pode virar protótipo de foreground apenas, conforme evidência. Roteamento local, políticas de cancelamento e privacidade continuam obrigatórios.
