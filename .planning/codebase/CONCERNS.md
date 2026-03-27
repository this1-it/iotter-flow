# Codebase Concerns

**Analysis Date:** 2026-03-27

## Tech Debt

**Incomplete Vaadin Flow migration across core UI flows:**
- Issue: Multiple UI classes still contain explicit `TODO(flow-migration)` markers, temporary compatibility code, or behavior downgraded during Vaadin 8 to Flow migration.
- Files: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/visualizers/ControlPanelBaseAdapter.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/maps/ImageOverlayMap.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/maps/DevicesGoogleMap.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/groupwidgets/GroupWidgetListing.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/charts/AbstractChartAdapter.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/charts/controls/TimeFieldPopup.java`
- Impact: Interactive UI behavior is inconsistent; features that depended on Vaadin 8 `Window`, `TabSheet`, navigator routing, event bus wiring, or client-detail APIs are only partially restored.
- Fix approach: Replace migration placeholders with injected Flow services and concrete implementations, then remove temporary compatibility branches once the affected views are verified end-to-end.

**Custom framework shims increase upgrade cost:**
- Issue: The repo ships custom replacements under framework-like packages and compatibility wrappers that emulate Vaadin 8 behavior on Flow.
- Files: `iotter-flow-ui-core/src/main/java/org/vaadin/flow/components/TabSheet.java`, `iotter-flow-ui-core/src/main/java/org/vaadin/flow/components/SplitLayoutCompat.java`, `iotter-flow-ui-core/src/main/java/org/vaadin/flow/components/PanelFlow.java`, `iotter-flow-ui-core/src/main/java/org/vaadin/flow/components/LabelWrapper.java`
- Impact: Future Flow upgrades depend on internal implementation details and reflection hacks instead of supported APIs; breakage risk is concentrated in foundational UI primitives.
- Fix approach: Isolate these classes behind project-owned packages, remove reflection against Flow internals where possible, and progressively replace them with native Flow components or narrow adapters.

**Signup flow is scaffolded but not implemented:**
- Issue: The signup wizard and steps are present, but the lifecycle callbacks and wizard-step contracts are mostly placeholders.
- Files: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/signup/SignUpWizard.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/signup/CredentialStep.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/signup/LegalInfoStep.java`
- Impact: A user can reach an unfinished registration flow that returns `null` captions, blocks progression, and never completes the wizard lifecycle.
- Fix approach: Either finish the signup path completely or remove/hide the route until each step validates, advances, and persists data correctly.

## Known Bugs

**Remote-control and alarm listings expose actions that are not implemented:**
- Symptoms: Opening details or remove actions can throw `UnsupportedOperationException` or do nothing.
- Files: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels/ChannelRemoteControlListing.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels/ChannelAlarmListing.java`
- Trigger: Use row-level detail/remove actions in remote-control or alarm channel listings.
- Workaround: Avoid those actions or patch the UI to hide them until implementations exist.

**Graphic feed selection can fail with hard exceptions in normal UI paths:**
- Symptoms: The UI throws `UnsupportedOperationException("vaadin8 legacy missing backend support")` while collecting measure units or listing available devices.
- Files: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/graphicfeeds/GraphicFeedChoice.java`
- Trigger: Open graphic-feed selection flows that need unit lookup or device enumeration.
- Workaround: None in code; the affected UI path needs implementation.

**Channel listing editor contract is incomplete:**
- Symptoms: `getEditor()` returns `null`, so flows that expect an editor instance cannot safely open edit/view behavior.
- Files: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels/ChannelListing.java`
- Trigger: Any action that tries to edit or display a channel through the base listing contract.
- Workaround: Use alternate channel screens that do not rely on the listing editor path.

**REST endpoint behavior relies on nullable control flow with thin validation:**
- Symptoms: Device configuration revision requests fall through to `404` based on nullable backend responses; API-key validation is inline and duplicated rather than centralized.
- Files: `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceConfigurationRevisionService.java`
- Trigger: Requests to `/v1/device/{serial}/configuration_rev` for missing devices, missing configuration revisions, or inconsistent API-key state.
- Workaround: None beyond caller retries and manual data correction.

## Security Considerations

**Login success/failure bookkeeping is not implemented:**
- Risk: Authentication events are not persisted even though the service exposes hooks for them.
- Files: `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/UserService.java`
- Current mitigation: None beyond comments and empty method bodies in `registerLogin()` and `registerLoginFailure()`.
- Recommendations: Record last-login timestamp, increment and reset failure counters, and lock or rate-limit users after repeated failures.

**Role-sensitive UI checks are stubbed to false or left TODO:**
- Risk: Permission-sensitive screens are making authorization decisions without a real authenticated-user integration.
- Files: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels/ChannelListing.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels/ChannelRemoteControlListing.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels/ChannelGrid.java`
- Current mitigation: Some code paths default to hiding supervisor-only features by returning `false`.
- Recommendations: Route role checks through `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/AuthenticatedUser.java` or a dedicated authorization service, and add tests that prove admin and non-admin behavior.

**Legacy service-locator patterns obscure security boundaries:**
- Risk: UI code still references shared utility access patterns and migration comments about fetching services from parent/UI context, which makes auditing permissions and data access harder.
- Files: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/maps/ImageOverlayMap.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/maps/GroupWidgetsCustomMap.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/maps/DeviceCustomMapForm.java`, `iotter-flow-ui-core/src/main/java/it/thisone/iotter/ui/common/UIUtils.java`
- Current mitigation: Partial migration to constructor/service injection exists in some screens.
- Recommendations: Eliminate remaining implicit service lookups and parent-coupled access so authorization and ownership checks are explicit in constructors and handlers.

## Performance Bottlenecks

**Bulk device operations load entire datasets into memory:**
- Problem: Recovery, exporter, and subscription jobs repeatedly fetch all devices or fetch up to 10,000 devices in one call.
- Files: `iotter-integration/src/main/java/it/thisone/iotter/integration/RecoveryService.java`, `iotter-integration/src/main/java/it/thisone/iotter/integration/SubscriptionService.java`, `iotter-integration/src/main/java/it/thisone/iotter/quartz/ExporterJob.java`, `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java`
- Cause: Job logic is written around `findAll()` and `search(criteria, 0, 10000)` instead of batched streaming or pagination.
- Improvement path: Switch scheduled jobs to paged processing with bounded batch size, and prefer status-specific repository queries over loading all devices into application memory.

**Large service and UI classes concentrate too much behavior:**
- Problem: Several core classes exceed 700 to 1400 lines and mix orchestration, business logic, and data access details.
- Files: `iotter-integration/src/main/java/it/thisone/iotter/integration/SubscriptionService.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/devices/DevicesListing.java`, `iotter-rest-endpoints/src/main/java/it/thisone/iotter/rest/DeviceConfigurationService.java`, `iotter-backend/src/main/java/it/thisone/iotter/persistence/service/DeviceService.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/visualizers/ControlPanelBaseAdapter.java`
- Cause: Features have accumulated in central classes instead of being split into focused services or components.
- Improvement path: Extract query services, command handlers, and smaller UI subcomponents before adding more feature work in these files.

## Fragile Areas

**Flow compatibility components depend on reflection and undocumented internals:**
- Files: `iotter-flow-ui-core/src/main/java/org/vaadin/flow/components/SplitLayoutCompat.java`
- Why fragile: `clearFirstComponent()` and `clearSecondComponent()` mutate private `SplitLayout` fields via reflection; any Flow internal rename or behavior change will break runtime behavior.
- Safe modification: Avoid broad edits in-place; replace call sites gradually with native Flow split layouts and add a focused regression test around component replacement before removal.
- Test coverage: No automated tests cover these compatibility components.

**Chart and map adapters still contain downgraded or approximated behavior:**
- Files: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/charts/WindRoseChartAdapter.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/charts/HistogramChartAdapter.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/charts/VariationChartAdapter.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/visualizers/TandemTraceChartAdapter.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/maps/DevicesGoogleMap.java`
- Why fragile: The code explicitly documents approximations where Vaadin 8 Highcharts or legacy map listeners do not map 1:1 to current libraries; some methods still throw `UnsupportedOperationException`.
- Safe modification: Treat each adapter as a separate migration target, verify visual behavior manually, and avoid refactoring multiple adapter types in one change set.
- Test coverage: No chart or map adapter tests were found under `src/test/java` or `iotter-flow-it`.

**Summary aggregation has silent fallback behavior for unsupported qualifiers:**
- Files: `iotter-cassandra-model/src/main/java/it/thisone/iotter/cassandra/model/SummaryMeasure.java`
- Why fragile: Unsupported qualifiers (`INSTANT_MIN`, `INSTANT_MAX`, `ALM`) silently fall back to mean values, and `value == Double.NaN` is a broken NaN check.
- Safe modification: Replace fallbacks with explicit handling per qualifier, use `Double.isNaN(value)`, and add targeted tests for every qualifier branch before changing downstream consumers.
- Test coverage: No unit tests for `SummaryMeasure` were found.

## Scaling Limits

**Scheduled and recovery jobs are bounded by hard-coded result windows:**
- Current capacity: Job code assumes the active device set fits within `findAll()` or a single `search(..., 0, 10000)` call.
- Limit: Installations above roughly 10,000 relevant devices will silently miss work or degrade heavily due to full-table loads.
- Scaling path: Introduce cursor- or page-based iteration in `RecoveryService`, `SubscriptionService`, and `ExporterJob`, and persist progress for long-running jobs.

**UI listings still rely on in-memory providers in several migrated views:**
- Current capacity: Listings such as `ChannelListing` and `ChannelRemoteControlListing` copy entire collections into `ListDataProvider`.
- Limit: Large channel sets increase browser payload and server-side memory cost, especially where filters and refreshes operate on full in-memory collections.
- Scaling path: Move affected views to repository-backed lazy providers, matching the newer pageable patterns already used in `DevicesListing` and `GroupWidgetListing`.

## Dependencies at Risk

**Project-owned replacements inside `org.vaadin.flow.components`:**
- Risk: Classes under `iotter-flow-ui-core/src/main/java/org/vaadin/flow/components/` shadow or extend framework concepts in a way that is tightly coupled to current Flow internals.
- Impact: Framework upgrades can break tab, panel, split-layout, or wrapper behavior in foundational UI flows.
- Migration plan: Repackage these adapters under a project namespace, reduce API surface, and retire them incrementally in favor of native Flow components.

**Migration-era Chart.js approximations:**
- Risk: Chart adapters still carry explicit notes that legacy Highcharts behavior is not fully reproduced.
- Impact: User-visible regressions remain likely in advanced chart interactions and visual parity.
- Migration plan: Prioritize adapter-by-adapter acceptance criteria, then remove TODO-based approximations only after manual and automated visual verification.

## Missing Critical Features

**End-to-end signup/registration UX:**
- Problem: The wizard shell exists but step captions, navigation hooks, and completion behavior are unfinished.
- Blocks: Self-service user onboarding through the migrated UI.

**Complete CRUD actions on migrated channel management screens:**
- Problem: Detail/remove/editor flows are partially missing in channel listings.
- Blocks: Admin maintenance workflows in migrated channel UIs.

**Operationally complete authentication audit trail:**
- Problem: Login success/failure registration hooks are present but not implemented.
- Blocks: Account lockout, login auditing, and reliable incident forensics around user authentication.

## Test Coverage Gaps

**Most modules have no detected automated tests:**
- What's not tested: `iotter-backend`, `iotter-cassandra`, `iotter-integration`, `iotter-flow-ui`, `iotter-flow-ui-core`, and REST modules do not show module-local `src/test/java` suites in this workspace.
- Files: `iotter-backend/src/main/java/...`, `iotter-cassandra/src/main/java/...`, `iotter-integration/src/main/java/...`, `iotter-flow-ui/src/main/java/...`, `iotter-flow-ui-core/src/main/java/...`, `iotter-rest-endpoints/src/main/java/...`
- Risk: Regressions in core services, Cassandra aggregation, security flows, and migrated UI behavior can ship unnoticed.
- Priority: High

**Existing UI integration tests cover only login and about views:**
- What's not tested: Complex migrated screens such as device listings, charts, maps, signup, channel management, and graphic feed selection.
- Files: `iotter-flow-it/src/test/java/it/thisone/iotter/ui/authentication/LoginScreenIT.java`, `iotter-flow-it/src/test/java/it/thisone/iotter/ui/about/AboutViewIT.java`
- Risk: The most migration-heavy UI areas have no browser-level regression protection.
- Priority: High

**Critical runtime stubs have no safety net:**
- What's not tested: Unsupported or placeholder behavior in `ChannelRemoteControlListing`, `ChannelAlarmListing`, `GraphicFeedChoice`, `SummaryMeasure`, and `ExporterJobScheduler`.
- Files: `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels/ChannelRemoteControlListing.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels/ChannelAlarmListing.java`, `iotter-flow-ui/src/main/java/it/thisone/iotter/ui/graphicfeeds/GraphicFeedChoice.java`, `iotter-cassandra-model/src/main/java/it/thisone/iotter/cassandra/model/SummaryMeasure.java`, `iotter-integration/src/main/java/it/thisone/iotter/integration/ExporterJobScheduler.java`
- Risk: These are exactly the kinds of partial implementations that fail only when exercised in production paths.
- Priority: High

---

*Concerns audit: 2026-03-27*
