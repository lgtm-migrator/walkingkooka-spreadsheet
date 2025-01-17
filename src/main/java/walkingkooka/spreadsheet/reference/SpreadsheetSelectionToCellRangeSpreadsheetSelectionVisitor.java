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

package walkingkooka.spreadsheet.reference;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * This visitor is used to turn any {@link SpreadsheetSelection} into a {@link SpreadsheetCellRange}.
 * <br>
 * Labels will be resolved until the final destination cell or cell-range is found. A single column wll be made into
 * a {@link SpreadsheetCellRange} that includes all cells for that column.
 */
final class SpreadsheetSelectionToCellRangeSpreadsheetSelectionVisitor extends SpreadsheetSelectionVisitor {

    static Optional<SpreadsheetCellRange> toCellRange(final SpreadsheetSelection selection,
                                                      final Function<SpreadsheetLabelName, Optional<SpreadsheetCellRange>> labelToCellRange) {
        Objects.requireNonNull(labelToCellRange, "labelToCellRange");

        final SpreadsheetSelectionToCellRangeSpreadsheetSelectionVisitor visitor = new SpreadsheetSelectionToCellRangeSpreadsheetSelectionVisitor(labelToCellRange);
        visitor.accept(selection);

        return Optional.ofNullable(
                visitor.cellRange
        );
    }

    private SpreadsheetSelectionToCellRangeSpreadsheetSelectionVisitor(final Function<SpreadsheetLabelName, Optional<SpreadsheetCellRange>> labelToCellRange) {
        this.labelToCellRange = labelToCellRange;
    }

    @Override
    protected void visit(final SpreadsheetCellRange cells) {
        this.cellRange = cells;
    }

    @Override
    protected void visit(final SpreadsheetCellReference reference) {
        this.cellRange = reference.cellRange(reference); // toCellRangeOrFail will result in StackOverflowError
    }

    @Override
    protected void visit(final SpreadsheetColumnReference column) {
        this.visit(column.toColumnRange());
    }

    @Override
    protected void visit(final SpreadsheetColumnReferenceRange columns) {
        this.cellRange = columns.setRowReferenceRange(SpreadsheetSelection.ALL_ROWS);
    }

    @Override
    protected void visit(final SpreadsheetLabelName label) {
        final Optional<SpreadsheetCellRange> maybeCellRange = this.labelToCellRange.apply(label);
        if (maybeCellRange.isPresent()) {
            this.accept(maybeCellRange.get());
        }
    }

    private final Function<SpreadsheetLabelName, Optional<SpreadsheetCellRange>> labelToCellRange;

    @Override
    protected void visit(final SpreadsheetRowReference row) {
        this.visit(row.toRowRange());
    }

    @Override
    protected void visit(final SpreadsheetRowReferenceRange rows) {
        this.cellRange = rows.setColumnReferenceRange(SpreadsheetSelection.ALL_COLUMNS);
    }

    /**
     * The final result or null if an unknown label was encountered.
     */
    private SpreadsheetCellRange cellRange;

    @Override
    public String toString() {
        return String.valueOf(this.cellRange);
    }
}
