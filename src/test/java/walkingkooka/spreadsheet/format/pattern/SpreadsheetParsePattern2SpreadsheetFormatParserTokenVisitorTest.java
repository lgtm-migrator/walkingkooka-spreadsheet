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

import org.junit.jupiter.api.Test;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatParserTokenVisitorTesting;
import walkingkooka.text.cursor.parser.SequenceParserToken;

public final class SpreadsheetParsePattern2SpreadsheetFormatParserTokenVisitorTest
        extends SpreadsheetParsePattern2TestCase<SpreadsheetParsePattern2SpreadsheetFormatParserTokenVisitor>
        implements SpreadsheetFormatParserTokenVisitorTesting<SpreadsheetParsePattern2SpreadsheetFormatParserTokenVisitor> {

    @Override
    public void testIfClassIsFinalIfAllConstructorsArePrivate() {
    }

    @Override
    public void testAllConstructorsVisibility() {
    }

    // ToString.........................................................................................................

    @Test
    public void testToString() {
        final SpreadsheetFormatParserToken token = this.parserToken("yyyymmdd");

        final SpreadsheetParsePattern2SpreadsheetFormatParserTokenVisitor visitor = new SpreadsheetParsePattern2SpreadsheetFormatParserTokenVisitor();
        visitor.accept(token);

        this.toStringAndCheck(
                visitor,
                "[yyyymmdd]"
        );
    }

    private SpreadsheetFormatParserToken parserToken(final String pattern) {
        final SpreadsheetParsePattern date = SpreadsheetParsePattern.parseDateParsePattern(pattern);
        final SequenceParserToken sequenceParserToken = (SequenceParserToken) date.value();
        return sequenceParserToken.value()
                .get(0)
                .cast(SpreadsheetFormatParserToken.class);
    }

    @Override
    public SpreadsheetParsePattern2SpreadsheetFormatParserTokenVisitor createVisitor() {
        return new SpreadsheetParsePattern2SpreadsheetFormatParserTokenVisitor();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetParsePattern2SpreadsheetFormatParserTokenVisitor> type() {
        return SpreadsheetParsePattern2SpreadsheetFormatParserTokenVisitor.class;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetParsePattern2.class.getSimpleName();
    }
}
