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

package walkingkooka.spreadsheet.expression;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.math.DecimalNumberContext;
import walkingkooka.math.DecimalNumberContexts;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.tree.expression.ExpressionEvaluationContext;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.ExpressionReference;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.UnknownExpressionFunctionException;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetExpressionEvaluationContextTest implements SpreadsheetExpressionEvaluationContextTesting<BasicSpreadsheetExpressionEvaluationContext> {

    private final static SpreadsheetCellReference CELL_REFERENCE = SpreadsheetSelection.parseCell("B2");

    private final static Optional<SpreadsheetCell> CELL = Optional.of(
            CELL_REFERENCE.setFormula(SpreadsheetFormula.EMPTY.setText("=1+2"))
    );

    private final static SpreadsheetCellStore CELL_STORE = SpreadsheetCellStores.fake();

    private final static AbsoluteUrl SERVER_URL = Url.parseAbsolute("https://example.com");

    private final static SpreadsheetMetadata METADATA = SpreadsheetMetadata.EMPTY;

    private final static Function<FunctionExpressionName, ExpressionFunction<?, ExpressionEvaluationContext>> FUNCTIONS = (n) -> {
        Objects.requireNonNull(n, "name");
        throw new UnknownExpressionFunctionException(n);
    };

    private final static Function<SpreadsheetSelection, SpreadsheetSelection> RESOLVE_IF_LABEL = (s) -> {
        Objects.requireNonNull(s, "selection");
        throw new UnsupportedOperationException();
    };

    private final static Function<ExpressionReference, Optional<Optional<Object>>> REFERENCES = (r) -> {
        throw new UnsupportedOperationException();
    };

    private final static Supplier<LocalDateTime> NOW = LocalDateTime::now;

    // with.............................................................................................................

    @Test
    public void testWithNullCellFails() {
        this.withFails(
                null,
                CELL_STORE,
                SERVER_URL,
                METADATA,
                FUNCTIONS,
                REFERENCES,
                RESOLVE_IF_LABEL,
                NOW
        );
    }

    @Test
    public void testWithNullCellStoreFails() {
        this.withFails(
                CELL,
                null,
                SERVER_URL,
                METADATA,
                FUNCTIONS,
                REFERENCES,
                RESOLVE_IF_LABEL,
                NOW
        );
    }

    @Test
    public void testWithNullServerUrlFails() {
        this.withFails(
                CELL,
                CELL_STORE,
                null,
                METADATA,
                FUNCTIONS,
                REFERENCES,
                RESOLVE_IF_LABEL,
                NOW
        );
    }

    @Test
    public void testWithNullMetadataFails() {
        this.withFails(
                CELL,
                CELL_STORE,
                SERVER_URL,
                null,
                FUNCTIONS,
                REFERENCES,
                RESOLVE_IF_LABEL,
                NOW
        );
    }

    @Test
    public void testWithNullFunctionsFails() {
        this.withFails(
                CELL,
                CELL_STORE,
                SERVER_URL,
                METADATA,
                null,
                REFERENCES,
                RESOLVE_IF_LABEL,
                NOW
        );
    }

    @Test
    public void testWithNullReferencesFails() {
        this.withFails(
                CELL,
                CELL_STORE,
                SERVER_URL,
                METADATA,
                FUNCTIONS,
                null,
                RESOLVE_IF_LABEL,
                NOW
        );
    }

    @Test
    public void testWithNullResolveIfLabelFails() {
        this.withFails(
                CELL,
                CELL_STORE,
                SERVER_URL,
                METADATA,
                FUNCTIONS,
                REFERENCES,
                null,
                NOW
        );
    }

    @Test
    public void testWithNullNowFails() {
        this.withFails(
                CELL,
                CELL_STORE,
                SERVER_URL,
                METADATA,
                FUNCTIONS,
                REFERENCES,
                RESOLVE_IF_LABEL,
                null
        );
    }

    private void withFails(final Optional<SpreadsheetCell> cell,
                           final SpreadsheetCellStore cellStore,
                           final AbsoluteUrl serverUrl,
                           final SpreadsheetMetadata spreadsheetMetadata,
                           final Function<FunctionExpressionName, ExpressionFunction<?, ExpressionEvaluationContext>> functions,
                           final Function<ExpressionReference, Optional<Optional<Object>>> references,
                           final Function<SpreadsheetSelection, SpreadsheetSelection> resolveIfLabel,
                           final Supplier<LocalDateTime> now) {
        assertThrows(
                NullPointerException.class,
                () -> BasicSpreadsheetExpressionEvaluationContext.with(
                        cell,
                        cellStore,
                        serverUrl,
                        spreadsheetMetadata,
                        functions,
                        references,
                        resolveIfLabel,
                        now
                )
        );
    }

    // loadCell.........................................................................................................

    @Test
    public void testLoadCellCurrentCell() {
        this.loadCellAndCheck(
                this.createContext(SpreadsheetCellStores.fake()),
                CELL_REFERENCE,
                CELL
        );
    }

    @Test
    public void testLoadCell() {
        final SpreadsheetCellStore cellStore = SpreadsheetCellStores.treeMap();

        final SpreadsheetCellReference cellReference = SpreadsheetSelection.parseCell("Z999");
        final SpreadsheetCell cell = cellReference.setFormula(
                SpreadsheetFormula.EMPTY.setText("'9999")
        );
        cellStore.save(cell);

        this.loadCellAndCheck(
                this.createContext(cellStore),
                cellReference,
                Optional.of(cell)
        );
    }

    // parseExpression.....................................................................................................

    @Test
    public void testParseExpressionQuotedString() {
        final String text = "abc123";
        final String expression = '"' + text + '"';

        this.parseExpressionAndCheck(
                expression,
                SpreadsheetParserToken.text(
                        Lists.of(
                                SpreadsheetParserToken.doubleQuoteSymbol("\"", "\""),
                                SpreadsheetParserToken.textLiteral(text, text),
                                SpreadsheetParserToken.doubleQuoteSymbol("\"", "\"")
                        ),
                        expression
                )
        );
    }

    @Test
    public void testParseExpressionNumber() {
        final String text = "123";

        this.parseExpressionAndCheck(
                text,
                SpreadsheetParserToken.number(
                        Lists.of(
                                SpreadsheetParserToken.digits(text, text)
                        ),
                        text
                )
        );
    }

    private final static char DECIMAL = '.';

    @Test
    public void testParseExpressionNumber2() {
        final String text = "1" + DECIMAL + "5";

        this.parseExpressionAndCheck(
                text,
                SpreadsheetParserToken.number(
                        Lists.of(
                                SpreadsheetParserToken.digits("1", "1"),
                                SpreadsheetParserToken.decimalSeparatorSymbol("" + DECIMAL, "" + DECIMAL),
                                SpreadsheetParserToken.digits("5", "5")
                        ),
                        text
                )
        );
    }

    @Test
    public void testParseExpressionAdditionExpression() {
        final String text = "1+2";

        this.parseExpressionAndCheck(
                text,
                SpreadsheetParserToken.addition(
                        Lists.of(
                                SpreadsheetParserToken.number(
                                        Lists.of(
                                                SpreadsheetParserToken.digits("1", "1")
                                        ),
                                        "1"
                                ),
                                SpreadsheetParserToken.plusSymbol("+", "+"),
                                SpreadsheetParserToken.number(
                                        Lists.of(
                                                SpreadsheetParserToken.digits("2", "2")
                                        ),
                                        "2"
                                )
                        ),
                        text
                )
        );
    }

    // ExpressionEvaluationContextTesting................................................................................

    @Override
    public void testEvaluateExpressionUnknownFunctionNameFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testFunctionNullFunctionNameFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testFunctionUnknownFunctionNameFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testEvaluateFunctionNullFunctionNameFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testEvaluateFunctionNullParametersFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testReferenceNullReferenceFails() {
        throw new UnsupportedOperationException();
    }

    // HasConverter.....................................................................................................

    @Test
    public void testConverter() {
        final BasicSpreadsheetExpressionEvaluationContext context = this.createContext();

        final ExpressionNumber from = context.expressionNumberKind().create(123);
        final String to = context.convertOrFail(from, String.class);

        this.checkEquals(
                to,
                context.converter().convertOrFail(from, String.class, context),
                () -> "converter with context and context convertOrFail should return the same"
        );
    }

    // DecimalNumberContextTesting.....................................................................................

    @Override
    public String currencySymbol() {
        return DECIMAL_NUMBER_CONTEXT.currencySymbol();
    }

    @Override
    public char decimalSeparator() {
        return DECIMAL_NUMBER_CONTEXT.decimalSeparator();
    }

    @Override
    public String exponentSymbol() {
        return "e";
    }

    @Override
    public char groupingSeparator() {
        return DECIMAL_NUMBER_CONTEXT.groupingSeparator();
    }

    @Override
    public MathContext mathContext() {
        return DECIMAL_NUMBER_CONTEXT.mathContext();
    }

    @Override
    public char negativeSign() {
        return DECIMAL_NUMBER_CONTEXT.negativeSign();
    }

    @Override
    public char percentageSymbol() {
        return DECIMAL_NUMBER_CONTEXT.percentageSymbol();
    }

    @Override
    public char positiveSign() {
        return DECIMAL_NUMBER_CONTEXT.positiveSign();
    }

    private final static DecimalNumberContext DECIMAL_NUMBER_CONTEXT = DecimalNumberContexts.american(MathContext.DECIMAL32);

    // ClassTesting......................................................................................................

    @Override
    public Class<BasicSpreadsheetExpressionEvaluationContext> type() {
        return BasicSpreadsheetExpressionEvaluationContext.class;
    }

    @Override
    public BasicSpreadsheetExpressionEvaluationContext createContext() {
        return this.createContext(CELL_STORE);
    }

    public BasicSpreadsheetExpressionEvaluationContext createContext(final SpreadsheetCellStore cellStore) {
        return BasicSpreadsheetExpressionEvaluationContext.with(
                CELL,
                cellStore,
                SERVER_URL,
                SpreadsheetMetadata.EMPTY.set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                        .loadFromLocale()
                        .set(SpreadsheetMetadataPropertyName.PRECISION, DECIMAL_NUMBER_CONTEXT.mathContext().getPrecision())
                        .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, DECIMAL_NUMBER_CONTEXT.mathContext().getRoundingMode())
                        .set(SpreadsheetMetadataPropertyName.DATETIME_OFFSET, 0L)
                        .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 20)
                        .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, ExpressionNumberKind.DEFAULT)
                        .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.parseTextFormatPattern("@"))
                        .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 20),
                FUNCTIONS,
                REFERENCES,
                RESOLVE_IF_LABEL,
                NOW
        );
    }
}
