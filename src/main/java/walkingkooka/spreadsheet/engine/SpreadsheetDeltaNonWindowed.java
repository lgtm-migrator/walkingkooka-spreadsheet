/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.spreadsheet.engine;

import walkingkooka.ToStringBuilder;
import walkingkooka.collect.set.Sets;
import walkingkooka.predicate.Predicates;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.text.printer.IndentingPrinter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link SpreadsheetDelta} without any window/filtering.
 */
final class SpreadsheetDeltaNonWindowed extends SpreadsheetDelta {

    /**
     * Factory that creates a new {@link SpreadsheetDeltaNonWindowed} without copying or filtering the cells.
     */
    static SpreadsheetDeltaNonWindowed withNonWindowed(final Optional<SpreadsheetViewportSelection> selection,
                                                       final Set<SpreadsheetCell> cells,
                                                       final Set<SpreadsheetColumn> columns,
                                                       final Set<SpreadsheetLabelMapping> labels,
                                                       final Set<SpreadsheetRow> rows,
                                                       final Set<SpreadsheetCellReference> deletedCells,
                                                       final Map<SpreadsheetColumnReference, Double> columnWidths,
                                                       final Map<SpreadsheetRowReference, Double> rowHeights) {
        return new SpreadsheetDeltaNonWindowed(
                selection,
                cells,
                columns,
                labels,
                rows,
                deletedCells,
                columnWidths,
                rowHeights
        );
    }

    private SpreadsheetDeltaNonWindowed(final Optional<SpreadsheetViewportSelection> selection,
                                        final Set<SpreadsheetCell> cells,
                                        final Set<SpreadsheetColumn> columns,
                                        final Set<SpreadsheetLabelMapping> labels,
                                        final Set<SpreadsheetRow> rows,
                                        final Set<SpreadsheetCellReference> deletedCells,
                                        final Map<SpreadsheetColumnReference, Double> columnWidths,
                                        final Map<SpreadsheetRowReference, Double> rowHeights) {
        super(
                selection,
                cells,
                columns,
                labels,
                rows,
                deletedCells,
                columnWidths,
                rowHeights
        );
    }

    @Override
    SpreadsheetDelta replaceSelection(final Optional<SpreadsheetViewportSelection> selection) {
        return new SpreadsheetDeltaNonWindowed(
                selection,
                this.cells,
                this.columns,
                this.labels,
                this.rows,
                this.deletedCells,
                this.columnWidths,
                this.rowHeights
        );
    }

    @Override
    SpreadsheetDelta replaceCells(final Set<SpreadsheetCell> cells) {
        // cells have already been filtered by window
        return new SpreadsheetDeltaNonWindowed(
                this.selection,
                cells,
                this.columns,
                this.labels,
                this.rows,
                this.deletedCells,
                this.columnWidths,
                this.rowHeights
        );
    }

    @Override
    SpreadsheetDelta replaceColumns(final Set<SpreadsheetColumn> columns) {
        return new SpreadsheetDeltaNonWindowed(
                this.selection,
                this.cells,
                columns,
                this.labels,
                this.rows,
                this.deletedCells,
                this.columnWidths,
                this.rowHeights
        );
    }

    @Override
    SpreadsheetDelta replaceLabels(final Set<SpreadsheetLabelMapping> labels) {
        return new SpreadsheetDeltaNonWindowed(
                this.selection,
                this.cells,
                this.columns,
                labels,
                this.rows,
                this.deletedCells,
                this.columnWidths,
                this.rowHeights
        );
    }

    @Override
    SpreadsheetDelta replaceRows(final Set<SpreadsheetRow> rows) {
        return new SpreadsheetDeltaNonWindowed(
                this.selection,
                this.cells,
                this.columns,
                this.labels,
                rows,
                this.deletedCells,
                this.columnWidths,
                this.rowHeights
        );
    }

    @Override
    SpreadsheetDelta replaceDeletedCells(final Set<SpreadsheetCellReference> deletedCells) {
        return new SpreadsheetDeltaNonWindowed(
                this.selection,
                this.cells,
                this.columns,
                this.labels,
                this.rows,
                deletedCells,
                this.columnWidths,
                this.rowHeights
        );
    }

    @Override
    SpreadsheetDelta replaceColumnWidths(final Map<SpreadsheetColumnReference, Double> columnWidths) {
        return new SpreadsheetDeltaNonWindowed(
                this.selection,
                this.cells,
                this.columns,
                this.labels,
                this.rows,
                this.deletedCells,
                columnWidths,
                this.rowHeights
        );
    }

    @Override
    SpreadsheetDelta replaceRowHeights(final Map<SpreadsheetRowReference, Double> rowHeights) {
        return new SpreadsheetDeltaNonWindowed(
                this.selection,
                this.cells,
                this.columns,
                this.labels,
                this.rows,
                this.deletedCells,
                this.columnWidths,
                rowHeights
        );
    }

    /**
     * There is no window.
     */
    @Override
    public Optional<SpreadsheetCellRange> window() {
        return NO_WINDOW;
    }

    @Override
    Set<SpreadsheetCell> filterCells(final Set<SpreadsheetCell> cells) {
        return copy(cells);
    }

    @Override
    Set<SpreadsheetColumn> filterColumns(final Set<SpreadsheetColumn> columns) {
        return copy(columns);
    }

    @Override
    Set<SpreadsheetRow> filterRows(final Set<SpreadsheetRow> rows) {
        return copy(rows);
    }

    private <S> Set<S> copy(final Set<S> from) {
        final Set<S> copy = Sets.sorted();
        copy.addAll(from);
        return Sets.immutable(copy);
    }

    @Override
    Set<SpreadsheetCellReference> filterDeletedCells(final Set<SpreadsheetCellReference> deletedCells) {
        return filter(
                deletedCells,
                Predicates.always(),
                SpreadsheetCellReference::toRelative
        );
    }

    @Override
    Map<SpreadsheetColumnReference, Double> filterColumnWidths(final Map<SpreadsheetColumnReference, Double> columnWidths) {
        return filterMap(
                columnWidths
        );
    }

    @Override
    Map<SpreadsheetRowReference, Double> filterRowHeights(final Map<SpreadsheetRowReference, Double> rowHeights) {
        return filterMap(
                rowHeights
        );
    }

    private static <R extends SpreadsheetColumnOrRowReference> Map<R, Double> filterMap(final Map<R, Double> source) {
        return filterMap(
                source,
                Predicates.always()
        );
    }

    // TreePrintable.....................................................................................................

    @Override
    void printWindow(final IndentingPrinter printer) {
        // nop
    }

    // Object...........................................................................................................

    @Override
    void toStringWindow(final ToStringBuilder b) {
    }
}
