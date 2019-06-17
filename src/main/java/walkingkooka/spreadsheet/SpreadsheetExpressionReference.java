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

import walkingkooka.compare.Comparators;
import walkingkooka.compare.Range;
import walkingkooka.spreadsheet.parser.SpreadsheetParsers;
import walkingkooka.test.HashCodeEqualsDefined;
import walkingkooka.tree.expression.ExpressionReference;
import walkingkooka.tree.json.HasJsonNode;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonNodeException;
import walkingkooka.tree.json.JsonObjectNode;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/**
 * Base class for all Spreadsheet {@link ExpressionReference}
 */
abstract public class SpreadsheetExpressionReference implements ExpressionReference, HashCodeEqualsDefined, HasJsonNode {

    /**
     * A comparator that orders {@link SpreadsheetLabelName} before {@link SpreadsheetCellReference}.
     */
    public final static Comparator<SpreadsheetExpressionReference> COMPARATOR = SpreadsheetExpressionReferenceComparator.INSTANCE;

    // modes used by isCellReference
    private final static int MODE_COLUMN = 0;
    private final static int MODE_ROW = MODE_COLUMN + 1;
    private final static int MODE_FAIL = MODE_ROW + 1;

    /**
     * Tests if the {@link String name} is a valid cell reference.
     */
    public static boolean isCellReference(final String name) {
        int mode = MODE_COLUMN; // -1 too long or contains invalid char
        int column = 0;
        int row = 0;

        // AB11 max row, max column
        final int length = name.length();
        for (int i = 0; i < length; i++) {
            final char c = name.charAt(i);

            // try and parseCellReference into column + row
            if (MODE_COLUMN == mode) {
                final int digit = SpreadsheetParsers.valueFromDigit(c);
                if (-1 != digit) {
                    column = column * SpreadsheetColumnReference.RADIX + digit;
                    if (column >= SpreadsheetColumnReference.MAX) {
                        mode = MODE_FAIL;
                        break; // column is too big cant be a cell reference.
                    }
                    continue;
                }
                mode = MODE_ROW;
            }
            if (MODE_ROW == mode) {
                final int digit = Character.digit(c, SpreadsheetRowReference.RADIX);
                if (-1 != digit) {
                    row = SpreadsheetRowReference.RADIX * row + digit;
                    if (row >= SpreadsheetRowReference.MAX) {
                        mode = MODE_FAIL;
                        break; // row is too big cant be a cell reference.
                    }
                    continue;
                }
                mode = MODE_FAIL;
                break;
            }
        }

        // ran out of characters still checking row must be a valid cell reference.
        return MODE_ROW == mode;
    }

    // sub class factories..............................................................................................

    /**
     * {@see SpreadsheetCellReference}
     */
    public static SpreadsheetCellReference cellReference(final SpreadsheetColumnReference column,
                                                         final SpreadsheetRowReference row) {
        return SpreadsheetCellReference.with(column, row);
    }

    /**
     * {@see SpreadsheetLabelName}
     */
    public static SpreadsheetLabelName labelName(final String name) {
        return SpreadsheetLabelName.with(name);
    }

    // parse............................................................................................................

    /**
     * Parsers the given text into a {@link SpreadsheetExpressionReference}
     */
    public static SpreadsheetExpressionReference parse(final String text) {
        Objects.requireNonNull(text, "text");

        return SpreadsheetLabelName.isCellReference(text) ?
                parseCellReference(text) :
                labelName(text);
    }

    /**
     * Parsers a range of cell referencs.
     */
    public static Range<SpreadsheetCellReference> parseCellReferenceRange(final String text) {
        return SpreadsheetCellReference.parseCellReferenceRange0(text);
    }

    /**
     * Parsers the text expecting a valid {@link SpreadsheetCellReference} or fails.
     */
    public static SpreadsheetCellReference parseCellReference(final String text) {
        return SpreadsheetCellReference.parseCellReference0(text);
    }

    /**
     * Package private to limit sub classing.
     */
    SpreadsheetExpressionReference() {
        super();
    }

    @Override
    public final boolean equals(final Object other) {
        return this == other ||
                this.canBeEqual(other) &&
                        this.equals0(other);
    }

    abstract boolean canBeEqual(final Object other);

    abstract boolean equals0(final Object other);

    /**
     * Invoked by {@link SpreadsheetExpressionReferenceComparator} using double dispatch
     * to compare two {@link SpreadsheetExpressionReference}. Each sub class will use double dispatch which will invoke
     * either of the #compare0 methods. saving the need for instanceof checks.
     */
    abstract int compare(final SpreadsheetExpressionReference other);

    abstract int compare0(final SpreadsheetCellReference other);

    abstract int compare0(final SpreadsheetLabelName other);

    /**
     * Labels come before references, used as the result when a label compares with a reference.
     */
    final static int LABEL_COMPARED_WITH_CELL_RESULT = Comparators.LESS;

    // HasJsonNode......................................................................................................

    /**
     * Attempts to convert a {@link JsonNode} into a {@link SpreadsheetExpressionReference}.
     */
    public static SpreadsheetExpressionReference fromJsonNode(final JsonNode node) {
        return fromJsonNode0(node, SpreadsheetExpressionReference::parse);
    }

    /**
     * Accepts a json string and returns a {@link SpreadsheetCellReference} or fails.
     */
    public static SpreadsheetCellReference fromJsonNodeCellReference(final JsonNode node) {
        return fromJsonNode0(node,
                SpreadsheetExpressionReference::parseCellReference);
    }

    /**
     * Accepts a json string and returns a {@link SpreadsheetLabelName} or fails.
     */
    public static SpreadsheetLabelName fromJsonNodeLabelName(final JsonNode node) {
        return fromJsonNode0(node, SpreadsheetExpressionReference::labelName);
    }

    /**
     * Generic helper that tries to convert the node into a string and call a parse method.
     */
    private static <R extends SpreadsheetExpressionReference> R fromJsonNode0(final JsonNode node,
                                                                              final Function<String, R> parse) {
        Objects.requireNonNull(node, "node");

        try {
            return parse.apply(node.stringValueOrFail());
        } catch (final JsonNodeException cause) {
            throw new IllegalArgumentException(cause.getMessage(), cause);
        }
    }

    /**
     * The json form of this object is also {@link #toString()}
     */
    @Override
    public final JsonNode toJsonNode() {
        return JsonObjectNode.string(this.toString());
    }

    static {
        HasJsonNode.register("spreadsheet-cell-reference",
                SpreadsheetCellReference::fromJsonNodeCellReference,
                SpreadsheetCellReference.class);
        HasJsonNode.register("spreadsheet-label-name",
                SpreadsheetLabelName::fromJsonNodeLabelName,
                SpreadsheetLabelName.class);
    }
}
