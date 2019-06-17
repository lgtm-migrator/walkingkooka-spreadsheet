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

package walkingkooka.spreadsheet.format;

import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatAmPmParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatDayParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatEscapeParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatHourParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatMonthOrMinuteParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatParserTokenVisitor;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatQuotedTextParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatSecondParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatTextLiteralParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatYearParserToken;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * This visitor is used exclusively by {@link TextSpreadsheetTextFormatter#format(Object, SpreadsheetTextFormatContext)}.
 * Only some methods in {@link SpreadsheetFormatParserTokenVisitor} are overridden, all other tokens will be ignored.
 */
final class LocalDateTimeSpreadsheetTextFormatterFormattingSpreadsheetFormatParserTokenVisitor extends TextFormatterSpreadsheetFormatParserTokenVisitor {

    /**
     * Visits all the individual tokens in the given token which was compiled from the given pattern.
     */
    static Optional<SpreadsheetFormattedText> format(final SpreadsheetFormatParserToken token,
                                                     final LocalDateTime value,
                                                     final SpreadsheetTextFormatContext context,
                                                     final boolean twelveHourTime) {
        final LocalDateTimeSpreadsheetTextFormatterFormattingSpreadsheetFormatParserTokenVisitor visitor = new LocalDateTimeSpreadsheetTextFormatterFormattingSpreadsheetFormatParserTokenVisitor(value,
                context,
                twelveHourTime);
        visitor.accept(token);
        return Optional.of(SpreadsheetFormattedText.with(SpreadsheetFormattedText.WITHOUT_COLOR, visitor.text.toString()));
    }

    /**
     * Private ctor use static method.
     */
    // @VisibleForTesting
    LocalDateTimeSpreadsheetTextFormatterFormattingSpreadsheetFormatParserTokenVisitor(final LocalDateTime value,
                                                                                       final SpreadsheetTextFormatContext context,
                                                                                       final boolean twelveHourTime) {
        super();
        this.context = context;
        this.value = value;
        this.twelveHourTime = twelveHourTime;
        this.month = true;
    }

    private final SpreadsheetTextFormatContext context;

    @Override
    protected void visit(final SpreadsheetFormatAmPmParserToken token) {
        text.append(context.ampm(value.getHour()));
    }

    @Override
    protected void visit(final SpreadsheetFormatDayParserToken token) {
        final int day = this.value.getDayOfMonth();
        switch (token.text().length()) {
            case 1:
                this.append(day);
                break;
            case 2:
                this.appendWithLeadingZero(day);
                break;
            case 3:
                this.append(this.context.weekDayNameAbbreviation(day));
                break;
            default:
                this.append(this.context.weekDayName(day));
                break;
        }
        this.month = true;
    }

    @Override
    protected void visit(final SpreadsheetFormatEscapeParserToken token) {
        this.append(token.value());
    }

    @Override
    protected void visit(final SpreadsheetFormatHourParserToken token) {
        final int hour = this.value.getHour();
        final String pattern = token.text();

        if (this.twelveHourTime) {
            this.visit12(hour, pattern);
        } else {
            this.visit24(hour, pattern);
        }

        this.month = false;
    }

    private final boolean twelveHourTime;

    protected void visit12(final int hour, final String pattern) {
        final int h = hour % 12;
        this.append(0 == h ? 12 : h,
                pattern,
                1);
    }

    protected void visit24(final int hour, final String pattern) {
        this.append(hour, pattern, 1);
    }

    @Override
    protected void visit(final SpreadsheetFormatMonthOrMinuteParserToken token) {
        final String pattern = token.text();
        if (this.month) {
            this.visitMonth(pattern.length());
        } else {
            this.visitMinute(pattern);
        }
    }

    private void visitMonth(final int patternLength) {
        final int month = this.value.getMonthValue();
        switch (patternLength) {
            case 1:
                this.append(month);
                break;
            case 2:
                this.appendWithLeadingZero(month);
                break;
            case 3:
                this.append(this.context.monthNameAbbreviation(month));
                break;
            default:
                this.append(this.context.monthName(month));
                break;
        }
    }

    private void visitMinute(final String pattern) {
        this.append(this.value.getMinute(),
                pattern,
                1);
    }

    private boolean month;

    @Override
    protected void visit(final SpreadsheetFormatQuotedTextParserToken token) {
        this.append(token.value());
    }

    @Override
    protected void visit(final SpreadsheetFormatSecondParserToken token) {
        this.append(this.value.getSecond(),
                token.text(),
                1);
        this.month = true;
    }

    @Override
    protected void visit(final SpreadsheetFormatTextLiteralParserToken token) {
        this.append(token.value());
    }

    @Override
    protected void visit(final SpreadsheetFormatYearParserToken token) {
        final int year = this.value.getYear();
        if (token.text().length() <= 2) {
            this.appendWithLeadingZero(year % 100);
        } else {
            this.append(year);
        }
        this.month = true;
    }

    private void append(final int value, final String pattern, final int without) {
        if (pattern.length() <= without) {
            this.append(value);
        } else {
            this.appendWithLeadingZero(value);
        }
    }

    private void append(final int text) {
        this.text.append(text);
    }

    private void appendWithLeadingZero(final int value) {
        if (value < 10) {
            this.append('0');
        }
        this.append(value);
    }

    private void append(final char c) {
        this.text.append(c);
    }

    private void append(final String text) {
        this.text.append(text);
    }

    /**
     * Aggregates the formatted output text.
     */
    private final StringBuilder text = new StringBuilder();

    /**
     * The date/time.
     */
    private final LocalDateTime value;

    @Override
    public String toString() {
        return this.text.toString();
    }
}