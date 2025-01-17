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

import walkingkooka.spreadsheet.parser.SpreadsheetNumberParserToken;
import walkingkooka.spreadsheet.parser.SpreadsheetParserContext;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursorSavePoint;
import walkingkooka.text.cursor.parser.Parser;
import walkingkooka.text.cursor.parser.ParserToken;

import java.util.List;
import java.util.Optional;

/**
 * The {@link Parser} returned by {@link SpreadsheetNumberParsePattern#converter()}.
 */
final class SpreadsheetNumberParsePatternParser implements Parser<SpreadsheetParserContext> {

    static SpreadsheetNumberParsePatternParser with(final SpreadsheetNumberParsePattern pattern,
                                                    final SpreadsheetNumberParsePatternMode mode) {
        return new SpreadsheetNumberParsePatternParser(
                pattern,
                mode
        );
    }

    private SpreadsheetNumberParsePatternParser(final SpreadsheetNumberParsePattern pattern,
                                                final SpreadsheetNumberParsePatternMode mode) {
        super();
        this.pattern = pattern;
        this.mode = mode;

        this.mode.checkCompatible(pattern);
    }

    @Override
    public Optional<ParserToken> parse(final TextCursor cursor,
                                       final SpreadsheetParserContext context) {
        SpreadsheetNumberParserToken token = null;

        final TextCursorSavePoint save = cursor.save();

        for (final List<SpreadsheetNumberParsePatternComponent> pattern : this.pattern.patterns) {
            final SpreadsheetNumberParsePatternRequest request = SpreadsheetNumberParsePatternRequest.with(
                    pattern.iterator(),
                    this.mode,
                    context
            );
            if (request.nextComponent(cursor)) {
                final List<ParserToken> tokens = request.tokens;
                if (!tokens.isEmpty()) {
                    token = SpreadsheetNumberParserToken.number(
                            tokens,
                            save.textBetween().toString()
                    );
                    break;
                }
            }
            save.restore();
        }

        return Optional.ofNullable(token);
    }

    private final SpreadsheetNumberParsePatternMode mode;

    @Override
    public String toString() {
        return this.pattern.toString();
    }

    /**
     * The enclosing {@link SpreadsheetNumberParsePattern}.
     */
    private final SpreadsheetNumberParsePattern pattern;
}
