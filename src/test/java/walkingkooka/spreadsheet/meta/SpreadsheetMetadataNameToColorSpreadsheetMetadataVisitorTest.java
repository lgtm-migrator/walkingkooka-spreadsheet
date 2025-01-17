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

import org.junit.jupiter.api.Test;
import walkingkooka.color.Color;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetColorName;

public final class SpreadsheetMetadataNameToColorSpreadsheetMetadataVisitorTest extends SpreadsheetMetadataTestCase2<SpreadsheetMetadataNameToColorSpreadsheetMetadataVisitor>
        implements SpreadsheetMetadataVisitorTesting<SpreadsheetMetadataNameToColorSpreadsheetMetadataVisitor> {

    @Override
    public void testAllConstructorsVisibility() {
    }

    @Override
    public void testIfClassIsFinalIfAllConstructorsArePrivate() {
    }

    @Test
    public void testToString() {
        final SpreadsheetMetadataNameToColorSpreadsheetMetadataVisitor visitor = new SpreadsheetMetadataNameToColorSpreadsheetMetadataVisitor();

        visitor.accept(
                SpreadsheetMetadata.EMPTY
                        .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("user@example.com"))
                        .set(SpreadsheetMetadataPropertyName.namedColor(SpreadsheetColorName.with("apple")), Color.fromRgb(0x112233))
                        .set(SpreadsheetMetadataPropertyName.namedColor(SpreadsheetColorName.with("banana")), Color.fromRgb(0xffeedd))
        );
        this.toStringAndCheck(visitor, "{apple=#112233, banana=#ffeedd}");
    }

    @Override
    public SpreadsheetMetadataNameToColorSpreadsheetMetadataVisitor createVisitor() {
        return new SpreadsheetMetadataNameToColorSpreadsheetMetadataVisitor();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetMetadataNameToColorSpreadsheetMetadataVisitor> type() {
        return SpreadsheetMetadataNameToColorSpreadsheetMetadataVisitor.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    // TypeNameTesting...................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetMetadata.class.getSimpleName();
    }
}
