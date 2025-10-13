ADR 0005 — Scheduler de reprocessamento automático

Status: Aceita
Data: 2025-10-08
Serviços afetados: customer-service (integração com transaction-service)

Contexto

Quando o transaction-service estiver indisponível ou instável, o customer-service registrará tentativas pendentes de provisionamento de contas (ver ADR 0004). Precisamos de um mecanismo automático e periódico para reprocessar essas pendências até concluir a operação, sem intervenção manual e com controle de carga.

Decisão

Adotar o scheduler nativo do Spring para o MVP, executando um job periódico que:

seleciona pendências elegíveis (status PENDING/FAILED com next_run_at vencido),

tenta o reprocesso com política de backoff e limite de tentativas,

atualiza o status para COMPLETED em caso de sucesso, ou FAILED e reprograma next_run_at conforme backoff em caso de falha.

Quartz fica como opção futura quando precisarmos de persistência de jobs, calendários complexos ou clusterização. Para o MVP, o scheduler nativo é suficiente, simples e leve.

Escopo

Frequência inicial do job: a cada 60 segundos (ajustável).

Critérios de seleção:

status ∈ {PENDING, FAILED}

next_run_at ≤ now()

(opcional) attempt_count < max_attempts

Tamanho do lote (batch): até 100 itens por execução (configurável) para evitar longos ciclos.

Reprocesso idempotente: a chamada ao transaction-service deve aceitar repetição segura.

Convivência com ADR 0003 (Resilience4j): o job também se beneficia de retry/CB em nível de cliente HTTP.

Motivadores

Confiabilidade: garante execução eventual sem bloquear o fluxo de cadastro do usuário.

Simplicidade operacional no MVP: nenhuma infra adicional.

Observabilidade: permite medir taxa de queima de pendências e tempos de reprocesso.

Alternativas consideradas

Quartz: fornece persistência de jobs e clusterização; adiaremos até haver necessidade real (multinstância/coordenar concorrência, calendários complexos).

Mensageria (Kafka + Outbox/consumidor): abordagem mais robusta e desacoplada; fora do escopo do MVP, será reavaliada na evolução do statement-service.

Política de backoff e limites

Backoff exponencial com jitter para evitar thundering herd (ex.: base 30s, multiplicador 2, teto 10 min).

Limite de tentativas (ex.: 10). Ao exceder, manter como FAILED e sinalizar alerta/observabilidade (dash/alerta).

Recalcular next_run_at a cada falha conforme o backoff.

Janela silenciosa: quando o Circuit Breaker do cliente estiver OPEN, o job respeita a janela antes de tentar novamente (reduz chamadas inúteis).

Concurrency e segurança

MVP: uma instância do customer-service.

Em múltiplas instâncias (futuro), adotar lock por registro (ex.: flag IN_PROGRESS com checagem otimista) ou mecanismo de “claim” via UPDATE atômico, para evitar dois workers processando o mesmo item.

Alternativa futura: Quartz em cluster ou fila dedicada.

Observabilidade (mínimos)

Métricas por execução: itens selecionados, sucesso, falhas, reprogramados, tempo total do ciclo, idade média das pendências.

Logs estruturados por item: userId, correlationId, status_anterior → status_novo, attempt_count, next_run_at, erro_resumido.

Alertas:

CB OPEN por janelas prolongadas,

crescimento anormal de pendências,

idade média acima do SLA (ex.: 15 min).

Consequências

Positivas

Entrega garantida eventual, sem bloquear o fluxo crítico de cadastro.

Simplicidade e baixo custo operacional no MVP.

Negativas / Trade-offs

Não há persistência de “estado do job” (somente dos itens) — suficiente para nosso caso.

Em alta concorrência ou múltiplas instâncias, exige coordenação extra (ver “Concurrency e segurança”).

Implementação (alto nível)

Configurar o agendador periódico (perfil “default/dev/prod” com intervalos ajustáveis via configuração).

Consulta paginada das pendências elegíveis (ordenar por next_run_at e created_at).

Para cada item:

marcar como IN_PROGRESS (controle de concorrência),

executar a integração,

em sucesso → COMPLETED; em falha → FAILED com incremento de attempt_count e atualização de next_run_at conforme backoff.

Expor endpoint administrativo (protegido) para forçar reprocesso de um item e para rever estado da fila.

Critérios de aceitação

Pendências são reprocessadas automaticamente quando o transaction-service volta a responder.

Métricas evidenciam a queima da fila e tempos de reprocesso dentro de um SLA aceitável.

Em janelas de indisponibilidade longa, a fila não explode (backoff + batch limit) e o sistema não derruba o serviço destino quando volta (evita picos por thundering herd).

Relacionadas

ADR 0004 — Tabela de reprocessamento (Fallback persistente)

ADR 0003 — Resilience4j (Circuit Breaker + Retry + Fallback)

ADR 0001 — Flyway (versionamento da tabela de pendências)

ADR 0002 — Banco por serviço (armazenamento no domínio correto)