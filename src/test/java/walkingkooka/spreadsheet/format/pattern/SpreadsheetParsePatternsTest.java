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
import walkingkooka.Cast;
import walkingkooka.collect.list.Lists;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatDateParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatDateTimeParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatNumberParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatParserContexts;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatParsers;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatSeparatorSymbolParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatTimeParserToken;
import walkingkooka.text.CharSequences;
import walkingkooka.text.cursor.TextCursors;
import walkingkooka.text.cursor.parser.ParserReporters;
import walkingkooka.text.cursor.parser.ParserTokens;
import walkingkooka.text.printer.TreePrintableTesting;

import java.util.function.Function;

public final class SpreadsheetParsePatternsTest implements ClassTesting2<SpreadsheetParsePatterns>, TreePrintableTesting {

    @Test
    public void testParseDate() {
        this.parseAndCheck(
                "dmyy",
                SpreadsheetParsePatterns::parseDateParsePatterns,
                this.dmyy()
        );
    }

    @Test
    public void testParseDateTwoPatterns() {
        this.parseAndCheck(
                "dmyy;ddmmyyyy",
                SpreadsheetParsePatterns::parseDateParsePatterns,
                this.dmyy(),
                this.separator(),
                this.ddmmyyyy()
        );
    }

    @Test
    public void testParseDateTime() {
        this.parseAndCheck(
                "hhmmyyyy",
                SpreadsheetParsePatterns::parseDateTimeParsePatterns,
                this.hhmmyyyy()
        );
    }

    @Test
    public void testParseDateTimeTwoPatterns() {
        this.parseAndCheck(
                "hhmmyyyy;yyyymmhh",
                SpreadsheetParsePatterns::parseDateTimeParsePatterns,
                this.hhmmyyyy(),
                this.separator(),
                this.yyyymmhh()
        );
    }

    @Test
    public void testParseNumber() {
        this.parseAndCheck(
                "#0.0",
                SpreadsheetParsePatterns::parseNumberParsePatterns,
                this.number()
        );
    }

    @Test
    public void testParseNumberTwoPatterns() {
        this.parseAndCheck(
                "#0.0;$ #0.00",
                SpreadsheetParsePatterns::parseNumberParsePatterns,
                this.number(),
                this.separator(),
                this.money()
        );
    }

    @Test
    public void testParseTime() {
        this.parseAndCheck(
                "hhmm",
                SpreadsheetParsePatterns::parseTimeParsePatterns,
                this.hhmm()
        );
    }

    @Test
    public void testParseTimeTwoPatterns() {
        this.parseAndCheck(
                "hhmm;hhmmss",
                SpreadsheetParsePatterns::parseTimeParsePatterns,
                this.hhmm(),
                this.separator(),
                this.hhmmss()
        );
    }

    private void parseAndCheck(final String text,
                               final Function<String, SpreadsheetParsePatterns> parse,
                               final SpreadsheetFormatParserToken... tokens) {
        this.checkEquals(
                ParserTokens.sequence(
                        Lists.of(tokens),
                        text
                ),
                parse.apply(text)
                        .value(),
                () -> "parse " + CharSequences.quoteAndEscape(text)
        );
    }

    private SpreadsheetFormatDateParserToken dmyy() {
        return this.parseDateParserToken("dmyy");
    }

    private SpreadsheetFormatDateParserToken ddmmyyyy() {
        return this.parseDateParserToken("ddmmyyyy");
    }

    private SpreadsheetFormatDateParserToken parseDateParserToken(final String text) {
        return SpreadsheetFormatParsers.date()
                .orFailIfCursorNotEmpty(ParserReporters.basic())
                .parse(TextCursors.charSequence(text), SpreadsheetFormatParserContexts.basic())
                .map(SpreadsheetFormatDateParserToken.class::cast)
                .get();
    }

    private SpreadsheetFormatDateTimeParserToken hhmmyyyy() {
        return this.parseDateTimeParserToken("hhmmyyyy");
    }

    private SpreadsheetFormatDateTimeParserToken yyyymmhh() {
        return this.parseDateTimeParserToken("yyyymmhh");
    }

    private SpreadsheetFormatDateTimeParserToken parseDateTimeParserToken(final String text) {
        return SpreadsheetFormatParsers.dateTime()
                .orFailIfCursorNotEmpty(ParserReporters.basic())
                .parse(TextCursors.charSequence(text), SpreadsheetFormatParserContexts.basic())
                .map(SpreadsheetFormatDateTimeParserToken.class::cast)
                .get();
    }

    private SpreadsheetFormatNumberParserToken number() {
        return this.parseNumberParserToken("#0.0");
    }

    private SpreadsheetFormatNumberParserToken money() {
        return this.parseNumberParserToken("$ #0.00");
    }

    private SpreadsheetFormatNumberParserToken parseNumberParserToken(final String text) {
        return SpreadsheetFormatParsers.number()
                .orFailIfCursorNotEmpty(ParserReporters.basic())
                .parse(TextCursors.charSequence(text), SpreadsheetFormatParserContexts.basic())
                .map(SpreadsheetFormatNumberParserToken.class::cast)
                .get();
    }

    private SpreadsheetFormatTimeParserToken hhmm() {
        return this.parseTimeParserToken("hhmm");
    }

    private SpreadsheetFormatTimeParserToken hhmmss() {
        return this.parseTimeParserToken("hhmmss");
    }

    private SpreadsheetFormatTimeParserToken parseTimeParserToken(final String text) {
        return SpreadsheetFormatParsers.time()
                .orFailIfCursorNotEmpty(ParserReporters.basic())
                .parse(TextCursors.charSequence(text), SpreadsheetFormatParserContexts.basic())
                .map(SpreadsheetFormatTimeParserToken.class::cast)
                .get();
    }

    private SpreadsheetFormatSeparatorSymbolParserToken separator() {
        return SpreadsheetFormatParserToken.separatorSymbol(
                ";",
                ";"
        );
    }

    @Override
    public Class<SpreadsheetParsePatterns> type() {
        return Cast.to(SpreadsheetParsePatterns.class);
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
