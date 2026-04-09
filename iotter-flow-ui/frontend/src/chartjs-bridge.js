// Bridge module for Chart.js 4.x with software.xdev wrapper.
// The wrapper loads its own UMD Chart.js build and creates charts through
// the global window.Chart singleton, so the adapter must patch that same
// instance instead of an ESM-local chart.js module instance.
//
// NOTE: We do NOT import the chartjs-adapter-date-fns UMD bundle because in
// a Webpack/Vaadin bundle context `typeof exports === 'object'` is true, which
// causes the UMD to call require('chart.js') and register on a different
// Chart.js instance than window.Chart.  Instead we import date-fns directly
// and call window.Chart._adapters._date.override() ourselves.

import {
    toDate, parse, parseISO, isValid, format,
    addYears, addQuarters, addMonths, addWeeks, addDays,
    addHours, addMinutes, addSeconds, addMilliseconds,
    differenceInYears, differenceInQuarters, differenceInMonths, differenceInWeeks, differenceInDays,
    differenceInHours, differenceInMinutes, differenceInSeconds, differenceInMilliseconds,
    startOfYear, startOfQuarter, startOfMonth, startOfWeek, startOfDay,
    startOfHour, startOfMinute, startOfSecond,
    endOfYear, endOfQuarter, endOfMonth, endOfWeek, endOfDay,
    endOfHour, endOfMinute, endOfSecond
} from 'date-fns';

const FORMATS = {
    datetime: 'MMM d, yyyy, h:mm:ss aaaa',
    millisecond: 'h:mm:ss.SSS aaaa',
    second: 'h:mm:ss aaaa',
    minute: 'h:mm aaaa',
    hour: 'ha',
    day: 'MMM d',
    week: 'PP',
    month: 'MMM yyyy',
    quarter: 'qqq - yyyy',
    year: 'yyyy'
};

const DATE_FNS_ADAPTER = {
    _id: 'date-fns',

    formats: function() {
        return FORMATS;
    },

    parse: function(value, fmt) {
        if (value === null || typeof value === 'undefined') {
            return null;
        }
        const type = typeof value;
        if (type === 'number' || value instanceof Date) {
            value = toDate(value);
        } else if (type === 'string') {
            if (typeof fmt === 'string') {
                value = parse(value, fmt, new Date(), this.options);
            } else {
                value = parseISO(value, this.options);
            }
        }
        return isValid(value) ? value.getTime() : null;
    },

    format: function(time, fmt) {
        return format(time, fmt, this.options);
    },

    add: function(time, amount, unit) {
        switch (unit) {
        case 'millisecond': return addMilliseconds(time, amount);
        case 'second': return addSeconds(time, amount);
        case 'minute': return addMinutes(time, amount);
        case 'hour': return addHours(time, amount);
        case 'day': return addDays(time, amount);
        case 'week': return addWeeks(time, amount);
        case 'month': return addMonths(time, amount);
        case 'quarter': return addQuarters(time, amount);
        case 'year': return addYears(time, amount);
        default: return time;
        }
    },

    diff: function(max, min, unit) {
        switch (unit) {
        case 'millisecond': return differenceInMilliseconds(max, min);
        case 'second': return differenceInSeconds(max, min);
        case 'minute': return differenceInMinutes(max, min);
        case 'hour': return differenceInHours(max, min);
        case 'day': return differenceInDays(max, min);
        case 'week': return differenceInWeeks(max, min);
        case 'month': return differenceInMonths(max, min);
        case 'quarter': return differenceInQuarters(max, min);
        case 'year': return differenceInYears(max, min);
        default: return 0;
        }
    },

    startOf: function(time, unit, weekday) {
        switch (unit) {
        case 'second': return startOfSecond(time);
        case 'minute': return startOfMinute(time);
        case 'hour': return startOfHour(time);
        case 'day': return startOfDay(time);
        case 'week': return startOfWeek(time);
        case 'isoWeek': return startOfWeek(time, {weekStartsOn: +weekday});
        case 'month': return startOfMonth(time);
        case 'quarter': return startOfQuarter(time);
        case 'year': return startOfYear(time);
        default: return time;
        }
    },

    endOf: function(time, unit) {
        switch (unit) {
        case 'second': return endOfSecond(time);
        case 'minute': return endOfMinute(time);
        case 'hour': return endOfHour(time);
        case 'day': return endOfDay(time);
        case 'week': return endOfWeek(time);
        case 'month': return endOfMonth(time);
        case 'quarter': return endOfQuarter(time);
        case 'year': return endOfYear(time);
        default: return time;
        }
    }
};

const dateAdapterRegistered = () => !!(window.Chart?._adapters?._date?.prototype?.formats);

const registerDateAdapter = async () => {
    if (dateAdapterRegistered()) {
        return;
    }

    while (!window.Chart) {
        await new Promise((resolve) => window.setTimeout(resolve, 0));
    }

    // Apply the date-fns adapter directly on window.Chart (the xVaadin wrapper's Chart instance).
    // _adapters._date is a class; override() assigns methods onto its prototype.
    window.Chart._adapters._date.override(DATE_FNS_ADAPTER);

    if (dateAdapterRegistered()) {
        console.debug('chartjs-bridge: date adapter registered on window.Chart');
        return;
    }

    throw new Error('chartjs-bridge: date adapter registration on window.Chart failed');
};

const dateAdapterReady = registerDateAdapter().catch((error) => {
    console.error(error);
    throw error;
});

const wrapBuildChart = () => {
    const wrapper = window.xVaadinChartjsWrapper;
    if (!wrapper || typeof wrapper.buildChart !== 'function' || wrapper.__adapterWrappedBuildChart) {
        return false;
    }

    const originalBuildChart = wrapper.buildChart.bind(wrapper);
    wrapper.buildChart = async (...args) => {
        await dateAdapterReady;
        return originalBuildChart(...args);
    };
    wrapper.__adapterWrappedBuildChart = true;
    return true;
};

if (!wrapBuildChart()) {
    const intervalId = window.setInterval(() => {
        if (wrapBuildChart()) {
            window.clearInterval(intervalId);
        }
    }, 0);
}

const dataStorage = () => {
    if (window.xVaadinChartjsWrapper && window.xVaadinChartjsWrapper.dataStorage) {
        return window.xVaadinChartjsWrapper.dataStorage;
    }
    return null;
};

const getChart = (canvasId) => {
    const storage = dataStorage();
    if (!storage) {
        console.warn('chartjs-bridge: xVaadinChartjsWrapper.dataStorage not available');
        return null;
    }
    const canvas = document.getElementById(canvasId);
    if (!canvas) {
        console.warn('chartjs-bridge: canvas not found:', canvasId);
        return null;
    }
    if (!storage.has(canvas, 'chart-data')) {
        console.warn('chartjs-bridge: no chart-data for canvas:', canvasId);
        return null;
    }
    return storage.get(canvas, 'chart-data');
};

/**
 * Add a data point to a dataset without destroying/recreating the chart.
 * @param {string} canvasId - The canvas element ID
 * @param {number} datasetIndex - Index of the dataset
 * @param {string} x - ISO datetime string
 * @param {number} y - Value
 */
window.chartjsBridge_addDataPoint = function(canvasId, datasetIndex, x, y) {
    const chart = getChart(canvasId);
    if (!chart) return;
    if (datasetIndex >= 0 && datasetIndex < chart.data.datasets.length) {
        chart.data.datasets[datasetIndex].data.push({ x: x, y: y });
        chart.update('none');
    }
};

/**
 * Update scale min/max bounds without full re-render.
 * @param {string} canvasId - The canvas element ID
 * @param {string} scaleId - Scale ID (e.g. "x", "y")
 * @param {string|number} min - New minimum
 * @param {string|number} max - New maximum
 */
window.chartjsBridge_updateScaleBounds = function(canvasId, scaleId, min, max) {
    const chart = getChart(canvasId);
    if (!chart) return;
    if (chart.options.scales && chart.options.scales[scaleId]) {
        chart.options.scales[scaleId].min = min;
        chart.options.scales[scaleId].max = max;
        chart.update('none');
    }
};

/**
 * Trigger a chart update (re-render with current data).
 * @param {string} canvasId - The canvas element ID
 */
window.chartjsBridge_update = function(canvasId) {
    const chart = getChart(canvasId);
    if (chart) {
        chart.update();
    }
};
