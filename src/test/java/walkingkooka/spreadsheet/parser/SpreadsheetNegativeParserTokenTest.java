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

package walkingkooka.spreadsheet.parser;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.text.cursor.parser.ParserToken;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.visit.Visiting;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetNegativeParserTokenTest extends SpreadsheetParentParserTokenTestCase<SpreadsheetNegativeParserToken> {

    @Test
    public void testWithMissingNonNoisyTokenFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.createToken("", this.whitespace())
        );
    }

    @Test
    public void testWithMissingNonNoisyTokenFails2() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.createToken("", this.whitespace(), this.whitespace())
        );
    }

    @Test
    public void testAccept() {
        final StringBuilder b = new StringBuilder();
        final List<ParserToken> visited = Lists.array();

        final SpreadsheetNegativeParserToken negative = this.createToken();
        final SpreadsheetParserToken symbol = this.minusSymbol();
        final SpreadsheetParserToken parameter = negative.parameter();

        new FakeSpreadsheetParserTokenVisitor() {
            @Override
            protected Visiting startVisit(final SpreadsheetParserToken n) {
                b.append("1");
                visited.add(n);
                return Visiting.CONTINUE;
            }

            @Override
            protected void endVisit(final SpreadsheetParserToken n) {
                b.append("2");
                visited.add(n);
            }

            @Override
            protected Visiting startVisit(final SpreadsheetNegativeParserToken t) {
                assertSame(negative, t);
                b.append("3");
                visited.add(t);
                return Visiting.CONTINUE;
            }

            @Override
            protected void endVisit(final SpreadsheetNegativeParserToken t) {
                assertSame(negative, t);
                b.append("4");
                visited.add(t);
            }


            @Override
            protected Visiting startVisit(final SpreadsheetNumberParserToken t) {
                b.append("5");
                visited.add(t);
                return Visiting.SKIP;
            }

            @Override
            protected void endVisit(final SpreadsheetNumberParserToken t) {
                b.append("6");
                visited.add(t);
            }

            @Override
            protected void visit(final SpreadsheetMinusSymbolParserToken t) {
                b.append("7");
                visited.add(t);
            }

            @Override
            protected Visiting startVisit(final ParserToken t) {
                b.append("8");
                visited.add(t);
                return Visiting.CONTINUE;
            }

            @Override
            protected void endVisit(final ParserToken t) {
                b.append("8");
                visited.add(t);
            }
        }.accept(negative);
        this.checkEquals("81381728815628428", b.toString());
        this.checkEquals(Lists.of(negative, negative, negative,
                        symbol, symbol, symbol, symbol, symbol,
                        parameter, parameter, parameter, parameter, parameter, parameter,
                        negative, negative, negative),
                visited,
                "visited");
    }

    @Test
    public void testToExpression() {
        this.toExpressionAndCheck(
                Expression.negative(
                        expression1()
                )
        );
    }

    @Override
    SpreadsheetNegativeParserToken createToken(final String text, final List<ParserToken> tokens) {
        return SpreadsheetParserToken.negative(tokens, text);
    }

    @Override
    public String text() {
        return "-" + NUMBER1;
    }

    @Override
    List<ParserToken> tokens() {
        return Lists.of(this.minusSymbol(), this.number1());
    }

    @Override
    public SpreadsheetNegativeParserToken createDifferentToken() {
        return this.createToken("-" + NUMBER2, this.minusSymbol(), this.number2());
    }

    @Override
    public Class<SpreadsheetNegativeParserToken> type() {
        return SpreadsheetNegativeParserToken.class;
    }

    @Override
    public SpreadsheetNegativeParserToken unmarshall(final JsonNode from,
                                                     final JsonNodeUnmarshallContext context) {
        return SpreadsheetParserToken.unmarshallNegative(from, context);
    }
}
