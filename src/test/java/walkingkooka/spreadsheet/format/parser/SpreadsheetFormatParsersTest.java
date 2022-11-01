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

package walkingkooka.spreadsheet.format.parser;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicStaticHelperTesting;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursorSavePoint;
import walkingkooka.text.cursor.TextCursors;
import walkingkooka.text.cursor.parser.Parser;
import walkingkooka.text.cursor.parser.ParserReporters;
import walkingkooka.text.cursor.parser.ParserTesting2;
import walkingkooka.text.cursor.parser.ParserToken;
import walkingkooka.text.cursor.parser.Parsers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class SpreadsheetFormatParsersTest extends SpreadsheetFormatParserTestCase implements PublicStaticHelperTesting<SpreadsheetFormatParsers>,
        ParserTesting2<Parser<SpreadsheetFormatParserContext>, SpreadsheetFormatParserContext> {

    // snakeCaseParserClassSimpleName...................................................................................

    @Test
    public void testSnakeCaseParserClassSimpleNameSpreadsheetFormatFractionSymbolParserToken() {
        this.snakeCaseParserClassSimpleNameAndCheck(SpreadsheetFormatFractionSymbolParserToken.class, "FRACTION");
    }

    @Test
    public void testSnakeCaseParserClassSimpleNameSpreadsheetFormatGreaterThanSymbolParserToken() {
        this.snakeCaseParserClassSimpleNameAndCheck(SpreadsheetFormatGreaterThanSymbolParserToken.class, "GREATER_THAN");
    }

    @Test
    public void testSnakeCaseParserClassSimpleNameSpreadsheetFormatGreaterThanEqualsSymbolParserToken() {
        this.snakeCaseParserClassSimpleNameAndCheck(SpreadsheetFormatGreaterThanEqualsSymbolParserToken.class, "GREATER_THAN_EQUALS");
    }

    @Test
    public void testSnakeCaseParserClassSimpleNameSpreadsheetFormatEscapeParserToken() {
        this.snakeCaseParserClassSimpleNameAndCheck(SpreadsheetFormatEscapeParserToken.class, "ESCAPE");
    }

    @Test
    public void testSnakeCaseParserClassSimpleNameSpreadsheetFormatStarParserToken() {
        this.snakeCaseParserClassSimpleNameAndCheck(SpreadsheetFormatStarParserToken.class, "STAR");
    }

    @Test
    public void testSnakeCaseParserClassSimpleNameSpreadsheetFormatUnderscoreParserToken() {
        this.snakeCaseParserClassSimpleNameAndCheck(SpreadsheetFormatUnderscoreParserToken.class, "UNDERSCORE");
    }

    private void snakeCaseParserClassSimpleNameAndCheck(final Class<? extends SpreadsheetFormatParserToken> type,
                                                        final String expected) {
        this.checkEquals(expected,
                SpreadsheetFormatParsers.snakeCaseParserClassSimpleName(type),
                () -> "snakeCaseParserClassSimpleName " + type.getSimpleName());
    }

    // color............................................................................................................

    @Test
    public void testColorDigitFails() {
        this.colorThrows(digit());
    }

    @Test
    public void testColorDigitZeroFails() {
        this.colorThrows(digitZero());
    }

    @Test
    public void testColorDigitSpaceFails() {
        this.colorThrows(digitSpace());
    }

    @Test
    public void testColorHourFails() {
        this.colorThrows(hour());
    }

    @Test
    public void testColorMonthOrMinuteFails() {
        this.colorThrows(monthOrMinute());
    }

    @Test
    public void testColorSecondFails() {
        this.colorThrows(second());
    }

    @Test
    public void testColorDayFails() {
        this.colorThrows(day());
    }

    @Test
    public void testColorYearFails() {
        this.colorThrows(year());
    }

    @Test
    public void testColorName() {
        this.colorParseAndCheck(
                bracketOpenSymbol(),
                red(),
                bracketCloseSymbol()
        );
    }

    @Test
    public void testColorNameWhitespace() {
        this.colorParseAndCheck(
                bracketOpenSymbol(),
                red(),
                whitespace(),
                bracketCloseSymbol()
        );
    }

    @Test
    public void testColorNumber() {
        this.colorParseAndCheck(
                bracketOpenSymbol(),
                colorLiteral(),
                colorNumberFive(),
                bracketCloseSymbol()
        );
    }

    @Test
    public void testColorWhitespaceNumber() {
        this.colorParseAndCheck(
                bracketOpenSymbol(),
                colorLiteral(),
                whitespace(),
                colorNumberFive(),
                bracketCloseSymbol()
        );
    }

    @Test
    public void testColorWhitespaceNumberWhitespace() {
        this.colorParseAndCheck(
                bracketOpenSymbol(),
                colorLiteral(),
                whitespace(),
                colorNumberFive(),
                whitespace(),
                bracketCloseSymbol()
        );
    }

    private void colorParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck3(
                this.colorParser(),
                SpreadsheetFormatParserToken::color,
                tokens
        );
    }

    private void colorThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(
                this.colorParser(),
                tokens
        );
    }

    private Parser<SpreadsheetFormatParserContext> colorParser() {
        return SpreadsheetFormatParsers.color();
    }

    // condition........................................................................................................

    @Test
    public void testConditionCloseParensFails() {
        this.conditionParseThrows(textLiteralCloseParens());
    }

    @Test
    public void testConditionColonFails() {
        this.conditionParseThrows(textLiteralColon());
    }

    @Test
    public void testConditionDayFails() {
        this.conditionParseThrows(day());
    }

    @Test
    public void testConditionDigitFails() {
        this.conditionParseThrows(digit());
    }

    @Test
    public void testConditionDigitZeroFails() {
        this.conditionParseThrows(digitZero());
    }

    @Test
    public void testConditionDigitSpaceFails() {
        this.conditionParseThrows(digitSpace());
    }

    @Test
    public void testConditionDollarFails() {
        this.conditionParseThrows(textLiteralDollar());
    }

    @Test
    public void testConditionFractionFails() {
        this.conditionParseThrows(fractionSymbol());
    }

    @Test
    public void testConditionHourFails() {
        this.conditionParseThrows(hour());
    }

    @Test
    public void testConditionMinusFails() {
        this.conditionParseThrows(textLiteralMinus());
    }

    @Test
    public void testConditionMonthOrMinuteFails() {
        this.conditionParseThrows(monthOrMinute());
    }

    @Test
    public void testConditionOpenParensFails() {
        this.conditionParseThrows(textLiteralOpenParens());
    }

    @Test
    public void testConditionPlusFails() {
        this.conditionParseThrows(textLiteralPlus());
    }

    @Test
    public void testConditionSecondFails() {
        this.conditionParseThrows(second());
    }

    @Test
    public void testConditionSlashFails() {
        this.conditionParseThrows(textLiteralSlash());
    }

    @Test
    public void testConditionSpaceFails() {
        this.conditionParseThrows(textLiteralSpace());
    }

    @Test
    public void testConditionYearFails() {
        this.conditionParseThrows(year());
    }

    @Test
    public void testConditionTextPlaceholderFails() {
        this.conditionParseThrows(textPlaceholder());
    }

    @Test
    public void testConditionOpenSquareBracketFails() {
        this.conditionParseThrows(bracketOpenSymbol());
    }

    @Test
    public void testConditionOpenSquareBracketEqualsFails() {
        this.conditionParseThrows(bracketOpenSymbol(), equalsSymbol());
    }

    @Test
    public void testConditionOpenSquareBracketGreaterThanFails() {
        this.conditionParseThrows(bracketOpenSymbol(), greaterThan());
    }

    @Test
    public void testConditionOpenSquareBracketGreaterThanEqualsFails() {
        this.conditionParseThrows(bracketOpenSymbol(), greaterThanEquals());
    }

    @Test
    public void testConditionOpenSquareBracketLessThanFails() {
        this.conditionParseThrows(bracketOpenSymbol(), lessThan());
    }

    @Test
    public void testConditionOpenSquareBracketLessThanEqualsFails() {
        this.conditionParseThrows(bracketOpenSymbol(), lessThanEquals());
    }

    @Test
    public void testConditionOpenSquareBracketNotEqualsFails() {
        this.conditionParseThrows(bracketOpenSymbol(), notEquals());
    }

    @Test
    public void testConditionOpenSquareBracketEqualsNumberFails() {
        this.conditionParseThrows(bracketOpenSymbol(), equalsSymbol(), conditionNumber());
    }

    @Test
    public void testConditionOpenSquareBracketGreaterThanNumberFails() {
        this.conditionParseThrows(bracketOpenSymbol(), greaterThan(), conditionNumber());
    }

    @Test
    public void testConditionOpenSquareBracketGreaterThanEqualsNumberFails() {
        this.conditionParseThrows(bracketOpenSymbol(), greaterThanEquals(), conditionNumber());
    }

    @Test
    public void testConditionOpenSquareBracketLessThanNumberFails() {
        this.conditionParseThrows(bracketOpenSymbol(), lessThan(), conditionNumber());
    }

    @Test
    public void testConditionOpenSquareBracketLessThanEqualsNumberFails() {
        this.conditionParseThrows(bracketOpenSymbol(), lessThanEquals(), conditionNumber());
    }

    @Test
    public void testConditionOpenSquareBracketNotEqualsNumberFails() {
        this.conditionParseThrows(bracketOpenSymbol(), notEquals(), conditionNumber());
    }

    @Test
    public void testConditionOpenSquareBracketEqualsNumber() {
        this.conditionParseAndCheck(SpreadsheetFormatParserToken::equalsParserToken,
                bracketOpenSymbol(), equalsSymbol(), conditionNumber(), bracketCloseSymbol());
    }

    @Test
    public void testConditionOpenSquareBracketGreaterThanNumber() {
        this.conditionParseAndCheck(SpreadsheetFormatParserToken::greaterThan,
                bracketOpenSymbol(), greaterThan(), conditionNumber(), bracketCloseSymbol());
    }

    @Test
    public void testConditionOpenSquareBracketGreaterThanEqualsNumber() {
        this.conditionParseAndCheck(SpreadsheetFormatParserToken::greaterThanEquals,
                bracketOpenSymbol(), greaterThanEquals(), conditionNumber(), bracketCloseSymbol());
    }

    @Test
    public void testConditionOpenSquareBracketLessThanNumber() {
        this.conditionParseAndCheck(SpreadsheetFormatParserToken::lessThan,
                bracketOpenSymbol(), lessThan(), conditionNumber(), bracketCloseSymbol());
    }

    @Test
    public void testConditionOpenSquareBracketLessThanEqualsNumber() {
        this.conditionParseAndCheck(SpreadsheetFormatParserToken::lessThanEquals,
                bracketOpenSymbol(), lessThanEquals(), conditionNumber(), bracketCloseSymbol());
    }

    @Test
    public void testConditionOpenSquareBracketNotEqualsNumber() {
        this.conditionParseAndCheck(SpreadsheetFormatParserToken::notEquals,
                bracketOpenSymbol(), notEquals(), conditionNumber(), bracketCloseSymbol());
    }

    private void conditionParseAndCheck(final BiFunction<List<ParserToken>, String, SpreadsheetFormatParserToken> factory,
                                        final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck3(
                this.conditionParser(),
                factory,
                tokens
        );
    }

    private void conditionParseThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(
                this.conditionParser(),
                tokens
        );
    }

    private Parser<SpreadsheetFormatParserContext> conditionParser() {
        return SpreadsheetFormatParsers.condition();
    }

    // date format......................................................................................................

    @Test
    public void testDateFormatGeneral() {
        this.dateFormatParseAndCheck(
                general()
        );
    }

    @Test
    public void testDateFormatWhitespaceGeneral() {
        this.dateFormatParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateFormatGeneralWhitespace() {
        this.dateFormatParseAndCheck(
                general(
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testDateFormatWhitespaceGeneralWhitespace() {
        this.dateFormatParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testDateFormatColorGeneral() {
        this.dateFormatParseAndCheck(
                general(
                        color(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateFormatColorWhitespaceGeneral() {
        this.dateFormatParseAndCheck(
                general(
                        color(),
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateFormatColorEscaped() {
        this.dateFormatParseAndCheck(
                date(
                        color(),
                        escape()
                )
        );
    }

    @Test
    public void testDateFormatTextDigitFails() {
        this.dateFormatParseThrows(
                digit()
        );
    }

    @Test
    public void testDateFormatTextDigitZeroFails() {
        this.dateFormatParseThrows(
                digitZero()
        );
    }

    @Test
    public void testDateFormatTextDigitSpaceFails() {
        this.dateFormatParseThrows(
                digitSpace()
        );
    }

    @Test
    public void testDateFormatHourFails() {
        this.dateFormatParseThrows(
                hour()
        );
    }

    @Test
    public void testDateFormatSecondFails() {
        this.dateFormatParseThrows(
                second()
        );
    }

    @Test
    public void testDateFormatTextPlaceholderFails() {
        this.dateFormatParseThrows(
                textPlaceholder()
        );
    }

    @Test
    public void testDateFormatEscaped() {
        this.dateFormatParseAndCheck(
                date(
                        escape()
                )
        );
    }

    @Test
    public void testDateFormatDollar() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralDollar()
                )
        );
    }

    @Test
    public void testDateFormatMinus() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralMinus()
                )
        );
    }

    @Test
    public void testDateFormatPlus() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralPlus()
                )
        );
    }

    @Test
    public void testDateFormatSlash() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralSlash()
                )
        );
    }

    @Test
    public void testDateFormatOpenParen() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralOpenParens()
                )
        );
    }

    @Test
    public void testDateFormatCloseParen() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralCloseParens()
                )
        );
    }

    @Test
    public void testDateFormatColon() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralColon()
                )
        );
    }

    @Test
    public void testDateFormatSpace() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralSpace()
                )
        );
    }

    @Test
    public void testDateFormatQuotedText() {
        this.dateFormatParseAndCheck(
                date(
                        quotedText()
                )
        );
    }

    @Test
    public void testDateFormatDay() {
        this.dateFormatParseAndCheck(
                date(
                        day()
                )
        );
    }

    @Test
    public void testDateFormatMonth() {
        this.dateFormatParseAndCheck(
                date(
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDay2Month2Year2() {
        this.dateFormatParseAndCheck(
                date(
                        day(2),
                        monthOrMinute(2),
                        year(2)
                )
        );
    }

    @Test
    public void testDateFormatDay3Month3Year3() {
        this.dateFormatParseAndCheck(
                date(
                        day(3),
                        monthOrMinute(3),
                        year(3)
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearDateDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatMonthDayYear() {
        this.dateFormatParseAndCheck(
                date(
                        monthOrMinute(),
                        day(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatYearMonthDay() {
        this.dateFormatParseAndCheck(
                date(
                        year(),
                        monthOrMinute(),
                        day()
                )
        );
    }

    @Test
    public void testDateFormatYearCommaMonthCommaDay() {
        this.dateFormatParseAndCheck(
                date(
                        year(),
                        textLiteralComma(),
                        monthOrMinute(),
                        textLiteralComma(),
                        day()
                )
        );
    }

    // escaped

    @Test
    public void testDateFormatEscapedDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        escape(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayEscapedMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        escape(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthEscapedYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        escape(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearEscaped() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        escape()
                )
        );
    }

    // quotedText

    @Test
    public void testDateFormatQuotedTextDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        quotedText(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayQuotedTextMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        quotedText(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthQuotedTextYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        quotedText(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearQuotedText() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        quotedText()
                )
        );
    }

    // closeParens

    @Test
    public void testDateFormatCloseParensDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralCloseParens(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayCloseParensMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        textLiteralCloseParens(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthCloseParensYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralCloseParens(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearCloseParens() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralCloseParens()
                )
        );
    }

    // colon

    @Test
    public void testDateFormatColonDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralColon(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayColonMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        textLiteralColon(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthColonYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralColon(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearColon() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralColon()

                )
        );
    }

    // dollar

    @Test
    public void testDateFormatDollarDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralDollar(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayDollarMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        textLiteralDollar(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthDollarYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralDollar(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearDollar() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralDollar()
                )
        );
    }

    // minus

    @Test
    public void testDateFormatMinusDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralMinus(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMinusMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        textLiteralMinus(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthMinusYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralMinus(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearMinus() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralMinus()
                )
        );
    }

    // openParens

    @Test
    public void testDateFormatOpenParensDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralOpenParens(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayOpenParensMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        textLiteralOpenParens(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthOpenParensYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralOpenParens(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearOpenParens() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralOpenParens()
                )
        );
    }

    // plus

    @Test
    public void testDateFormatPlusDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralPlus(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayPlusMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        textLiteralPlus(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthPlusYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralPlus(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearPlus() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralPlus()
                )
        );
    }

    // slash

    @Test
    public void testDateFormatSlashDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralSlash(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDaySlashMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        textLiteralSlash(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthSlashYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralSlash(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearSlash() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralSlash()
                )
        );
    }

    // space

    @Test
    public void testDateFormatSpaceDayMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        textLiteralSpace(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDaySpaceMonthYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        textLiteralSpace(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthSpaceYear() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralSpace(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayMonthYearSpace() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralSpace()
                )
        );
    }

    // equals

    @Test
    public void testDateFormatEqualsDayMonthYearFails() {
        this.dateFormatParseThrows(
                equalsSymbol(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayEqualsMonthYearFails() {
        this.dateFormatParseThrows(
                day(),
                equalsSymbol(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthEqualsYearFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                equalsSymbol(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthYearEqualsFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                year(),
                equalsSymbol()
        );
    }

    // greaterThan

    @Test
    public void testDateFormatGreaterThanDayMonthYearFails() {
        this.dateFormatParseThrows(
                greaterThan(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayGreaterThanMonthYearFails() {
        this.dateFormatParseThrows(
                day(),
                greaterThan(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthGreaterThanYearFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                greaterThan(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthYearGreaterThanFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                year(),
                greaterThan()
        );
    }

    // greaterThanEquals

    @Test
    public void testDateFormatGreaterThanEqualsDayMonthYearFails() {
        this.dateFormatParseThrows(
                greaterThanEquals(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayGreaterThanEqualsMonthYearFails() {
        this.dateFormatParseThrows(
                day(),
                greaterThanEquals(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthGreaterThanEqualsYearFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                greaterThanEquals(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthYearGreaterThanEqualsFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                year(),
                greaterThanEquals()
        );
    }

    // lessThan

    @Test
    public void testDateFormatLessThanDayMonthYearFails() {
        this.dateFormatParseThrows(
                lessThan(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayLessThanMonthYearFails() {
        this.dateFormatParseThrows(
                day(),
                lessThan(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthLessThanYearFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                lessThan(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthYearLessThanFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                year(),
                lessThan()
        );
    }

    @Test
    public void testDateFormatLessThanEqualsDayMonthYearFails() {
        this.dateFormatParseThrows(
                lessThanEquals(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayLessThanEqualsMonthYearFails() {
        this.dateFormatParseThrows(
                day(),
                lessThanEquals(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthLessThanEqualsYearFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                lessThanEquals(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthYearLessThanEqualsFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                year(),
                lessThanEquals()
        );
    }

    // notEquals

    @Test
    public void testDateFormatNotEqualsDayMonthYearFails() {
        this.dateFormatParseThrows(
                notEquals(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayNotEqualsMonthYearFails() {
        this.dateFormatParseThrows(
                day(),
                notEquals(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthNotEqualsYearFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                notEquals(),
                year()
        );
    }

    @Test
    public void testDateFormatDayMonthYearNotEqualsFails() {
        this.dateFormatParseThrows(
                day(),
                monthOrMinute(),
                year(),
                notEquals()
        );
    }

    // color

    @Test
    public void testDateFormatColorDay() {
        this.dateFormatParseAndCheck(
                date(
                        color(),
                        day()
                )
        );
    }

    @Test
    public void testDateFormatColorMonth() {
        this.dateFormatParseAndCheck(
                date(
                        color(),
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testDateFormatColorYear() {
        this.dateFormatParseAndCheck(
                date(
                        color(),
                        year()
                )
        );
    }

    @Test
    public void testDateFormatDayColor() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        color()
                )
        );
    }

    @Test
    public void testDateFormatMonthColor() {
        this.dateFormatParseAndCheck(
                date(
                        monthOrMinute(),
                        color()
                )
        );
    }

    @Test
    public void testDateFormatYearColor() {
        this.dateFormatParseAndCheck(
                date(
                        year(),
                        color()
                )
        );
    }

    // condition

    @Test
    public void testDateFormatConditionEqualsDayFails() {
        this.dateFormatParseThrows(
                conditionEquals(),
                day()
        );
    }

    @Test
    public void testDateFormatConditionGreaterThanDayFails() {
        this.dateFormatParseThrows(
                conditionGreaterThan(),
                day()
        );
    }

    @Test
    public void testDateFormatConditionGreaterThanEqualsDayFails() {
        this.dateFormatParseThrows(
                conditionGreaterThanEquals(),
                day()
        );
    }

    @Test
    public void testDateFormatConditionLessThanDayFails() {
        this.dateFormatParseThrows(
                conditionLessThan(),
                day());
    }

    @Test
    public void testDateFormatConditionLessThanEqualsDayFails() {
        this.dateFormatParseThrows(
                conditionLessThanEquals(),
                day()
        );
    }

    @Test
    public void testDateFormatConditionNotEqualsDayFails() {
        this.dateFormatParseThrows(
                conditionNotEquals(),
                day()
        );
    }

    @Test
    public void testDayFormatDateConditionEqualsFails() {
        this.dateFormatParseThrows(
                day(),
                conditionEquals()
        );
    }

    @Test
    public void testDateFormatDayConditionGreaterThanFails() {
        this.dateFormatParseThrows(
                day(),
                conditionGreaterThan()
        );
    }

    @Test
    public void testDateFormatDayConditionGreaterThanEqualsFails() {
        this.dateFormatParseThrows(
                day(),
                conditionGreaterThanEquals()
        );
    }

    @Test
    public void testDateFormatDayConditionLessThanFails() {
        this.dateFormatParseThrows(
                day(),
                conditionLessThan()
        );
    }

    @Test
    public void testDateFormatDayConditionLessThanEqualsFails() {
        this.dateFormatParseThrows(
                day(),
                conditionLessThanEquals()
        );
    }

    @Test
    public void testDateFormatDayConditionNotEqualsFails() {
        this.dateFormatParseThrows(
                day(),
                conditionNotEquals()
        );
    }

    @Test
    public void testDateFormatPatternSeparator() {
        this.dateFormatParseAndCheck(
                date(
                        year(),
                        monthOrMinute(),
                        day()
                ),
                separator()
        );
    }

    @Test
    public void testDateFormatPatternSeparatorPattern() {
        this.dateFormatParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year()
                ),
                separator(),
                date(
                        year(),
                        monthOrMinute(),
                        day()
                )
        );
    }

    @Test
    public void testDateFormatColorPatternSeparatorPattern() {
        this.dateFormatParseAndCheck(
                date(
                        color(),
                        day(),
                        monthOrMinute(),
                        year()
                ),
                separator(),
                date(
                        year(),
                        monthOrMinute(),
                        day()
                )
        );
    }

    @Test
    public void testDateFormatConditionPatternSeparatorPattern() {
        this.dateFormatParseAndCheck(
                conditionEquals(),
                date(
                        day(),
                        monthOrMinute(),
                        year()
                ),
                separator(),
                date(
                        year(),
                        monthOrMinute(),
                        day()
                )
        );
    }

    // date format helpers..............................................................................................

    private void dateFormatParseAndCheck(final SpreadsheetFormatDateParserToken... tokens) {
        this.parseAndCheck2(
                SpreadsheetFormatParsers.dateFormat(),
                tokens
        );
    }

    private void dateFormatParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck2(
                SpreadsheetFormatParsers.dateFormat(),
                tokens
        );
    }

    private void dateFormatParseThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(
                SpreadsheetFormatParsers.dateFormat(),
                tokens
        );
    }

    // date format......................................................................................................

    @Test
    public void testDateParseGeneral() {
        this.dateParseParseAndCheck(
                general()
        );
    }

    @Test
    public void testDateParseWhitespaceGeneral() {
        this.dateParseParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateParseGeneralWhitespace() {
        this.dateParseParseAndCheck(
                general(
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testDateParseWhitespaceGeneralWhitespace() {
        this.dateParseParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testDateParseColorGeneral() {
        this.dateParseParseAndCheck(
                general(
                        color(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateParseColorWhitespaceGeneral() {
        this.dateParseParseAndCheck(
                general(
                        color(),
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateParseColorEscapedFails() {
        this.dateParseParseThrows(
                date(
                        color(),
                        escape()
                )
        );
    }

    @Test
    public void testDateParseTextDigitFails() {
        this.dateParseParseThrows(
                digit()
        );
    }

    @Test
    public void testDateParseTextDigitZeroFails() {
        this.dateParseParseThrows(
                digitZero()
        );
    }

    @Test
    public void testDateParseTextDigitSpaceFails() {
        this.dateParseParseThrows(
                digitSpace()
        );
    }

    @Test
    public void testDateParseHourFails() {
        this.dateParseParseThrows(
                hour()
        );
    }

    @Test
    public void testDateParseSecondFails() {
        this.dateParseParseThrows(
                second()
        );
    }

    @Test
    public void testDateParseTextPlaceholderFails() {
        this.dateParseParseThrows(
                textPlaceholder()
        );
    }

    @Test
    public void testDateParseEscaped() {
        this.dateParseParseAndCheck(
                date(
                        escape()
                )
        );
    }

    @Test
    public void testDateParseDollar() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralDollar()
                )
        );
    }

    @Test
    public void testDateParseMinus() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralMinus()
                )
        );
    }

    @Test
    public void testDateParsePlus() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralPlus()
                )
        );
    }

    @Test
    public void testDateParseSlash() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralSlash()
                )
        );
    }

    @Test
    public void testDateParseOpenParen() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralOpenParens()
                )
        );
    }

    @Test
    public void testDateParseCloseParen() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralCloseParens()
                )
        );
    }

    @Test
    public void testDateParseColon() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralColon()
                )
        );
    }

    @Test
    public void testDateParseSpace() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralSpace()
                )
        );
    }

    @Test
    public void testDateParseQuotedText() {
        this.dateParseParseAndCheck(
                date(
                        quotedText()
                )
        );
    }

    @Test
    public void testDateParseDay() {
        this.dateParseParseAndCheck(
                date(
                        day()
                )
        );
    }

    @Test
    public void testDateParseMonth() {
        this.dateParseParseAndCheck(
                date(
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDay2Month2Year2() {
        this.dateParseParseAndCheck(
                date(
                        day(2),
                        monthOrMinute(2),
                        year(2)
                )
        );
    }

    @Test
    public void testDateParseDay3Month3Year3() {
        this.dateParseParseAndCheck(
                date(
                        day(3),
                        monthOrMinute(3),
                        year(3)
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearDateDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseMonthDayYear() {
        this.dateParseParseAndCheck(
                date(
                        monthOrMinute(),
                        day(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseYearMonthDay() {
        this.dateParseParseAndCheck(
                date(
                        year(),
                        monthOrMinute(),
                        day()
                )
        );
    }

    @Test
    public void testDateParseYearCommaMonthCommaDay() {
        this.dateParseParseAndCheck(
                date(
                        year(),
                        textLiteralComma(),
                        monthOrMinute(),
                        textLiteralComma(),
                        day()
                )
        );
    }

    // escaped

    @Test
    public void testDateParseEscapedDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        escape(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayEscapedMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        escape(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthEscapedYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        escape(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearEscaped() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        escape()
                )
        );
    }

    // quotedText

    @Test
    public void testDateParseQuotedTextDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        quotedText(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayQuotedTextMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        quotedText(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthQuotedTextYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        quotedText(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearQuotedText() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        quotedText()
                )
        );
    }

    // closeParens

    @Test
    public void testDateParseCloseParensDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralCloseParens(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayCloseParensMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        textLiteralCloseParens(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthCloseParensYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralCloseParens(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearCloseParens() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralCloseParens()
                )
        );
    }

    // colon

    @Test
    public void testDateParseColonDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralColon(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayColonMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        textLiteralColon(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthColonYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralColon(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearColon() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralColon()

                )
        );
    }

    // dollar

    @Test
    public void testDateParseDollarDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralDollar(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayDollarMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        textLiteralDollar(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthDollarYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralDollar(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearDollar() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralDollar()
                )
        );
    }

    // minus

    @Test
    public void testDateParseMinusDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralMinus(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMinusMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        textLiteralMinus(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthMinusYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralMinus(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearMinus() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralMinus()
                )
        );
    }

    // openParens

    @Test
    public void testDateParseOpenParensDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralOpenParens(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayOpenParensMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        textLiteralOpenParens(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthOpenParensYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralOpenParens(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearOpenParens() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralOpenParens()
                )
        );
    }

    // plus

    @Test
    public void testDateParsePlusDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralPlus(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayPlusMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        textLiteralPlus(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthPlusYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralPlus(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearPlus() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralPlus()
                )
        );
    }

    // slash

    @Test
    public void testDateParseSlashDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralSlash(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDaySlashMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        textLiteralSlash(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthSlashYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralSlash(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearSlash() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralSlash()
                )
        );
    }

    // space

    @Test
    public void testDateParseSpaceDayMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        textLiteralSpace(),
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDaySpaceMonthYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        textLiteralSpace(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthSpaceYear() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        textLiteralSpace(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayMonthYearSpace() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year(),
                        textLiteralSpace()
                )
        );
    }

    // equals

    @Test
    public void testDateParseEqualsDayMonthYearFails() {
        this.dateParseParseThrows(
                equalsSymbol(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayEqualsMonthYearFails() {
        this.dateParseParseThrows(
                day(),
                equalsSymbol(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthEqualsYearFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                equalsSymbol(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthYearEqualsFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                year(),
                equalsSymbol()
        );
    }

    // greaterThan

    @Test
    public void testDateParseGreaterThanDayMonthYearFails() {
        this.dateParseParseThrows(
                greaterThan(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayGreaterThanMonthYearFails() {
        this.dateParseParseThrows(
                day(),
                greaterThan(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthGreaterThanYearFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                greaterThan(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthYearGreaterThanFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                year(),
                greaterThan()
        );
    }

    // greaterThanEquals

    @Test
    public void testDateParseGreaterThanEqualsDayMonthYearFails() {
        this.dateParseParseThrows(
                greaterThanEquals(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayGreaterThanEqualsMonthYearFails() {
        this.dateParseParseThrows(
                day(),
                greaterThanEquals(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthGreaterThanEqualsYearFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                greaterThanEquals(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthYearGreaterThanEqualsFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                year(),
                greaterThanEquals()
        );
    }

    // lessThan

    @Test
    public void testDateParseLessThanDayMonthYearFails() {
        this.dateParseParseThrows(
                lessThan(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayLessThanMonthYearFails() {
        this.dateParseParseThrows(
                day(),
                lessThan(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthLessThanYearFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                lessThan(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthYearLessThanFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                year(),
                lessThan()
        );
    }

    @Test
    public void testDateParseLessThanEqualsDayMonthYearFails() {
        this.dateParseParseThrows(
                lessThanEquals(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayLessThanEqualsMonthYearFails() {
        this.dateParseParseThrows(
                day(),
                lessThanEquals(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthLessThanEqualsYearFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                lessThanEquals(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthYearLessThanEqualsFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                year(),
                lessThanEquals()
        );
    }

    // notEquals

    @Test
    public void testDateParseNotEqualsDayMonthYearFails() {
        this.dateParseParseThrows(
                notEquals(),
                day(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayNotEqualsMonthYearFails() {
        this.dateParseParseThrows(
                day(),
                notEquals(),
                monthOrMinute(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthNotEqualsYearFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                notEquals(),
                year()
        );
    }

    @Test
    public void testDateParseDayMonthYearNotEqualsFails() {
        this.dateParseParseThrows(
                day(),
                monthOrMinute(),
                year(),
                notEquals()
        );
    }

    // color

    @Test
    public void testDateParseColorDayFails() {
        this.dateParseParseThrows(
                date(
                        color(),
                        day()
                )
        );
    }

    @Test
    public void testDateParseColorMonthFails() {
        this.dateParseParseThrows(
                date(
                        color(),
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testDateParseColorYearFails() {
        this.dateParseParseThrows(
                date(
                        color(),
                        year()
                )
        );
    }

    @Test
    public void testDateParseDayColorFails() {
        this.dateParseParseThrows(
                date(
                        day(),
                        color()
                )
        );
    }

    @Test
    public void testDateParseMonthColorFails() {
        this.dateParseParseThrows(
                date(
                        monthOrMinute(),
                        color()
                )
        );
    }

    @Test
    public void testDateParseYearColorFails() {
        this.dateParseParseThrows(
                date(
                        year(),
                        color()
                )
        );
    }

    // condition

    @Test
    public void testDateParseConditionEqualsDayFails() {
        this.dateParseParseThrows(
                conditionEquals(),
                day()
        );
    }

    @Test
    public void testDateParseConditionGreaterThanDayFails() {
        this.dateParseParseThrows(
                conditionGreaterThan(),
                day()
        );
    }

    @Test
    public void testDateParseConditionGreaterThanEqualsDayFails() {
        this.dateParseParseThrows(
                conditionGreaterThanEquals(),
                day()
        );
    }

    @Test
    public void testDateParseConditionLessThanDayFails() {
        this.dateParseParseThrows(
                conditionLessThan(),
                day());
    }

    @Test
    public void testDateParseConditionLessThanEqualsDayFails() {
        this.dateParseParseThrows(
                conditionLessThanEquals(),
                day()
        );
    }

    @Test
    public void testDateParseConditionNotEqualsDayFails() {
        this.dateParseParseThrows(
                conditionNotEquals(),
                day()
        );
    }

    @Test
    public void testDateDayConditionEqualsFails() {
        this.dateParseParseThrows(
                day(),
                conditionEquals()
        );
    }

    @Test
    public void testDateParseDayConditionGreaterThanFails() {
        this.dateParseParseThrows(
                day(),
                conditionGreaterThan()
        );
    }

    @Test
    public void testDateParseDayConditionGreaterThanEqualsFails() {
        this.dateParseParseThrows(
                day(),
                conditionGreaterThanEquals()
        );
    }

    @Test
    public void testDateParseDayConditionLessThanFails() {
        this.dateParseParseThrows(
                day(),
                conditionLessThan()
        );
    }

    @Test
    public void testDateParseDayConditionLessThanEqualsFails() {
        this.dateParseParseThrows(
                day(),
                conditionLessThanEquals()
        );
    }

    @Test
    public void testDateParseDayConditionNotEqualsFails() {
        this.dateParseParseThrows(
                day(),
                conditionNotEquals()
        );
    }

    @Test
    public void testDateParsePatternSeparator() {
        this.dateParseParseAndCheck(
                date(
                        year(),
                        monthOrMinute(),
                        day()
                ),
                separator()
        );
    }

    @Test
    public void testDateParsePatternSeparatorPattern() {
        this.dateParseParseAndCheck(
                date(
                        day(),
                        monthOrMinute(),
                        year()
                ),
                separator(),
                date(
                        year(),
                        monthOrMinute(),
                        day()
                )
        );
    }

    @Test
    public void testDateParseColorPatternSeparatorPatternFails() {
        this.dateParseParseThrows(
                date(
                        color(),
                        day(),
                        monthOrMinute(),
                        year()
                ),
                separator(),
                date(
                        year(),
                        monthOrMinute(),
                        day()
                )
        );
    }

    // date parse helpers...............................................................................................

    private void dateParseParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck2(
                SpreadsheetFormatParsers.dateParse(),
                tokens
        );
    }

    private void dateParseParseThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(
                SpreadsheetFormatParsers.dateParse(),
                tokens
        );
    }

    // bigDecimal........................................................................................................

    @Test
    public void testNumberDayFails() {
        this.numberParseThrows(digit(), day());
    }

    @Test
    public void testNumberHourFails() {
        this.numberParseThrows(digit(), hour());
    }

    @Test
    public void testNumberMinuteOrMonthFails() {
        this.numberParseThrows(digit(), monthOrMinute());
    }

    @Test
    public void testNumberSecondFails() {
        this.numberParseThrows(digit(), second());
    }

    @Test
    public void testNumberStarFails() {
        this.numberParseThrows(star());
    }

    @Test
    public void testNumberTextPlaceholderFails() {
        this.numberParseThrows(digit(), textPlaceholder());
    }

    @Test
    public void testNumberUnderscoreFails() {
        this.numberParseThrows(underscore());
    }

    @Test
    public void testNumberYearFails() {
        this.numberParseThrows(digit(), year());
    }

    @Test
    public void testNumberSlashFails() {
        this.numberParseThrows(fractionSymbol());
    }

    @Test
    public void testNumberDigitSpaceNumberFails() {
        this.numberParseThrows(digitSpace(), fractionSymbol());
    }

    @Test
    public void testNumberDigitZeroNumberFails() {
        this.numberParseThrows(digitZero(), fractionSymbol());
    }

    @Test
    public void testNumberDigitNumberFails() {
        this.numberParseThrows(digit(), fractionSymbol());
    }

    // general.........................................................................................................

    @Test
    public void testNumberGeneral() {
        this.numberParseAndCheck(
                general()
        );
    }

    @Test
    public void testNumberGeneralWhitespace() {
        this.numberParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testNumberWhitespaceGeneralWhitespace() {
        this.numberParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testNumberColorGeneral() {
        this.numberParseAndCheck(
                general(
                        color(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testNumberColorWhitespaceGeneral() {
        this.numberParseAndCheck(
                general(
                        color(),
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testNumberConditionEqualsGeneral() {
        this.numberParseAndCheck(
                conditionEquals(),
                general()
        );
    }

    @Test
    public void testNumberConditionGreaterThanGeneral() {
        this.numberParseAndCheck(
                conditionGreaterThan(),
                general()
        );
    }

    @Test
    public void testNumberConditionGreaterThanEqualsGeneral() {
        this.numberParseAndCheck(
                conditionGreaterThanEquals(),
                general()
        );
    }

    @Test
    public void testNumberConditionLessThanGeneral() {
        this.numberParseAndCheck(
                conditionLessThan(),
                general()
        );
    }

    @Test
    public void testNumberConditionLessThanEqualsGeneral() {
        this.numberParseAndCheck(
                conditionLessThanEquals(),
                general()
        );
    }

    @Test
    public void testNumberConditionNotEqualsGeneral() {
        this.numberParseAndCheck(
                conditionNotEquals(),
                general()
        );
    }

    // literals only...........................................................................

    @Test
    public void testNumberEscaped() {
        this.numberParseAndCheck(escape());
    }

    @Test
    public void testNumberMinus() {
        this.numberParseAndCheck(textLiteralMinus());
    }

    @Test
    public void testNumberPlus() {
        this.numberParseAndCheck(textLiteralPlus());
    }

    @Test
    public void testNumberOpenParen() {
        this.numberParseAndCheck(textLiteralOpenParens());
    }

    @Test
    public void testNumberCloseParen() {
        this.numberParseAndCheck(textLiteralCloseParens());
    }

    @Test
    public void testNumberColon() {
        this.numberParseAndCheck(textLiteralColon());
    }

    @Test
    public void testNumberSpace() {
        this.numberParseAndCheck(textLiteralSpace());
    }

    @Test
    public void testNumberQuotedText() {
        this.numberParseAndCheck(quotedText());
    }

    // digitSpace

    @Test
    public void testNumberDigitSpaceNumberDigitSpace() {
        this.numberParseAndCheck(digitSpace(), decimalPoint(), digitSpace());
    }

    @Test
    public void testNumberDigitSpaceDigitSpaceNumberDigitSpace() {
        this.numberParseAndCheck(digitSpace(), digitSpace(), decimalPoint(), digitSpace());
    }

    @Test
    public void testNumberDigitSpaceDigitZeroNumberDigitSpace() {
        this.numberParseAndCheck(digitSpace(), digitZero(), decimalPoint(), digitSpace());
    }

    @Test
    public void testNumberDigitSpaceDigitNumberDigitSpace() {
        this.numberParseAndCheck(digitSpace(), digit(), decimalPoint(), digitSpace());
    }

    @Test
    public void testNumberDigitSpaceNumberDigitSpaceDigitSpace() {
        this.numberParseAndCheck(digitSpace(), decimalPoint(), digitSpace(), digitSpace());
    }

    // digitSpace

    @Test
    public void testNumberDigitZeroDigitSpaceNumberDigitZero() {
        this.numberParseAndCheck(digitZero(), digitSpace(), decimalPoint(), digitZero());
    }

    @Test
    public void testNumberDigitZeroDigitZeroNumberDigitZero() {
        this.numberParseAndCheck(digitZero(), digitZero(), decimalPoint(), digitZero());
    }

    @Test
    public void testNumberDigitZeroDigitNumberDigitZero() {
        this.numberParseAndCheck(digitZero(), digit(), decimalPoint(), digitZero());
    }

    @Test
    public void testNumberDigitZeroNumberDigitZeroDigitZero() {
        this.numberParseAndCheck(digitZero(), decimalPoint(), digitZero(), digitZero());
    }

    // digitZero

    @Test
    public void testNumberDigitNumberDigit() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitDigitSpaceNumberDigit() {
        this.numberParseAndCheck(digit(), digitSpace(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitDigitZeroNumberDigit() {
        this.numberParseAndCheck(digit(), digitZero(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitDigitNumberDigit() {
        this.numberParseAndCheck(digit(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitNumberDigitDigit() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), digit());
    }

    // currency

    @Test
    public void testNumberCurrencyDigitSlashDigit() {
        this.numberParseAndCheck(currency(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitCurrencySlashDigit() {
        this.numberParseAndCheck(digit(), currency(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashCurrencyDigit() {
        this.numberParseAndCheck(currency(), digit(), decimalPoint(), currency(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitCurrency() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), currency());
    }

    // percentage

    @Test
    public void testNumberPercentageDigitSlashDigit() {
        this.numberParseAndCheck(percentSymbol(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitPercentageSlashDigit() {
        this.numberParseAndCheck(digit(), percentSymbol(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashPercentageDigit() {
        this.numberParseAndCheck(percentSymbol(), digit(), decimalPoint(), percentSymbol(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitPercentage() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), percentSymbol());
    }

    // thousands

    @Test
    public void testNumberThousandsDigitSlashDigit() {
        this.numberParseAndCheck(thousands(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitThousandsSlashDigit() {
        this.numberParseAndCheck(digit(), thousands(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashThousandsDigit() {
        this.numberParseAndCheck(thousands(), digit(), decimalPoint(), thousands(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitThousands() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), thousands());
    }

    // text literals

    // escaped

    @Test
    public void testNumberDigitEscapedDigitSlashDigit() {
        this.numberParseAndCheck(digit(), escape());
    }

    @Test
    public void testNumberDigitEscapedSlashDigit() {
        this.numberParseAndCheck(digit(), escape(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashEscapedDigit() {
        this.numberParseAndCheck(escape(), digit(), decimalPoint(), escape(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitEscaped() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), escape());
    }

    // quotedText

    @Test
    public void testNumberQuotedTextDigitSlashDigit() {
        this.numberParseAndCheck(quotedText(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitQuotedTextSlashDigit() {
        this.numberParseAndCheck(digit(), quotedText(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashQuotedTextDigit() {
        this.numberParseAndCheck(quotedText(), digit(), decimalPoint(), quotedText(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitQuotedText() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), quotedText());
    }

    // closeParens

    @Test
    public void testNumberCloseParensDigitSlashDigit() {
        this.numberParseAndCheck(textLiteralCloseParens(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitCloseParensSlashDigit() {
        this.numberParseAndCheck(digit(), textLiteralCloseParens(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashCloseParensDigit() {
        this.numberParseAndCheck(textLiteralCloseParens(), digit(), decimalPoint(), textLiteralCloseParens(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitCloseParens() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), textLiteralCloseParens());
    }

    // colon

    @Test
    public void testNumberColonDigitSlashDigit() {
        this.numberParseAndCheck(textLiteralColon(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitColonSlashDigit() {
        this.numberParseAndCheck(digit(), textLiteralColon(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashColonDigit() {
        this.numberParseAndCheck(textLiteralColon(), digit(), decimalPoint(), textLiteralColon(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitColon() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), textLiteralColon());
    }

    // minus

    @Test
    public void testNumberMinusDigitSlashDigit() {
        this.numberParseAndCheck(textLiteralMinus(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitMinusSlashDigit() {
        this.numberParseAndCheck(digit(), textLiteralMinus(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashMinusDigit() {
        this.numberParseAndCheck(textLiteralMinus(), digit(), decimalPoint(), textLiteralMinus(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitMinus() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), textLiteralMinus());
    }

    // openParens

    @Test
    public void testNumberOpenParensDigitSlashDigit() {
        this.numberParseAndCheck(textLiteralOpenParens(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitOpenParensSlashDigit() {
        this.numberParseAndCheck(digit(), textLiteralOpenParens(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashOpenParensDigit() {
        this.numberParseAndCheck(textLiteralOpenParens(), digit(), decimalPoint(), textLiteralOpenParens(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitOpenParens() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), textLiteralOpenParens());
    }

    // plus

    @Test
    public void testNumberPlusDigitSlashDigit() {
        this.numberParseAndCheck(textLiteralPlus(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitPlusSlashDigit() {
        this.numberParseAndCheck(digit(), textLiteralPlus(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashPlusDigit() {
        this.numberParseAndCheck(textLiteralPlus(), digit(), decimalPoint(), textLiteralPlus(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitPlus() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), textLiteralPlus());
    }

    // space

    @Test
    public void testNumberSpaceDigitSlashDigit() {
        this.numberParseAndCheck(textLiteralSpace(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSpaceSlashDigit() {
        this.numberParseAndCheck(digit(), textLiteralSpace(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitSlashSpaceDigit() {
        this.numberParseAndCheck(textLiteralSpace(), digit(), decimalPoint(), textLiteralSpace(), digit());
    }

    @Test
    public void testNumberDigitSlashDigitSpace() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), textLiteralSpace());
    }

    // equals

    @Test
    public void testNumberEqualsDigitDecimalPointDigitFails() {
        this.numberParseThrows(equalsSymbol(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitEqualsDecimalPointDigitFails() {
        this.numberParseThrows(digit(), equalsSymbol(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointEqualsDigitFails() {
        this.numberParseThrows(digit(), decimalPoint(), equalsSymbol(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointDigitEqualsFails() {
        this.numberParseThrows(digit(), decimalPoint(), digit(), equalsSymbol());
    }

    // greaterThan

    @Test
    public void testNumberGreaterThanDigitDecimalPointDigitFails() {
        this.numberParseThrows(greaterThan(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitGreaterThanDecimalPointDigitFails() {
        this.numberParseThrows(digit(), greaterThan(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointGreaterThanDigitFails() {
        this.numberParseThrows(digit(), decimalPoint(), greaterThan(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointDigitGreaterThanFails() {
        this.numberParseThrows(digit(), decimalPoint(), digit(), greaterThan());
    }

    // greaterThanEquals

    @Test
    public void testNumberGreaterThanEqualsDigitDecimalPointDigitFails() {
        this.numberParseThrows(greaterThanEquals(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitGreaterThanEqualsDecimalPointDigitFails() {
        this.numberParseThrows(digit(), greaterThanEquals(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointGreaterThanEqualsDigitFails() {
        this.numberParseThrows(digit(), decimalPoint(), greaterThanEquals(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointDigitGreaterThanEqualsFails() {
        this.numberParseThrows(digit(), decimalPoint(), digit(), greaterThanEquals());
    }

    // lessThan

    @Test
    public void testNumberLessThanDigitDecimalPointDigitFails() {
        this.numberParseThrows(lessThan(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitLessThanDecimalPointDigitFails() {
        this.numberParseThrows(digit(), lessThan(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointLessThanDigitFails() {
        this.numberParseThrows(digit(), decimalPoint(), lessThan(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointDigitLessThanFails() {
        this.numberParseThrows(digit(), decimalPoint(), digit(), lessThan());
    }

    // lessThanEquals

    @Test
    public void testNumberLessThanEqualsDigitDecimalPointDigitFails() {
        this.numberParseThrows(lessThanEquals(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitLessThanEqualsDecimalPointDigitFails() {
        this.numberParseThrows(digit(), lessThanEquals(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointLessThanEqualsDigitFails() {
        this.numberParseThrows(digit(), decimalPoint(), lessThanEquals(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointDigitLessThanEqualsFails() {
        this.numberParseThrows(digit(), decimalPoint(), digit(), lessThanEquals());
    }

    // notEquals

    @Test
    public void testNumberNotEqualsDigitDecimalPointDigitFails() {
        this.numberParseThrows(notEquals(), digit(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitNotEqualsDecimalPointDigitFails() {
        this.numberParseThrows(digit(), notEquals(), decimalPoint(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointNotEqualsDigitFails() {
        this.numberParseThrows(digit(), decimalPoint(), notEquals(), digit());
    }

    @Test
    public void testNumberDigitDecimalPointDigitNotEqualsFails() {
        this.numberParseThrows(digit(), decimalPoint(), digit(), notEquals());
    }

    // exponent.............................................................................

    // currency

    @Test
    public void testNumberDigitExponentCurrencyDigit() {
        this.numberParseAndCheck(digit(), exponent1(currency()));
    }

    @Test
    public void testNumberDigitExponentDigitCurrencyDigit() {
        this.numberParseAndCheck(digit(), exponent2(currency()));
    }

    @Test
    public void testNumberDigitExponentDigitCurrency() {
        this.numberParseAndCheck(digit(), exponent3(currency()));
    }

    // text literals

    // escaped

    @Test
    public void testNumberDigitExponentEscapedDigit() {
        this.numberParseAndCheck(digit(), exponent1(escape()));
    }

    @Test
    public void testNumberDigitExponentDigitEscapedDigit() {
        this.numberParseAndCheck(digit(), exponent2(escape()));
    }

    @Test
    public void testNumberDigitExponentDigitEscaped() {
        this.numberParseAndCheck(digit(), exponent3(escape()));
    }

    // quotedText

    @Test
    public void testNumberDigitExponentQuotedTextDigit() {
        this.numberParseAndCheck(digit(), exponent1(quotedText()));
    }

    @Test
    public void testNumberDigitExponentDigitQuotedTextDigit() {
        this.numberParseAndCheck(digit(), exponent2(quotedText()));
    }

    @Test
    public void testNumberDigitExponentDigitQuotedText() {
        this.numberParseAndCheck(digit(), exponent3(quotedText()));
    }

    // closeParens

    @Test
    public void testNumberDigitExponentCloseParensDigit() {
        this.numberParseAndCheck(digit(), exponent1(textLiteralCloseParens()));
    }

    @Test
    public void testNumberDigitExponentDigitCloseParensDigit() {
        this.numberParseAndCheck(digit(), exponent2(textLiteralCloseParens()));
    }

    @Test
    public void testNumberDigitExponentDigitCloseParens() {
        this.numberParseAndCheck(digit(), exponent3(textLiteralCloseParens()));
    }

    // colon

    @Test
    public void testNumberDigitExponentColonDigit() {
        this.numberParseAndCheck(digit(), exponent1(textLiteralColon()));
    }

    @Test
    public void testNumberDigitExponentDigitColonDigit() {
        this.numberParseAndCheck(digit(), exponent2(textLiteralColon()));
    }

    @Test
    public void testNumberDigitExponentDigitColon() {
        this.numberParseAndCheck(digit(), exponent3(textLiteralColon()));
    }

    // minus

    @Test
    public void testNumberDigitExponentMinusDigit() {
        this.numberParseAndCheck(digit(), exponent1(textLiteralMinus()));
    }

    @Test
    public void testNumberDigitExponentDigitMinusDigit() {
        this.numberParseAndCheck(digit(), exponent2(textLiteralMinus()));
    }

    @Test
    public void testNumberDigitExponentDigitMinus() {
        this.numberParseAndCheck(digit(), exponent3(textLiteralMinus()));
    }

    // openParens

    @Test
    public void testNumberDigitExponentOpenParensDigit() {
        this.numberParseAndCheck(digit(), exponent1(textLiteralOpenParens()));
    }

    @Test
    public void testNumberDigitExponentDigitOpenParensDigit() {
        this.numberParseAndCheck(digit(), exponent2(textLiteralOpenParens()));
    }

    @Test
    public void testNumberDigitExponentDigitOpenParens() {
        this.numberParseAndCheck(digit(), exponent3(textLiteralOpenParens()));
    }

    // plus

    @Test
    public void testNumberDigitExponentPlusDigit() {
        this.numberParseAndCheck(digit(), exponent1(textLiteralPlus()));
    }

    @Test
    public void testNumberDigitExponentDigitPlusDigit() {
        this.numberParseAndCheck(digit(), exponent2(textLiteralPlus()));
    }

    @Test
    public void testNumberDigitExponentDigitPlus() {
        this.numberParseAndCheck(digit(), exponent3(textLiteralPlus()));
    }

    // space

    @Test
    public void testNumberDigitExponentSpaceDigit() {
        this.numberParseAndCheck(digit(), exponent1(textLiteralSpace()));
    }

    @Test
    public void testNumberDigitExponentDigitSpaceDigit() {
        this.numberParseAndCheck(digit(), exponent2(textLiteralSpace()));
    }

    @Test
    public void testNumberDigitExponentDigitSpace() {
        this.numberParseAndCheck(digit(), exponent3(textLiteralSpace()));
    }

    // equals

    @Test
    public void testNumberDigitExponentEqualsDigit() {
        this.numberParseThrows(digit(), exponent1(equalsSymbol()));
    }

    @Test
    public void testNumberDigitExponentDigitEqualsDigit() {
        this.numberParseThrows(digit(), exponent2(equalsSymbol()));
    }

    @Test
    public void testNumberDigitExponentDigitEquals() {
        this.numberParseThrows(digit(), exponent3(equalsSymbol()));
    }

    // greaterThan

    @Test
    public void testNumberDigitExponentGreaterThanDigit() {
        this.numberParseThrows(digit(), exponent1(greaterThan()));
    }

    @Test
    public void testNumberDigitExponentDigitGreaterThanDigit() {
        this.numberParseThrows(digit(), exponent2(greaterThan()));
    }

    @Test
    public void testNumberDigitExponentDigitGreaterThan() {
        this.numberParseThrows(digit(), exponent3(greaterThan()));
    }

    // greaterThanEquals

    @Test
    public void testNumberDigitExponentGreaterThanEqualsDigit() {
        this.numberParseThrows(digit(), exponent1(greaterThanEquals()));
    }

    @Test
    public void testNumberDigitExponentDigitGreaterThanEqualsDigit() {
        this.numberParseThrows(digit(), exponent2(greaterThanEquals()));
    }

    @Test
    public void testNumberDigitExponentDigitGreaterThanEquals() {
        this.numberParseThrows(digit(), exponent3(greaterThanEquals()));
    }

    // lessThan

    @Test
    public void testNumberDigitExponentLessThanDigit() {
        this.numberParseThrows(digit(), exponent1(lessThan()));
    }

    @Test
    public void testNumberDigitExponentDigitLessThanDigit() {
        this.numberParseThrows(digit(), exponent2(lessThan()));
    }

    @Test
    public void testNumberDigitExponentDigitLessThan() {
        this.numberParseThrows(digit(), exponent3(lessThan()));
    }

    // lessThanEquals

    @Test
    public void testNumberDigitExponentLessThanEqualsDigit() {
        this.numberParseThrows(digit(), exponent1(lessThanEquals()));
    }

    @Test
    public void testNumberDigitExponentDigitLessThanEqualsDigit() {
        this.numberParseThrows(digit(), exponent2(lessThanEquals()));
    }

    @Test
    public void testNumberDigitExponentDigitLessThanEquals() {
        this.numberParseThrows(digit(), exponent3(lessThanEquals()));
    }

    // notEquals

    @Test
    public void testNumberDigitExponentNotEqualsDigit() {
        this.numberParseThrows(digit(), exponent1(notEquals()));
    }

    @Test
    public void testNumberDigitExponentDigitNotEqualsDigit() {
        this.numberParseThrows(digit(), exponent2(notEquals()));
    }

    @Test
    public void testNumberDigitExponentDigitNotEquals() {
        this.numberParseThrows(digit(), exponent3(notEquals()));
    }

    // color

    @Test
    public void testNumberColorDigit() {
        this.numberParseAndCheck(color(), digit());
    }

    @Test
    public void testNumberDigitColor() {
        this.numberParseAndCheck(digit(), color());
    }

    @Test
    public void testNumberDigitDecimalColor() {
        this.numberParseAndCheck(digit(), decimalPoint(), color());
    }

    @Test
    public void testNumberDigitDecimalDigitColor() {
        this.numberParseAndCheck(digit(), decimalPoint(), digit(), color());
    }

    @Test
    public void testNumberDigitExponentColorDigit() {
        this.numberParseAndCheck(digit(), exponent1(color()));
    }

    @Test
    public void testNumberDigitExponentDigitColorDigit() {
        this.numberParseAndCheck(digit(), exponent2(color()));
    }

    @Test
    public void testNumberDigitExponentDigitColor() {
        this.numberParseAndCheck(digit(), exponent3(color()));
    }

    // condition

    @Test
    public void testNumberConditionEqualsNumber() {
        this.numberParseAndCheck(conditionEquals(), digit());
    }

    @Test
    public void testNumberConditionGreaterThanNumber() {
        this.numberParseAndCheck(conditionGreaterThan(), digit());
    }

    @Test
    public void testNumberConditionGreaterThanEqualsNumber() {
        this.numberParseAndCheck(conditionGreaterThanEquals(), digit());
    }

    @Test
    public void testNumberConditionLessThanNumber() {
        this.numberParseAndCheck(conditionLessThan(), digit());
    }

    @Test
    public void testNumberConditionLessThanEqualsNumber() {
        this.numberParseAndCheck(conditionLessThanEquals(), digit());
    }

    @Test
    public void testNumberConditionNotEqualsNumber() {
        this.numberParseAndCheck(conditionNotEquals(), digit());
    }

    @Test
    public void testNumberTokenEqualsConditionFails() {
        this.numberParseFails(
                digit(),
                conditionEquals()
        );
    }

    @Test
    public void testNumberTokenLessThanConditionFails() {
        this.numberParseFails(
                digit(),
                conditionLessThan()
        );
    }

    @Test
    public void testNumberTokenLessThanEqualsConditionFails() {
        this.numberParseFails(
                digit(),
                conditionLessThanEquals()
        );
    }

    @Test
    public void testNumberTokenGreaterThanConditionFails() {
        this.numberParseFails(
                digit(),
                conditionGreaterThan()
        );
    }

    @Test
    public void testNumberTokenGreaterThanEqualsConditionFails() {
        this.numberParseFails(
                digit(),
                conditionGreaterThanEquals()
        );
    }

    @Test
    public void testNumberTokenNotEqualsConditionFails() {
        this.numberParseFails(
                digit(),
                conditionNotEquals()
        );
    }

    // number helpers...

    private void numberParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck3(
                this.numberParser(),
                SpreadsheetFormatParserToken::number,
                tokens
        );
    }

    private void numberParseFails(final SpreadsheetFormatParserToken... tokens) {
        this.parseFailAndCheck2(
                this.numberParser(),
                tokens
        );
    }

    private void numberParseThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(this.numberParser(), tokens);
    }

    private Parser<SpreadsheetFormatParserContext> numberParser() {
        return SpreadsheetFormatParsers.number();
    }

    // fraction........................................................................................................

    @Test
    public void testFractionDayFails() {
        this.fractionParseThrows(digit(), day());
    }

    @Test
    public void testFractionHourFails() {
        this.fractionParseThrows(digit(), hour());
    }

    @Test
    public void testFractionMinuteOrMonthFails() {
        this.fractionParseThrows(digit(), monthOrMinute());
    }

    @Test
    public void testFractionSecondFails() {
        this.fractionParseThrows(digit(), second());
    }

    @Test
    public void testFractionStarFails() {
        this.fractionParseThrows(star());
    }

    @Test
    public void testFractionTextPlaceholderFails() {
        this.fractionParseThrows(digit(), textPlaceholder());
    }

    @Test
    public void testFractionUnderscoreFails() {
        this.fractionParseThrows(underscore());
    }

    @Test
    public void testFractionYearFails() {
        this.fractionParseThrows(digit(), year());
    }

    @Test
    public void testFractionSlashFails() {
        this.fractionParseThrows(fractionSymbol());
    }

    @Test
    public void testFractionDigitSpaceFractionFails() {
        this.fractionParseThrows(digitSpace(), fractionSymbol());
    }

    @Test
    public void testFractionDigitZeroFractionFails() {
        this.fractionParseThrows(digitZero(), fractionSymbol());
    }

    @Test
    public void testFractionDigitFractionFails() {
        this.fractionParseThrows(digit(), fractionSymbol());
    }

    @Test
    public void testFractionThousandsFails() {
        this.fractionParseThrows(thousands());
    }

    @Test
    public void testFractionDigitThousandsFails() {
        this.fractionParseThrows(digit(), thousands());
    }

    @Test
    public void testFractionGeneralFails() {
        this.fractionParseThrows(generalSymbol());
    }

    // digitSpace

    @Test
    public void testFractionDigitSpaceFractionDigitSpace() {
        this.fractionParseAndCheck(digitSpace(), fractionSymbol(), digitSpace());
    }

    @Test
    public void testFractionDigitSpaceDigitSpaceFractionDigitSpace() {
        this.fractionParseAndCheck(digitSpace(), digitSpace(), fractionSymbol(), digitSpace());
    }

    @Test
    public void testFractionDigitSpaceDigitZeroFractionDigitSpace() {
        this.fractionParseAndCheck(digitSpace(), digitZero(), fractionSymbol(), digitSpace());
    }

    @Test
    public void testFractionDigitSpaceDigitFractionDigitSpace() {
        this.fractionParseAndCheck(digitSpace(), digit(), fractionSymbol(), digitSpace());
    }

    @Test
    public void testFractionDigitSpaceFractionDigitSpaceDigitSpace() {
        this.fractionParseAndCheck(digitSpace(), fractionSymbol(), digitSpace(), digitSpace());
    }

    // digitSpace

    @Test
    public void testFractionDigitZeroDigitSpaceFractionDigitZero() {
        this.fractionParseAndCheck(digitZero(), digitSpace(), fractionSymbol(), digitZero());
    }

    @Test
    public void testFractionDigitZeroDigitZeroFractionDigitZero() {
        this.fractionParseAndCheck(digitZero(), digitZero(), fractionSymbol(), digitZero());
    }

    @Test
    public void testFractionDigitZeroDigitFractionDigitZero() {
        this.fractionParseAndCheck(digitZero(), digit(), fractionSymbol(), digitZero());
    }

    @Test
    public void testFractionDigitZeroFractionDigitZeroDigitZero() {
        this.fractionParseAndCheck(digitZero(), fractionSymbol(), digitZero(), digitZero());
    }

    // digitSpace

    @Test
    public void testFractionDigitFractionDigit() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitDigitSpaceFractionDigit() {
        this.fractionParseAndCheck(digit(), digitSpace(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitDigitZeroFractionDigit() {
        this.fractionParseAndCheck(digit(), digitZero(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitDigitFractionDigit() {
        this.fractionParseAndCheck(digit(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitFractionDigitDigit() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), digit());
    }

    // currency

    @Test
    public void testFractioncurrencyDigitSlashDigit() {
        this.fractionParseAndCheck(currency(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitcurrencySlashDigit() {
        this.fractionParseAndCheck(digit(), currency(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashcurrencyDigit() {
        this.fractionParseAndCheck(currency(), digit(), fractionSymbol(), currency(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitcurrency() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), currency());
    }

    // text literals

    // escaped

    @Test
    public void testFractionEscapedDigitSlashDigit() {
        this.fractionParseAndCheck(escape(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitEscapedSlashDigit() {
        this.fractionParseAndCheck(digit(), escape(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashEscapedDigit() {
        this.fractionParseAndCheck(escape(), digit(), fractionSymbol(), escape(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitEscaped() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), escape());
    }

    // quotedText

    @Test
    public void testFractionQuotedTextDigitSlashDigit() {
        this.fractionParseAndCheck(quotedText(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitQuotedTextSlashDigit() {
        this.fractionParseAndCheck(digit(), quotedText(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashQuotedTextDigit() {
        this.fractionParseAndCheck(quotedText(), digit(), fractionSymbol(), quotedText(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitQuotedText() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), quotedText());
    }

    // closeParens

    @Test
    public void testFractionCloseParensDigitSlashDigit() {
        this.fractionParseAndCheck(textLiteralCloseParens(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitCloseParensSlashDigit() {
        this.fractionParseAndCheck(digit(), textLiteralCloseParens(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashCloseParensDigit() {
        this.fractionParseAndCheck(textLiteralCloseParens(), digit(), fractionSymbol(), textLiteralCloseParens(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitCloseParens() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), textLiteralCloseParens());
    }

    // colon

    @Test
    public void testFractionColonDigitSlashDigit() {
        this.fractionParseAndCheck(textLiteralColon(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitColonSlashDigit() {
        this.fractionParseAndCheck(digit(), textLiteralColon(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashColonDigit() {
        this.fractionParseAndCheck(textLiteralColon(), digit(), fractionSymbol(), textLiteralColon(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitColon() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), textLiteralColon());
    }

    // minus

    @Test
    public void testFractionMinusDigitSlashDigit() {
        this.fractionParseAndCheck(textLiteralMinus(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitMinusSlashDigit() {
        this.fractionParseAndCheck(digit(), textLiteralMinus(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashMinusDigit() {
        this.fractionParseAndCheck(textLiteralMinus(), digit(), fractionSymbol(), textLiteralMinus(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitMinus() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), textLiteralMinus());
    }

    // openParens

    @Test
    public void testFractionOpenParensDigitSlashDigit() {
        this.fractionParseAndCheck(textLiteralOpenParens(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitOpenParensSlashDigit() {
        this.fractionParseAndCheck(digit(), textLiteralOpenParens(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashOpenParensDigit() {
        this.fractionParseAndCheck(textLiteralOpenParens(), digit(), fractionSymbol(), textLiteralOpenParens(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitOpenParens() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), textLiteralOpenParens());
    }

    // percentage

    @Test
    public void testFractionPercentageDigitSlashDigit() {
        this.fractionParseAndCheck(percentSymbol(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitPercentageSlashDigit() {
        this.fractionParseAndCheck(digit(), percentSymbol(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashPercentageDigit() {
        this.fractionParseAndCheck(percentSymbol(), digit(), fractionSymbol(), percentSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitPercentage() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), percentSymbol());
    }

    // plus

    @Test
    public void testFractionPlusDigitSlashDigit() {
        this.fractionParseAndCheck(textLiteralPlus(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitPlusSlashDigit() {
        this.fractionParseAndCheck(digit(), textLiteralPlus(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashPlusDigit() {
        this.fractionParseAndCheck(textLiteralPlus(), digit(), fractionSymbol(), textLiteralPlus(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitPlus() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), textLiteralPlus());
    }

    // space

    @Test
    public void testFractionSpaceDigitSlashDigit() {
        this.fractionParseAndCheck(textLiteralSpace(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSpaceSlashDigit() {
        this.fractionParseAndCheck(digit(), textLiteralSpace(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashSpaceDigit() {
        this.fractionParseAndCheck(textLiteralSpace(), digit(), fractionSymbol(), textLiteralSpace(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitSpace() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), textLiteralSpace());
    }

    // thousands

    @Test
    public void testFractionThousandsDigitSlashDigit() {
        this.fractionParseAndCheck(thousands(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitThousandsSlashDigit() {
        this.fractionParseAndCheck(digit(), thousands(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitSlashThousandsDigit() {
        this.fractionParseAndCheck(thousands(), digit(), fractionSymbol(), thousands(), digit());
    }

    @Test
    public void testFractionDigitSlashDigitThousands() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), thousands());
    }

    // equals

    @Test
    public void testFractionEqualsDigitFractionDigitFails() {
        this.fractionParseThrows(equalsSymbol(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitEqualsFractionDigitFails() {
        this.fractionParseThrows(digit(), equalsSymbol(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitFractionEqualsDigitFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), equalsSymbol(), digit());
    }

    @Test
    public void testFractionDigitFractionDigitEqualsFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), digit(), equalsSymbol());
    }

    // greaterThan

    @Test
    public void testFractionGreaterThanDigitFractionDigitFails() {
        this.fractionParseThrows(greaterThan(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitGreaterThanFractionDigitFails() {
        this.fractionParseThrows(digit(), greaterThan(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitFractionGreaterThanDigitFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), greaterThan(), digit());
    }

    @Test
    public void testFractionDigitFractionDigitGreaterThanFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), digit(), greaterThan());
    }

    // greaterThanEquals

    @Test
    public void testFractionGreaterThanEqualsDigitFractionDigitFails() {
        this.fractionParseThrows(greaterThanEquals(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitGreaterThanEqualsFractionDigitFails() {
        this.fractionParseThrows(digit(), greaterThanEquals(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitFractionGreaterThanEqualsDigitFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), greaterThanEquals(), digit());
    }

    @Test
    public void testFractionDigitFractionDigitGreaterThanEqualsFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), digit(), greaterThanEquals());
    }

    // lessThan

    @Test
    public void testFractionLessThanDigitFractionDigitFails() {
        this.fractionParseThrows(lessThan(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitLessThanFractionDigitFails() {
        this.fractionParseThrows(digit(), lessThan(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitFractionLessThanDigitFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), lessThan(), digit());
    }

    @Test
    public void testFractionDigitFractionDigitLessThanFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), digit(), lessThan());
    }

    // lessThanEquals

    @Test
    public void testFractionLessThanEqualsDigitFractionDigitFails() {
        this.fractionParseThrows(lessThanEquals(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitLessThanEqualsFractionDigitFails() {
        this.fractionParseThrows(digit(), lessThanEquals(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitFractionLessThanEqualsDigitFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), lessThanEquals(), digit());
    }

    @Test
    public void testFractionDigitFractionDigitLessThanEqualsFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), digit(), lessThanEquals());
    }

    // notEquals

    @Test
    public void testFractionNotEqualsDigitFractionDigitFails() {
        this.fractionParseThrows(notEquals(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitNotEqualsFractionDigitFails() {
        this.fractionParseThrows(digit(), notEquals(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitFractionNotEqualsDigitFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), notEquals(), digit());
    }

    @Test
    public void testFractionDigitFractionDigitNotEqualsFails() {
        this.fractionParseThrows(digit(), fractionSymbol(), digit(), notEquals());
    }

    // color

    @Test
    public void testFractionColorDigitFractionDigit() {
        this.fractionParseAndCheck(color(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitColorFractionDigit() {
        this.fractionParseAndCheck(digit(), color(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitColorDigitFractionDigit() {
        this.fractionParseAndCheck(digit(), color(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionDigitFractionColorDigit() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), color(), digit());
    }

    @Test
    public void testFractionDigitFractionDigitColorDigit() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), color(), digit());
    }

    @Test
    public void testFractionDigitFractionDigitColor() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), color());
    }

    // condition

    @Test
    public void testFractionConditionEqualsFraction() {
        this.fractionParseAndCheck(conditionEquals(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionConditionGreaterThanFraction() {
        this.fractionParseAndCheck(conditionGreaterThan(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionConditionGreaterThanEqualsFraction() {
        this.fractionParseAndCheck(conditionGreaterThanEquals(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionConditionLessThanFraction() {
        this.fractionParseAndCheck(conditionLessThan(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionConditionLessThanEqualsFraction() {
        this.fractionParseAndCheck(conditionLessThanEquals(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionConditionNotEqualsFraction() {
        this.fractionParseAndCheck(conditionNotEquals(), digit(), fractionSymbol(), digit());
    }

    @Test
    public void testFractionFractionConditionEquals() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), conditionEquals());
    }

    @Test
    public void testFractionFractionConditionGreaterThan() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), conditionGreaterThan());
    }

    @Test
    public void testFractionFractionConditionGreaterThanEquals() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), conditionGreaterThanEquals());
    }

    @Test
    public void testFractionFractionConditionLessThan() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), conditionLessThan());
    }

    @Test
    public void testFractionFractionConditionLessThanEquals() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), conditionLessThanEquals());
    }

    @Test
    public void testFractionFractionConditionNotEquals() {
        this.fractionParseAndCheck(digit(), fractionSymbol(), digit(), conditionNotEquals());
    }

    // fraction helpers...

    private void fractionParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck3(
                this.fractionParser(),
                SpreadsheetFormatParserToken::fraction,
                tokens
        );
    }

    private void fractionParseThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(
                this.fractionParser(),
                tokens
        );
    }

    private Parser<SpreadsheetFormatParserContext> fractionParser() {
        return SpreadsheetFormatParsers.fraction();
    }

    // general .............................................................................................

    @Test
    public void testGeneralGeneral() {
        this.generalParseAndCheck(generalSymbol());
    }

    @Test
    public void testGeneralWhitespaceGeneral() {
        this.generalParseAndCheck(whitespace(), generalSymbol());
    }

    @Test
    public void testGeneralGeneralWhitespace() {
        this.generalParseAndCheck(generalSymbol(), whitespace());
    }

    @Test
    public void testGeneralColorGeneral() {
        this.generalParseAndCheck(color(), generalSymbol());
    }

    @Test
    public void testGeneralColorWhitespaceGeneral() {
        this.generalParseAndCheck(color(), whitespace(), generalSymbol());
    }

    @Test
    public void testGeneralGeneralColor() {
        this.generalParseAndCheck(generalSymbol(), color());
    }

    @Test
    public void testGeneralGeneralColorWhitespace() {
        this.generalParseAndCheck(generalSymbol(), color(), whitespace());
    }

    @Test
    public void testGeneralGeneralWhitespaceColor() {
        this.generalParseAndCheck(generalSymbol(), whitespace(), color());
    }

    /**
     * Parsers the general expression using the general parser.
     */
    private void generalParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck3(
                this.generalParser(),
                SpreadsheetFormatParserToken::general,
                tokens
        );
    }

    private Parser<SpreadsheetFormatParserContext> generalParser() {
        return SpreadsheetFormatParsers.general();
    }

    // text........................................................................................................

    @Test
    public void testTextFormatTextDigitZeroFails() {
        this.textFormatParseThrows(digitZero());
    }

    @Test
    public void testTextFormatTextDigitSpaceFails() {
        this.textFormatParseThrows(digitSpace());
    }

    @Test
    public void testTextFormatLetterFails() {
        this.textFormatParseThrows(textLiteral('A'));
    }

    @Test
    public void testTextFormatSeparatorFails() {
        this.textFormatParseThrows(separator());
    }

    @Test
    public void testTextFormatGeneraFailsl() {
        this.textFormatParseThrows(generalSymbol());
    }

    @Test
    public void testTextFormatEscaped() {
        this.textFormatParseAndCheck(escape());
    }

    @Test
    public void testTextFormatStar() {
        this.textFormatParseAndCheck(star());
    }

    @Test
    public void testTextFormatStar2() {
        this.textFormatParseAndCheck(star2());
    }

    @Test
    public void testTextFormatStarStarFails() {
        this.textFormatParseThrows(
                star(),
                star2()
        );
    }

    @Test
    public void testTextFormatStarTextPlaceholderStarFails() {
        this.textFormatParseThrows(
                star(),
                textPlaceholder(),
                star2()
        );
    }

    // text literals

    @Test
    public void testTextFormatDollar() {
        this.textFormatParseAndCheck(
                textLiteralDollar()
        );
    }

    @Test
    public void testTextFormatMinusSign() {
        this.textFormatParseAndCheck(
                textLiteralMinus()
        );
    }

    @Test
    public void testTextFormatPlusSign() {
        this.textFormatParseAndCheck(
                textLiteralPlus()
        );
    }

    @Test
    public void testTextFormatSlash() {
        this.textFormatParseAndCheck(
                textLiteralSlash()
        );
    }

    @Test
    public void testTextFormatOpenParens() {
        this.textFormatParseAndCheck(
                textLiteralOpenParens()
        );
    }

    @Test
    public void testTextFormatCloseParens() {
        this.textFormatParseAndCheck(
                textLiteralCloseParens()
        );
    }

    @Test
    public void testTextFormatColon() {
        this.textFormatParseAndCheck(
                textLiteral(':')
        );
    }

    @Test
    public void testTextFormatEqualsSign() {
        this.textFormatParseAndCheck(
                textLiteral('=')
        );
    }

    @Test
    public void testTextFormatGreaterThanEquals() {
        this.textFormatParseAndCheck(
                textLiteral('>')
        );
    }

    @Test
    public void testTextFormatGreaterThanEqualsSign() {
        this.textFormatParseAndCheck(
                textLiteral(">=")
        );
    }

    @Test
    public void testTextFormatLessThan() {
        this.textFormatParseAndCheck(
                textLiteral('<')
        );
    }

    @Test
    public void testTextFormatLessThanEqualsSign() {
        this.textFormatParseAndCheck(
                textLiteral("<=")
        );
    }

    @Test
    public void testTextFormatNotEqualsSign() {
        this.textFormatParseAndCheck(
                textLiteral("!=")
        );
    }

    @Test
    public void testTextFormatSpace() {
        this.textFormatParseAndCheck(textLiteralSpace());
    }

    @Test
    public void testTextFormatTextPlaceholder() {
        this.textFormatParseAndCheck(textPlaceholder());
    }

    @Test
    public void testTextFormatTextPlaceholder2() {
        this.textFormatParseAndCheck(textPlaceholder(), textPlaceholder());
    }

    @Test
    public void testTextFormatTextQuoted() {
        this.textFormatParseAndCheck(quotedText());
    }

    @Test
    public void testTextFormatUnderscore() {
        this.textFormatParseAndCheck(underscore());
    }

    @Test
    public void testTextFormatUnderscore2() {
        this.textFormatParseAndCheck(underscore());
    }

    @Test
    public void testTextFormatUnderscoreUnderscore() {
        this.textFormatParseAndCheck(underscore(), underscore2());
    }

    @Test
    public void testTextFormatAll() {
        this.textFormatParseAndCheck(textLiteralSpace(), quotedText(), textPlaceholder(), underscore());
    }

    @Test
    public void testTextFormatColorQuotedText() {
        this.textFormatParseAndCheck(color(), quotedText());
    }

    @Test
    public void testTextFormatQuotedTextColor() {
        this.textFormatParseAndCheck(quotedText(), color());
    }

    @Test
    public void testTextFormatConditionNotEqualsTextPlaceholderFails() {
        this.textFormatParseThrows(
                conditionNotEquals(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionEqualsTextPlaceholderFails() {
        this.textFormatParseThrows(
                conditionEquals(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionGreaterThanTextPlaceholderFails() {
        this.textFormatParseThrows(
                conditionGreaterThan(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionGreaterThanEqualsTextPlaceholderFails() {
        this.textFormatParseThrows(
                conditionGreaterThanEquals(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionLessThanTextPlaceholderFails() {
        this.textFormatParseThrows(
                conditionLessThan(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionLessThanEqualsTextPlaceholderFails() {
        this.textFormatParseThrows(
                conditionLessThanEquals(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionEqualsFails() {
        this.textFormatParseThrows(
                textPlaceholder(),
                conditionEquals()
        );
    }

    @Test
    public void testTextFormatPlaceholderConditionGreaterThanFails() {
        this.textFormatParseThrows(
                textPlaceholder(),
                conditionGreaterThan()
        );
    }

    @Test
    public void testTextFormatPlaceholderConditionGreaterThanEqualsFails() {
        this.textFormatParseThrows(
                textPlaceholder(),
                conditionGreaterThanEquals()
        );
    }

    @Test
    public void testTextFormatPlaceholderConditionLessThanFails() {
        this.textFormatParseThrows(
                textPlaceholder(),
                conditionLessThan()
        );
    }

    @Test
    public void testTextFormatPlaceholderConditionLessThanEqualsFails() {
        this.textFormatParseThrows(
                textPlaceholder(),
                conditionLessThanEquals()
        );
    }

    @Test
    public void testTextFormatPlaceholderConditionNotEqualsFails() {
        this.textFormatParseThrows(
                textPlaceholder(),
                conditionNotEquals()
        );
    }

    // multiple patterns................................................................................................

    @Test
    public void testTextFormatConditionNotEqualsTextPlaceholderPatternSeparatorTextPlaceholder() {
        this.textFormatParseAndCheck(
                conditionNotEquals(),
                textPlaceholder(),
                separator(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionEqualsTextPlaceholderPatternSeparatorTextPlaceholder() {
        this.textFormatParseAndCheck(
                conditionEquals(),
                textPlaceholder(),
                separator(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionGreaterThanTextPlaceholderPatternSeparatorTextPlaceholder() {
        this.textFormatParseAndCheck(
                conditionGreaterThan(),
                textPlaceholder(),
                separator(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionGreaterThanEqualsTextPlaceholderPatternSeparatorTextPlaceholder() {
        this.textFormatParseAndCheck(
                conditionGreaterThanEquals(),
                textPlaceholder(),
                separator(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionLessThanTextPlaceholderPatternSeparatorTextPlaceholder() {
        this.textFormatParseAndCheck(
                conditionLessThan(),
                textPlaceholder(),
                separator(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionLessThanEqualsTextPlaceholderPatternSeparatorTextPlaceholder() {
        this.textFormatParseAndCheck(
                conditionLessThanEquals(),
                textPlaceholder(),
                separator(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatPatternSeparatorPatternFails() {
        this.textFormatParseThrows(
                textPlaceholder(),
                separator(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatConditionColorPatternSeparatorColorPattern() {
        this.textFormatParseAndCheck(
                conditionEquals(),
                color(),
                textPlaceholder(),
                separator(),
                color(),
                textPlaceholder()
        );
    }

    @Test
    public void testTextFormatPatternSeparator() {
        this.textFormatParseAndCheck(
                textPlaceholder(),
                separator()
        );
    }

    @Test
    public void testTextFormatPatternSeparatorPatternSeparatorFails() {
        this.textFormatParseThrows(
                textPlaceholder(),
                separator(),
                textPlaceholder(),
                separator()
        );
    }

    @Test
    public void testTextFormatConditionPatternSeparatorPatternSeparator() {
        this.textFormatParseAndCheck(
                conditionEquals(),
                textPlaceholder(),
                separator(),
                textPlaceholder(),
                separator()
        );
    }

    // text helpers......................................................................................................

    private void textFormatParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck3(
                SpreadsheetFormatParsers.textFormat(),
                SpreadsheetFormatParserToken::text,
                tokens
        );
    }

    private void textFormatParseThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(
                SpreadsheetFormatParsers.textFormat(),
                tokens
        );
    }

    // time format......................................................................................................

    @Test
    public void testTimeFormatTextDigitFails() {
        this.timeFormatParseThrows(
                digit()
        );
    }

    @Test
    public void testTimeFormatTextDigitZeroFails() {
        this.timeFormatParseThrows(
                digitZero()
        );
    }

    @Test
    public void testTimeFormatTextDigitSpaceFails() {
        this.timeFormatParseThrows(
                digitSpace()
        );
    }

    @Test
    public void testTimeFormatDayFails() {
        this.timeFormatParseThrows(
                day()
        );
    }

    @Test
    public void testTimeFormatYearFails() {
        this.timeFormatParseThrows(
                year()
        );
    }

    @Test
    public void testTimeFormatTextPlaceholderFails() {
        this.timeFormatParseThrows(
                textPlaceholder()
        );
    }

    @Test
    public void testTimeFormatGeneral() {
        this.timeFormatParseAndCheck(
                general()
        );
    }

    @Test
    public void testTimeFormatWhitespaceGeneral() {
        this.timeFormatParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testTimeFormatGeneralWhitespace() {
        this.timeFormatParseAndCheck(
                general(
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testTimeFormatWhitespaceGeneralWhitespace() {
        this.timeFormatParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testTimeFormatColorGeneral() {
        this.timeFormatParseAndCheck(
                general(
                        color(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testTimeFormatColorWhitespaceGeneral() {
        this.timeFormatParseAndCheck(
                general(
                        color(),
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testTimeFormatColorEscaped() {
        this.timeFormatParseAndCheck(
                time(
                        color(),
                        escape()
                )
        );
    }

    @Test
    public void testTimeFormatEscaped() {
        this.timeFormatParseAndCheck(
                time(
                        escape()
                )
        );
    }

    @Test
    public void testTimeFormatDollar() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralDollar()
                )
        );
    }

    @Test
    public void testTimeFormatMinus() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralMinus()
                )
        );
    }

    @Test
    public void testTimeFormatPlus() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralPlus()
                )
        );
    }

    @Test
    public void testTimeFormatSlash() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralSlash()
                )
        );
    }

    @Test
    public void testTimeFormatOpenParen() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralOpenParens()
                )
        );
    }

    @Test
    public void testTimeFormatCloseParen() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralCloseParens()
                )
        );
    }

    @Test
    public void testTimeFormatColon() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralColon()
                )
        );
    }

    @Test
    public void testTimeFormatSpace() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralSpace()
                )
        );
    }

    @Test
    public void testTimeFormatQuotedText() {
        this.timeFormatParseAndCheck(
                time(
                        quotedText()
                )
        );
    }

    @Test
    public void testTimeFormatASlashP() {
        this.timeFormatParseAndCheck(
                time(
                        aSlashP()
                )
        );
    }

    @Test
    public void testTimeFormatAmSlashPm() {
        this.timeFormatParseAndCheck(
                time(
                        amSlashPm()
                )
        );
    }

    @Test
    public void testTimeFormatHour() {
        this.timeFormatParseAndCheck(
                time(
                        hour()
                )
        );
    }

    @Test
    public void testTimeFormatMinute() {
        this.timeFormatParseAndCheck(
                time(
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testTimeFormatHourMinuteSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMinuteSecondASlashP() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        aSlashP()
                )
        );
    }

    @Test
    public void testTimeFormatHourMinuteSecondAmSlashPm() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        amSlashPm()
                )
        );
    }

    @Test
    public void testTimeFormatHour2Minute2Second2() {
        this.timeFormatParseAndCheck(
                time(
                        hour(2),
                        monthOrMinute(2),
                        second(2)
                )
        );
    }

    @Test
    public void testTimeFormatHour3Minute3Second3() {
        this.timeFormatParseAndCheck(
                time(
                        hour(3),
                        monthOrMinute(3),
                        second(3)
                )
        );
    }

    @Test
    public void testTimeFormatHourMinuteSecondHourMinuteSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatMinuteHourSecond() {
        this.timeFormatParseAndCheck(
                time(
                        monthOrMinute(),
                        hour(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatSecondMinuteHour() {
        this.timeFormatParseAndCheck(
                time(
                        second(),
                        monthOrMinute(),
                        hour()
                )
        );
    }

    @Test
    public void testTimeFormatSecondCommaMinuteCommaHour() {
        this.timeFormatParseAndCheck(
                time(
                        second(),
                        textLiteralComma(),
                        monthOrMinute(),
                        textLiteralComma(),
                        hour()
                )
        );
    }

    // escaped

    @Test
    public void testTimeFormatEscapedHourMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        escape(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourEscapedMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        escape(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthEscapedSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        escape(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsEscaped() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        escape()
                )
        );
    }

    // quotedText

    @Test
    public void testTimeFormatQuotedTextHourMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        quotedText(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourQuotedTextMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        quotedText(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthQuotedTextSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        quotedText(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsQuotedText() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        quotedText()
                )
        );
    }

    // closeParens

    @Test
    public void testTimeFormatCloseParensHourMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralCloseParens(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourCloseParensMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        textLiteralCloseParens(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthCloseParensSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralCloseParens(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsCloseParens() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralCloseParens()
                )
        );
    }

    // colon

    @Test
    public void testTimeFormatColonHourMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralColon(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourColonMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        textLiteralColon(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthColonSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralColon(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsColon() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralColon()
                )
        );
    }

    // dollar

    @Test
    public void testTimeFormatDollarHourMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralDollar(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourDollarMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        textLiteralDollar(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthDollarSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralDollar(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsDollar() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralDollar()
                )
        );
    }

    // minus

    @Test
    public void testTimeFormatMinusHourMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralMinus(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMinusMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        textLiteralMinus(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthMinusSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralMinus(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsMinus() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralMinus()
                )
        );
    }

    // openParens

    @Test
    public void testTimeFormatOpenParensHourMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralOpenParens(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourOpenParensMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        textLiteralOpenParens(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthOpenParensSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralOpenParens(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsOpenParens() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralOpenParens()
                )
        );
    }

    // plus

    @Test
    public void testTimeFormatPlusHourMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralPlus(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourPlusMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        textLiteralPlus(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthPlusSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralPlus(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsPlus() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralPlus()
                )
        );
    }

    // slash

    @Test
    public void testTimeFormatSlashHourMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralSlash(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourSlashMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        textLiteralSlash(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSlashSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralSlash(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsSlash() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralSlash()
                )
        );
    }

    // space

    @Test
    public void testTimeFormatSpaceHourMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        textLiteralSpace(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourSpaceMonthSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        textLiteralSpace(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSpaceSecond() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralSpace(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsSpace() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralSpace()
                )
        );
    }

    // equals

    @Test
    public void testTimeFormatEqualsHourMonthSecondFails() {
        this.timeFormatParseThrows(
                equalsSymbol(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourEqualsMonthSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                equalsSymbol(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthEqualsSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                equalsSymbol(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsEqualsFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                equalsSymbol()
        );
    }

    // greaterThan

    @Test
    public void testTimeFormatGreaterThanHourMonthSecondFails() {
        this.timeFormatParseThrows(
                greaterThan(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourGreaterThanMonthSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                greaterThan(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthGreaterThanSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                greaterThan(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsGreaterThanFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                greaterThan()
        );
    }

    // greaterThanEquals

    @Test
    public void testTimeFormatGreaterThanEqualsHourMonthSecondFails() {
        this.timeFormatParseThrows(
                greaterThanEquals(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourGreaterThanEqualsMonthSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                greaterThanEquals(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthGreaterThanEqualsSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                greaterThanEquals(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsGreaterThanEqualsFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                greaterThanEquals()
        );
    }

    // lessThan

    @Test
    public void testTimeFormatLessThanHourMonthSecondFails() {
        this.timeFormatParseThrows(
                lessThan(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourLessThanMonthSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                lessThan(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthLessThanSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                lessThan(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsLessThanFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                lessThan()
        );
    }

    // lessThanEquals

    @Test
    public void testTimeFormatLessThanEqualsHourMonthSecondFails() {
        this.timeFormatParseThrows(
                lessThanEquals(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourLessThanEqualsMonthSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                lessThanEquals(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthLessThanEqualsSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                lessThanEquals(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsLessThanEqualsFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                lessThanEquals()
        );
    }

    // notEquals

    @Test
    public void testTimeFormatNotEqualsHourMonthSecondFails() {
        this.timeFormatParseThrows(
                notEquals(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourNotEqualsMonthSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                notEquals(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthNotEqualsSecondFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                notEquals(),
                second()
        );
    }

    @Test
    public void testTimeFormatHourMonthSecondsNotEqualsFails() {
        this.timeFormatParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                notEquals()
        );
    }

    // color

    @Test
    public void testTimeFormatColorHour() {
        this.timeFormatParseAndCheck(
                time(
                        color(),
                        hour()
                )
        );
    }

    @Test
    public void testTimeFormatColorMinute() {
        this.timeFormatParseAndCheck(
                time(
                        color(),
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testTimeFormatColorSeconds() {
        this.timeFormatParseAndCheck(
                time(
                        color(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatHourColor() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        color()
                )
        );
    }

    @Test
    public void testTimeFormatMinuteColor() {
        this.timeFormatParseAndCheck(
                time(
                        monthOrMinute(),
                        color()
                )
        );
    }

    @Test
    public void testTimeFormatSecondsColor() {
        this.timeFormatParseAndCheck(
                time(
                        second(),
                        color()
                )
        );
    }

    // condition

    @Test
    public void testTimeFormatConditionEqualsHourFails() {
        this.timeFormatParseThrows(
                conditionEquals(),
                hour()
        );
    }

    @Test
    public void testTimeFormatConditionGreaterThanHourFails() {
        this.timeFormatParseThrows(
                conditionGreaterThan(),
                hour()
        );
    }

    @Test
    public void testTimeFormatConditionGreaterThanEqualsHourFails() {
        this.timeFormatParseThrows(
                conditionGreaterThanEquals(),
                hour()
        );
    }

    @Test
    public void testTimeFormatConditionLessThanHourFails() {
        this.timeFormatParseThrows(
                conditionLessThan(),
                hour()
        );
    }

    @Test
    public void testTimeFormatConditionLessThanEqualsHourFails() {
        this.timeFormatParseThrows(
                conditionLessThanEquals(),
                hour()
        );
    }

    @Test
    public void testTimeFormatConditionNotEqualsHourFails() {
        this.timeFormatParseThrows(
                conditionNotEquals(),
                hour()
        );
    }

    @Test
    public void testTimeFormatHourConditionEqualsFails() {
        this.timeFormatParseThrows(
                hour(),
                conditionEquals()
        );
    }

    @Test
    public void testTimeFormatHourConditionGreaterThanFails() {
        this.timeFormatParseThrows(
                hour(),
                conditionGreaterThan()
        );
    }

    @Test
    public void testTimeFormatHourConditionGreaterThanEqualsFails() {
        this.timeFormatParseThrows(
                hour(),
                conditionGreaterThanEquals()
        );
    }

    @Test
    public void testTimeFormatHourConditionLessThanFails() {
        this.timeFormatParseThrows(
                hour(),
                conditionLessThan()
        );
    }

    @Test
    public void testTimeFormatHourConditionLessThanEqualsFails() {
        this.timeFormatParseThrows(
                hour(),
                conditionLessThanEquals()
        );
    }

    @Test
    public void testTimeFormatHourConditionNotEqualsFails() {
        this.timeFormatParseThrows(
                hour(),
                conditionNotEquals()
        );
    }

    @Test
    public void testTimeFormatPatternSeparator() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute()
                ),
                separator()
        );
    }

    @Test
    public void testTimeFormatPatternSeparatorPattern() {
        this.timeFormatParseAndCheck(
                time(
                        hour(),
                        monthOrMinute()
                ),
                separator(),
                time(
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatColorPatternSeparatorPattern() {
        this.timeFormatParseAndCheck(
                time(
                        color(),
                        hour(),
                        monthOrMinute()
                ),
                separator(),
                time(
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeFormatConditionPatternSeparatorPattern() {
        this.timeFormatParseAndCheck(
                conditionEquals(),
                time(
                        hour(),
                        monthOrMinute()
                ),
                separator(),
                time(
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    // time format helpers..............................................................................................

    private void timeFormatParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck2(
                SpreadsheetFormatParsers.timeFormat(),
                tokens
        );
    }

    private void timeFormatParseThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(
                SpreadsheetFormatParsers.timeFormat(),
                tokens
        );
    }

    // time parse.......................................................................................................

    @Test
    public void testTimeParseTextDigitFails() {
        this.timeParseParseThrows(
                digit()
        );
    }

    @Test
    public void testTimeParseTextDigitZeroFails() {
        this.timeParseParseThrows(
                digitZero()
        );
    }

    @Test
    public void testTimeParseTextDigitSpaceFails() {
        this.timeParseParseThrows(
                digitSpace()
        );
    }

    @Test
    public void testTimeParseDayFails() {
        this.timeParseParseThrows(
                day()
        );
    }

    @Test
    public void testTimeParseYearFails() {
        this.timeParseParseThrows(
                year()
        );
    }

    @Test
    public void testTimeParseTextPlaceholderFails() {
        this.timeParseParseThrows(
                textPlaceholder()
        );
    }

    @Test
    public void testTimeParseGeneral() {
        this.timeParseParseAndCheck(
                general()
        );
    }

    @Test
    public void testTimeParseWhitespaceGeneral() {
        this.timeParseParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testTimeParseGeneralWhitespace() {
        this.timeParseParseAndCheck(
                general(
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testTimeParseWhitespaceGeneralWhitespace() {
        this.timeParseParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testTimeParseColorGeneral() {
        this.timeParseParseAndCheck(
                general(
                        color(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testTimeParseColorWhitespaceGeneral() {
        this.timeParseParseAndCheck(
                general(
                        color(),
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testTimeParseColorEscapedFails() {
        this.timeParseParseThrows(
                time(
                        color(),
                        escape()
                )
        );
    }

    @Test
    public void testTimeParseEscaped() {
        this.timeParseParseAndCheck(
                time(
                        escape()
                )
        );
    }

    @Test
    public void testTimeParseDollar() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralDollar()
                )
        );
    }

    @Test
    public void testTimeParseMinus() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralMinus()
                )
        );
    }

    @Test
    public void testTimeParsePlus() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralPlus()
                )
        );
    }

    @Test
    public void testTimeParseSlash() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralSlash()
                )
        );
    }

    @Test
    public void testTimeParseOpenParen() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralOpenParens()
                )
        );
    }

    @Test
    public void testTimeParseCloseParen() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralCloseParens()
                )
        );
    }

    @Test
    public void testTimeParseColon() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralColon()
                )
        );
    }

    @Test
    public void testTimeParseSpace() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralSpace()
                )
        );
    }

    @Test
    public void testTimeParseQuotedText() {
        this.timeParseParseAndCheck(
                time(
                        quotedText()
                )
        );
    }

    @Test
    public void testTimeParseASlashP() {
        this.timeParseParseAndCheck(
                time(
                        aSlashP()
                )
        );
    }

    @Test
    public void testTimeParseAmSlashPm() {
        this.timeParseParseAndCheck(
                time(
                        amSlashPm()
                )
        );
    }

    @Test
    public void testTimeParseHour() {
        this.timeParseParseAndCheck(
                time(
                        hour()
                )
        );
    }

    @Test
    public void testTimeParseMinute() {
        this.timeParseParseAndCheck(
                time(
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testTimeParseHourMinuteSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMinuteSecondASlashP() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        aSlashP()
                )
        );
    }

    @Test
    public void testTimeParseHourMinuteSecondAmSlashPm() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        amSlashPm()
                )
        );
    }

    @Test
    public void testTimeParseHour2Minute2Second2() {
        this.timeParseParseAndCheck(
                time(
                        hour(2),
                        monthOrMinute(2),
                        second(2)
                )
        );
    }

    @Test
    public void testTimeParseHour3Minute3Second3() {
        this.timeParseParseAndCheck(
                time(
                        hour(3),
                        monthOrMinute(3),
                        second(3)
                )
        );
    }

    @Test
    public void testTimeParseHourMinuteSecondHourMinuteSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseMinuteHourSecond() {
        this.timeParseParseAndCheck(
                time(
                        monthOrMinute(),
                        hour(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseSecondMinuteHour() {
        this.timeParseParseAndCheck(
                time(
                        second(),
                        monthOrMinute(),
                        hour()
                )
        );
    }

    @Test
    public void testTimeParseSecondCommaMinuteCommaHour() {
        this.timeParseParseAndCheck(
                time(
                        second(),
                        textLiteralComma(),
                        monthOrMinute(),
                        textLiteralComma(),
                        hour()
                )
        );
    }

    // escaped

    @Test
    public void testTimeParseEscapedHourMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        escape(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourEscapedMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        escape(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthEscapedSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        escape(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsEscaped() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        escape()
                )
        );
    }

    // quotedText

    @Test
    public void testTimeParseQuotedTextHourMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        quotedText(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourQuotedTextMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        quotedText(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthQuotedTextSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        quotedText(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsQuotedText() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        quotedText()
                )
        );
    }

    // closeParens

    @Test
    public void testTimeParseCloseParensHourMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralCloseParens(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourCloseParensMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        textLiteralCloseParens(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthCloseParensSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralCloseParens(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsCloseParens() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralCloseParens()
                )
        );
    }

    // colon

    @Test
    public void testTimeParseColonHourMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralColon(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourColonMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        textLiteralColon(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthColonSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralColon(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsColon() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralColon()
                )
        );
    }

    // dollar

    @Test
    public void testTimeParseDollarHourMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralDollar(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourDollarMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        textLiteralDollar(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthDollarSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralDollar(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsDollar() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralDollar()
                )
        );
    }

    // minus

    @Test
    public void testTimeParseMinusHourMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralMinus(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMinusMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        textLiteralMinus(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthMinusSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralMinus(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsMinus() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralMinus()
                )
        );
    }

    // openParens

    @Test
    public void testTimeParseOpenParensHourMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralOpenParens(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourOpenParensMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        textLiteralOpenParens(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthOpenParensSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralOpenParens(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsOpenParens() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralOpenParens()
                )
        );
    }

    // plus

    @Test
    public void testTimeParsePlusHourMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralPlus(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourPlusMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        textLiteralPlus(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthPlusSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralPlus(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsPlus() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralPlus()
                )
        );
    }

    // slash

    @Test
    public void testTimeParseSlashHourMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralSlash(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourSlashMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        textLiteralSlash(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSlashSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralSlash(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsSlash() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralSlash()
                )
        );
    }

    // space

    @Test
    public void testTimeParseSpaceHourMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        textLiteralSpace(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourSpaceMonthSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        textLiteralSpace(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSpaceSecond() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        textLiteralSpace(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsSpace() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute(),
                        second(),
                        textLiteralSpace()
                )
        );
    }

    // equals

    @Test
    public void testTimeParseEqualsHourMonthSecondFails() {
        this.timeParseParseThrows(
                equalsSymbol(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourEqualsMonthSecondFails() {
        this.timeParseParseThrows(
                hour(),
                equalsSymbol(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthEqualsSecondFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                equalsSymbol(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsEqualsFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                equalsSymbol()
        );
    }

    // greaterThan

    @Test
    public void testTimeParseGreaterThanHourMonthSecondFails() {
        this.timeParseParseThrows(
                greaterThan(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourGreaterThanMonthSecondFails() {
        this.timeParseParseThrows(
                hour(),
                greaterThan(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthGreaterThanSecondFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                greaterThan(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsGreaterThanFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                greaterThan()
        );
    }

    // greaterThanEquals

    @Test
    public void testTimeParseGreaterThanEqualsHourMonthSecondFails() {
        this.timeParseParseThrows(
                greaterThanEquals(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourGreaterThanEqualsMonthSecondFails() {
        this.timeParseParseThrows(
                hour(),
                greaterThanEquals(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthGreaterThanEqualsSecondFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                greaterThanEquals(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsGreaterThanEqualsFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                greaterThanEquals()
        );
    }

    // lessThan

    @Test
    public void testTimeParseLessThanHourMonthSecondFails() {
        this.timeParseParseThrows(
                lessThan(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourLessThanMonthSecondFails() {
        this.timeParseParseThrows(
                hour(),
                lessThan(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthLessThanSecondFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                lessThan(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsLessThanFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                lessThan()
        );
    }

    // lessThanEquals

    @Test
    public void testTimeParseLessThanEqualsHourMonthSecondFails() {
        this.timeParseParseThrows(
                lessThanEquals(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourLessThanEqualsMonthSecondFails() {
        this.timeParseParseThrows(
                hour(),
                lessThanEquals(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthLessThanEqualsSecondFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                lessThanEquals(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsLessThanEqualsFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                lessThanEquals()
        );
    }

    // notEquals

    @Test
    public void testTimeParseNotEqualsHourMonthSecondFails() {
        this.timeParseParseThrows(
                notEquals(),
                hour(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourNotEqualsMonthSecondFails() {
        this.timeParseParseThrows(
                hour(),
                notEquals(),
                monthOrMinute(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthNotEqualsSecondFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                notEquals(),
                second()
        );
    }

    @Test
    public void testTimeParseHourMonthSecondsNotEqualsFails() {
        this.timeParseParseThrows(
                hour(),
                monthOrMinute(),
                second(),
                notEquals()
        );
    }

    // color

    @Test
    public void testTimeParseColorHourFails() {
        this.timeParseParseThrows(
                time(
                        color(),
                        hour()
                )
        );
    }

    @Test
    public void testTimeParseColorMinuteFails() {
        this.timeParseParseThrows(
                time(
                        color(),
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testTimeParseColorSecondsFails() {
        this.timeParseParseThrows(
                time(
                        color(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseHourColorFails() {
        this.timeParseParseThrows(
                time(
                        hour(),
                        color()
                )
        );
    }

    @Test
    public void testTimeParseMinuteColorFails() {
        this.timeParseParseThrows(
                time(
                        monthOrMinute(),
                        color()
                )
        );
    }

    @Test
    public void testTimeParseSecondsColorFails() {
        this.timeParseParseThrows(
                time(
                        second(),
                        color()
                )
        );
    }

    // condition

    @Test
    public void testTimeParseConditionEqualsHourFails() {
        this.timeParseParseThrows(
                conditionEquals(),
                hour()
        );
    }

    @Test
    public void testTimeParseConditionGreaterThanHourFails() {
        this.timeParseParseThrows(
                conditionGreaterThan(),
                hour()
        );
    }

    @Test
    public void testTimeParseConditionGreaterThanEqualsHourFails() {
        this.timeParseParseThrows(
                conditionGreaterThanEquals(),
                hour()
        );
    }

    @Test
    public void testTimeParseConditionLessThanHourFails() {
        this.timeParseParseThrows(
                conditionLessThan(),
                hour()
        );
    }

    @Test
    public void testTimeParseConditionLessThanEqualsHourFails() {
        this.timeParseParseThrows(
                conditionLessThanEquals(),
                hour()
        );
    }

    @Test
    public void testTimeParseConditionNotEqualsHourFails() {
        this.timeParseParseThrows(
                conditionNotEquals(),
                hour()
        );
    }

    @Test
    public void testTimeParseHourConditionEqualsFails() {
        this.timeParseParseThrows(
                hour(),
                conditionEquals()
        );
    }

    @Test
    public void testTimeParseHourConditionGreaterThanFails() {
        this.timeParseParseThrows(
                hour(),
                conditionGreaterThan()
        );
    }

    @Test
    public void testTimeParseHourConditionGreaterThanEqualsFails() {
        this.timeParseParseThrows(
                hour(),
                conditionGreaterThanEquals()
        );
    }

    @Test
    public void testTimeParseHourConditionLessThanFails() {
        this.timeParseParseThrows(
                hour(),
                conditionLessThan()
        );
    }

    @Test
    public void testTimeParseHourConditionLessThanEqualsFails() {
        this.timeParseParseThrows(
                hour(),
                conditionLessThanEquals()
        );
    }

    @Test
    public void testTimeParseHourConditionNotEqualsFails() {
        this.timeParseParseThrows(
                hour(),
                conditionNotEquals()
        );
    }

    @Test
    public void testTimeParsePatternSeparator() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute()
                ),
                separator()
        );
    }

    @Test
    public void testTimeParsePatternSeparatorPattern() {
        this.timeParseParseAndCheck(
                time(
                        hour(),
                        monthOrMinute()
                ),
                separator(),
                time(
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testTimeParseColorPatternSeparatorPatternFails() {
        this.timeParseParseThrows(
                time(
                        color(),
                        hour(),
                        monthOrMinute()
                ),
                separator(),
                time(
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    private void timeParseParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck2(
                SpreadsheetFormatParsers.timeParse(),
                tokens
        );
    }

    private void timeParseParseThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(
                SpreadsheetFormatParsers.timeParse(),
                tokens
        );
    }

    // dateTime format..................................................................................................

    @Test
    public void testDateTimeFormatGeneral() {
        this.dateTimeFormatParseAndCheck(
                general()
        );
    }

    @Test
    public void testDateTimeFormatWhitespaceGeneral() {
        this.dateTimeFormatParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateTimeFormatGeneralWhitespace() {
        this.dateTimeFormatParseAndCheck(
                general(
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testDateTimeFormatWhitespaceGeneralWhitespace() {
        this.dateTimeFormatParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testDateTimeFormatColorGeneral() {
        this.dateTimeFormatParseAndCheck(
                general(
                        color(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateTimeFormatColorWhitespaceGeneral() {
        this.dateTimeFormatParseAndCheck(
                general(
                        color(),
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateTimeFormatColorEscaped() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        color(),
                        escape()
                )
        );
    }

    @Test
    public void testDateTimeFormatEscaped() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        escape()
                )
        );
    }

    @Test
    public void testDateTimeFormatDollar() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        textLiteralDollar()
                )
        );
    }

    @Test
    public void testDateTimeFormatMinus() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        textLiteralMinus()
                )
        );
    }

    @Test
    public void testDateTimeFormatPlus() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        textLiteralPlus()
                )
        );
    }

    @Test
    public void testDateTimeFormatSlash() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        textLiteralSlash()
                )
        );
    }

    @Test
    public void testDateTimeFormatOpenParen() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        textLiteralOpenParens()
                )
        );
    }

    @Test
    public void testDateTimeFormatCloseParen() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        textLiteralCloseParens()
                )
        );
    }

    @Test
    public void testDateTimeFormatColon() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        textLiteralColon()
                )
        );
    }

    @Test
    public void testDateTimeFormatSpace() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        textLiteralSpace()
                )
        );
    }

    @Test
    public void testDateTimeFormatQuotedText() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        quotedText()
                )
        );
    }

    // date only........................................................................................................

    @Test
    public void testDateTimeFormatDay() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        day()
                )
        );
    }

    @Test
    public void testDateTimeFormatDayMonth() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        day(),
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testDateTimeFormatDayMonthYear() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    // time only........................................................................................................

    @Test
    public void testDateTimeFormatHour() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        hour()
                )
        );
    }

    @Test
    public void testDateTimeFormatHourMinute() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        hour(),
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testDateTimeFormatHourMinuteSecond() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    // time with millis..................................................................................................

    @Test
    public void testDateTimeFormatSecondsDecimal() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        second(),
                        decimalPoint()
                )
        );
    }

    @Test
    public void testDateTimeFormatSecondsDecimalNonZeroFails() {
        this.dateTimeFormatParseThrows(
                second(),
                decimalPoint(),
                digit()
        );
    }

    @Test
    public void testDateTimeFormatSecondsDecimalSpaceFails() {
        this.dateTimeFormatParseThrows(
                second(),
                decimalPoint(),
                digitSpace()
        );
    }

    @Test
    public void testDateTimeFormatSecondsDecimalDigitZero() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        second(),
                        decimalPoint(),
                        digitZero()
                )
        );
    }

    @Test
    public void testDateTimeFormatSecondsDecimalDigitZeroZero() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        second(),
                        decimalPoint(),
                        digitZero(),
                        digitZero()
                )
        );
    }

    // date&time........................................................................................................

    @Test
    public void testDateTimeFormatDayHour() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        day(),
                        hour()
                )
        );
    }

    @Test
    public void testDateTimeFormatDayHour2() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        day(),
                        hour(),
                        day(),
                        hour()
                )
        );
    }

    @Test
    public void testDateTimeFormatDayMonthYearHourMinuteSecond() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        day(),
                        monthOrMinute(),
                        year(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testDateTimeFormatDayMonthYearHourMinuteSecond2() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        day(),
                        monthOrMinute(),
                        year(),
                        hour(),
                        monthOrMinute(),
                        second(),
                        day(),
                        monthOrMinute(),
                        year(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testDateTimeFormatDayMonthYearHourMinuteSecondMillis() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        day(),
                        monthOrMinute(),
                        year(),
                        hour(),
                        monthOrMinute(),
                        second(),
                        day(),
                        monthOrMinute(),
                        year(),
                        hour(),
                        monthOrMinute(),
                        second(),
                        decimalPoint(),
                        digitZero()
                )
        );
    }

    @Test
    public void testDateTimeFormatColorDay() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        color(),
                        day()
                )
        );
    }

    @Test
    public void testDateTimeFormatDayColor() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        day(),
                        color()
                )
        );
    }

    @Test
    public void testDateTimeFormatDayCommaMonthCommaYear() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        day(),
                        textLiteralComma(),
                        monthOrMinute(),
                        textLiteralComma(),
                        year()
                )
        );
    }

    // condition

    @Test
    public void testDateTimeFormatConditionEqualsDayFails() {
        this.dateTimeFormatParseThrows(
                conditionEquals(),
                day()
        );
    }

    @Test
    public void testDateTimeFormatConditionGreaterThanDayFails() {
        this.dateTimeFormatParseThrows(
                conditionGreaterThan(),
                day()
        );
    }

    @Test
    public void testDateTimeFormatConditionGreaterThanEqualsDayFails() {
        this.dateTimeFormatParseThrows(
                conditionGreaterThanEquals(),
                day()
        );
    }

    @Test
    public void testDateTimeFormatConditionLessThanDayFails() {
        this.dateTimeFormatParseThrows(
                conditionLessThan(),
                day()
        );
    }

    @Test
    public void testDateTimeFormatConditionLessThanEqualsDayFails() {
        this.dateTimeFormatParseThrows(
                conditionLessThanEquals(),
                day()
        );
    }

    @Test
    public void testDateTimeFormatConditionNotEqualsDayFails() {
        this.dateTimeFormatParseThrows(
                conditionNotEquals(),
                day()
        );
    }

    @Test
    public void testDateTimeFormatConditionEqualsHourFails() {
        this.dateTimeFormatParseThrows(
                conditionEquals(),
                hour()
        );
    }

    @Test
    public void testDateTimeFormatConditionGreaterThanHourFails() {
        this.dateTimeFormatParseThrows(
                conditionGreaterThan(),
                hour()
        );
    }

    @Test
    public void testDateTimeFormatConditionGreaterThanEqualsHourFails() {
        this.dateTimeFormatParseThrows(
                conditionGreaterThanEquals(),
                hour()
        );
    }

    @Test
    public void testDateTimeFormatConditionLessThanHourFails() {
        this.dateTimeFormatParseThrows(
                conditionLessThan(),
                hour()
        );
    }

    @Test
    public void testDateTimeFormatConditionLessThanEqualsHourFails() {
        this.dateTimeFormatParseThrows(
                conditionLessThanEquals(),
                hour()
        );
    }

    @Test
    public void testDateTimeFormatConditionNotEqualsHourFails() {
        this.dateTimeFormatParseThrows(
                conditionNotEquals(),
                hour()
        );
    }

    @Test
    public void testDateTimeFormatDayConditionFails() {
        this.dateTimeFormatParseThrows(
                day(),
                conditionEquals()
        );
    }

    @Test
    public void testDateTimeFormatDayConditionGreaterThanFails() {
        this.dateTimeFormatParseThrows(
                day(),
                conditionGreaterThan()
        );
    }

    @Test
    public void testDateTimeFormatDayConditionGreaterThanEqualsFails() {
        this.dateTimeFormatParseThrows(
                day(),
                conditionGreaterThanEquals()
        );
    }

    @Test
    public void testDateTimeFormatDayConditionLessThanFails() {
        this.dateTimeFormatParseThrows(
                day(),
                conditionLessThan()
        );
    }

    @Test
    public void testDateTimeFormatDayConditionLessThanEqualsFails() {
        this.dateTimeFormatParseThrows(
                day(),
                conditionLessThanEquals()
        );
    }

    @Test
    public void testDateTimeFormatDayConditionNotEqualsFails() {
        this.dateTimeFormatParseThrows(
                day(),
                conditionNotEquals()
        );
    }

    @Test
    public void testDateTimeFormatHourConditionEqualsFails() {
        this.dateTimeFormatParseThrows(
                hour(),
                conditionEquals()
        );
    }

    @Test
    public void testDateTimeFormatHourConditionGreaterThanFails() {
        this.dateTimeFormatParseThrows(
                hour(),
                conditionGreaterThan()
        );
    }

    @Test
    public void testDateTimeFormatHourConditionGreaterThanEqualsFails() {
        this.dateTimeFormatParseThrows(
                hour(),
                conditionGreaterThanEquals()
        );
    }

    @Test
    public void testDateTimeFormatHourConditionLessThanFails() {
        this.dateTimeFormatParseThrows(
                hour(),
                conditionLessThan()
        );
    }

    @Test
    public void testDateTimeFormatHourConditionLessThanEqualsFails() {
        this.dateTimeFormatParseThrows(
                hour(),
                conditionLessThanEquals()
        );
    }

    @Test
    public void testDateTimeFormatHourConditionNotEqualsFails() {
        this.dateTimeFormatParseThrows(
                hour(),
                conditionNotEquals()
        );
    }


    @Test
    public void testDateTimeFormatPatternSeparator() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute()
                ),
                separator()
        );
    }

    @Test
    public void testDateTimeFormatPatternSeparatorPattern() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute()
                ),
                separator(),
                dateTime(
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testDateTimeFormatColorPatternSeparatorPattern() {
        this.dateTimeFormatParseAndCheck(
                dateTime(
                        color(),
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute()
                ),
                separator(),
                dateTime(
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testDateTimeFormatConditionPatternSeparatorPattern() {
        this.dateTimeFormatParseAndCheck(
                conditionEquals(),
                dateTime(
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute()
                ),
                separator(),
                dateTime(
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    // date format helpers..............................................................................................

    private void dateTimeFormatParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck2(
                SpreadsheetFormatParsers.dateTimeFormat(),
                tokens
        );
    }

    private void dateTimeFormatParseThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(
                SpreadsheetFormatParsers.dateTimeFormat(),
                tokens
        );
    }

    // date time parse..................................................................................................

    // dateTime parse..................................................................................................

    @Test
    public void testDateTimeParseGeneral() {
        this.dateTimeParseParseAndCheck(
                general()
        );
    }

    @Test
    public void testDateTimeParseWhitespaceGeneral() {
        this.dateTimeParseParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateTimeParseGeneralWhitespace() {
        this.dateTimeParseParseAndCheck(
                general(
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testDateTimeParseWhitespaceGeneralWhitespace() {
        this.dateTimeParseParseAndCheck(
                general(
                        whitespace(),
                        generalSymbol(),
                        whitespace()
                )
        );
    }

    @Test
    public void testDateTimeParseColorGeneral() {
        this.dateTimeParseParseAndCheck(
                general(
                        color(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateTimeParseColorWhitespaceGeneral() {
        this.dateTimeParseParseAndCheck(
                general(
                        color(),
                        whitespace(),
                        generalSymbol()
                )
        );
    }

    @Test
    public void testDateTimeParseColorEscapedFails() {
        this.dateTimeParseParseThrows(
                dateTime(
                        color(),
                        escape()
                )
        );
    }

    @Test
    public void testDateTimeParseEscaped() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        escape()
                )
        );
    }

    @Test
    public void testDateTimeParseDollar() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        textLiteralDollar()
                )
        );
    }

    @Test
    public void testDateTimeParseMinus() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        textLiteralMinus()
                )
        );
    }

    @Test
    public void testDateTimeParsePlus() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        textLiteralPlus()
                )
        );
    }

    @Test
    public void testDateTimeParseSlash() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        textLiteralSlash()
                )
        );
    }

    @Test
    public void testDateTimeParseOpenParen() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        textLiteralOpenParens()
                )
        );
    }

    @Test
    public void testDateTimeParseCloseParen() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        textLiteralCloseParens()
                )
        );
    }

    @Test
    public void testDateTimeParseColon() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        textLiteralColon()
                )
        );
    }

    @Test
    public void testDateTimeParseSpace() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        textLiteralSpace()
                )
        );
    }

    @Test
    public void testDateTimeParseQuotedText() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        quotedText()
                )
        );
    }

    // date only........................................................................................................

    @Test
    public void testDateTimeParseDay() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        day()
                )
        );
    }

    @Test
    public void testDateTimeParseDayMonth() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        day(),
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testDateTimeParseDayMonthYear() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        day(),
                        monthOrMinute(),
                        year()
                )
        );
    }

    // time only........................................................................................................

    @Test
    public void testDateTimeParseHour() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        hour()
                )
        );
    }

    @Test
    public void testDateTimeParseHourMinute() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        hour(),
                        monthOrMinute()
                )
        );
    }

    @Test
    public void testDateTimeParseHourMinuteSecond() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    // time with millis..................................................................................................

    @Test
    public void testDateTimeParseSecondsDecimal() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        second(),
                        decimalPoint()
                )
        );
    }

    @Test
    public void testDateTimeParseSecondsDecimalNonZeroFails() {
        this.dateTimeParseParseThrows(
                second(),
                decimalPoint(),
                digit()
        );
    }

    @Test
    public void testDateTimeParseSecondsDecimalSpaceFails() {
        this.dateTimeParseParseThrows(
                second(),
                decimalPoint(),
                digitSpace()
        );
    }

    @Test
    public void testDateTimeParseSecondsDecimalDigitZero() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        second(),
                        decimalPoint(),
                        digitZero()
                )
        );
    }

    @Test
    public void testDateTimeParseSecondsDecimalDigitZeroZero() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        second(),
                        decimalPoint(),
                        digitZero(),
                        digitZero()
                )
        );
    }

    // date&time........................................................................................................

    @Test
    public void testDateTimeParseDayHour() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        day(),
                        hour()
                )
        );
    }

    @Test
    public void testDateTimeParseDayHour2() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        day(),
                        hour(),
                        day(),
                        hour()
                )
        );
    }

    @Test
    public void testDateTimeParseDayMonthYearHourMinuteSecond() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        day(),
                        monthOrMinute(),
                        year(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testDateTimeParseDayMonthYearHourMinuteSecond2() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        day(),
                        monthOrMinute(),
                        year(),
                        hour(),
                        monthOrMinute(),
                        second(),
                        day(),
                        monthOrMinute(),
                        year(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testDateTimeParseDayMonthYearHourMinuteSecondMillis() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        day(),
                        monthOrMinute(),
                        year(),
                        hour(),
                        monthOrMinute(),
                        second(),
                        day(),
                        monthOrMinute(),
                        year(),
                        hour(),
                        monthOrMinute(),
                        second(),
                        decimalPoint(),
                        digitZero()
                )
        );
    }

    @Test
    public void testDateTimeParseColorDayFails() {
        this.dateTimeParseParseThrows(
                dateTime(
                        color(),
                        day()
                )
        );
    }

    @Test
    public void testDateTimeParseDayColorFails() {
        this.dateTimeParseParseThrows(
                dateTime(
                        day(),
                        color()
                )
        );
    }

    @Test
    public void testDateTimeParseDayCommaMonthCommaYear() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        day(),
                        textLiteralComma(),
                        monthOrMinute(),
                        textLiteralComma(),
                        year()
                )
        );
    }

    // condition

    @Test
    public void testDateTimeParseConditionEqualsDayFails() {
        this.dateTimeParseParseThrows(
                conditionEquals(),
                day()
        );
    }

    @Test
    public void testDateTimeParseConditionGreaterThanDayFails() {
        this.dateTimeParseParseThrows(
                conditionGreaterThan(),
                day()
        );
    }

    @Test
    public void testDateTimeParseConditionGreaterThanEqualsDayFails() {
        this.dateTimeParseParseThrows(
                conditionGreaterThanEquals(),
                day()
        );
    }

    @Test
    public void testDateTimeParseConditionLessThanDayFails() {
        this.dateTimeParseParseThrows(
                conditionLessThan(),
                day()
        );
    }

    @Test
    public void testDateTimeParseConditionLessThanEqualsDayFails() {
        this.dateTimeParseParseThrows(
                conditionLessThanEquals(),
                day()
        );
    }

    @Test
    public void testDateTimeParseConditionNotEqualsDayFails() {
        this.dateTimeParseParseThrows(
                conditionNotEquals(),
                day()
        );
    }

    @Test
    public void testDateTimeParseConditionEqualsHourFails() {
        this.dateTimeParseParseThrows(
                conditionEquals(),
                hour()
        );
    }

    @Test
    public void testDateTimeParseConditionGreaterThanHourFails() {
        this.dateTimeParseParseThrows(
                conditionGreaterThan(),
                hour()
        );
    }

    @Test
    public void testDateTimeParseConditionGreaterThanEqualsHourFails() {
        this.dateTimeParseParseThrows(
                conditionGreaterThanEquals(),
                hour()
        );
    }

    @Test
    public void testDateTimeParseConditionLessThanHourFails() {
        this.dateTimeParseParseThrows(
                conditionLessThan(),
                hour()
        );
    }

    @Test
    public void testDateTimeParseConditionLessThanEqualsHourFails() {
        this.dateTimeParseParseThrows(
                conditionLessThanEquals(),
                hour()
        );
    }

    @Test
    public void testDateTimeParseConditionNotEqualsHourFails() {
        this.dateTimeParseParseThrows(
                conditionNotEquals(),
                hour()
        );
    }

    @Test
    public void testDateTimeParseDayConditionFails() {
        this.dateTimeParseParseThrows(
                day(),
                conditionEquals()
        );
    }

    @Test
    public void testDateTimeParseDayConditionGreaterThanFails() {
        this.dateTimeParseParseThrows(
                day(),
                conditionGreaterThan()
        );
    }

    @Test
    public void testDateTimeParseDayConditionGreaterThanEqualsFails() {
        this.dateTimeParseParseThrows(
                day(),
                conditionGreaterThanEquals()
        );
    }

    @Test
    public void testDateTimeParseDayConditionLessThanFails() {
        this.dateTimeParseParseThrows(
                day(),
                conditionLessThan()
        );
    }

    @Test
    public void testDateTimeParseDayConditionLessThanEqualsFails() {
        this.dateTimeParseParseThrows(
                day(),
                conditionLessThanEquals()
        );
    }

    @Test
    public void testDateTimeParseDayConditionNotEqualsFails() {
        this.dateTimeParseParseThrows(
                day(),
                conditionNotEquals()
        );
    }

    @Test
    public void testDateTimeParseHourConditionEqualsFails() {
        this.dateTimeParseParseThrows(
                hour(),
                conditionEquals()
        );
    }

    @Test
    public void testDateTimeParseHourConditionGreaterThanFails() {
        this.dateTimeParseParseThrows(
                hour(),
                conditionGreaterThan()
        );
    }

    @Test
    public void testDateTimeParseHourConditionGreaterThanEqualsFails() {
        this.dateTimeParseParseThrows(
                hour(),
                conditionGreaterThanEquals()
        );
    }

    @Test
    public void testDateTimeParseHourConditionLessThanFails() {
        this.dateTimeParseParseThrows(
                hour(),
                conditionLessThan()
        );
    }

    @Test
    public void testDateTimeParseHourConditionLessThanEqualsFails() {
        this.dateTimeParseParseThrows(
                hour(),
                conditionLessThanEquals()
        );
    }

    @Test
    public void testDateTimeParseHourConditionNotEqualsFails() {
        this.dateTimeParseParseThrows(
                hour(),
                conditionNotEquals()
        );
    }


    @Test
    public void testDateTimeParsePatternSeparator() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute()
                ),
                separator()
        );
    }

    @Test
    public void testDateTimeParsePatternSeparatorPattern() {
        this.dateTimeParseParseAndCheck(
                dateTime(
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute()
                ),
                separator(),
                dateTime(
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    @Test
    public void testDateTimeParseColorPatternSeparatorPatternFails() {
        this.dateTimeParseParseThrows(
                dateTime(
                        color(),
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute()
                ),
                separator(),
                dateTime(
                        year(),
                        monthOrMinute(),
                        day(),
                        hour(),
                        monthOrMinute(),
                        second()
                )
        );
    }

    private void dateTimeParseParseAndCheck(final SpreadsheetFormatParserToken... tokens) {
        this.parseAndCheck2(
                SpreadsheetFormatParsers.dateTimeParse(),
                tokens
        );
    }

    private void dateTimeParseParseThrows(final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows2(
                SpreadsheetFormatParsers.dateTimeParse(),
                tokens
        );
    }

    // helpers................................................................................................

    private void parseAndCheck2(final Parser<SpreadsheetFormatParserContext> parser,
                                final SpreadsheetFormatParserToken... tokens) {
        final List<ParserToken> list = Lists.of(tokens);
        final String text = ParserToken.text(list);

        this.checkEquals(
                text.toUpperCase(),
                text,
                "text should be all upper case"
        );

        this.parseAndCheck4(
                parser,
                sequence(list)
        );

        final List<ParserToken> lower = Arrays.stream(tokens)
                .map(SpreadsheetFormatParsersTestSpreadsheetFormatParserTokenVisitor::toLower)
                .collect(Collectors.toList());

        this.parseAndCheck4(
                parser,
                sequence(lower)
        );
    }

    private void parseAndCheck3(final Parser<SpreadsheetFormatParserContext> parser,
                                final BiFunction<List<ParserToken>, String, SpreadsheetFormatParserToken> factory,
                                final SpreadsheetFormatParserToken... tokens) {
        final List<ParserToken> list = Lists.of(tokens);
        final String text = ParserToken.text(list);

        this.checkEquals(
                text.toUpperCase(),
                text,
                "text should be all upper case"
        );

        this.parseAndCheck4(
                parser,
                factory.apply(
                        list,
                        text
                )
        );

        final List<ParserToken> lower = Arrays.stream(tokens)
                .map(SpreadsheetFormatParsersTestSpreadsheetFormatParserTokenVisitor::toLower)
                .collect(Collectors.toList());
        final String textLower = text.toLowerCase();

        this.parseAndCheck4(
                parser,
                factory.apply(
                        lower,
                        textLower
                )
        );
    }

    private void parseAndCheck4(final Parser<SpreadsheetFormatParserContext> parser,
                                final ParserToken token) {
        final List<ParserToken> list = Lists.of(token);
        final String text = ParserToken.text(list);

        this.parseAndCheck(
                parser,
                text,
                token,
                text
        );
    }

    private void parseFailAndCheck2(final Parser<SpreadsheetFormatParserContext> parser,
                                    final SpreadsheetFormatParserToken... tokens) {
        // https://github.com/mP1/walkingkooka-spreadsheet/issues/2626
        final TextCursor cursor = TextCursors.charSequence(
                ParserToken.text(
                        Lists.of(tokens)
                )
        );
        final TextCursorSavePoint start = cursor.save();

        this.parse(
                parser,
                cursor,
                this.createContext()
        );
        this.checkNotEquals(
                "",
                start.textBetween().toString()
        );
    }

    private void parseFailAndCheck3(final Parser<SpreadsheetFormatParserContext> parser,
                                    final SpreadsheetFormatParserToken... tokens) {
        // https://github.com/mP1/walkingkooka-spreadsheet/issues/2626
        final String text = ParserToken.text(
                Lists.of(tokens)
        );
        final TextCursor cursor = TextCursors.charSequence(text);
        final TextCursorSavePoint start = cursor.save();

        this.checkEquals(
                Optional.empty(),
                this.parse(
                        parser,
                        cursor,
                        this.createContext()
                )
        );

        this.checkEquals(
                text,
                start.textBetween().toString()
        );
    }

    private void parseThrows2(final Parser<SpreadsheetFormatParserContext> parser,
                              final SpreadsheetFormatParserToken... tokens) {
        this.parseThrows(parser.orFailIfCursorNotEmpty(ParserReporters.basic()),
                ParserToken.text(Lists.of(tokens)));
    }

    @Override
    public Parser<SpreadsheetFormatParserContext> createParser() {
        return Parsers.fake();
    }

    @Override
    public SpreadsheetFormatParserContext createContext() {
        return SpreadsheetFormatParserContexts.basic();
    }

    // PublicStaticHelperTesting........................................................................................

    @Override
    public Class<SpreadsheetFormatParsers> type() {
        return SpreadsheetFormatParsers.class;
    }

    @Override
    public boolean canHavePublicTypes(final Method method) {
        return false;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
