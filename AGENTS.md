# DELAMAIN — instruções para agentes de código

## Objetivo
Construir um aplicativo Android pessoal que funcione como assistente automotivo, inspirado na estética de Delamain/KITT. O projeto é para uso privado do proprietário.

## Regras de desenvolvimento

- Não adicionar funcionalidades não solicitadas apenas por iniciativa própria.
- Trabalhar em incrementos pequenos e compiláveis.
- Antes de alterar arquitetura, consultar `docs/ARCHITECTURE.md`.
- Atualizar `docs/ROADMAP.md` quando uma etapa for concluída ou mudar.
- Preferir APIs Android oficiais e bibliotecas maduras.
- Evitar dependências pesadas sem justificativa.
- Priorizar baixa latência e funcionamento local para comandos simples.
- Não enviar áudio para serviços externos sem uma ação explícita que necessite disso.
- Não implementar acesso irrestrito ao sistema. Ações do assistente devem passar por comandos/ações permitidos.
- Não adicionar chaves de API, tokens, senhas ou credenciais ao repositório.
- Não colocar segredos em código-fonte, `local.properties` versionado ou recursos do app.
- Testar build após mudanças relevantes.
- Se algo não puder ser testado no ambiente, declarar isso claramente em vez de afirmar que funciona.

## Estilo de código

- Kotlin.
- Preferir Jetpack Compose para a interface.
- Código simples e legível.
- Nomes descritivos.
- Comentários somente quando explicarem uma decisão não óbvia.

## Hardware alvo

O aplicativo não deve depender inicialmente de um aparelho específico. O primeiro aparelho de teste previsto é um Xiaomi Redmi 13. Um Redmi 6 também poderá ser usado futuramente.

A conexão com o monitor automotivo de 7 polegadas é uma etapa posterior e não deve bloquear o desenvolvimento do app.

## Ordem de implementação

1. Fundação do projeto.
2. UI fullscreen e tela de boot.
3. Estados visuais do personagem/interface.
4. Motor de comandos locais.
5. TTS.
6. STT/wake word.
7. Integração de IA.
8. Mídia/Bluetooth/GPS/câmera.
9. Integração com tela externa.

## Critério de conclusão

Uma tarefa só é considerada concluída quando o código estiver implementado, compilação/testes relevantes forem executados quando possível e a documentação necessária estiver atualizada.
