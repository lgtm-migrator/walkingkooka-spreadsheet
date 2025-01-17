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

package walkingkooka.spreadsheet;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.list.Lists;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetCellRangeStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ExpressionReferenceSpreadsheetCellReferencesBiConsumerTest implements ClassTesting2<ExpressionReferenceSpreadsheetCellReferencesBiConsumer>,
        ToStringTesting<ExpressionReferenceSpreadsheetCellReferencesBiConsumer> {

    @Test
    public void testWithNullLabelStoreFails() {
        assertThrows(NullPointerException.class, () -> ExpressionReferenceSpreadsheetCellReferencesBiConsumer.with(null, SpreadsheetCellRangeStores.fake()));
    }

    @Test
    public void testWithNullSpreadsheetCellRangeStoreFails() {
        assertThrows(NullPointerException.class, () -> ExpressionReferenceSpreadsheetCellReferencesBiConsumer.with(SpreadsheetLabelStores.fake(), null));
    }

    @Test
    public void testAcceptNullExpressionReferenceFails() {
        assertThrows(NullPointerException.class, () -> this.createBiConsumer().accept(null, (c) -> {
        }));
    }

    @Test
    public void testAcceptNullBiConsumerFails() {
        assertThrows(NullPointerException.class, () -> this.createBiConsumer().accept(this.cellB1(), null));
    }

    @Test
    public void testCell() {
        final SpreadsheetCellReference z99 = SpreadsheetSelection.parseCell("Z99");
        this.acceptAndCheck(z99, z99);
    }

    @Test
    public void testRange() {
        this.acceptAndCheck(this.rangeC1C2(), this.cellC1(), this.cellC2());
    }

    @Test
    public void testLabelToCell() {
        this.acceptAndCheck(this.labelB1(), this.cellB1());
    }

    @Test
    public void testLabelToUnknown() {
        this.acceptAndCheck(SpreadsheetSelection.labelName("unknown"));
    }

    @Test
    public void testLabelToRange() {
        this.acceptAndCheck(this.labelRangeC1D2(), this.cellC1(), this.cellC2());
    }

    private void acceptAndCheck(final SpreadsheetExpressionReference reference,
                                final SpreadsheetCellReference... references) {
        final List<SpreadsheetCellReference> actual = Lists.array();
        this.createBiConsumer().accept(reference, actual::add);
        this.checkEquals(Lists.of(references), actual);
    }

    @Test
    public void testToString() {
        final SpreadsheetLabelStore labelStore = SpreadsheetLabelStores.fake();
        final SpreadsheetCellRangeStore<SpreadsheetCellReference> rangeToCellStore = SpreadsheetCellRangeStores.fake();
        this.toStringAndCheck(ExpressionReferenceSpreadsheetCellReferencesBiConsumer.with(labelStore, rangeToCellStore),
                "ExpressionReference->Consumer<SpreadsheetCellReference>(" + labelStore + " " + rangeToCellStore + ")");
    }

    private ExpressionReferenceSpreadsheetCellReferencesBiConsumer createBiConsumer() {
        final SpreadsheetLabelStore labelStore = SpreadsheetLabelStores.treeMap();

        labelStore.save(SpreadsheetLabelMapping.with(labelB1(), this.cellB1()));
        labelStore.save(SpreadsheetLabelMapping.with(labelRangeC1D2(), this.rangeC1C2()));

        final SpreadsheetCellRangeStore<SpreadsheetCellReference> rangeToCellStore = SpreadsheetCellRangeStores.treeMap();
        rangeToCellStore.addValue(this.rangeC1C2(), this.cellC1());
        rangeToCellStore.addValue(this.rangeC1C2(), this.cellC2());

        return ExpressionReferenceSpreadsheetCellReferencesBiConsumer.with(SpreadsheetLabelStores.readOnly(labelStore),
                SpreadsheetCellRangeStores.readOnly(rangeToCellStore));
    }

    private SpreadsheetLabelName labelB1() {
        return SpreadsheetSelection.labelName("labelB1");
    }

    private SpreadsheetCellReference cellB1() {
        return SpreadsheetSelection.parseCell("B1");
    }

    private SpreadsheetLabelName labelRangeC1D2() {
        return SpreadsheetSelection.labelName("labelRangeC1D2");
    }

    private SpreadsheetCellRange rangeC1C2() {
        return this.cellC1().cellRange(this.cellC2());
    }

    private SpreadsheetCellReference cellC1() {
        return SpreadsheetSelection.parseCell("C1");
    }

    private SpreadsheetCellReference cellC2() {
        return SpreadsheetSelection.parseCell("C2");
    }

    @Override
    public Class<ExpressionReferenceSpreadsheetCellReferencesBiConsumer> type() {
        return ExpressionReferenceSpreadsheetCellReferencesBiConsumer.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
