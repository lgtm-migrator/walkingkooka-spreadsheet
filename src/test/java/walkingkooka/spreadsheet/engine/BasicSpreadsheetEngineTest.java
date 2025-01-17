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
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.Converters;
import walkingkooka.datetime.DateTimeContexts;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetDescription;
import walkingkooka.spreadsheet.SpreadsheetError;
import walkingkooka.spreadsheet.SpreadsheetErrorKind;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.SpreadsheetViewport;
import walkingkooka.spreadsheet.conditionalformat.SpreadsheetConditionalFormattingRule;
import walkingkooka.spreadsheet.convert.SpreadsheetConverters;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContexts;
import walkingkooka.spreadsheet.format.FakeSpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetText;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetFormatPattern;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePattern;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.parser.SpreadsheetParserContexts;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.parser.SpreadsheetParsers;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelectionAnchor;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelectionNavigation;
import walkingkooka.spreadsheet.reference.store.SpreadsheetCellRangeStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.store.FakeSpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStore;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStore;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.CharSequences;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursors;
import walkingkooka.text.cursor.parser.ParserReporters;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionEvaluationContext;
import walkingkooka.tree.expression.ExpressionEvaluationContexts;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.ExpressionPurityContext;
import walkingkooka.tree.expression.ExpressionReference;
import walkingkooka.tree.expression.FakeExpressionEvaluationContext;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;
import walkingkooka.tree.expression.function.ExpressionFunctionParameterKind;
import walkingkooka.tree.expression.function.ExpressionFunctionParameterName;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("PointlessArithmeticExpression")
public final class BasicSpreadsheetEngineTest extends BasicSpreadsheetEngineTestCase<BasicSpreadsheetEngine>
        implements SpreadsheetEngineTesting<BasicSpreadsheetEngine> {

    private final static String FORMATTED_PATTERN_SUFFIX = "FORMATTED_PATTERN_SUFFIX";

    private final static String DATE_PATTERN = "yyyy/mm/dd";
    private final static String TIME_PATTERN = "hh:mm";
    private final static String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;
    private final static String NUMBER_PATTERN = "#";
    private final static String TEXT_PATTERN = "@";

    private final static ExpressionNumberKind EXPRESSION_NUMBER_KIND = ExpressionNumberKind.DEFAULT;

    private final static Function<SpreadsheetSelection, SpreadsheetSelection> RESOLVE_IF_LABEL = (s) -> {
        throw new UnsupportedOperationException();
    };

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

        private final Converter<SpreadsheetFormatterContext> converter = Converters.collection(
                Lists.of(
                        ExpressionNumber.fromConverter(
                                Converters.collection(
                                        Lists.of(
                                                Converters.simple(),
                                                SpreadsheetConverters.errorToString()
                                                        .cast(SpreadsheetFormatterContext.class),
                                                SpreadsheetConverters.errorToNumber()
                                                        .cast(SpreadsheetFormatterContext.class),
                                                SpreadsheetConverters.errorThrowing()
                                                        .cast(SpreadsheetFormatterContext.class),
                                                Converters.localDateLocalDateTime(),
                                                Converters.localTimeLocalDateTime(),
                                                Converters.numberNumber(),
                                                Converters.objectString()
                                        )
                                )
                        )
                )
        );

        @Override
        public char decimalSeparator() {
            return '.';
        }

        @Override
        public ExpressionNumberKind expressionNumberKind() {
            return EXPRESSION_NUMBER_KIND;
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
    private final static SpreadsheetCellReference LABEL_CELL = SpreadsheetSelection.parseCell("Z99");

    private final static double COLUMN_WIDTH = 50;

    private final static Map<SpreadsheetColumnReference, Double> COLUMN_A_WIDTH = columnWidths("A");

    private static Map<SpreadsheetColumnReference, Double> columnWidths(final String columns) {
        final Map<SpreadsheetColumnReference, Double> map = Maps.sorted();

        Arrays.stream(columns.split(","))
                .forEach(c ->
                        map.put(
                                SpreadsheetSelection.parseColumn(c),
                                COLUMN_WIDTH
                        )
                );

        return map;
    }

    private final static double ROW_HEIGHT = 30;

    private final static Map<SpreadsheetRowReference, Double> ROW_1_HEIGHT = rowHeights("1");

    private static Map<SpreadsheetRowReference, Double> rowHeights(final String rows) {
        final Map<SpreadsheetRowReference, Double> map = Maps.sorted();

        Arrays.stream(rows.split(","))
                .forEach(r ->
                        map.put(
                                SpreadsheetSelection.parseRow(r),
                                ROW_HEIGHT
                        )
                );

        return map;
    }

    private final static Supplier<LocalDateTime> NOW = LocalDateTime::now;

    // loadCells........................................................................................................

    @Test
    public void testLoadCellsCellWhenEmpty() {
        this.loadCellFailCheck(cellReference(1, 1), SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY);
    }

    @Test
    public void testLoadCellsWithFormulaWithInvalidValueFails() {
        this.loadCellFails(
                "1.X",
                SpreadsheetErrorKind.ERROR.setMessage("Invalid character '1' at (1,1) \"1.X\" expected APOSTROPHE_STRING | EQUALS_EXPRESSION | VALUE")
        );
    }

    @Test
    public void testLoadCellsWithFormulaExpressionErrorFails() {
        this.loadCellFails(
                "=1+",
                SpreadsheetErrorKind.ERROR.setMessage(
                        "End of text at (4,1) \"=1+\" expected BINARY_SUB_EXPRESSION"
                )
        );
    }

    @Test
    public void testLoadCellsWithFormulaWithInvalidLabelFails() {
        this.loadCellFails(
                "=UnknownLabel",
                SpreadsheetError.selectionNotFound(SpreadsheetSelection.labelName("UnknownLabel"))
        );
    }

    @Test
    public void testLoadCellsWithDivideByZeroFails() {
        this.loadCellFails(
                "=1/0",
                SpreadsheetErrorKind.DIV0.setMessage("Division by zero")
        );
    }

    @Test
    public void testLoadCellsWithUnknownFunctionFails() {
        this.loadCellFails(
                "=unknownFunction()",
                SpreadsheetError.functionNotFound(
                        FunctionExpressionName.with("unknownFunction")
                )
        );
    }

    private void loadCellFails(final String formulaText,
                               final SpreadsheetError error) {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");

        context.storeRepository()
                .cells()
                .save(a1.setFormula(SpreadsheetFormula.EMPTY.setText(formulaText)));

        final String errorMessage = error.message();

        this.loadCellAndCheck(
                engine,
                a1,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                error,
                error.kind().text() + " " + FORMATTED_PATTERN_SUFFIX, // formatted text
                errorMessage
        );
    }

    @Test
    public void testLoadCellsWithCellFormulaEqMissingCellReference() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");

        context.storeRepository()
                .cells()
                .save(a1.setFormula(SpreadsheetFormula.EMPTY.setText("=Z99")));

        this.loadCellAndCheckFormatted(
                engine,
                a1,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                context.metadata()
                        .expressionNumberKind()
                        .zero(),
                " " + FORMATTED_PATTERN_SUFFIX
        );
    }

    @Test
    public void testLoadCellsWithCellFormulaWithMissingCellReference() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");

        context.storeRepository()
                .cells()
                .save(a1.setFormula(SpreadsheetFormula.EMPTY.setText("=2+Z99")));

        this.loadCellAndCheckFormatted(
                engine,
                a1,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                context.metadata()
                        .expressionNumberKind()
                        .create(2),
                "2 " + FORMATTED_PATTERN_SUFFIX
        );
    }

    @Test
    public void testLoadCellsWithCellFormulaEqUnknownLabel() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);


        final SpreadsheetLabelMapping labelMapping = LABEL.mapping(LABEL_CELL);
        context.storeRepository()
                .labels()
                .save(labelMapping);

        this.checkEquals(
                SpreadsheetDelta.EMPTY
                        .setCells(SpreadsheetDelta.NO_CELLS)
                        .setDeletedCells(
                                Sets.of(LABEL_CELL)
                        ).setLabels(
                                Sets.of(LABEL.mapping(LABEL_CELL))
                        ),
                engine.loadCells(
                        LABEL_CELL,
                        SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                        SpreadsheetDeltaProperties.ALL,
                        context
                )
        );
    }

    @Test
    public void testLoadCellsWithCellFormulaWithFunctionMissingCellReferenceNumberParameter() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");

        context.storeRepository()
                .cells()
                .save(
                        a1.setFormula(
                                SpreadsheetFormula.EMPTY
                                        .setText("=BasicSpreadsheetEngineTestNumberParameter(A2)")
                        )
                );

        this.loadCellAndCheckFormatted(
                engine,
                a1,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                EXPRESSION_NUMBER_KIND.zero(),
                " " + FORMATTED_PATTERN_SUFFIX
        );
    }

    @Test
    public void testLoadCellsWithCellFormulaWithFunctionMissingCellReferenceStringParameter() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");

        context.storeRepository()
                .cells()
                .save(
                        a1.setFormula(
                        SpreadsheetFormula.EMPTY
                                .setText("=BasicSpreadsheetEngineTestStringParameter(A2)")
                        )
                );

        this.loadCellAndCheckFormatted(
                engine,
                a1,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                SpreadsheetError.selectionNotFound(
                        SpreadsheetSelection.parseCell("A2")
                ).setNameString(),
                "#NAME? " + FORMATTED_PATTERN_SUFFIX
        );
    }

    @Test
    public void testLoadCellsWithLabelToMissingCell() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        this.checkEquals(
                Optional.empty(),
                context.storeRepository()
                        .cells()
                        .load(LABEL_CELL)
        );

        final SpreadsheetLabelMapping labelMapping = context.storeRepository()
                .labels()
                .save(LABEL.mapping(LABEL_CELL));

        this.checkEquals(
                SpreadsheetDelta.EMPTY
                        .setCells(SpreadsheetDelta.NO_CELLS)
                        .setDeletedCells(
                                Sets.of(LABEL_CELL)
                        )
                        .setLabels(
                                Sets.of(labelMapping)
                        ),
                engine.loadCells(
                        LABEL,
                        SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                        SpreadsheetDeltaProperties.ALL,
                        context
                )
        );
    }

    @Test
    public void testLoadCellsSkipEvaluate() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(this.cell(cellReference, "=1+2"));

        this.loadCellAndCheckWithoutValueOrError(
                engine,
                cellReference,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                SpreadsheetDeltaProperties.ALL,
                context
        );
    }

    @Test
    public void testLoadCellsWithoutFormatPattern() {
        this.cellStoreSaveAndLoadCellAndCheck(
                SpreadsheetCell.NO_FORMAT_PATTERN,
                FORMATTED_PATTERN_SUFFIX
        );
    }

    @Test
    public void testLoadCellsWithFormatPattern() {
        this.cellStoreSaveAndLoadCellAndCheck(
                Optional.of(
                        SpreadsheetPattern.parseNumberFormatPattern("# \"" + FORMATTED_PATTERN_SUFFIX + "\"")
                ),
                FORMATTED_PATTERN_SUFFIX
        );
    }

    private void cellStoreSaveAndLoadCellAndCheck(final Optional<SpreadsheetFormatPattern> formatPattern,
                                                  final String patternSuffix) {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(this.cell(cellReference, "=1+2")
                        .setFormatPattern(formatPattern));

        this.loadCellAndCheckFormatted2(engine,
                cellReference,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(1 + 2),
                patternSuffix);
    }

    @Test
    public void testLoadCellsComputeIfNecessaryCachesCell() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(this.cell(cellReference, "=1+2"));

        final SpreadsheetCell first = this.loadCellOrFail(
                engine,
                cellReference,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context
        );

        final SpreadsheetCell second = this.loadCellOrFail(
                engine,
                cellReference,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context
        );

        assertSame(first, second, "different instances of SpreadsheetCell returned not cached");
    }

    @Test
    public void testLoadCellsParsePatternFails() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(
                        this.cell(
                                cellReference,
                                "=1+2"
                        ).setParsePattern(
                                Optional.of(
                                        SpreadsheetPattern.parseNumberParsePattern("#")
                                )
                        )
                );

        this.loadCellAndCheckError(
                engine,
                cellReference,
                SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                context,
                "Invalid character '=' at (1,1) \"=1+2\" expected \"#\""
        );
    }

    @Test
    public void testLoadCellsParsePattern() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(
                        this.cell(
                                cellReference,
                                "123"
                        ).setParsePattern(
                                Optional.of(
                                        SpreadsheetPattern.parseNumberParsePattern("$#;#")
                                )
                        )
                );

        this.loadCellAndCheckValue(
                engine,
                cellReference,
                SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                context,
                number(123)
        );
    }

    @Test
    public void testLoadCellsComputeIfNecessaryKeepsExpression() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(this.cell(a, "1/2"));

        final SpreadsheetCell first = this.loadCellOrFail(engine, a, SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, context);
        this.checkValue(first, LocalDate.of(DEFAULT_YEAR, 2, 1));

        final int defaultYear = DEFAULT_YEAR + 100;

        final SpreadsheetCell second = this.loadCellOrFail(
                engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                this.createContext(defaultYear, engine, context.storeRepository()));

        assertSame(first, second, "same instances of SpreadsheetCell returned should have new expression and value");

        this.checkValue(
                second,
                LocalDate.of(1900, 2, 1)
        );
        this.checkFormattedText(
                second,
                "1900/02/01 FORMATTED_PATTERN_SUFFIX"
        );
    }

    @Test
    public void testLoadCellsComputeIfNecessaryHonoursExpressionIsPure() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(this.cell(a, "1/2"));

        final SpreadsheetCell first = this.loadCellOrFail(
                engine,
                a,
                SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                context
        );
        this.checkValue(
                first,
                LocalDate.of(DEFAULT_YEAR, 2, 1)
        );

        final int defaultYear = DEFAULT_YEAR + 100;

        final SpreadsheetCell second = this.loadCellOrFail(
                engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                this.createContext(defaultYear, engine, context.storeRepository())
        );

        assertSame(first, second, "same instances of SpreadsheetCell returned should have new expression and value");

        this.checkValue(
                second,
                LocalDate.of(1900, 2, 1)
        );
        this.checkFormattedText(
                second,
                "1900/02/01 FORMATTED_PATTERN_SUFFIX"
        );
    }

    @Test
    public void testLoadCellsComputeIfNecessaryHonoursFunctionIsPure() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(
                        this.cell(
                                a,
                                "=BasicSpreadsheetEngineTestValue()"
                        )
                );

        final Object value = EXPRESSION_NUMBER_KIND.one();
        this.value = value;

        final SpreadsheetCell first = this.loadCellOrFail(
                engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context
        );
        this.checkValue(
                first,
                this.value
        );
        this.checkFormattedText(
                first,
                "1 FORMATTED_PATTERN_SUFFIX"
        );

        final Object value2 = EXPRESSION_NUMBER_KIND.create(2);
        this.value = value2;

        final SpreadsheetCell second = this.loadCellOrFail(
                engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context
        );
        this.checkValue(
                second,
                value2
        );
        this.checkFormattedText(
                second,
                "2 FORMATTED_PATTERN_SUFFIX"
        );
    }

    @Test
    public void testLoadCellsComputeIfNecessaryCachesCellWithInvalidFormulaAndErrorCached() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(this.cell(cellReference, "=1+2+"));

        final SpreadsheetCell first = this.loadCellOrFail(engine,
                cellReference,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context);
        this.checkNotEquals(
                SpreadsheetFormula.NO_VALUE,
                first.formula()
                        .error(),
                () -> "Expected error absent=" + first
        );

        final SpreadsheetCell second = this.loadCellOrFail(engine, cellReference, SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, context);
        assertSame(first, second, "different instances of SpreadsheetCell returned not cached");

        this.checkFormattedText(
                second,
                "#ERROR " + FORMATTED_PATTERN_SUFFIX
        );
    }

    @Test
    public void testLoadCellsForceRecomputeIgnoresValue() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(this.cell(a, "1/2"));

        final SpreadsheetCell first = this.loadCellOrFail(engine, a, SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, context);
        this.checkValue(first, LocalDate.of(DEFAULT_YEAR, 2, 1));

        final int defaultYear = DEFAULT_YEAR + 100;

        final SpreadsheetCell second = this.loadCellOrFail(
                engine,
                a,
                SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                this.createContext(defaultYear, engine, context.storeRepository()));

        assertNotSame(first, second, "same instances of SpreadsheetCell returned should have new expression and value");
        this.checkValue(
                second,
                LocalDate.of(defaultYear, 2, 1)
        );
        this.checkFormattedText(
                second,
                "2000/02/01 FORMATTED_PATTERN_SUFFIX"
        );
    }

    @Test
    public void testLoadCellsForceRecomputeIgnoresCache() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        cellStore.save(this.cell(a, "=1"));

        final SpreadsheetCellReference b = this.cellReference(2, 2);
        cellStore.save(this.cell(b, "=" + a));

        final SpreadsheetCell first = this.loadCellOrFail(engine, a, SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, context);

        cellStore.save(this.cell(a, "=999"));

        final SpreadsheetCell second = this.loadCellOrFail(engine, a, SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, context);
        assertNotSame(first, second, "different instances of SpreadsheetCell returned not cached");
        this.checkEquals(Optional.of(number(999)),
                second.formula().value(),
                "first should have value updated to 999 and not 1 the original value.");
    }

    @Test
    public void testLoadCellsForceRecomputeIgnoresPreviousError() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("A1");
        final SpreadsheetCell unsaved = this.cell(a, "=1+$B$2");
        final Set<SpreadsheetCell> saved = engine.saveCell(
                        unsaved,
                        context)
                .cells();

        this.checkEquals(
                this.formattedCell(
                        unsaved,
                        EXPRESSION_NUMBER_KIND.one()
                ),
                saved.iterator().next()
        );

        final SpreadsheetCellReference b = this.cellReference("B2");
        context.storeRepository()
                .cells()
                .save(this.cell(b, "=99"));

        this.loadCellAndCheckValue(engine,
                a,
                SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                context,
                number(1 + 99));
    }

    @Test
    public void testLoadCellsComputeThenSkipEvaluate() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);
        context.storeRepository()
                .cells()
                .save(this.cell(cellReference, "=1+2"));

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
    public void testLoadCellsManyWithoutCrossCellReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 1);
        final SpreadsheetCellReference c = this.cellReference(3, 1);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();
        cellStore.save(this.cell(a, "=1+2"));
        cellStore.save(this.cell(b, "=3+4"));
        cellStore.save(this.cell(c, "=5+6"));

        this.loadCellAndCheckFormatted2(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(1 + 2),
                FORMATTED_PATTERN_SUFFIX);
        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_PATTERN_SUFFIX);
        this.loadCellAndCheckFormatted2(engine,
                c,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(5 + 6),
                FORMATTED_PATTERN_SUFFIX);
    }

    @Test
    public void testLoadCellsWithCrossCellReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 1);
        final SpreadsheetCellReference c = this.cellReference(3, 1);

        this.value = BigDecimal.ZERO;

        engine.saveCell(this.cell(a, "=1+2+BasicSpreadsheetEngineTestValue()"), context);
        engine.saveCell(this.cell(b, "=3+4+" + a), context);
        engine.saveCell(this.cell(c, "=5+6+" + a), context);

        // updating this counter results in $A having its value recomputed forcing a cascade update of $b and $c
        this.value = number(100);

        this.loadCellAndCheck(
                engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        formattedCell(a, "=1+2+BasicSpreadsheetEngineTestValue()", number(100 + 3)),
                                        formattedCell(b, "=3+4+" + a, number(3 + 4 + 103)),
                                        formattedCell(c, "=5+6+" + a, number(5 + 6 + 103))
                                )
                        ).setColumnWidths(
                                columnWidths("B,C,D")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );
    }

    @Test
    public void testLoadCellsValueLabelInvalidFails() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        context.storeRepository()
                .cells()
                .save(this.cell(a, "=INVALIDLABEL"));

        this.loadCellAndCheckError(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                "Label not found");
    }

    @Test
    public void testLoadCellsValueIsCellReference() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // B1

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();
        cellStore.save(this.cell(a, "=B1"));
        cellStore.save(this.cell(b, "=3+4"));

        // formula
        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_PATTERN_SUFFIX);

        // reference to B1 which has formula
        this.loadCellAndCheckFormatted2(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_PATTERN_SUFFIX);
    }

    @Test
    public void testLoadCellsValueIsLabel() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // B1

        final SpreadsheetStoreRepository repository = context.storeRepository();

        final SpreadsheetCellStore cellStore = repository.cells();
        cellStore.save(this.cell(a, "=" + LABEL.value()));
        cellStore.save(this.cell(b, "=3+4"));

        repository.labels()
                .save(SpreadsheetLabelMapping.with(LABEL, b));

        // formula
        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_PATTERN_SUFFIX);

        // reference to B1 which has formula
        this.loadCellAndCheckFormatted2(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_PATTERN_SUFFIX);
    }

    @Test
    public void testLoadCellsWithConditionalFormattingRule() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();

        final SpreadsheetCellRangeStore<SpreadsheetConditionalFormattingRule> rules = repository.rangeToConditionalFormattingRules();
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

        repository.cells()
                .save(this.cell(a, "=3+4"));

        final SpreadsheetCell cell = this.loadCellAndCheckFormatted2(engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                context,
                number(3 + 4),
                FORMATTED_PATTERN_SUFFIX);

        // UNDERLINED from conditional formatting rule #2.
        this.checkEquals(Optional.of(italics.replace(TextNode.text("7 " + FORMATTED_PATTERN_SUFFIX)).root()),
                cell.formatted(),
                () -> "TextStyle should include underline if correct rule was applied=" + cell);
    }

    @Test
    public void testLoadCellSpreadsheetDeltaPropertiesCells() {
        this.loadCellAndCheck(
                SpreadsheetDeltaProperties.CELLS
        );
    }

    @Test
    public void testLoadCellSpreadsheetDeltaPropertiesCellsLabel() {
        this.loadCellAndCheck(
                SpreadsheetDeltaProperties.CELLS,
                SpreadsheetDeltaProperties.LABELS
        );
    }

    @Test
    public void testLoadCellSpreadsheetDeltaPropertiesCellsColumnWidths() {
        this.loadCellAndCheck(
                SpreadsheetDeltaProperties.CELLS,
                SpreadsheetDeltaProperties.COLUMN_WIDTHS
        );
    }

    @Test
    public void testLoadCellSpreadsheetDeltaPropertiesCellsRowHeights() {
        this.loadCellAndCheck(
                SpreadsheetDeltaProperties.CELLS,
                SpreadsheetDeltaProperties.ROW_HEIGHTS
        );
    }

    @Test
    public void testLoadCellSpreadsheetDeltaPropertiesAll() {
        this.loadCellAndCheck(SpreadsheetDeltaProperties.ALL);
    }

    private void loadCellAndCheck(final SpreadsheetDeltaProperties... deltaProperties) {
        this.loadCellAndCheck(
                Sets.of(deltaProperties)
        );
    }

    private void loadCellAndCheck(final Set<SpreadsheetDeltaProperties> deltaProperties) {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = SpreadsheetSelection.parseCell("A1");

        final SpreadsheetCell cell = this.cell(cellReference, "=1+2");

        context.storeRepository()
                .cells()
                .save(cell);

        final SpreadsheetLabelMapping label = LABEL.mapping(cellReference);

        context.storeRepository()
                .labels()
                .save(label);

        final SpreadsheetDelta delta = engine.loadCells(
                cellReference,
                SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                deltaProperties,
                context
        );

        if (deltaProperties.contains(SpreadsheetDeltaProperties.CELLS)) {
            this.checkNotEquals(
                    Sets.empty(),
                    delta.cells(),
                    () -> "cells should have cell, " + deltaProperties
            );
        } else {
            this.checkEquals(
                    Sets.empty(),
                    delta.cells(),
                    () -> "cells should be empty, " + deltaProperties
            );
        }

        this.checkEquals(
                deltaProperties.contains(SpreadsheetDeltaProperties.LABELS) ? Sets.of(label) : Sets.empty(),
                delta.labels(),
                () -> "labels, " + deltaProperties
        );

        this.checkEquals(
                deltaProperties.contains(SpreadsheetDeltaProperties.COLUMN_WIDTHS) ?
                        Maps.of(cellReference.column(), COLUMN_WIDTH) :
                        SpreadsheetDelta.NO_COLUMN_WIDTHS,
                delta.columnWidths(),
                () -> "columnWidths, " + deltaProperties
        );

        this.checkEquals(
                deltaProperties.contains(SpreadsheetDeltaProperties.ROW_HEIGHTS) ?
                        Maps.of(cellReference.row(), ROW_HEIGHT) :
                        SpreadsheetDelta.NO_ROW_HEIGHTS,
                delta.rowHeights(),
                () -> "rowHeights, " + deltaProperties
        );
    }

    private void saveRule(final boolean result,
                          final int priority,
                          final TextStyle style,
                          final SpreadsheetCellReference cell,
                          final SpreadsheetCellRangeStore<SpreadsheetConditionalFormattingRule> rules) {
        rules.addValue(cell.cellRange(cell), rule(result, priority, style));
    }

    private SpreadsheetConditionalFormattingRule rule(final boolean result,
                                                      final int priority,
                                                      final TextStyle style) {


        return SpreadsheetConditionalFormattingRule.with(SpreadsheetDescription.with(priority + "=" + result),
                priority,
                SpreadsheetFormula.EMPTY
                        .setText(
                                String.valueOf(result)
                        ).setExpression(
                                Optional.of(
                                        Expression.value(result)
                                )
                        ),
                (c) -> style);
    }

    @Test
    public void testLoadCellsWithCellRange() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCell b2 = cellStore.save(
                this.cell(
                        SpreadsheetSelection.parseCell("B2"),
                        "=22"
                )
        );

        final SpreadsheetCell c3 = cellStore.save(
                this.cell(
                        SpreadsheetSelection.parseCell("c3"),
                        "=33"
                )
        );

        this.checkEquals(
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(b2, EXPRESSION_NUMBER_KIND.create(22)),
                                        this.formattedCell(c3, EXPRESSION_NUMBER_KIND.create(33))
                                )
                        ).setDeletedCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("B3"),
                                        SpreadsheetSelection.parseCell("C2")
                                )
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("2,3")
                        ),
                engine.loadCells(
                        SpreadsheetSelection.parseCellRange("B2:C3"),
                        SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                        SpreadsheetDeltaProperties.ALL,
                        context
                )
        );
    }

    // saveCell....................................................................................................

    @Test
    public void testSaveCellEmptyFormula() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", "");
        final SpreadsheetCell a1Formatted = this.formattedCell(a1, "");
        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(a1Formatted)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);
    }

    @Test
    public void testSaveCellEmptyFormulaTwice() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", "");
        final SpreadsheetCell a1Formatted = this.formattedCell(a1, "");
        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(a1Formatted)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);

        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(a1Formatted)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );
    }

    @Test
    public void testSaveCellInvalidDate() {
        this.saveCellWithErrorAndCheck(
                "1999/99/31",
                SpreadsheetErrorKind.VALUE.setMessage(
                        "Invalid value for MonthOfYear (valid values 1 - 12): 99"
                )
        );
    }

    @Test
    public void testSaveCellInvalidDateTime() {
        this.saveCellWithErrorAndCheck(
                "1999/99/31 12:58",
                SpreadsheetErrorKind.VALUE.setMessage("Invalid value for MonthOfYear (valid values 1 - 12): 99")
        );
    }

    @Test
    public void testSaveCellInvalidTime() {
        this.saveCellWithErrorAndCheck(
                "12:99",
                SpreadsheetErrorKind.VALUE.setMessage("Invalid value for MinuteOfHour (valid values 0 - 59): 99")
        );
    }

    private void saveCellWithErrorAndCheck(final String formula,
                                           final SpreadsheetError error) {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", formula);
        final SpreadsheetCell a1Formatted = this.formattedCell(
                a1,
                error
        );

        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        a1Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);
    }

    @Test
    public void testSaveCellWithoutCellReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", "=1+2");
        final SpreadsheetCell a1Formatted = this.formattedCell(a1, number(3));
        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        a1Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);
    }

    @Test
    public void testSaveCellWithUnknownCellReference() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        final SpreadsheetCell a1 = this.cell("a1", "=$B$2+99");
        final SpreadsheetCell a1Formatted = this.formattedCell(
                a1,
                this.number(99)
        );

        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        a1Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);

        // verify references all ways are present in the store.
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("$B$2");

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference(), b2.toRelative()); // references from A1 -> B2
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference()); // references to A1 -> none

        this.loadReferencesAndCheck(cellReferenceStore, b2); // references to B2 -> none
        this.loadReferrersAndCheck(cellReferenceStore, b2, a1.reference()); // references from B2 -> A1
    }

    @Test
    public void testSaveCellIgnoresPreviousErrorComputesValue() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);

        final SpreadsheetCell cell = this.cell(
                cellReference,
                SpreadsheetFormula.EMPTY
                        .setText("=1+2")
                        .setValue(
                                Optional.of(
                                        SpreadsheetErrorKind.VALUE.setMessage("error!")
                                )
                        )
        );

        this.saveCellAndCheck(
                engine,
                cell,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(cell, number(1 + 2))
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );
    }

    @Test
    public void testSaveCellSecondTimeWithDifferentStyle() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference cellReference = this.cellReference(1, 1);

        final SpreadsheetCell cell = cellReference.setFormula(
                SpreadsheetFormula.EMPTY
                        .setText("=1+2")
        );

        final SpreadsheetCell cellWithValue = this.formattedCell(
                cell,
                number(1 + 2),
                TextStyle.EMPTY
        );

        this.saveCellAndCheck(
                engine,
                cell,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        cellWithValue
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );

        final TextStyle newStyle = TextStyle.EMPTY
                .set(
                        TextStylePropertyName.COLOR,
                        Color.parse("#123456")
                );

        this.saveCellAndCheck(
                engine,
                cellWithValue.setStyle(newStyle),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                cell.setStyle(newStyle),
                                                number(1 + 2),
                                                newStyle
                                        )
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );
    }

    @Test
    public void testSaveCellMultipleIndependentUnreferenced() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+2");
        final SpreadsheetCell a1Formatted = this.formattedCell(a1, number(3));

        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(a1Formatted)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        final SpreadsheetCell b2 = this.cell("$B$2", "=3+4");
        final SpreadsheetCell b2Formatted = this.formattedCell(b2, number(7));

        this.saveCellAndCheck(
                engine,
                b2,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        b2Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
                );

        final SpreadsheetCell c3 = this.cell("$C$3", "=5+6");
        final SpreadsheetCell c3Formatted = this.formattedCell(c3, number(11));

        this.saveCellAndCheck(
                engine,
                c3,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(c3Formatted)
                        ).setColumnWidths(
                                columnWidths("C")
                        ).setRowHeights(
                                rowHeights("3")
                        )
                );

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName unknown = SpreadsheetSelection.labelName("LABELXYZ");

        final SpreadsheetCell a1 = this.cell("a1", "=1+" + unknown);
        final SpreadsheetCell a1Formatted = this.formattedCell(
                a1,
                SpreadsheetError.selectionNotFound(unknown)
        );
        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        a1Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = repository.labelReferences();

        this.loadCellStoreAndCheck(cellStore, a1Formatted);
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);

        this.loadReferencesAndCheck(labelReferencesStore, unknown, a1.reference());
    }

    @Test
    public void testSaveCellTwiceLaterCellReferencesPrevious() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+2");
        engine.saveCell(a1, context);

        final SpreadsheetCellReference a1Reference = SpreadsheetSelection.parseCell("$A$1");
        final SpreadsheetCell b2 = this.cell("$B$2", "=5+" + a1Reference);

        this.saveCellAndCheck(
                engine,
                b2,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(b2, number(5 + 3))
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );

        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = context.storeRepository()
                .cellReferences();

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference(), b2.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference(), a1Reference.toRelative());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference());
    }

    @Test
    public void testSaveCellTwiceLaterCellReferencesPrevious2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+C3");
        engine.saveCell(a1, context);

        final SpreadsheetCell b2 = this.cell("$B$2", "=5+A1");
        engine.saveCell(b2, context);

        final SpreadsheetCell c3 = this.cell("$C$3", "=10");

        this.saveCellAndCheck(
                engine,
                c3,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a1, number(1 + 10)),
                                        this.formattedCell(b2, number(5 + 1 + 10)),
                                        this.formattedCell(c3, number(10))
                                )
                        ).setColumnWidths(
                                columnWidths("A,B,C")
                        ).setRowHeights(
                                rowHeights("1,2,3")
                        )
                );
    }

    @Test
    public void testSaveCellTwiceLaterReferencesPreviousAgain() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+2");
        engine.saveCell(a1, context);

        final SpreadsheetCell b2 = this.cell("$B$2", "=5+$A$1");
        final SpreadsheetCell b2Formatted = this.formattedCell(b2, number(5 + 1 + 2));

        this.saveCellAndCheck(
                engine,
                b2,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        b2Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );

        this.saveCellAndCheck(
                engine,
                b2Formatted,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        b2Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );
    }

    @Test
    public void testSaveCellReferencesUpdated() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference b2Reference = SpreadsheetSelection.parseCell("$B$2");
        final SpreadsheetCell a1 = this.cell("$A$1", "=" + b2Reference + "+5");
        engine.saveCell(a1, context);

        final SpreadsheetCell b2 = this.cell("$B$2", "=1+2");
        this.saveCellAndCheck(
                engine,
                b2,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a1, number(1 + 2 + 5)),
                                        this.formattedCell(b2, number(1 + 2))
                                )
                        ).setColumnWidths(
                                columnWidths("A,B")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        );

        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = context.storeRepository()
                .cellReferences();

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference(), b2Reference.toRelative());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference(), a1.reference());
    }

    @Test
    public void testSaveCellLabelReference() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        context.storeRepository()
                .labels()
                .save(SpreadsheetLabelMapping.with(SpreadsheetSelection.labelName("LABELA1"), this.cellReference("A1")));

        final SpreadsheetCell a1 = this.cell("$A$1", "=10");
        engine.saveCell(a1, context);

        final SpreadsheetCell b2 = this.cell("$B$2", "=5+LABELA1");
        this.saveCellAndCheck(
                engine,
                b2,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(b2, number(5 + 10))
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
                );
    }

    @Test
    public void testSaveCellLabelReference2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = repository.labelReferences();

        final SpreadsheetCell b2 = this.cell("$B$2", "=5");

        final SpreadsheetLabelName labelB2 = SpreadsheetSelection.labelName("LABELB2");
        labelStore.save(SpreadsheetLabelMapping.with(labelB2, b2.reference()));

        final SpreadsheetCell a1 = this.cell("$A$1", "=10+" + labelB2);
        engine.saveCell(a1, context);

        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference());

        this.loadReferencesAndCheck(labelReferencesStore, labelB2, a1.reference());

        this.saveCellAndCheck(engine,
                b2,
                context,
                SpreadsheetDelta.EMPTY.setCells(
                        Sets.of(
                                this.formattedCell(a1, number(10 + 5)),
                                this.formattedCell(b2, number(5))
                        )
                ).setLabels(
                        Sets.of(
                                labelB2.mapping(b2.reference())
                        )
                ).setColumnWidths(
                        columnWidths("A,B")
                ).setRowHeights(
                        rowHeights("1,2")
                )
        );
    }

    @Test
    public void testSaveCellReplacesCellReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell d4 = this.cell("$D$4", "=20");
        engine.saveCell(d4, context);

        final SpreadsheetCell e5 = this.cell("$E$5", "=30");
        engine.saveCell(e5, context);

        engine.saveCell(this.cell("$A$1", "=10+" + d4.reference()), context);

        final SpreadsheetCell a1 = this.cell("$A$1", "=40+" + e5.reference());
        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a1, number(40 + 30))
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
                );

        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = context
                .storeRepository()
                .cellReferences();

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference(), e5.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, d4.reference());
        this.loadReferrersAndCheck(cellReferenceStore, d4.reference());
    }

    @Test
    public void testSaveCellReplacesLabelReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = repository.labelReferences();

        final SpreadsheetLabelName labelB2 = SpreadsheetSelection.labelName("LABELB2");
        labelStore.save(SpreadsheetLabelMapping.with(labelB2, this.cellReference("B2")));

        final SpreadsheetLabelName labelD4 = SpreadsheetSelection.labelName("LABELD4");
        labelStore.save(SpreadsheetLabelMapping.with(labelD4, this.cellReference("D4")));

        final SpreadsheetCell d4 = this.cell("$D$4", "=20");
        engine.saveCell(d4, context);

        engine.saveCell(this.cell("$A$1", "=10+" + labelB2), context);

        final SpreadsheetCell a1 = this.cell("$A$1", "=40+" + labelD4);
        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a1, number(40 + 20))
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
                );

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, d4.reference());
        this.loadReferrersAndCheck(cellReferenceStore, d4.reference());

        this.loadReferencesAndCheck(labelReferencesStore, labelB2);
        this.loadReferencesAndCheck(labelReferencesStore, labelD4, a1.reference());
    }

    @Test
    public void testSaveCellReplacesCellAndLabelReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();

        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = repository.labelReferences();

        final SpreadsheetLabelName labelB2 = SpreadsheetSelection.labelName("LABELB2");
        final SpreadsheetCellReference b2Reference = this.cellReference("B2");
        labelStore.save(SpreadsheetLabelMapping.with(labelB2, b2Reference));

        final SpreadsheetLabelName labelD4 = SpreadsheetSelection.labelName("LABELD4");
        labelStore.save(SpreadsheetLabelMapping.with(labelD4, this.cellReference("D4")));

        final SpreadsheetCell d4 = this.cell("$D$4", "=20");
        engine.saveCell(d4, context);

        final SpreadsheetCell e5 = this.cell("$E$5", "=30");
        engine.saveCell(e5, context);

        engine.saveCell(this.cell("$A$1", "=10+" + labelB2 + "+C2"), context);

        final SpreadsheetCellReference e5Reference = SpreadsheetSelection.parseCell("$E$5");
        final SpreadsheetCell a1 = this.cell("$A$1", "=40+" + labelD4 + "+" + e5Reference);
        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a1, number(40 + 20 + 30))
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
                );

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
    public void testSaveCellFormulaDateLiteral() {
        this.saveCellAndLoadAndFormattedCheck(
                "1999/12/31",
                LocalDate.of(1999, 12, 31)
        );
    }

    @Test
    public void testSaveCellFormulaDateTimeLiteral() {
        this.saveCellAndLoadAndFormattedCheck(
                "1999/12/31 12:34",
                LocalDateTime.of(
                        LocalDate.of(1999, 12, 31),
                        LocalTime.of(12, 34)
                )
        );
    }

    @Test
    public void testSaveCellFormulaNumberLiteral() {
        this.saveCellAndLoadAndFormattedCheck(
                "123",
                this.expressionNumberKind().create(123)
        );
    }

    @Test
    public void testSaveCellFormulaNumber() {
        this.saveCellAndLoadAndFormattedCheck(
                "=123",
                this.expressionNumberKind().create(123)
        );
    }

    @Test
    public void testSaveCellFormulaNumberMath() {
        this.saveCellAndLoadAndFormattedCheck(
                "=123+456.75",
                this.expressionNumberKind()
                        .create(123 + 456.75)
        );
    }

    @Test
    public void testSaveCellFormulaNumberGreaterThan() {
        this.saveCellAndLoadAndFormattedCheck(
                "=123>45",
                true
        );
    }

    @Test
    public void testSaveCellFormulaNumberLessThanEquals() {
        this.saveCellAndLoadAndFormattedCheck(
                "=123<=45",
                false
        );
    }

    @Test
    public void testSaveCellFormulaStringEqualsSameCase() {
        this.saveCellAndLoadAndFormattedCheck(
                "=\"hello\"=\"hello\"",
                true
        );
    }

    @Test
    public void testSaveCellFormulaStringEqualsDifferentCase() {
        this.saveCellAndLoadAndFormattedCheck(
                "=\"hello\"=\"HELLO\"",
                true
        );
    }

    @Test
    public void testSaveCellFormulaStringEqualsDifferent() {
        this.saveCellAndLoadAndFormattedCheck(
                "=\"hello\"=\"different\"",
                false
        );
    }

    @Test
    public void testSaveCellFormulaStringNotEqualsSameCase() {
        this.saveCellAndLoadAndFormattedCheck(
                "=\"hello\"<>\"hello\"",
                false
        );
    }

    @Test
    public void testSaveCellFormulaStringNotEqualsDifferentCase() {
        this.saveCellAndLoadAndFormattedCheck(
                "=\"hello\"<>\"HELLO\"",
                false
        );
    }

    @Test
    public void testSaveCellFormulaStringNotEqualsDifferent() {
        this.saveCellAndLoadAndFormattedCheck(
                "=\"hello\"<>\"different\"",
                true
        );
    }

    @Test
    public void testSaveCellFormulaTimeLiteral() {
        this.saveCellAndLoadAndFormattedCheck(
                "12:34",
                LocalTime.of(12, 34)
        );
    }

    private void saveCellAndLoadAndFormattedCheck(final String formula,
                                                  final Object value) {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", formula);
        final SpreadsheetCell a1Formatted = this.formattedCell(
                a1,
                value
        );
        this.saveCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        a1Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        this.loadCellStoreAndCheck(context.storeRepository().cells(), a1Formatted);
    }

    @Test
    public void testSaveCellTwice() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", "'Hello");
        final SpreadsheetCell a1Formatted = this.formattedCell(
                a1,
                "Hello"
        );

        final SpreadsheetDelta saved = SpreadsheetDelta.EMPTY
                .setCells(
                        Sets.of(
                                a1Formatted
                        )
                ).setColumnWidths(
                        columnWidths("A")
                ).setRowHeights(
                        rowHeights("1")
                );

        this.saveCellAndCheck(
                engine,
                a1,
                context,
                saved
        );

        this.saveCellAndCheck(
                engine,
                a1,
                context,
                saved
        );

        this.loadCellStoreAndCheck(context.storeRepository().cells(), a1Formatted);
    }

    // saveCells......................................................................................................

    @Test
    public void testSaveCellsNoCells() {
        this.saveCellsAndCheck(
                this.createSpreadsheetEngine(),
                SpreadsheetDelta.NO_CELLS,
                SpreadsheetEngineContexts.fake(),
                SpreadsheetDelta.EMPTY
        );
    }

    @Test
    public void testSaveCellsOnlyValues() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", "=1+2");
        final SpreadsheetCell a1Formatted = this.formattedCell(
                a1,
                number(3)
        );

        final SpreadsheetCell b2 = this.cell("b2", "=4+5");
        final SpreadsheetCell b2Formatted = this.formattedCell(
                b2,
                number(9)
        );

        this.saveCellsAndCheck(
                engine,
                Sets.of(
                        a1, b2
                ),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        a1Formatted,
                                        b2Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("A,B")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        );

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        this.loadCellStoreAndCheck(
                cellStore,
                a1Formatted,
                b2Formatted
        );
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 0);
    }

    @Test
    public void testSaveCellsOnlyWithCrossReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", "=100");
        final SpreadsheetCell a1Formatted = this.formattedCell(
                a1,
                number(100)
        );

        final SpreadsheetCell b2 = this.cell("b2", "=a1+2");
        final SpreadsheetCell b2Formatted = this.formattedCell(
                b2,
                number(100 + 2)
        );

        this.saveCellsAndCheck(
                engine,
                Sets.of(
                        a1, b2
                ),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        a1Formatted,
                                        b2Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("A,B")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        );

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        this.loadCellStoreAndCheck(
                cellStore,
                a1Formatted,
                b2Formatted
        );
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 1); // b2 -> a1
    }

    @Test
    public void testSaveCellsOnlyWithCrossReferences2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", "=b2+1");
        final SpreadsheetCell a1Formatted = this.formattedCell(
                a1,
                number(1000 + 1)
        );

        final SpreadsheetCell b2 = this.cell("b2", "=1000");
        final SpreadsheetCell b2Formatted = this.formattedCell(
                b2,
                number(1000)
        );

        this.saveCellsAndCheck(
                engine,
                Sets.of(
                        a1, b2
                ),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        a1Formatted,
                                        b2Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("A,B")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        );

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        this.loadCellStoreAndCheck(
                cellStore,
                a1Formatted,
                b2Formatted
        );
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 1); // b2 -> a1
    }

    @Test
    public void testSaveCellsOnlyWithCrossReferences3() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCell a1 = this.cell("a1", "=b2+100");
        final SpreadsheetCell a1Formatted = this.formattedCell(
                a1,
                number(1000 + 100)
        );

        final SpreadsheetCell b2 = this.cell("b2", "=1000");
        final SpreadsheetCell b2Formatted = this.formattedCell(
                b2,
                number(1000)
        );

        final SpreadsheetCell c3 = this.cell("c3", "=a1+1");
        final SpreadsheetCell c3Formatted = this.formattedCell(
                c3,
                number(1000 + 100 + 1)
        );

        this.saveCellsAndCheck(
                engine,
                Sets.of(
                        a1, b2, c3
                ),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        a1Formatted,
                                        b2Formatted,
                                        c3Formatted
                                )
                        ).setColumnWidths(
                                columnWidths("A,B,C")
                        ).setRowHeights(
                                rowHeights("1,2,3")
                        )
        );

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();

        this.loadCellStoreAndCheck(
                cellStore,
                a1Formatted,
                b2Formatted,
                c3Formatted
        );
        this.loadLabelStoreAndCheck(labelStore);
        this.countAndCheck(cellReferenceStore, 2);
    }

    // deleteCell....................................................................................................

    @Test
    public void testDeleteCellsMatchingCellReference() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");

        engine.saveCell(this.cell(a1, "=123"), context);

        this.deleteCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(a1)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );
    }

    @Test
    public void testDeleteCellsMatchingCellRange() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        engine.saveCell(
                this.cell(
                        a1, "=111"
                ),
                context
        );

        final SpreadsheetCellReference a2 = SpreadsheetSelection.parseCell("A2");
        engine.saveCell(
                this.cell(
                        a2,
                        "=222"
                ),
                context
        );

        this.deleteCellAndCheck(
                engine,
                SpreadsheetSelection.parseCellRange("A1:A2"),
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(a1, a2)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        );
    }

    @Test
    public void testDeleteCellsMatchingColumn() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        engine.saveCell(
                this.cell(
                        a1, "=111"
                ),
                context
        );

        final SpreadsheetCellReference a2 = SpreadsheetSelection.parseCell("A2");
        engine.saveCell(
                this.cell(
                        a2,
                        "=222"
                ),
                context
        );

        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        engine.saveCell(
                this.cell(
                        c3,
                        "=333"
                ),
                context
        );

        this.deleteCellAndCheck(
                engine,
                SpreadsheetSelection.parseColumn("A"),
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(a1, a2)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        );
    }

    @Test
    public void testDeleteCellsWhereCellHasCellReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("$A$1");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("$B$2");

        engine.saveCell(this.cell(a1, "=99+" + b2), context);

        this.deleteCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(a1)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = context.storeRepository()
                .cellReferences();

        this.loadReferencesAndCheck(cellReferenceStore, a1);
        this.loadReferrersAndCheck(cellReferenceStore, a1);

        this.loadReferencesAndCheck(cellReferenceStore, b2);
        this.loadReferrersAndCheck(cellReferenceStore, b2);
    }

    @Test
    public void testDeleteCellsWhereCellHasCellReferrers() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);
        final SpreadsheetCellReference b2Reference = SpreadsheetSelection.parseCell("$B$2");

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+" + b2Reference);
        engine.saveCell(a1, context);

        final SpreadsheetCell b2 = this.cell("$B$2", "=20");
        engine.saveCell(b2, context);

        this.deleteCellAndCheck(
                engine,
                b2.reference(),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                a1,
                                                this.number(1) // https://github.com/mP1/walkingkooka-spreadsheet/issues/2549
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(
                                        b2.reference()
                                )
                        ).setColumnWidths(
                                columnWidths("A,B")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        );

        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = context.storeRepository()
                .cellReferences();

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference(), b2Reference.toRelative());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference(), a1.reference());
    }

    @Test
    public void testDeleteCellsIncludesColumn() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetColumnStore columnStore = context.storeRepository()
                .columns();

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");

        final SpreadsheetColumn a = a1.column()
                .column();

        columnStore.save(a);
        columnStore.save(
                SpreadsheetSelection.parseColumn("B")
                        .column()
        );

        engine.saveCell(this.cell(a1, "=123"), context);

        this.deleteCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setColumns(Sets.of(a))
                        .setDeletedCells(Sets.of(a1))
                        .setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );
    }

    @Test
    public void testDeleteCellWithLabelReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = repository.labelReferences();

        final SpreadsheetLabelName labelB2 = SpreadsheetSelection.labelName("LABELB2");
        final SpreadsheetCell b2 = this.cell("$B$2", "=20");
        labelStore.save(SpreadsheetLabelMapping.with(labelB2, b2.reference()));

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+" + labelB2);
        engine.saveCell(a1, context);

        engine.saveCell(b2, context);

        this.deleteCellAndCheck(
                engine,
                a1.reference(),
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(Sets.of(a1.reference()))
                        .setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference());

        this.loadReferencesAndCheck(labelReferencesStore, labelB2);
    }

    @Test
    public void testDeleteCellWithLabelReferrers() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetLabelStore labelStore = repository.labels();
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferenceStore = repository.cellReferences();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = repository.labelReferences();

        final SpreadsheetLabelName labelB2 = SpreadsheetSelection.labelName("LABELB2");
        final SpreadsheetCell b2 = this.cell("$B$2", "=20");
        final SpreadsheetLabelMapping labelMapping = SpreadsheetLabelMapping.with(
                labelB2,
                b2.reference()
        );
        labelStore.save(labelMapping);

        final SpreadsheetCell a1 = this.cell("$A$1", "=1+" + labelB2);
        engine.saveCell(a1, context);

        engine.saveCell(b2, context);

        this.deleteCellAndCheck(
                engine,
                b2.reference(),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                a1,
                                                SpreadsheetError.selectionNotFound(labelB2)
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(
                                        b2.reference()
                                )
                        ).setLabels(
                                Sets.of(labelMapping)
                        ).setColumnWidths(
                                columnWidths("A,B")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        );

        this.loadReferencesAndCheck(cellReferenceStore, a1.reference());
        this.loadReferrersAndCheck(cellReferenceStore, a1.reference());

        this.loadReferencesAndCheck(cellReferenceStore, b2.reference());
        this.loadReferrersAndCheck(cellReferenceStore, b2.reference());

        this.loadReferencesAndCheck(labelReferencesStore, labelB2, a1.reference());
    }

    @Test
    public void testDeleteCellWithRow() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetRowStore rowStore = context.storeRepository()
                .rows();

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");

        final SpreadsheetRow a = a1.row()
                .row();

        rowStore.save(a);
        rowStore.save(
                SpreadsheetSelection.parseRow("2")
                        .row()
        );

        engine.saveCell(this.cell(a1, "=123"), context);

        this.deleteCellAndCheck(
                engine,
                a1,
                context,
                SpreadsheetDelta.EMPTY
                        .setRows(Sets.of(a))
                        .setDeletedCells(Sets.of(a1))
                        .setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );
    }

    // loadColumn......................................................................................................

    @Test
    public void testLoadColumnMissingColumn() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        this.checkEquals(
                SpreadsheetDelta.EMPTY,
                engine.loadColumn(
                        SpreadsheetSelection.parseColumn("Z"),
                        context
                )
        );
    }

    @Test
    public void testLoadColumn() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetColumnReference columnReference = SpreadsheetSelection.parseColumn("Z");
        final SpreadsheetColumn column = columnReference.column()
                .setHidden(true);

        context.storeRepository()
                .columns()
                .save(column);

        this.checkEquals(
                SpreadsheetDelta.EMPTY
                        .setColumns(
                                Sets.of(column)
                        ),
                engine.loadColumn(
                        columnReference,
                        context
                )
        );
    }

    // saveColumn......................................................................................................

    @Test
    public void testSaveColumnWithoutCells() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        this.addFailingCellSaveWatcherAndDeleteWatcher(context);

        final SpreadsheetColumnReference reference = SpreadsheetSelection.parseColumn("B");

        engine.saveColumn(
                reference.column(),
                context
        );

        this.countAndCheck(
                context.storeRepository()
                        .columns(),
                1
        );
    }

    @Test
    public void testSaveColumnWithCells() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetColumnReference reference = SpreadsheetSelection.parseColumn("B");

        final SpreadsheetCell cell = this.cell(
                reference.setRow(SpreadsheetSelection.parseRow("2")),
                "=1+2"
        );

        context.storeRepository()
                .cells()
                .save(cell);

        engine.saveColumn(
                reference.column(),
                context
        );

        this.countAndCheck(
                context.storeRepository()
                        .columns(),
                1
        );

        this.loadCellAndCheckFormatted(
                engine,
                cell.reference(),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                this.expressionNumberKind().create(3),
                "3 " + FORMATTED_PATTERN_SUFFIX
        );
    }

    // https://github.com/mP1/walkingkooka-spreadsheet/issues/2022
    @Test
    public void testSaveColumnHiddenTheUnhiddenWithCells() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetColumnReference reference = SpreadsheetSelection.parseColumn("B");

        final SpreadsheetCell cell = this.cell(
                reference.setRow(SpreadsheetSelection.parseRow("2")),
                "=1+2"
        );

        context.storeRepository()
                .cells()
                .save(cell);

        final SpreadsheetColumn column = reference.column();

        engine.saveColumn(
                column.setHidden(true),
                context
        );

        this.countAndCheck(
                context.storeRepository()
                        .columns(),
                1
        );

        // cell B2 in hidden column B should not load.
        this.loadCellFailCheck(
                engine,
                cell.reference(),
                context
        );

        this.checkEquals(
                SpreadsheetDelta.EMPTY
                        .setColumns(
                                Sets.of(
                                        column
                                )
                        ),
                engine.saveColumn(
                        column.setHidden(false),
                        context
                )
        );

        this.loadCellAndCheckFormatted(
                engine,
                cell.reference(),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                this.expressionNumberKind().create(3),
                "3 " + FORMATTED_PATTERN_SUFFIX
        );
    }

    // deleteColumn....................................................................................................

    @Test
    public void testDeleteColumnZeroNothingDeleted() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference reference = this.cellReference(99, 0); // A3

        engine.saveCell(this.cell(reference, "=99+0"), context);

        this.addFailingCellSaveWatcherAndDeleteWatcher(context);

        this.deleteColumnsAndCheck(
                engine,
                reference.column(),
                0,
                context
        );

        this.countAndCheck(context.storeRepository().cells(), 1);
    }

    @Test
    public void testDeleteColumnNoCellsRefreshed() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("A1"); // A1
        final SpreadsheetCellReference b = this.cellReference("B2"); // B2
        final SpreadsheetCellReference c = this.cellReference("C3"); // C3

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        this.deleteColumnsAndCheck(
                engine,
                c.column(),
                1,
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(c)
                        ).setColumnWidths(
                                columnWidths("C")
                        ).setRowHeights(
                                rowHeights("3")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 2);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshed() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("A1"); // A1
        final SpreadsheetCellReference b = this.cellReference("B2"); // B2
        final SpreadsheetCellReference c = this.cellReference("C3"); // C3

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context); // deleted/replaced by $c
        engine.saveCell(this.cell(c, "=5+6"), context); // becomes b3

        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("b3", "=5+6", number(5 + 6))
                                )
                        ).setDeletedCells(
                                Sets.of(b, c)
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("2,3")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 2);
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
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8=8", true);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedEqualsFalse() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8=7", false);
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
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8<>7", true);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshedNotEqualsFalse() {
        this.deleteColumnColumnsAfterCellsRefreshedAndCheck("=8<>8", false);
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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("A1"); // A1
        final SpreadsheetCellReference b = this.cellReference("B2"); // B2
        final SpreadsheetCellReference c = this.cellReference("C3"); // C3

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context); // deleted/replaced by $c
        engine.saveCell(this.cell(c, formula), context); // becomes b3

        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("b3", formula, value)
                                )
                        ).setDeletedCells(
                                Sets.of(b, c)
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("2,3")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 2);
    }

    @Test
    public void testDeleteColumnColumnsAfterCellsRefreshed2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("A1");
        final SpreadsheetCellReference b = this.cellReference("B2"); //replaced by $c
        final SpreadsheetCellReference c = this.cellReference("C3");
        final SpreadsheetCellReference d = this.cellReference("Z99");// B99 moved

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);
        engine.saveCell(this.cell(d, "=7+8"), context);

        final int count = 1;
        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("B3", "=5+6", number(5 + 6)),
                                        this.formattedCell("Y99", "=7+8", number(7 + 8))
                                )
                        ).setDeletedCells(
                                Sets.of(b, c, d)
                        ).setColumnWidths(
                                columnWidths("B,C,Y,Z")
                        ).setRowHeights(
                                rowHeights("2,3,99")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 3);
    }

    @Test
    public void testDeleteColumnWithLabelsToCellReferenceIgnored() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = SpreadsheetSelection.parseCell("A1"); // A1
        final SpreadsheetCellReference b = SpreadsheetSelection.parseCell("E2"); // E2

        engine.saveCell(this.cell(a, "=99+0"), context);
        engine.saveCell(this.cell(b, "=2+0+" + LABEL), context);

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a));

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                b.column().add(-1),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(b.addColumn(-1), "=2+0+" + LABEL, number(2 + 99))
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("D,E")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );

        this.countAndCheck(cellStore, 2);

        this.loadLabelAndCheck(labelStore, LABEL, a);
    }

    @Test
    public void testDeleteColumnWithLabelsToCellReferencedFixed() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

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
        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+" + LABEL, number(1 + 99 + 0)),
                                        this.formattedCell(c.addColumn(-count), "=3+0", number(3 + 0)),
                                        this.formattedCell(d.addColumn(-count), "=4+" + LABEL, number(4 + 99 + 0)),
                                        this.formattedCell(e.addColumn(-count), "=99+0", number(99 + 0))
                                )
                        ).setLabels(
                                Sets.of(
                                        LABEL.mapping(SpreadsheetSelection.parseCell("$N$10"))
                                )
                        ).setDeletedCells(
                                Sets.of(c, d, e)
                        ).setColumnWidths(
                                columnWidths("A,B,C,M,N,O")
                        ).setRowHeights(
                                rowHeights("1,9,10")
                        )
        ); // old $b delete, $c,$d columns -1.

        this.loadLabelAndCheck(labelStore, LABEL, e.addColumn(-count));

        this.countAndCheck(cellStore, 4);
    }

    @Test
    public void testDeleteColumnWithLabelToDeletedCell() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // A2

        engine.saveCell(this.cell(a, "=1+0"), context);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, b));

        final int count = 1;
        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
        ); // $b delete, $c columns -1.

        this.loadLabelFailCheck(labelStore, LABEL);

        this.countAndCheck(cellStore, 1);
    }

    @Test
    public void testDeleteColumnWithCellReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
        this.deleteColumnsAndCheck(
                engine,
                c.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+" + d.addColumn(-count), number(1 + 4)),
                                        this.formattedCell(d.addColumn(-count), "=4", number(4)),
                                        this.formattedCell(e.addColumn(-count), "=5+" + b, number(5 + 2))
                                )
                        ).setDeletedCells(
                                Sets.of(c, d, e)
                        ).setColumnWidths(
                                columnWidths("A,K,M,N,O")
                        ).setRowHeights(
                                rowHeights("1,9,10")
                        )
        ); // $c delete

        this.countAndCheck(context.storeRepository().cells(), 4);
    }

    @Test
    public void testDeleteColumnWithCellReferences2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
        this.deleteColumnsAndCheck(
                engine,
                c.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+" + d.addColumn(-count), number(1 + 4)),
                                        this.formattedCell(d.addColumn(-count), "=4", number(4)),
                                        this.formattedCell(e.addColumn(-count), "=5+" + b, number(5 + 2))
                                )
                        ).setDeletedCells(
                                Sets.of(c, d, e)
                        ).setColumnWidths(
                                columnWidths("A,K,L,M,N,O")
                        ).setRowHeights(
                                rowHeights("1,9,10")
                        )
        ); // $c deleted, old-d & old-e refreshed

        this.countAndCheck(context.storeRepository().cells(), 4);
    }

    @Test
    public void testDeleteColumnWithCellReferencesToDeletedCell() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(0, 0); // A1
        final SpreadsheetCellReference b = this.cellReference(1, 0); // A2

        engine.saveCell(this.cell(a, "=1+" + b), context);
        engine.saveCell(this.cell(b, "=2"), context);

        final int count = 1;
        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                a,
                                                "=1+#REF!",
                                                SpreadsheetError.selectionDeleted()
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("A,B")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        ); // $b delete

        this.countAndCheck(context.storeRepository().cells(), 1);
    }

    @Test
    public void testDeleteColumnSeveral() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
        this.deleteColumnsAndCheck(
                engine,
                this.column(7),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(d.addColumn(-count), "=4", number(4)),
                                        this.formattedCell(e.addColumn(-count), "=5", number(5)),
                                        this.formattedCell(f.addColumn(-count), "=6", number(6))
                                )
                        ).setDeletedCells(
                                Sets.of(b, c, d, e, f)
                        ).setColumnWidths(
                                columnWidths("H,K,L,M,P,Q,U,V")
                        ).setRowHeights(
                                rowHeights("1,3,4,5")
                        )
        ); // $b & $c

        this.countAndCheck(context.storeRepository().cells(), 4);
    }

    // loadRow......................................................................................................

    @Test
    public void testLoadRowMissingRow() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        this.checkEquals(
                SpreadsheetDelta.EMPTY,
                engine.loadRow(
                        SpreadsheetSelection.parseRow("999"),
                        context
                )
        );
    }

    @Test
    public void testLoadRow() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetRowReference rowReference = SpreadsheetSelection.parseRow("999");
        final SpreadsheetRow row = rowReference.row()
                .setHidden(true);

        context.storeRepository()
                .rows()
                .save(row);

        this.checkEquals(
                SpreadsheetDelta.EMPTY
                        .setRows(
                                Sets.of(row)
                        ),
                engine.loadRow(
                        rowReference,
                        context
                )
        );
    }

    // saveRow......................................................................................................

    @Test
    public void testSaveRowWithoutCells() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        this.addFailingCellSaveWatcherAndDeleteWatcher(context);

        final SpreadsheetRowReference reference = SpreadsheetSelection.parseRow("2");

        engine.saveRow(
                reference.row(),
                context
        );

        this.countAndCheck(
                context.storeRepository()
                        .rows(),
                1
        );
    }

    @Test
    public void testSaveRowWithCells() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetRowReference reference = SpreadsheetSelection.parseRow("2");

        final SpreadsheetCell cell = this.cell(
                reference.setColumn(SpreadsheetSelection.parseColumn("B")),
                "=1+2"
        );

        context.storeRepository()
                .cells()
                .save(cell);

        engine.saveRow(
                reference.row(),
                context
        );

        this.countAndCheck(
                context.storeRepository()
                        .rows(),
                1
        );

        this.loadCellAndCheckFormatted(
                engine,
                cell.reference(),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                this.expressionNumberKind().create(3),
                "3 " + FORMATTED_PATTERN_SUFFIX
        );
    }

    // https://github.com/mP1/walkingkooka-spreadsheet/issues/2023
    @Test
    public void testSaveRowHiddenTheUnhiddenWithCells() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetRowReference reference = SpreadsheetSelection.parseRow("2");

        final SpreadsheetCell cell = this.cell(
                reference.setColumn(SpreadsheetSelection.parseColumn("B")),
                "=1+2"
        );

        context.storeRepository()
                .cells()
                .save(cell);

        final SpreadsheetRow row = reference.row();

        engine.saveRow(
                row.setHidden(true),
                context
        );

        this.countAndCheck(
                context.storeRepository()
                        .rows(),
                1
        );

        // cell B2 in hidden row B should not load.
        this.loadCellFailCheck(
                engine,
                cell.reference(),
                context
        );

        this.checkEquals(
                SpreadsheetDelta.EMPTY
                        .setRows(
                                Sets.of(
                                        row
                                )
                        ),
                engine.saveRow(
                        row.setHidden(false),
                        context
                )
        );

        this.loadCellAndCheckFormatted(
                engine,
                cell.reference(),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                this.expressionNumberKind().create(3),
                "3 " + FORMATTED_PATTERN_SUFFIX
        );
    }

    // deleteRow....................................................................................................

    @Test
    public void testDeleteRowsNone() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference reference = this.cellReference(0, 1); // A2

        engine.saveCell(this.cell(reference, "=99+0"), context);

        this.addFailingCellSaveWatcherAndDeleteWatcher(context);

        this.deleteRowsAndCheck(engine,
                reference.row(),
                0,
                context);

        this.countAndCheck(context.storeRepository().cells(), 1);
    }

    @Test
    public void testDeleteRowsOne() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(c.addRow(-1), "=5+6", number(5 + 6))
                                )
                        ).setDeletedCells(
                                Sets.of(c)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("2,3")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 2);

        this.loadCellAndCheckFormulaAndValue(engine,
                c.addRow(-1),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+6",
                number(5 + 6));
    }

    @Test
    public void testDeleteRowsOne2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("A1"); //
        final SpreadsheetCellReference b = this.cellReference("A2"); // replaced by c
        final SpreadsheetCellReference c = this.cellReference("A3"); // DELETED
        final SpreadsheetCellReference d = this.cellReference("B10"); // moved

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);
        engine.saveCell(this.cell(d, "=7+8"), context);

        final int count = 1;
        this.deleteRowsAndCheck(
                engine,
                b.row(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(c.addRow(-count), "=5+6", number(5 + 6)),
                                        this.formattedCell(d.addRow(-count), "=7+8", number(7 + 8))
                                )
                        ).setDeletedCells(
                                Sets.of(c, d)
                        ).setColumnWidths(
                                columnWidths("A,B")
                        ).setRowHeights(
                                rowHeights("2,3,9,10")
                        )
        ); // $b delete

        this.countAndCheck(context.storeRepository().cells(), 3);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));
    }

    @Test
    public void testDeleteRowsMany() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(d.addRow(-count), "=7+8", number(7 + 8))
                                )
                        ).setDeletedCells(
                                Sets.of(b, c, d)
                        ).setColumnWidths(
                                columnWidths("A,B")
                        ).setRowHeights(
                                rowHeights("2,3,8,10")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 2);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$A$2");
        final SpreadsheetCellReference c = this.cellReference("$A$6");

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(b, "=20+0+" + LABEL), context);
        engine.saveCell(this.cell(c, "=99+0"), context);

        final int count = 2;
        this.deleteRowsAndCheck(
                engine,
                b.row().add(count),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$4", "=99+0", number(99 + 0))
                                )
                        ).setDeletedCells(
                                Sets.of(c)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("4,6")
                        )
        ); // $c moved, $b unmodified label refs $a also unmodified.

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$A$6");

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, b));

        cellStore.save(this.cell(a, "=1+0+" + LABEL));
        cellStore.save(this.cell(b, "=2+0"));

        final int count = 2;
        this.deleteRowsAndCheck(
                engine,
                a.row().add(1),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(Sets.of(
                                this.formattedCell(a, "=1+0+" + LABEL, number(1 + 0 + 2 + 0)),
                                this.formattedCell(b.addRow(-count), "=2+0", number(2 + 0)))
                        ).setLabels(
                                Sets.of(
                                        LABEL.mapping(SpreadsheetSelection.parseCell("$A$4"))
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1,4,6")
                        )
        ); // $b moved

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$A$6");

        engine.saveCell(this.cell(a, "=1+" + b), context);
        engine.saveCell(this.cell(b, "=2+0"), context);

        this.deleteRowsAndCheck(
                engine,
                b.row(),
                1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                a,
                                                "=1+#REF!",
                                                SpreadsheetError.selectionDeleted()
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1,6")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 1);

        this.loadCellAndCheckFormulaAndValue(
                engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+#REF!",
                SpreadsheetError.selectionDeleted()
        ); // reference should have been fixed.
    }

    @Test
    public void testDeleteRowsWithCellReferencesFixed() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
        this.deleteRowsAndCheck(
                engine,
                c.row(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$1", "=1+$I$13", number(1 + 4)),
                                        this.formattedCell("$I$13", "=4", number(4)),
                                        this.formattedCell("$J$14", "=5+$A$2", number(5 + 2))
                                )
                        ).setDeletedCells(
                                Sets.of(c, d, e)
                        ).setColumnWidths(
                                columnWidths("A,I,J")
                        ).setRowHeights(
                                rowHeights("1,11,13,14,15")
                        )
        ); // $c delete

        this.countAndCheck(context.storeRepository().cells(), 4);

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
                FORMATTED_PATTERN_SUFFIX);

        this.loadCellAndCheckFormatted2(engine,
                d.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(4),
                FORMATTED_PATTERN_SUFFIX);

        this.loadCellAndCheckFormulaAndValue(engine,
                e.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+" + b,
                number(5 + 2));
    }

    @Test
    public void testDeleteRowsWithCellReferencesFixed2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
        this.deleteRowsAndCheck(
                engine,
                c.row(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$1", "=1+$I$12", number(1 + 4)),
                                        this.formattedCell("$I$12", "=4", number(4)),
                                        this.formattedCell("$J$13", "=5+$A$2", number(5 + 2))
                                )
                        ).setDeletedCells(
                                Sets.of(c, d, e)
                        ).setColumnWidths(
                                columnWidths("A,I,J")
                        ).setRowHeights(
                                rowHeights("1,11,12,13,14,15")
                        )
        ); // $c delete

        this.countAndCheck(
                context.storeRepository()
                        .cells(),
                4
        );

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
                FORMATTED_PATTERN_SUFFIX);

        this.loadCellAndCheckFormatted2(engine,
                d.addRow(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(4),
                FORMATTED_PATTERN_SUFFIX);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 15);

        final SpreadsheetCellRange ab = a.cellRange(b);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, ab));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(c, "=20+0+" + LABEL), context);
        engine.saveCell(this.cell(d, "=99+0"), context); // DELETED

        final int count = 2;
        this.deleteRowsAndCheck(engine,
                d.row(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(d)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("16")
                        )
        );

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);

        engine.saveCell(this.cell(a, "=1+0"), context);

        final SpreadsheetCellRange bc = b.cellRange(c);
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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 20);

        final SpreadsheetCellRange bc = b.cellRange(c);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bc));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(d, "=20+0"), context);

        final int count = c.row().value() - b.row().value() + 1;
        this.deleteRowsAndCheck(
                engine,
                b.row(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(d.addRow(-count), "=20+0", number(20 + 0))
                                )
                        ).setDeletedCells(
                                Sets.of(d)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("15,21")
                        )
        ); // b..c deleted, d moved

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);

        final SpreadsheetCellRange bc = b.cellRange(c);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bc));

        engine.saveCell(this.cell(a, "=1+0+" + LABEL), context);
        engine.saveCell(this.cell(b, "=20+0"), context);

        final int count = c.row().value() - b.row().value() + 1;
        this.deleteRowsAndCheck(
                engine,
                b.row(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        formattedCell(
                                                a,
                                                "=1+0+" + LABEL,
                                                SpreadsheetError.selectionNotFound(LABEL)
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1,6")
                        )
        ); // b..c deleted

        this.countAndCheck(cellStore, 1); // a
        this.countAndCheck(labelStore, 0);

        this.loadCellAndCheckFormulaAndValue(
                engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + LABEL,
                SpreadsheetError.selectionNotFound(LABEL)
        );
    }

    @Test
    public void testDeleteRowsWithLabelsToRangeFixed() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 15);
        final SpreadsheetCellReference e = this.cellReference(0, 20);

        final SpreadsheetCellRange de = d.cellRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, de));

        engine.saveCell(this.cell(a, "=1+0+" + LABEL), context);
        engine.saveCell(this.cell(d, "=20+0"), context);

        final int count = c.row().value() - b.row().value() + 1;
        this.deleteRowsAndCheck(
                engine,
                b.row(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+0+" + LABEL, number(1 + 0 + 20 + 0)),
                                        this.formattedCell(d.addRow(-count), "=20+0", number(20 + 0))
                                )
                        ).setLabels(
                                Sets.of(
                                        LABEL.mapping(
                                                SpreadsheetSelection.parseCellRange("$A$10:$A$15")
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(d)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1,10,16")
                        )
        ); // b..c deleted, d moved

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
        this.loadLabelAndCheck(labelStore, LABEL, begin.cellRange(end));
    }

    @SuppressWarnings("unused")
    @Test
    public void testDeleteRowsWithLabelsToRangeFixed2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 15);
        final SpreadsheetCellReference e = this.cellReference(0, 20);

        final SpreadsheetCellRange ce = c.cellRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, ce));

        final int count = e.row().value() - d.row().value() + 1;
        this.deleteRowsAndCheck(engine,
                d.row(),
                count,
                context); // b..c deleted, d moved

        this.countAndCheck(labelStore, 1);
        this.loadLabelAndCheck(labelStore, LABEL, c.cellRange(d));
    }

    @Test
    public void testDeleteRowsWithLabelsToRangeFixed3() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 15);

        final SpreadsheetCellRange bd = b.cellRange(d);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bd));

        final int count = 1;
        this.deleteRowsAndCheck(engine,
                c.row(),
                count,
                context); // b..c deleted, d moved

        this.countAndCheck(labelStore, 1);

        final SpreadsheetCellReference end = d.addRow(-count);
        this.loadLabelAndCheck(labelStore, LABEL, b.cellRange(end));
    }

    @SuppressWarnings("unused")
    @Test
    public void testDeleteRowsWithLabelsToRangeFixed4() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetCellReference a = this.cellReference(0, 5);  // delete
        final SpreadsheetCellReference b = this.cellReference(0, 10); // range delete
        final SpreadsheetCellReference c = this.cellReference(0, 15); // range delete
        final SpreadsheetCellReference d = this.cellReference(0, 20); // range
        final SpreadsheetCellReference e = this.cellReference(0, 25); // range

        final SpreadsheetCellRange be = b.cellRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, be));

        final int count = c.row().value() - a.row().value();
        this.deleteRowsAndCheck(engine,
                a.row(),
                count,
                context);

        this.countAndCheck(labelStore, 1);

        this.loadLabelAndCheck(labelStore, LABEL, a.cellRange(b));
    }

    // deleteColumn....................................................................................................

    @Test
    public void testDeleteColumnsNone() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference reference = this.cellReference(1, 0); // A2

        engine.saveCell(this.cell(reference, "=99+0"), context);

        this.addFailingCellSaveWatcherAndDeleteWatcher(context);

        engine.deleteColumns(reference.column(), 0, context);

        this.countAndCheck(context.storeRepository().cells(), 1);

        this.loadCellAndCheckFormulaAndValue(engine,
                reference,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99));
    }

    @Test
    public void testDeleteColumnsOne() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("$A1");
        final SpreadsheetCellReference b = this.cellReference(1, 0); // B1
        final SpreadsheetCellReference c = this.cellReference(2, 0); // C1

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$B$1", "=5+6", number(5 + 6))
                                )
                        ).setDeletedCells(
                                Sets.of(c)
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 2);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); //
        final SpreadsheetCellReference b = this.cellReference("$B$1"); // replaced by c
        final SpreadsheetCellReference c = this.cellReference("$C$1"); // DELETED
        final SpreadsheetCellReference d = this.cellReference("$J$2"); // moved

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context); // DELETE
        engine.saveCell(this.cell(c, "=5+6"), context);
        engine.saveCell(this.cell(d, "=7+8"), context);

        final int count = 1;

        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$B$1", "=5+6", number(5 + 6)),
                                        this.formattedCell("$I$2", "=7+8", number(7 + 8))
                                )
                        ).setDeletedCells(
                                Sets.of(c, d)
                        ).setColumnWidths(
                                columnWidths("B,C,I,J")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        ); // $b delete

        this.countAndCheck(context.storeRepository().cells(), 3);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$H$2", "=7+8", number(7 + 8))
                                )
                        ).setDeletedCells(
                                Sets.of(b, c, d)
                        ).setColumnWidths(
                                columnWidths("B,C,H,J")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
                ); // $b, $c deleted

        this.countAndCheck(context.storeRepository().cells(), 2);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$D$1", "=99+0", number(99 + 0))
                                )
                        ).setDeletedCells(
                                Sets.of(c)
                        ).setColumnWidths(
                                columnWidths("D,F")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$E$1");

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, b));

        engine.saveCell(this.cell(a, "=1+0+" + LABEL), context);
        engine.saveCell(this.cell(b, "=2+0"), context);

        final int count = 2;
        this.deleteColumnsAndCheck(
                engine,
                a.column().add(1),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$1", "=1+0+" + LABEL, number(1 + 0 + 2 + 0)),
                                        this.formattedCell("$C$1", "=2+0", number(2 + 0))
                                )
                        ).setLabels(
                                Sets.of(
                                        LABEL.mapping(SpreadsheetSelection.parseCell("$C$1"))
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("A,C,E")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        ); // $b moved

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1");
        final SpreadsheetCellReference b = this.cellReference("$E$1");

        engine.saveCell(this.cell(a, "=1+" + b), context);
        engine.saveCell(this.cell(b, "=2+0"), context);

        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                a,
                                                "=1+#REF!",
                                                SpreadsheetError.selectionDeleted()
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("A,E")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        ); // $v delete

        this.countAndCheck(context.storeRepository().cells(), 1);

        this.loadCellAndCheckFormulaAndValue(
                engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+#REF!",
                SpreadsheetError.selectionDeleted()
        ); // reference should have been fixed.
    }

    @Test
    public void testDeleteColumnsWithCellReferencesFixed() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
        this.deleteColumnsAndCheck(
                engine,
                c.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$1", "=1+$M$9", number(1 + 4)),
                                        this.formattedCell("$M$9", "=4", number(4)),
                                        this.formattedCell("$N$10", "=5+" + b, number(5 + 2))
                                )
                        ).setDeletedCells(
                                Sets.of(c, d, e)
                        ).setColumnWidths(
                                columnWidths("A,K,M,N,O")
                        ).setRowHeights(
                                rowHeights("1,9,10")
                        )
        ); // $c delete

        this.countAndCheck(context.storeRepository().cells(), 4);

        this.loadCellAndCheckFormulaAndValue(
                engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+" + d.addColumn(-count),

                number(1 + 4)
        ); // reference should have been fixed.

        this.loadCellAndCheckFormatted2(engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(2),
                FORMATTED_PATTERN_SUFFIX);

        this.loadCellAndCheckFormatted2(engine,
                d.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(4),
                FORMATTED_PATTERN_SUFFIX);

        this.loadCellAndCheckFormulaAndValue(engine,
                e.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=5+" + b,
                number(5 + 2));
    }

    @Test
    public void testDeleteColumnsWithCellReferencesFixed2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
        this.deleteColumnsAndCheck(
                engine,
                c.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$1", "=1+$L$9", number(1 + 4)),
                                        this.formattedCell("$L$9", "=4", number(4)),
                                        this.formattedCell("$M$10", "=5+$B$1", number(5 + 2))
                                )
                        ).setDeletedCells(
                                Sets.of(c, d, e)
                        ).setColumnWidths(
                                columnWidths("A,K,L,M,N,O")
                        ).setRowHeights(
                                rowHeights("1,9,10")
                        )
        ); // $c delete

        this.countAndCheck(context.storeRepository().cells(), 4);

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
                FORMATTED_PATTERN_SUFFIX);

        this.loadCellAndCheckFormatted2(engine,
                d.addColumn(-count),
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                number(4),
                FORMATTED_PATTERN_SUFFIX);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(15, 0);

        final SpreadsheetCellRange ab = a.cellRange(b);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, ab));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(c, "=20+0+" + LABEL), context);
        engine.saveCell(this.cell(d, "=99+0"), context); // deleted!!!

        final int count = 2;
        this.deleteColumnsAndCheck(
                engine,
                d.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(d)
                        ).setColumnWidths(
                                columnWidths("P")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        ); // $d moved

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);

        final SpreadsheetCellRange bc = b.cellRange(c);
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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(20, 0);

        final SpreadsheetCellRange bc = b.cellRange(c);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bc));

        engine.saveCell(this.cell(a, "=1+0"), context);
        engine.saveCell(this.cell(d, "=20+0"), context);

        final int count = c.column().value() - b.column().value() + 1;
        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(d.addColumn(-count), "=20+0", number(20 + 0))
                                )
                        ).setDeletedCells(
                                Sets.of(d)
                        ).setColumnWidths(
                                columnWidths("O,U")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        ); // b..c deleted, d moved

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);

        final SpreadsheetCellRange bc = b.cellRange(c);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bc));

        engine.saveCell(this.cell(a, "=1+0+" + LABEL), context);
        engine.saveCell(this.cell(b, "=20+0"), context);

        final int count = c.column().value() - b.column().value() + 1;
        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                "$A$1",
                                                "=1+0+" + LABEL,
                                                SpreadsheetError.selectionNotFound(LABEL)
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("A,F")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        ); // b..c deleted

        this.countAndCheck(cellStore, 1); // a
        this.countAndCheck(labelStore, 0);

        this.loadCellAndCheckFormulaAndValue(
                engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+0+" + LABEL,
                SpreadsheetError.selectionNotFound(LABEL)
        );
    }

    @Test
    public void testDeleteColumnsWithLabelsToRangeFixed() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(15, 0);
        final SpreadsheetCellReference e = this.cellReference(20, 0);

        final SpreadsheetCellRange de = d.cellRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, de));

        engine.saveCell(this.cell(a, "=1+0+" + LABEL), context);
        engine.saveCell(this.cell(d, "=20+0"), context);

        final int count = c.column().value() - b.column().value() + 1;
        this.deleteColumnsAndCheck(
                engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+0+" + LABEL, number(1 + 0 + 20 + 0)),
                                        this.formattedCell(d.addColumn(-count), "=20+0", number(20 + 0)))
                        ).setLabels(
                                Sets.of(
                                        LABEL.mapping(
                                                SpreadsheetSelection.parseCellRange("$J$1:$O$1")
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(d)
                        ).setColumnWidths(
                                columnWidths("A,J,P")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        ); // b..c deleted, d moved

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
        this.loadLabelAndCheck(labelStore, LABEL, begin.cellRange(end));
    }

    @SuppressWarnings("unused")
    @Test
    public void testDeleteColumnsWithLabelsToRangeFixed2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(15, 0);
        final SpreadsheetCellReference e = this.cellReference(20, 0);

        final SpreadsheetCellRange ce = c.cellRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, ce));

        final int count = e.column().value() - d.column().value() + 1;
        this.deleteColumnsAndCheck(engine,
                d.column(),
                count,
                context); // b..c deleted, d moved

        this.countAndCheck(labelStore, 1);
        this.loadLabelAndCheck(labelStore, LABEL, c.cellRange(d));
    }

    @Test
    public void testDeleteColumnsWithLabelsToRangeFixed3() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(15, 0);

        final SpreadsheetCellRange bd = b.cellRange(d);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, bd));

        final int count = 1;
        this.deleteColumnsAndCheck(engine,
                c.column(),
                count,
                context); // b..c deleted, d moved

        this.countAndCheck(labelStore, 1);

        final SpreadsheetCellReference end = d.addColumn(-count);
        this.loadLabelAndCheck(labelStore, LABEL, b.cellRange(end));
    }

    @SuppressWarnings("unused")
    @Test
    public void testDeleteColumnsWithLabelsToRangeFixed4() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetCellReference a = this.cellReference(5, 0);  // delete
        final SpreadsheetCellReference b = this.cellReference(10, 0); // range delete
        final SpreadsheetCellReference c = this.cellReference(15, 0); // range delete
        final SpreadsheetCellReference d = this.cellReference(20, 0); // range
        final SpreadsheetCellReference e = this.cellReference(25, 0); // range

        final SpreadsheetCellRange be = b.cellRange(e);
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, be));

        final int count = c.column().value() - a.column().value();
        this.deleteColumnsAndCheck(engine,
                a.column(),
                count,
                context);

        this.countAndCheck(labelStore, 1);

        this.loadLabelAndCheck(labelStore, LABEL, a.cellRange(b));
    }

    // insertColumn....................................................................................................

    @Test
    public void testInsertColumnsZero() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference reference = this.cellReference("$CV$1");

        engine.saveCell(this.cell(reference, "=99+0"),
                context);

        this.addFailingCellSaveWatcherAndDeleteWatcher(context);

        this.insertColumnsAndCheck(engine,
                reference.column(),
                0,
                context);

        this.countAndCheck(context.storeRepository().cells(), 1);

        this.loadCellAndCheckFormulaAndValue(engine,
                reference,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99));
    }

    @Test
    public void testInsertColumns() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$B$1"); // MOVED
        final SpreadsheetCellReference c = this.cellReference("$C$1"); // MOVED

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        final int count = 1;
        this.insertColumnsAndCheck(
                engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$C$1", "=3+4", number(3 + 4)),
                                        this.formattedCell("$D$1", "=5+6", number(5 + 6))
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("B,C,D")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 3);

        this.loadCellAndCheckFormulaAndValue(engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=1+2",
                number(1 + 2));
    }

    @Test
    public void testInsertColumns2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$B$1"); // MOVED
        final SpreadsheetCellReference c = this.cellReference("$C$1"); // MOVED

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        final int count = 1;
        this.insertColumnsAndCheck(
                engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$C$1", "=3+4", number(3 + 4)),
                                        this.formattedCell("$D$1", "=5+6", number(5 + 6))
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("B,C,D")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        ); // $b insert

        this.countAndCheck(context.storeRepository().cells(), 3);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$F$4", "=2+" + LABEL, number(2 + 100))
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("E,F")
                        ).setRowHeights(
                                rowHeights("4")
                        )
        ); // $b insert

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+" + LABEL, number(1 + 99 + 0)),
                                        this.formattedCell("$C$1", "=2+0", number(2 + 0)),
                                        this.formattedCell("$D$1", "=3+0+" + LABEL, number(3 + 0 + 99 + 0)),
                                        this.formattedCell("$O$9", "=99+0", number(99 + 0))
                                )
                        ).setLabels(
                                Sets.of(
                                        LABEL.mapping(SpreadsheetSelection.parseCell("$O$9"))
                                )
                        ).setDeletedCells(
                                Sets.of(b, d)
                        ).setColumnWidths(
                                columnWidths("A,B,C,D,N,O")
                        ).setRowHeights(
                                rowHeights("1,9")
                        )
        ); // $b insert

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$F$6"); // moved

        final SpreadsheetCellRange a1 = a.cellRange(a.add(1, 1));
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a1));

        engine.saveCell(this.cell(a, "=99+0"), context);
        engine.saveCell(this.cell(b, "=2+0+" + LABEL), context);

        final int count = 1;
        this.insertColumnsAndCheck(engine,
                b.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$G$6", "=2+0+" + LABEL, number(2 + 0 + 99 + 0))
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("F,G")
                        ).setRowHeights(
                                rowHeights("6")
                        )
        ); // $b insert

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(5, 0);
        final SpreadsheetCellReference c = this.cellReference(10, 0);
        final SpreadsheetCellReference d = this.cellReference(15, 0);
        final SpreadsheetCellReference e = this.cellReference(20, 0);

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, c.cellRange(d)));

        engine.saveCell(this.cell(a, "=1+" + LABEL), context);
        engine.saveCell(this.cell(c, "=99+0"), context);

        this.insertColumnsAndCheck(
                engine,
                b.column(),
                c.column().value() - b.column().value(),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+" + LABEL, number(1 + 99 + 0)),
                                        this.formattedCell("$P$1", "=99+0", number(99 + 0))
                                )
                        ).setLabels(
                                Sets.of(
                                        LABEL.mapping(
                                                SpreadsheetSelection.parseCellRange("$P$1:$U$1")
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(c)
                        ).setColumnWidths(
                                columnWidths("A,K,P")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        ); // $b insert

        this.countAndCheck(labelStore, 1);
        this.loadLabelAndCheck(labelStore, LABEL, d.cellRange(e));

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$1", "=1+0+$O$9", number(1 + 0 + 4 + 0 + 2 + 0)),
                                        this.formattedCell("$L$1", "=3+0", number(3 + 0)),
                                        this.formattedCell("$O$9", "=4+0+" + b, number(4 + 0 + 2 + 0))
                                )
                        ).setDeletedCells(
                                Sets.of(c, d)
                        ).setColumnWidths(
                                columnWidths("A,K,L,N,O")
                        ).setRowHeights(
                                rowHeights("1,9")
                        )
        ); // $c insert

        this.countAndCheck(context.storeRepository().cells(), 4);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$1", "=1+0+$P$9", number(1 + 0 + 4 + 0 + 2 + 0)),
                                        this.formattedCell("$M$1", "=3+0", number(3 + 0)),
                                        this.formattedCell("$P$9", "=4+0+" + b, number(4 + 0 + 2 + 0))
                                )
                        )
                        .setDeletedCells(
                                Sets.of(c, d)
                        ).setColumnWidths(
                                columnWidths("A,K,M,N,P")
                        ).setRowHeights(
                                rowHeights("1,9")
                        )
        ); // $c insert

        this.countAndCheck(context.storeRepository().cells(), 4);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$P$1", "=2+0", number(2 + 0)),
                                        this.formattedCell("$Q$1", "=3+0", number(3 + 0)),
                                        this.formattedCell("$R$3", "=4+0", number(4 + 0)),
                                        this.formattedCell("$Z$4", "=5+0", number(5 + 0))
                                )
                        ).setDeletedCells(
                                Sets.of(b, c, d, e)
                        ).setColumnWidths(
                                columnWidths("K,L,M,P,Q,R,U,Z")
                        ).setRowHeights(
                                rowHeights("1,3,4")
                        )
        ); // $b & $c

        this.countAndCheck(context.storeRepository().cells(), 5);

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

    @Test
    public void testInsertColumnsWithColumns() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = this.cellReference("A1"); // A1
        final SpreadsheetCellReference b1 = this.cellReference("B1"); // MOVED

        final SpreadsheetColumn b = b1.column()
                .column();
        engine.saveColumn(b, context);

        final SpreadsheetColumn c = SpreadsheetSelection.parseColumn("c")
                .column();
        engine.saveColumn(c, context);

        engine.saveCell(this.cell(a1, ""), context);
        engine.saveCell(this.cell(b1, ""), context);

        final int count = 1;
        this.insertColumnsAndCheck(
                engine,
                b1.column(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("C1", "", "")
                                )
                        ).setColumns(
                                Sets.of(c)
                        ).setDeletedCells(
                                Sets.of(b1)
                        ).setDeletedColumns(
                                Sets.of(b.reference())
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        this.countAndCheck(
                context.storeRepository().cells(),
                2
        );
    }

    @Test
    public void testInsertColumnsWithRows() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = this.cellReference("A1"); // A1
        final SpreadsheetCellReference b1 = this.cellReference("B1"); // MOVED

        final SpreadsheetRow row2 = b1.row()
                .row();
        engine.saveRow(row2, context);

        final SpreadsheetRow row3 = SpreadsheetSelection.parseRow("2")
                .row();
        engine.saveRow(row3, context);

        engine.saveCell(this.cell(a1, ""), context);
        engine.saveCell(this.cell(b1, ""), context);

        this.insertColumnsAndCheck(
                engine,
                b1.column(),
                1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("C1", "", "")
                                )
                        ).setRows(
                                Sets.of(row2)
                        ).setDeletedCells(
                                Sets.of(b1)
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        this.countAndCheck(
                context.storeRepository().cells(),
                2
        );
    }

    // insertRow....................................................................................................

    @Test
    public void testInsertRowsZero() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference reference = this.cellReference("$A$100"); // A3

        engine.saveCell(this.cell(reference, "=99+0"), context);

        this.addFailingCellSaveWatcherAndDeleteWatcher(context);

        this.insertRowsAndCheck(
                engine,
                reference.row(),
                0,
                context
        );

        this.countAndCheck(context.storeRepository().cells(), 1);

        this.loadCellAndCheckFormulaAndValue(engine,
                reference,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                context,
                "=99+0",
                number(99));
    }

    @Test
    public void testInsertRows() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$A$2"); // MOVED
        final SpreadsheetCellReference c = this.cellReference("$A$3"); // MOVED

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        final int count = 1;
        this.insertRowsAndCheck(
                engine,
                b.row(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$3", "=3+4", number(3 + 4)),
                                        this.formattedCell("$A$4", "=5+6", number(5 + 6))
                                )
                        )
                        .setDeletedCells(
                                Sets.of(
                                        b
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("2,3,4")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 3);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference("$A$1"); // A1
        final SpreadsheetCellReference b = this.cellReference("$A$2"); // MOVED
        final SpreadsheetCellReference c = this.cellReference("$A$3"); // MOVED

        engine.saveCell(this.cell(a, "=1+2"), context);
        engine.saveCell(this.cell(b, "=3+4"), context);
        engine.saveCell(this.cell(c, "=5+6"), context);

        final int count = 1;
        this.insertRowsAndCheck(
                engine,
                b.row(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$3", "=3+4", number(3 + 4)),
                                        this.formattedCell("$A$4", "=5+6", number(5 + 6))
                                )
                        ).setDeletedCells(
                                Sets.of(
                                        b
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("2,3,4")
                        )
        ); // $b insert

        this.countAndCheck(context.storeRepository().cells(), 3);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$D$6", "=2+" + LABEL, number(2 + 100))
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("D")
                        ).setRowHeights(
                                rowHeights("5,6")
                        )
        ); // $b insert

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$1", "=1+" + LABEL, number(1 + 99 + 0)),
                                        this.formattedCell("$A$3", "=2+0", number(2 + 0)),
                                        this.formattedCell("$A$4", "=3+0+" + LABEL, number(3 + 0 + 99)),
                                        this.formattedCell("$I$15", "=99+0", number(99 + 0)) // $b insert
                                )
                        ).setLabels(
                                Sets.of(
                                        SpreadsheetLabelMapping.with(LABEL, SpreadsheetSelection.parseCell("$I$15"))
                                )
                        ).setDeletedCells(
                                Sets.of(b, d)
                        ).setColumnWidths(
                                columnWidths("A,I")
                        ).setRowHeights(
                                rowHeights("1,2,3,4,14,15")
                        )
        );

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference("$A$1"); //
        final SpreadsheetCellReference b = this.cellReference("$F$6"); // moved

        final SpreadsheetCellRange a1 = a.cellRange(a.add(1, 1));
        labelStore.save(SpreadsheetLabelMapping.with(LABEL, a1));

        engine.saveCell(this.cell(a, "=99+0"), context);
        engine.saveCell(this.cell(b, "=2+0+" + LABEL), context);

        final int count = 1;
        this.insertRowsAndCheck(
                engine,
                b.row(),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$F$7", "=2+0+" + LABEL, number(2 + 0 + 99 + 0)) // $b insert
                                )
                        ).setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("F")
                        ).setRowHeights(
                                rowHeights("6,7")
                        )
        );

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repository = context.storeRepository();
        final SpreadsheetCellStore cellStore = repository.cells();
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetCellReference a = this.cellReference(0, 0);
        final SpreadsheetCellReference b = this.cellReference(0, 5);
        final SpreadsheetCellReference c = this.cellReference(0, 10);
        final SpreadsheetCellReference d = this.cellReference(0, 15);
        final SpreadsheetCellReference e = this.cellReference(0, 20);

        labelStore.save(SpreadsheetLabelMapping.with(LABEL, c.cellRange(d)));

        engine.saveCell(this.cell(a, "=1+" + LABEL), context);
        engine.saveCell(this.cell(c, "=99+0"), context);

        this.insertRowsAndCheck(
                engine,
                b.row(),
                c.row().value() - b.row().value(),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+" + LABEL, number(1 + 99 + 0)),
                                        this.formattedCell("$A$16", "=99+0", number(99 + 0))// $b insert
                                )
                        ).setLabels(
                                Sets.of(
                                        LABEL.mapping(
                                                SpreadsheetSelection.parseCellRange("$A$16:$A$21")
                                        )
                                )
                        ).setDeletedCells(
                                Sets.of(c)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1,11,16")
                        )
        );

        this.countAndCheck(labelStore, 1);
        this.loadLabelAndCheck(labelStore, LABEL, d.cellRange(e));

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$1", "=1+0+$I$15", number(3 + 4)),
                                        this.formattedCell("$A$12", "=3+0", number(3 + 0)),
                                        this.formattedCell("$I$15", "=4+0+" + b, number(4 + 0 + 2 + 0))// $c insert
                                )
                        ).setDeletedCells(
                                Sets.of(c, d)
                        ).setColumnWidths(
                                columnWidths("A,I")
                        ).setRowHeights(
                                rowHeights("1,11,12,14,15")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 4);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$1", "=1+0+$I$16", number(1 + 0 + 4 + 0 + 2 + 0)),
                                        this.formattedCell("$A$13", "=3+0", number(3 + 0)),
                                        this.formattedCell("$I$16", "=4+0+" + b, number(4 + 0 + 2 + 0))  // $c insert
                                )
                        ).setDeletedCells(
                                Sets.of(c, d)
                        ).setColumnWidths(
                                columnWidths("A,I")
                        ).setRowHeights(
                                rowHeights("1,11,13,14,16")
                        )
        );

        this.countAndCheck(context.storeRepository().cells(), 4);

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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

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
        this.insertRowsAndCheck(
                engine,
                this.row(7),
                count,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("$A$16", "=2+0", number(2 + 0)),
                                        this.formattedCell("$A$17", "=3+0", number(3 + 0)),
                                        this.formattedCell("$C$18", "=4+0", number(4 + 0)),
                                        this.formattedCell("$D$26", "=5+0", number(5 + 0))
                                )
                        ).setDeletedCells(
                                Sets.of(b, c, d, e)
                        ).setColumnWidths(
                                columnWidths("A,C,D")
                        ).setRowHeights(
                                rowHeights("11,12,13,16,17,18,21,26")
                        )
        ); // $b & $c

        this.countAndCheck(context.storeRepository().cells(), 5);

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

    @Test
    public void testInsertRowsWithColumns() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = this.cellReference("A1"); // A1

        final SpreadsheetColumn a = a1.column().column();
        engine.saveColumn(a, context);

        engine.saveCell(
                this.cell(a1, ""),
                context
        );

        this.insertRowsAndCheck(
                engine,
                a1.row(),
                1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("A2", "", "")
                                )
                        ).setColumns(
                                Sets.of(a)
                        ).setDeletedCells(
                                Sets.of(a1)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        );

        this.countAndCheck(
                context.storeRepository().cells(),
                1
        );
    }

    @Test
    public void testInsertRowsWithRows() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a1 = this.cellReference("A1"); // A1

        final SpreadsheetRow row1 = a1.row()
                .row();
        engine.saveRow(row1, context);

        final SpreadsheetRow row2 = SpreadsheetSelection.parseRow("2")
                .row();
        engine.saveRow(row2, context);

        final SpreadsheetRow row3 = SpreadsheetSelection.parseRow("3")
                .row();
        engine.saveRow(row3, context);

        engine.saveCell(
                this.cell(a1, ""),
                context
        );

        this.insertRowsAndCheck(
                engine,
                a1.row(),
                1,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("A2", "", "")
                                )
                        ).setRows(
                                Sets.of(row2)
                        ).setDeletedCells(
                                Sets.of(a1)
                        ).setDeletedRows(
                                Sets.of(
                                        row1.reference()
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1,2")
                        )
        );

        this.countAndCheck(
                context.storeRepository().cells(),
                1
        );
    }

    // loadCells........................................................................................................

    @Test
    public void testLoadCellsNothing() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        this.loadCellsAndCheck(
                engine,
                "A1:B2",
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context
        );
    }

    @Test
    public void testLoadCellsMultipleRanges() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("a1");
        cellStore.save(
                a1.setFormula(SpreadsheetFormula.EMPTY.setText("=1"))
        );

        cellStore.save(
                SpreadsheetSelection.parseCell("c3")
                        .setFormula(SpreadsheetFormula.EMPTY.setText("=3"))
        );

        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("d4");
        cellStore.save(
                d4.setFormula(SpreadsheetFormula.EMPTY.setText("=4"))
        );

        final Set<SpreadsheetCellRange> cells = SpreadsheetSelection.parseWindow("A1:B2,D4:E5");

        // c3 must not be returned
        this.loadCellsAndCheck(
                engine,
                cells,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.loadCellOrFail(engine, a1, SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, context),
                                        this.loadCellOrFail(engine, d4, SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, context)
                                )
                        ).setColumnWidths(
                                columnWidths("A,D")
                        ).setRowHeights(
                                rowHeights("1,4")
                        ).setWindow(cells)
        );
    }

    @Test
    public void testLoadCellsNothingWithColumns() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetColumnStore columnStore = context.storeRepository()
                .columns();

        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetColumn c = c3.column()
                .column();

        columnStore.save(
                SpreadsheetSelection.parseColumn("a")
                        .column()
        );
        columnStore.save(c);
        columnStore.save(
                SpreadsheetSelection.parseColumn("d")
                        .column()
        );

        final Set<SpreadsheetCellRange> range = SpreadsheetSelection.parseWindow("B2:C3");

        this.loadCellsAndCheck(
                engine,
                range,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(SpreadsheetDelta.NO_CELLS)
                        .setWindow(range)
                        .setColumns(
                                Sets.of(c)
                        )
        );
    }

    @Test
    public void testLoadCellsNothingWithLabels() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetLabelName label = SpreadsheetLabelName.labelName("LabelC3");

        labelStore.save(label.mapping(b2));

        final Set<SpreadsheetCellRange> range = SpreadsheetSelection.parseWindow("B2:C3");

        this.loadCellsAndCheck(
                engine,
                range,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(SpreadsheetDelta.NO_CELLS)
                        .setWindow(range)
                        .setLabels(
                                Sets.of(
                                        label.mapping(b2)
                                )
                        )
        );
    }

    @Test
    public void testLoadCellsNothingWithRows() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetRowStore rowStore = context.storeRepository()
                .rows();

        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetRow c = c3.row()
                .row();

        rowStore.save(
                SpreadsheetSelection.parseRow("1")
                        .row()
        );
        rowStore.save(c);
        rowStore.save(
                SpreadsheetSelection.parseRow("4")
                        .row()
        );

        final Set<SpreadsheetCellRange> range = SpreadsheetSelection.parseWindow("B2:C3");

        this.loadCellsAndCheck(
                engine,
                range,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(SpreadsheetDelta.NO_CELLS)
                        .setWindow(range)
                        .setRows(
                                Sets.of(c)
                        )
        );
    }

    @Test
    public void testLoadCellsWithDivideByZero() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCell z9 = this.cell("z9", "=0/0");
        cellStore.save(z9);

        final Set<SpreadsheetCellRange> window = SpreadsheetSelection.parseWindow("z9");

        this.loadCellsAndCheck(
                engine,
                window,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                z9,
                                                SpreadsheetErrorKind.DIV0.setMessage("Division by zero")
                                        )
                                )
                        ).setColumnWidths(
                                columnWidths("Z")
                        ).setRowHeights(
                                rowHeights("9")
                        ).setWindow(window)
        );
    }

    @Test
    public void testLoadCells() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCell b2 = this.cell("b2", "=2");
        cellStore.save(b2);

        final SpreadsheetCell c3 = this.cell("c3", "=3");
        cellStore.save(c3);

        final Set<SpreadsheetCellRange> cells = SpreadsheetSelection.parseWindow("b2:c3");

        this.loadCellsAndCheck(
                engine,
                cells,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(b2, this.expressionNumberKind().create(2)),
                                        this.formattedCell(c3, this.expressionNumberKind().create(3))
                                )
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("2,3")
                        ).setWindow(cells)
        );
    }

    @Test
    public void testLoadCellsWithCellReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCell b2 = this.cell("b2", "=c3*2");
        cellStore.save(b2);

        final SpreadsheetCell c3 = this.cell("c3", "=2");
        cellStore.save(c3);

        final Set<SpreadsheetCellRange> cells = SpreadsheetSelection.parseWindow("b2:c3");

        this.loadCellsAndCheck(
                engine,
                cells,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(b2, this.expressionNumberKind().create(4)),
                                        this.formattedCell(c3, this.expressionNumberKind().create(2))
                                )
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("2,3")
                        ).setWindow(cells)
        );
    }

    @Test
    public void testLoadCellsWithReferencesToReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCell a1 = this.cell("a1", "=b2+100");
        cellStore.save(a1);

        final SpreadsheetCell b2 = this.cell("b2", "=c3+10");
        cellStore.save(b2);

        final SpreadsheetCell c3 = this.cell("c3", "=1");
        cellStore.save(c3);

        final Set<SpreadsheetCellRange> window = SpreadsheetSelection.parseWindow("a1:c3");

        this.loadCellsAndCheck(
                engine,
                window,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a1, this.expressionNumberKind().create(1 + 10 + 100)),
                                        this.formattedCell(b2, this.expressionNumberKind().create(1 + 10)),
                                        this.formattedCell(c3, this.expressionNumberKind().one())
                                )
                        ).setColumnWidths(
                                columnWidths("A,B,C")
                        ).setRowHeights(
                                rowHeights("1,2,3")
                        ).setWindow(window)
        );
    }

    @Test
    public void testLoadCellsWithReferencesToReferences2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCell a1 = this.cell("a1", "=c3+100");
        cellStore.save(a1);

        final SpreadsheetCell b2 = this.cell("b2", "=a1+10");
        cellStore.save(b2);

        final SpreadsheetCell c3 = this.cell("c3", "=1");
        cellStore.save(c3);

        final Set<SpreadsheetCellRange> window = SpreadsheetSelection.parseWindow("a1:c3");

        this.loadCellsAndCheck(
                engine,
                window,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a1, this.expressionNumberKind().create(1 + 100)),
                                        this.formattedCell(b2, this.expressionNumberKind().create(1 + 100 + 10)),
                                        this.formattedCell(c3, this.expressionNumberKind().one())
                                )
                        ).setColumnWidths(
                                columnWidths("A,B,C")
                        ).setRowHeights(
                                rowHeights("1,2,3")
                        ).setWindow(window)
        );
    }

    @Test
    public void testLoadCellsWithLabels() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCell c3 = this.cell("c3", "=1");
        cellStore.save(c3);

        final SpreadsheetCell d4 = this.cell("D4", "=2");
        cellStore.save(d4);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetLabelName label = SpreadsheetLabelName.labelName("LabelD4");

        labelStore.save(label.mapping(d4.reference()));

        final Set<SpreadsheetCellRange> range = SpreadsheetSelection.parseWindow("c3:d4");

        this.loadCellsAndCheck(
                engine,
                range,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(c3, this.expressionNumberKind().one()),
                                        this.formattedCell(d4, this.expressionNumberKind().create(2))
                                )
                        )
                        .setWindow(range)
                        .setLabels(
                                Sets.of(
                                        label.mapping(d4.reference())
                                )
                        ).setColumnWidths(
                                columnWidths("C,D")
                        ).setRowHeights(
                                rowHeights("3,4")
                        )
        );
    }

    @Test
    public void testLoadCellsOnlyLabelsToCell() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetLabelName label = SpreadsheetLabelName.labelName("LabelC3");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("c3");
        final SpreadsheetLabelMapping mapping = labelStore.save(label.mapping(c3));

        final Set<SpreadsheetCellRange> range = SpreadsheetSelection.parseWindow("b2:d4");

        this.loadCellsAndCheck(
                engine,
                range,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setWindow(range)
                        .setLabels(
                                Sets.of(mapping)
                        )
        );
    }

    @Test
    public void testLoadCellsOnlyLabelsToRange() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetLabelName labelC3d4 = SpreadsheetLabelName.labelName("LabelC3d4");
        final SpreadsheetCellRange c3d4 = SpreadsheetSelection.parseCellRange("c3:d4");
        final SpreadsheetLabelMapping mappingC3d4 = labelStore.save(labelC3d4.mapping(c3d4));

        final Set<SpreadsheetCellRange> range = SpreadsheetSelection.parseWindow("b2:e5");

        this.loadCellsAndCheck(
                engine,
                range,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setWindow(range)
                        .setLabels(
                                Sets.of(mappingC3d4)
                        )
        );
    }

    @Test
    public void testLoadCellsOnlyLabelsToCellAndRange() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetLabelName labelC3 = SpreadsheetLabelName.labelName("LabelC3");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("c3");
        final SpreadsheetLabelMapping mappingC3 = labelStore.save(labelC3.mapping(c3));

        final SpreadsheetLabelName labelC3d4 = SpreadsheetLabelName.labelName("LabelC3d4");
        final SpreadsheetCellRange c3d4 = SpreadsheetSelection.parseCellRange("c3:d4");
        final SpreadsheetLabelMapping mappingC3d4 = labelStore.save(labelC3d4.mapping(c3d4));

        final Set<SpreadsheetCellRange> range = SpreadsheetSelection.parseWindow("b2:e5");

        this.loadCellsAndCheck(
                engine,
                range,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setWindow(range)
                        .setLabels(
                                Sets.of(
                                        mappingC3,
                                        mappingC3d4
                                )
                        )
        );
    }

    @Test
    public void testLoadCellsWithLabelsLabelWithoutCell() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCell c3 = this.cell("c3", "=1");
        cellStore.save(c3);

        final SpreadsheetLabelStore labelStore = context.storeRepository()
                .labels();

        final SpreadsheetLabelName label = SpreadsheetLabelName.labelName("LabelD4");
        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("d4");
        labelStore.save(label.mapping(d4));

        final Set<SpreadsheetCellRange> range = SpreadsheetSelection.parseWindow("c3:d4");

        this.loadCellsAndCheck(
                engine,
                range,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(c3, this.expressionNumberKind().one())
                                )
                        )
                        .setColumnWidths(
                                columnWidths("C")
                        ).setRowHeights(
                                rowHeights("3")
                        )
                        .setWindow(range)
                        .setLabels(
                                Sets.of(
                                        label.mapping(d4)
                                )
                        )
        );
    }

    @Test
    public void testLoadCellsFiltersHiddenColumns() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        // this must not appear in the loaded result because column:B is hidden.
        final SpreadsheetCell b2 = this.cell("b2", "=2");
        cellStore.save(b2);

        final SpreadsheetCell c3 = this.cell("c3", "=3");
        cellStore.save(c3);

        final SpreadsheetCell d4 = this.cell("d4", "=4");
        cellStore.save(d4);

        final SpreadsheetCellRange range = SpreadsheetSelection.parseCellRange("a1:c3");

        final SpreadsheetColumn bHidden = b2.reference()
                .column()
                .column()
                .setHidden(true);
        engine.saveColumn(
                bHidden,
                context
        );

        final SpreadsheetColumn c = c3.reference()
                .column()
                .column()
                .setHidden(false);
        engine.saveColumn(
                c,
                context
        );

        this.loadCellsAndCheck(
                engine,
                Sets.of(range),
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                c3,
                                                this.expressionNumberKind().create(3)
                                        )
                                )
                        ).setColumns(
                                Sets.of(
                                        bHidden,
                                        c
                                )
                        ).setColumnWidths(
                                columnWidths("C")
                        ).setRowHeights(
                                rowHeights("2,3")
                        ).setWindow(
                                Sets.of(range)
                        )
        );
    }

    @Test
    public void testLoadCellsFiltersHiddenRows() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        // this must not appear in the loaded result because row:2 is hidden.
        final SpreadsheetCell b2 = this.cell("b2", "=2");
        cellStore.save(b2);

        final SpreadsheetCell c3 = this.cell("c3", "=3");
        cellStore.save(c3);

        final SpreadsheetCell d4 = this.cell("d4", "=4");
        cellStore.save(d4);

        final Set<SpreadsheetCellRange> range = SpreadsheetSelection.parseWindow("a1:c3");

        final SpreadsheetRow row2Hidden = b2.reference()
                .row()
                .row()
                .setHidden(true);
        engine.saveRow(
                row2Hidden,
                context
        );

        final SpreadsheetRow row3 = c3.reference()
                .row()
                .row()
                .setHidden(false);
        engine.saveRow(
                row3,
                context
        );

        this.loadCellsAndCheck(
                engine,
                range,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                c3,
                                                this.expressionNumberKind().create(3)
                                        )
                                )
                        ).setRows(
                                Sets.of(
                                        row2Hidden,
                                        row3
                                )
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("3")
                        ).setWindow(range)
        );
    }

    // fillCells........................................................................................................

    // fill deletes.....................................................................................................

    @Test
    public void testFillCellsDeleteOneCell() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference a = this.cellReference(5, 5);
        final SpreadsheetCell cellA = this.cell(a, "=1+0");

        cellStore.save(cellA);

        final SpreadsheetCellRange rangeA = a.cellRange(a);

        this.fillCellsAndCheck(
                engine,
                SpreadsheetDelta.NO_CELLS,
                rangeA,
                rangeA,
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(a)
                        ).setColumnWidths(
                                columnWidths("F")
                        ).setRowHeights(
                                rowHeights("6")
                        )
        );

        this.countAndCheck(cellStore, 0); // a deleted
    }

    @Test
    public void testFillCellsDeleteOneCell2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference a = this.cellReference(5, 5);
        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        cellStore.save(cellA);

        final SpreadsheetCellReference b = this.cellReference(10, 10);
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        cellStore.save(cellB);

        final SpreadsheetCellRange rangeA = a.cellRange(a);

        this.fillCellsAndCheck(
                engine,
                SpreadsheetDelta.NO_CELLS,
                rangeA,
                rangeA,
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(a)
                        ).setColumnWidths(
                                columnWidths("F")
                        ).setRowHeights(
                                rowHeights("6")
                        )
        );

        this.countAndCheck(cellStore, 1); // a deleted

        this.loadCellAndCheck(
                engine,
                b,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(b, "=2+0", number(2))
                                )
                        ).setColumnWidths(
                                columnWidths("K")
                        ).setRowHeights(
                                rowHeights("11")
                        )
        );
    }

    @Test
    public void testFillCellsDeletesManyCells() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference a = this.cellReference(5, 5);
        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        cellStore.save(cellA);

        final SpreadsheetCellReference b = this.cellReference(6, 6);
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        cellStore.save(cellB);

        final SpreadsheetCellRange rangeAtoB = a.cellRange(b);

        this.fillCellsAndCheck(
                engine,
                SpreadsheetDelta.NO_CELLS,
                rangeAtoB,
                rangeAtoB,
                context,
                SpreadsheetDelta.EMPTY.setDeletedCells(
                                Sets.of(a, b)
                        ).setColumnWidths(
                                columnWidths("F,G")
                        ).setRowHeights(
                                rowHeights("6,7")
                        )
        );

        this.countAndCheck(cellStore, 0); // a deleted
    }

    @Test
    public void testFillCellsDeletesManyCells2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference a = this.cellReference(5, 5);
        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        cellStore.save(cellA);

        final SpreadsheetCellReference b = this.cellReference(6, 6);
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        cellStore.save(cellB);

        final SpreadsheetCellRange rangeAtoB = a.cellRange(b);

        final SpreadsheetCellReference c = this.cellReference(10, 10);
        final SpreadsheetCell cellC = this.cell(c, "=3+0");
        cellStore.save(cellC);

        this.fillCellsAndCheck(
                engine,
                SpreadsheetDelta.NO_CELLS,
                rangeAtoB,
                rangeAtoB,
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(a, b)
                        ).setColumnWidths(
                                columnWidths("F,G")
                        ).setRowHeights(
                                rowHeights("6,7")
                        )
        );

        this.countAndCheck(cellStore, 1); // a&b deleted, leaving c

        this.loadCellAndCheck(
                engine,
                c,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(c, "=3+0", number(3))
                                )
                        ).setColumnWidths(
                                columnWidths("K")
                        ).setRowHeights(
                                rowHeights("11")
                        )
        );
    }

    // fill save with missing cells......................................................................................

    @Test
    public void testFillCellsSaveWithMissingCells() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 2);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");

        final SpreadsheetCellRange range = a.cellRange(b);

        this.fillCellsAndCheck(
                engine,
                Sets.of(cellA, cellB),
                range,
                range,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+0", number(1)),
                                        this.formattedCell(b, "=2+0", number(2))
                                )
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("2,3")
                        )
               );

        this.countAndCheck(context.storeRepository().cells(), 2); // a + b saved

        this.loadCellAndCheck(
                engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+0", number(1))
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );

        this.loadCellAndCheck(
                engine,
                b,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(b, "=2+0", number(2))
                                )
                        ).setColumnWidths(
                                columnWidths("C")
                        ).setRowHeights(
                                rowHeights("3")
                        )
        );
    }

    @Test
    public void testFillCellsSaveWithMissingCells2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 2);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");

        final SpreadsheetCellRange range = a.cellRange(b);

        final SpreadsheetCellReference c = this.cellReference(10, 10);
        final SpreadsheetCell cellC = this.cell(c, "=3+0");
        cellStore.save(cellC);

        this.fillCellsAndCheck(
                engine,
                Sets.of(cellA, cellB),
                range,
                range,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+0", number(1)),
                                        this.formattedCell(b, "=2+0", number(2))
                                )
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("2,3")
                        )
                );

        this.countAndCheck(cellStore, 3); // a + b saved + c

        this.loadCellAndCheck(
                engine,
                a,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+0", number(1))
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        ); // fill should have evaluated.

        this.loadCellAndCheck(
                engine,
                b,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(b, "=2+0", number(2))
                                )
                        ).setColumnWidths(
                                columnWidths("C")
                        ).setRowHeights(
                                rowHeights("3")
                        )
        );

        this.loadCellAndCheck(
                engine,
                c,
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        cellC
                                )
                        ).setColumnWidths(
                                columnWidths("K")
                        ).setRowHeights(
                                rowHeights("11")
                        )
        );
    }

    // fill moves cell..................................................................................................

    @Test
    public void testFillCellsRangeOneEmptyCells2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 1);
        final SpreadsheetCellReference c = this.cellReference(1, 2);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        final SpreadsheetCell cellC = this.cell(c, "=3+0");

        cellStore.save(cellA);
        cellStore.save(cellB);
        cellStore.save(cellC);

        this.fillCellsAndCheck(
                engine,
                SpreadsheetDelta.NO_CELLS,
                a.cellRange(a),
                SpreadsheetCellRange.fromCells(Lists.of(b)),
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(b)
                        ).setColumnWidths(
                                columnWidths("C")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );

        this.countAndCheck(cellStore, 2); // a + c, b deleted

        this.loadCellAndCheck(
                engine,
                a,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+0", number(1))
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );

        this.loadCellAndCheck(
                engine,
                c,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(c, "=3+0", number(3))
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("3")
                        )
        );
    }

    @Test
    public void testFillCellsRangeTwoEmptyCells() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference a = this.cellReference(1, 1);
        final SpreadsheetCellReference b = this.cellReference(2, 1);
        final SpreadsheetCellReference c = this.cellReference(1, 2);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=2+0");
        final SpreadsheetCell cellC = this.cell(c, "=3+0");

        cellStore.save(cellA);
        cellStore.save(cellB);
        cellStore.save(cellC);

        this.fillCellsAndCheck(
                engine,
                SpreadsheetDelta.NO_CELLS,
                a.cellRange(a),
                SpreadsheetCellRange.fromCells(Lists.of(a, b)),
                context,
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(a, b)
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("2")
                        )
        );

        this.countAndCheck(cellStore, 1);

        this.loadCellAndCheck(
                engine,
                c,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(c, "=3+0", number(3))
                                )
                        ).setColumnWidths(
                                columnWidths("B")
                        ).setRowHeights(
                                rowHeights("3")
                        )
        );
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
        this.fillCellsAndCheck("=10=10", true);
    }

    @Test
    public void testFillCellsEqualsFalse() {
        this.fillCellsAndCheck("=10=9", false);
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
        this.fillCellsAndCheck("=10<>9", true);
    }

    @Test
    public void testFillCellsNotEqualsFalse() {
        this.fillCellsAndCheck("=10<>10", false);
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
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

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

        this.fillCellsAndCheck(
                engine,
                Lists.of(cellA),
                a.cellRange(a),
                d.cellRange(d),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(d, formulaText, expected)
                                )
                        ).setColumnWidths(
                                columnWidths("AE")
                        ).setRowHeights(
                                rowHeights("41")
                        )
        );

        this.countAndCheck(cellStore, 3 + 1);
    }

    @Test
    public void testFillCellsRepeatCellInto2x2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

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
                SpreadsheetCellRange.fromCells(Lists.of(a, b)),
                d.cellRange(d.add(2, 2)),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(d, "=1+0", number(1 + 0)),
                                        this.formattedCell(d.add(1, 1), "=2+0", number(2 + 0))
                                )
                        ).setColumnWidths(
                                columnWidths("AE,AF")
                        ).setRowHeights(
                                rowHeights("41,42")
                        )
        );

        this.countAndCheck(cellStore, 3 + 2);
    }

    @Test
    public void testFillCells2x2CellInto1x1() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

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
                SpreadsheetCellRange.fromCells(Lists.of(a, b)),
                d.cellRange(d.add(1, 1)),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(d, "=1+0", number(1 + 0)),
                                        this.formattedCell(d.add(1, 1), "=2+0", number(2 + 0))
                                )
                        ).setColumnWidths(
                                columnWidths("AE,AF")
                        ).setRowHeights(
                                rowHeights("41,42")
                        )
        );

        this.countAndCheck(cellStore, 3 + 2);
    }

    @Test
    public void testFillCells2x2Into2x2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

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
                SpreadsheetCellRange.fromCells(Lists.of(a, b)),
                d.cellRange(d.add(2, 2)),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(d, "=1+0", number(1 + 0)),
                                        this.formattedCell(d.add(1, 1), "=2+0", number(2 + 0))
                                )
                        ).setColumnWidths(
                                columnWidths("AE,AF")
                        ).setRowHeights(
                                rowHeights("41,42")
                        )
        );

        this.countAndCheck(cellStore, 3 + 2);
    }

    @Test
    public void testFillCells2x2Into7x2Gives6x2() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

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
                SpreadsheetCellRange.fromCells(Lists.of(a, b)),
                d.cellRange(d.add(6, 1)),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(d, "=1+0", number(1 + 0)),
                                        this.formattedCell(d.add(1, 1), "=2+0", number(2 + 0)),
                                        this.formattedCell(d.add(2, 0), "=1+0", number(1 + 0)),
                                        this.formattedCell(d.add(3, 1), "=2+0", number(2 + 0)),
                                        this.formattedCell(d.add(4, 0), "=1+0", number(1 + 0)),
                                        this.formattedCell(d.add(5, 1), "=2+0", number(2 + 0))
                                )
                        ).setColumnWidths(
                                columnWidths("AE,AF,AG,AH,AI,AJ")
                        ).setRowHeights(
                                rowHeights("41,42")
                        )
        );

        this.countAndCheck(cellStore, 3 + 6);
    }

    @Test
    public void testFillCells2x2Into2x7Gives2x6() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

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
                SpreadsheetCellRange.fromCells(Lists.of(a, b)),
                d.cellRange(d.add(1, 6)),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(d, "=1+0", number(1 + 0)),
                                        this.formattedCell(d.add(1, 1), "=2+0", number(2 + 0)),
                                        this.formattedCell(d.addRow(2), "=1+0", number(1 + 0)),
                                        this.formattedCell(d.add(1, 3), "=2+0", number(2 + 0)),
                                        this.formattedCell(d.addRow(4), "=1+0", number(1 + 0)),
                                        this.formattedCell(d.add(1, 5), "=2+0", number(2 + 0))
                                )
                        ).setColumnWidths(
                                columnWidths("AE,AF")
                        ).setRowHeights(
                                rowHeights("41,42,43,44,45,46")
                        )
        );

        this.countAndCheck(cellStore, 3 + 6);
    }

    @Test
    public void testFillCellsAbsoluteCellReference() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference a = this.cellReference(10, 20);
        final SpreadsheetCellReference b = this.cellReference(11, 21);

        final SpreadsheetCell cellA = this.cell(a, "=1+0");
        final SpreadsheetCell cellB = this.cell(b, "=" + a);

        cellStore.save(cellA);
        cellStore.save(cellB);

        final SpreadsheetCellReference d = this.cellReference(30, 40);

        this.fillCellsAndCheck(
                engine,
                Lists.of(cellB),
                b.cellRange(b),
                d.cellRange(d),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(a, "=1+0", number(1 + 0)),
                                        this.formattedCell(d, "=" + a, number(1 + 0))
                                )
                        ).setColumnWidths(
                                columnWidths("K,AE")
                        ).setRowHeights(
                                rowHeights("21,41")
                        )
                );

        this.countAndCheck(cellStore, 2 + 1);
    }

    @Test
    public void testFillCellsExpressionRelativeCellReferenceFixed() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCell cellB = this.cell("B2", "=2");
        final SpreadsheetCell cellC = this.cell("C3", "=3+B2");

        cellStore.save(cellB);
        cellStore.save(cellC);

        this.fillCellsAndCheck(
                engine,
                Lists.of(cellB, cellC),
                cellB.reference().cellRange(cellC.reference()),
                SpreadsheetSelection.parseCellRange("E5:F6"),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("E5", "=2", number(2 + 0)),
                                        this.formattedCell("F6", "=3+E5", number(3 + 2))
                                )
                        ).setColumnWidths(
                                columnWidths("E,F")
                        ).setRowHeights(
                                rowHeights("5,6")
                        )
        );

        this.countAndCheck(cellStore, 2 + 2);
    }

    @Test
    public void testFillCellsExternalCellReferencesRefreshed() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        final SpreadsheetCellReference b = this.cellReference("b1");
        final SpreadsheetCell cellB = this.cell(b, "=2+0"); // copied to C1
        final SpreadsheetCellReference c = this.cellReference("C1"); // fillCells dest...
        final SpreadsheetCell cellA = this.cell("a1", "=10+" + c);

        engine.saveCell(cellA, context);
        engine.saveCell(cellB, context);

        this.fillCellsAndCheck(
                engine,
                Lists.of(cellB),
                b.cellRange(b),
                c.cellRange(c),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(cellA.reference(), "=10+" + c, number(10 + 2 + 0)), // external reference to copied
                                        this.formattedCell(c, "=2+0", number(2 + 0))
                                )
                        ).setColumnWidths(
                                columnWidths("A,C")
                        ).setRowHeights(
                                rowHeights("1")
                        )
                ); // copied

        this.countAndCheck(cellStore, 2 + 1);
    }

    @Test
    public void testFillCellsWithColumns() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repo = context.storeRepository();

        final SpreadsheetCellReference b2 = this.cellReference("b2");
        final SpreadsheetCell cellB2 = this.cell(b2, "");

        final SpreadsheetColumnStore columnStore = repo.columns();
        final SpreadsheetColumn b = b2.column()
                .column();
        columnStore.save(b);

        final SpreadsheetColumn c = SpreadsheetSelection.parseColumn("c")
                .column();
        columnStore.save(c);

        final SpreadsheetCellStore cellStore = repo.cells();

        engine.saveCell(cellB2, context);

        final SpreadsheetCellReference c3 = this.cellReference("c3");

        this.fillCellsAndCheck(
                engine,
                Lists.of(cellB2),
                b2.cellRange(b2),
                c3.cellRange(c3),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                this.cell(c3, ""),
                                                ""
                                        )
                                )
                        )
                        .setColumns(
                                Sets.of(c)
                        ).setColumnWidths(
                                columnWidths("C")
                        ).setRowHeights(
                                rowHeights("3")
                        )
        ); // copied

        this.countAndCheck(cellStore, 1 + 1);
    }

    @Test
    public void testFillCellsWithRows() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetStoreRepository repo = context.storeRepository();

        final SpreadsheetCellReference b2 = this.cellReference("b2");
        final SpreadsheetCell cellB2 = this.cell(b2, "");

        final SpreadsheetRowStore rowStore = repo.rows();
        final SpreadsheetRow row2 = b2.row()
                .row();
        rowStore.save(row2);

        final SpreadsheetRow row3 = SpreadsheetSelection.parseRow("3")
                .row();
        rowStore.save(row3);

        final SpreadsheetCellStore cellStore = repo.cells();

        engine.saveCell(cellB2, context);

        final SpreadsheetCellReference c3 = this.cellReference("c3");

        this.fillCellsAndCheck(
                engine,
                Lists.of(cellB2),
                b2.cellRange(b2),
                c3.cellRange(c3),
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                this.cell(c3, ""),
                                                ""
                                        )
                                )
                        )
                        .setRows(
                                Sets.of(row3)
                        ).setColumnWidths(
                                columnWidths("C")
                        ).setRowHeights(
                                rowHeights("3")
                        )
        ); // copied

        this.countAndCheck(cellStore, 1 + 1);
    }

    //  loadLabel.......................................................................................................

    @Test
    public void testLoadLabelUnknownFails() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetSelection.parseCell("B2"));

        this.saveLabelAndCheck(engine,
                mapping,
                context);

        this.loadLabelAndFailCheck(
                engine,
                SpreadsheetSelection.labelName("UnknownLabel"),
                this.createContext()
        );
    }

    //  saveLabel.......................................................................................................

    @Test
    public void testSaveLabelAndLoadFromLabelStore() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetSelection.parseCell("B2"));

        this.saveLabelAndCheck(engine,
                mapping,
                context);

        this.loadLabelAndCheck(context.storeRepository().labels(),
                label,
                mapping);
    }

    @Test
    public void testSaveLabelAndLoadLabel() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetSelection.parseCell("B2"));

        this.saveLabelAndCheck(engine,
                mapping,
                context);

        this.loadLabelAndCheck(engine,
                label,
                context,
                mapping);
    }

    @Test
    public void testSaveLabelWithoutCellReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetSelection.parseCell("B2"));

        engine.saveCell(this.cell("B2", "=99"), context);

        this.saveLabelAndCheck(engine,
                mapping,
                context);

        engine.saveCell(this.cell("A1", label + "+1"), context);
    }

    @Test
    public void testSaveLabelRefreshesCellReferences() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetSelection.parseCell("B2"));

        engine.saveCell(this.cell("A1", "=" + label + "+1"), context);
        engine.saveCell(this.cell("B2", "=99"), context);

        this.saveLabelAndCheck(
                engine,
                mapping,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("A1", "=" + label + "+1", number(99 + 1))
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
                );
    }

    @Test
    public void testSaveLabelRefreshesCellReferencesAndColumns() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetSelection.parseCell("B2"));

        final SpreadsheetColumn a = SpreadsheetSelection.parseColumn("a")
                .column();
        engine.saveColumn(a, context);

        final SpreadsheetColumn b = SpreadsheetSelection.parseColumn("b")
                .column();
        engine.saveColumn(b, context);

        final SpreadsheetColumn c = SpreadsheetSelection.parseColumn("c")
                .column();
        engine.saveColumn(c, context);

        engine.saveCell(this.cell("A1", "=" + label + "+1"), context);
        engine.saveCell(this.cell("B2", "=99"), context);

        this.saveLabelAndCheck(
                engine,
                mapping,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("A1", "=" + label + "+1", number(99 + 1))
                                )
                        ).setColumns(
                                Sets.of(a)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );
    }

    @Test
    public void testSaveLabelRefreshesCellReferencesAndRows() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetSelection.parseCell("B2"));

        final SpreadsheetRow row1 = SpreadsheetSelection.parseRow("1")
                .row();
        engine.saveRow(row1, context);

        final SpreadsheetRow row2 = SpreadsheetSelection.parseRow("2")
                .row();
        engine.saveRow(row2, context);

        final SpreadsheetRow row3 = SpreadsheetSelection.parseRow("3")
                .row();
        engine.saveRow(row3, context);

        engine.saveCell(this.cell("A1", "=" + label + "+1"), context);
        engine.saveCell(this.cell("B2", "=99"), context);

        this.saveLabelAndCheck(
                engine,
                mapping,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell("A1", "=" + label + "+1", number(99 + 1))
                                )
                        ).setRows(
                                Sets.of(row1)
                        ).setColumnWidths(
                                COLUMN_A_WIDTH
                        ).setRowHeights(
                                ROW_1_HEIGHT
                        )
        );
    }

    //  removeLabel.......................................................................................................

    @Test
    public void testRemoveLabelAndLoadFromLabelStore() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetSelection.parseCell("B2"));

        this.saveLabelAndCheck(engine,
                mapping,
                context);

        this.removeLabelAndCheck(engine,
                label,
                context);

        this.loadLabelFailCheck(context.storeRepository().labels(), label);
    }

    @Test
    public void testRemoveLabelRefreshesCell() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(label, SpreadsheetSelection.parseCell("B2"));

        engine.saveCell(this.cell("A1", "=" + label + "+1"), context);
        engine.saveCell(this.cell("B2", "=99"), context);

        engine.saveLabel(mapping, context);

        this.removeLabelAndCheck(
                engine,
                label,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                "A1",
                                                "=" + label + "+1",
                                                SpreadsheetError.selectionNotFound(label)
                                        )
                                )
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        this.loadLabelFailCheck(context.storeRepository().labels(), label);
    }

    @Test
    public void testRemoveLabelRefreshesCellAndColumns() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(
                label,
                SpreadsheetSelection.parseCell("B2")
        );

        final SpreadsheetColumn a = SpreadsheetSelection.parseColumn("A")
                .column();
        engine.saveColumn(a, context);

        final SpreadsheetColumn b = SpreadsheetSelection.parseColumn("B")
                .column();
        engine.saveColumn(b, context);

        engine.saveCell(
                this.cell("A1", "=" + label + "+1"),
                context
        );
        engine.saveCell(
                this.cell("B2", "=99"),
                context
        );

        engine.saveLabel(mapping, context);

        this.removeLabelAndCheck(
                engine,
                label,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                "A1",
                                                "=" + label + "+1",
                                                SpreadsheetError.selectionNotFound(label)
                                        )
                                )
                        ).setColumns(
                                Sets.of(a)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        this.loadLabelFailCheck(
                context.storeRepository().labels(),
                label
        );
    }

    @Test
    public void testRemoveLabelRefreshesCellAndRows() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext(engine);

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("LABEL123");
        final SpreadsheetLabelMapping mapping = SpreadsheetLabelMapping.with(
                label,
                SpreadsheetSelection.parseCell("B2")
        );

        final SpreadsheetRow row1 = SpreadsheetSelection.parseRow("1")
                .row();
        engine.saveRow(row1, context);

        final SpreadsheetRow row2 = SpreadsheetSelection.parseRow("2")
                .row();
        engine.saveRow(row2, context);

        engine.saveCell(
                this.cell("A1", "=" + label + "+1"),
                context
        );
        engine.saveCell(
                this.cell("B2", "=99"),
                context
        );

        engine.saveLabel(mapping, context);

        this.removeLabelAndCheck(
                engine,
                label,
                context,
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        this.formattedCell(
                                                "A1",
                                                "=" + label + "+1",
                                                SpreadsheetError.selectionNotFound(label)
                                        )
                                )
                        ).setRows(
                                Sets.of(row1)
                        ).setColumnWidths(
                                columnWidths("A")
                        ).setRowHeights(
                                rowHeights("1")
                        )
        );

        this.loadLabelFailCheck(
                context.storeRepository().labels(),
                label
        );
    }

    // columnWidth, rowHeight...........................................................................................

    @Test
    public void testColumnWidth() {
        final SpreadsheetColumnReference column = SpreadsheetSelection.parseColumn("Z");
        final double expected = 150.5;

        this.columnWidthAndCheck2(
                column,
                this.metadata(),
                expected,
                expected);
    }

    @Test
    public void testColumnWidthDefaults() {
        final SpreadsheetColumnReference column = SpreadsheetSelection.parseColumn("Z");
        final double expected = 150.5;

        SpreadsheetMetadata metadata = SpreadsheetMetadata.NON_LOCALE_DEFAULTS;
        final TextStyle style = metadata.getOrFail(SpreadsheetMetadataPropertyName.STYLE)
                .set(TextStylePropertyName.WIDTH, Length.pixel(expected));
        metadata = metadata.set(SpreadsheetMetadataPropertyName.STYLE, style);

        this.columnWidthAndCheck2(
                column,
                metadata,
                0,
                expected);
    }

    private void columnWidthAndCheck2(final SpreadsheetColumnReference column,
                                      final SpreadsheetMetadata metadata,
                                      final double maxColumnWidth,
                                      final double expected) {
        this.columnWidthAndCheck(
                this.createSpreadsheetEngine(),
                column,
                this.createContext(
                        metadata,
                        new FakeSpreadsheetCellStore() {
                            @Override
                            public double maxColumnWidth(final SpreadsheetColumnReference c) {
                                checkEquals(column, c);
                                return maxColumnWidth;
                            }
                        }),
                expected
        );
    }

    @Test
    public void testColumnWidthDefaultMissing() {
        final SpreadsheetColumnReference column = SpreadsheetSelection.parseColumn("Z");
        assertThrows(
                TextStylePropertyValueException.class,
                () -> this.createSpreadsheetEngine()
                        .columnWidth(
                                column,
                                this.createContext(
                                        new FakeSpreadsheetCellStore() {
                                            @Override
                                            public double maxColumnWidth(final SpreadsheetColumnReference c) {
                                                checkEquals(column, c);
                                                return 0;
                                            }
                                        }
                                )
                        )
        );
    }

    // rowHeight........................................................................................................

    @Test
    public void testRowHeight() {
        this.rowHeightAndCheck2(
                SpreadsheetSelection.parseRow("987"),
                SpreadsheetMetadata.EMPTY,
                150.5
        );
    }

    @Test
    public void testRowHeightDefaults() {
        final SpreadsheetRowReference row = SpreadsheetSelection.parseRow("987");
        final double expected = 150.5;

        final SpreadsheetMetadata metadata = SpreadsheetMetadata.NON_LOCALE_DEFAULTS;
        final TextStyle style = metadata.getOrFail(SpreadsheetMetadataPropertyName.STYLE)
                .set(TextStylePropertyName.HEIGHT, Length.pixel(expected));

        this.rowHeightAndCheck2(
                row,
                metadata.set(SpreadsheetMetadataPropertyName.STYLE, style),
                expected
        );
    }

    private void rowHeightAndCheck2(final SpreadsheetRowReference row,
                                    final SpreadsheetMetadata metadata,
                                    final double expected) {
        this.rowHeightAndCheck(
                this.createSpreadsheetEngine(),
                row,
                this.createContext(
                        metadata,
                        new FakeSpreadsheetCellStore() {
                            @Override
                            public double maxRowHeight(final SpreadsheetRowReference c) {
                                checkEquals(row, c);
                                return expected;
                            }
                        }),
                expected
        );
    }

    @Test
    public void testRowHeightDefaultMissing() {
        final SpreadsheetRowReference row = SpreadsheetSelection.parseRow("999");
        assertThrows(
                TextStylePropertyValueException.class,
                () -> this.createSpreadsheetEngine()
                        .rowHeight(
                                row,
                                this.createContext(
                                        new FakeSpreadsheetCellStore() {

                                            @Override
                                            public double maxRowHeight(final SpreadsheetRowReference r) {
                                                checkEquals(row, r);
                                                return 0;
                                            }
                                        }
                                )
                )
        );
    }

    // widths top left .................................................................................................

    @Test
    public void testWindowLeft() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH,
                ROW_HEIGHT,
                "A1"
        );
    }

    @Test
    public void testWindowLeft2() {
        this.windowAndCheck(
                "A1",
                3 * COLUMN_WIDTH,
                ROW_HEIGHT,
                "A1:C1"
        );
    }

    @Test
    public void testWindowLeft3() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH - 1,
                ROW_HEIGHT,
                "A1"
        );
    }

    @Test
    public void testWindowLeft4() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH + 1,
                ROW_HEIGHT,
                "A1:B1"
        );
    }

    @Test
    public void testWindowLeft5() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH * 4 - 1,
                ROW_HEIGHT,
                "A1:D1"
        );
    }

    @Test
    public void testWindowLeft6() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH * 4 + 1,
                ROW_HEIGHT,
                "A1:E1"
        );
    }

    @Test
    public void testWindowMidX() {
        this.windowAndCheck(
                "M1",
                COLUMN_WIDTH,
                ROW_HEIGHT,
                "M1"
        );
    }

    @Test
    public void testWindowMidX2() {
        this.windowAndCheck(
                "M1",
                3 * COLUMN_WIDTH,
                ROW_HEIGHT,
                "M1:O1"
        );
    }

    @Test
    public void testWindowMidX3() {
        this.windowAndCheck(
                "M1",
                COLUMN_WIDTH - 1,
                ROW_HEIGHT,
                "M1"
        );
    }

    @Test
    public void testWindowMidX4() {
        this.windowAndCheck(
                "M1",
                COLUMN_WIDTH + 1,
                ROW_HEIGHT,
                "M1:N1"
        );
    }

    @Test
    public void testWindowMidX5() {
        this.windowAndCheck(
                "M1",
                COLUMN_WIDTH * 4 - 1,
                ROW_HEIGHT,
                "M1:P1"
        );
    }

    @Test
    public void testWindowMidX6() {
        this.windowAndCheck(
                "M1",
                COLUMN_WIDTH * 4 + 1,
                ROW_HEIGHT,
                "M1:Q1"
        );
    }

    // widths top right .................................................................................................

    @Test
    public void testWindowRight() {
        this.windowAndCheck(
                "XFD1",
                COLUMN_WIDTH,
                ROW_HEIGHT,
                "XFD1"
        );
    }

    @Test
    public void testWindowRight2() {
        this.windowAndCheck(
                "XFD1",
                3 * COLUMN_WIDTH,
                ROW_HEIGHT,
                "XFB1:XFD1"
        );
    }

    @Test
    public void testWindowRight3() {
        this.windowAndCheck(
                "XFD1",
                COLUMN_WIDTH - 1,
                ROW_HEIGHT,
                "XFD1"
        );
    }

    @Test
    public void testWindowRight4() {
        this.windowAndCheck(
                "XFD1",
                COLUMN_WIDTH + 1,
                ROW_HEIGHT,
                "XFC1:XFD1"
        );
    }

    @Test
    public void testWindowRight5() {
        this.windowAndCheck(
                "XFD1",
                COLUMN_WIDTH * 4 - 1,
                ROW_HEIGHT,
                "XFA1:XFD1"
        );
    }

    @Test
    public void testWindowRight6() {
        this.windowAndCheck(
                "XFD1",
                COLUMN_WIDTH * 4 + 1,
                ROW_HEIGHT,
                "XEZ1:XFD1"
        );
    }

    // heights top left .................................................................................................

    @Test
    public void testWindowTop() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH,
                ROW_HEIGHT,
                "A1"
        );
    }

    @Test
    public void testWindowTop2() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH,
                3 * ROW_HEIGHT,
                "A1:A3"
        );
    }

    @Test
    public void testWindowTop3() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH,
                ROW_HEIGHT - 1,
                "A1"
        );
    }

    @Test
    public void testWindowTop4() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH,
                ROW_HEIGHT + 1,
                "A1:A2"
        );
    }

    @Test
    public void testWindowTop5() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH,
                ROW_HEIGHT * 4 - 1,
                "A1:A4"
        );
    }

    @Test
    public void testWindowTop6() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH,
                ROW_HEIGHT * 4 + 1,
                "A1:A5"
        );
    }

    @Test
    public void testWindowMidY() {
        this.windowAndCheck(
                "A10",
                COLUMN_WIDTH,
                ROW_HEIGHT,
                "A10"
        );
    }

    @Test
    public void testWindowMidY2() {
        this.windowAndCheck(
                "A10",
                COLUMN_WIDTH,
                3 * ROW_HEIGHT,
                "A10:A12"
        );
    }

    @Test
    public void testWindowMidY3() {
        this.windowAndCheck(
                "A10",
                COLUMN_WIDTH,
                ROW_HEIGHT - 1,
                "A10"
        );
    }

    @Test
    public void testWindowMidY4() {
        this.windowAndCheck(
                "A10",
                COLUMN_WIDTH,
                ROW_HEIGHT + 1,
                "A10:A11"
        );
    }

    @Test
    public void testWindowMidY5() {
        this.windowAndCheck(
                "A10",
                COLUMN_WIDTH,
                ROW_HEIGHT * 4 - 1,
                "A10:A13"
        );
    }

    @Test
    public void testWindowMidY6() {
        this.windowAndCheck(
                "A10",
                COLUMN_WIDTH,
                ROW_HEIGHT * 4 + 1,
                "A10:A14"
        );
    }

    // heights top right .................................................................................................

    @Test
    public void testWindowBottom() {
        this.windowAndCheck(
                "A1048576",
                COLUMN_WIDTH,
                ROW_HEIGHT,
                "A1048576"
        );
    }

    @Test
    public void testWindowBottom2() {
        this.windowAndCheck(
                "A1048576",
                COLUMN_WIDTH,
                3 * ROW_HEIGHT,
                "A1048574:A1048576"
        );
    }

    @Test
    public void testWindowBottom3() {
        this.windowAndCheck(
                "A1048576",
                COLUMN_WIDTH,
                ROW_HEIGHT - 1,
                "A1048576"
        );
    }

    @Test
    public void testWindowBottom4() {
        this.windowAndCheck(
                "A1048576",
                COLUMN_WIDTH,
                ROW_HEIGHT + 1,
                "A1048575:A1048576"
        );
    }

    @Test
    public void testWindowBottom5() {
        this.windowAndCheck(
                "A1048576",
                COLUMN_WIDTH,
                ROW_HEIGHT * 4 - 1,
                "A1048573:A1048576"
        );
    }

    @Test
    public void testWindowBottom6() {
        this.windowAndCheck(
                "A1048576",
                COLUMN_WIDTH,
                ROW_HEIGHT * 4 + 1,
                "A1048572:A1048576"
        );
    }

    // window with selection within.....................................................................................

    @Test
    public void testWindowSelectionCellWithin() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("B2"),
                "B2:D4"
        );
    }

    @Test
    public void testWindowSelectionCellWithin2() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("C3"),
                "B2:D4"
        );
    }

    @Test
    public void testWindowSelectionCellWithin3() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("D4"),
                "B2:D4"
        );
    }

    @Test
    public void testWindowSelectionColumnWithin() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("B"),
                "B2:D4"
        );
    }

    @Test
    public void testWindowSelectionColumnWithin2() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("B"),
                "B2:D4"
        );
    }

    @Test
    public void testWindowSelectionColumnWithin3() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("C"),
                "B2:D4"
        );
    }

    @Test
    public void testWindowSelectionRowWithin() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseRow("2"),
                "B2:D4"
        );
    }

    @Test
    public void testWindowSelectionRowWithin2() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseRow("3"),
                "B2:D4"
        );
    }

    @Test
    public void testWindowSelectionRowWithin3() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseRow("4"),
                "B2:D4"
        );
    }

    // window Selection Outside..........................................................................................

    @Test
    public void testWindowSelectionCellLeft() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("A2"),
                "A2:C4"
        );
    }

    @Test
    public void testWindowSelectionCellLeft2() {
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("A3"),
                "A3:C5"
        );
    }

    @Test
    public void testWindowSelectionCellRight() {
        // B2:D4 -> 1
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("E2"),
                "C2:E4"
        );
    }

    @Test
    public void testWindowSelectionCellRight2() {
        // B2:D4 -> 2
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("F2"),
                "D2:F4"
        );
    }

    @Test
    public void testWindowSelectionCellTop() {
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("B1"),
                "B1:D3"
        );
    }

    @Test
    public void testWindowSelectionCellTop2() {
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("C1"),
                "C1:E3"
        );
    }

    @Test
    public void testWindowSelectionCellBottom() {
        // B2:D4 -> 1
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("B5"),
                "B3:D5"
        );
    }

    @Test
    public void testWindowSelectionCellBottom2() {
        // B2:D4 -> 2
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("B6"),
                "B4:D6"
        );
    }

    @Test
    public void testWindowSelectionCellTopLeft() {
        // C3:E5
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("A2"),
                "A2:C4"
        );
    }

    @Test
    public void testWindowSelectionCellBottomRight() {
        // C3:E5
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCell("G6"),
                "E4:G6"
        );
    }

    @Test
    public void testWindowSelectionColumnLeft() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("A"),
                "A2:C4"
        );
    }

    @Test
    public void testWindowSelectionColumnLeft2() {
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("A"),
                "A3:C5"
        );
    }

    @Test
    public void testWindowSelectionColumnRight() {
        // B2:D4 -> 1
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("E"),
                "C2:E4"
        );
    }

    @Test
    public void testWindowSelectionColumnRight2() {
        // B2:D4 -> 2
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("F"),
                "D2:F4"
        );
    }

    @Test
    public void testWindowSelectionRowTop() {
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseRow("1"),
                "B1:D3"
        );
    }

    @Test
    public void testWindowSelectionRowTop2() {
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseRow("1"),
                "C1:E3"
        );
    }

    @Test
    public void testWindowSelectionRowBottom() {
        // B2:D4 -> 1
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseRow("5"),
                "B3:D5"
        );
    }

    @Test
    public void testWindowSelectionRowBottom2() {
        // B2:D4 -> 2
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseRow("6"),
                "B4:D6"
        );
    }

    @Test
    public void testWindowSelectionColumnReferenceLeft() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumnRange("A"),
                "A2:C4"
        );
    }

    @Test
    public void testWindowSelectionColumnReferenceLeft2() {
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("A"),
                "A3:C5"
        );
    }

    @Test
    public void testWindowSelectionColumnReferenceLeft3() {
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("B"),
                "B3:D5"
        );
    }

    @Test
    public void testWindowSelectionColumnReferenceReferenceRight() {
        // B2:D4 -> 1
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("E"),
                "C2:E4"
        );
    }

    @Test
    public void testWindowSelectionColumnReferenceReferenceRight2() {
        // B2:D4 -> 1
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("F"),
                "D2:F4"
        );
    }

    @Test
    public void testWindowSelectionColumnReferenceReferenceRight3() {
        // B2:D4 -> 2
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("F"),
                "D2:F4"
        );
    }

    @Test
    public void testWindowSelectionColumnReferenceReferenceRight4() {
        // B2:D4 -> 2
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseColumn("G"),
                "E2:G4"
        );
    }

    @Test
    public void testWindowSelectionCellRangeLeft() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCellRange("A2"),
                "A2:C4"
        );
    }

    @Test
    public void testWindowSelectionCellRangeLeft2() {
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCellRange("A3"),
                "A3:C5"
        );
    }

    @Test
    public void testWindowSelectionCellRangeRight() {
        // B2:D4 -> 1
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCellRange("E2"),
                "C2:E4"
        );
    }

    @Test
    public void testWindowSelectionCellRangeRight2() {
        // B2:D4 -> 2
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCellRange("F2"),
                "D2:F4"
        );
    }

    @Test
    public void testWindowSelectionCellRangeTop() {
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCellRange("B1"),
                "B1:D3"
        );
    }

    @Test
    public void testWindowSelectionCellRangeTop2() {
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCellRange("C1"),
                "C1:E3"
        );
    }

    @Test
    public void testWindowSelectionCellRangeBottom() {
        // B2:D4 -> 1
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCellRange("B5"),
                "B3:D5"
        );
    }

    @Test
    public void testWindowSelectionCellRangeBottom2() {
        // B2:D4 -> 2
        // BCD:234
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCellRange("B6"),
                "B4:D6"
        );
    }

    @Test
    public void testWindowSelectionCellRangeTopLeft() {
        // C3:E5
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCellRange("A2"),
                "A2:C4"
        );
    }

    @Test
    public void testWindowSelectionCellRangeBottomRight() {
        // C3:E5
        this.windowAndCheck(
                "C3",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                SpreadsheetSelection.parseCellRange("G6"),
                "E4:G6"
        );
    }

    // window column/row hidden.........................................................................................

    @Test
    public void testWindowColumnHidden() {
        final SpreadsheetViewport viewport = SpreadsheetViewport.with(
                SpreadsheetSelection.parseCell("A1"),
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 2
        );

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        this.windowAndCheck(
                engine,
                viewport,
                false, // includeFrozenColumnsRows
                SpreadsheetEngine.NO_SELECTION,
                context,
                "A1:D2"
        );

        final SpreadsheetColumnStore columnStore = context.storeRepository()
                .columns();

        columnStore.save(
                SpreadsheetSelection.parseColumn("A")
                        .column()
                        .setHidden(true)
        );

        columnStore.save(
                SpreadsheetSelection.parseColumn("B")
                        .column()
                        .setHidden(true)
        );

        this.windowAndCheck(
                engine,
                viewport,
                false, // includeFrozenColumnsRows
                SpreadsheetEngine.NO_SELECTION,
                context,
                "A1:F2"
        );
    }

    @Test
    public void testWindowRowHidden() {
        final SpreadsheetViewport viewport = SpreadsheetViewport.with(
                SpreadsheetSelection.parseCell("A1"),
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 2
        );

        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        this.windowAndCheck(
                engine,
                viewport,
                false, // includeFrozenColumnsRows
                SpreadsheetEngine.NO_SELECTION,
                context,
                "A1:D2"
        );

        final SpreadsheetRowStore rowStore = context.storeRepository()
                .rows();

        rowStore.save(
                SpreadsheetSelection.parseRow("1")
                        .row()
                        .setHidden(true)
        );

        rowStore.save(
                SpreadsheetSelection.parseRow("2")
                        .row()
                        .setHidden(true)
        );

        this.windowAndCheck(
                engine,
                viewport,
                false, // includeFrozenColumnsRows
                SpreadsheetEngine.NO_SELECTION,
                context,
                "A1:D4"
        );
    }

    // window helpers....................................................................................................

    private void windowAndCheck(final String cellOrLabel,
                                final double width,
                                final double height,
                                final String range) {
        this.windowAndCheck(
                cellOrLabel,
                width,
                height,
                SpreadsheetEngine.NO_SELECTION,
                range
        );
    }

    private void windowAndCheck(final String cellOrLabel,
                                final double width,
                                final double height,
                                final SpreadsheetSelection selection,
                                final String range) {
        this.windowAndCheck(
                cellOrLabel,
                width,
                height,
                Optional.of(selection),
                range
        );
    }

    private void windowAndCheck(final String cellOrLabel,
                                final double width,
                                final double height,
                                final Optional<SpreadsheetSelection> selection,
                                final String range) {
        this.windowAndCheck(
                this.createSpreadsheetEngine(),
                SpreadsheetSelection.parseCellOrLabel(cellOrLabel).viewport(
                        width,
                        height
                ),
                false, // includeFrozenColumnsAndRows
                selection,
                this.createContext(),
                range
        );
    }

    // window with frozen columns / rows.................................................................................

    @Test
    public void testWindowIgnoreFrozenColumnsFrozenRows() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 3,
                0, // frozenColumns
                0, // frozenRows
                SpreadsheetEngine.NO_SELECTION,
                "A1:D3"
        );
    }

    @Test
    public void testWindowIgnoreFrozenColumnsFrozenRows2() {
        this.windowAndCheck(
                "B2",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 3,
                0, // frozenColumns
                0, // frozenRows
                SpreadsheetEngine.NO_SELECTION,
                "B2:E4"
        );
    }

    // window with frozen columns / rows.................................................................................

    @Test
    public void testWindowFrozenColumnsFrozenRows() {
        this.windowAndCheck(
                "Z99",
                COLUMN_WIDTH * 2,
                ROW_HEIGHT * 2,
                2, // frozenColumns
                2, // frozenRows
                "A1:B2"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRows2() {
        this.windowAndCheck(
                "Z99",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                3, // frozenColumns
                3, // frozenRows
                "A1:C3"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsOnly() {
        this.windowAndCheck(
                "Z99",
                COLUMN_WIDTH * 2,
                ROW_HEIGHT * 2,
                9, // frozenColumns
                9, // frozenRows
                "A1:B2"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsOnly2() {
        this.windowAndCheck(
                "Z99",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                9, // frozenColumns
                9, // frozenRows
                "A1:C3"
        );
    }

    // A1
    // A2
    // A3
    @Test
    public void testWindowFrozenColumnsOnlyInvalidOverlappingHome() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH * 1,
                ROW_HEIGHT * 3,
                1, // frozenColumns
                0, // frozenRows
                "A1:A3"
        );
    }

    // A1
    // A2
    // A3
    @Test
    public void testWindowFrozenColumnsOnly() {
        this.windowAndCheck(
                "B1",
                COLUMN_WIDTH * 1,
                ROW_HEIGHT * 3,
                1, // frozenColumns
                0, // frozenRows
                "A1:A3"
        );
    }

    // A1 B1
    // A2 B2
    // A3 B3
    @Test
    public void testWindowFrozenColumnsOnly2() {
        this.windowAndCheck(
                "B1",
                COLUMN_WIDTH * 2,
                ROW_HEIGHT * 3,
                2, // frozenColumns
                0, // frozenRows
                "A1:B3"
        );
    }

    // A1 B1 C1
    // A2 B2 C2
    // A3 B3 C3
    @Test
    public void testWindowFrozenColumnsOnly3() {
        this.windowAndCheck(
                "B1",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                3, // frozenColumns
                0, // frozenRows
                "A1:C3"
        );
    }

    // A1 B1 C1
    // A2 B2 C2
    // A3 B3 C3
    @Test
    public void testWindowFrozenColumnsOnly4() {
        this.windowAndCheck(
                "B1",
                COLUMN_WIDTH * 3,
                ROW_HEIGHT * 3,
                99, // frozenColumns
                0, // frozenRows
                "A1:C3"
        );
    }

    // A1  b1 c1 d1
    // A2  b2 c2 d2
    // A3  b3 c3 d3
    @Test
    public void testWindowFrozenColumns() {
        this.windowAndCheck(
                "B1",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 3,
                1, // frozenColumns
                0, // frozenRows
                "A1:A3,B1:D3"
        );
    }

    // A1 B1  c1 d1
    // A2 B2  c2 d2
    // A3 B3  c3 d3
    @Test
    public void testWindowFrozenColumns2() {
        this.windowAndCheck(
                "c1",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 3,
                2, // frozenColumns
                0, // frozenRows
                "A1:B3,C1:D3"
        );
    }

    // A1 B1  f1 g1
    // A2 B2  f2 g2
    // A3 B3  f3 g3
    @Test
    public void testWindowFrozenColumnsNonFrozenGap() {
        this.windowAndCheck(
                "f1",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 3,
                2, // frozenColumns
                0, // frozenRows
                "A1:B3,F1:G3"
        );
    }

    // A1 B1 C1 D1
    @Test
    public void testWindowFrozenRowsOnlyInvalidOverlappingHome() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 1,
                0, // frozenColumns
                1, // frozenRows
                "A1:D1"
        );
    }

    // A1 B1 C1 D1
    @Test
    public void testWindowFrozenRowsOnly() {
        this.windowAndCheck(
                "A2",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 1,
                0, // frozenColumns
                1, // frozenRows
                "A1:D1"
        );
    }

    // A1 B1 C1 D1
    // A2 B2 C2 D2
    @Test
    public void testWindowFrozenRowsOnly2() {
        this.windowAndCheck(
                "A2",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 2,
                0, // frozenColumns
                2, // frozenRows
                "A1:D2"
        );
    }

    // A1 B1 C1 D1
    // A2 B2 C2 D2
    @Test
    public void testWindowFrozenRowsOnly3() {
        this.windowAndCheck(
                "A2",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 2,
                0, // frozenColumns
                99, // frozenRows
                "A1:D2"
        );
    }

    // A1 B1 C1 D1
    //
    // a2 b2 c2 d2
    // a3 b3 c3 d3
    @Test
    public void testWindowFrozenRows() {
        this.windowAndCheck(
                "A2",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 3,
                0, // frozenColumns
                1, // frozenRows
                "A1:D1,A2:D3"
        );
    }


    // A1 B1 C1 D1
    // A2 B2 C2 D2
    //
    // a3 b3 c3 d3
    @Test
    public void testWindowFrozenRows2() {
        this.windowAndCheck(
                "a3",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 3,
                0, // frozenColumns
                2, // frozenRows
                "A1:D2,A3:D3"
        );
    }


    // A1 B1 C1 D1
    // A2 B2 C2 D2
    //
    // a3 b3 c3 d3
    // a4 b4 c4 d4
    // a5 b5 c5 d5
    @Test
    public void testWindowFrozenRowsNonFrozenGap() {
        this.windowAndCheck(
                "a3",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 5,
                0, // frozenColumns
                2, // frozenRows
                "A1:D2,A3:D5"
        );
    }

    // A1 B1 C1 D1
    // A2 B2 C2 D2
    //
    // a6 b6 c6 d6
    // a7 b7 c7 d7
    // a8 b8 c8 d8
    @Test
    public void testWindowFrozenRowsNonFrozenGap2() {
        this.windowAndCheck(
                "a6",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 5,
                0, // frozenColumns
                2, // frozenRows
                "A1:D2,A6:D8"
        );
    }

    // A1 B1 C1 D1
    // A2 B2 C2 D2
    //
    // a3 b3 c3 d3
    // a4 b4 c4 d4
    // a5 b5 c5 d5
    @Test
    public void testWindowOnlyFrozenRowsInvalidHome() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 5,
                0, // frozenColumns
                2, // frozenRows
                "A1:D2,A3:D5"
        );
    }

    // A1   B1 C1 D1
    //
    // A2   b2 c2 d2
    // A3   b3 c3 d3
    // A4   b4 c4 d4
    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozen() {
        this.windowAndCheck(
                "b2",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 4,
                1, // frozenColumns
                1, // frozenRows
                "A1,B1:D1,A2:A4,B2:D4"
        );
    }

    // A1   B1 C1 D1
    // A2   B2 C2 D2
    //
    // A3   b3 c3 d3
    // A4   b4 c4 d4
    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozen2() {
        this.windowAndCheck(
                "b3",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 4,
                1, // frozenColumns
                2, // frozenRows
                "A1:A2,B1:D2,A3:A4,B3:D4"
        );
    }

    // A1 B1   C1 D1
    //
    // A2 B2   c2 d2
    // A3 B3   c3 d3
    // A4 B4   c4 d4
    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozen3() {
        this.windowAndCheck(
                "c2",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 4,
                2, // frozenColumns
                1, // frozenRows
                "A1:B1,C1:D1,A2:B4,C2:D4"
        );
    }

    // A1 B1   C1 D1
    // A2 B2   c2 d2
    //
    // A3 B3   c3 d3
    // A4 B4   c4 d4
    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozen4() {
        this.windowAndCheck(
                "c3",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 4,
                2, // frozenColumns
                2, // frozenRows
                "A1:B2,C1:D2,A3:B4,C3:D4"
        );
    }

    // A1 B1   C1 D1
    // A2 B2   c2 d2
    //
    // A3 B3   c3 d3
    // A4 B4   c4 d4
    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenInvalidHome() {
        this.windowAndCheck(
                "A1",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 4,
                2, // frozenColumns
                2, // frozenRows
                "A1:B2,C1:D2,A3:B4,C3:D4"
        );
    }

    // A1 B1   C1 D1
    // A2 B2   C2 D2
    // A3 B3   C3 D3
    //
    // A8 B8   c8 d8

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenGap() {
        this.windowAndCheck(
                "c8",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 4,
                2, // frozenColumns
                3, // frozenRows
                "A1:B3,C1:D3,A8:B8,C8:D8"
        );
    }

    // A1 B1   C1 D1
    // A2 B2   C2 D2
    // A3 B3   C3 D3
    //
    // A8 B8   c8 d8
    // A9 B9   c9 d9

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenGap2() {
        this.windowAndCheck(
                "c8",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 5,
                2, // frozenColumns
                3, // frozenRows
                "A1:B3,C1:D3,A8:B9,C8:D9"
        );
    }


    private void windowAndCheck(final String cellOrLabel,
                                final double width,
                                final double height,
                                final int frozenColumns,
                                final int frozenRows,
                                final String range) {
        this.windowAndCheck(
                cellOrLabel,
                width,
                height,
                frozenColumns,
                frozenRows,
                SpreadsheetEngine.NO_SELECTION,
                range
        );
    }

    // window selection .................................................................................................

    // A1 B1 C1   D1 E1
    // A2 B2 C2   D2 E2
    // A3 B3 C3   D3 E3
    //
    // A4 B4 C4   D4 E4
    // A5 B5 C5   D5 E5
    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenFrozenColumn() {
        this.windowAndCheck(
                "D4",
                COLUMN_WIDTH * 5,
                ROW_HEIGHT * 5,
                3, // frozenColumns
                3, // frozenRows
                SpreadsheetSelection.parseColumn("A"),
                "A1:C3,D1:E3,A4:C5,D4:E5"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenNonFrozenColumn() {
        this.windowAndCheck(
                "D4",
                COLUMN_WIDTH * 5,
                ROW_HEIGHT * 5,
                3, // frozenColumns
                3, // frozenRows
                SpreadsheetSelection.parseColumn("D"),
                "A1:C3,D1:E3,A4:C5,D4:E5"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenFrozenRow() {
        this.windowAndCheck(
                "D4",
                COLUMN_WIDTH * 5,
                ROW_HEIGHT * 5,
                3, // frozenColumns
                3, // frozenRows
                SpreadsheetSelection.parseRow("1"),
                "A1:C3,D1:E3,A4:C5,D4:E5"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenNonFrozenRow() {
        this.windowAndCheck(
                "D4",
                COLUMN_WIDTH * 5,
                ROW_HEIGHT * 5,
                3, // frozenColumns
                3, // frozenRows
                SpreadsheetSelection.parseRow("4"),
                "A1:C3,D1:E3,A4:C5,D4:E5"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenFrozenCell() {
        this.windowAndCheck(
                "D4",
                COLUMN_WIDTH * 5,
                ROW_HEIGHT * 5,
                3, // frozenColumns
                3, // frozenRows
                SpreadsheetSelection.parseCell("A1"),
                "A1:C3,D1:E3,A4:C5,D4:E5"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenNonFrozenCell() {
        this.windowAndCheck(
                "D4",
                COLUMN_WIDTH * 5,
                ROW_HEIGHT * 5,
                3, // frozenColumns
                3, // frozenRows
                SpreadsheetSelection.parseCell("D4"),
                "A1:C3,D1:E3,A4:C5,D4:E5"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenNonFrozenCell2() {
        this.windowAndCheck(
                "D4",
                COLUMN_WIDTH * 5,
                ROW_HEIGHT * 5,
                3, // frozenColumns
                3, // frozenRows
                SpreadsheetSelection.parseCell("E5"),
                "A1:C3,D1:E3,A4:C5,D4:E5"
        );
    }

    // window selection pan.............................................................................................

    // A1 B1 C1   D1 E1
    // A2 B2 C2   D2 E2
    // A3 B3 C3   D3 E3
    //
    // A4 B4 C4   D4 E4 F4
    // A5 B5 C5   D5 E5

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenPanNonFrozenCell() {
        this.windowAndCheck(
                "D4",
                COLUMN_WIDTH * 5,
                ROW_HEIGHT * 5,
                3, // frozenColumns
                3, // frozenRows
                SpreadsheetSelection.parseCell("F4"),
                "A1:C3,D1:E3,A4:C5,E4:F5"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenPanNonFrozenCell2() {
        this.windowAndCheck(
                "D4",
                COLUMN_WIDTH * 5,
                ROW_HEIGHT * 5,
                3, // frozenColumns
                3, // frozenRows
                SpreadsheetSelection.parseCell("G4"),
                "A1:C3,D1:E3,A4:C5,F4:G5"
        );
    }

    // A1 B1   C1  D1 E1
    // A2 B2   C2  D2 E2
    //
    // A3 B3   C3  D3 E3
    //
    // A4 B4   C4  D4 E4
    // A5 B5   C5  D5 E5
    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenPanNonFrozenCellPartialWidth() {
        this.windowAndCheck(
                "C4",
                COLUMN_WIDTH * 4 - 2,
                ROW_HEIGHT * 4,
                2, // frozenColumns
                2, // frozenRows
                SpreadsheetSelection.parseCell("D4"),
                "A1:B2,D1:E2,A4:B5,D4:E5"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenPanNonFrozenCellPartialWidth2() {
        this.windowAndCheck(
                "C4",
                COLUMN_WIDTH * 4 - 1,
                ROW_HEIGHT * 4,
                2, // frozenColumns
                2, // frozenRows
                SpreadsheetSelection.parseCell("D4"),
                "A1:B2,D1:E2,A4:B5,D4:E5"
        );
    }

    // A1 B1   C1  D1 E1
    // A2 B2   C2  D2 E2
    //
    // A3 B3   C3  D3 E3
    //
    // A4 B4   C4  D4 E4
    // A5 B5   C5  D5 E5
    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenPanNonFrozenCellPartialHeight() {
        this.windowAndCheck(
                "D3",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 4 - 2,
                2, // frozenColumns
                2, // frozenRows
                SpreadsheetSelection.parseCell("D4"),
                "A1:B2,D1:E2,A4:B5,D4:E5"
        );
    }

    @Test
    public void testWindowFrozenColumnsFrozenRowsNonFrozenPanNonFrozenCellPartialHeight2() {
        this.windowAndCheck(
                "D3",
                COLUMN_WIDTH * 4,
                ROW_HEIGHT * 4 - 1,
                2, // frozenColumns
                2, // frozenRows
                SpreadsheetSelection.parseCell("D4"),
                "A1:B2,D1:E2,A4:B5,D4:E5"
        );
    }

    private void windowAndCheck(final String cellOrLabel,
                                final double width,
                                final double height,
                                final int frozenColumns,
                                final int frozenRows,
                                final SpreadsheetSelection selection,
                                final String window) {
        this.windowAndCheck(
                cellOrLabel,
                width,
                height,
                frozenColumns,
                frozenRows,
                Optional.of(selection),
                window
        );
    }

    private void windowAndCheck(final String cellOrLabel,
                                final double width,
                                final double height,
                                final int frozenColumns,
                                final int frozenRows,
                                final Optional<SpreadsheetSelection> selection,
                                final String window) {
        final SpreadsheetMetadata metadata = this.metadata()
                .setOrRemove(
                        SpreadsheetMetadataPropertyName.FROZEN_COLUMNS,
                        frozenColumns > 0 ?
                                SpreadsheetReferenceKind.RELATIVE.firstColumn().columnRange(SpreadsheetReferenceKind.RELATIVE.column(frozenColumns - 1)) :
                                null
                ).setOrRemove(
                        SpreadsheetMetadataPropertyName.FROZEN_ROWS,
                        frozenRows > 0 ?
                                SpreadsheetReferenceKind.RELATIVE.firstRow().rowRange(SpreadsheetReferenceKind.RELATIVE.row(frozenRows - 1)) :
                                null
                );

        this.windowAndCheck(
                this.createSpreadsheetEngine(),
                SpreadsheetSelection.parseCellOrLabel(cellOrLabel)
                        .viewport(
                                width,
                                height
                        ),
                true, // includeFrozenColumnsAndRows
                selection,
                this.createContext(metadata),
                window
        );
    }

    //  navigate........................................................................................................

    @Test
    public void testNavigateSelectionHiddenAndMissingNavigation() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        final SpreadsheetColumnReference column = SpreadsheetSelection.parseColumn("B");

        final SpreadsheetColumnStore store = context.storeRepository()
                .columns();

        store.save(
                column.column()
                        .setHidden(true)
        );

        final SpreadsheetViewportSelection viewportSelection = column.setAnchor(SpreadsheetViewportSelectionAnchor.NONE);

        this.navigateAndCheck(
                engine,
                viewportSelection,
                context,
                SpreadsheetEngine.NO_VIEWPORT_SELECTION
        );
    }

    @Test
    public void testNavigateSelectionMissingNavigation() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        final SpreadsheetViewportSelection viewportSelection = SpreadsheetSelection.parseColumn("B")
                .setAnchor(SpreadsheetViewportSelectionAnchor.NONE);

        this.navigateAndCheck(
                engine,
                viewportSelection,
                context,
                Optional.of(viewportSelection)
        );
    }

    @Test
    public void testNavigateSelectionLabelMissingNavigation() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        context.storeRepository()
                .labels()
                .save(LABEL.mapping(SpreadsheetSelection.parseCell("B2")));

        final SpreadsheetViewportSelection viewportSelection = LABEL
                .setAnchor(SpreadsheetViewportSelectionAnchor.NONE);

        this.navigateAndCheck(
                engine,
                viewportSelection,
                context,
                Optional.of(viewportSelection)
        );
    }

    @Test
    public void testNavigateCell() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        this.navigateAndCheck(
                engine,
                SpreadsheetSelection.parseCell("B2")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
                        .setNavigation(
                                Optional.of(
                                        SpreadsheetViewportSelectionNavigation.RIGHT
                                )
                        ),
                context,
                SpreadsheetSelection.parseCell("C2")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    @Test
    public void testNavigateLabelUnchanged() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        final SpreadsheetCellReference selection = SpreadsheetSelection.parseCell("A1");

        context.storeRepository()
                .labels()
                .save(LABEL.mapping(selection));

        final SpreadsheetViewportSelection viewportSelection = selection.setAnchor(SpreadsheetViewportSelectionAnchor.NONE);

        this.navigateAndCheck(
                engine,
                viewportSelection
                        .setNavigation(
                                Optional.of(
                                        SpreadsheetViewportSelectionNavigation.LEFT
                                )
                        ),
                context,
                viewportSelection
        );
    }

    @Test
    public void testNavigateLabelToCell() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        final SpreadsheetCellReference selection = SpreadsheetSelection.parseCell("A1");

        context.storeRepository()
                .labels()
                .save(LABEL.mapping(selection));

        this.navigateAndCheck(
                engine,
                selection.setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
                        .setNavigation(
                                Optional.of(
                                        SpreadsheetViewportSelectionNavigation.RIGHT
                                )
                        ),
                context,
                SpreadsheetSelection.parseCell("B1")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    @Test
    public void testNavigateLabelToRange() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        final SpreadsheetCellRange selection = SpreadsheetSelection.parseCellRange("A1:B1");

        context.storeRepository()
                .labels()
                .save(LABEL.mapping(selection));

        this.navigateAndCheck(
                engine,
                selection.setAnchor(SpreadsheetViewportSelectionAnchor.TOP_LEFT)
                        .setNavigation(
                                Optional.of(
                                        SpreadsheetViewportSelectionNavigation.EXTEND_RIGHT
                                )
                        ),
                context,
                SpreadsheetSelection.parseCellRange("A1:C1")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.TOP_LEFT)
        );
    }

    @Test
    public void testNavigateHidden() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        final SpreadsheetColumnStore store = context.storeRepository()
                .columns();

        store.save(
                SpreadsheetSelection.parseColumn("A")
                        .column()
                        .setHidden(true)
        );

        store.save(
                SpreadsheetSelection.parseColumn("B")
                        .column()
                        .setHidden(true)
        );

        this.navigateAndCheck(
                engine,
                SpreadsheetSelection.parseColumn("B")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
                        .setNavigation(
                                Optional.of(
                                        SpreadsheetViewportSelectionNavigation.LEFT
                                )
                        ),
                context,
                SpreadsheetEngine.NO_VIEWPORT_SELECTION
        );
    }

    @Test
    public void testNavigateSkipsHiddenColumn() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        context.storeRepository()
                .columns()
                .save(
                        SpreadsheetSelection.parseColumn("B")
                                .column()
                                .setHidden(true)
                );

        this.navigateAndCheck(
                engine,
                SpreadsheetSelection.parseColumn("A")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
                        .setNavigation(
                                Optional.of(
                                        SpreadsheetViewportSelectionNavigation.RIGHT
                                )
                        ),
                context,
                SpreadsheetSelection.parseColumn("C").setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    @Test
    public void testNavigateSkipsHiddenRow() {
        final BasicSpreadsheetEngine engine = this.createSpreadsheetEngine();
        final SpreadsheetEngineContext context = this.createContext();

        context.storeRepository()
                .rows()
                .save(
                        SpreadsheetSelection.parseRow("3")
                                .row()
                                .setHidden(true)
                );

        this.navigateAndCheck(
                engine,
                SpreadsheetSelection.parseRow("2")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
                        .setNavigation(
                                Optional.of(
                                        SpreadsheetViewportSelectionNavigation.DOWN
                                )
                        ),
                context,
                SpreadsheetSelection.parseRow("4").setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    //  helpers.........................................................................................................

    @Override
    public BasicSpreadsheetEngine createSpreadsheetEngine() {
        return BasicSpreadsheetEngine.INSTANCE;
    }

    @Override
    public SpreadsheetEngineContext createContext() {
        return this.createContext(SpreadsheetEngines.fake());
    }

    private SpreadsheetEngineContext createContext(final SpreadsheetCellStore cellStore) {
        return this.createContext(
                SpreadsheetMetadata.EMPTY,
                cellStore
        );
    }

    private SpreadsheetEngineContext createContext(final SpreadsheetMetadata metadata,
                                                   final SpreadsheetCellStore cellStore) {
        return this.createContext(
                DEFAULT_YEAR,
                SpreadsheetEngines.fake(),
                metadata,
                this.createSpreadsheetStoreRepository(cellStore)
        );
    }

    private SpreadsheetStoreRepository createSpreadsheetStoreRepository(final SpreadsheetCellStore cellStore) {
        return new FakeSpreadsheetStoreRepository() {
            @Override
            public SpreadsheetCellStore cells() {
                return cellStore;
            }

            @Override
            public SpreadsheetColumnStore columns() {
                return this.columnStore;
            }

            private final SpreadsheetColumnStore columnStore = SpreadsheetColumnStores.treeMap();

            @Override
            public SpreadsheetRowStore rows() {
                return this.rowStore;
            }

            private final SpreadsheetRowStore rowStore = SpreadsheetRowStores.treeMap();
        };
    }

    private SpreadsheetEngineContext createContext(final SpreadsheetEngine engine) {
        return this.createContext(
                DEFAULT_YEAR,
                engine
        );
    }

    private SpreadsheetEngineContext createContext(final int defaultYear,
                                                   final SpreadsheetEngine engine) {
        return this.createContext(
                defaultYear,
                engine,
                SpreadsheetStoreRepositories.basic(
                        SpreadsheetCellStores.treeMap(),
                        SpreadsheetExpressionReferenceStores.treeMap(),
                        SpreadsheetColumnStores.treeMap(),
                        SpreadsheetGroupStores.fake(),
                        SpreadsheetLabelStores.treeMap(),
                        SpreadsheetExpressionReferenceStores.treeMap(),
                        SpreadsheetMetadataStores.fake(),
                        SpreadsheetCellRangeStores.treeMap(),
                        SpreadsheetCellRangeStores.treeMap(),
                        SpreadsheetRowStores.treeMap(),
                        SpreadsheetUserStores.fake()
                )
        );
    }

    private SpreadsheetEngineContext createContext(final int defaultYear,
                                                   final SpreadsheetEngine engine,
                                                   final SpreadsheetStoreRepository storeRepository) {
        return this.createContext(
                defaultYear,
                engine,
                this.metadata(),
                storeRepository
        );
    }

    private SpreadsheetEngineContext createContext(final SpreadsheetMetadata metadata) {
        return this.createContext(
            20,
            SpreadsheetEngines.fake(),
            metadata,
            this.createSpreadsheetStoreRepository(SpreadsheetCellStores.treeMap())
        );
    }

    private SpreadsheetEngineContext createContext(final int defaultYear,
                                                   final SpreadsheetEngine engine,
                                                   final SpreadsheetMetadata metadata, // required by ranges tests with frozen columns/rows.
                                                   final SpreadsheetStoreRepository storeRepository) {
        return new FakeSpreadsheetEngineContext() {

            @Override
            public SpreadsheetSelection resolveIfLabel(final SpreadsheetSelection selection) {
                if (selection.isLabelName()) {
                    return this.storeRepository()
                            .labels()
                            .cellReferenceOrRangeOrFail((SpreadsheetExpressionReference) selection);
                }
                return selection;
            }

            public SpreadsheetMetadata metadata() {
                return metadata.set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, defaultYear);
            }

            @Override
            public int defaultYear() {
                return this.metadata()
                        .getOrFail(SpreadsheetMetadataPropertyName.DEFAULT_YEAR);
            }

            @Override
            public SpreadsheetParserToken parseFormula(final TextCursor formula) {
                return SpreadsheetParsers.valueOrExpression(BasicSpreadsheetEngineTest.this.metadata().parser())
                        .orFailIfCursorNotEmpty(ParserReporters.basic())
                        .parse(
                                formula,
                                SpreadsheetParserContexts.basic(
                                        DateTimeContexts.fake(),
                                        converterContext(),
                                        this.metadata().expressionNumberKind(),
                                        VALUE_SEPARATOR
                                )
                        )
                        .get()
                        .cast(SpreadsheetParserToken.class);
            }

            @Override
            public Object evaluate(final Expression node,
                                   final Optional<SpreadsheetCell> cell) {
                return node.toValue(
                        ExpressionEvaluationContexts.basic(
                                this.metadata().expressionNumberKind(),
                                this.functions(),
                                SpreadsheetErrorKind::translate,
                                this.references(),
                                SpreadsheetExpressionEvaluationContexts.referenceNotFound(),
                                CaseSensitivity.INSENSITIVE,
                                this.converterContext()
                        )
                );
            }

            @Override
            public boolean isPure(final FunctionExpressionName function) {
                return this.functions()
                        .apply(function)
                        .isPure(this);
            }

            private Function<FunctionExpressionName, ExpressionFunction<?, ExpressionEvaluationContext>> functions() {
                return (name) -> {
                    switch (name.value()) {
                        case "BasicSpreadsheetEngineTestNumberParameter":
                            return new FakeExpressionFunction<>() {
                                @Override
                                public Object apply(final List<Object> parameters,
                                                    final ExpressionEvaluationContext context) {
                                    return NUMBER.getOrFail(
                                            parameters,
                                            0
                                    );
                                }

                                private final ExpressionFunctionParameter<ExpressionNumber> NUMBER = ExpressionFunctionParameterName.with("parameters")
                                        .required(ExpressionNumber.class)
                                        .setKinds(ExpressionFunctionParameterKind.CONVERT_EVALUATE_RESOLVE_REFERENCES);

                                @Override
                                public List<ExpressionFunctionParameter<?>> parameters(final int count) {
                                    return Lists.of(NUMBER);
                                }

                                @Override
                                public boolean isPure(final ExpressionPurityContext context) {
                                    return false;
                                }
                            };
                        case "BasicSpreadsheetEngineTestStringParameter":
                            return new FakeExpressionFunction<>() {
                                @Override
                                public Object apply(final List<Object> parameters,
                                                    final ExpressionEvaluationContext context) {
                                    return STRING.getOrFail(
                                            parameters,
                                            0
                                    );
                                }

                                private final ExpressionFunctionParameter<String> STRING = ExpressionFunctionParameterName.with("parameters")
                                        .required(String.class)
                                        .setKinds(ExpressionFunctionParameterKind.CONVERT_EVALUATE_RESOLVE_REFERENCES);

                                @Override
                                public List<ExpressionFunctionParameter<?>> parameters(final int count) {
                                    return Lists.of(STRING);
                                }

                                @Override
                                public boolean isPure(final ExpressionPurityContext context) {
                                    return false;
                                }
                            };
                        case "BasicSpreadsheetEngineTestSum":
                            return new FakeExpressionFunction<>() {
                                @Override
                                public Object apply(final List<Object> parameters,
                                                    final ExpressionEvaluationContext context) {
                                    return parameters.stream()
                                            .map(ExpressionNumber.class::cast)
                                            .reduce(context.expressionNumberKind().zero(), (l, r) -> l.add(r, context));
                                }

                                @Override
                                public List<ExpressionFunctionParameter<?>> parameters(final int count) {
                                    return Lists.of(
                                            ExpressionFunctionParameterName.with("parameters")
                                                    .variable(Object.class)
                                                    .setKinds(ExpressionFunctionParameterKind.CONVERT_EVALUATE_RESOLVE_REFERENCES)
                                    );
                                }
                            };
                        case "BasicSpreadsheetEngineTestValue":
                            return new FakeExpressionFunction<>() {
                                @Override
                                public Object apply(final List<Object> parameters,
                                                    final ExpressionEvaluationContext context) {
                                    return BasicSpreadsheetEngineTest.this.value;
                                }

                                @Override
                                public List<ExpressionFunctionParameter<?>> parameters(final int count) {
                                    return Lists.of(
                                            ExpressionFunctionParameterName.with("parameters")
                                                    .variable(Object.class)
                                    );
                                }

                                @Override
                                public boolean isPure(final ExpressionPurityContext context) {
                                    return false;
                                }
                            };
                        default:
                            throw new UnknownExpressionFunctionException(name);
                    }
                };
            }

            private Function<ExpressionReference, Optional<Optional<Object>>> references() {
                return (r -> {
                    if (r instanceof SpreadsheetExpressionReference) {
                        final SpreadsheetCellReference cell = this.resolveIfLabel((SpreadsheetExpressionReference) r)
                                .toCellOrFail();
                        final SpreadsheetDelta delta = engine.loadCells(
                                cell,
                                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                                SpreadsheetDeltaProperties.ALL,
                                this
                        );
                        return delta.cell(cell)
                                .map(c -> c.formula().value());
                    }
                    return Optional.empty();
                });
            }

            private ConverterContext converterContext() {
                return this.metadata()
                        .converterContext(
                                NOW,
                                RESOLVE_IF_LABEL
                        );
            }

            @Override
            public MathContext mathContext() {
                return MATH_CONTEXT;
            }

            @Override
            public Optional<SpreadsheetText> format(final Object value,
                                                    final SpreadsheetFormatter formatter) {
                assertFalse(value instanceof Optional, () -> "Value must not be optional" + value);
                return formatter.format(Cast.to(value), SPREADSHEET_TEXT_FORMAT_CONTEXT);
            }

            @Override
            public SpreadsheetStoreRepository storeRepository() {
                return storeRepository;
            }
        };
    }

    private Object value;

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

    private SpreadsheetCell formattedCell(final String reference,
                                          final String formulaText,
                                          final Object value) {
        return this.formattedCell(
                SpreadsheetSelection.parseCell(reference),
                formulaText,
                value
        );
    }

    private SpreadsheetCell formattedCell(final SpreadsheetCellReference reference,
                                          final String formulaText,
                                          final Object value) {
        return this.formattedCell(
                this.cell(reference, formulaText),
                value
        );
    }

    /**
     * Makes a {@link SpreadsheetCell} updating the formula expression and expected value and then formats the cell adding styling etc,
     * mimicking the very actions that happen during evaluation.
     */
    private SpreadsheetCell formattedCell(final SpreadsheetCell cell,
                                          final Object value) {
        return this.formattedCell(
                cell,
                value,
                this.style()
        );
    }

    private SpreadsheetCell formattedCell(final SpreadsheetCell cell,
                                          final Object value,
                                          final TextStyle style) {
        final SpreadsheetText formattedText = this.metadata()
                .formatter()
                .format(value, SPREADSHEET_TEXT_FORMAT_CONTEXT)
                .orElseThrow(() -> new AssertionError("Failed to format " + CharSequences.quoteIfChars(value)));
        final Optional<TextNode> formattedCell = Optional.of(
                style.replace(formattedText.toTextNode())
                        .root()
        );

        return cell.setFormula(
                        this.parseFormula(cell.formula()
                        ).setValue(Optional.of(value)))
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
                parsedFormula = parsedFormula.setValue(
                        Optional.of(
                                SpreadsheetErrorKind.VALUE.setMessage(
                                        fail.getMessage()
                                )
                        )
                );
            }
        }

        return parsedFormula;
    }

    private void loadCellStoreAndCheck(final SpreadsheetCellStore store,
                                       final SpreadsheetCell... cells) {
        this.checkEquals(
                Lists.of(cells),
                store.all(),
                () -> "loaded all cells in " + store
        );
    }

    private void loadLabelStoreAndCheck(final SpreadsheetLabelStore store,
                                        final SpreadsheetLabelMapping... mappings) {
        this.checkEquals(
                Lists.of(mappings),
                store.all(),
                () -> "loaded all label mappings in " + store
        );
    }

    private <E extends SpreadsheetExpressionReference & Comparable<E>>
    void loadReferencesAndCheck(final SpreadsheetExpressionReferenceStore<E> store,
                                final E cell,
                                final SpreadsheetCellReference... out) {
        this.checkEquals(
                Optional.ofNullable(out.length == 0 ? null : Sets.of(out)),
                store.load(cell),
                () -> "references to " + cell
        );
    }

    private void loadReferrersAndCheck(final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> store,
                                       final SpreadsheetCellReference cell,
                                       final SpreadsheetCellReference... out) {
        this.checkEquals(Sets.of(out),
                store.loadReferred(cell),
                "referrers from " + cell);
    }

    private ExpressionNumber number(final Number number) {
        return this.expressionNumberKind().create(number);
    }

    private SpreadsheetMetadata metadata() {
        final String suffix = " \"" + FORMATTED_PATTERN_SUFFIX + "\"";

        return SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                .loadFromLocale()
                .set(SpreadsheetMetadataPropertyName.DATETIME_OFFSET, Converters.EXCEL_1900_DATE_SYSTEM_OFFSET)
                .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, DEFAULT_YEAR)
                .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, EXPRESSION_NUMBER_KIND)
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                .set(SpreadsheetMetadataPropertyName.PRECISION, 7)
                .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, TWO_DIGIT_YEAR)
                .set(SpreadsheetMetadataPropertyName.DATE_FORMAT_PATTERN, SpreadsheetParsePattern.parseDateFormatPattern(DATE_PATTERN + suffix))
                .set(SpreadsheetMetadataPropertyName.DATE_PARSE_PATTERN, SpreadsheetParsePattern.parseDateParsePattern(DATE_PATTERN + ";dd/mm"))
                .set(SpreadsheetMetadataPropertyName.DATETIME_FORMAT_PATTERN, SpreadsheetParsePattern.parseDateTimeFormatPattern(DATETIME_PATTERN + suffix))
                .set(SpreadsheetMetadataPropertyName.DATETIME_PARSE_PATTERN, SpreadsheetParsePattern.parseDateTimeParsePattern(DATETIME_PATTERN))
                .set(SpreadsheetMetadataPropertyName.NUMBER_FORMAT_PATTERN, SpreadsheetParsePattern.parseNumberFormatPattern(NUMBER_PATTERN + suffix))
                .set(SpreadsheetMetadataPropertyName.NUMBER_PARSE_PATTERN, SpreadsheetParsePattern.parseNumberParsePattern(NUMBER_PATTERN))
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetParsePattern.parseTextFormatPattern(TEXT_PATTERN + suffix))
                .set(SpreadsheetMetadataPropertyName.TIME_FORMAT_PATTERN, SpreadsheetParsePattern.parseTimeFormatPattern(TIME_PATTERN + suffix))
                .set(SpreadsheetMetadataPropertyName.TIME_PARSE_PATTERN, SpreadsheetParsePattern.parseTimeParsePattern(TIME_PATTERN))
                .set(SpreadsheetMetadataPropertyName.STYLE, TextStyle.EMPTY
                        .set(TextStylePropertyName.WIDTH, Length.parsePixels(COLUMN_WIDTH + "px"))
                        .set(TextStylePropertyName.HEIGHT, Length.parsePixels(ROW_HEIGHT + "px"))
                );
    }

    private SpreadsheetColumnReference column(final int column) {
        return SpreadsheetReferenceKind.ABSOLUTE.column(column);
    }

    private SpreadsheetRowReference row(final int row) {
        return SpreadsheetReferenceKind.ABSOLUTE.row(row);
    }

    private SpreadsheetCellReference cellReference(final String reference) {
        return SpreadsheetSelection.parseCell(reference);
    }

    private SpreadsheetCellReference cellReference(final int column, final int row) {
        return SpreadsheetReferenceKind.ABSOLUTE.column(column).setRow(SpreadsheetReferenceKind.ABSOLUTE.row(row));
    }

    private SpreadsheetCell cell(final String reference, final String formula) {
        return this.cell(SpreadsheetSelection.parseCell(reference), formula);
    }

    private SpreadsheetCell cell(final SpreadsheetCellReference reference,
                                 final String formula) {
        return this.cell(
                reference,
                SpreadsheetFormula.EMPTY
                        .setText(formula)
        );
    }

    private SpreadsheetCell cell(final SpreadsheetCellReference reference, final SpreadsheetFormula formula) {
        return reference.setFormula(formula)
                .setStyle(this.style());
    }

    private TextStyle style() {
        return TextStyle.with(Maps.of(TextStylePropertyName.FONT_WEIGHT, FontWeight.BOLD));
    }

    private void addFailingCellSaveWatcherAndDeleteWatcher(final SpreadsheetEngineContext context) {
        final SpreadsheetCellStore store = context.storeRepository()
                .cells();

        store.addSaveWatcher((ignored) -> {
            throw new UnsupportedOperationException();
        });
        store.addDeleteWatcher((ignored) -> {
            throw new UnsupportedOperationException();
        });
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
