window.Vaadin.Flow.multiMonthCrossSelectionUtils = {

    // register the multi month selection for the given FC calendar instance
    register: function (calendar) {
        let element = calendar.el;

        // state object
        calendar.__multiMonth = {};

        calendar.__multiMonth.mouseDownListener = e => {
            let startCell = this.findTdFromEvent(e);
            if (startCell && startCell.dataset && startCell.dataset.date) {
                let lastHoveredCell = startCell;
                this.markSelectedCells(calendar.__multiMonth, startCell);

                // update selection on mouse move
                calendar.__multiMonth.mouseMoveListener = e => {
                    let hoveredCell = this.findTdFromEvent(e);
                    if (hoveredCell !== lastHoveredCell) {
                        this.markSelectedCells(calendar.__multiMonth, startCell, hoveredCell);
                        lastHoveredCell = hoveredCell;
                    }
                };

                // selects the hovered cells and clears the listeners
                calendar.__multiMonth.mouseUpListener = e => {
                    this.unmarkSelectedCells(calendar.__multiMonth);

                    if (lastHoveredCell) {
                        let startDate = new Date(startCell.dataset.date);
                        let endDate = new Date(lastHoveredCell.dataset.date);

                        // swap dates if necessary
                        if (startDate > endDate) {
                            let tmp = startDate;
                            startDate = endDate;
                            endDate = tmp;
                        }

                        // increase end date by one day to match fc behavior of excluding the end date
                        endDate.setDate(endDate.getDate() + 1);

                        calendar.select(startDate, endDate);
                    } else {
                        calendar.select(startCell.dataset.date);
                    }

                    window.removeEventListener("mousemove", calendar.__multiMonth.mouseMoveListener);
                    window.removeEventListener("mouseup", calendar.__multiMonth.mouseUpListener);
                }

                window.addEventListener("mousemove", calendar.__multiMonth.mouseMoveListener);
                window.addEventListener("mouseup", calendar.__multiMonth.mouseUpListener);
            }
        };

        element.addEventListener("mousedown", calendar.__multiMonth.mouseDownListener);

        // maps the calendar's date cells to their dates to allow easier access on mouse move
        // will be refreshed on each period change
        calendar.setOption("datesSet", function (eventInfo) {
            let view = eventInfo.view;
            // generate dates between two dates

            let map = new Map();
            element.querySelectorAll("td[data-date]").forEach(td => {
                map.set(td.dataset.date, td);
            });

            calendar.__multiMonth.dateCellMap = map;
            calendar.__multiMonth.selectedCells = [];

        });
    },

    markSelectedCells(state, startCell, hoveredCell = startCell) {
        let cellMap = state.dateCellMap;

        this.unmarkSelectedCells(state);

        // iterate over all dates between startCell and hoveredCell
        let fromDate = new Date(startCell.dataset.date);
        let toDate = new Date(hoveredCell.dataset.date);

        if (fromDate > toDate) {
            let tmp = fromDate;
            fromDate = toDate;
            toDate = tmp;
        }

        let date = new Date(fromDate);
        while (date <= toDate) {
            let td = cellMap.get(date.toISOString().slice(0, 10));
            if (td) {
                let highlight = document.createElement("div");
                // highlight.classList.add("multi-month-highlight");

                // TODO move to a css class
                highlight.style.backgroundColor = "var(--fc-highlight-color)";
                highlight.style.zIndex = "3";
                highlight.style.position = "absolute";
                highlight.style.top = "0";
                highlight.style.left = "0";
                highlight.style.width = "100%";
                highlight.style.height = "100%";

                td.children[0].appendChild(highlight);
                td.__multiMonthHighlight = highlight;

                state.selectedCells.push(td);
            }

            date.setDate(date.getDate() + 1);
        }
    },

    unmarkSelectedCells: function (state) {
        state.selectedCells.forEach(td => {
            if (td.__multiMonthHighlight) {
                td.__multiMonthHighlight.remove();
                delete td.__multiMonthHighlight;
            }
        });
        state.selectedCells = [];
    },

    // tries to find the td element from the given dom event
    findTdFromEvent(e) {
        if (e.target.tagName === "TD" && e.target.dataset && e.target.dataset.date) { // unlikely but not impossible
            return e.target;
        }

        let composedPath = e.composedPath();
        for (let i = 0; i < 6 && i < composedPath.length; i++) { // prevent to deep searches up the hierarchy
            if (composedPath[i].tagName === "TD" && composedPath[i].dataset && composedPath[i].dataset.date) {
                return composedPath[i];
            }

        }
        return undefined;
    },

}