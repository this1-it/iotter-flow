## Implement End-to-End Signup Wizard

Implement the scaffolded signup flow so an unauthenticated user can open signup, complete credential and legal-information steps, submit the wizard successfully, and either create the expected account/profile records or fail with a clear user-visible validation/error message.

## Purpose / Big Picture

The current signup component is reachable in source form but not operational: step captions return `null`, one step has no UI at all, navigation callbacks always block progress, and finish/cancel callbacks do nothing. The completed work should make signup behave like a real Flow wizard:

- Users can enter credentials, legal information, and mandatory consent fields.
- Each step validates before allowing forward navigation.
- Finish persists the signup payload through existing backend services plus any missing identity-profile persistence seam.
- Cancel and success outcomes close or navigate away cleanly and provide visible feedback.
- The route/entry point is intentional rather than an orphaned component.

Observable outcome: a tester can start from the login area or dedicated signup route, complete the wizard with valid sample data, and see the created account reflected in persistence and/or in the existing user management UI.

## Context and Orientation

Current implementation state:

- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/signup/SignUpWizard.java`
  Owns the wizard shell but currently wires only two placeholder steps and leaves all `WizardProgressListener` callbacks empty. Finish and cancel button listeners are commented out.
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/signup/CredentialStep.java`
  Wraps `CredentialInputForm` but returns `null` caption and `false` for both navigation methods, so the wizard cannot advance.
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/signup/LegalInfoStep.java`
  Is entirely scaffolded: no content, no caption, no navigation behavior.
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/signup/CredentialInputForm.java`
  Already provides a Flow/Firitin-backed form with binder fields for username, email, serial number, activation key, password, and password confirmation. This is the strongest existing reusable piece.
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/signup/LegalInfoInput.java`
  Defines the intended legal/profile payload, including `IdentityProfileType`, personal/company details, address fields, and mandatory correctness/conditions booleans, but there is no matching form class yet.
- `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/wizards/Wizard.java`
  The wizard core enforces navigation only through `WizardStep.getCaption()`, `getContent()`, `onAdvance()`, and `onBack()`. The flow wrapper must explicitly wire finish/cancel side effects.
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/provisioning/ProvisioningWizard.java`
  This is the closest reference implementation for wizard composition, button labeling, event forwarding, and step-driven validation in the current codebase.
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/UserService.java`
  Already contains `userRegistration(User entity, String serial)` for registering a normal user against an activated device serial. This likely covers part of signup persistence.
- `iotter-backend/src/main/java/it/thisone/iotter/persistence/model/IdentityProfile.java`
  A JPA entity exists for legal/profile data, but current discovery found no matching service/DAO usage. That gap is the primary implementation risk and must be resolved before finish wiring is finalized.
- `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/authentication/LoginScreen.java`
  The current login route has no signup entry point, and `SignUpWizard` is not used anywhere else in the repo. The implementation must either add a dedicated `@Route` or embed the wizard from login intentionally.

Domain assumptions to validate during implementation:

- Signup is intended for ordinary end users, not administrators.
- Device serial is the association key for `UserService.userRegistration(...)`; the meaning of `activationKey` must be clarified because that method currently ignores it.
- Legal profile persistence is required because the input model exists and the concern was explicitly identified in the codebase map.

Implementation style:

- This plan is not pure TDD. The repo has no existing signup test harness and no current route for this flow. Use focused backend/service tests where feasible, add UI-level tests only if the harness is practical, and document manual verification for the full Flow wizard journey.

## Plan of Work

1. Confirm signup target behavior and missing persistence seam.
   Inspect existing DAOs/services for `IdentityProfile`, any security or onboarding constraints, and whether `activationKey` must be enforced during self-registration. If no persistence seam exists, define the minimal service/DAO additions required to save `IdentityProfile` atomically with `User`.

2. Introduce a concrete signup coordinator model in the UI layer.
   Expand `ISignUpWizard` or replace it with a real contract that exposes the wizard’s shared state: credential input, legal input, validation helpers, submit action, cancel action, and any listeners/navigation hooks. This prevents step classes from depending on the concrete wizard implementation.

3. Finish the credential step.
   Update `CredentialStep` so it returns a translated caption, exposes the existing `CredentialInputForm`, initializes/binds the backing bean explicitly, and allows `onAdvance()` only when binder validation passes and password mismatch state is clean.

4. Build the legal-information form and step.
   Add a new Flow form for `LegalInfoInput` using the same Firitin/Binder style already used in signup and provisioning forms. Include profile type, personal/company details, address fields, and mandatory correctness/conditions acknowledgements. Implement `LegalInfoStep` with a translated caption, real content, and validation-driven `onAdvance()`/`onBack()` behavior.

5. Implement end-to-end submit/cancel behavior in `SignUpWizard`.
   Convert `SignUpWizard` from a passive shell into an orchestrator that:
   initializes shared state for both steps,
   wires finish to a dedicated `commit()` or `submit()` method,
   maps validation and backend exceptions to translated notifications,
   disables duplicate submissions on finish,
   and either navigates back to login or emits an event on success/cancel.
   Reuse patterns from `ProvisioningWizard` for listener registration and button setup where appropriate.

6. Add the missing backend bridge for legal profile persistence.
   If discovery confirms there is no existing service, implement the smallest coherent persistence path for `IdentityProfile`:
   create or expose DAO/service operations,
   map `LegalInfoInput` to `IdentityProfile`,
   link the profile to the created `User`,
   and keep the write atomic with the user registration transaction.
   Be explicit about ownership, audit fields (`createdAt`, `updatedAt`), and any required normalization.

7. Make signup reachable intentionally.
   Add a dedicated signup route/view or an explicit link from `LoginScreen`, depending on the existing product UX. The chosen entry point must be consistent with anonymous access and with how success should return users to login.

8. Verify behavior with focused tests and manual walkthrough.
   Add backend/service tests around any new registration/profile-persistence code. If practical, add a focused UI integration test for happy-path step navigation; otherwise document manual verification covering:
   invalid credentials,
   missing legal acknowledgements,
   duplicate username,
   invalid/missing serial,
   successful completion,
   and cancel behavior.

## Progress

- [x] (2026-03-27 11:52 Z) Validate target signup rules, especially `activationKey` handling and `IdentityProfile` persistence ownership.
- [x] (2026-03-27 15:43 Z) Implement shared signup coordinator state and step contracts.
- [x] (2026-03-27 15:43 Z) Complete `CredentialStep` validation and caption/content behavior.
- [x] (2026-03-27 15:43 Z) Build legal info form and complete `LegalInfoStep`.
- [x] (2026-03-27 15:43 Z) Wire `SignUpWizard` finish/cancel and route the finished flow through `UserService.userRegistration(...)`.
- [x] (2026-03-27 15:49 Z) Add backend persistence for `IdentityProfile` and wire it into `UserService.userRegistration(...)`.
- [x] (2026-03-27 15:43 Z) Add the anonymous route and login entry point for signup.
- [x] (2026-03-27 15:43 Z) Compile verification: `mvn -pl iotter-flow-ui -am -DskipTests compile`.
- [ ] Add focused tests and record manual verification results.

## Surprises & Discoveries

- `CredentialInputForm` is already much closer to production-ready than the surrounding wizard classes; the implementation should preserve and extend it rather than rewrite it.
- `IdentityProfile` exists only as an entity in current discovery, not as an obviously wired service path. That implies the original signup work likely stopped halfway between model design and transactional implementation.
- `SignUpWizard` is currently not referenced anywhere else in the repo, so “fix the wizard” alone is insufficient; reachability is part of the feature.

## Decision Log

- **Decision:** Plan for full implementation rather than hiding the route/component.
  **Rationale:** The codebase already contains credential and legal-profile models, wizard scaffolding, and backend user registration support; these are enough signals that signup is intended to exist, not be removed.
  **Date/Author:** 2026-03-27 — Codex

- **Decision:** Use provisioning wizard patterns as the primary structural reference.
  **Rationale:** It is the nearest in-repo example of how this custom wizard framework is expected to manage steps, validation, finish behavior, and listener/event flow under Vaadin Flow.
  **Date/Author:** 2026-03-27 — Codex

- **Decision:** Treat backend discovery around `IdentityProfile` as the first execution checkpoint.
  **Rationale:** The UI can be completed mechanically, but finish behavior is not correct until legal profile data has a defined persistence path and transaction boundary.
  **Date/Author:** 2026-03-27 — Codex

## Outcomes & Retrospective

Implemented the Flow-side signup flow using the old Vaadin 8 registration page as the reference for validation and field structure.

What is done:

- The wizard now has a real credential step with username uniqueness, password/email checks, and serial/activation-key validation.
- The missing legal-info step now uses a Viritin `AbstractForm`.
- Signup is reachable from the login screen and via a dedicated anonymous route.
- Finishing the wizard creates a `User` through the existing `UserService.userRegistration(...)` backend path.

What remains intentionally out of scope for this slice:

- No automated signup-specific tests were added yet; only compile verification was run.

Success criteria for completion:

- Signup is reachable by an anonymous user through a defined route or login affordance.
- Both wizard steps render non-null captions and content.
- Step validation blocks invalid progression and allows valid progression.
- Finish creates the user and persists associated legal profile data, or surfaces a translated error without partial writes.
- Manual happy-path verification and at least one backend automated test exist.

## Risks / Open Questions

- `UserService.userRegistration(...)` currently validates only the device serial; if the product also requires `activationKey`, the backend API will need to change or an additional validation layer must be added.
- There is no discovered `IdentityProfile` service/DAO yet. If persistence infrastructure is missing, implementation scope increases beyond simple UI wiring.
- The exact anonymous UX is undefined: dedicated `/signup` route versus a button/link in `LoginScreen`.
- The repo currently has no obvious signup-specific translations; missing i18n keys may widen the change set into translation bundles.
- If the product should send activation emails or other out-of-band onboarding messages, that requirement is not represented in the currently discovered code.

## Next Steps / Handoff Notes

- Start execution with backend discovery, not UI polish.
- If `IdentityProfile` persistence is absent, create a small companion ExecPlan only if the backend slice becomes materially larger than “minimal service/DAO + transaction wiring”.
- After implementation, refresh `.planning/codebase/CONCERNS.md` because this item is explicitly listed there as a current codebase concern.
