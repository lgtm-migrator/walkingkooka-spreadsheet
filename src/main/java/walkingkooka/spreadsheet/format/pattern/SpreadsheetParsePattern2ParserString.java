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

import walkingkooka.spreadsheet.parser.SpreadsheetParserContext;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursorSavePoint;
import walkingkooka.text.cursor.parser.Parser;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link Parser} that handles partial matches from a list taken from the {@link SpreadsheetParserContext}.
 */
final class SpreadsheetParsePattern2ParserString extends SpreadsheetParsePattern2Parser {

    static SpreadsheetParsePattern2ParserString with(final Function<SpreadsheetParserContext, List<String>> values,
                                                     final BiFunction<Integer, String, SpreadsheetParserToken> tokenFactory,
                                                     final String pattern) {
        return new SpreadsheetParsePattern2ParserString(
                values,
                tokenFactory,
                pattern
        );
    }

    private SpreadsheetParsePattern2ParserString(final Function<SpreadsheetParserContext, List<String>> values,
                                                 final BiFunction<Integer, String, SpreadsheetParserToken> tokenFactory,
                                                 final String pattern) {
        super();
        this.values = values;
        this.tokenFactory = tokenFactory;
        this.pattern = pattern;
    }

    @Override
    SpreadsheetParserToken parseNotEmpty0(final TextCursor cursor,
                                          final SpreadsheetParserContext context,
                                          final TextCursorSavePoint start) {
        SpreadsheetParserToken token = null;

        final List<String> list = this.values.apply(context);
        int count = list.size();
        final String[] values = list.toArray(new String[count]);

        int i = 0;

        Exit:
//
        for (; ; ) {
            final char c = cursor.at();

            int candidates = 0;
            int choice = -1;
            String choiceText = null;

            for (int j = 0; j < values.length; j++) {
                final String possible = values[j];

                if (null != possible) {
                    if (i < possible.length() && isEqual(possible.charAt(i), c)) {
                        candidates++;
                        if (1 == candidates) {
                            choice = j;
                            choiceText = possible;
                        }
                    } else {
                        values[j] = null; // no match ignore
                    }
                }
            }

            i++;
            switch (candidates) {
                case 0:
                    break Exit;
                case 1:
                    cursor.next();
                    TextCursorSavePoint save = cursor.save();

                    for (; ; ) {
                        if (cursor.isEmpty() || i == choiceText.length()) { // lgtm [java/dereferenced-value-may-be-null]
                            token = this.token(choice, start);
                            break Exit;
                        }
                        if (!isEqual(choiceText.charAt(i), cursor.at())) {
                            save.restore();
                            token = this.token(choice, start);
                            break Exit;
                        }
                        save = cursor.save();
                        cursor.next();
                        i++;
                    }
                default:
                    cursor.next();
                    if (cursor.isEmpty()) {
                        break Exit;
                    }
                    // keep trying remaining candidates
                    break;
            }
        }

        return token;
    }

    private static boolean isEqual(final char c, final char d) {
        return CaseSensitivity.INSENSITIVE.isEqual(c, d);
    }

    /**
     * This provides the list of month names, month names abbreviations or am/pms.
     */
    private final Function<SpreadsheetParserContext, List<String>> values;

    private SpreadsheetParserToken token(final int choice,
                                         final TextCursorSavePoint start) {
        return this.tokenFactory.apply(
                choice,
                start.textBetween().toString()
        );
    }

    /**
     * Factory that creates the {@link SpreadsheetParserToken}. This is typically a method-reference to a static
     * {@link SpreadsheetParserToken} factory method.
     */
    private final BiFunction<Integer, String, SpreadsheetParserToken> tokenFactory;

    @Override
    public String toString() {
        return this.pattern;
    }

    private final String pattern;
}
