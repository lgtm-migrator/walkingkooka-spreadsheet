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

package walkingkooka.spreadsheet.format.pattern;

import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.cursor.TextCursor;

/**
 * A {@link SpreadsheetNumberParsePatternsComponent} that matches any decimal separators.
 */
final class SpreadsheetNumberParsePatternsComponentPercent extends SpreadsheetNumberParsePatternsComponent2 {

    /**
     * Singleton
     */
    final static SpreadsheetNumberParsePatternsComponentPercent INSTANCE = new SpreadsheetNumberParsePatternsComponentPercent();

    private SpreadsheetNumberParsePatternsComponentPercent() {
        super();
    }

    @Override
    boolean parse(final TextCursor cursor,
               final SpreadsheetNumberParsePatternsRequest request) {
        final char percentSymbol = request.context.percentageSymbol();
        return this.parseToken(
                cursor,
                Character.toString(percentSymbol),
                CaseSensitivity.SENSITIVE,
                SpreadsheetParserToken::percentSymbol,
                null, // dont update mode
                request
        );
    }

    @Override
    public String toString() {
        return ".";
    }
}
