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

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.HashCodeEqualsDefinedTesting2;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.TypeNameTesting;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelectionAnchor;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class SpreadsheetDeltaTestCase<D extends SpreadsheetDelta> implements ClassTesting2<D>,
        TypeNameTesting<D>,
        HashCodeEqualsDefinedTesting2<D>,
        ToStringTesting<D>,
        JsonNodeMarshallingTesting<D>,
        TreePrintableTesting {

    SpreadsheetDeltaTestCase() {
        super();
    }

    // selection........................................................................................................

    @Test
    public final void testSetViewportSelectionSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setViewportSelection(this.viewportSelection()));
    }

    @Test
    public final void testSetViewportSelectionDifferent() {
        final D before = this.createSpreadsheetDelta();

        final Optional<SpreadsheetViewportSelection> different = this.differentViewportSelection();

        final SpreadsheetDelta after = before.setViewportSelection(different);
        assertNotSame(before, after);

        this.checkViewportSelection(after, different);
        this.checkCells(after);
        this.checkColumns(after);
        this.checkLabels(after, before.labels());
        this.checkRows(after);

        this.checkDeletedCells(after);
        this.checkDeletedColumns(after);
        this.checkDeletedRows(after);

        this.checkColumnWidths(after);
        this.checkRowHeights(after);

        this.checkViewportSelection(before);
    }

    // cells............................................................................................................

    @Test
    public final void testCellsReadOnly() {
        final D delta = this.createSpreadsheetDelta();
        final Set<SpreadsheetCell> cells = delta.cells();

        assertThrows(UnsupportedOperationException.class, () -> cells.add(this.a1()));

        this.checkCells(delta, this.cells());
    }

    @Test
    public final void testSetCellsSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setCells(this.cells()));
    }

    @Test
    public final void testSetCellsDifferent() {
        final D before = this.createSpreadsheetDelta();

        final Set<SpreadsheetCell> different = Sets.of(
                SpreadsheetSelection.parseCell("E1")
                        .setFormula(
                                SpreadsheetFormula.EMPTY
                                        .setText("99")
                        )
        );

        final SpreadsheetDelta after = before.setCells(different);
        assertNotSame(before, after);
        this.checkCells(after, different);
        this.checkLabels(after, before.labels());
    }

    @Test
    public final void testSetCellsSorted() {
        final SpreadsheetCell a1 = cell("A1", "1");
        final SpreadsheetCell b2 = cell("B2", "2");

        final SpreadsheetDelta delta = this.createSpreadsheetDelta()
                .setCells(Sets.of(b2, a1));

        this.checkCells(
                delta,
                Sets.of(a1, b2)
        );
        this.checkEquals(Lists.of(a1, b2), new ArrayList<>(delta.cells()));
    }

    // columns............................................................................................................

    @Test
    public final void testColumnsReadOnly() {
        final D delta = this.createSpreadsheetDelta();
        final Set<SpreadsheetColumn> columns = delta.columns();

        assertThrows(
                UnsupportedOperationException.class,
                () -> columns.add(this.a())
        );

        this.checkColumns(delta, this.columns());
    }

    @Test
    public final void testSetColumnsSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setColumns(this.columns()));
    }

    @Test
    public final void testSetColumnsDifferent() {
        final D before = this.createSpreadsheetDelta();

        final Set<SpreadsheetColumn> different = Sets.of(
                SpreadsheetSelection.parseColumn("E").column()
        );

        final SpreadsheetDelta after = before.setColumns(different);
        assertNotSame(before, after);
        this.checkColumns(after, different);
        this.checkLabels(after, before.labels());
    }

    @Test
    public final void testSetColumnsSorted() {
        final SpreadsheetColumn a = a();
        final SpreadsheetColumn b = b();
        final SpreadsheetColumn c = c();

        final SpreadsheetDelta delta = this.createSpreadsheetDelta()
                .setColumns(
                        Sets.of(b, c, a));

        this.checkColumns(
                delta,
                Sets.of(a, b, c)
        );

        this.checkEquals(
                Lists.of(a, b, c),
                new ArrayList<>(delta.columns())
        );
    }

    @Test
    public final void testSetColumnsWithHiddenFiltersCells() {
        final SpreadsheetDelta delta = this.createSpreadsheetDelta();
        this.checkCells(
                delta,
                Sets.of(
                        this.a1(),
                        this.b2(),
                        this.c3()
                )
        );

        final Set<SpreadsheetColumn> hiddenA = Sets.of(
                this.a().setHidden(true)
        );

        final SpreadsheetDelta after = delta.setColumns(hiddenA);
        this.checkCells(
                after,
                Sets.of(
                        this.b2(),
                        this.c3()
                )
        );
        this.checkColumns(
                after,
                hiddenA
        );
    }

    // labels.....................................................................................................

    @Test
    public final void testlabelsReadOnly() {
        final D delta = this.createSpreadsheetDelta();
        final Set<SpreadsheetLabelMapping> labels = delta.labels();

        assertThrows(UnsupportedOperationException.class, () -> labels.add(this.label1b().mapping(this.a1().reference())));

        this.checkLabels(delta, this.labels());
    }

    @Test
    public final void testSetLabelsNull() {
        final D delta = this.createSpreadsheetDelta();
        assertThrows(NullPointerException.class, () -> delta.setLabels(null));
    }

    @Test
    public final void testSetLabelsSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setLabels(this.labels()));
    }

    @Test
    public void testSetLabelsEmpty() {
        final D before = this.createSpreadsheetDelta();
        final Set<SpreadsheetLabelMapping> different = SpreadsheetDelta.NO_LABELS;

        final SpreadsheetDelta after = before.setLabels(different);
        assertNotSame(before, after);
        this.checkLabels(after, different);

        this.checkCells(after);
        this.checkColumns(after);
        this.checkRows(after);

        this.checkDeletedCells(after);
        this.checkDeletedColumns(after);
        this.checkDeletedRows(after);

        this.checkColumnWidths(after);
        this.checkRowHeights(after);
    }

    @Test
    public final void testSetLabelsDifferent() {
        final D before = this.createSpreadsheetDelta();
        final Set<SpreadsheetLabelMapping> different = this.differentLabels();

        final SpreadsheetDelta after = before.setLabels(different);
        assertNotSame(before, after);

        this.checkLabels(after, different);

        this.checkCells(after);
        this.checkColumns(after);
        this.checkRows(after);

        this.checkDeletedCells(after);
        this.checkDeletedColumns(after);
        this.checkDeletedRows(after);

        this.checkColumnWidths(after);
        this.checkRowHeights(after);
    }

    // rows............................................................................................................

    @Test
    public final void testRowsReadOnly() {
        final D delta = this.createSpreadsheetDelta();
        final Set<SpreadsheetRow> rows = delta.rows();

        assertThrows(
                UnsupportedOperationException.class,
                () -> rows.add(this.row1())
        );

        this.checkRows(delta, this.rows());
    }

    @Test
    public final void testSetRowsSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setRows(this.rows()));
    }

    @Test
    public final void testSetRowsDifferent() {
        final D before = this.createSpreadsheetDelta();

        final Set<SpreadsheetRow> different = this.differentRows();
        final SpreadsheetDelta after = before.setRows(different);
        assertNotSame(before, after);
        this.checkRows(after, different);

        this.checkCells(after);
        this.checkColumns(after);
        this.checkLabels(after);

        this.checkDeletedCells(after);
        this.checkDeletedColumns(after);
        this.checkDeletedRows(after);

        this.checkColumnWidths(after);
        this.checkRowHeights(after);
    }

    @Test
    public final void testSetRowsSorted() {
        final SpreadsheetRow a = row1();
        final SpreadsheetRow b = row2();
        final SpreadsheetRow c = row3();

        final SpreadsheetDelta delta = this.createSpreadsheetDelta()
                .setRows(
                        Sets.of(b, c, a));

        this.checkRows(
                delta,
                Sets.of(a, b, c)
        );

        this.checkEquals(
                Lists.of(a, b, c),
                new ArrayList<>(delta.rows())
        );
    }

    @Test
    public final void testSetRowsWithHiddenFiltersCells() {
        final SpreadsheetDelta delta = this.createSpreadsheetDelta();
        this.checkCells(
                delta,
                Sets.of(
                        this.a1(),
                        this.b2(),
                        this.c3()
                )
        );

        final Set<SpreadsheetRow> hiddenRow1 = Sets.of(
                this.row1().setHidden(true)
        );

        final SpreadsheetDelta after = delta.setRows(hiddenRow1);
        this.checkCells(
                after,
                Sets.of(
                        this.b2(),
                        this.c3()
                )
        );
        this.checkRows(
                after,
                hiddenRow1
        );
    }

    // deletedCells.....................................................................................................

    @Test
    public final void testDeletedCellsReadOnly() {
        final D delta = this.createSpreadsheetDelta();
        final Set<SpreadsheetCellReference> deletedCells = delta.deletedCells();

        assertThrows(UnsupportedOperationException.class, () -> deletedCells.add(this.a1().reference()));

        this.checkDeletedCells(delta, this.deletedCells());
    }

    @Test
    public final void testSetDeletedCellsSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setDeletedCells(this.deletedCells()));
    }

    @Test
    public final void testSetDeletedCellsDifferent() {
        final D before = this.createSpreadsheetDelta();

        final Set<SpreadsheetCellReference> different = this.differentDeletedCells();

        final SpreadsheetDelta after = before.setDeletedCells(different);
        assertNotSame(before, after);

        this.checkDeletedCells(after, different);

        this.checkCells(after);
        this.checkColumns(after);
        this.checkLabels(after, before.labels());
        this.checkRows(after);

        this.checkDeletedColumns(after);
        this.checkDeletedRows(after);

        this.checkColumnWidths(after);
        this.checkRowHeights(after);
    }

    @Test
    public final void testSetDeletedCellsSorted() {
        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");

        final SpreadsheetDelta delta = this.createSpreadsheetDelta()
                .setDeletedCells(Sets.of(b2, a1));

        this.checkDeletedCells(
                delta,
                Sets.of(a1, b2)
        );
        this.checkEquals(Lists.of(a1, b2), new ArrayList<>(delta.deletedCells()));
    }

    @Test
    public final void testSetDeletedCellsAllRelative() {
        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("$A$1");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B$2");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C$3");
        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("D4");

        final SpreadsheetDelta delta = this.createSpreadsheetDelta()
                .setDeletedCells(Sets.of(b2, a1, d4, c3));

        this.checkDeletedCells(
                delta,
                Sets.of(
                        a1.toRelative(),
                        b2.toRelative(),
                        c3.toRelative(),
                        d4.toRelative()
                )
        );
        this.checkEquals(Lists.of(a1.toRelative(), b2.toRelative(), c3.toRelative(), d4), new ArrayList<>(delta.deletedCells()));
    }

    // deletedColumns.....................................................................................................

    @Test
    public final void testDeletedColumnsReadOnly() {
        final D delta = this.createSpreadsheetDelta();
        final Set<SpreadsheetColumnReference> deletedColumns = delta.deletedColumns();

        assertThrows(
                UnsupportedOperationException.class,
                () -> deletedColumns.add(this.a1().reference().column())
        );

        this.checkDeletedColumns(delta, this.deletedColumns());
    }

    @Test
    public final void testSetDeletedColumnsSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setDeletedColumns(this.deletedColumns()));
    }

    @Test
    public final void testSetDeletedColumnsDifferent() {
        final D before = this.createSpreadsheetDelta();

        final Set<SpreadsheetColumnReference> different = this.differentDeletedColumns();

        final SpreadsheetDelta after = before.setDeletedColumns(different);
        assertNotSame(before, after);

        this.checkDeletedColumns(after, different);

        this.checkColumns(after);
        this.checkColumns(after);
        this.checkLabels(after, before.labels());
        this.checkRows(after);

        this.checkColumnWidths(after);
        this.checkRowHeights(after);
    }

    @Test
    public final void testSetDeletedColumnsSorted() {
        final SpreadsheetColumnReference a = SpreadsheetSelection.parseColumn("A");
        final SpreadsheetColumnReference b = SpreadsheetSelection.parseColumn("B");

        final SpreadsheetDelta delta = this.createSpreadsheetDelta()
                .setDeletedColumns(Sets.of(b, a));

        this.checkDeletedColumns(
                delta,
                Sets.of(a, b)
        );
        this.checkEquals(
                Lists.of(a, b),
                new ArrayList<>(delta.deletedColumns())
        );
    }

    @Test
    public final void testSetDeletedColumnsAllRelative() {
        final SpreadsheetColumnReference a = SpreadsheetSelection.parseColumn("$A");
        final SpreadsheetColumnReference b = SpreadsheetSelection.parseColumn("B");

        final SpreadsheetDelta delta = this.createSpreadsheetDelta()
                .setDeletedColumns(Sets.of(b, a));

        this.checkDeletedColumns(
                delta,
                Sets.of(
                        a.toRelative(),
                        b
                )
        );
        this.checkEquals(
                Lists.of(
                        a.toRelative(),
                        b
                ),
                new ArrayList<>(delta.deletedColumns())
        );
    }

    // deletedRows.....................................................................................................

    @Test
    public final void testDeletedRowsReadOnly() {
        final D delta = this.createSpreadsheetDelta();
        final Set<SpreadsheetRowReference> deletedRows = delta.deletedRows();

        assertThrows(
                UnsupportedOperationException.class,
                () -> deletedRows.add(this.a1().reference().row())
        );

        this.checkDeletedRows(delta, this.deletedRows());
    }

    @Test
    public final void testSetDeletedRowsSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setDeletedRows(this.deletedRows()));
    }

    @Test
    public final void testSetDeletedRowsDifferent() {
        final D before = this.createSpreadsheetDelta();

        final Set<SpreadsheetRowReference> different = this.differentDeletedRows();

        final SpreadsheetDelta after = before.setDeletedRows(different);
        assertNotSame(before, after);

        this.checkDeletedRows(after, different);

        this.checkRows(after);
        this.checkRows(after);
        this.checkLabels(after, before.labels());
        this.checkRows(after);

        this.checkDeletedCells(after);
        this.checkDeletedColumns(after);

        this.checkColumnWidths(after);
        this.checkRowHeights(after);
    }

    @Test
    public final void testSetDeletedRowsSorted() {
        final SpreadsheetRowReference a1 = SpreadsheetSelection.parseRow("1");
        final SpreadsheetRowReference b2 = SpreadsheetSelection.parseRow("2");

        final SpreadsheetDelta delta = this.createSpreadsheetDelta()
                .setDeletedRows(Sets.of(b2, a1));

        this.checkDeletedRows(
                delta,
                Sets.of(a1, b2)
        );
        this.checkEquals(
                Lists.of(a1, b2),
                new ArrayList<>(delta.deletedRows())
        );
    }

    @Test
    public final void testSetDeletedRowsAllRelative() {
        final SpreadsheetRowReference row1 = SpreadsheetSelection.parseRow("$1");
        final SpreadsheetRowReference row2 = SpreadsheetSelection.parseRow("2");

        final SpreadsheetDelta delta = this.createSpreadsheetDelta()
                .setDeletedRows(Sets.of(row1, row2));

        this.checkDeletedRows(
                delta,
                Sets.of(
                        row1.toRelative(),
                        row2.toRelative()
                )
        );
        this.checkEquals(
                Lists.of(
                        row1.toRelative(),
                        row2
                ),
                new ArrayList<>(delta.deletedRows())
        );
    }
    // setColumnWidths...............................................................................................

    @Test
    public final void testSetColumnWidthsNullFails() {
        assertThrows(NullPointerException.class, () -> this.createSpreadsheetDelta().setColumnWidths(null));
    }

    @Test
    public final void testSetColumnWidthsSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setColumnWidths(this.columnWidths()));
    }

    @Test
    public final void testSetColumnWidthsDifferent() {
        final D delta = this.createSpreadsheetDelta();
        final Map<SpreadsheetColumnReference, Double> different = this.differentColumnWidths();

        final SpreadsheetDelta after = delta.setColumnWidths(different);
        assertNotSame(delta, after);
        this.checkColumnWidths(after, different);

        this.checkCells(after);
        this.checkColumns(after);
        this.checkLabels(after);
        this.checkRows(after);

        this.checkRowHeights(after);
    }

    // setRowHeights...............................................................................................

    @Test
    public final void testSetRowHeightsNullFails() {
        assertThrows(NullPointerException.class, () -> this.createSpreadsheetDelta().setRowHeights(null));
    }

    @Test
    public final void testSetRowHeightsSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setRowHeights(this.rowHeights()));
    }

    @Test
    public final void testSetRowHeightsDifferent() {
        final D delta = this.createSpreadsheetDelta();

        final Map<SpreadsheetRowReference, Double> different = this.differentRowHeights();
        final SpreadsheetDelta after = delta.setRowHeights(different);
        assertNotSame(delta, after);
        this.checkRowHeights(after, different);

        this.checkCells(after);
        this.checkColumns(after);
        this.checkLabels(after);
        this.checkRows(after);

        this.checkDeletedCells(after);
        this.checkColumnWidths(after);
    }

    // setWindow........................................................................................................

    @Test
    public final void testSetWindowsSame() {
        final D delta = this.createSpreadsheetDelta();
        assertSame(delta, delta.setWindow(this.window()));
    }

    @Test
    public final void testSetDifferentWindow() {
        final D before = this.createSpreadsheetDelta();

        final Set<SpreadsheetCellRange> window = SpreadsheetSelection.parseWindow("A1:Z9999");
        this.checkNotEquals(window, this.window());

        final SpreadsheetDelta after = before.setWindow(window);

        this.checkCells(after);
        this.checkColumns(after);
        this.checkRows(after);
        this.checkWindow(after, window);

        this.checkCells(before);
        this.checkColumns(before);
        this.checkRows(before);
    }

    @Test
    public final void testSetDifferentWindowWithLabels() {
        final D before = this.createSpreadsheetDelta();

        final Set<SpreadsheetCellRange> window = SpreadsheetSelection.parseWindow("A1:Z9999");
        this.checkNotEquals(window, this.window());

        final SpreadsheetDelta after = before.setWindow(window);

        this.checkCells(after);
        this.checkColumns(after);
        this.checkRows(after);
        this.checkWindow(after, window);

        this.checkCells(before);
        this.checkColumns(before);
        this.checkRows(before);
    }

    // unmarshall.......................................................................................................

    @Test
    public final void testUnmarshallViewportSelectionWithCell() {
        this.unmarshallViewportSelectionAndCheck(
                SpreadsheetSelection.parseCell("B2")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    @Test
    public final void testUnmarshallViewportSelectionWithCellRange() {
        this.unmarshallViewportSelectionAndCheck(
                SpreadsheetSelection.parseCellRange("B2:C3")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.TOP_LEFT)
        );
    }

    @Test
    public final void testUnmarshallViewportSelectionWithColumn() {
        this.unmarshallViewportSelectionAndCheck(
                SpreadsheetSelection.parseColumn("B")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    @Test
    public final void testUnmarshallViewportSelectionWithColumnRange() {
        this.unmarshallViewportSelectionAndCheck(
                SpreadsheetSelection.parseColumnRange("B:CD")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.RIGHT)
        );
    }

    @Test
    public final void testUnmarshallViewportSelectionWithRow() {
        this.unmarshallViewportSelectionAndCheck(
                SpreadsheetSelection.parseRow("2")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    @Test
    public final void testUnmarshallViewportSelectionWithRowRange() {
        this.unmarshallViewportSelectionAndCheck(
                SpreadsheetSelection.parseRowRange("2:34")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.BOTTOM)
        );
    }

    @Test
    public final void testUnmarshallNullViewportSelection() {
        this.unmarshallViewportSelectionAndCheck(null);
    }

    abstract void unmarshallViewportSelectionAndCheck(final SpreadsheetViewportSelection viewportSelection);

    // equals...........................................................................................................

    @Test
    public final void testDifferentSelection() {
        final Optional<SpreadsheetViewportSelection> viewportSelection = this.differentViewportSelection();
        this.checkNotEquals(
                this.viewportSelection(),
                viewportSelection,
                "viewportSelection() and differentViewportSelection() must be un equal"
        );

        this.checkNotEquals(this.createSpreadsheetDelta().setViewportSelection(viewportSelection));
    }

    @Test
    public final void testDifferentCells() {
        final Set<SpreadsheetCell> cells = this.differentCells();
        this.checkNotEquals(this.cells(), cells, "cells() and differentCells() must be un equal");

        this.checkNotEquals(this.createSpreadsheetDelta(cells));
    }

    @Test
    public final void testDifferentColumns() {
        final Set<SpreadsheetColumn> columns = this.differentColumns();

        this.checkNotEquals(
                this.columns(),
                columns,
                "columns() and differentColumns() must be un equal"
        );

        this.checkNotEquals(
                this.createSpreadsheetDelta().setColumns(columns)
        );
    }

    @Test
    public final void testDifferentLabels() {
        final Set<SpreadsheetLabelMapping> labels = this.differentLabels();
        this.checkNotEquals(this.labels(), labels, "labels() and differentLabels() must be un equal");

        final SpreadsheetDelta delta = this.createSpreadsheetDelta();

        this.checkNotEquals(
                delta,
                delta.setLabels(labels)
        );
    }

    @Test
    public final void testDifferentRows() {
        final Set<SpreadsheetRow> rows = this.differentRows();

        this.checkNotEquals(
                this.rows(),
                rows,
                "rows() and differentRows() must be un equal"
        );

        this.checkNotEquals(
                this.createSpreadsheetDelta().setRows(rows)
        );
    }

    @Test
    public final void testDifferentDeletedLabels() {
        final Set<SpreadsheetCellReference> deletedCells = this.differentDeletedCells();
        this.checkNotEquals(this.labels(), deletedCells, "deletedCells() and differentDeletedCells() must be un equal");

        this.checkNotEquals(this.createSpreadsheetDelta().setDeletedCells(deletedCells));
    }

    @Test
    public final void testDifferentColumnWidths() {
        final Map<SpreadsheetColumnReference, Double> columnWidths = this.differentColumnWidths();
        this.checkNotEquals(this.columnWidths(), columnWidths, "columnWidths() and differentColumnWidths() must be un equal");

        this.checkNotEquals(this.createSpreadsheetDelta().setColumnWidths(columnWidths));
    }

    @Test
    public final void testDifferentRowHeights() {
        final Map<SpreadsheetRowReference, Double> rowHeights = this.differentRowHeights();
        this.checkNotEquals(this.rowHeights(), rowHeights, "rowHeights() and differentRowHeights() must be un equal");

        this.checkNotEquals(this.createSpreadsheetDelta().setRowHeights(rowHeights));
    }

    @Test
    public final void testDifferentWindow() {
        final Set<SpreadsheetCellRange> differentWindow = this.differentWindow();
        this.checkNotEquals(this.window(), differentWindow, "window() and differentWindow() must be un equal");

        this.checkNotEquals(this.createSpreadsheetDelta().setWindow(differentWindow));
    }

    // helpers..........................................................................................................

    final D createSpreadsheetDelta() {
        return this.createSpreadsheetDelta(this.cells());
    }

    abstract D createSpreadsheetDelta(final Set<SpreadsheetCell> cells);

    // selection........................................................................................................

    final Optional<SpreadsheetViewportSelection> viewportSelection() {
        return Optional.of(
                SpreadsheetSelection.parseCellRange("A1:B2")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.BOTTOM_RIGHT)
        );
    }

    final Optional<SpreadsheetViewportSelection> differentViewportSelection() {
        return Optional.of(
                SpreadsheetSelection.parseCell("C3")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    final void checkViewportSelection(final SpreadsheetDelta delta) {
        this.checkViewportSelection(delta, this.viewportSelection());
    }

    final void checkViewportSelection(final SpreadsheetDelta delta,
                                      final Optional<SpreadsheetViewportSelection> viewportSelection) {
        this.checkEquals(viewportSelection, delta.viewportSelection(), "viewportSelection");
    }

    // cells............................................................................................................

    final Set<SpreadsheetCell> cells() {
        return Sets.of(
                this.a1(),
                this.b2(),
                this.c3()
        );
    }

    final Set<SpreadsheetCell> differentCells() {
        return Sets.of(
                this.a1().setFormula(SpreadsheetFormula.EMPTY.setText("'different A1")),
                this.b2().setFormula(SpreadsheetFormula.EMPTY.setText("'different B2")),
                this.c3().setFormula(SpreadsheetFormula.EMPTY.setText("'different C3"))
        );
    }

    final Set<SpreadsheetCell> cells0(final String... cellReferences) {
        return Arrays.stream(cellReferences)
                .map(r -> this.cell(r, "55"))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    final SpreadsheetCell a1() {
        return this.cell("A1", "1");
    }

    final SpreadsheetCell b2() {
        return this.cell("B2", "2");
    }

    final SpreadsheetCell c3() {
        return this.cell("C3", "3");
    }

    final SpreadsheetCell cell(final String cellReference,
                               final String formulaText) {
        return SpreadsheetSelection.parseCell(cellReference)
                .setFormula(
                        SpreadsheetFormula.EMPTY
                                .setText(formulaText)
                );
    }

    final void checkCells(final SpreadsheetDelta delta) {
        this.checkCells(delta, this.cells());
    }

    final void checkCells(final SpreadsheetDelta delta,
                          final Set<SpreadsheetCell> cells) {
        this.checkEquals(cells, delta.cells(), "cells");

        assertThrows(
                UnsupportedOperationException.class,
                () -> delta.cells()
                        .add(this.cell("ZZ99", "read only"))
        );
    }

    // columns.........................................................................................................

    final Set<SpreadsheetColumn> columns() {
        return Sets.of(
                this.a(),
                this.b(),
                this.c(),
                this.hiddenD()
        );
    }

    final Set<SpreadsheetColumn> differentColumns() {
        return Sets.of(
                this.a(),
                this.b(),
                this.c(),
                this.hiddenD().setHidden(false)
        );
    }

    final SpreadsheetColumn a() {
        return this.column("A");
    }

    final SpreadsheetColumn b() {
        return this.column("B");
    }

    final SpreadsheetColumn c() {
        return this.column("C");
    }

    final SpreadsheetColumn hiddenD() {
        return this.column("d")
                .setHidden(true);
    }

    final SpreadsheetColumn column(final String columnReference) {
        return SpreadsheetSelection.parseColumn(columnReference)
                .column();
    }

    final void checkColumns(final SpreadsheetDelta delta) {
        this.checkColumns(delta, this.columns());
    }

    final void checkColumns(final SpreadsheetDelta delta,
                            final Set<SpreadsheetColumn> columns) {
        this.checkEquals(columns, delta.columns(), "columns");

        assertThrows(
                UnsupportedOperationException.class,
                () -> delta.columns()
                        .add(this.column("Z"))
        );
    }

    // labels......................................................................................................

    final Set<SpreadsheetLabelMapping> labels() {
        return Sets.of(
                this.label1a().mapping(this.a1().reference()),
                this.label1b().mapping(this.a1().reference()),
                this.label2().mapping(this.b2().reference()),
                this.label3().mapping(SpreadsheetSelection.parseCellRange("C3:D4"))
        );
    }

    final Set<SpreadsheetLabelMapping> differentLabels() {
        final SpreadsheetCellReference a1 = this.a1().reference();

        return Sets.of(
                this.label1a().mapping(a1),
                this.label1b().mapping(a1),
                this.label2().mapping(a1),
                this.label3().mapping(a1)
        );
    }

    final void checkLabels(final SpreadsheetDelta delta) {
        this.checkLabels(delta, this.labels());
    }

    final void checkLabels(final SpreadsheetDelta delta,
                           final Set<SpreadsheetLabelMapping> labels) {
        this.checkEquals(labels, delta.labels(), "labels");
        assertThrows(UnsupportedOperationException.class, () -> delta.labels()
                .add(SpreadsheetLabelName.labelName("LabelZ").mapping(SpreadsheetSelection.parseCell("Z9")))
        );
    }

    final SpreadsheetLabelName label1a() {
        return SpreadsheetLabelName.labelName("LabelA1A");
    }

    final SpreadsheetLabelName label1b() {
        return SpreadsheetLabelName.labelName("LabelA1B");
    }

    final SpreadsheetLabelName label2() {
        return SpreadsheetLabelName.labelName("LabelB2");
    }

    final SpreadsheetLabelName label3() {
        return SpreadsheetLabelName.labelName("LabelC3");
    }

    // rows.........................................................................................................

    final Set<SpreadsheetRow> rows() {
        return Sets.of(
                this.row1(),
                this.row2(),
                this.row3(),
                this.hiddenRow4()
        );
    }

    final Set<SpreadsheetRow> differentRows() {
        return Sets.of(
                this.row1(),
                this.row2(),
                this.row3(),
                this.hiddenRow4().setHidden(false)
        );
    }

    final SpreadsheetRow row1() {
        return this.row("1");
    }

    final SpreadsheetRow row2() {
        return this.row("2");
    }

    final SpreadsheetRow row3() {
        return this.row("3");
    }

    final SpreadsheetRow hiddenRow4() {
        return this.row("4")
                .setHidden(true);
    }

    final SpreadsheetRow row(final String rowReference) {
        return SpreadsheetSelection.parseRow(rowReference)
                .row();
    }

    final void checkRows(final SpreadsheetDelta delta) {
        this.checkRows(delta, this.rows());
    }

    final void checkRows(final SpreadsheetDelta delta,
                         final Set<SpreadsheetRow> rows) {
        this.checkEquals(rows, delta.rows(), "rows");

        assertThrows(
                UnsupportedOperationException.class,
                () -> delta.rows()
                        .add(this.row("999"))
        );
    }
    // deletedCells.....................................................................................................

    final Set<SpreadsheetCellReference> deletedCells() {
        return Sets.of(
                SpreadsheetSelection.parseCell("C1"),
                SpreadsheetSelection.parseCell("C2")
        );
    }

    final Set<SpreadsheetCellReference> differentDeletedCells() {
        return Set.of(SpreadsheetSelection.parseCell("C2"));
    }

    final void checkDeletedCells(final SpreadsheetDelta delta) {
        this.checkDeletedCells(delta, this.deletedCells());
    }

    final void checkDeletedCells(final SpreadsheetDelta delta,
                                 final Set<SpreadsheetCellReference> cells) {
        this.checkEquals(cells,
                delta.deletedCells(),
                "deletedCells");
        assertThrows(UnsupportedOperationException.class, () -> delta.deletedCells().add(null));
    }

    // deletedColumns.....................................................................................................

    final Set<SpreadsheetColumnReference> deletedColumns() {
        return Sets.of(
                SpreadsheetSelection.parseColumn("C"),
                SpreadsheetSelection.parseColumn("D")
        );
    }

    final Set<SpreadsheetColumnReference> differentDeletedColumns() {
        return Set.of(SpreadsheetSelection.parseColumn("E"));
    }

    final void checkDeletedColumns(final SpreadsheetDelta delta) {
        this.checkDeletedColumns(delta, this.deletedColumns());
    }

    final void checkDeletedColumns(final SpreadsheetDelta delta,
                                   final Set<SpreadsheetColumnReference> columns) {
        this.checkEquals(columns,
                delta.deletedColumns(),
                "deletedColumns");
        assertThrows(
                UnsupportedOperationException.class,
                () -> delta.deletedColumns().add(null)
        );
    }

    // deletedRows.....................................................................................................

    final Set<SpreadsheetRowReference> deletedRows() {
        return Sets.of(
                SpreadsheetSelection.parseRow("3"),
                SpreadsheetSelection.parseRow("4")
        );
    }

    final Set<SpreadsheetRowReference> differentDeletedRows() {
        return Set.of(
                SpreadsheetSelection.parseRow("5")
        );
    }

    final void checkDeletedRows(final SpreadsheetDelta delta) {
        this.checkDeletedRows(delta, this.deletedRows());
    }

    final void checkDeletedRows(final SpreadsheetDelta delta,
                                final Set<SpreadsheetRowReference> rows) {
        this.checkEquals(rows,
                delta.deletedRows(),
                "deletedRows");
        assertThrows(
                UnsupportedOperationException.class,
                () -> delta.deletedRows().add(null)
        );
    }

    // columnWidths..................................................................................................

    final Map<SpreadsheetColumnReference, Double> columnWidths() {
        return Maps.of(
                SpreadsheetSelection.parseColumn("A"),
                50.0
        );
    }

    final static JsonNode COLUMN_WIDTHS_JSON = JsonNode.parse("{\"A\": 50.0}");

    final Map<SpreadsheetColumnReference, Double> differentColumnWidths() {
        return Maps.of(
                SpreadsheetSelection.parseColumn("B"),
                999.0
        );
    }

    final void checkColumnWidths(final SpreadsheetDelta delta) {
        checkColumnWidths(delta, this.columnWidths());
    }

    final void checkColumnWidths(final SpreadsheetDelta delta,
                                 final Map<SpreadsheetColumnReference, Double> columnWidths) {
        this.checkEquals(
                columnWidths,
                delta.columnWidths(),
                "columnWidths"
        );
    }

    // rowHeights.......................................................................................................

    final Map<SpreadsheetRowReference, Double> rowHeights() {
        return Maps.of(
                SpreadsheetSelection.parseRow("1"),
                75.0
        );
    }

    final static JsonNode ROW_HEIGHTS_JSON = JsonNode.parse("{\"1\": 75.0}");

    final Map<SpreadsheetRowReference, Double> differentRowHeights() {
        return Maps.of(
                SpreadsheetSelection.parseRow("2"),
                999.0
        );
    }

    final void checkRowHeights(final SpreadsheetDelta delta) {
        checkRowHeights(delta, this.rowHeights());
    }

    final void checkRowHeights(final SpreadsheetDelta delta,
                               final Map<SpreadsheetRowReference, Double> rowHeights) {
        this.checkEquals(
                rowHeights,
                delta.rowHeights(),
                "rowHeights"
        );
    }

    final void checkWindow(final SpreadsheetDelta delta,
                           final Set<SpreadsheetCellRange> window) {
        this.checkEquals(window, delta.window(), "window");
    }

    // window...........................................................................................................

    abstract Set<SpreadsheetCellRange> window();

    final Set<SpreadsheetCellRange> differentWindow() {
        return SpreadsheetSelection.parseWindow("A1:Z99");
    }

    final void checkWindow(final SpreadsheetDelta delta) {
        this.checkWindow(delta, this.window());
    }

    // ClassTesting.....................................................................................................

    @Override
    public final JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    // TypeNameTesting..................................................................................................


    @Override
    public final String typeNamePrefix() {
        return SpreadsheetDelta.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "";
    }

    // HashCodeDefinedTesting............................................................................................

    @Override
    public final D createObject() {
        return this.createSpreadsheetDelta();
    }

    // JsonNodeMarshallingTesting...........................................................................................

    @Override
    public final D createJsonNodeMarshallingValue() {
        return this.createSpreadsheetDelta();
    }

    final JsonNode viewportSelectionJson() {
        final JsonNodeMarshallContext context = this.marshallContext();

        return context.marshall(this.viewportSelection().get());
    }

    final JsonNode cellsJson() {
        final JsonNodeMarshallContext context = this.marshallContext();

        JsonObject object = JsonNode.object();
        object = cellsJson0(this.a1(), context, object);
        object = cellsJson0(this.b2(), context, object);
        object = cellsJson0(this.c3(), context, object);

        return object;
    }

    private static JsonObject cellsJson0(final SpreadsheetCell cell,
                                         final JsonNodeMarshallContext context,
                                         final JsonObject object) {
        JsonObject updated = object;
        for (Map.Entry<JsonPropertyName, JsonNode> propertyAndValue : context.marshall(cell)
                .objectOrFail()
                .asMap()
                .entrySet()) {
            updated = updated.set(propertyAndValue.getKey(), propertyAndValue.getValue());
        }
        return updated;
    }

    final JsonNode columnsJson() {
        final JsonNodeMarshallContext context = this.marshallContext();

        JsonObject object = JsonNode.object();
        object = columnsJson0(this.a(), context, object);
        object = columnsJson0(this.b(), context, object);
        object = columnsJson0(this.c(), context, object);
        object = columnsJson0(this.hiddenD(), context, object);

        return object;
    }

    private static JsonObject columnsJson0(final SpreadsheetColumn column,
                                           final JsonNodeMarshallContext context,
                                           final JsonObject object) {
        JsonObject updated = object;
        for (Map.Entry<JsonPropertyName, JsonNode> propertyAndValue : context.marshall(column)
                .objectOrFail()
                .asMap()
                .entrySet()) {
            updated = updated.set(propertyAndValue.getKey(), propertyAndValue.getValue());
        }
        return updated;
    }

    final JsonNode labelsJson() {
        return this.marshallContext()
                .marshallCollection(
                        this.labels()
                );
    }

    final JsonNode rowsJson() {
        final JsonNodeMarshallContext context = this.marshallContext();

        JsonObject object = JsonNode.object();
        object = rowsJson0(this.row1(), context, object);
        object = rowsJson0(this.row2(), context, object);
        object = rowsJson0(this.row3(), context, object);
        object = rowsJson0(this.hiddenRow4(), context, object);

        return object;
    }

    private static JsonObject rowsJson0(final SpreadsheetRow row,
                                        final JsonNodeMarshallContext context,
                                        final JsonObject object) {
        JsonObject updated = object;
        for (Map.Entry<JsonPropertyName, JsonNode> propertyAndValue : context.marshall(row)
                .objectOrFail()
                .asMap()
                .entrySet()) {
            updated = updated.set(propertyAndValue.getKey(), propertyAndValue.getValue());
        }
        return updated;
    }

    final JsonNode deletedCellsJson() {
        return JsonNode.string("C1,C2");
    }

    final JsonNode deletedColumnsJson() {
        return JsonNode.string("C,D");
    }

    final JsonNode deletedRowsJson() {
        return JsonNode.string("3,4");
    }

    @Override
    public final D unmarshall(final JsonNode jsonNode,
                              final JsonNodeUnmarshallContext context) {
        return Cast.to(SpreadsheetDelta.unmarshall(jsonNode, context));
    }
}
