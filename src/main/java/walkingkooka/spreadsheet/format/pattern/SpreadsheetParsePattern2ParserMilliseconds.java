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

import walkingkooka.spreadsheet.parser.SpreadsheetMillisecondParserToken;
import walkingkooka.spreadsheet.parser.SpreadsheetParserContext;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursorSavePoint;
import walkingkooka.text.cursor.parser.Parser;

import java.time.LocalTime;

/**
 * A {@link Parser} that matches the milliseconds and returns a {@link walkingkooka.spreadsheet.parser.SpreadsheetMillisecondParserToken}
 */
final class SpreadsheetParsePattern2ParserMilliseconds extends SpreadsheetParsePattern2Parser {

    /**
     * Singleton instance
     */
    static SpreadsheetParsePattern2ParserMilliseconds with(final String pattern) {
        return new SpreadsheetParsePattern2ParserMilliseconds(pattern);
    }

    private SpreadsheetParsePattern2ParserMilliseconds(final String pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    SpreadsheetMillisecondParserToken parseNotEmpty0(final TextCursor cursor,
                                                     final SpreadsheetParserContext context,
                                                     final TextCursorSavePoint start) {
        SpreadsheetMillisecondParserToken token = null;
        double digitValue = FIRST_DIGIT;
        double value = 0;


        for (; ; ) {
            final char c = cursor.at();
            final int digit = Character.digit(c, 10);
            if (-1 == digit) {
                token = digitValue != FIRST_DIGIT ?
                        token(value, start) :
                        null;
                break;
            }
            value += digit * digitValue;

            cursor.next();
            if (cursor.isEmpty()) {
                token = token(value, start);
                break;
            }

            digitValue = digitValue * 0.1f;
        }

        return token;
    }

    private final static long FIRST_DIGIT = LocalTime.of(0, 0, 1).toNanoOfDay() / 10;

    private static SpreadsheetMillisecondParserToken token(final double value,
                                                           final TextCursorSavePoint start) {
        return SpreadsheetParserToken.millisecond(
                (int) Math.round(value), // shouldnt overload
                start.textBetween().toString()
        );
    }

    @Override
    public String toString() {
        return this.pattern;
    }

    /**
     * Pattern ignored during parsing
     */
    private final String pattern;
}
