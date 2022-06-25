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

import walkingkooka.collect.set.Sets;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceVisitor;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;

import java.util.Map;
import java.util.Set;

/**
 * A {@link SpreadsheetExpressionReferenceVisitor} that visits all mappings, and aims to return only {@link SpreadsheetLabelName} that map to the given {@link SpreadsheetCellReference}.
 */
final class TreeMapSpreadsheetLabelStoreLabelsSpreadsheetExpressionReferenceVisitor extends SpreadsheetExpressionReferenceVisitor {

    static Set<SpreadsheetLabelMapping> gather(final Map<SpreadsheetLabelName, SpreadsheetLabelMapping> mappings,
                                               final SpreadsheetExpressionReference selection) {
        final TreeMapSpreadsheetLabelStoreLabelsSpreadsheetExpressionReferenceVisitor visitor = new TreeMapSpreadsheetLabelStoreLabelsSpreadsheetExpressionReferenceVisitor(
                mappings,
                selection
        );

        mappings.values()
                .forEach(visitor::acceptAndUpdateLabels);

        return Sets.readOnly(visitor.labels);
    }

    // VisibleForTesting
    TreeMapSpreadsheetLabelStoreLabelsSpreadsheetExpressionReferenceVisitor(final Map<SpreadsheetLabelName, SpreadsheetLabelMapping> mappings,
                                                                            final SpreadsheetExpressionReference selection) {
        super();

        this.filter = selection;
        this.mappings = mappings;
    }

    // VisibleForTesting
    void acceptAndUpdateLabels(final SpreadsheetLabelMapping mapping) {
        this.add = false;
        this.accept(mapping.reference());
        if (this.add) {
            this.labels.add(mapping);
        }
    }

    @Override
    protected void visit(final SpreadsheetCellReference reference) {
        this.add = this.filter.test(reference);
    }

    @Override
    protected void visit(final SpreadsheetLabelName label) {
        if (false == this.add) {
            final SpreadsheetLabelMapping mapping = this.mappings.get(label);
            if (null != mapping) {
                this.accept(mapping.reference());
            }
        }
    }

    @Override
    protected void visit(final SpreadsheetCellRange range) {
        this.add = this.add | range.cellStream()
                .anyMatch(filter);
    }

    /**
     * The source containing all mappings.
     */
    private final Map<SpreadsheetLabelName, SpreadsheetLabelMapping> mappings;

    /**
     * This filter will match label mappings as they are visited.
     */
    private final SpreadsheetExpressionReference filter;

    /**
     * The result being built.
     */
    private final Set<SpreadsheetLabelMapping> labels = Sets.ordered();

    /**
     * A flag that starts as false and becomes true if a mapping is matched by the filter.
     */
    private boolean add;

    @Override
    public String toString() {
        return this.labels.toString();
    }
}
