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

package walkingkooka.spreadsheet.convert;

import walkingkooka.Cast;
import walkingkooka.Either;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.spreadsheet.SpreadsheetError;
import walkingkooka.spreadsheet.SpreadsheetErrorConversionException;
import walkingkooka.spreadsheet.SpreadsheetErrorKind;

/**
 * A {@link Converter} that can convert {@link SpreadsheetError} to a {@link String} value.
 * This basically returns the {@link SpreadsheetErrorKind#text()}, giving text like <code>#ERROR</code>.
 * All other types ill throw a {@link SpreadsheetErrorConversionException}.
 */
final class SpreadsheetErrorConverter implements Converter<ConverterContext> {

    /**
     * Singleton
     */
    final static SpreadsheetErrorConverter INSTANCE = new SpreadsheetErrorConverter();

    /**
     * Private ctor use singleton.
     */
    private SpreadsheetErrorConverter() {
    }

    @Override
    public boolean canConvert(final Object value,
                              final Class<?> type,
                              final ConverterContext context) {
        return value instanceof SpreadsheetError;
    }

    @Override
    public <T> Either<T, String> convert(final Object value,
                                         final Class<T> type,
                                         final ConverterContext context) {
        return this.canConvert(value, type, context) ?
                this.convertSpreadsheetError(
                        (SpreadsheetError) value,
                        type
                ) :
                this.failConversion(value, type);
    }

    private <T> Either<T, String> convertSpreadsheetError(final SpreadsheetError error,
                                                          final Class<T> type) {
        if (String.class != type) {
            throw new SpreadsheetErrorConversionException(error);
        }

        return Cast.to(
                Either.left(error.kind().text())
        );
    }

    @Override
    public String toString() {
        return SpreadsheetError.class.getSimpleName();
    }
}
