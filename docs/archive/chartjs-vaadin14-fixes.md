# Chart.js Fixes for Vaadin 14 (Migration Notes for Vaadin 24)

## Context

The project uses the Vaadin Chart.js addon (`org.vaadin.addons.chartjs:chartjs:0.1.39`) which bundles:
- **Chart.js 2.7.2** — `META-INF/resources/frontend/chart/Chart.min.js` (browserify bundle)
- **Moment.js 2.20.1** — `META-INF/resources/frontend/chart/Moment.js` (UMD format)
- **chartjs-plugin-annotation.min.js**, **chartjs-plugin-zoom.min.js**, **hammer.min.js**

Chart.js 2.7.2 requires `window.moment` to be available at module-evaluation time for its time scale to work. The addon declares both files via `@JsModule` annotations on its Java classes.

## Problem 1: Moment.js / Chart.js Load Order

### Root Cause

Vaadin 14's `generated-flow-imports.js` sorts all `@JsModule` imports **alphabetically**. This produces:

```javascript
import '@vaadin/flow-frontend/chart/Chart.min.js';   // loads first (C < M)
import '@vaadin/flow-frontend/chart/Moment.js';       // loads second
```

Chart.min.js is a self-contained **browserify bundle** with an internal module system. At evaluation time, its time scale module executes:

```javascript
var n = t(1);                                    // internal require — returns undefined
n = "function" == typeof n ? n : window.moment;  // fallback to window.moment
```

Since Moment.js hasn't loaded yet, `window.moment` is `undefined`, and `n` is captured as `undefined` in a closure. When the chart later tries to render a time axis:

```
Error: Chart.js - Moment.js could not be found!
```

### Why Common Fixes Don't Work

| Approach | Why it fails |
|----------|-------------|
| `@JavaScript` annotation for Moment.js | Webpack modules don't guarantee eager global execution |
| Wrapper JS with `import moment; window.moment = moment;` via `@JsModule` | ESM `import` statements are hoisted — all imports in `generated-flow-imports.js` evaluate before any module body code. The wrapper's `window.moment = moment` runs AFTER Chart.min.js has already captured `undefined` |
| Webpack `ProvidePlugin` for `moment` | Chart.min.js is a pre-built browserify bundle — webpack doesn't rewrite its internal `require` calls |
| Webpack entry point prepend | ESM imports in `generated-flow-imports.js` are hoisted above entry-point synchronous code |

### Solution: `NormalModuleReplacementPlugin` + Wrapper

**Files involved:**
- `iotter-flow-ui/webpack.config.js`
- `iotter-flow-ui/frontend/src/chartjs-init.js`
- `AbstractChartAdapter.java` — `@JsModule("./src/chartjs-init.js")`

**How it works:**

1. `webpack.config.js` copies `Chart.min.js` to `Chart.original.js` at build time (to avoid circular resolution)
2. `NormalModuleReplacementPlugin` intercepts all imports matching `chart/Chart.min.js` and redirects them to `frontend/src/chartjs-init.js`
3. `chartjs-init.js` uses **CommonJS `require()`** (not ESM `import`) to synchronously:
   - Load Moment.js and set `window.moment`
   - Then load `Chart.original.js` (the real Chart.min.js)

Because `require()` is synchronous within a webpack module, `window.moment` is guaranteed to be set before Chart.js evaluates.

**webpack.config.js:**
```javascript
const chartDir = path.resolve(__dirname, 'node_modules/@vaadin/flow-frontend/chart');
const chartOriginal = path.join(chartDir, 'Chart.original.js');
const chartMin = path.join(chartDir, 'Chart.min.js');
if (!fs.existsSync(chartOriginal)) {
    fs.copyFileSync(chartMin, chartOriginal);
}

module.exports = merge(flowDefaults, {
    plugins: [
        new webpack.NormalModuleReplacementPlugin(
            /chart\/Chart\.min\.js$/,
            chartInitWrapper  // points to frontend/src/chartjs-init.js
        )
    ]
});
```

**frontend/src/chartjs-init.js:**
```javascript
const moment = require('@vaadin/flow-frontend/chart/Moment.js');
if (typeof window !== 'undefined') {
    window.moment = moment;
}
require('@vaadin/flow-frontend/chart/Chart.original.js');
```

### Vaadin 24 Migration Notes

- Vaadin 24 uses **Vite** instead of webpack. `NormalModuleReplacementPlugin` and `webpack.config.js` do not exist in Vaadin 24.
- The Vite equivalent would be a custom plugin in `vite.config.ts` or using `optimizeDeps.include` / `resolve.alias`.
- Consider upgrading to **Chart.js 3.x or 4.x** which dropped the hard Moment.js dependency (uses a pluggable date adapter system). This eliminates the load-order problem entirely.
- If sticking with Chart.js 2.x on Vaadin 24, a Vite plugin that rewrites the import or a custom `index.html` script tag loading Moment.js globally would be needed.
- The `chartjs:0.1.39` addon is Vaadin 14-specific. A Vaadin 24 replacement or custom wrapper around Chart.js 4.x with `@NpmPackage` is recommended.

## Problem 2: Undefined `labels` in Time Scale Config

### Root Cause

Chart.js 2.7.2's time scale `determineDataLimits` unconditionally reads:

```javascript
for (t = 0, i = f.data.labels.length; t < i; ++t)
```

If `chart.data.labels` is `undefined` (i.e., never set in the Java config), this throws:

```
TypeError: Cannot read properties of undefined (reading 'length')
```

The `MultiTraceChartAdapter` and `RollupActivityChartAdapter` use `TimeScale` on the X axis but only add datasets (with time-value pairs) — they never set `labels` on the data config, because time-axis charts don't use categorical labels.

### Solution

Initialize an empty labels list in `createBaseConfiguration()`:

```java
protected LineChartConfig createBaseConfiguration() {
    LineChartConfig config = new LineChartConfig();
    config.data().labelsAsList(new ArrayList<>());  // prevents undefined labels
    // ... rest of config
}
```

**Files changed:**
- `MultiTraceChartAdapter.java` — `createBaseConfiguration()` line 68
- `RollupActivityChartAdapter.java` — `createBaseConfiguration()` line 62

### Vaadin 24 Migration Notes

- Chart.js 3.x/4.x handles missing `labels` gracefully (defaults to empty array internally), so this fix would not be needed after upgrading Chart.js.
- If using Chart.js 2.x on Vaadin 24, the same empty-labels initialization is still required.

## Summary of All Changed Files

| File | Change |
|------|--------|
| `iotter-flow-ui/webpack.config.js` | `NormalModuleReplacementPlugin` to redirect Chart.min.js through Moment.js wrapper |
| `iotter-flow-ui/frontend/src/chartjs-init.js` | Wrapper that loads Moment.js before Chart.js using synchronous `require()` |
| `AbstractChartAdapter.java` | Added `@JsModule("./src/chartjs-init.js")` annotation |
| `MultiTraceChartAdapter.java` | Added `config.data().labelsAsList(new ArrayList<>())` in `createBaseConfiguration()` |
| `RollupActivityChartAdapter.java` | Added `config.data().labelsAsList(new ArrayList<>())` in `createBaseConfiguration()` |

## Recommended Vaadin 24 Strategy

1. Replace `chartjs:0.1.39` addon with a direct `@NpmPackage("chart.js", "4.x")` dependency
2. Use Chart.js 4.x date adapter (`chartjs-adapter-date-fns` or `chartjs-adapter-luxon`) instead of Moment.js
3. Build a thin Java wrapper around Chart.js 4.x using Vaadin's `@ClientCallable` / `LitElement` pattern
4. This eliminates both the load-order issue (no Moment.js dependency) and the labels issue (Chart.js 4.x is more defensive)
