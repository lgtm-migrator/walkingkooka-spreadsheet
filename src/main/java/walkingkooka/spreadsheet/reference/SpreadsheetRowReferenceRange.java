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

import walkingkooka.collect.Range;

/**
 * Holds a row range.
 */
@SuppressWarnings("lgtm[java/inconsistent-equals-and-hashcode]")
public final class SpreadsheetRowReferenceRange extends SpreadsheetColumnOrRowReferenceRange<SpreadsheetRowReference>
        implements Comparable<SpreadsheetRowReferenceRange> {

    /**
     * Factory that creates a {@link SpreadsheetRowReferenceRange}
     */
    static SpreadsheetRowReferenceRange with(final Range<SpreadsheetRowReference> range) {
        SpreadsheetRangeRangeVisitor.check(range);

        return new SpreadsheetRowReferenceRange(range);
    }

    /**
     * Private ctor
     */
    private SpreadsheetRowReferenceRange(final Range<SpreadsheetRowReference> range) {
        super(range);
    }

    /**
     * Creates a {@link SpreadsheetCellRange} combining this row range and the given column range.
     */
    public SpreadsheetCellRange setColumnReferenceRange(final SpreadsheetColumnReferenceRange columnReferenceRange) {
        checkColumnReferenceRange(columnReferenceRange);

        return columnReferenceRange.setRowReferenceRange(this);
    }

    public SpreadsheetRowReferenceRange setRange(final Range<SpreadsheetRowReference> range) {
        return this.setRange0(range);
    }

    @Override
    SpreadsheetRowReferenceRange replace(final Range<SpreadsheetRowReference> range) {
        return with(range);
    }

    @Override
    public boolean testCellRange(final SpreadsheetCellRange range) {
        return this.end().compareTo(range.begin().row()) >= 0 &&
                this.begin().compareTo(range.end().row()) <= 0;
    }

    @Override
    public boolean test(final SpreadsheetCellReference reference) {
        return this.range.test(reference.row());
    }

    @Override
    public SpreadsheetRowReferenceRange toRelative() {
        final SpreadsheetRowReferenceRange relative = this.begin()
                .toRelative()
                .spreadsheetRowRange(this.end()
                        .toRelative());
        return this.equals(relative) ?
                this :
                relative;
    }

    @Override
    void accept(final SpreadsheetSelectionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    boolean canBeEqual(final Object other) {
        return other instanceof SpreadsheetRowReferenceRange;
    }

    @Override
    public int compareTo(final SpreadsheetRowReferenceRange other) {
        throw new UnsupportedOperationException();
    }
}
