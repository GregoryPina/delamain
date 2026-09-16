# TASK-018 — preferências de personalidade local

Status: IMPLEMENTADA em `codex/task-018-personality` (base: TASK-017). Aguardando validação do proprietário. Handoff: `docs/handoffs/TASK-018.md`.

## Resultado para o proprietário

Configurar nome de tratamento e tom sem IA. A identidade VEXA e a precisão das confirmações permanecem constantes. Defaults propostos: nome vazio (não inventar Gregory em todo aparelho), tom atual e humor discreto. Expor no máximo dois tons inicialmente: atual e direto; não criar dezenas de perfis.

## Ponto de partida real

`LocalCommandEngine` já recebe `configuredName` e mantém índices de variantes por intenção/resultado. Aproveitar isso. A troca de preferência não deve recriar o motor a cada recomposição nem zerar repetidamente a rotação de respostas. Se precisar alterar o contrato de configuração, documentar como a atualização ocorre entre comandos.

## Regras

- Nome opcional, trim, limite proposto 40 caracteres, vazio desativa tratamento. Aceitar nomes Unicode; não aceitar controles/quebras de linha como comandos. Nome é dado, nunca instrução para executor/IA.
- Reusar store da 014 em vez de outro arquivo/singleton. Reset de personalidade não apaga voz nem modo de resposta.
- Templates variam estilo, não fatos. Percentual, hora, disponibilidade, sucesso/falha e `Dispatched` permanecem tipados. Humor nunca transforma “solicitei” em “executei”.
- Não incorporar insultos, sarcasmo em erro operacional ou frases longas por padrão. Não alegar trânsito, localização, sensores ou condição do carro.
- Prévia de tom apenas gera exemplo local, não executa volume/apps/mídia nem consulta bateria por acidente. Não usar o roteador de ações para produzir prévia.
- Não adicionar falas espontâneas, memória de conversa, monitoramento ou evento de partida nesta tarefa.

## Arquivos permitidos

Preferências existentes, banco/templates locais, engine apenas para configuração/seleção, tela compacta de configuração, testes e docs. Não alterar reconhecimento de frases, allowlist ou adapters Android.

## Casos de teste e aceite

Nome ausente/nome Unicode/limite/exclusão; alternância de tom mantém números e semântica em sucesso, limite, indisponível, falha e despacho; preview faz zero chamadas ao ActionPort; atualização não duplica ação; persistência/reset parcial; variantes não repetem imediatamente quando houver opções válidas. Não testar strings aleatórias sem contrato; criar exemplos de cada resultado relevante.

Proprietário avalia se as falas soam naturais e curtas, troca preferências/reabre app e confirma que comandos continuam iguais. Entrega inclui tabela pequena de exemplos antes/depois e defaults realmente adotados. A qualidade do timbre TTS não é corrigida por troca de texto; registrar feedback de voz separadamente.
