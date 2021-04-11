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
import walkingkooka.Either;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.color.Color;
import walkingkooka.convert.Converter;
import walkingkooka.convert.Converters;
import walkingkooka.datetime.DateTimeContexts;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetCellBox;
import walkingkooka.spreadsheet.SpreadsheetCellFormat;
import walkingkooka.spreadsheet.SpreadsheetCoordinates;
import walkingkooka.spreadsheet.SpreadsheetDescription;
import walkingkooka.spreadsheet.SpreadsheetError;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.conditionalformat.SpreadsheetConditionalFormattingRule;
import walkingkooka.spreadsheet.format.FakeSpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatException;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatters;
import walkingkooka.spreadsheet.format.SpreadsheetText;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePatterns;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserContexts;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.parser.SpreadsheetParsers;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReferenceOrLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetRangeStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetRangeStores;
import walkingkooka.spreadsheet.store.FakeSpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.text.CharSequences;
import walkingkooka.text.cursor.TextCursors;
import walkingkooka.text.cursor.parser.ParserReporters;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionEvaluationContexts;
import walkingkooka.tree.expression.ExpressionEvaluationException;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FakeExpressionEvaluationContext;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;
import walkingkooka.tree.expression.function.FakeExpressionFunction;
import walkingkooka.tree.expression.function.UnknownExpressionFunctionException;
import walkingkooka.tree.text.FontStyle;
import walkingkooka.tree.text.FontWeight;
import walkingkooka.tree.text.Length;
import walkingkooka.tree.text.TextDecorationLine;
import walkingkooka.tree.text.TextNode;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;
import walkingkooka.tree.text.TextStylePropertyValueException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("PointlessArithmeticExpression")
public final class BasicSpreadsheetEngineTest extends BasicSpreadsheetEngineTestCase<BasicSpreadsheetEngine>
        implements SpreadsheetEngineTesting<BasicSpreadsheetEngine> {

    private final static String FORMATTED_PATTERN_SUFFIX = "FORMATTED_PATTERN_SUFFIX";
    private final static String FORMATTED_DEFAULT_SUFFIX = "FORMATTED_DEFAULT_SUFFIX";

    private final static Optional<Color> COLOR = Optional.of(Color.BLACK);

    private final static String PATTERN = "$text+" + FORMATTED_PATTERN_SUFFIX;
    private final static String PATTERN_COLOR = "$text+" + FORMATTED_PATTERN_SUFFIX + "+" + COLOR.get();
    private final static String PATTERN_FORMAT_FAIL = "<none>";

    private final static String DATE_PATTERN = "yyyy/mm/dd";
    private final static String TIME_PATTERN = "hh:mm";
    private final static String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;
    private final static String NUMBER_PATTERN = "#";
    private final static String TEXT_PATTERN = "@";

    private final static SpreadsheetFormatterContext SPREADSHEET_TEXT_FORMAT_CONTEXT = new FakeSpreadsheetFormatterContext() {
        @Override
        public boolean canConvert(final Object value,
                                  final Class<?> type) {
            return this.converter.canConvert(value, type, this);
        }

        @Override
        public <T> Either<T, String> convert(final Object value,
                                             final Class<T> target) {
            return this.converter.convert(value, target, this);
        }

        private final Converter converter = ExpressionNumber.fromConverter(
            Converters.collection(
                    Lists.of(
                            Converters.simple(),
                            Converters.localDateLocalDateTime(),
                            Converters.localTimeLocalDateTime(),
                            Converters.numberNumber(),
                            Converters.objectString()
                    )
            )
        );

        @Override
        public char decimalSeparator() {
            return '.';
        }

        @Override
        public char negativeSign() {
            return '-';
        }

        @Override
        public char positiveSign() {
            return '+';
        }

        @Override
        public MathContext mathContext() {
            return MATH_CONTEXT;
        }
    };

    private final static MathContext MATH_CONTEXT = MathContext.DECIMAL32;

    private final static int DEFAULT_YEAR = 1900;
    private final static int TWO_DIGIT_YEAR = 20;
    private final static char VALUE_SEPARATOR = ';';

    private final static SpreadsheetLabelName LABEL = SpreadsheetLabelName.labelName("Label123");
    private final static SpreadsheetCellReference LABEL_CELL = SpreadsheetCellReference.parseCellReference("Z99");

    @Test
    public void testWithNullMetadataFails() {
        assertThrows(NullPointerException.class, () -> BasicSpreadsheetEngine.with(
                null,
                this.cellStore(),
                this.cellReferencesStore(),
                this.labelStore(),
                this.labelReferencesStore(),
                this.rangeToCellStore(),
                this.rangeToConditionalFormattingRuleStore()));
    }

    @Test
    public void testWithNullCellStoreFails() {
        assertThrows(NullPointerException.class, () -> BasicSpreadsheetEngine.with(
                this.metadata(),
                null,
                this.cellReferencesStore(),
                this.labelStore(),
                this.labelReferencesStore(),
                this.rangeToCellStore(),
                this.rangeToConditionalFormattingRuleStore()));
    }

    @Test
    public void testWithNullCellReferencesStoreFails() {
        assertThrows(NullPointerException.class, () -> BasicSpreadsheetEngine.with(
                this.metadata(),
                this.cellStore(),
                null,
                this.labelStore(),
                this.labelReferencesStore(),
                this.rangeToCellStore(),
                this.rangeToConditionalFormattingRuleStore()));
    }

    @Test
    public void testWithNullLabelStoreFails() {
        assertThrows(NullPointerException.class, () -> BasicSpreadsheetEngine.with(
                this.metadata(),
                this.cellStore(),
                this.cellReferencesStore(),
                null,
                this.labelReferencesStore(),
                this.rangeToCellStore(),
                this.rangeToConditionalFormattingRuleStore()));
    }

    @Test
    public void testWithNullLabelReferencesStoreFails() {
        assertThrows(NullPointerException.class, () -> BasicSpreadsheetEngine.with(
                this.metadata(),
                this.cellStore(),
                this.cellReferencesStore(),
                this.labelStore(),
                null,
                this.rangeToCellStore(),
                this.rangeToConditionalFormattingRuleStore()));
    }

    @Test
    public void testWithNullRangeToCellStoreFails() {
        assertThrows(NullPointerException.class, () -> BasicSpreadsheetEngine.with(
                this.metadata(),
                this.cellStore(),
                this.cellReferencesStore(),
                this.labelStore(),
                this.labelReferencesStore(),
                null,
                this.rangeToConditionalFormattingRuleStore()));
    }

    @Test
    public void testWithNullRangeToConditionalFormattingRuleStoreFails() {
        assertThrows(NullPointerException.class, () -> BasicSpreadsheetEngine.with(
                this.metadata(),
                this.cellStore(),
                this.cellReferencesStore(),
                this.labelStore(),
                this.labelReferencesStore(),
                this.rangeToCellStore(),
                null));
    }

    // loadCell.........................................................................................................

    @Test
    public void testLoadCellCellWhenEmpty() {
        this.loadCellFailCheck(cellReference(1, 1), SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY);
    }

    @Test
    public void testLoadCellSkipEvaluate() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        cellStore.save(this.cell(cellReference, "=1+2"));

        this.loadCellAndCheckWithoutValueOrError(engine,
                cellReference,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context);
    }

    @Test
    public void testLoadCellWithoutFormatPattern() {
        this.cellStoreSaveAndLoadCellAndCheck(SpreadsheetCell.NO_FORMAT, FORMATTED_DEFAULT_SUFFIX);
    }

    @Test
    public void testLoadCellWithFormatPattern() {
        this.cellStoreSaveAndLoadCellAndCheck(Optional.of(SpreadsheetCellFormat.with(PATTERN)),
                FORMATTED_PATTERN_SUFFIX);
    }

    @Test
    public void testLoadCellWithFormatPatternAndFormatter() {
        final String pattern = "Custom";
        final String suffix = "CustomSuffix";
        this.cellStoreSaveAndLoadCellAndCheck(Optional.of(SpreadsheetCellFormat.with(pattern)
                        .setFormatter(Optional.of(this.formatter(pattern, SpreadsheetText.WITHOUT_COLOR, suffix)))),
                suffix);
    }

    private void cellStoreSaveAndLoadCellAndCheck(final Optional<SpreadsheetCellFormat> format,
                                                  final String patternSuffix) {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        cellStore.save(this.cell(cellReference, "=1+2")
                .setFormat(format));

        this.loadCellAndCheckFormatted2(engine,
                cellReference,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(1 + 2),
                patternSuffix);
    }

    @Test
    public void testLoadCellComputeIfNecessaryCachesCell() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        cellStore.save(this.cell(cellReference, "=1+2"));

        final SpreadsheetCell first = this.loadCellOrFail(engine,
                cellReference,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context);
        final SpreadsheetCell second = this.loadCellOrFail(engine,
                cellReference,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context);

        assertSame(first, second, "different instances of SpreadsheetCell returned not cached");
    }

    @Test
    public void testLoadCellComputeIfNecessaryKeepsExpression() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        cellStore.save(this.cell(a, "1/2"));

        final SpreadsheetCell first = this.loadCellOrFail(engine, a, SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, context);
        this.checkValueOrError(first, LocalDate.of(DEFAULT_YEAR, 2, 1));

        final int defaultYear = DEFAULT_YEAR + 100;

        final SpreadsheetCell second = this.loadCellOrFail(
                engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                this.createContext(defaultYear, engine));

        assertSame(first, second, "same instances of SpreadsheetCell returned should have new expression and value");
    }

    @Test
    public void testLoadCellComputeIfNecessaryCachesCellWithInvalidFormulaAndErrorCached() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        cellStore.save(this.cell(cellReference, "=1+2+"));

        final SpreadsheetCell first = this.loadCellOrFail(engine,
                cellReference,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context);
        assertNotEquals(SpreadsheetFormula.NO_ERROR, first.formula().error(), () -> "Expected error absent=" + first);

        final SpreadsheetCell second = this.loadCellOrFail(engine, cellReference, SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, context);
        assertSame(first, second, "different instances of SpreadsheetCell returned not cached");
    }

    @Test
    public void testLoadCellForceRecomputeIgnoresExpression() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        cellStore.save(this.cell(a, "1/2"));

        final SpreadsheetCell first = this.loadCellOrFail(engine, a, SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, context);
        this.checkValueOrError(first, LocalDate.of(DEFAULT_YEAR, 2, 1));

        final int defaultYear = DEFAULT_YEAR + 100;

        final SpreadsheetCell second = this.loadCellOrFail(
                engine,
                a,
                SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                this.createContext(defaultYear, engine));

        assertNotSame(first, second, "same instances of SpreadsheetCell returned should have new expression and value");
        this.checkValueOrError(
                second,
                LocalDate.of(defaultYear, 2, 1)
        );
    }

    @Test
    public void testLoadCellForceRecomputeIgnoresCache() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        cellStore.save(this.cell(a, "=1"));

        final SpreadsheetCellReference b = this.cellReference(2, 2);
        cellStore.save(this.cell(b, "=" + a));

        final SpreadsheetCell first = this.loadCellOrFail(engine, a, SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, context);

        cellStore.save(this.cell(a, "=999"));

        final SpreadsheetCell second = this.loadCellOrFail(engine, a, SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, context);
        assertNotSame(first, second, "different instances of SpreadsheetCell returned not cached");
        assertEquals(Optional.of(number(999)),
                second.formula().value(),
                "first should have value updated to 999 and not 1 the original value.");
    }

    @Test
    public void testLoadCellForceRecomputeIgnoresPreviousError() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("A1");
        final Set<SpreadsheetCell> saved = engine.saveCell(this.cell(a, "=1+$B$2"), context)
                .cells();

        final SpreadsheetCell withError = saved.iterator().next();
        assertNotEquals(SpreadsheetFormula.NO_ERROR,
                withError.formula().error(),
                () -> "cell should have error because B2 reference is unknown=" + withError);

        final SpreadsheetCellReference b = this.cellReference("B2");
        cellStore.save(this.cell(b, "=99"));

        this.loadCellAndCheckValue(engine,
                a,
                SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                context,
                number(1 + 99));
    }

    @Test
    public void testLoadCellComputeThenSkipEvaluate() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        cellStore.save(this.cell(cellReference, "=1+2"));

        final SpreadsheetCell first = this.loadCellOrFail(engine,
                cellReference,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context);
        final SpreadsheetCell second = this.loadCellOrFail(engine,
                cellReference,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context);

        assertSame(first, second, "different instances of SpreadsheetCell returned not cached");
    }

    @Test
    public void testLoadCellManyWithoutCrossReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 1);
        final SpreadsheetCellReference c = this.cellReference(3, 1);

        cellStore.save(this.cell(a, "=1+2"));
        cellStore.save(this.cell(b, "=3+4"));
        cellStore.save(this.cell(c, "=5+6"));

        this.loadCellAndCheckFormatted2(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(1 + 2),
                FORMATTED_DEFAULT_SUFFIX);
        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_DEFAULT_SUFFIX);
        this.loadCellAndCheckFormatted2(engine,
                c,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(5 + 6),
                FORMATTED_DEFAULT_SUFFIX);
    }

    @Test
    public void testLoadCellWithCrossReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 1);
        final SpreadsheetCellReference c = this.cellReference(3, 1);

        this.counter = BigDecimal.ZERO;

        engine.saveCell(this.cell(a, "=1+2+BasicSpreadsheetEngineTestCounter()"), context);
        engine.saveCell(this.cell(b, "=3+4+" + a), context);
        engine.saveCell(this.cell(c, "=5+6+" + a), context);

        // updating this counter results in $A having its value recomputed forcing a cascade update of $b and $c
        this.counter = number(100);

        this.loadCellAndCheck(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                formattedCellWithValue(a, "=1+2+BasicSpreadsheetEngineTestCounter()", number(100 + 3)),
                formattedCellWithValue(b, "=3+4+" + a, number(3 + 4 + 103)),
                formattedCellWithValue(c, "=5+6+" + a, number(5 + 6 + 103)));
    }

    @Test
    public void testLoadCellValueCellReferenceInvalidFails() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        cellStore.save(this.cell(a, "=X99"));

        this.loadCellAndCheckError(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                "Unknown cell reference");
    }

    @Test
    public void testLoadCellValueLabelInvalidFails() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        cellStore.save(this.cell(a, "=INVALIDLABEL"));

        this.loadCellAndCheckError(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                "Unknown label");
    }

    @Test
    public void testLoadCellValueIsCellReference() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // B1

        cellStore.save(this.cell(a, "=B1"));
        cellStore.save(this.cell(b, "=3+4"));

        // formula
        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_DEFAULT_SUFFIX);

        // reference to B1 which has formula
        this.loadCellAndCheckFormatted2(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_DEFAULT_SUFFIX);
    }

    @Test
    public void testLoadCellValueIsLabel() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // B1

        cellStore.save(this.cell(a, "=" + LABEL.value()));
        cellStore.save(this.cell(b, "=3+4"));

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, b));

        // formula
        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_DEFAULT_SUFFIX);

        // reference to B1 which has formula
        this.loadCellAndCheckFormatted2(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_DEFAULT_SUFFIX);
    }

    @Test
    public void testLoadCellWithConditionalFormattingRule() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final SpreadsheetRangeStore<SpreadsheetConditionalFormattingRule> rules = this.rangeToConditionalFormattingRuleStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore, rules);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1

        // rule 3 is ignored because it returns false, rule 2 short circuits the conditional testing ...
        final TextStyle italics = TextStyle.with(Maps.of(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC));
        this.saveRule(true,
                1,
                italics,
                a,
                rules);

        this.saveRule(true,
                2,
                TextStyle.with(Maps.of(TextStylePropertyName.TEXT_DECORATION_LINE, TextDecorationLine.UNDERLINE)),
                a,
                rules);
        this.saveRule(false,
                3,
                TextStyle.with(Maps.of(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC, TextStylePropertyName.FONT_WEIGHT, FontWeight.BOLD, TextStylePropertyName.TEXT_DECORATION_LINE, TextDecorationLine.UNDERLINE)),
                a,
                rules);

        cellStore.save(this.cell(a, "=3+4"));

        final SpreadsheetCell cell = this.loadCellAndCheckFormatted2(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_DEFAULT_SUFFIX);

        // UNDERLINED from conditional formatting rule #2.
        assertEquals(Optional.of(italics.replace(TextNode.text("7 " + FORMATTED_DEFAULT_SUFFIX)).root()),
                cell.formatted(),
                () -> "TextStyle should include underline if correct rule was applied=" + cell);
    }

    private void saveRule(final boolean result,
                          final int priority,
                          final TextStyle style,
                          final SpreadsheetCellReference cell,
                          final SpreadsheetRangeStore<SpreadsheetConditionalFormattingRule> rules) {
        rules.addValue(cell.spreadsheetRange(cell), rule(result, priority, style));
    }

    private SpreadsheetConditionalFormattingRule rule(final boolean result,
                                                      final int priority,
                                                      final TextStyle style) {


        return SpreadsheetConditionalFormattingRule.with(SpreadsheetDescription.with(priority + "=" + result),
                priority,
                SpreadsheetFormula.with(String.valueOf(result)).setExpression(Optional.of(Expression.booleanExpression(result))),
                (c) -> style);
    }

    // saveCell....................................................................................................

    @Test
    public void testSaveCellEmptyFormula() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("a1", "");
        final SpreadsheetCell a1Formatted = this.formattedCellWithValue(a1, "");
        this.saveCellAndCheck(engine,
                a1,
                context,
                a1Formatted);

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);
    }

    @Test
    public void testSaveCellInvalidDate() {
        this.saveCellWithErrorAndCheck(
                "1999/99/31",
                "Invalid value for MonthOfYear (valid values 1 - 12): 99"
        );
    }

    @Test
    public void testSaveCellInvalidDateTime() {
        this.saveCellWithErrorAndCheck(
                "1999/99/31 12:58",
                "Invalid value for MonthOfYear (valid values 1 - 12): 99"
        );
    }

    @Test
    public void testSaveCellInvalidTime() {
        this.saveCellWithErrorAndCheck(
                "12:99",
                "Invalid value for MinuteOfHour (valid values 0 - 59): 99"
        );
    }

    private void saveCellWithErrorAndCheck(final String formula, final String errorMessage) {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("a1", formula);
        final SpreadsheetCell a1Formatted = this.formattedCellWithError(a1, errorMessage);
        this.saveCellAndCheck(engine,
                a1,
                context,
                a1Formatted);

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);
    }

    @Test
    public void testSaveCellWithoutReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("a1", "=1+2");
        final SpreadsheetCell a1Formatted = this.formattedCellWithValue(a1, number(3));
        this.saveCellAndCheck(engine,
                a1,
                context,
                a1Formatted);

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);
    }

    @Test
    public void testSaveCellWithUnknownReference() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("a1", "=$B$2+99");
        final SpreadsheetCell a1Formatted = this.formattedCellWithError(a1, "Unknown cell reference $B$2");
        this.saveCellAndCheck(engine,
                a1,
                context,
                a1Formatted);

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);

        // verify references all ways are present in the store.
        final SpreadsheetCellReference b2 = SpreadsheetExpressionReference.parseCellReference("$B$2");

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference(), b2.toRelative()); // references from A1 -> B2
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference()); // references to A1 -> none

        this.loadReferencesAndCheck(cellReferenceStore, b2); // references to B2 -> none
        this.loadReferrersAndCheck(cellReferenceStore, b2, a1.reference()); // references from B2 -> A1
    }

    @Test
    public void testSaveCellIgnoresPreviousErrorComputesValue() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);

        final SpreadsheetCell cell = this.cell(cellReference,
                SpreadsheetFormula.with("=1+2")
                        .setError(Optional.of(SpreadsheetError.with("error!"))));

        this.saveCellAndCheck(engine,
                cell,
                context,
                this.formattedCellWithValue(cell, number(1 + 2)));
    }

    @Test
    public void testSaveCellMultipleIndependentUnreferenced() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);

        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+2");
        final SpreadsheetCell a1Formatted = this.formattedCellWithValue(a1, number(3));

        this.saveCellAndCheck(engine,
                a1,
                context,
                a1Formatted);

        final SpreadsheetCell b2 = this.cell("$B$2", "=3+4");
        final SpreadsheetCell b2Formatted = this.formattedCellWithValue(b2, number(7));

        this.saveCellAndCheck(engine,
                b2,
                context,
                b2Formatted);

        final SpreadsheetCell c3 = this.cell("$C$3", "=5+6");
        final SpreadsheetCell c3Formatted = this.formattedCellWithValue(c3, number(11));

        this.saveCellAndCheck(engine,
                c3,
                context,
                c3Formatted);

        this.loadCellStoreAndCheck(cellStore, a1Formatted, b2Formatted, c3Formatted);
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference()); // references to A1 -> none
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference()); // references from A1 -> none

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference()); // references to B2 -> none
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference()); // references from B2 -> none

        this.loadReferencesAndCheck(cellReferenceStore, c3.reference()); // references to C3 -> none
        this.loadReferrersAndCheck(cellReferenceStore, c3.reference()); // references from C3 -> none
    }

    @Test
    public void testSaveCellWithLabelReference() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferenceStore = this.labelReferencesStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore,
                labelReferenceStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetLabelName unknown = SpreadsheetExpressionReference.labelName("LABELXYZ");

        final SpreadsheetCell a1 = this.cell("a1", "=1+" + unknown);
        final SpreadsheetCell a1Formatted = this.formattedCellWithError(a1, "Unknown label: " + unknown);
        this.saveCellAndCheck(engine,
                a1,
                context,
                a1Formatted);

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);

        this.loadReferencesAndCheck(labelReferenceStore, unknown, a1.reference());
    }

    @Test
    public void testSaveCellTwiceLaterReferencesPrevious() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+2");
        engine.saveCell(a1, context);

        final SpreadsheetCellReference a1Reference = SpreadsheetCellReference.parseCellReference("$A$1");
        final SpreadsheetCell b2 = this.cell("$B$2", "=5+" + a1Reference);

        this.saveCellAndCheck(engine,
                b2,
                context,
                this.formattedCellWithValue(b2, number(5 + 3)));

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference(), b2.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference(), a1Reference.toRelative());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference());
    }

    @Test
    public void testSaveCellTwiceLaterReferencesPrevious2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+C3");
        engine.saveCell(a1, context);

        final SpreadsheetCell b2 = this.cell("$B$2", "=5+A1");
        engine.saveCell(b2, context);

        final SpreadsheetCell c3 = this.cell("$C$3", "=10");

        this.saveCellAndCheck(engine,
                c3,
                context,
                this.formattedCellWithValue(a1, number(1 + 10)),
                this.formattedCellWithValue(b2, number(5 + 1 + 10)),
                this.formattedCellWithValue(c3, number(10)));
    }

    @Test
    public void testSaveCellTwiceLaterReferencesPreviousAgain() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+2");
        engine.saveCell(a1, context);

        final SpreadsheetCell b2 = this.cell("$B$2", "=5+$A$1");
        final SpreadsheetCell b2Formatted = this.formattedCellWithValue(b2, number(5 + 1 + 2));

        this.saveCellAndCheck(engine,
                b2,
                context,
                b2Formatted);

        this.saveCellAndCheck(engine,
                b2Formatted,
                context);
    }

    @Test
    public void testSaveCellReferencesUpdated() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference b2Reference = SpreadsheetCellReference.parseCellReference("$B2$2");
        final SpreadsheetCell a1 = this.cell("$A$1", "=" + b2Reference + "+5");
        engine.saveCell(a1, context);

        final SpreadsheetCell b2 = this.cell("$B$2", "=1+2");
        this.saveCellAndCheck(engine,
                b2,
                context,
                this.formattedCellWithValue(a1, number(1 + 2 + 5)),
                this.formattedCellWithValue(b2, number(1 + 2)));

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference(), b2Reference.toRelative());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference(), a1.reference());
    }

    @Test
    public void testSaveCellLabelReference() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = this.labelReferencesStore();

        labelStore.save(SpreadsheetLabelMapping.with(SpreadsheetExpressionReference.labelName("LABELA1"), this.cellReference("A1")));

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore,
                labelReferencesStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=10");
        engine.saveCell(a1, context);

        final SpreadsheetCell b2 = this.cell("$B$2", "=5+LABELA1");
        this.saveCellAndCheck(engine,
                b2,
                context,
                this.formattedCellWithValue(b2, number(5 + 10)));
    }

    @Test
    public void testSaveCellLabelReference2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = this.labelReferencesStore();

        final SpreadsheetLabelName labelB2 = SpreadsheetExpressionReference.labelName("LABELB2");
        final SpreadsheetCell b2 = this.cell("$B$2", "=5");

        labelStore.save(SpreadsheetLabelMapping.with(labelB2, b2.reference()));

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore,
                labelReferencesStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=10+" + labelB2);
        engine.saveCell(a1, context);

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference());

        this.loadReferencesAndCheck(labelReferencesStore, labelB2, a1.reference());

        this.saveCellAndCheck(engine,
                b2,
                context,
                this.formattedCellWithValue(a1, number(10 + 5)),
                this.formattedCellWithValue(b2, number(5)));
    }

    @Test
    public void testSaveCellReplacesCellReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell d4 = this.cell("$D$4", "=20");
        engine.saveCell(d4, context);

        final SpreadsheetCell e5 = this.cell("$E$5", "=30");
        engine.saveCell(e5, context);

        engine.saveCell(this.cell("$A$1", "=10+" + d4.reference()), context);

        final SpreadsheetCell a1 = this.cell("$A$1", "=40+" + e5.reference());
        this.saveCellAndCheck(engine,
                a1,
                context,
                this.formattedCellWithValue(a1, number(40 + 30)));

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference(), e5.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, d4.reference());
        this.loadReferrersAndCheck(cellReferenceStore, d4.reference());
    }

    @Test
    public void testSaveCellReplacesLabelReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = this.labelReferencesStore();

        final SpreadsheetLabelName labelB2 = SpreadsheetExpressionReference.labelName("LABELB2");
        labelStore.save(SpreadsheetLabelMapping.with(labelB2, this.cellReference("B2")));

        final SpreadsheetLabelName labelD4 = SpreadsheetExpressionReference.labelName("LABELD4");
        labelStore.save(SpreadsheetLabelMapping.with(labelD4, this.cellReference("D4")));

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore,
                labelReferencesStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell d4 = this.cell("$D$4", "=20");
        engine.saveCell(d4, context);

        engine.saveCell(this.cell("$A$1", "=10+" + labelB2), context);

        final SpreadsheetCell a1 = this.cell("$A$1", "=40+" + labelD4);
        this.saveCellAndCheck(engine,
                a1,
                context,
                this.formattedCellWithValue(a1, number(40 + 20)));

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, d4.reference());
        this.loadReferrersAndCheck(cellReferenceStore, d4.reference());

        this.loadReferencesAndCheck(labelReferencesStore, labelB2);
        this.loadReferencesAndCheck(labelReferencesStore, labelD4, a1.reference());
    }

    @Test
    public void testSaveCellReplacesCellAndLabelReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = this.labelReferencesStore();

        final SpreadsheetLabelName labelB2 = SpreadsheetExpressionReference.labelName("LABELB2");
        final SpreadsheetCellReference b2Reference = this.cellReference("B2");
        labelStore.save(SpreadsheetLabelMapping.with(labelB2, b2Reference));

        final SpreadsheetLabelName labelD4 = SpreadsheetExpressionReference.labelName("LABELD4");
        labelStore.save(SpreadsheetLabelMapping.with(labelD4, this.cellReference("D4")));

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore,
                labelReferencesStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell d4 = this.cell("$D$4", "=20");
        engine.saveCell(d4, context);

        final SpreadsheetCell e5 = this.cell("$E$5", "=30");
        engine.saveCell(e5, context);

        engine.saveCell(this.cell("$A$1", "=10+" + labelB2 + "+C2"), context);

        final SpreadsheetCellReference e5Reference = SpreadsheetCellReference.parseCellReference("$E$5");
        final SpreadsheetCell a1 = this.cell("$A$1", "=40+" + labelD4 + "+" + e5Reference);
        this.saveCellAndCheck(engine,
                a1,
                context,
                this.formattedCellWithValue(a1, number(40 + 20 + 30)));

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference(), e5Reference.toRelative());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, d4.reference());
        this.loadReferrersAndCheck(cellReferenceStore, d4.reference());

        this.loadReferencesAndCheck(labelReferencesStore, labelB2);
        this.loadReferencesAndCheck(labelReferencesStore, labelD4, a1.reference());
    }

    // saveCell tests with non expression formula's only value literals.................................................

    @Test
    public void testSaveCellFormulaApostropheString() {
        this.saveCellAndLoadAndFormattedCheck(
                "'Hello",
                "Hello"
        );
    }

    @Test
    public void testSaveCellFormulaNumber() {
        this.saveCellAndLoadAndFormattedCheck(
                "123",
                this.expressionNumberKind().create(123)
        );
    }

    @Test
    public void testSaveCellFormulaDate() {
        this.saveCellAndLoadAndFormattedCheck(
                "1999/12/31",
                LocalDate.of(1999, 12, 31)
        );
    }

    @Test
    public void testSaveCellFormulaDateTime() {
        this.saveCellAndLoadAndFormattedCheck(
                "1999/12/31 12:34",
                LocalDateTime.of(
                        LocalDate.of(1999, 12, 31),
                        LocalTime.of(12, 34)
                )
        );
    }

    @Test
    public void testSaveCellFormulaTime() {
        this.saveCellAndLoadAndFormattedCheck(
                "12:34",
                LocalTime.of(12, 34)
        );
    }

    private void saveCellAndLoadAndFormattedCheck(final String formula,
                                                  final Object value) {
        final SpreadsheetCellStore cellStore = this.cellStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", formula);
        final SpreadsheetCell a1Formatted = this.formattedCellWithValue(
                a1,
                value
        );
        this.saveCellAndCheck(engine,
                a1,
                context,
                a1Formatted);

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
    }

    // deleteCell....................................................................................................

    @Test
    public void testDeleteCellWithReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a1 = SpreadsheetExpressionReference.parseCellReference("$A$1");
        final SpreadsheetCellReference b2 = SpreadsheetExpressionReference.parseCellReference("$B$2");

        engine.saveCell(this.cell(a1, "=99+" + b2), context);
        this.deleteCellAndCheck(engine, a1, context);

        this.loadReferencesAndCheck(cellReferenceStore, a1);
        this.loadReferrersAndCheck(cellReferenceStore, a1);

        this.loadReferencesAndCheck(cellReferenceStore, b2);
        this.loadReferrersAndCheck(cellReferenceStore, b2);
    }

    @Test
    public void testDeleteCellWithCellReferrers() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference b2Reference = SpreadsheetCellReference.parseCellReference("$B$2");

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+" + b2Reference);
        engine.saveCell(a1, context);

        final SpreadsheetCell b2 = this.cell("$B$2", "=20");
        engine.saveCell(b2, context);

        this.deleteCellAndCheck(engine,
                b2.reference(),
                context,
                this.formattedCellWithError(a1, "Unknown cell reference " + b2Reference));

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference(), b2Reference.toRelative());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference(), a1.reference());
    }

    @Test
    public void testDeleteCellWithLabelReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = this.labelReferencesStore();

        final SpreadsheetLabelName labelB2 = SpreadsheetExpressionReference.labelName("LABELB2");
        final SpreadsheetCell b2 = this.cell("$B$2", "=20");
        labelStore.save(SpreadsheetLabelMapping.with(labelB2, b2.reference()));

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore,
                labelReferencesStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+" + labelB2);
        engine.saveCell(a1, context);

        engine.saveCell(b2, context);

        this.deleteCellAndCheck(engine,
                a1.reference(),
                context);

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference());

        this.loadReferencesAndCheck(labelReferencesStore, labelB2);
    }

    @Test
    public void testDeleteCellWithLabelReferrers() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = this.cellReferencesStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = this.labelReferencesStore();

        final SpreadsheetLabelName labelB2 = SpreadsheetExpressionReference.labelName("LABELB2");
        final SpreadsheetCell b2 = this.cell("$B$2", "=20");
        labelStore.save(SpreadsheetLabelMapping.with(labelB2, b2.reference()));

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore,
                cellReferenceStore,
                labelStore,
                labelReferencesStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+" + labelB2);
        engine.saveCell(a1, context);

        engine.saveCell(b2, context);

        this.deleteCellAndCheck(engine,
                b2.reference(),
                context,
                this.formattedCellWithError(a1, "Unknown cell reference " + labelB2));

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference());

        this.loadReferencesAndCheck(labelReferencesStore, labelB2, a1.reference());
    }

    // deleteColumn....................................................................................................

    @Test
    public void testDeleteColumnZeroNothingDeleted() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference reference = this.cellReference(99, 0); // A3

        engine.saveCell(this.cell(reference, "=99+0"), context);

        this.deleteColumnsAndCheck(engine,
                reference.column(),
                0,
                context);

        this.countAndCheck(cellStore, 1);
    }

    @Test
    public void testDeleteColumnNoCellsRefreshed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("A1"); // A1
        final SpreadsheetCellReference b = this.cellReference("B2"); // B2
        final SpreadsheetCellReference c = this.cellReference("C3"); // C3

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        this.deleteColumnsAndCheck(engine,
                c.column(),
                1,
                context);

        this.countAndCheck(cellStore, 2);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("A1"); // A1
        final SpreadsheetCellReference b = this.cellReference("B2"); // B2
        final SpreadsheetCellReference c = this.cellReference("C3"); // C3

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context); // deleted/replaced by $c
        engine.saveCell(this.cell(c, "=5+6"), context); // becomes b3

        this.deleteColumnsAndCheck(engine,
                b.column(),
                1,
                context,
                this.formattedCellWithValue("b3", "=5+6", number(5 + 6)));

        this.countAndCheck(cellStore, 2);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedAddition() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=5+6", number(5 + 6));
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedExpressionNumber() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=55.5", number(55.5));
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedExpressionNumber2() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=55", number(55));
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedDivision() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=9/3", number(9 / 3));
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedEqualsTrue() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8==8", true);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedEqualsFalse() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8==7", false);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedFunction() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=BasicSpreadsheetEngineTestSum(1;99)", number(1 + 99));
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedGreaterThanTrue() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8>7", true);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedGreaterThanFalse() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=7>8", false);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedGreaterThanEqualsTrue() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8>=7", true);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedGreaterThanEqualsFalse() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=7>=8", false);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedGroup() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=(99)", number(99));
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedLessThanTrue() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8<9", true);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedLessThanFalse() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=7<6", false);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedLessThanEqualsTrue() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8<=8", true);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedLessThanEqualsFalse() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8<=7", false);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedMultiplication() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=9*3", number(9 * 3));
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedNegative() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=-99", number(-99));
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedNotEqualsTrue() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8!=7", true);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedNotEqualsFalse() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8!=8", false);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedPercentage() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=120%", number(1.2));
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedSubtraction() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=9-7", number(9 - 7));
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedText() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=\"ABC123\"", "ABC123");
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedAdditionWithWhitespace() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=1 + 2", number(1 + 2));
    }

    private void deleteColumnColumnsAfterCellsRefreshedAndCheck(final String formula,
                                                                final Object value) {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("A1"); // A1
        final SpreadsheetCellReference b = this.cellReference("B2"); // B2
        final SpreadsheetCellReference c = this.cellReference("C3"); // C3

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context); // deleted/replaced by $c
        engine.saveCell(this.cell(c, formula), context); // becomes b3

        this.deleteColumnsAndCheck(engine,
                b.column(),
                1,
                context,
                this.formattedCellWithValue("b3", formula, value));

        this.countAndCheck(cellStore, 2);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshed2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("A1");
        final SpreadsheetCellReference b = this.cellReference("B2"); //replaced by $c
        final SpreadsheetCellReference c = this.cellReference("C3");
        final SpreadsheetCellReference d = this.cellReference("Z99");// B99 moved

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);
        engine.saveCell(this.cell(d, "=7+8"), context);

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue("B3", "=5+6", number(5 + 6)),
                this.formattedCellWithValue("Y99", "=7+8", number(7 + 8)));

        this.countAndCheck(cellStore, 3);
    }

    @Test
    public void testDeleteColumnWithLabelsToCellReferenceIgnored() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = SpreadsheetExpressionReference.parseCellReference("A1"); // A1
        final SpreadsheetCellReference b = SpreadsheetExpressionReference.parseCellReference("E2"); // E2

        engine.saveCell(this.cell(a, "=99+0"), context);
        engine.saveCell(this.cell(b, "=2+0+" + LABEL), context);

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a));

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                b.column().add(-1),
                count,
                context,
                this.formattedCellWithValue(b.addColumn(-1), "=2+0+" + LABEL, number(2 + 99)));

        this.countAndCheck(cellStore, 2);

        this.loadLabelAndCheck(labelStore, LABEL, a);
    }

    @Test
    public void testDeleteColumnWithLabelsToCellReferencedFixed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // A2 replaced by c
        final SpreadsheetCellReference c = this.cellReference(2, 0); // A3 DELETED
        final SpreadsheetCellReference d = this.cellReference(13, 8); // B8 moved
        final SpreadsheetCellReference e = this.cellReference(14, 9); // C9 moved LABEL=

        engine.saveCell(this.cell(a, "=1+" + LABEL), context);
        engine.saveCell(this.cell(b, "=2+0"), context);
        engine.saveCell(this.cell(c, "=3+0"), context);
        engine.saveCell(this.cell(d, "=4+" + LABEL), context);
        engine.saveCell(this.cell(e, "=99+0"), context); // LABEL=

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, e));

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue(a, "=1+" + LABEL, number(1 + 99 + 0)),
                this.formattedCellWithValue(c.addColumn(-count), "=3+0", number(3 + 0)),
                this.formattedCellWithValue(d.addColumn(-count), "=4+" + LABEL, number(4 + 99 + 0)),
                this.formattedCellWithValue(e.addColumn(-count), "=99+0", number(99 + 0))); // old $b delete, $c,$d columns -1.

        this.loadLabelAndCheck(labelStore, LABEL, e.addColumn(-count));

        this.countAndCheck(cellStore, 4);
    }

    @Test
    public void testDeleteColumnWithLabelToDeletedCell() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // A2

        engine.saveCell(this.cell(a, "=1+0"), context);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, b));

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                b.column(),
                count,
                context); // $b delete, $c,$d columns -1.

        this.loadLabelFailCheck(labelStore, LABEL);

        this.countAndCheck(cellStore, 1);
    }

    @Test
    public void testDeleteColumnWithCellReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // A2
        final SpreadsheetCellReference c = this.cellReference(10, 0); // A10 deleted
        final SpreadsheetCellReference d = this.cellReference(13, 8); // H13 moved
        final SpreadsheetCellReference e = this.cellReference(14, 9); // I14 moved

        engine.saveCell(this.cell(a, "=1+" + d), context);
        engine.saveCell(this.cell(b, "=2"), context);
        engine.saveCell(this.cell(c, "=3"), context);
        engine.saveCell(this.cell(d, "=4"), context);
        engine.saveCell(this.cell(e, "=5+" + b), context); // =5+2

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                c.column(),
                count,
                context,
                this.formattedCellWithValue(a, "=1+" + d.addColumn(-count), number(1 + 4)),
                this.formattedCellWithValue(d.addColumn(-count), "=4", number(4)),
                this.formattedCellWithValue(e.addColumn(-count), "=5+" + b, number(5 + 2))); // $c delete

        this.countAndCheck(cellStore, 4);
    }

    @Test
    public void testDeleteColumnWithCellReferences2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // A2
        final SpreadsheetCellReference c = this.cellReference(10, 0); // A10 deleted
        final SpreadsheetCellReference d = this.cellReference(13, 8); // H13 moved
        final SpreadsheetCellReference e = this.cellReference(14, 9); // I14 moved

        engine.saveCell(this.cell(a, "=1+" + d), context);
        engine.saveCell(this.cell(b, "=2"), context);
        engine.saveCell(this.cell(c, "=3"), context);
        engine.saveCell(this.cell(d, "=4"), context);
        engine.saveCell(this.cell(e, "=5+" + b), context); // =5+2

        final int count = 2;
        this.deleteColumnsAndCheck(engine,
                c.column(),
                count,
                context,
                this.formattedCellWithValue(a, "=1+" + d.addColumn(-count), number(1 + 4)),
                this.formattedCellWithValue(d.addColumn(-count), "=4", number(4)),
                this.formattedCellWithValue(e.addColumn(-count), "=5+" + b, number(5 + 2))); // $c deleted, old-d & old-e refreshed

        this.countAndCheck(cellStore, 4);
    }

    @Test
    public void testDeleteColumnWithCellReferencesToDeletedCell() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // A2

        engine.saveCell(this.cell(a, "=1+" + b), context);
        engine.saveCell(this.cell(b, "=2"), context);

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithError(a, "=1+InvalidCellReference(\"" + b + "\")", "Invalid cell reference: " + b)); // $b delete

        this.countAndCheck(cellStore, 1);
    }

    @Test
    public void testDeleteColumnSeveral() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(10, 0); // DELETED
        final SpreadsheetCellReference c = this.cellReference(11, 0); // DELETED
        final SpreadsheetCellReference d = this.cellReference(12, 2); // C4
        final SpreadsheetCellReference e = this.cellReference(20, 3); // T3
        final SpreadsheetCellReference f = this.cellReference(21, 4); // U4

        engine.saveCell(this.cell(a, "=1"), context);
        engine.saveCell(this.cell(b, "=2"), context);
        engine.saveCell(this.cell(c, "=3"), context);
        engine.saveCell(this.cell(d, "=4"), context);
        engine.saveCell(this.cell(e, "=5"), context);
        engine.saveCell(this.cell(f, "=6"), context);

        final int count = 5;
        this.deleteColumnsAndCheck(engine,
                this.column(7),
                count,
                context,
                this.formattedCellWithValue(d.addColumn(-count), "=4", number(4)),
                this.formattedCellWithValue(e.addColumn(-count), "=5", number(5)),
                this.formattedCellWithValue(f.addColumn(-count), "=6", number(6))); // $b & $c

        this.countAndCheck(cellStore, 4);
    }

    // deleteRow....................................................................................................

    @Test
    public void testDeleteRowsNone() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference reference = this.cellReference(0, 1); // A2

        engine.saveCell(this.cell(reference, "=99+0"), context);

        this.deleteRowsAndCheck(engine,
                reference.row(),
                0,
                context);

        this.countAndCheck(cellStore, 1);
    }

    @Test
    public void testDeleteRowsOne() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("A1");
        final SpreadsheetCellReference b = this.cellReference("A2");
        final SpreadsheetCellReference c = this.cellReference("A3");

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        this.deleteRowsAndCheck(engine,
                b.row(),
                1,
                context,
                this.formattedCellWithValue(c.addRow(-1), "=5+6", number(5 + 6)));

        this.countAndCheck(cellStore, 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addRow(-1),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+6",
                number(5 + 6));
    }

    @Test
    public void testDeleteRowsOne2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("A1"); //
        final SpreadsheetCellReference b = this.cellReference("A2"); // replaced by c
        final SpreadsheetCellReference c = this.cellReference("A3"); // DELETED
        final SpreadsheetCellReference d = this.cellReference("B10"); // moved

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);
        engine.saveCell(this.cell(d, "=7+8"), context);

        final int count = 1;
        this.deleteRowsAndCheck(engine,
                b.row(),
                count,
                context,
                this.formattedCellWithValue(c.addRow(-count), "=5+6", number(5 + 6)),
                this.formattedCellWithValue(d.addRow(-count), "=7+8", number(7 + 8))); // $b delete

        this.countAndCheck(cellStore, 3);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));
    }

    @Test
    public void testDeleteRowsMany() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); //
        final SpreadsheetCellReference b = this.cellReference(0, 1); // replaced by c
        final SpreadsheetCellReference c = this.cellReference(0, 2); // DELETED
        final SpreadsheetCellReference d = this.cellReference(1, 9); // moved

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);
        engine.saveCell(this.cell(d, "=7+8"), context);

        final int count = 2;
        this.deleteRowsAndCheck(engine,
                b.row(),
                count,
                context,
                this.formattedCellWithValue(d.addRow(-count), "=7+8", number(7 + 8)));

        this.countAndCheck(cellStore, 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=7+8",
                number(7 + 8));
    }

    // delete row with labels to cell references..................................................................

    @Test
    public void testDeleteRowsWithLabelsToCellUnmodified() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$A$2");
        final SpreadsheetCellReference c = this.cellReference("$A$6");

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(b, "=20+0+" + LABEL), context);
        engine.saveCell(this.cell(c, "=99+0"), context);

        final int count = 2;
        this.deleteRowsAndCheck(engine,
                b.row().add(count),
                count,
                context,
                this.formattedCellWithValue("$A$4", "=99+0", number(99 + 0))); // $c moved, $b unmodified label refs $a also unmodified.

        this.countAndCheck(cellStore, 3);

        this.loadLabelAndCheck(labelStore, LABEL, a);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0",
                number(1));

        this.loadCellAndCheckFormulaAndValue(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=20+0+" + LABEL,
                number(20 + 0 + 1));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99));
    }

    @Test
    public void testDeleteRowsWithLabelsToCellFixed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$A$6");

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, b));

        cellStore.save(this.cell(a, "=1+0+" + LABEL));
        cellStore.save(this.cell(b, "=2+0"));

        final int count = 2;
        this.deleteRowsAndCheck(engine,
                a.row().add(1),
                count,
                context,
                this.formattedCellWithValue(a, "=1+0+" + LABEL, number(1 + 0 + 2 + 0)),
                this.formattedCellWithValue(b.addRow(-count), "=2+0", number(2 + 0))); // $b moved

        this.countAndCheck(cellStore, 2);

        this.loadLabelAndCheck(labelStore, LABEL, b.addRow(-count));

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                "=1+0+" + LABEL,
                number(3));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addRow(-count),
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                "=2+0",
                number(2));
    }

    @Test
    public void testDeleteRowsWithLabelToCellReferenceDeleted() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$A$6");

        engine.saveCell(this.cell(a, "=1+" + b), context);
        engine.saveCell(this.cell(b, "=2+0"), context);

        this.deleteRowsAndCheck(engine,
                b.row(),
                1,
                context,
                this.formattedCellWithError(a, "=1+InvalidCellReference(\"" + b + "\")", "Invalid cell reference: $A$6"));

        this.countAndCheck(cellStore, 1);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+InvalidCellReference(\"" + b + "\")",
                "Invalid cell reference: " + b); // reference should have been fixed.
    }

    @Test
    public void testDeleteRowsWithCellReferencesFixed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$A$2"); // B1
        final SpreadsheetCellReference c = this.cellReference("$A$11"); // A10 deleted
        final SpreadsheetCellReference d = this.cellReference("$I$14"); // H13 moved
        final SpreadsheetCellReference e = this.cellReference("$J$15"); // I14 moved

        engine.saveCell(this.cell(a, "=1+" + d), context);
        engine.saveCell(this.cell(b, "=2"), context);
        engine.saveCell(this.cell(c, "=3"), context); // DELETED
        engine.saveCell(this.cell(d, "=4"), context); // REFRESHED
        engine.saveCell(this.cell(e, "=5+" + b), context); // REFRESHED =5+2

        final int count = 1;
        this.deleteRowsAndCheck(engine,
                c.row(),
                count,
                context,
                this.formattedCellWithValue("$A$1", "=1+$I$13", number(1 + 4)),
                this.formattedCellWithValue("$I$13", "=4", number(4)),
                this.formattedCellWithValue("$J$14", "=5+$A$2", number(5 + 2))); // $c delete

        this.countAndCheck(cellStore, 4);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+" + d.addRow(-count),
                number(1 + 4)); // reference should have been fixed.

        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(2),
                FORMATTED_DEFAULT_SUFFIX);

        this.loadCellAndCheckFormatted2(engine,
                d.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(4),
                FORMATTED_DEFAULT_SUFFIX);

        this.loadCellAndCheckFormulaAndValue(engine,
                e.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+" + b,
                number(5 + 2));
    }

    @Test
    public void testDeleteRowsWithCellReferencesFixed2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$A$2");
        final SpreadsheetCellReference c = this.cellReference("$A$11");// DELETED
        final SpreadsheetCellReference d = this.cellReference("$I$14"); // MOVED
        final SpreadsheetCellReference e = this.cellReference("$J$15"); // MOVED

        engine.saveCell(this.cell(a, "=1+" + d), context);
        engine.saveCell(this.cell(b, "=2"), context);
        engine.saveCell(this.cell(c, "=3"), context);
        engine.saveCell(this.cell(d, "=4"), context);
        engine.saveCell(this.cell(e, "=5+" + b), context); // =5+2

        final int count = 2;
        this.deleteRowsAndCheck(engine,
                c.row(),
                count,
                context,
                this.formattedCellWithValue("$A$1", "=1+$I$12", number(1 + 4)),
                this.formattedCellWithValue("$I$12", "=4", number(4)),
                this.formattedCellWithValue("$J$13", "=5+$A$2", number(5 + 2))); // $c delete

        this.countAndCheck(cellStore, 4);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+" + d.addRow(-count),
                number(1 + 4)); // reference should have been fixed.

        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(2),
                FORMATTED_DEFAULT_SUFFIX);

        this.loadCellAndCheckFormatted2(engine,
                d.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(4),
                FORMATTED_DEFAULT_SUFFIX);

        this.loadCellAndCheckFormulaAndValue(engine,
                e.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+" + b,
                number(5 + 2));
    }

    // delete range....................................................................................

    @Test
    public void testDeleteRowsWithLabelsToRangeUnmodified() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 15);

        final SpreadsheetRange ab = a.spreadsheetRange(b);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, ab));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(c, "=20+0+" + LABEL), context);
        engine.saveCell(this.cell(d, "=99+0"), context); // DELETED

        final int count = 2;
        this.deleteRowsAndCheck(engine,
                d.row(),
                count,
                context);

        this.countAndCheck(cellStore, 2); // a&c
        this.countAndCheck(labelStore, 1);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0",
                number(1));

        this.loadCellAndCheckFormulaAndValue(engine,
                c,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=20+0+" + LABEL,
                number(21));

        this.loadLabelAndCheck(labelStore, LABEL, ab);
    }

    @Test
    public void testDeleteRowsWithLabelsToRangeDeleted() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);

        engine.saveCell(this.cell(a, "=1+0"), context);

        final SpreadsheetRange bc = b.spreadsheetRange(c);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bc));

        final int count = c.row().value() - b.row().value() + 1;
        this.deleteRowsAndCheck(engine,
                b.row(),
                count,
                context); // b..c deleted

        this.countAndCheck(cellStore, 1); // a
        this.countAndCheck(labelStore, 0);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0",
                number(1));
    }

    @Test
    public void testDeleteRowsWithLabelsToRangeDeleted2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 20);

        final SpreadsheetRange bc = b.spreadsheetRange(c);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bc));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(d, "=20+0"), context);

        final int count = c.row().value() - b.row().value() + 1;
        this.deleteRowsAndCheck(engine,
                b.row(),
                count,
                context,
                this.formattedCellWithValue(d.addRow(-count), "=20+0", number(20 + 0))); // b..c deleted, d moved

        this.countAndCheck(cellStore, 2); // a&d
        this.countAndCheck(labelStore, 0);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0",
                number(1));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=20+0",
                number(20));
    }

    @Test
    public void testDeleteRowsWithLabelsToRangeDeleted3() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);

        final SpreadsheetRange bc = b.spreadsheetRange(c);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bc));

        engine.saveCell(this.cell(a, "=1+0+" + LABEL), context);
        engine.saveCell(this.cell(b, "=20+0"), context);

        final int count = c.row().value() - b.row().value() + 1;
        this.deleteRowsAndCheck(engine,
                b.row(),
                count,
                context,
                formattedCellWithError(a, "=1+0+" + LABEL, "Unknown label: " + LABEL)); // b..c deleted

        this.countAndCheck(cellStore, 1); // a
        this.countAndCheck(labelStore, 0);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + LABEL,
                "Unknown label: " + LABEL);
    }

    @Test
    public void testDeleteRowsWithLabelsToRangeFixed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 15);
        final SpreadsheetCellReference e = this.cellReference(0, 20);

        final SpreadsheetRange de = d.spreadsheetRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, de));

        engine.saveCell(this.cell(a, "=1+0+" + LABEL), context);
        engine.saveCell(this.cell(d, "=20+0"), context);

        final int count = c.row().value() - b.row().value() + 1;
        this.deleteRowsAndCheck(engine,
                b.row(),
                count,
                context,
                this.formattedCellWithValue(d.addRow(-count), "=20+0", number(20 + 0))); // b..c deleted, d moved

        this.countAndCheck(cellStore, 2); // a&d

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + LABEL,
                number(1 + 20));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=20+0",
                number(20));

        this.countAndCheck(labelStore, 1);
        final SpreadsheetCellReference begin = d.addRow(-count);
        final SpreadsheetCellReference end = e.addRow(-count);
        this.loadLabelAndCheck(labelStore, LABEL, begin.spreadsheetRange(end));
    }

    @SuppressWarnings("unused")
    @Test
    public void testDeleteRowsWithLabelsToRangeFixed2() {
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 15);
        final SpreadsheetCellReference e = this.cellReference(0, 20);

        final SpreadsheetRange ce = c.spreadsheetRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, ce));

        final int count = e.row().value() - d.row().value() + 1;
        this.deleteRowsAndCheck(engine,
                d.row(),
                count,
                context); // b..c deleted, d moved

        this.countAndCheck(labelStore, 1);
        this.loadLabelAndCheck(labelStore, LABEL, c.spreadsheetRange(d));
    }

    @Test
    public void testDeleteRowsWithLabelsToRangeFixed3() {
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 15);

        final SpreadsheetRange bd = b.spreadsheetRange(d);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bd));

        final int count = 1;
        this.deleteRowsAndCheck(engine,
                c.row(),
                count,
                context); // b..c deleted, d moved

        this.countAndCheck(labelStore, 1);

        final SpreadsheetCellReference end = d.addRow(-count);
        this.loadLabelAndCheck(labelStore, LABEL, b.spreadsheetRange(end));
    }

    @SuppressWarnings("unused")
    @Test
    public void testDeleteRowsWithLabelsToRangeFixed4() {
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 5);  // delete
        final SpreadsheetCellReference b = this.cellReference(0, 10); // range delete
        final SpreadsheetCellReference c = this.cellReference(0, 15); // range delete
        final SpreadsheetCellReference d = this.cellReference(0, 20); // range
        final SpreadsheetCellReference e = this.cellReference(0, 25); // range

        final SpreadsheetRange be = b.spreadsheetRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, be));

        final int count = c.row().value() - a.row().value();
        this.deleteRowsAndCheck(engine,
                a.row(),
                count,
                context);

        this.countAndCheck(labelStore, 1);

        this.loadLabelAndCheck(labelStore, LABEL, a.spreadsheetRange(b));
    }

    // deleteColumn....................................................................................................

    @Test
    public void testDeleteColumnsNone() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference reference = this.cellReference(1, 0); // A2

        engine.saveCell(this.cell(reference, "=99+0"), context);

        engine.deleteColumns(reference.column(), 0, context);

        this.countAndCheck(cellStore, 1);

        this.loadCellAndCheckFormulaAndValue(engine,
                reference,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99));
    }

    @Test
    public void testDeleteColumnsOne() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A1");
        final SpreadsheetCellReference b = this.cellReference(1, 0); // B1
        final SpreadsheetCellReference c = this.cellReference(2, 0); // C1

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        this.deleteColumnsAndCheck(engine,
                b.column(),
                1,
                context,
                this.formattedCellWithValue("$B$1", "=5+6", number(5 + 6)));

        this.countAndCheck(cellStore, 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));

        this.loadCellAndCheckFormulaAndValue(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+6",
                number(5 + 6));
    }

    @Test
    public void testDeleteColumnsOne2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); //
        final SpreadsheetCellReference b = this.cellReference("$B$1"); // replaced by c
        final SpreadsheetCellReference c = this.cellReference("$C$1"); // DELETED
        final SpreadsheetCellReference d = this.cellReference("$J$2"); // moved

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context); // DELETE
        engine.saveCell(this.cell(c, "=5+6"), context);
        engine.saveCell(this.cell(d, "=7+8"), context);

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue("$B$1", "=5+6", number(5 + 6)),
                this.formattedCellWithValue("$I$2", "=7+8", number(7 + 8))); // $b delete

        this.countAndCheck(cellStore, 3);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));

        this.loadCellAndCheckFormulaAndValue(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+6",
                number(5 + 6));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=7+8",
                number(7 + 8));
    }

    @Test
    public void testDeleteColumnsMany() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); //
        final SpreadsheetCellReference b = this.cellReference("$B$1"); // DELETED
        final SpreadsheetCellReference c = this.cellReference("$C$1"); // DELETED
        final SpreadsheetCellReference d = this.cellReference("$J$2"); // MOVED

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);
        engine.saveCell(this.cell(d, "=7+8"), context);

        final int count = 2;
        this.deleteColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue("$H$2", "=7+8", number(7 + 8))); // $b, $c deleted

        this.countAndCheck(cellStore, 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=7+8",
                number(7 + 8));
    }

    // delete column with labels to cell references..................................................................

    @Test
    public void testDeleteColumnsWithLabelsToCellUnmodified() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$B$1");
        final SpreadsheetCellReference c = this.cellReference("$F$1");

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(b, "=20+0+" + LABEL), context);
        engine.saveCell(this.cell(c, "=99+0"), context);

        final int count = 2;
        this.deleteColumnsAndCheck(engine,
                b.column().add(2),
                count,
                context,
                this.formattedCellWithValue("$D$1", "=99+0", number(99 + 0)));

        this.countAndCheck(cellStore, 3);

        this.loadLabelAndCheck(labelStore, LABEL, a);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0",
                number(1));

        this.loadCellAndCheckFormulaAndValue(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=20+0+" + LABEL,
                number(21));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99));
    }

    @Test
    public void testDeleteColumnsWithLabelsToCellFixed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$E$1");

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, b));

        engine.saveCell(this.cell(a, "=1+0+" + LABEL), context);
        engine.saveCell(this.cell(b, "=2+0"), context);

        final int count = 2;
        this.deleteColumnsAndCheck(engine,
                a.column().add(1),
                count,
                context,
                this.formattedCellWithValue("$C$1", "=2+0", number(2 + 0))); // $b moved

        this.countAndCheck(cellStore, 2);

        this.loadLabelAndCheck(labelStore, LABEL, b.addColumn(-count));

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + LABEL,
                number(3));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0",
                number(2));
    }

    @Test
    public void testDeleteColumnsWithLabelToCellReferenceDeleted() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$E$1");

        engine.saveCell(this.cell(a, "=1+" + b), context);
        engine.saveCell(this.cell(b, "=2+0"), context);

        this.deleteColumnsAndCheck(engine,
                b.column(),
                1,
                context,
                this.formattedCellWithError(a, "=1+InvalidCellReference(\"" + b + "\")", "Invalid cell reference: $E$1")); // $v delete

        this.countAndCheck(cellStore, 1);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+InvalidCellReference(\"" + b + "\")",
                "Invalid cell reference: " + b); // reference should have been fixed.
    }

    @Test
    public void testDeleteColumnsWithCellReferencesFixed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$B$1");
        final SpreadsheetCellReference c = this.cellReference("$K$1"); // DELETED
        final SpreadsheetCellReference d = this.cellReference("$N$9"); // MOVED
        final SpreadsheetCellReference e = this.cellReference("$O$10"); // MOVED

        engine.saveCell(this.cell(a, "=1+" + d), context);
        engine.saveCell(this.cell(b, "=2"), context);
        engine.saveCell(this.cell(c, "=3"), context);
        engine.saveCell(this.cell(d, "=4"), context);
        engine.saveCell(this.cell(e, "=5+" + b), context); // =5+2

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                c.column(),
                count,
                context,
                this.formattedCellWithValue("$A$1", "=1+$M$9", number(1 + 4)),
                this.formattedCellWithValue("$M$9", "=4", number(4)),
                this.formattedCellWithValue("$N$10", "=5+" + b, number(5 + 2))); // $c delete

        this.countAndCheck(cellStore, 4);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+" + d.addColumn(-count),
                number(1 + 4)); // reference should have been fixed.

        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(2),
                FORMATTED_DEFAULT_SUFFIX);

        this.loadCellAndCheckFormatted2(engine,
                d.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(4),
                FORMATTED_DEFAULT_SUFFIX);

        this.loadCellAndCheckFormulaAndValue(engine,
                e.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+" + b,
                number(5 + 2));
    }

    @Test
    public void testDeleteColumnsWithCellReferencesFixed2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$B$1"); // B1
        final SpreadsheetCellReference c = this.cellReference("$K$1"); // J1 deleted
        final SpreadsheetCellReference d = this.cellReference("$N$9"); // M8 moved
        final SpreadsheetCellReference e = this.cellReference("$O$10"); // N9 moved

        engine.saveCell(this.cell(a, "=1+" + d), context);
        engine.saveCell(this.cell(b, "=2"), context);
        engine.saveCell(this.cell(c, "=3"), context); // DELETED
        engine.saveCell(this.cell(d, "=4"), context); // MOVED
        engine.saveCell(this.cell(e, "=5+" + b), context); // MOVED

        final int count = 2;
        this.deleteColumnsAndCheck(engine,
                c.column(),
                count,
                context,
                this.formattedCellWithValue("$A$1", "=1+$L$9", number(1 + 4)),
                this.formattedCellWithValue("$L$9", "=4", number(4)),
                this.formattedCellWithValue("$M$10", "=5+$B$1", number(5 + 2))); // $c delete

        this.countAndCheck(cellStore, 4);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+" + d.addColumn(-count),
                number(1 + 4)); // reference should have been fixed.

        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(2),
                FORMATTED_DEFAULT_SUFFIX);

        this.loadCellAndCheckFormatted2(engine,
                d.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(4),
                FORMATTED_DEFAULT_SUFFIX);

        this.loadCellAndCheckFormulaAndValue(engine,
                e.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+" + b,
                number(5 + 2));
    }

    // delete range....................................................................................

    @Test
    public void testDeleteColumnsWithLabelsToRangeUnmodified() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(15, 0);

        final SpreadsheetRange ab = a.spreadsheetRange(b);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, ab));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(c, "=20+0+" + LABEL), context);
        engine.saveCell(this.cell(d, "=99+0"), context); // deleted!!!

        final int count = 2;
        this.deleteColumnsAndCheck(engine,
                d.column(),
                count,
                context); // $d moved

        this.countAndCheck(cellStore, 2); // a&c
        this.countAndCheck(labelStore, 1);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0",
                number(1));

        this.loadCellAndCheckFormulaAndValue(engine,
                c,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=20+0+" + LABEL,
                number(21));

        this.loadLabelAndCheck(labelStore, LABEL, ab);
    }

    @Test
    public void testDeleteColumnsWithLabelsToRangeDeleted() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);

        final SpreadsheetRange bc = b.spreadsheetRange(c);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bc));

        engine.saveCell(this.cell(a, "=1+0"), context);

        final int count = c.column().value() - b.column().value() + 1;
        this.deleteColumnsAndCheck(engine,
                b.column(),
                count,
                context); // b..c deleted, d moved

        this.countAndCheck(cellStore, 1); // a
        this.countAndCheck(labelStore, 0);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0",
                number(1));
    }

    @Test
    public void testDeleteColumnsWithLabelsToRangeDeleted2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(20, 0);

        final SpreadsheetRange bc = b.spreadsheetRange(c);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bc));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(d, "=20+0"), context);

        final int count = c.column().value() - b.column().value() + 1;
        this.deleteColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue(d.addColumn(-count), "=20+0", number(20 + 0))); // b..c deleted, d moved

        this.countAndCheck(cellStore, 2); // a&d
        this.countAndCheck(labelStore, 0);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0",
                number(1));
    }

    @Test
    public void testDeleteColumnsWithLabelsToRangeDeleted3() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);

        final SpreadsheetRange bc = b.spreadsheetRange(c);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bc));

        engine.saveCell(this.cell(a, "=1+0+" + LABEL), context);
        engine.saveCell(this.cell(b, "=20+0"), context);

        final int count = c.column().value() - b.column().value() + 1;
        this.deleteColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithError("$A$1", "=1+0+" + LABEL, "Unknown label: " + LABEL)); // b..c deleted

        this.countAndCheck(cellStore, 1); // a
        this.countAndCheck(labelStore, 0);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + LABEL,
                "Unknown label: " + LABEL);
    }

    @Test
    public void testDeleteColumnsWithLabelsToRangeFixed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(15, 0);
        final SpreadsheetCellReference e = this.cellReference(20, 0);

        final SpreadsheetRange de = d.spreadsheetRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, de));

        engine.saveCell(this.cell(a, "=1+0+" + LABEL), context);
        engine.saveCell(this.cell(d, "=20+0"), context);

        final int count = c.column().value() - b.column().value() + 1;
        this.deleteColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue(d.addColumn(-count), "=20+0", number(20 + 0))); // b..c deleted, d moved

        this.countAndCheck(cellStore, 2); // a&d

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + LABEL,
                number(1 + 20));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=20+0",
                number(20));

        this.countAndCheck(labelStore, 1);
        final SpreadsheetCellReference begin = d.addColumn(-count);
        final SpreadsheetCellReference end = e.addColumn(-count);
        this.loadLabelAndCheck(labelStore, LABEL, begin.spreadsheetRange(end));
    }

    @SuppressWarnings("unused")
    @Test
    public void testDeleteColumnsWithLabelsToRangeFixed2() {
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(15, 0);
        final SpreadsheetCellReference e = this.cellReference(20, 0);

        final SpreadsheetRange ce = c.spreadsheetRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, ce));

        final int count = e.column().value() - d.column().value() + 1;
        this.deleteColumnsAndCheck(engine,
                d.column(),
                count,
                context); // b..c deleted, d moved

        this.countAndCheck(labelStore, 1);
        this.loadLabelAndCheck(labelStore, LABEL, c.spreadsheetRange(d));
    }

    @Test
    public void testDeleteColumnsWithLabelsToRangeFixed3() {
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(15, 0);

        final SpreadsheetRange bd = b.spreadsheetRange(d);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bd));

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                c.column(),
                count,
                context); // b..c deleted, d moved

        this.countAndCheck(labelStore, 1);

        final SpreadsheetCellReference end = d.addColumn(-count);
        this.loadLabelAndCheck(labelStore, LABEL, b.spreadsheetRange(end));
    }

    @SuppressWarnings("unused")
    @Test
    public void testDeleteColumnsWithLabelsToRangeFixed4() {
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(5, 0);  // delete
        final SpreadsheetCellReference b = this.cellReference(10, 0); // range delete
        final SpreadsheetCellReference c = this.cellReference(15, 0); // range delete
        final SpreadsheetCellReference d = this.cellReference(20, 0); // range
        final SpreadsheetCellReference e = this.cellReference(25, 0); // range

        final SpreadsheetRange be = b.spreadsheetRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, be));

        final int count = c.column().value() - a.column().value();
        this.deleteColumnsAndCheck(engine,
                a.column(),
                count,
                context);

        this.countAndCheck(labelStore, 1);

        this.loadLabelAndCheck(labelStore, LABEL, a.spreadsheetRange(b));
    }

    // insertColumn....................................................................................................

    @Test
    public void testInsertColumnsZero() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference reference = this.cellReference("$CV$1");

        engine.saveCell(this.cell(reference, "=99+0"),
                context);

        this.insertColumnsAndCheck(engine,
                reference.column(),
                0,
                context);

        this.countAndCheck(cellStore, 1);

        this.loadCellAndCheckFormulaAndValue(engine,
                reference,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99));
    }

    @Test
    public void testInsertColumns() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$B$1"); // MOVED
        final SpreadsheetCellReference c = this.cellReference("$C$1"); // MOVED

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        final int count = 1;
        this.insertColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue("$C$1", "=3+4", number(3 + 4)),
                this.formattedCellWithValue("$D$1", "=5+6", number(5 + 6)));

        this.countAndCheck(cellStore, 3);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));
    }

    @Test
    public void testInsertColumns2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$B$1"); // MOVED
        final SpreadsheetCellReference c = this.cellReference("$C$1"); // MOVED

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        final int count = 1;
        this.insertColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue("$C$1", "=3+4", number(3 + 4)),
                this.formattedCellWithValue("$D$1", "=5+6", number(5 + 6))); // $b insert

        this.countAndCheck(cellStore, 3);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+4",
                number(3 + 4));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+6",
                number(5 + 6));
    }

    @Test
    public void testInsertColumnsWithLabelToCellIgnored() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$C$2"); //
        final SpreadsheetCellReference b = this.cellReference("$E$4"); // moved

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a));

        engine.saveCell(this.cell(a, "=100"), context);
        engine.saveCell(this.cell(b, "=2+" + LABEL), context);

        final int count = 1;
        this.insertColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue("$F$4", "=2+" + LABEL, number(2 + 100))); // $b insert

        this.loadLabelAndCheck(labelStore, LABEL, a);

        this.countAndCheck(cellStore, 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=100",
                number(100));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+" + LABEL,
                number(2 + 100));
    }

    @Test
    public void testInsertColumnsWithLabelToCell() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$B$1"); // moved
        final SpreadsheetCellReference c = this.cellReference("$C$1"); // MOVED
        final SpreadsheetCellReference d = this.cellReference("$N$9"); // moved

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, d));

        engine.saveCell(this.cell(a, "=1+" + LABEL), context);
        engine.saveCell(this.cell(b, "=2+0"), context);
        engine.saveCell(this.cell(c, "=3+0+" + LABEL), context);
        engine.saveCell(this.cell(d, "=99+0"), context);

        final int count = 1;
        this.insertColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue("$C$1", "=2+0", number(2 + 0)),
                this.formattedCellWithValue("$D$1", "=3+0+" + LABEL, number(3 + 0 + 99 + 0)),
                this.formattedCellWithValue("$O$9", "=99+0", number(99 + 0))); // $b insert

        this.loadLabelAndCheck(labelStore, LABEL, d.addColumn(count));

        this.countAndCheck(cellStore, 4);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+" + LABEL,
                number(1 + 99));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0",
                number(2 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+0+" + LABEL,
                number(3 + 99));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99 + 0));
    }

    @Test
    public void testInsertColumnsWithLabelToRangeUnchanged() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$F$6"); // moved

        final SpreadsheetRange a1 = a.spreadsheetRange(a.add(1, 1));
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a1));

        engine.saveCell(this.cell(a, "=99+0"), context);
        engine.saveCell(this.cell(b, "=2+0+" + LABEL), context);

        final int count = 1;
        this.insertColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                this.formattedCellWithValue("$G$6", "=2+0+" + LABEL, number(2 + 0 + 99 + 0))); // $b insert

        this.countAndCheck(labelStore, 1);

        this.loadLabelAndCheck(labelStore, LABEL, a1);

        this.countAndCheck(cellStore, 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0+" + LABEL,
                number(2 + 99));
    }

    @Test
    public void testInsertColumnsWithLabelToRangeUpdated() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(15, 0);
        final SpreadsheetCellReference e = this.cellReference(20, 0);

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, c.spreadsheetRange(d)));

        engine.saveCell(this.cell(a, "=1+" + LABEL), context);
        engine.saveCell(this.cell(c, "=99+0"), context);

        this.insertColumnsAndCheck(engine,
                b.column(),
                c.column().value() - b.column().value(),
                context,
                this.formattedCellWithValue("$P$1", "=99+0", number(99 + 0))); // $b insert

        this.countAndCheck(labelStore, 1);
        this.loadLabelAndCheck(labelStore, LABEL, d.spreadsheetRange(e));

        this.countAndCheck(cellStore, 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+" + LABEL,
                number(1 + 99));

        this.loadCellAndCheckFormulaAndValue(engine,
                d,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99 + 0));
    }

    @Test
    public void testInsertColumnsWithCellReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$B$1"); // B1
        final SpreadsheetCellReference c = this.cellReference("$K$1"); // moved
        final SpreadsheetCellReference d = this.cellReference("$N$9"); // moved

        engine.saveCell(this.cell(a, "=1+0+" + d), context);
        engine.saveCell(this.cell(b, "=2+0"), context);
        engine.saveCell(this.cell(c, "=3+0"), context);
        engine.saveCell(this.cell(d, "=4+0+" + b), context);

        final int count = 1;
        this.insertColumnsAndCheck(engine,
                c.column(),
                count,
                context,
                this.formattedCellWithValue("$A$1", "=1+0+$O$9", number(1 + 0 + 4 + 0 + 2 + 0)),
                this.formattedCellWithValue("$L$1", "=3+0", number(3 + 0)),
                this.formattedCellWithValue("$O$9", "=4+0+" + b, number(4 + 0 + 2 + 0))); // $c insert

        this.countAndCheck(cellStore, 4);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + d.addColumn(count),
                number(1 + 0 + 4 + 2)); // reference should have been fixed.

        this.loadCellAndCheckFormulaAndValue(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0",
                number(2));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+0",
                number(3));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=4+0+" + b,
                number(4 + 2));
    }

    @Test
    public void testInsertColumnsWithCellReferences2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$B$1"); //
        final SpreadsheetCellReference c = this.cellReference("$K$1"); // moved
        final SpreadsheetCellReference d = this.cellReference("$N$9"); // moved

        engine.saveCell(this.cell(a, "=1+0+" + d), context);
        engine.saveCell(this.cell(b, "=2+0"), context);
        engine.saveCell(this.cell(c, "=3+0"), context);
        engine.saveCell(this.cell(d, "=4+0+" + b), context); // =5+2

        final int count = 2;
        this.insertColumnsAndCheck(engine,
                c.column(),
                count,
                context,
                this.formattedCellWithValue("$A$1", "=1+0+$P$9", number(1 + 0 + 4 + 0 + 2 + 0)),
                this.formattedCellWithValue("$M$1", "=3+0", number(3 + 0)),
                this.formattedCellWithValue("$P$9", "=4+0+" + b, number(4 + 0 + 2 + 0))); // $c insert

        this.countAndCheck(cellStore, 4);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + d.addColumn(count),
                number(1 + 0 + 4 + 2)); // reference should have been fixed.

        this.loadCellAndCheckFormulaAndValue(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0",
                number(2 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+0",
                number(3 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=4+0+" + b,
                number(4 + 0 + 2));
    }

    @Test
    public void testInsertColumnsSeveral() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); //
        final SpreadsheetCellReference b = this.cellReference("$K$1"); // MOVED
        final SpreadsheetCellReference c = this.cellReference("$L$1"); // MOVED
        final SpreadsheetCellReference d = this.cellReference("$M$3"); // MOVED
        final SpreadsheetCellReference e = this.cellReference("$U$4"); // MOVED

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(b, "=2+0"), context);
        engine.saveCell(this.cell(c, "=3+0"), context);
        engine.saveCell(this.cell(d, "=4+0"), context);
        engine.saveCell(this.cell(e, "=5+0"), context);

        final int count = 5;
        this.insertColumnsAndCheck(engine,
                this.column(7),
                count,
                context,
                this.formattedCellWithValue("$P$1", "=2+0", number(2 + 0)),
                this.formattedCellWithValue("$Q$1", "=3+0", number(3 + 0)),
                this.formattedCellWithValue("$R$3", "=4+0", number(4 + 0)),
                this.formattedCellWithValue("$Z$4", "=5+0", number(5 + 0))); // $b & $c

        this.countAndCheck(cellStore, 5);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0",
                number(1 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0",
                number(2 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+0",
                number(3 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=4+0",
                number(4 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                e.addColumn(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+0",
                number(5 + 0));
    }

    // insertRow....................................................................................................

    @Test
    public void testInsertRowsZero() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference reference = this.cellReference("$A$100"); // A3

        engine.saveCell(this.cell(reference, "=99+0"), context);

        this.insertRowsAndCheck(engine,
                reference.row(),
                0,
                context);

        this.countAndCheck(cellStore, 1);

        this.loadCellAndCheckFormulaAndValue(engine,
                reference,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99));
    }

    @Test
    public void testInsertRows() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$A$2"); // MOVED
        final SpreadsheetCellReference c = this.cellReference("$A$3"); // MOVED

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        final int count = 1;
        this.insertRowsAndCheck(engine,
                b.row(),
                count,
                context,
                this.formattedCellWithValue("$A$3", "=3+4", number(3 + 4)),
                this.formattedCellWithValue("$A$4", "=5+6", number(5 + 6)));

        this.countAndCheck(cellStore, 3);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addRow(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+4",
                number(3 + 4));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addRow(count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+6",
                number(5 + 6));
    }

    @Test
    public void testInsertRows2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$A$2"); // MOVED
        final SpreadsheetCellReference c = this.cellReference("$A$3"); // MOVED

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        final int count = 1;
        this.insertRowsAndCheck(engine,
                b.row(),
                count,
                context,
                this.formattedCellWithValue("$A$3", "=3+4", number(3 + 4)),
                this.formattedCellWithValue("$A$4", "=5+6", number(5 + 6))); // $b insert

        this.countAndCheck(cellStore, 3);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+4",
                number(3 + 4));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+6",
                number(5 + 6));
    }

    @Test
    public void testInsertRowsWithLabelToCellIgnored() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$B$3"); //
        final SpreadsheetCellReference b = this.cellReference("$D$5"); // moved

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a));

        engine.saveCell(this.cell(a, "=100"), context);
        engine.saveCell(this.cell(b, "=2+" + LABEL), context);

        final int count = 1;
        this.insertRowsAndCheck(engine,
                b.row(),
                count,
                context,
                this.formattedCellWithValue("$D$6", "=2+" + LABEL, number(2 + 100))); // $b insert

        this.loadLabelAndCheck(labelStore, LABEL, a);

        this.countAndCheck(cellStore, 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=100",
                number(100));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+" + LABEL,
                number(2 + 100));
    }

    @Test
    public void testInsertRowsWithLabelToCell() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$A$2"); // moved
        final SpreadsheetCellReference c = this.cellReference("$A$3"); // MOVED
        final SpreadsheetCellReference d = this.cellReference("$I$14"); // moved

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, d));

        engine.saveCell(this.cell(a, "=1+" + LABEL), context);
        engine.saveCell(this.cell(b, "=2+0"), context);
        engine.saveCell(this.cell(c, "=3+0+" + LABEL), context);
        engine.saveCell(this.cell(d, "=99+0"), context);

        final int count = 1;
        this.insertRowsAndCheck(engine,
                b.row(),
                count,
                context,
                this.formattedCellWithValue("$A$3", "=2+0", number(2 + 0)),
                this.formattedCellWithValue("$A$4", "=3+0+" + LABEL, number(3 + 0 + 99)),
                this.formattedCellWithValue("$I$15", "=99+0", number(99 + 0))); // $b insert

        this.loadLabelAndCheck(labelStore, LABEL, d.addRow(+count));

        this.countAndCheck(cellStore, 4);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+" + LABEL,
                number(1 + 99));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0",
                number(2 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+0+" + LABEL,
                number(3 + 99));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99 + 0));
    }

    @Test
    public void testInsertRowsWithLabelToRangeUnchanged() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); //
        final SpreadsheetCellReference b = this.cellReference("$F$6"); // moved

        final SpreadsheetRange a1 = a.spreadsheetRange(a.add(1, 1));
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a1));

        engine.saveCell(this.cell(a, "=99+0"), context);
        engine.saveCell(this.cell(b, "=2+0+" + LABEL), context);

        final int count = 1;
        this.insertRowsAndCheck(engine,
                b.row(),
                count,
                context,
                this.formattedCellWithValue("$F$7", "=2+0+" + LABEL, number(2 + 0 + 99 + 0))); // $b insert

        this.countAndCheck(labelStore, 1);

        this.loadLabelAndCheck(labelStore, LABEL, a1);

        this.countAndCheck(cellStore, 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0+" + LABEL,
                number(2 + 99));
    }

    @Test
    public void testInsertRowsWithLabelToRangeUpdated() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 15);
        final SpreadsheetCellReference e = this.cellReference(0, 20);

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, c.spreadsheetRange(d)));

        engine.saveCell(this.cell(a, "=1+" + LABEL), context);
        engine.saveCell(this.cell(c, "=99+0"), context);

        this.insertRowsAndCheck(engine,
                b.row(),
                c.row().value() - b.row().value(),
                context,
                this.formattedCellWithValue("$A$16", "=99+0", number(99 + 0))); // $b insert

        this.countAndCheck(labelStore, 1);
        this.loadLabelAndCheck(labelStore, LABEL, d.spreadsheetRange(e));

        this.countAndCheck(cellStore, 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+" + LABEL,
                number(1 + 99));

        this.loadCellAndCheckFormulaAndValue(engine,
                d,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99 + 0));
    }

    @Test
    public void testInsertRowsWithCellReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$A$2"); // A2
        final SpreadsheetCellReference c = this.cellReference("$A$11"); // moved
        final SpreadsheetCellReference d = this.cellReference("$I$14"); // moved

        engine.saveCell(this.cell(a, "=1+0+" + d), context);
        engine.saveCell(this.cell(b, "=2+0"), context);
        engine.saveCell(this.cell(c, "=3+0"), context);
        engine.saveCell(this.cell(d, "=4+0+" + b), context);

        final int count = 1;
        this.insertRowsAndCheck(engine,
                c.row(),
                count,
                context,
                this.formattedCellWithValue("$A$1", "=1+0+$I$15", number(3 + 4)),
                this.formattedCellWithValue("$A$12", "=3+0", number(3 + 0)),
                this.formattedCellWithValue("$I$15", "=4+0+" + b, number(4 + 0 + 2 + 0))); // $c insert

        this.countAndCheck(cellStore, 4);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + d.addRow(+count),
                number(1 + 0 + 4 + 2)); // reference should have been fixed.

        this.loadCellAndCheckFormulaAndValue(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0",
                number(2));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+0",
                number(3));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=4+0+" + b,
                number(4 + 2));
    }

    @Test
    public void testInsertRowsWithCellReferences2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$A$2"); // A2
        final SpreadsheetCellReference c = this.cellReference("$A$11"); // moved
        final SpreadsheetCellReference d = this.cellReference("$I$14"); // moved

        engine.saveCell(this.cell(a, "=1+0+" + d), context);
        engine.saveCell(this.cell(b, "=2+0"), context);
        engine.saveCell(this.cell(c, "=3+0"), context);
        engine.saveCell(this.cell(d, "=4+0+" + b), context); // =5+2

        final int count = 2;
        this.insertRowsAndCheck(engine,
                c.row(),
                count,
                context,
                this.formattedCellWithValue("$A$1", "=1+0+$I$16", number(1 + 0 + 4 + 0 + 2 + 0)),
                this.formattedCellWithValue("$A$13", "=3+0", number(3 + 0)),
                this.formattedCellWithValue("$I$16", "=4+0+" + b, number(4 + 0 + 2 + 0))); // $c insert

        this.countAndCheck(cellStore, 4);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + d.addRow(+count),
                number(1 + 0 + 4 + 2)); // reference should have been fixed.

        this.loadCellAndCheckFormulaAndValue(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0",
                number(2 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+0",
                number(3 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=4+0+" + b,
                number(4 + 0 + 2));
    }

    @Test
    public void testInsertRowsSeveral() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$A$11"); // MOVED
        final SpreadsheetCellReference c = this.cellReference("$A$12"); // MOVED
        final SpreadsheetCellReference d = this.cellReference("$C$13"); // MOVED
        final SpreadsheetCellReference e = this.cellReference("$D$21"); // MOVED

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(b, "=2+0"), context);
        engine.saveCell(this.cell(c, "=3+0"), context);
        engine.saveCell(this.cell(d, "=4+0"), context);
        engine.saveCell(this.cell(e, "=5+0"), context);

        final int count = 5;
        this.insertRowsAndCheck(engine,
                this.row(7),
                count,
                context,
                this.formattedCellWithValue("$A$16", "=2+0", number(2 + 0)),
                this.formattedCellWithValue("$A$17", "=3+0", number(3 + 0)),
                this.formattedCellWithValue("$C$18", "=4+0", number(4 + 0)),
                this.formattedCellWithValue("$D$26", "=5+0", number(5 + 0))); // $b & $c

        this.countAndCheck(cellStore, 5);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0",
                number(1 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                b.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=2+0",
                number(2 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=3+0",
                number(3 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                d.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=4+0",
                number(4 + 0));

        this.loadCellAndCheckFormulaAndValue(engine,
                e.addRow(+count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+0",
                number(5 + 0));
    }

    // fillCells........................................................................................................

    // fill deletes.....................................................................................................

    @Test
    public void testFillCellsDeleteOneCell() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(5, 5);
        final SpreadsheetCell cellA = this.cell(a, "=1+0");

        cellStore.save(cellA);

        final SpreadsheetRange rangeA = a.spreadsheetRange(a);

        this.fillCellsAndCheck(engine,
                SpreadsheetDelta.NO_CELLS,
                rangeA,
                rangeA,
                context);

        this.countAndCheck(cellStore, 0); // a deleted
    }

    @Test
    public void testFillCellsDeleteOneCell2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(5, 5);
        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        cellStore.save(cellA);

        final SpreadsheetCellReference b = this.cellReference(10, 10);
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        cellStore.save(cellB);

        final SpreadsheetRange rangeA = a.spreadsheetRange(a);

        this.fillCellsAndCheck(engine,
                SpreadsheetDelta.NO_CELLS,
                rangeA,
                rangeA,
                context);

        this.countAndCheck(cellStore, 1); // a deleted

        this.loadCellAndCheck(engine,
                b,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                this.formattedCellWithValue(b, "=2+0", number(2)));
    }

    @Test
    public void testFillCellsDeletesManyCells() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(5, 5);
        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        cellStore.save(cellA);

        final SpreadsheetCellReference b = this.cellReference(6, 6);
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        cellStore.save(cellB);

        final SpreadsheetRange rangeA = a.spreadsheetRange(b);

        this.fillCellsAndCheck(engine,
                SpreadsheetDelta.NO_CELLS,
                rangeA,
                rangeA,
                context);

        this.countAndCheck(cellStore, 0); // a deleted
    }

    @Test
    public void testFillCellsDeletesManyCells2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(5, 5);
        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        cellStore.save(cellA);

        final SpreadsheetCellReference b = this.cellReference(6, 6);
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        cellStore.save(cellB);

        final SpreadsheetRange rangeA = a.spreadsheetRange(b);

        final SpreadsheetCellReference c = this.cellReference(10, 10);
        final SpreadsheetCell cellC = this.cell(c, "=3+0");
        cellStore.save(cellC);

        this.fillCellsAndCheck(engine,
                SpreadsheetDelta.NO_CELLS,
                rangeA,
                rangeA,
                context);

        this.countAndCheck(cellStore, 1); // a&b deleted, leaving c

        this.loadCellAndCheck(engine,
                c,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                this.formattedCellWithValue(c, "=3+0", number(3)));
    }

    // fill save with missing cells......................................................................................

    @Test
    public void testFillCellsSaveWithMissingCells() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 2);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");

        final SpreadsheetRange range = a.spreadsheetRange(b);

        this.fillCellsAndCheck(engine,
                Sets.of(cellA, cellB),
                range,
                range,
                context,
                this.formattedCellWithValue(a, "=1+0", number(1)),
                this.formattedCellWithValue(b, "=2+0", number(2)));

        this.countAndCheck(cellStore, 2); // a + b saved

        this.loadCellAndCheck(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                this.formattedCellWithValue(a, "=1+0", number(1)));

        this.loadCellAndCheck(engine,
                b,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                this.formattedCellWithValue(b, "=2+0", number(2)));
    }

    @Test
    public void testFillCellsSaveWithMissingCells2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 2);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");

        final SpreadsheetRange range = a.spreadsheetRange(b);

        final SpreadsheetCellReference c = this.cellReference(10, 10);
        final SpreadsheetCell cellC = this.cell(c, "=3+0");
        cellStore.save(cellC);

        this.fillCellsAndCheck(engine,
                Sets.of(cellA, cellB),
                range,
                range,
                context,
                this.formattedCellWithValue(a, "=1+0", number(1)),
                this.formattedCellWithValue(b, "=2+0", number(2)));

        this.countAndCheck(cellStore, 3); // a + b saved + c

        this.loadCellAndCheck(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                this.formattedCellWithValue(a, "=1+0", number(1))); // fill should have evaluated.

        this.loadCellAndCheck(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                this.formattedCellWithValue(b, "=2+0", number(2)));

        this.loadCellAndCheck(engine,
                c,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                cellC);
    }

    // fill moves cell..................................................................................................

    @Test
    public void testFillCellsRangeOneEmptyCells2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 1);
        final SpreadsheetCellReference c = this.cellReference(1, 2);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        final SpreadsheetCell cellC = this.cell(c, "=3+0");

        cellStore.save(cellA);
        cellStore.save(cellB);
        cellStore.save(cellC);

        this.fillCellsAndCheck(engine,
                SpreadsheetDelta.NO_CELLS,
                a.spreadsheetRange(a),
                SpreadsheetRange.fromCells(Lists.of(b)),
                context);

        this.countAndCheck(cellStore, 2); // a + c, b deleted

        this.loadCellAndCheck(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                this.formattedCellWithValue(a, "=1+0", number(1)));

        this.loadCellAndCheck(engine,
                c,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                this.formattedCellWithValue(c, "=3+0", number(3)));
    }

    @Test
    public void testFillCellsRangeTwoEmptyCells() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 1);
        final SpreadsheetCellReference c = this.cellReference(1, 2);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        final SpreadsheetCell cellC = this.cell(c, "=3+0");

        cellStore.save(cellA);
        cellStore.save(cellB);
        cellStore.save(cellC);

        this.fillCellsAndCheck(engine,
                SpreadsheetDelta.NO_CELLS,
                a.spreadsheetRange(a),
                SpreadsheetRange.fromCells(Lists.of(a, b)),
                context);

        this.countAndCheck(cellStore, 1);

        this.loadCellAndCheck(engine,
                c,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                this.formattedCellWithValue(c, "=3+0", number(3)));
    }

    // fill moves 1 cell................................................................................................

    @Test
    public void testFillCellsAddition() {
        this.fillCellsAndCheck("=1+0", number(1 + 0));
    }

    @Test
    public void testFillCellsExpressionNumber() {
        this.fillCellsAndCheck("=99.5", number(99.5));
    }

    @Test
    public void testFillCellsExpressionNumber2() {
        this.fillCellsAndCheck("=99", number(99));
    }

    @Test
    public void testFillCellsDivision() {
        this.fillCellsAndCheck("=10/5", number(10 / 5));
    }

    @Test
    public void testFillCellsEqualsTrue() {
        this.fillCellsAndCheck("=10==10", true);
    }

    @Test
    public void testFillCellsEqualsFalse() {
        this.fillCellsAndCheck("=10==9", false);
    }

    @Test
    public void testFillCellsGreaterThanTrue() {
        this.fillCellsAndCheck("=10>9", true);
    }

    @Test
    public void testFillCellsGreaterThanFalse() {
        this.fillCellsAndCheck("=10>11", false);
    }

    @Test
    public void testFillCellsGreaterThanEqualsTrue() {
        this.fillCellsAndCheck("=10>=10", true);
    }

    @Test
    public void testFillCellsGreaterThanEqualsFalse() {
        this.fillCellsAndCheck("=10>=11", false);
    }

    @Test
    public void testFillCellsFunction() {
        this.fillCellsAndCheck("=BasicSpreadsheetEngineTestSum(1;99)", number(1 + 99));
    }

    @Test
    public void testFillCellsGroup() {
        this.fillCellsAndCheck("=(99)", number(99));
    }

    @Test
    public void testFillCellsLessThanTrue() {
        this.fillCellsAndCheck("=10<11", true);
    }

    @Test
    public void testFillCellsLessThanFalse() {
        this.fillCellsAndCheck("=10<9", false);
    }

    @Test
    public void testFillCellsLessThanEqualsTrue() {
        this.fillCellsAndCheck("=10<=10", true);
    }

    @Test
    public void testFillCellsLessThanEqualsFalse() {
        this.fillCellsAndCheck("=10<=9", false);
    }

    @Test
    public void testFillCellsMultiplication() {
        this.fillCellsAndCheck("=6*7", number(6 * 7));
    }

    @Test
    public void testFillCellsNegative() {
        this.fillCellsAndCheck("=-123", number(-123));
    }

    @Test
    public void testFillCellsNotEqualsTrue() {
        this.fillCellsAndCheck("=10!=9", true);
    }

    @Test
    public void testFillCellsNotEqualsFalse() {
        this.fillCellsAndCheck("=10!=10", false);
    }

    @Test
    public void testFillCellsPercentage() {
        this.fillCellsAndCheck("=123.5%", number(123.5 / 100));
    }

    @Test
    public void testFillCellsSubtraction() {
        this.fillCellsAndCheck("=13-4", number(13 - 4));
    }

    @Test
    public void testFillCellsText() {
        this.fillCellsAndCheck("=\"abc123\"", "abc123");
    }

    @Test
    public void testFillCellsAdditionWithWhitespace() {
        this.fillCellsAndCheck("=1 + 2", number(1 + 2));
    }

    private void fillCellsAndCheck(final String formulaText, final Object expected) {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetCellReference a = this.cellReference(10, 20);
        final SpreadsheetCellReference b = this.cellReference(11, 21);
        final SpreadsheetCellReference c = this.cellReference(12, 22);

        final SpreadsheetCell cellA = this.cell(a, formulaText);
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        final SpreadsheetCell cellC = this.cell(c, "=3+0");

        cellStore.save(cellA);
        cellStore.save(cellB);
        cellStore.save(cellC);

        final SpreadsheetCellReference d = this.cellReference(30, 40);

        this.fillCellsAndCheck(engine,
                Lists.of(cellA),
                a.spreadsheetRange(a),
                d.spreadsheetRange(d),
                context,
                this.formattedCellWithValue(d, formulaText, expected));

        this.countAndCheck(cellStore, 3 + 1);
    }

    @Test
    public void testFillCellsRepeatCellInto2x2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(10, 20);
        final SpreadsheetCellReference b = this.cellReference(11, 21);
        final SpreadsheetCellReference c = this.cellReference(12, 22);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        final SpreadsheetCell cellC = this.cell(c, "=3+0");

        cellStore.save(cellA);
        cellStore.save(cellB);
        cellStore.save(cellC);

        final SpreadsheetCellReference d = this.cellReference(30, 40);

        this.fillCellsAndCheck(engine,
                Lists.of(cellA, cellB),
                SpreadsheetRange.fromCells(Lists.of(a, b)),
                d.spreadsheetRange(d.add(2, 2)),
                context,
                this.formattedCellWithValue(d, "=1+0", number(1 + 0)),
                this.formattedCellWithValue(d.add(1, 1), "=2+0", number(2 + 0)));

        this.countAndCheck(cellStore, 3 + 2);
    }

    @Test
    public void testFillCells2x2CellInto1x1() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(10, 20);
        final SpreadsheetCellReference b = this.cellReference(11, 21);
        final SpreadsheetCellReference c = this.cellReference(12, 22);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        final SpreadsheetCell cellC = this.cell(c, "=3+0");

        cellStore.save(cellA);
        cellStore.save(cellB);
        cellStore.save(cellC);

        final SpreadsheetCellReference d = this.cellReference(30, 40);

        this.fillCellsAndCheck(engine,
                Lists.of(cellA, cellB),
                SpreadsheetRange.fromCells(Lists.of(a, b)),
                d.spreadsheetRange(d.add(1, 1)),
                context,
                this.formattedCellWithValue(d, "=1+0", number(1 + 0)),
                this.formattedCellWithValue(d.add(1, 1), "=2+0", number(2 + 0)));

        this.countAndCheck(cellStore, 3 + 2);
    }

    @Test
    public void testFillCells2x2Into2x2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(10, 20);
        final SpreadsheetCellReference b = this.cellReference(11, 21);
        final SpreadsheetCellReference c = this.cellReference(12, 22);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        final SpreadsheetCell cellC = this.cell(c, "=3+0");

        cellStore.save(cellA);
        cellStore.save(cellB);
        cellStore.save(cellC);

        final SpreadsheetCellReference d = this.cellReference(30, 40);

        this.fillCellsAndCheck(
                engine,
                Lists.of(cellA, cellB),
                SpreadsheetRange.fromCells(Lists.of(a, b)),
                d.spreadsheetRange(d.add(2, 2)),
                context,
                this.formattedCellWithValue(d, "=1+0", number(1 + 0)),
                this.formattedCellWithValue(d.add(1, 1), "=2+0", number(2 + 0)));

        this.countAndCheck(cellStore, 3 + 2);
    }

    @Test
    public void testFillCells2x2Into7x2Gives6x2() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(10, 20);
        final SpreadsheetCellReference b = this.cellReference(11, 21);
        final SpreadsheetCellReference c = this.cellReference(12, 22);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        final SpreadsheetCell cellC = this.cell(c, "=3+0");

        cellStore.save(cellA);
        cellStore.save(cellB);
        cellStore.save(cellC);

        final SpreadsheetCellReference d = this.cellReference(30, 40);

        this.fillCellsAndCheck(engine,
                Lists.of(cellA, cellB),
                SpreadsheetRange.fromCells(Lists.of(a, b)),
                d.spreadsheetRange(d.add(6, 1)),
                context,
                this.formattedCellWithValue(d, "=1+0", number(1 + 0)),
                this.formattedCellWithValue(d.add(1, 1), "=2+0", number(2 + 0)),
                this.formattedCellWithValue(d.add(2, 0), "=1+0", number(1 + 0)),
                this.formattedCellWithValue(d.add(3, 1), "=2+0", number(2 + 0)),
                this.formattedCellWithValue(d.add(4, 0), "=1+0", number(1 + 0)),
                this.formattedCellWithValue(d.add(5, 1), "=2+0", number(2 + 0)));

        this.countAndCheck(cellStore, 3 + 6);
    }

    @Test
    public void testFillCells2x2Into2x7Gives2x6() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(10, 20);
        final SpreadsheetCellReference b = this.cellReference(11, 21);
        final SpreadsheetCellReference c = this.cellReference(12, 22);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        final SpreadsheetCell cellC = this.cell(c, "=3+0");

        cellStore.save(cellA);
        cellStore.save(cellB);
        cellStore.save(cellC);

        final SpreadsheetCellReference d = this.cellReference(30, 40);

        this.fillCellsAndCheck(engine,
                Lists.of(cellA, cellB),
                SpreadsheetRange.fromCells(Lists.of(a, b)),
                d.spreadsheetRange(d.add(1, 6)),
                context,
                this.formattedCellWithValue(d, "=1+0", number(1 + 0)),
                this.formattedCellWithValue(d.add(1, 1), "=2+0", number(2 + 0)),
                this.formattedCellWithValue(d.addRow(2), "=1+0", number(1 + 0)),
                this.formattedCellWithValue(d.add(1, 3), "=2+0", number(2 + 0)),
                this.formattedCellWithValue(d.addRow(4), "=1+0", number(1 + 0)),
                this.formattedCellWithValue(d.add(1, 5), "=2+0", number(2 + 0)));

        this.countAndCheck(cellStore, 3 + 6);
    }

    @Test
    public void testFillCellsAbsoluteReference() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(10, 20);
        final SpreadsheetCellReference b = this.cellReference(11, 21);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=" + a);

        cellStore.save(cellA);
        cellStore.save(cellB);

        final SpreadsheetCellReference d = this.cellReference(30, 40);

        this.fillCellsAndCheck(engine,
                Lists.of(cellB),
                b.spreadsheetRange(b),
                d.spreadsheetRange(d),
                context,
                this.formattedCellWithValue(a, "=1+0", number(1 + 0)),
                this.formattedCellWithValue(d, "=" + a, number(1 + 0)));

        this.countAndCheck(cellStore, 2 + 1);
    }

    @Test
    public void testFillCellsExpressionRelativeReferenceFixed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell cellB = this.cell("B2", "=2");
        final SpreadsheetCell cellC = this.cell("C3", "=3+B2");

        cellStore.save(cellB);
        cellStore.save(cellC);

        this.fillCellsAndCheck(engine,
                Lists.of(cellB, cellC),
                cellB.reference().spreadsheetRange(cellC.reference()),
                SpreadsheetExpressionReference.parseRange("E5:F6"),
                context,
                this.formattedCellWithValue("E5", "=2", number(2 + 0)),
                this.formattedCellWithValue("F6", "=3+E5", number(3 + 2)));

        this.countAndCheck(cellStore, 2 + 2);
    }

    @Test
    public void testFillCellsExternalReferencesRefreshed() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore);
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference b = this.cellReference("b1");
        final SpreadsheetCell cellB = this.cell(b, "=2+0"); // copied to C1
        final SpreadsheetCellReference c = this.cellReference("C1"); // fillCells dest...
        final SpreadsheetCell cellA = this.cell("a1", "=10+" + c);

        engine.saveCell(cellA, context);
        engine.saveCell(cellB, context);

        this.fillCellsAndCheck(engine,
                Lists.of(cellB),
                b.spreadsheetRange(b),
                c.spreadsheetRange(c),
                context,
                this.formattedCellWithValue(cellA.reference(), "=10+" + c, number(10 + 2 + 0)), // external reference to copied
                this.formattedCellWithValue(c, "=2+0", number(2 + 0))); // copied

        this.countAndCheck(cellStore, 2 + 1);
    }

    //  loadLabel.......................................................................................................

    @Test
    public void testLoadLabelUnknownFails() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetLabelName label = SpreadsheetExpressionReference.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetExpressionReference.parseCellReference("B2"));

        this.saveLabelAndCheck(engine,
                mapping,
                context);

        this.loadLabelAndFailCheck(
                engine,
                SpreadsheetExpressionReference.labelName("UnknownLabel"),
                this.createContext()
        );
    }

    //  saveLabel.......................................................................................................

    @Test
    public void testSaveLabelAndLoadFromLabelStore() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetLabelName label = SpreadsheetExpressionReference.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetExpressionReference.parseCellReference("B2"));

        this.saveLabelAndCheck(engine,
                mapping,
                context);

        this.loadLabelAndCheck(labelStore,
                label,
                mapping);
    }

    @Test
    public void testSaveLabelAndLoadLabel() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetLabelName label = SpreadsheetExpressionReference.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetExpressionReference.parseCellReference("B2"));

        this.saveLabelAndCheck(engine,
                mapping,
                context);

        this.loadLabelAndCheck(engine,
                label,
                context,
                mapping);
    }

    @Test
    public void testSaveLabelWithoutReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetLabelName label = SpreadsheetExpressionReference.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetExpressionReference.parseCellReference("B2"));

        engine.saveCell(this.cell("B2", "=99"), context);

        this.saveLabelAndCheck(engine,
                mapping,
                context);

        engine.saveCell(this.cell("A1", label + "+1"), context);
    }

    @Test
    public void testSaveLabelRefreshesReferences() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetLabelName label = SpreadsheetExpressionReference.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetExpressionReference.parseCellReference("B2"));

        engine.saveCell(this.cell("A1", "=" + label + "+1"), context);
        engine.saveCell(this.cell("B2", "=99"), context);

        this.saveLabelAndCheck(engine,
                mapping,
                context,
                this.formattedCellWithValue("A1", "=" + label + "+1", number(99 + 1)));
    }

    //  removeLabel.......................................................................................................

    @Test
    public void testRemoveLabelAndLoadFromLabelStore() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetLabelName label = SpreadsheetExpressionReference.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetExpressionReference.parseCellReference("B2"));

        this.saveLabelAndCheck(engine,
                mapping,
                context);

        this.removeLabelAndCheck(engine,
                label,
                context);

        this.loadLabelFailCheck(labelStore, label);
    }

    @Test
    public void testRemoveLabelRefreshesCell() {
        final SpreadsheetCellStore cellStore = this.cellStore();
        final SpreadsheetLabelStore labelStore = this.labelStore();
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine(cellStore, labelStore);
        final SpreadsheetEngineContext context = this.createContext(labelStore, engine);

        final SpreadsheetLabelName label = SpreadsheetExpressionReference.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetExpressionReference.parseCellReference("B2"));

        engine.saveCell(this.cell("A1", "=" + label + "+1"), context);
        engine.saveCell(this.cell("B2", "=99"), context);

        engine.saveLabel(mapping, context);

        this.removeLabelAndCheck(engine,
                label,
                context,
                this.formattedCellWithError("A1", "=" + label + "+1", "Unknown label: " + label));

        this.loadLabelFailCheck(labelStore, label);
    }

    // columnWidth, rowHeight...........................................................................................

    @Test
    public void testColumnWidth() {
        final SpreadsheetColumnReference column = SpreadsheetColumnReference.parseColumn("Z");
        final double expected = 150.5;
        final BasicSpreadsheetEngine engine = this.createEngine(
                this.metadata(),
                new FakeSpreadsheetCellStore() {
                    @Override
                    public double maxColumnWidth(final SpreadsheetColumnReference c) {
                        assertEquals(column, c);
                        return expected;
                    }
                });

        this.columnWidthAndCheck(engine, column, expected);
    }

    @Test
    public void testColumnWidthDefaults() {
        final SpreadsheetColumnReference column = SpreadsheetColumnReference.parseColumn("Z");
        final double expected = 150.5;

        final SpreadsheetMetadata metadata = SpreadsheetMetadata.NON_LOCALE_DEFAULTS;
        final TextStyle style = metadata.getOrFail(SpreadsheetMetadataPropertyName.STYLE)
                .set(TextStylePropertyName.WIDTH, Length.pixel(expected));

        final BasicSpreadsheetEngine engine = this.createEngine(
                metadata.set(SpreadsheetMetadataPropertyName.STYLE, style),
                new FakeSpreadsheetCellStore() {
                    @Override
                    public double maxColumnWidth(final SpreadsheetColumnReference c) {
                        assertEquals(column, c);
                        return 0;
                    }
                });

        this.columnWidthAndCheck(engine, column, expected);
    }

    @Test
    public void testColumnWidthDefaultMissing() {
        final SpreadsheetColumnReference column = SpreadsheetColumnReference.parseColumn("Z");
        final BasicSpreadsheetEngine engine = this.createEngine(
                SpreadsheetMetadata.EMPTY,
                new FakeSpreadsheetCellStore() {
                    @Override
                    public double maxColumnWidth(final SpreadsheetColumnReference c) {
                        assertEquals(column, c);
                        return 0;
                    }
                });
        assertThrows(TextStylePropertyValueException.class, () -> engine.columnWidth(column));
    }

    @Test
    public void testRowHeight() {
        final SpreadsheetRowReference row = SpreadsheetRowReference.parseRow("987");
        final double expected = 150.5;
        final BasicSpreadsheetEngine engine = this.createEngine(
                SpreadsheetMetadata.EMPTY,
                new FakeSpreadsheetCellStore() {
                    @Override
                    public double maxRowHeight(final SpreadsheetRowReference c) {
                        assertEquals(row, c);
                        return expected;
                    }
                });

        this.rowHeightAndCheck(engine, row, expected);
    }

    @Test
    public void testRowHeightDefaults() {
        final SpreadsheetRowReference row = SpreadsheetRowReference.parseRow("987");
        final double expected = 150.5;

        final SpreadsheetMetadata metadata = SpreadsheetMetadata.NON_LOCALE_DEFAULTS;
        final TextStyle style = metadata.getOrFail(SpreadsheetMetadataPropertyName.STYLE)
                .set(TextStylePropertyName.HEIGHT, Length.pixel(expected));

        final BasicSpreadsheetEngine engine = this.createEngine(
                metadata.set(SpreadsheetMetadataPropertyName.STYLE, style),
                new FakeSpreadsheetCellStore() {
                    @Override
                    public double maxRowHeight(final SpreadsheetRowReference c) {
                        assertEquals(row, c);
                        return 0;
                    }
                });

        this.rowHeightAndCheck(engine, row, expected);
    }

    @Test
    public void testRowHeightDefaultMissing() {
        final SpreadsheetRowReference row = SpreadsheetRowReference.parseRow("999");
        final BasicSpreadsheetEngine engine = this.createEngine(
                SpreadsheetMetadata.EMPTY,
                new FakeSpreadsheetCellStore() {

                    @Override
                    public double maxRowHeight(final SpreadsheetRowReference r) {
                        assertEquals(row, r);
                        return 0;
                    }
                });
        assertThrows(TextStylePropertyValueException.class, () -> engine.rowHeight(row));
    }

    //  computeRange....................................................................................................

    @Test
    public void testComputeRangeA1() {
        this.computeRangeAndCheck(
                SpreadsheetViewport.parseViewport("A1:" + WIDTH * 3 + ":" + HEIGHT * 2),
                SpreadsheetRange.parseRange("A1:D3")
        );
    }

    @Test
    public void testComputeRangeB2() {
        this.computeRangeAndCheck(
                SpreadsheetViewport.parseViewport("B2:" + WIDTH * 3 + ":" + HEIGHT * 2),
                SpreadsheetRange.parseRange("B2:E4")
        );
    }

    @Test
    public void testComputeRange3() {
        final SpreadsheetCellReference cell = SpreadsheetCellReference.parseCellReference("C3");
        final int column = 7;
        final int row = 4;

        this.computeRangeAndCheck(
                SpreadsheetViewport.parseViewport(cell + ":" + WIDTH * column + ":" + HEIGHT * row),
                SpreadsheetRange.parseRange(cell + ":" + cell.addColumn(column).addRow(row))
        );
    }

    //  cellBox.........................................................................................................

    @Test
    public void testCellBox() {
        final SpreadsheetCellBox box = SpreadsheetCellReference.parseCellReference("B999")
                .cellBox(12, 34, 56, 78);
        final SpreadsheetCoordinates coords = SpreadsheetCoordinates.with(1, 2);

        final BasicSpreadsheetEngine engine = this.createEngine(
                SpreadsheetMetadata.EMPTY,
                new FakeSpreadsheetCellStore() {

                    @Override
                    public SpreadsheetCellBox cellBox(final SpreadsheetCoordinates c) {
                        assertEquals(coords, c);
                        return box;
                    }
                });
        assertEquals(box,
                engine.cellBox(coords));
    }

    private BasicSpreadsheetEngine createEngine(final SpreadsheetMetadata metadata,
                                                final SpreadsheetCellStore store) {
        return BasicSpreadsheetEngine.with(
                metadata,
                store,
                SpreadsheetExpressionReferenceStores.fake(),
                SpreadsheetLabelStores.fake(),
                SpreadsheetExpressionReferenceStores.fake(),
                SpreadsheetRangeStores.fake(),
                SpreadsheetRangeStores.fake());
    }

    //  helpers.......................................................................................................

    @Override
    public BasicSpreadsheetEngine createSpreadsheetEngine() {
        return this.createSpreadsheetEngine(this.cellStore());
    }

    private BasicSpreadsheetEngine createSpreadsheetEngine(final SpreadsheetCellStore cellStore) {
        return this.createSpreadsheetEngine(cellStore,
                this.labelStore());
    }

    private BasicSpreadsheetEngine createSpreadsheetEngine(final SpreadsheetLabelStore labelStore) {
        return this.createSpreadsheetEngine(this.cellStore(),
                labelStore);
    }

    private BasicSpreadsheetEngine createSpreadsheetEngine(final SpreadsheetCellStore cellStore,
                                                           final SpreadsheetLabelStore labelStore) {
        return this.createSpreadsheetEngine(cellStore,
                labelStore,
                this.rangeToConditionalFormattingRuleStore());
    }

    private BasicSpreadsheetEngine createSpreadsheetEngine(final SpreadsheetCellStore cellStore,
                                                           final SpreadsheetLabelStore labelStore,
                                                           final SpreadsheetRangeStore<SpreadsheetConditionalFormattingRule> rangeToRules) {
        return BasicSpreadsheetEngine.with(
                this.metadata(),
                cellStore,
                this.cellReferencesStore(),
                labelStore,
                this.labelReferencesStore(),
                SpreadsheetRangeStores.readOnly(this.rangeToCellStore()),
                rangeToRules);
    }

    private BasicSpreadsheetEngine createSpreadsheetEngine(final SpreadsheetCellStore cellStore,
                                                           final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferencesStore,
                                                           final SpreadsheetLabelStore labelStore) {
        return this.createSpreadsheetEngine(cellStore,
                cellReferencesStore,
                labelStore,
                this.labelReferencesStore());
    }

    private BasicSpreadsheetEngine createSpreadsheetEngine(final SpreadsheetCellStore cellStore,
                                                           final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferencesStore,
                                                           final SpreadsheetLabelStore labelStore,
                                                           final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore) {
        return BasicSpreadsheetEngine.with(
                this.metadata(),
                cellStore,
                cellReferencesStore,
                labelStore,
                labelReferencesStore,
                SpreadsheetRangeStores.readOnly(this.rangeToCellStore()),
                this.rangeToConditionalFormattingRuleStore()
        );
    }

    @Override
    public SpreadsheetEngineContext createContext() {
        return new FakeSpreadsheetEngineContext() {
            @Override
            public SpreadsheetCellReference resolveCellReference(final SpreadsheetExpressionReference reference) {
                if (reference.isCellReference()) {
                    return (SpreadsheetCellReference) reference;
                }
                assertEquals(LABEL, reference.toString());
                return LABEL_CELL;
            }
        };
    }

    private SpreadsheetEngineContext createContext(final BasicSpreadsheetEngine engine) {
        return this.createContext(SpreadsheetLabelStores.fake(), engine);
    }

    private SpreadsheetEngineContext createContext(final int defaultYear,
                                                   final BasicSpreadsheetEngine engine) {
        return this.createContext(
                defaultYear,
                SpreadsheetLabelStores.fake(),
                engine
        );
    }

    private SpreadsheetEngineContext createContext(final SpreadsheetLabelStore labelStore,
                                                   final BasicSpreadsheetEngine engine) {
        return this.createContext(
                DEFAULT_YEAR,
                labelStore,
                engine
        );
    }

    private SpreadsheetEngineContext createContext(final int defaultYear,
                                                   final SpreadsheetLabelStore labelStore,
                                                   final BasicSpreadsheetEngine engine) {
        return new FakeSpreadsheetEngineContext() {

            @Override
            public int defaultYear() {
                return defaultYear;
            }

            @Override
            public ExpressionNumberKind expressionNumberKind() {
                return BasicSpreadsheetEngineTest.this.expressionNumberKind();
            }

            @Override
            public SpreadsheetParserToken parseFormula(final String formula) {
                return SpreadsheetParsers.valueOrExpression(BasicSpreadsheetEngineTest.this.metadata().parser())
                        .orFailIfCursorNotEmpty(ParserReporters.basic())
                        .parse(
                                TextCursors.charSequence(formula),
                                SpreadsheetParserContexts.basic(
                                        DateTimeContexts.fake(),
                                        converterContext(),
                                        this.expressionNumberKind(),
                                        VALUE_SEPARATOR
                                )
                        )
                        .get()
                        .cast(SpreadsheetParserToken.class);
            }

            @Override
            public Object evaluate(final Expression node) {
                // throw an exception which is an "error" when the invalidCellReference function appears in a formula and executed
                final Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>> functions = (name) -> {
                    assertEquals(SpreadsheetFormula.INVALID_CELL_REFERENCE.value(), "InvalidCellReference");
                    switch (name.value()) {
                        case "InvalidCellReference":
                            return new FakeExpressionFunction<>() {
                                @Override
                                public Object apply(final List<Object> parameters,
                                                    final ExpressionFunctionContext context) {
                                    throw new ExpressionEvaluationException("Invalid cell reference: " + parameters.get(0).toString());
                                }

                                @Override
                                public boolean resolveReferences() {
                                    return true;
                                }
                            };
                        case "BasicSpreadsheetEngineTestSum":
                            return new FakeExpressionFunction<>() {
                                @Override
                                public Object apply(final List<Object> parameters,
                                                    final ExpressionFunctionContext context) {
                                    return parameters.stream()
                                            .map(ExpressionNumber.class::cast)
                                            .reduce(context.expressionNumberKind().create(0), (l, r) -> l.add(r, context));
                                }

                                @Override
                                public boolean resolveReferences() {
                                    return true;
                                }
                            };
                        case "BasicSpreadsheetEngineTestCounter":
                            return new FakeExpressionFunction<>() {
                                @Override
                                public Object apply(final List<Object> parameters,
                                                    final ExpressionFunctionContext context) {
                                    return BasicSpreadsheetEngineTest.this.counter;
                                }

                                @Override
                                public boolean resolveReferences() {
                                    return true;
                                }
                            };
                        default:
                            throw new UnknownExpressionFunctionException(name);
                    }
                };

                return node.toValue(ExpressionEvaluationContexts.basic(this.expressionNumberKind(),
                        functions,
                        SpreadsheetEngineExpressionEvaluationContextExpressionReferenceExpressionFunction.with(engine, labelStore, this),
                        converterContext()));
            }

            @Override
            public <T> Either<T, String> convert(final Object value, final Class<T> target) {
                assertEquals(Boolean.class, target, "Only support converting to Boolean=" + value);
                return Either.left(target.cast(Boolean.parseBoolean(String.valueOf(value))));
            }

            @Override
            public MathContext mathContext() {
                return MATH_CONTEXT;
            }

            @Override
            public SpreadsheetFormatter parsePattern(final String pattern) {
                if (PATTERN_COLOR.equals(pattern)) {
                    return formatter(pattern, COLOR, FORMATTED_PATTERN_SUFFIX);
                }
                if (PATTERN.equals(pattern)) {
                    return formatter(pattern, SpreadsheetText.WITHOUT_COLOR, FORMATTED_PATTERN_SUFFIX);
                }
                if (PATTERN_FORMAT_FAIL.equals(pattern)) {
                    return new SpreadsheetFormatter() {
                        @Override
                        public boolean canFormat(final Object value,
                                                 final SpreadsheetFormatterContext context) throws SpreadsheetFormatException {
                            return true;
                        }

                        @Override
                        public Optional<SpreadsheetText> format(final Object value, final SpreadsheetFormatterContext context) throws SpreadsheetFormatException {
                            return Optional.empty();
                        }
                    };
                }

                throw new AssertionError("Unknown pattern=" + pattern + " expected one of " + PATTERN_FORMAT_FAIL + "|" + PATTERN + "|" + PATTERN_COLOR);
            }

            @Override
            public SpreadsheetFormatter defaultSpreadsheetFormatter() {
                return BasicSpreadsheetEngineTest.this.defaultSpreadsheetFormatter();
            }

            @Override
            public Optional<SpreadsheetText> format(final Object value,
                                                    final SpreadsheetFormatter formatter) {
                assertFalse(value instanceof Optional, () -> "Value must not be optional" + value);
                return formatter.format(Cast.to(value), SPREADSHEET_TEXT_FORMAT_CONTEXT);
            }
        };
    }

    private Object counter;

    private SpreadsheetFormatter defaultSpreadsheetFormatter() {
        final String suffix = " \"" + FORMATTED_DEFAULT_SUFFIX + "\"";
        return SpreadsheetFormatters.chain(
                Lists.of(
                        SpreadsheetPattern.parseDateFormatPattern(DATE_PATTERN + suffix).formatter(),
                        SpreadsheetPattern.parseDateTimeFormatPattern(DATETIME_PATTERN + suffix).formatter(),
                        SpreadsheetPattern.parseNumberFormatPattern(NUMBER_PATTERN + suffix).formatter(),
                        SpreadsheetPattern.parseTimeFormatPattern(TIME_PATTERN + suffix).formatter(),
                        SpreadsheetPattern.parseTextFormatPattern(TEXT_PATTERN + suffix).formatter()
                )
        );
    }

    private SpreadsheetFormatter formatter(final String pattern,
                                           final Optional<Color> color,
                                           final String suffix) {
        return new SpreadsheetFormatter() {

            @Override
            public boolean canFormat(final Object value,
                                     final SpreadsheetFormatterContext context) throws SpreadsheetFormatException {
                return value instanceof BigDecimal;
            }

            @Override
            public Optional<SpreadsheetText> format(final Object value,
                                                    final SpreadsheetFormatterContext context) throws SpreadsheetFormatException {
                assertNotNull(value, "value");
                assertSame(SPREADSHEET_TEXT_FORMAT_CONTEXT, context, "Wrong SpreadsheetFormatterContext passed");

                return Optional.of(SpreadsheetText.with(color, value + " " + suffix));
            }

            @Override
            public String toString() {
                return pattern;
            }
        };
    }

    private SpreadsheetCell loadCellAndCheckFormatted2(final SpreadsheetEngine engine,
                                                       final SpreadsheetCellReference reference,
                                                       final SpreadsheetEngineEvaluation evaluation,
                                                       final SpreadsheetEngineContext context,
                                                       final Object value,
                                                       final String suffix) {
        final SpreadsheetCell cell = this.loadCellAndCheckValue(engine, reference, evaluation, context, value);
        this.checkFormattedText(cell, value + " " + suffix);
        return cell;
    }

    private SpreadsheetCell formattedCellWithValue(final String reference,
                                                   final String formulaText,
                                                   final Object value) {
        return this.formattedCellWithValue(SpreadsheetExpressionReference.parseCellReference(reference),
                formulaText,
                value);
    }

    private SpreadsheetCell formattedCellWithValue(final SpreadsheetCellReference reference,
                                                   final String formulaText,
                                                   final Object value) {
        return this.formattedCellWithValue(this.cell(reference, formulaText),
                value);
    }

    /**
     * Makes a {@link SpreadsheetCell} updating the formula expression and expected value and then formats the cell adding styling etc,
     * mimicking the very actions that happen during evaluation.
     */
    private SpreadsheetCell formattedCellWithValue(final SpreadsheetCell cell,
                                                   final Object value) {

        final SpreadsheetText formattedText = this.defaultSpreadsheetFormatter().format(value, SPREADSHEET_TEXT_FORMAT_CONTEXT)
                .orElseThrow(() -> new AssertionError("Failed to format " + CharSequences.quoteIfChars(value)));
        final Optional<TextNode> formattedCell = Optional.of(this.style()
                .replace(formattedText.toTextNode())
                .root());

        return cell.setFormula(this.parseFormula(cell.formula())
                .setValue(Optional.of(value)))
                .setFormatted(formattedCell);
    }

    private SpreadsheetCell formattedCellWithError(final String reference,
                                                   final String formulaText,
                                                   final String message) {
        return this.formattedCellWithError(SpreadsheetExpressionReference.parseCellReference(reference),
                formulaText,
                message);
    }

    private SpreadsheetCell formattedCellWithError(final SpreadsheetCellReference reference,
                                                   final String formulaText,
                                                   final String message) {
        return this.formattedCellWithError(this.cell(reference, formulaText),
                message);
    }

    /**
     * Makes a {@link SpreadsheetCell} updating the formula expression and setting the error and formatted cell and style.
     */
    private SpreadsheetCell formattedCellWithError(final SpreadsheetCell cell,
                                                   final String errorMessage) {
        final Optional<TextNode> formattedCell = Optional.of(this.style()
                .replace(TextNode.text(errorMessage))
                .root());

        return cell.setFormula(this.parseFormula(cell.formula())
                .setError(Optional.of(SpreadsheetError.with(errorMessage))))
                .setFormatted(formattedCell);
    }

    /**
     * Assumes the formula is syntactically correct and updates the cell.
     */
    private SpreadsheetFormula parseFormula(final SpreadsheetFormula formula) {
        final String text = formula.text();
        final ExpressionNumberKind expressionNumberKind = this.expressionNumberKind();

        final SpreadsheetParserToken token =
                text.isEmpty() ?
                        null :
                        SpreadsheetParsers.valueOrExpression(BasicSpreadsheetEngineTest.this.metadata().parser())
                                .parse(TextCursors.charSequence(text),
                                        SpreadsheetParserContexts.basic(
                                                this.dateTimeContext(),
                                                this.decimalNumberContext(),
                                                expressionNumberKind,
                                                VALUE_SEPARATOR
                                        )
                                ).orElseThrow(() -> new AssertionError("Failed to parseFormula " + CharSequences.quote(text)))
                                .cast(SpreadsheetParserToken.class);
        SpreadsheetFormula parsedFormula = formula;
        if (null == token) {
            parsedFormula = formula.setToken(BasicSpreadsheetEngine.EMPTY_TOKEN)
                    .setExpression(BasicSpreadsheetEngine.EMPTY_EXPRESSION);
        } else {
            parsedFormula = parsedFormula.setToken(Optional.of(token));

            try {
                parsedFormula = parsedFormula.setExpression(
                        token.toExpression(
                                new FakeExpressionEvaluationContext() {

                                    @Override
                                    public int defaultYear() {
                                        return DEFAULT_YEAR;
                                    }

                                    @Override
                                    public ExpressionNumberKind expressionNumberKind() {
                                        return expressionNumberKind;
                                    }

                                    @Override
                                    public int twoDigitYear() {
                                        return TWO_DIGIT_YEAR;
                                    }
                                }
                        )
                );
            } catch (final Exception fail) {
                parsedFormula = parsedFormula.setError(
                        Optional.of(SpreadsheetError.with(fail.getMessage()))
                );
            }
        }

        return parsedFormula;
    }

    private void loadCellStoreAndCheck(final SpreadsheetCellStore store,
                                       final SpreadsheetCell... cells) {
        assertEquals(Lists.of(cells),
                store.all(),
                () -> "all cells in " + store);
    }

    private void loadLabelStoreAndCheck(final SpreadsheetLabelStore store,
                                        final SpreadsheetLabelMapping... mappings) {
        assertEquals(Lists.of(mappings),
                store.all(),
                () -> "all mappings in " + store);
    }

    private <E extends SpreadsheetCellReferenceOrLabelName<E>> void loadReferencesAndCheck(final SpreadsheetExpressionReferenceStore<E> store,
                                                                                           final E cell,
                                                                                           final SpreadsheetCellReference... out) {
        assertEquals(Optional.ofNullable(out.length == 0 ? null : Sets.of(out)),
                store.load(cell),
                "references to " + cell);
    }

    private void loadReferrersAndCheck(final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> store,
                                       final SpreadsheetCellReference cell,
                                       final SpreadsheetCellReference... out) {
        assertEquals(Sets.of(out),
                store.loadReferred(cell),
                "referrers from " + cell);
    }

    private ExpressionNumber number(final Number number) {
        return this.expressionNumberKind().create(number);
    }

    private SpreadsheetId id() {
        return SpreadsheetId.with(123);
    }

    private SpreadsheetMetadata metadata() {
        final String suffix = " " + '"' + FORMATTED_PATTERN_SUFFIX + '"';

        return SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                .loadFromLocale()
                .set(SpreadsheetMetadataPropertyName.DATE_FORMAT_PATTERN, SpreadsheetParsePatterns.parseDateFormatPattern(DATE_PATTERN + suffix))
                .set(SpreadsheetMetadataPropertyName.DATE_PARSE_PATTERNS, SpreadsheetParsePatterns.parseDateParsePatterns(DATE_PATTERN + ";dd/mm"))
                .set(SpreadsheetMetadataPropertyName.DATETIME_FORMAT_PATTERN, SpreadsheetParsePatterns.parseDateTimeFormatPattern(DATETIME_PATTERN + suffix))
                .set(SpreadsheetMetadataPropertyName.DATETIME_PARSE_PATTERNS, SpreadsheetParsePatterns.parseDateTimeParsePatterns(DATETIME_PATTERN))
                .set(SpreadsheetMetadataPropertyName.NUMBER_FORMAT_PATTERN, SpreadsheetParsePatterns.parseNumberFormatPattern(NUMBER_PATTERN + suffix))
                .set(SpreadsheetMetadataPropertyName.NUMBER_PARSE_PATTERNS, SpreadsheetParsePatterns.parseNumberParsePatterns(NUMBER_PATTERN))
                .set(SpreadsheetMetadataPropertyName.TIME_FORMAT_PATTERN, SpreadsheetParsePatterns.parseTimeFormatPattern(TIME_PATTERN + suffix))
                .set(SpreadsheetMetadataPropertyName.TIME_PARSE_PATTERNS, SpreadsheetParsePatterns.parseTimeParsePatterns(TIME_PATTERN))
                .set(SpreadsheetMetadataPropertyName.STYLE, TextStyle.EMPTY
                        .set(TextStylePropertyName.WIDTH, Length.parsePixels(WIDTH + "px"))
                        .set(TextStylePropertyName.HEIGHT, Length.parsePixels(HEIGHT + "px"))
                );
    }

    private final static int WIDTH = 50;
    private final static int HEIGHT = 30;

    private SpreadsheetCellStore cellStore() {
        return SpreadsheetCellStores.treeMap();
    }

    private SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferencesStore() {
        return SpreadsheetExpressionReferenceStores.treeMap();
    }

    private SpreadsheetLabelStore labelStore() {
        return SpreadsheetLabelStores.treeMap();
    }

    private SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore() {
        return SpreadsheetExpressionReferenceStores.treeMap();
    }

    private SpreadsheetRangeStore<SpreadsheetCellReference> rangeToCellStore() {
        return SpreadsheetRangeStores.treeMap();
    }

    private SpreadsheetRangeStore<SpreadsheetConditionalFormattingRule> rangeToConditionalFormattingRuleStore() {
        return SpreadsheetRangeStores.treeMap();
    }

    private SpreadsheetColumnReference column(final int column) {
        return SpreadsheetReferenceKind.ABSOLUTE.column(column);
    }

    private SpreadsheetRowReference row(final int row) {
        return SpreadsheetReferenceKind.ABSOLUTE.row(row);
    }

    private SpreadsheetCellReference cellReference(final String reference) {
        return SpreadsheetExpressionReference.parseCellReference(reference);
    }

    private SpreadsheetCellReference cellReference(final int column, final int row) {
        return SpreadsheetReferenceKind.ABSOLUTE.column(column).setRow(SpreadsheetReferenceKind.ABSOLUTE.row(row));
    }

    private SpreadsheetCell cell(final String reference, final String formula) {
        return this.cell(SpreadsheetExpressionReference.parseCellReference(reference), formula);
    }

    private SpreadsheetCell cell(final SpreadsheetCellReference reference, final String formula) {
        return this.cell(reference, SpreadsheetFormula.with(formula));
    }

    private SpreadsheetCell cell(final SpreadsheetCellReference reference, final SpreadsheetFormula formula) {
        return SpreadsheetCell.with(reference, formula)
                .setStyle(this.style());
    }

    private TextStyle style() {
        return TextStyle.with(Maps.of(TextStylePropertyName.FONT_WEIGHT, FontWeight.BOLD));
    }

    @Override
    public Class<BasicSpreadsheetEngine> type() {
        return BasicSpreadsheetEngine.class;
    }

    // TypeNameTesting..........................................................................................

    @Override
    public String typeNameSuffix() {
        return "";
    }
}
