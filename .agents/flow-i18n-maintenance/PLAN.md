## Metadata
- **Owner:** iotter-flow team
- **Created:** 2026-03-30
- **Last Updated:** 2026-03-30
- **Agent / Module:** `iotter-flow-ui`, `it.thisone.iotter.i18n`
- **Related Plans:** `.agents/signup-flow-implementation/PLAN.md`

---

## Complete and align Flow i18n bundles for supported locales
Fill every empty value required by [FlowI18NProvider.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/i18n/FlowI18NProvider.java) by first making [messages_en.properties](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/resources/messages_en.properties) the complete English source of truth, then verifying and completing the Italian, German, French, and Spanish bundles against that English baseline.

---

## Purpose / Big Picture
Vaadin Flow resolves UI labels through `FlowI18NProvider`, and any empty or missing value degrades the UX into blank labels or fallback markers. When this plan is complete, every locale returned by `FlowI18NProvider#getProvidedLocales()` will have a complete, non-empty translation set for the keys used by the current Flow UI, with English acting as the reviewed canonical baseline for the other locale files.

---

## Context and Orientation
- The runtime locale contract is defined in [FlowI18NProvider.java](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/java/it/thisone/iotter/i18n/FlowI18NProvider.java). It explicitly supports:
  - English via `messages_en.properties`
  - Italian via `messages_it.properties`
  - German via `messages_de.properties`
  - Spanish via `messages_es.properties`
  - French via `messages_fr.properties`
- Translation bundles live under [iotter-flow-ui/src/main/resources](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/resources).
- Current inventory from the repository state on 2026-03-30:
  - [messages_en.properties](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/resources/messages_en.properties) contains 18 empty values at the top of the file, all in the signup / filter area:
    `register.serialNumber`, `register.activationKey`, `register.password`, `register.passwordConfirmation`, `register.profileType`, `register.taxCode`, `register.taxCodeCountry`, `register.vatNumber`, `register.birthCountry`, `register.birthCity`, `register.companyRegistrationId`, `register.companyEmail`, `register.addressAdditions`, `register.addressStateProvince`, `register.finish`, `register.next`, `register.back`, `register.password_mismatch`.
  - [messages_es.properties](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/resources/messages_es.properties) currently contains 7 empty values:
    `basic.combobox.bacnet`, `basic.editor.are_you_sure`, `channel.crucial`, `channel.remote.permission`, `controlpanelbase.editor.commands`, `controlpanelbase.editor.feeds.apply_and_refresh`, `controlpanelbase.editor.lastMeasure`.
  - The Italian, German, and French bundles do not currently show empty assignments, but they still need parity verification against English because non-empty does not guarantee key completeness or semantic alignment.
- Relevant UI code already being edited in the worktree appears to include signup and authentication screens, which increases the chance that the empty English signup keys are now needed immediately.
- The translation bundles are already modified in the current worktree. Execution must preserve any legitimate in-progress edits and reconcile rather than overwrite blindly.
- Message-format placeholders such as `{0}`, `{1}`, and similar indexed tokens are part of the runtime contract and must be preserved exactly across all locale files. Translation work may change surrounding wording, but it must not remove, renumber, duplicate, or reorder placeholders unless the source English string changes intentionally and the call sites are verified.

---

## Plan of Work
1. Establish English as the canonical baseline in [messages_en.properties](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui/src/main/resources/messages_en.properties).
   - Fill the 18 empty English values with clear UI copy that matches existing terminology in the signup and filtering flows.
   - Keep wording consistent with the surrounding bundle style, especially the established use of sentence case for labels and short imperative verbs for actions.
   - Validation: no `key=` entries remain in `messages_en.properties`.
2. Build a parity checklist across all five locale bundles.
   - Compare key sets across `messages_en.properties`, `messages_it.properties`, `messages_de.properties`, `messages_fr.properties`, and `messages_es.properties`.
   - Identify four classes of defects: missing keys, empty values, placeholder mismatches, and suspicious placeholder fallbacks where another language was copied through unchanged unintentionally.
   - Validation: produce a mechanical diff or key audit showing that each supported locale contains the same key set as English.
3. Complete non-English bundles from the reviewed English source.
   - For Italian, German, French, and Spanish, add any missing keys and fill any empty values using the English text as the source meaning.
   - Preserve existing good translations; only touch entries that are empty, missing, or clearly inconsistent with the finalized English source.
   - Preserve every indexed placeholder exactly, including braces and numbering, when translating dynamic messages.
   - Give extra attention to Spanish because it already has known empty values.
   - Validation: no `key=` entries remain in any supported locale file.
4. Verify bundle loading behavior against Flow locale support.
   - Confirm the locale filenames match the locales returned by `FlowI18NProvider`.
   - Confirm no new keys depend on unsupported country-specific bundle suffixes.
   - Validation: translation resolution for `Locale.ENGLISH`, `Locale.ITALIAN`, `Locale.GERMAN`, `new Locale("es")`, and `Locale.FRENCH` remains filename-compatible.
5. Run lightweight repository verification.
   - Use a script or shell audit to confirm key parity, zero empty values, and placeholder parity across the five bundles.
   - If practical, compile [iotter-flow-ui](/home/bedinsky/git/aernet-vaadin8-boot/iotter-flow/iotter-flow-ui) with `mvn -pl iotter-flow-ui -am compile` to catch malformed properties escaping or resource-loading regressions.
   - Manual verification focus:
     signup wizard labels and navigation buttons,
     authentication and anonymous views using Flow translations,
     any visible filters recently introduced in Flow listings.

---

## Progress
- [x] (2026-03-30 10:25 Z) Located the ExecPlan template and confirmed the relevant i18n runtime entry point in `FlowI18NProvider`.
- [x] (2026-03-30 10:27 Z) Audited the five locale bundles for current empty-value hotspots.
- [x] (2026-03-30 10:31 Z) Drafted this ExecPlan in `.agents/flow-i18n-maintenance/PLAN.md`.
- [x] (2026-03-30 14:22 Z) Completed the 18 empty English entries in `messages_en.properties`.
- [x] (2026-03-30 14:27 Z) Audited key parity across `messages_en.properties`, `messages_it.properties`, `messages_de.properties`, `messages_fr.properties`, and `messages_es.properties`.
- [x] (2026-03-30 14:34 Z) Filled missing or empty non-English entries from the finalized English baseline, including the known Spanish blanks and the Italian `{0}` placeholder mismatch.
- [x] (2026-03-30 14:36 Z) Re-ran bundle verification: zero key-set gaps, zero placeholder mismatches, zero empty values in all five bundles.
- [x] (2026-03-30 14:17 Z) Compiled `iotter-flow-ui` and upstream modules with `mvn -pl iotter-flow-ui -am -DskipTests compile`.

---

## Surprises & Discoveries
- The major current defect is concentrated in English, not across every locale file. English has 18 empty values, while only Spanish currently shows empty assignments among the non-English bundles.
- The empty English keys sit at the top of the file and correspond to signup flow labels that are likely being surfaced by current UI work in the same dirty worktree.
- The locale bundles have different line counts (`messages_en.properties` is longer than the others), which suggests the execution step should not assume full key parity today.
- Because the translation files are already modified locally, future implementation must review diffs carefully before applying edits.
- The non-English bundles were missing 30 English keys each, not just a few empty values. Most of the gap came from newer Flow signup labels and generic filter/reset labels that had been added only to English.
- Italian had one real formatting bug beyond missing keys: `network.editor.concurrentUsers.validator` had dropped the `{0}` placeholder entirely.

---

## Decision Log
- **Decision:** Use `messages_en.properties` as the canonical source before touching other locales.
- **Rationale:** The user explicitly wants the English file completed first, and that prevents propagating unclear or placeholder meaning into the translated bundles.
- **Date/Author:** 2026-03-30 — Codex

- **Decision:** Treat key-set parity and empty-value checks as separate verification gates.
- **Rationale:** A locale file can appear complete because it has no empty values while still missing keys that exist in English.
- **Date/Author:** 2026-03-30 — Codex

- **Decision:** Limit non-English edits to missing, empty, or clearly dependent entries rather than rewriting whole files.
- **Rationale:** The locale bundles are already populated and locally modified; minimal, targeted edits reduce merge risk and preserve existing reviewed translations.
- **Date/Author:** 2026-03-30 — Codex

- **Decision:** Preserve indexed placeholders exactly during English completion and downstream translation updates.
- **Rationale:** `MessageSource` formatting depends on placeholder identity and position. A translation that changes `{0}` or `{1}` semantics can silently break runtime interpolation even if the text looks correct.
- **Date/Author:** 2026-03-30 — Codex

---

## Outcomes & Retrospective
- Pending execution. Success for this plan means:
- English and the four translated bundles now have matching key sets, no empty values, and matching indexed placeholders for all currently shared dynamic messages.
- `mvn -pl iotter-flow-ui -am -DskipTests compile` completed successfully after the bundle updates.
- Success for this plan means:
  - all five supported locale bundles have matching key sets,
  - no bundle contains empty values,
  - Flow-supported locales resolve to complete resource bundles without blank UI labels for the covered keys.

---

## Risks / Open Questions
- Some English strings may require domain-specific wording rather than literal translation from the other locales; those cases should be resolved in English first, not by copying foreign wording back into English.
- The bundle files may contain historical terminology inconsistencies such as `Plant`, `Device`, `Unit`, `Site`, or `Network`; execution should preserve existing domain conventions unless a mismatch blocks comprehension.
- If the current dirty changes in the translation files reflect partial user work, execution must merge with those edits rather than replace them.
- Compile-time validation will not prove all translations are semantically correct, so targeted manual UI checks remain necessary for the signup flow and listing filters.
- Placeholder mistakes are easy to miss in visual review because the UI may only fail when a specific code path supplies parameters. Mechanical placeholder audits are required, not optional.

---

## Next Steps / Handoff Notes
- Start execution in this order:
  1. finalize the 18 empty English entries,
  2. run a key-parity audit script across the five bundles,
  3. patch only the missing or empty entries in `it`, `de`, `fr`, and `es`,
  4. run the verification audit again and optionally compile `iotter-flow-ui`.
- Keep the plan updated as execution progresses, especially if the parity audit finds missing keys beyond the currently known empty-value set.
