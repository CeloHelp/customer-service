ADR 0004 — Tabela de reprocessamento (Fallback persistente)

Status: Aceita
Data: 2025-10-08
Serviços afetados: customer-service (integração com transaction-service)

Contexto

No fluxo de cadastro de cliente, o customer-service precisa chamar o transaction-service para provisionar contas. Em cenários de indisponibilidade temporária ou falhas de rede, queremos evitar erro final ao usuário e garantir execução posterior. O retry imediato pode não ser suficiente; precisamos de persistência para não perder solicitações.

Decisão

Criar uma tabela de reprocessamento no customer-service para armazenar, de forma durável, as tentativas pendentes de provisionamento de contas.
Essa tabela será consumida por um scheduler (ADR 0005) que reprocessa as pendências até sucesso, aplicando políticas de limite/ backoff quando necessário.

Escopo

Persistir uma linha por tentativa pendente (ex.: provisionar contas para userId).

Registrar estado (PENDING, IN_PROGRESS, FAILED, COMPLETED), contagem de tentativas, timestamps e último erro (truncado).

Garantir idempotência na execução (o transaction-service deve aceitar reexecução sem efeitos colaterais indevidos).

Auditar o histórico via colunas de rastreabilidade (ex.: correlation_id).

Motivadores

Confiabilidade: nenhuma solicitação é perdida por queda temporária do serviço alvo.

Resiliência além do retry imediato: reprocesso assíncrono e controlado.

Observabilidade: visibilidade do volume de pendências e taxa de sucesso após reprocesso.

Alternativas consideradas

Apenas Retry (imediato): insuficiente em janelas longas de indisponibilidade.

Dead-letter queue em mensageria: mais robusto, porém fora do escopo do MVP (complexidade e infra adicionais).

Persistência em memória: perde dados em restart; não atende confiabilidade.

Modelo de dados (alto nível)

Campos recomendados:

id (chave técnica)

user_id (referência ao usuário)

status (PENDING | IN_PROGRESS | FAILED | COMPLETED)

attempt_count (contagem)

next_run_at (quando pode tentar novamente)

last_error (mensagem resumida)

created_at, updated_at

correlation_id (rastreamento entre serviços)

Políticas:

Índice em (status, next_run_at) para busca eficiente de pendências.

Único lógico por (user_id, operação) quando aplicável, para evitar duplicidades.

Consequências

Positivas

Zero perda de solicitação; robustez percebida pelo usuário (cadastro prossegue mesmo com instabilidade).

Base para métricas de confiabilidade (taxa de reprocesso, tempo médio para concluir).

Negativas / Trade-offs

Complexidade operacional: limpar pendências zumbis, tratar backoffs maiores, monitorar crescimento da tabela.

Requer idempotência do serviço alvo para evitar duplicidade de efeitos.

Implementação (orientações)

Controlar transições válidas de estado (ex.: PENDING → IN_PROGRESS → COMPLETED | FAILED).

Definir limite de tentativas e política de backoff (ex.: exponencial) em conjunto com o scheduler (ADR 0005).

Expor endpoint (interno/admin) para inspeção e, se necessário, reprocesso manual de um item.

Versionar a tabela via Flyway (ADR 0001); manter histórico em flyway_schema_history.

Critérios de aceitação

Solicitações que falharem na chamada ao transaction-service ficam persistidas como PENDING.

O scheduler (ADR 0005) consome e conclui os itens quando o destino volta.

Métricas/Logs permitem acompanhar volume e evolução das pendências.

Riscos e mitigação

Acúmulo em downtime prolongado: aplicar backoff e limites; alertar quando fila crescer acima de threshold.

Duplicidade no destino: exigir contrato idempotente (chaves de deduplicação ou checagens por user_id).

Relacionadas

ADR 0003 — Resilience4j (Circuit Breaker + Retry + Fallback)

ADR 0005 — Scheduler de reprocessamento automático

ADR 0001 — Flyway (versionamento da tabela)

ADR 0002 — Banco por serviço (armazenamento fica no domínio do customer-service)