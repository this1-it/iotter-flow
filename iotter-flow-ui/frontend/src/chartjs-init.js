// Wrapper that ensures Moment.js is on window before Chart.js evaluates.
// Chart.js 2.7.2 (browserify bundle) captures window.moment at module-definition
// time inside a closure. It must exist BEFORE Chart.min.js is first evaluated.
//
// webpack.config.js redirects all Chart.min.js imports to this file via
// NormalModuleReplacementPlugin. This file loads Chart.original.js (a copy of
// Chart.min.js) to avoid circular resolution.
const moment = require('@vaadin/flow-frontend/chart/Moment.js');
if (typeof window !== 'undefined') {
    window.moment = moment;
}
require('@vaadin/flow-frontend/chart/Chart.original.js');
