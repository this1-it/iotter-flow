import 'gridstack/dist/gridstack.min.css';
import 'gridstack/dist/gridstack-extra.min.css';
import { GridStack } from 'gridstack';

class GridstackBoard extends HTMLElement {
    constructor() {
        super();
        this._grid = null;
        this._editable = true;
        this._onGridChange = null;
    }

    connectedCallback() {
        if (this._grid) {
            return;
        }
        // If this element is re-attached, remove any stale children left from
        // previous GridStack instances before re-initializing.
        this.innerHTML = '';
        this.classList.add('grid-stack');
        if (this._editable) {
            this.classList.add('gs-editable');
        } else {
            this.classList.remove('gs-editable');
        }
        this._grid = GridStack.init({
            // column: 12,
            // disableOneColumnMode: true,
            // cellHeight: 70,
            // float: false,
            // animate: true,
            // margin: 5,
            // staticGrid: !this._editable
        }, this);
        if (!this._grid) return;

        var items = [
            { content: 'my first widget' },
            { w: 2, content: 'another longer widget!' }
        ];
        this._grid.load(items)

        // Dialog overlays can attach while sizing; recalculate once painted.
        requestAnimationFrame(() => {
            if (this._grid) {
                this._grid.onParentResize();
            }
        });

        this._onGridChange = (event, items) => {
            if (!items || items.length === 0) return;
            if (!this._grid) return;
            const layout = this._grid.save(false);
            const layoutJson = JSON.stringify(layout);
            this.dispatchEvent(new CustomEvent('layout-changed', {
                detail: { layout: layoutJson },
                bubbles: true,
                composed: true
            }));
        };
        this._grid.on('change', this._onGridChange);
    }

    disconnectedCallback() {
        if (this._grid) {
            if (this._onGridChange) {
                this._grid.off('change', this._onGridChange);
            }
            this._grid.destroy(false);
            this._grid = null;
        }
        this._onGridChange = null;
    }

    makeWidget(widgetId) {
        if (!this._grid) return;
        const el = this.querySelector('[gs-id="' + widgetId + '"]');
        if (el) {
            this._grid.makeWidget(el);
        }
    }

    beforeRemoveWidget(widgetId) {
        if (!this._grid) return;
        const el = this.querySelector('[gs-id="' + widgetId + '"]');
        if (el) {
            this._grid.removeWidget(el, false, false);
        }
    }

    loadLayout(jsonString) {
        if (!this._grid) return;
        try {
            const items = JSON.parse(jsonString);
            if (!Array.isArray(items)) return;
            items.forEach(function(item) {
                if (!item.id) return;
                var el = this.querySelector('[gs-id="' + item.id + '"]');
                if (el) {
                    this._grid.update(el, {
                        x: item.x,
                        y: item.y,
                        w: item.w,
                        h: item.h
                    });
                }
            }.bind(this));
        } catch (e) {
            console.warn('GridstackBoard: failed to parse layout JSON', e);
        }
    }

    setEditable(editable) {
        // this._editable = editable;
        // if (!this._grid) return;
        // this._grid.enableMove(editable);
        // this._grid.enableResize(editable);
        // if (editable) {
        //     this.classList.add('gs-editable');
        // } else {
        //     this.classList.remove('gs-editable');
        // }
    }
}

customElements.define('gridstack-board', GridstackBoard);
