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

import walkingkooka.Context;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetText;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.Expression;

import java.util.Optional;

/**
 * Context that accompanies a value format, holding local sensitive attributes such as the decimal point character.
 */
public interface SpreadsheetEngineContext extends Context {

    /**
     * Returns the current {@link SpreadsheetMetadata}
     */
    SpreadsheetMetadata metadata();

    /**
     * ]]
     * <p>
     * Resolves a {@link SpreadsheetExpressionReference} to a {@link SpreadsheetCellReference}.
     */
    SpreadsheetCellReference resolveCellReference(final SpreadsheetExpressionReference reference);

    /**
     * Parses the formula into an {@link Expression}.
     */
    SpreadsheetParserToken parseFormula(final String formula);

    /**
     * Evaluates the expression into a value.
     */
    Object evaluate(final Expression node, final Optional<SpreadsheetCellReference> cell);

    /**
     * Accepts a pattern and returns the equivalent {@link SpreadsheetFormatter}.
     */
    SpreadsheetFormatter parsePattern(final String pattern);

    /**
     * Formats the given value using the provided formatter.
     */
    Optional<SpreadsheetText> format(final Object value,
                                     final SpreadsheetFormatter formatter);

    /**
     * Getter that returns the {@link SpreadsheetStoreRepository} for this spreadsheet.
     */
    SpreadsheetStoreRepository storeRepository();
}
