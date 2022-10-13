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
import walkingkooka.spreadsheet.SpreadsheetError;
import walkingkooka.spreadsheet.SpreadsheetErrorKind;
import walkingkooka.text.cursor.parser.ParserToken;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.visit.Visiting;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetErrorParserTokenTest extends SpreadsheetNonSymbolParserTokenTestCase<SpreadsheetErrorParserToken, SpreadsheetError> {

    @Test
    public void testWithEmptyTextFails() {
        assertThrows(IllegalArgumentException.class, () -> this.createToken(""));
    }

    @Test
    public void testAccept() {
        final StringBuilder b = new StringBuilder();
        final SpreadsheetErrorParserToken token = this.createToken();

        new FakeSpreadsheetParserTokenVisitor() {
            @Override
            protected Visiting startVisit(final ParserToken t) {
                assertSame(token, t);
                b.append("1");
                return Visiting.CONTINUE;
            }

            @Override
            protected void endVisit(final ParserToken t) {
                assertSame(token, t);
                b.append("2");
            }

            @Override
            protected Visiting startVisit(final SpreadsheetParserToken t) {
                assertSame(token, t);
                b.append("3");
                return Visiting.CONTINUE;
            }

            @Override
            protected void endVisit(final SpreadsheetParserToken t) {
                assertSame(token, t);
                b.append("4");
            }

            @Override
            protected void visit(final SpreadsheetErrorParserToken t) {
                assertSame(token, t);
                b.append("5");
            }
        }.accept(token);
        this.checkEquals("13542", b.toString());
    }

    @Test
    public void testToExpression() {
        this.toExpressionAndCheck(
                this.createToken(),
                Expression.call(
                        Expression.namedFunction(
                                FunctionExpressionName.with("error")
                        ),
                        Lists.of(
                                Expression.value(
                                        EXPRESSION_NUMBER_KIND.create(4) // #REF
                                )
                        )
                )
        );
    }

    @Override
    public String text() {
        return "sum";
    }

    @Override
    SpreadsheetError value() {
        return SpreadsheetErrorKind.REF.toError();
    }

    @Override
    SpreadsheetErrorParserToken createToken(final SpreadsheetError value, final String text) {
        return SpreadsheetErrorParserToken.with(value, text);
    }

    @Override
    public SpreadsheetErrorParserToken createDifferentToken() {
        final SpreadsheetError error = SpreadsheetErrorKind.DIV0.toError();

        return SpreadsheetErrorParserToken.with(
                error,
                error.kind().text()
        );
    }

    @Override
    public Class<SpreadsheetErrorParserToken> type() {
        return SpreadsheetErrorParserToken.class;
    }

    @Override
    public SpreadsheetErrorParserToken unmarshall(final JsonNode from,
                                                  final JsonNodeUnmarshallContext context) {
        return SpreadsheetParserToken.unmarshallError(from, context);
    }
}
