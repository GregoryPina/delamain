# TASK-016 — interação de uso normal, fora do DEV

Status: PLANEJADA, NÃO DESPACHADA. Depende de TASK-015 integrada e revisão visual deste recorte pelo coordenador.
BASE COMMIT: informar no despacho. Aplicar [protocolo](NEXT_TASKS.md) e DESIGN; interface original já aceita deve ser preservada.

## Objetivo

Usar a VEXA sem abrir ferramentas de desenvolvimento. Promover somente ouvir uma frase, cancelar/parar e entrada de texto acessível; manter seleção/diagnóstico detalhado em área de configuração/DEV até tarefa própria. Debug e release devem usar o mesmo coordenador, sem caminhos de execução duplicados.

## Proposta de interação

- Controle explícito OUVIR, mudando para CANCELAR durante preparação/escuta/processamento.
- Durante TTS, PARAR deve estar acessível por um toque.
- Entrada de texto recolhida por padrão, acessível também sem permissão/serviço de microfone; não obrigar voz para recuperar falha de voz.
- Mensagem curta explica serviço local indisponível ou permissão negada; não sugerir que o app está ouvindo quando não está.
- Permissão só depois do toque e sem captura automática após conceder. Não abrir Settings por conta própria; pode oferecer ação explícita para permissões quando negadas permanentemente.
- Toque acidental no rosto não inicia captura. Preservar boot, HUD, assets, orientação e efeitos. Se o layout exigir mudança material, enviar proposta visual pequena antes de reestruturar a tela.

## Engenharia e permissões

Mover a declaração RECORD_AUDIO/consulta RecognitionService do manifesto debug ao main somente nesta tarefa, para a funcionalidade agora presente na release. Não adicionar serviço em foreground, permissões Bluetooth/localização ou inicialização em background. Conferir manifesto mesclado das duas variantes pelo roteiro do proprietário.

O painel DEV permanece exclusivo de debug e apenas usa a mesma sessão. Fechar DEV não deve destruir uma sessão normal indevidamente: antes, definir propriedade/lifetime no coordenador. Nenhum componente deve registrar dois observadores para executar o mesmo comando. A demonstração por tap dos estados antigos não pode voltar na release.

Proposta de acessibilidade: botões com descrição semântica, estado anunciado sem repetir toda transcrição, área de toque adequada e indicação visual além de cor. Confirmar teclado, fontes maiores e landscape no aparelho. Não prometer segurança ao dirigir; nesta etapa o app não mede movimento.

## Arquivos permitidos

UI main, composição/coordenador da 015, painel debug/stub, manifestos main/debug, recursos de texto e testes de estado/interação pertinentes. Sem renomear identificadores técnicos ou reescrever efeitos. Evitar testes por screenshot que apenas fixem pixels; testar transições e ausência de ações inesperadas.

## Matriz de aceite do proprietário

Debug e release: abrir não pede permissão nem escuta; ouvir pede permissão quando necessário; negar preserva texto; conceder exige novo toque; mic encerra ao cancelar/sair; comandos são executados uma vez; fala pode ser parada sem abrir DEV. Release não mostra DEV, nomes de classes, IDs de pedido ou mensagens técnicas. Conferir tamanho do controle, scroll/teclado, recuperação de erro e navegação de acessibilidade.

## Entrega

PR com pequena descrição visual antes/depois, lista exata de permissões, evidência do proprietário por variante quando disponível e limitações. Release continua não assinada conforme processo atual; não incluir publicação em loja ou assinatura de distribuição. Atualizar instruções de uso do README, que hoje apontam somente para DEV.
