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

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.convert.ConversionException;
import walkingkooka.convert.ConverterContext;
import walkingkooka.spreadsheet.SpreadsheetValueVisitorTesting;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetConverterSpreadsheetValueVisitorTest extends SpreadsheetConverterTestCase<SpreadsheetConverterSpreadsheetValueVisitor<ConverterContext>>
        implements SpreadsheetValueVisitorTesting<SpreadsheetConverterSpreadsheetValueVisitor<ConverterContext>> {

    @Override
    public void testAllConstructorsVisibility() {
    }

    @Test
    public void testAcceptUnknownValue() {
        final ConversionException thrown = assertThrows(
                ConversionException.class,
                () -> this.createVisitor().accept(this)
        );
        this.checkEquals(
                "Unable to convert " + this + " to java.lang.String",
                thrown.getMessage(),
                "message"
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createVisitor(), "\"String\"");
    }

    @Override
    public SpreadsheetConverterSpreadsheetValueVisitor<ConverterContext> createVisitor() {
        return new SpreadsheetConverterSpreadsheetValueVisitor<>(String.class, null);
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetConverterSpreadsheetValueVisitor<ConverterContext>> type() {
        return Cast.to(SpreadsheetConverterSpreadsheetValueVisitor.class);
    }
}
