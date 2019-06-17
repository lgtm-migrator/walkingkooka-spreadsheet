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

import walkingkooka.Cast;
import walkingkooka.text.cursor.parser.ParserToken;

import java.util.List;

/**
 * Base class for any token with two parameters.
 */
abstract class SpreadsheetBinaryParserToken<T extends SpreadsheetBinaryParserToken> extends SpreadsheetParentParserToken<T> {

    SpreadsheetBinaryParserToken(final List<ParserToken> value,
                                 final String text,
                                 final List<ParserToken> valueWithout) {
        super(value, text, valueWithout);

        final List<ParserToken> without = Cast.to(SpreadsheetParentParserToken.class.cast(this.withoutSymbols().get()).value());
        final int count = without.size();
        if (2 != count) {
            throw new IllegalArgumentException("Expected 2 tokens but got " + count + "=" + without);
        }
        this.left = without.get(0).cast();
        this.right = without.get(1).cast();
    }

    /**
     * Returns the left parameter.
     */
    public final SpreadsheetParserToken left() {
        return this.left;
    }

    final SpreadsheetParserToken left;

    /**
     * Returns the right parameter.
     */
    public final SpreadsheetParserToken right() {
        return this.right;
    }

    final SpreadsheetParserToken right;

    // isXXX............................................................................................................

    @Override
    public final boolean isCellReference() {
        return false;
    }

    @Override
    public final boolean isFunction() {
        return false;
    }

    @Override
    public final boolean isGroup() {
        return false;
    }

    @Override
    public final boolean isNegative() {
        return false;
    }

    @Override
    public final boolean isPercentage() {
        return false;
    }
}