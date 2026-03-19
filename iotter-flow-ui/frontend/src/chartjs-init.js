// Wrapper that ensures Moment.js is on window before Chart.js evaluates.
// Chart.js 2.7.2 captures window.moment at module-evaluation time.
//
// Vaadin 23 uses Vite (not Webpack), so we use dynamic imports with
// top-level await to guarantee execution order.
const momentModule = await import('Frontend/generated/jar-resources/chart/Moment.js');
const moment = momentModule.default || momentModule;
if (typeof window !== 'undefined') {
    window.moment = moment;
}
await import('Frontend/generated/jar-resources/chart/Chart.min.js');
