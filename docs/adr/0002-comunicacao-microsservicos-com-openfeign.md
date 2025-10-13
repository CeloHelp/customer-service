ADR 0002 — Comunicação entre microsserviços com OpenFeign

Status: Proposta (aguardando implementação)
Data: 2025-10-08
Serviços afetados: customer-service, transaction-service (e leitura pelo statement-service)

Contexto

O CoderBank V2 é composto por três microsserviços independentes. O customer-service precisa, durante o cadastro de um cliente, provisionar contas no transaction-service. No MVP, precisamos de uma comunicação síncrona, simples e tipada, com observabilidade e resiliência adequadas, sem introduzir mensageria ainda.

Decisão

Adotar OpenFeign para chamadas HTTP entre serviços, iniciando pela integração:

customer-service → transaction-service: criação de contas (provisionamento após cadastro do usuário).

Manter a comunicação síncrona no MVP, com Resilience4j (Retry e Circuit Breaker) aplicado no cliente.

Escopo (MVP)

Cliente Feign no customer-service chamando o endpoint de provisionamento de contas do transaction-service.

Configurações mínimas: base URL via configuração, timeouts razoáveis, logs de requisições/respostas, e políticas de retry/circuit-breaker.

Tratamento de falhas: em indisponibilidade do transaction-service, registrar pendência local (customer) para reprocessamento via scheduler.

Motivadores

Produtividade: interface declarativa e tipada reduz boilerplate de HTTP.

Padronização: uso consolidado no ecossistema Spring; fácil integração com Spring Boot/Cloud.

Resiliência: combinação natural com Resilience4j.

Evolução incremental: permite migrar para mensageria (eventos) no futuro sem bloquear o MVP.

Alternativas consideradas

WebClient/RestTemplate: maior controle, porém mais verboso; Feign atende com menos código no MVP.

Mensageria (Kafka + Outbox): robustez e desacoplamento superiores, porém complexidade maior; será avaliado numa fase posterior (especialmente para statement-service).

gRPC: forte para contratos binários e performance; não necessário agora e adiciona ferramentas/infra novas.

Contrato inicial (alto nível)

Rota alvo no transaction-service: criação de contas para um userId.

Respostas com dados mínimos de contas geradas (números de conta e timestamp).

Em caso de falha temporária, o customer-service não reprova o cadastro do usuário; marca pendência e agenda reprocesso.

Resiliência e Observabilidade

Retry com backoff, Circuit Breaker com janela de erros e tempo de abertura configurados.

Logs estruturados com correlationId/userId.

Métricas expostas via Actuator (taxas de erro, estados do CB, latência).

Timeouts explícitos para evitar chamadas presas.

Consequências

Positivas

Código enxuto e contratos claros; menor tempo de entrega no MVP.

Resiliência funcional com componentes maduros do ecossistema Spring.

Negativas / Riscos

Acoplamento temporal (serviço destino deve estar “de pé”); mitigado por pendências e reprocesso.

Evolução de contratos deve ser versionada (ex.: /v1/...) para evitar quebras.

Plano de implementação (passos)

Definir contrato HTTP do provisionamento no transaction-service (recurso, verbos, status e payloads).

Criar cliente Feign no customer-service, configurando base URL, timeouts e logs.

Aplicar Resilience4j (Retry/Circuit Breaker) ao cliente Feign.

Implementar persistência de pendências no customer-service e scheduler de reprocesso.

Publicar documentação com exemplos de requisições/respostas (Swagger/OpenAPI).

Criar testes de integração (cenários de sucesso, falha com retry, fallback com pendência).

Critérios de aceitação

Cadastro de usuário não falha quando o transaction estiver temporariamente indisponível; pendência registrada e reprocessada com sucesso.

Métricas do Circuit Breaker e Retry visíveis no Actuator.

Logs com correlação entre as chamadas (IDs rastreáveis).

Acompanhamento

Monitorar taxa de pendências, tempo médio de reprocesso e taxa de sucesso após retry.

Revisitar a decisão ao introduzir eventos (Kafka + Outbox) para statement-service e para desligar acoplamento temporal desta integração.