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

import walkingkooka.text.cursor.parser.ParserToken;
import walkingkooka.tree.visit.Visiting;

import java.util.List;

/**
 * A wrapper around a numeric type that is also a percentage.
 */
public final class SpreadsheetPercentageParserToken extends SpreadsheetUnaryParserToken<SpreadsheetPercentageParserToken> {

    static SpreadsheetPercentageParserToken with(final List<ParserToken> value,
                                                 final String text) {
        return new SpreadsheetPercentageParserToken(copyAndCheckTokens(value),
                checkText(text),
                WITHOUT_COMPUTE_REQUIRED);
    }

    private SpreadsheetPercentageParserToken(final List<ParserToken> value,
                                             final String text,
                                             final List<ParserToken> valueWithout) {
        super(value, text, valueWithout);
    }

    @Override
    SpreadsheetParentParserToken replace(final List<ParserToken> tokens,
                                         final List<ParserToken> without) {
        return new SpreadsheetPercentageParserToken(tokens,
                text,
                without);
    }

    // isXXX............................................................................................................

    @Override
    public boolean isNegative() {
        return false;
    }

    @Override
    public boolean isPercentage() {
        return true;
    }

    // SpreadsheetParserTokenVisitor....................................................................................

    @Override
    public void accept(final SpreadsheetParserTokenVisitor visitor) {
        if (Visiting.CONTINUE == visitor.startVisit(this)) {
            this.acceptValues(visitor);
        }
        visitor.endVisit(this);
    }

    // Object...........................................................................................................

    @Override
    boolean canBeEqual(final Object other) {
        return other instanceof SpreadsheetPercentageParserToken;
    }
}