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

import walkingkooka.tree.text.FontFamilyName;

import java.util.Locale;
import java.util.Optional;

final class SpreadsheetMetadataPropertyNameDefaultFontFamilyName extends SpreadsheetMetadataPropertyName<FontFamilyName> {

    /**
     * Singleton
     */
    final static SpreadsheetMetadataPropertyNameDefaultFontFamilyName instance() {
        return new SpreadsheetMetadataPropertyNameDefaultFontFamilyName();
    }

    /**
     * Private constructor use singleton.
     */
    private SpreadsheetMetadataPropertyNameDefaultFontFamilyName() {
        super("default-font-family-name");
    }

    @Override
    Class<FontFamilyName> type() {
        return FontFamilyName.class;
    }

    @Override
    void checkValue0(final Object value) {
        this.checkValueType(value, v -> v instanceof FontFamilyName);
    }

    @Override
    String expected() {
        return "FontFamilyName";
    }

    @Override
    Optional<FontFamilyName> extractLocaleValue(final Locale locale) {
        return Optional.empty();
    }

    @Override
    void accept(final FontFamilyName value,
                final SpreadsheetMetadataVisitor visitor) {
        visitor.visitDefaultFontFamilyName(value);
    }
}