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

package walkingkooka.spreadsheet.reference.store;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.map.Maps;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetSelectionVisitorTesting;

import java.util.Map;

public final class TreeMapSpreadsheetLabelStoreLabelsSpreadsheetSelectionVisitorTest implements SpreadsheetSelectionVisitorTesting<TreeMapSpreadsheetLabelStoreLabelsSpreadsheetSelectionVisitor> {

    @Test
    public void testToString() {
        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");

        final SpreadsheetLabelName label1 = SpreadsheetSelection.labelName("ABCDEF123");
        final SpreadsheetLabelName label2 = SpreadsheetSelection.labelName("DEFGHI456");

        final Map<SpreadsheetLabelName, SpreadsheetLabelMapping> mappings = Maps.of(
                label1, label1.mapping(a1),
                label2, label2.mapping(a1)
        );

        final TreeMapSpreadsheetLabelStoreLabelsSpreadsheetSelectionVisitor visitor = new TreeMapSpreadsheetLabelStoreLabelsSpreadsheetSelectionVisitor(
                mappings,
                a1
        );

        mappings.values()
                .forEach(visitor::acceptAndUpdateLabels);

        this.toStringAndCheck(
                visitor,
                "[ABCDEF123=A1, DEFGHI456=A1]"
        );
    }

    @Override
    public TreeMapSpreadsheetLabelStoreLabelsSpreadsheetSelectionVisitor createVisitor() {
        return new TreeMapSpreadsheetLabelStoreLabelsSpreadsheetSelectionVisitor(null, null);
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public String typeNamePrefix() {
        return TreeMapSpreadsheetLabelStore.class.getSimpleName();
    }

    @Override
    public Class<TreeMapSpreadsheetLabelStoreLabelsSpreadsheetSelectionVisitor> type() {
        return TreeMapSpreadsheetLabelStoreLabelsSpreadsheetSelectionVisitor.class;
    }
}
