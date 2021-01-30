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

package walkingkooka.spreadsheet.meta;

import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatDateTimeParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatParserToken;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetDateTimeParsePatterns;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;

import java.text.DateFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

final class SpreadsheetMetadataPropertyNameSpreadsheetDateTimeParsePatterns extends SpreadsheetMetadataPropertyName<SpreadsheetDateTimeParsePatterns> {

    /**
     * Singleton
     */
    final static SpreadsheetMetadataPropertyNameSpreadsheetDateTimeParsePatterns instance() {
        return new SpreadsheetMetadataPropertyNameSpreadsheetDateTimeParsePatterns();
    }

    /**
     * Private constructor use singleton.
     */
    private SpreadsheetMetadataPropertyNameSpreadsheetDateTimeParsePatterns() {
        super("date-time-parse-patterns");
    }

    @Override
    SpreadsheetDateTimeParsePatterns checkValue0(final Object value) {
        return this.checkValueType(value,
                v -> v instanceof SpreadsheetDateTimeParsePatterns);
    }

    @Override
    String expected() {
        return "DateTime parse patterns";
    }

    @Override
    void accept(final SpreadsheetDateTimeParsePatterns value,
                final SpreadsheetMetadataVisitor visitor) {
        visitor.visitDateTimeParsePatterns(value);
    }

    @Override
    Optional<SpreadsheetDateTimeParsePatterns> extractLocaleValue(final Locale locale) {
        final StringBuilder pattern = new StringBuilder();

        String separator = "";

        for (final int dateStyle : styles) {
            for (final int timeStyle : styles) {
                pattern.append(separator);
                pattern.append(toPattern(DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale)));
                separator = ";";
            }
        }

        return Optional.of(SpreadsheetMetadataPropertyNameSpreadsheetDateTimeParsePatterns.parseDateTimeParsePatterns(pattern.toString()));
    }

    private final static int[] styles = new int[]{DateFormat.FULL, DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT};

    private static SpreadsheetDateTimeParsePatterns parseDateTimeParsePatterns(final String text) {
        final SpreadsheetDateTimeParsePatterns pattern = SpreadsheetPattern.parseDateTimeParsePatterns(text);

        return SpreadsheetPattern.dateTimeParsePatterns(
                pattern
                        .value()
                        .stream()
                        .map(SpreadsheetMetadataPropertyNameSpreadsheetDateTimeParsePatterns::parseDateTimeParsePatterns0)
                        .collect(Collectors.toList())
        );
    }

    private static SpreadsheetFormatDateTimeParserToken parseDateTimeParsePatterns0(final SpreadsheetFormatDateTimeParserToken token) {
        return SpreadsheetMetadataPropertyNameSpreadsheetTimeParsePatternsSpreadsheetFormatParserTokenVisitor.fix(
                token,
                SpreadsheetFormatParserToken::dateTime
        );
    }

    @Override
    Class<SpreadsheetDateTimeParsePatterns> type() {
        return SpreadsheetDateTimeParsePatterns.class;
    }
}
