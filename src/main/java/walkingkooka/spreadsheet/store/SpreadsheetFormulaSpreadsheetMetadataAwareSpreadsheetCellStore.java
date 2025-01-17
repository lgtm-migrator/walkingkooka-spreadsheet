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

package walkingkooka.spreadsheet.store;

import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetErrorKind;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.text.cursor.TextCursors;
import walkingkooka.text.cursor.parser.ParserReporters;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionEvaluationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A {@link SpreadsheetCellStore} that tries to parse any formula text into an {@link Expression} when necessary for any
 * cells that are saved. When cells are loaded, the {@link SpreadsheetFormula} text is updated using the {@link Expression}.
 * Most other methods simply delegate without modification to the wrapped {@link SpreadsheetCellStore}.
 */
final class SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore implements SpreadsheetCellStore {

    static SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore with(final SpreadsheetCellStore store,
                                                                               final SpreadsheetMetadata metadata,
                                                                               final Supplier<LocalDateTime> now) {
        Objects.requireNonNull(store, "store");
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(now, "now");

        return store instanceof SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore ?
                setMetadata(
                        (SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore) store,
                        metadata,
                        now
                ) :
                new SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore(
                        store,
                        metadata,
                        now
                );
    }

    /**
     * If the {@link SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore} has a different {@link SpreadsheetMetadata}
     * create with the wrapped store and new metadata.
     */
    private static SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore setMetadata(
            final SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore store,
            final SpreadsheetMetadata metadata,
            final Supplier<LocalDateTime> now) {
        return metadata.equals(store.metadata) ?
                store :
                new SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore(store.store, metadata, now);
    }

    private SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore(final SpreadsheetCellStore store,
                                                                           final SpreadsheetMetadata metadata,
                                                                           final Supplier<LocalDateTime> now) {
        this.store = store;
        this.metadata = metadata;
        this.now = now;
    }

    @Override
    public Optional<SpreadsheetCell> load(final SpreadsheetCellReference cellReference) {
        return this.store.load(cellReference).map(this::fixFormulaText);
    }

    // save begin.........................................................................................................s

    @Override
    public SpreadsheetCell save(final SpreadsheetCell cell) {
        Objects.requireNonNull(cell, "cell");

        return this.fixFormulaText(
                this.store.save(
                        this.ensureFormulaHasToken(cell)
                )
        );
    }

    /**
     * If the {@link SpreadsheetFormula#token()} or {@link SpreadsheetFormula#expression()} are missing, parsing the text
     * and rebuilding the expression is performed. This has the side effect that the value/error will also be cleared.
     */
    private SpreadsheetCell ensureFormulaHasToken(final SpreadsheetCell cell) {
        SpreadsheetFormula formula = cell.formula();
        final String text = formula.text();

        SpreadsheetCell result = cell;
        if (!text.isEmpty()) {
            // any value or error will be lost if token/expression is updated
            SpreadsheetParserToken token = formula.token()
                    .orElse(null);
            try {
                if (null == token) {
                    token = this.parseFormulaTextExpression(text);
                    formula = formula
                            .setToken(Optional.of(token));
                }
                if (null != token) {
                    formula = formula.setText(token.text());
                    formula = formula.setExpression(
                            token.toExpression(this.expressionEvaluationContext())
                    ); // also clears value/error
                }
            } catch (final Exception failed) {
                formula = formula.setValue(
                        Optional.of(
                                SpreadsheetErrorKind.translate(failed)
                        )
                );
            }
            result = cell.setFormula(formula);
        }

        return result;
    }

    /**
     * Parses the formula text into an {@link SpreadsheetParserToken}.
     */
    private SpreadsheetParserToken parseFormulaTextExpression(final String text) {
        final SpreadsheetMetadata metadata = this.metadata;

        return this.metadata.parser()
                .orFailIfCursorNotEmpty(ParserReporters.basic())
                .parse(
                        TextCursors.charSequence(text),
                        metadata.parserContext(this.now)
                ).orElse(null)
                .cast(SpreadsheetParserToken.class);
    }

    // save end.........................................................................................................

    @Override
    public Runnable addSaveWatcher(final Consumer<SpreadsheetCell> remover) {
        return this.store.addSaveWatcher(remover);
    }

    @Override
    public void delete(final SpreadsheetCellReference cellReference) {
        this.store.delete(cellReference);
    }

    @Override
    public Runnable addDeleteWatcher(final Consumer<SpreadsheetCellReference> remover) {
        return this.store.addDeleteWatcher(remover);
    }

    @Override
    public int count() {
        return this.store.count();
    }

    @Override
    public Set<SpreadsheetCellReference> ids(final int from,
                                             final int count) {
        return this.store.ids(from, count);
    }

    @Override
    public List<SpreadsheetCell> values(final SpreadsheetCellReference cellReference,
                                        final int count) {
        return this.fixFormulaTextList(this.store.values(cellReference, count));
    }

    @Override
    public int rows() {
        return this.store.rows();
    }

    @Override
    public int columns() {
        return this.store.columns();
    }

    @Override
    public Set<SpreadsheetCell> row(final SpreadsheetRowReference row) {
        return this.fixFormulaTextSet(this.store.row(row));
    }

    @Override
    public Set<SpreadsheetCell> column(final SpreadsheetColumnReference column) {
        return this.fixFormulaTextSet(this.store.column(column));
    }

    @Override
    public double maxColumnWidth(final SpreadsheetColumnReference column) {
        return this.store.maxColumnWidth(column);
    }

    @Override
    public double maxRowHeight(final SpreadsheetRowReference row) {
        return this.store.maxRowHeight(row);
    }

    // helpers that do the formula tokenization/text thing..............................................................

    private List<SpreadsheetCell> fixFormulaTextList(final List<SpreadsheetCell> cells) {
        return cells.stream()
                .map(this::fixFormulaText)
                .collect(Collectors.toCollection(Lists::array));
    }

    private Set<SpreadsheetCell> fixFormulaTextSet(final Set<SpreadsheetCell> cells) {
        return cells.stream()
                .map(this::fixFormulaText)
                .collect(Collectors.toCollection(Sets::ordered));
    }

    private SpreadsheetCell fixFormulaText(final SpreadsheetCell cell) {
        SpreadsheetCell fixed = cell;

        SpreadsheetFormula formula = cell.formula();

        SpreadsheetParserToken token = formula.token()
                .orElse(null);
        if (null != token) {
            token = SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStoreSpreadsheetParserTokenVisitor.update(
                    token,
                    this.metadata,
                    this.now
            );
            final String text = token.text();
            if (!formula.text().equals(text)) {
                // if the text is different update token and expression
                fixed = fixed.setFormula(
                        formula.setText(token.text())
                                .setToken(Optional.of(token))
                                .setExpression(token.toExpression(this.expressionEvaluationContext()))
                );
            }
        }

        return fixed;
    }

    private ExpressionEvaluationContext expressionEvaluationContext() {
        return SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStoreExpressionEvaluationContext.with(
                this.metadata,
                this.now
        );
    }

    // @VisibleForTesting
    final SpreadsheetCellStore store;

    // @VisibleForTesting
    final SpreadsheetMetadata metadata;

    final Supplier<LocalDateTime> now;

    @Override
    public String toString() {
        return this.metadata + " " + this.store;
    }
}
