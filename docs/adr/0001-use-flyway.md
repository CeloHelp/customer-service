ADR 0001 — Usar Flyway para versionamento de banco

Status: Aceita
Data: 2025-10-08
Serviços afetados: customer-service (e futuramente transaction-service, statement-service)

Contexto

Migração de um monólito para 3 microsserviços, cada um com seu próprio banco.

Necessidade de versionar o schema de forma rastreável, reproduzível e auditável em todos os ambientes (dev/homolog/prod).

Preferência por uma solução simples e aderente ao PostgreSQL.

Decisão

Adotar Flyway para gerenciar migrations SQL versionadas no Git.

Convenção de arquivos: src/main/resources/db/migration/V<versão>__<descrição>.sql
Ex.: V1__create_users.sql.

Configuração por serviço: habilitar Flyway e deixar o Hibernate em validate/none (Flyway é a fonte da verdade).

Motivadores

Rastreabilidade de alterações (tabela flyway_schema_history).

Reprodutibilidade entre ambientes: aplica sempre na mesma ordem.

Simplicidade operativa no MVP: ao subir a aplicação, o Flyway aplica o que falta.

Aderência a microsserviços: cada serviço mantém e evolui seu próprio schema.

Alternativas consideradas

Hibernate ddl-auto=update: simples, mas sem versionamento/auditoria; comportamentos variam por ambiente/versão → descartado para produção.

Liquibase: poderoso (diff/rollback declarativo), porém mais verboso e além das necessidades do MVP; pode ser revisitado no futuro.

Consequências

Positivas

Histórico claro, revisões auditáveis, onboarding fácil.

Alinha autonomia entre serviços e seus bancos.

Negativas / Trade-offs

Exige disciplina: toda alteração de schema deve virar migration.

Rollback não é automático: quando necessário, criar uma nova migration que desfaz a anterior (roll forward).

Implementação (customer-service)

Banco criado inicialmente vazio (customer_db).

Migration inicial: V1__create_users.sql com a criação da tabela users.

Ao subir a aplicação com Flyway habilitado, a migration foi aplicada com sucesso (registro em flyway_schema_history).

Padrões para os próximos serviços

transaction-service: iniciar com migrations para contas e transações (ex.: V1__init_accounts.sql, V2__create_transactions.sql).

statement-service: iniciar com migrations para extratos (ex.: V1__create_statement_entries.sql).

Cada serviço mantém suas migrations no próprio repositório/pasta.

Próximos passos

Criar novas migrations para evoluções de schema (ex.: adicionar colunas, índices, constraints).

Falhar o build em CI se houver migration inválida.

Documentar política de roll forward (evitar editar migrations já aplicadas).