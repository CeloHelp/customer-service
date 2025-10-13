Status: Aceita
Data: 2025-10-08
Serviços afetados: customer-service (chamadas ao transaction-service)

Contexto

O customer-service precisa chamar o transaction-service para provisionar contas após o cadastro de usuários. Como é uma integração síncrona, precisamos mitigar:

Falhas transitórias (picos de latência, timeouts).

Indisponibilidade parcial do serviço destino.

Efeito cascata (propagação de falhas entre serviços).

Decisão

Adotar Resilience4j com três estratégias complementares no customer-service:

Retry: nova(s) tentativa(s) em erros transitórios.

Circuit Breaker: abre o circuito após taxa de falhas configurada, evitando sobrecarga do destino.

Fallback: quando a chamada falhar/CB aberto, registrar pendência para reprocessamento assíncrono (ver ADR 0004 e ADR 0005).

Observação: esta ADR foca no como e por quê; a persistência das pendências e o agendamento do reprocesso estão descritos nas ADRs 0004 e 0005.

Escopo

Aplicar as políticas de resiliência apenas no cliente que chama POST /accounts (provisionamento) do transaction-service.

Outras chamadas externas futuras podem herdar as mesmas políticas, com configurações específicas por endpoint.

Políticas (valores iniciais)

Retry

Tentativas: 3

Backoff: exponencial, base 500ms

Erros que disparam retry: timeouts, 5xx; não repetir em 4xx (erros do cliente)

Circuit Breaker

Janela: count-based de 10 chamadas

Limite de falha: ≥ 50%

Tempo em open: 30s (volta para half-open e testa novamente)

Timeouts (via cliente HTTP/Feign/WebClient): explícitos, alinhados com o retry para evitar “pendurar”

Motivadores

Evitar cascata de falhas e proteger o transaction-service.

Melhorar UX: o cadastro do usuário não falha por uma indisponibilidade momentânea; fica pendente e é reprocessado depois.

Observabilidade clara: métricas de CB/Retry registram estabilidade da integração.

Alternativas consideradas

Sem resiliência: maior risco de indisponibilidade e saturação em cascata → descartado.

Spring Retry apenas: cobre tentativas, mas não protege de padrões de falha prolongados (sem CB).

Mensageria/Kafka: excelente desacoplamento, mas fora do escopo do MVP; avaliado em fase posterior (particularmente para eventos de transação/extrato).

Consequências

Positivas

Resiliência real em falhas transitórias e intermitentes.

Proteção do serviço destino e backpressure automático via CB.

Telemetria útil para SRE/observabilidade.

Negativas / Trade-offs

Complexidade a mais nas configurações e tuning fino.

Sem garantia exactly-once no MVP (pendências reprocessadas podem exigir idempotência no destino).

Implementação (alto nível)

Configurar Resilience4j no customer-service com instâncias nomeadas (ex.: createAccounts).

Anotar o método do cliente (Feign) com Retry/CB ou aplicar via config externa.

Fallback operacional: em erro final/CB aberto, registrar uma pendência (tabela/armazenamento definido na ADR 0004).

Métricas & logs: expor métricas do CB/Retry via Actuator; logar com correlationId e userId.

Testar cenários: sucesso; falha com retry e sucesso subsequente; falha com CB open e persistência de pendência; reprocesso posterior (ADR 0005).

Critérios de aceitação

Cadastro de usuário não retorna erro final quando o transaction-service está temporariamente indisponível; retorna status adequado e cria pendência.

Métricas do Circuit Breaker e Retry visíveis no Actuator; logs estruturados permitem traçar a tentativa/fallback por userId.

Reprocesso posterior conclui a criação das contas sem intervenção manual (coordenado com ADR 0005).

Observabilidade (mínimos)

Logs JSON com service, operation=createAccounts, userId, correlationId, cbState, retryCount, outcome.

Métricas: estado do CB, contadores de falha/sucesso, latência por endpoint.

Alertas: notificar se CB permanecer open além de janela definida (ex.: >5 min).

Riscos e mitigação

Reprocessos duplicados: exigir idempotência no transaction-service (ex.: chave de de-duplicação por userId/requestId).

Config inadequada (CB muito sensível ou muito permissivo): ajustar limiares com base nas métricas coletadas.

Erros funcionais (4xx): não disparar retry; encaminhar para tratativa funcional (ex.: validação de dados).

Relacionadas

ADR 0004 — Tabela de reprocessamento (Fallback persistente)

ADR 0005 — Scheduler de reprocessamento automático

ADR 0001 — Flyway (para versionar a tabela de pendências)

ADR 0002 — Banco por serviço (isola o armazenamento de pendências no domínio certo)