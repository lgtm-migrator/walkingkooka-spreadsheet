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

package walkingkooka.spreadsheet;

// #NAME
// #DIV/0
// #REF!
// #VALUE!
// #NA
// #NULL
// #NUM

import walkingkooka.convert.ConversionException;
import walkingkooka.text.CharSequences;
import walkingkooka.text.HasText;
import walkingkooka.text.cursor.parser.ParserException;
import walkingkooka.tree.expression.ExpressionEvaluationException;
import walkingkooka.tree.expression.HasExpressionReference;

import java.util.Objects;

/**
 * The type of {@link SpreadsheetError}.
 * <br>
 * https://exceljet.net/excel-functions/excel-errortype-function
 */
public enum SpreadsheetErrorKind implements HasText {

    NULL("#NULL!", 1),

    DIV0("#DIV/0!", 2),

    VALUE("#VALUE!", 3),

    REF("#REF!", 4),

    NAME("#NAME?", 5),

    NUM("#NUM!", 6),

    NA("#N/A", 7),

    ERROR("#ERROR", 8),

    SPILL("#SPILL!", 9),

    CALC("#CALC!", 14);

    SpreadsheetErrorKind(final String text,
                         final int value) {
        this.text = text;
        this.value = value;
    }

    @Override
    public String text() {
        return this.text;
    }

    private final String text;

    /**
     * This value is return by ERROR.TYPE expression.
     */
    public int value() {
        return this.value;
    }

    private final int value;

    public SpreadsheetError setMessage(final String message) {
        return SpreadsheetError.with(this, message);
    }

    @Override
    public String toString() {
        return this.text();
    }

    /**
     * Attempts to translate the given @link Throwable} into the a {@link SpreadsheetError}.
     */
    public static SpreadsheetError translate(final Throwable cause) {
        Objects.requireNonNull(cause, "cause");

        return cause instanceof HasSpreadsheetError ?
                translate0((HasSpreadsheetError) cause) :
                translate1(cause);
    }

    private static SpreadsheetError translate0(final HasSpreadsheetError has) {
        return has.spreadsheetError();
    }

    private static SpreadsheetError translate1(final Throwable cause) {
        Throwable translate = cause;

        if (cause instanceof ExpressionEvaluationException) {
            translate = cause.getCause();
            if (null == translate) {
                translate = cause;
            }
        }

        return translate2(translate);
    }

    private static SpreadsheetError translate2(final Throwable cause) {
        final SpreadsheetErrorKind kind;
        String message = cause.getMessage();

        do {
            if (cause instanceof HasSpreadsheetErrorKind) {
                final HasSpreadsheetErrorKind has = (HasSpreadsheetErrorKind) cause;
                kind = has.spreadsheetErrorKind();
                break;
            }

            // REF!
            if (cause instanceof HasExpressionReference) {
                kind = REF;
                break;
            }

            // Trying to divide by 0
            if (cause instanceof ArithmeticException) {
                kind = DIV0;
                break;
            }

            // #VALUE! 	The wrong type of operand or expression argument is used
            if (cause instanceof ClassCastException) {
                kind = VALUE;
                message = SpreadsheetErrorKindClassCastExceptionMessage.extractClassCastExceptionMessage(message);
                break;
            }

            // #VALUE! 	The wrong type of operand or expression argument is used
            if (cause instanceof ConversionException) {
                kind = VALUE;

                final ConversionException conversionException = (ConversionException) cause;
                message = "Cannot convert " + CharSequences.quoteIfChars(conversionException.value()) + " to " + conversionException.type().getSimpleName();
                break;
            }

            // #NUM! 	A formula has invalid numeric data for the type of operation
            if (cause instanceof NullPointerException ||
                    cause instanceof IllegalArgumentException) {
                kind = VALUE;
                break;
            }

            // #ERROR! 	Text in the formula is not recognized
            if (cause instanceof ParserException) {
                kind = ERROR;
                break;
            }

            kind = VALUE;
        } while (false);

        return SpreadsheetError.with(
                kind,
                CharSequences.nullToEmpty(message).toString()
        );
    }
}
