## Metadata
- **Owner:** iotter-flow team
- **Created:** 2026-02-02
- **Last Updated:** 2026-02-02
- **Agent / Module:** `iotter-cassandra`
- **Related Plans:** `.agents/vaadin14-ui-core-migration/PLAN.md`

## Fix timestamp binding in CassandraQueryBuilder.simpleStatement
Ensure every `java.util.Date` sent through `CassandraQueryBuilder.simpleStatement(...)` is bound as `Instant` for Cassandra `timestamp` columns, and keep row-to-`Date` conversion consistent via `row.getInstant(...)` + `Date.from(...)`.

## Purpose / Big Picture
The Cassandra Java Driver 4 expects modern Java time bindings for `timestamp` columns. Centralizing `Date -> Instant` conversion in `simpleStatement(...)` prevents per-query inconsistencies and avoids runtime codec/type issues when query builders still pass legacy `Date` objects.

## Context and Orientation
- Target file: `iotter-cassandra/src/main/java/it/thisone/iotter/cassandra/CassandraQueryBuilder.java`.
- `simpleStatement(String query, ConsistencyLevel consistency, Object... values)` is the single entry point used by all query builders to create `SimpleStatement` objects.
- Several query builders still pass `Date` values directly (examples in `MeasuresQueryBuilder`, `RollupQueryBuilder`, `FeedsQueryBuilder`, `RegistryQueryBuilder`, `AuthQueryBuilder`).
- `CassandraQueryBuilder#getDate(Row, ...)` already uses the expected read path (`row.getInstant(...)` then `Date.from(...)`), but usages should be re-audited to keep write/read symmetry.

## Plan of Work
1. Audit date bindings and date reads in `iotter-cassandra` query/DAO classes.
   - Identify every `simpleStatement(..., values...)` path where `Date` may be passed.
   - Identify row reads not using `CassandraQueryBuilder.getDate(...)` and decide whether to migrate them.
2. Implement centralized value normalization in `CassandraQueryBuilder.simpleStatement(...)`.
   - Before `SimpleStatement.newInstance(...)`, copy varargs values and convert each non-null `Date` to `((Date) value).toInstant()`.
   - Keep all non-date values untouched and preserve null semantics.
   - Do not change existing consistency-level behavior.
3. Preserve and document row conversion contract.
   - Keep/confirm `getDate(Row, String)` and `getDate(Row, int)` as canonical conversion helpers:
     `Instant instant = row.getInstant(column); Date date = instant == null ? null : Date.from(instant);`.
   - Replace any direct date extraction that bypasses this contract (if found during audit).
4. Validate.
   - Compile module: `mvn -pl iotter-cassandra -am compile`.
   - Add/update targeted unit tests for `CassandraQueryBuilder` conversion behavior if feasible in-module; otherwise document manual verification outcomes in this plan.

## Progress
- [x] (2026-02-02 14:50 Z) Drafted ExecPlan and scoped target files/behavior.
- [x] (2026-02-02 14:52 Z) Audited date writes/reads in `iotter-cassandra` query builders and row extraction paths.
- [x] (2026-02-02 14:51 Z) Implemented `Date -> Instant` normalization in `simpleStatement(...)`.
- [x] (2026-02-02 14:52 Z) Confirmed row-date extraction contract via `getDate(Row, ...)`; no extra call-site changes required.
- [x] (2026-02-02 14:51 Z) Compile-check passed with `mvn -pl iotter-cassandra -am clean compile`.

## Surprises & Discoveries
- `CassandraQueryBuilder#getDate(...)` already matches the desired `Instant -> Date` conversion pattern; the main gap is ensuring write-side normalization is centralized in `simpleStatement(...)`.
- No direct `row.getTimestamp(...)` / `row.get(Date.class)` usages were found in `iotter-cassandra`; existing read conversions are already aligned on `row.getInstant(...)`.

## Decision Log
- **Decision:** Apply conversion at the `simpleStatement(...)` boundary instead of patching each query builder separately.
- **Rationale:** Single-point normalization avoids missed call sites and keeps future query builders consistent by default.
- **Date/Author:** 2026-02-02 — Codex

## Risks / Open Questions
- Some call paths might intentionally pass `Instant` already; conversion logic must avoid double conversion and keep those values unchanged.
- If tests cannot easily introspect bound value types in `SimpleStatement`, verification may rely on focused integration behavior and compile checks.

## Next Steps / Handoff Notes
- Implement step 2 first (central conversion), then run a quick audit-driven cleanup for any non-standard date reads.
