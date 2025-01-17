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

import walkingkooka.text.CharSequences;

import java.math.BigDecimal;

/**
 * Handles preparing the integer, fraction and possibly exponent digits that will eventually appear in the formatted text.
 */
enum NumberSpreadsheetFormatterNormalOrScientific {

    /**
     * integer and fraction number.
     */
    NORMAL {
        @Override
        NumberSpreadsheetFormatterContext context(final BigDecimal value,
                                                  final NumberSpreadsheetFormatter formatter,
                                                  final SpreadsheetFormatterContext context) {
            final BigDecimal rounded = value.scaleByPowerOfTen(formatter.decimalPlacesShift)
                    .setScale(
                            formatter.fractionDigitSymbolCount,
                            context.mathContext().getRoundingMode()
                    );

            final int valueSign = rounded.signum();
            String integerDigits = "";
            String fractionDigits = "";

            if (0 != valueSign) {
                final String digits = rounded
                        .unscaledValue()
                        .abs()
                        .toString();
                final int integerDigitCount = Math.min(rounded.precision() - rounded.scale(), digits.length());
                integerDigits = integerDigitCount > 0 ?
                        digits.substring(0, integerDigitCount) :
                        "";
                fractionDigits = integerDigitCount >= 0 ?
                        digits.substring(integerDigitCount) :
                        CharSequences.repeating('0', -integerDigitCount) + digits;
            }

            return NumberSpreadsheetFormatterContext.with(
                    NumberSpreadsheetFormatterDigits.integer(NumberSpreadsheetFormatterMinusSign.fromSignum(valueSign), integerDigits, formatter.thousandsSeparator),
                    NumberSpreadsheetFormatterDigits.fraction(fractionDigits),
                    NO_EXPONENT,
                    formatter,
                    context);
        }
    },
    /**
     * formatted number includes an exponent and desired number of decimal places.
     */
    SCENTIFIC {
        @Override
        NumberSpreadsheetFormatterContext context(final BigDecimal value,
                                                  final NumberSpreadsheetFormatter formatter,
                                                  final SpreadsheetFormatterContext context) {

            final int integerDigitSymbolCount = formatter.integerDigitSymbolCount;
            final int fractionDigitSymbolCount = formatter.fractionDigitSymbolCount;

            final BigDecimal rounded = value.abs()
                    .setScale(
                            (integerDigitSymbolCount + fractionDigitSymbolCount) - (value.precision() - value.scale()),
                            context.mathContext().getRoundingMode()
                    )
                    .stripTrailingZeros();

            final String digits = rounded.unscaledValue()
                    .abs()
                    .toString();
            final int digitCount = digits.length();
            final int integerDigitCount = Math.min(integerDigitSymbolCount, digitCount);
            final int fractionDigitCount = Math.max(digitCount - integerDigitCount, fractionDigitSymbolCount);

            final int exponent = rounded.precision() - rounded.scale() - integerDigitCount;

            return NumberSpreadsheetFormatterContext.with(
                    NumberSpreadsheetFormatterDigits.integer(NumberSpreadsheetFormatterMinusSign.fromSignum(value.signum()), digits.substring(0, integerDigitCount), formatter.thousandsSeparator),
                    NumberSpreadsheetFormatterDigits.fraction(digits.substring(integerDigitCount, Math.min(integerDigitCount + fractionDigitCount, digitCount))),
                    NumberSpreadsheetFormatterDigits.exponent(NumberSpreadsheetFormatterMinusSign.fromSignum(exponent), String.valueOf(Math.abs(exponent))),
                    formatter,
                    context);
        }
    };

    /**
     * Creates a new {@link NumberSpreadsheetFormatterContext} which will accompany the current
     * format request. Note context cannot be recycled as they contain state.
     */
    abstract NumberSpreadsheetFormatterContext context(
            final BigDecimal value,
            final NumberSpreadsheetFormatter formatter,
            final SpreadsheetFormatterContext context);

    private final static NumberSpreadsheetFormatterDigits NO_EXPONENT = NumberSpreadsheetFormatterDigits.exponent(NumberSpreadsheetFormatterMinusSign.NOT_REQUIRED, "");
}
