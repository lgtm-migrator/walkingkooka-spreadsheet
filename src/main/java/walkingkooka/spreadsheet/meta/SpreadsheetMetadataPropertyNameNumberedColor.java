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

import walkingkooka.color.Color;
import walkingkooka.naming.Name;
import walkingkooka.text.CharSequences;

import java.util.stream.IntStream;

/**
 * The {@link Name} of metadata property for numbered colors.
 */
final class SpreadsheetMetadataPropertyNameNumberedColor extends SpreadsheetMetadataPropertyNameColor {

    /**
     * Retrieves a {@link SpreadsheetMetadataPropertyNameNumberedColor} for a numbered {@link Color}.
     */
    static SpreadsheetMetadataPropertyNameNumberedColor withNumber(final int number) {
        if (number < 0) {
            throw new IllegalArgumentException("Number " + number + " < 0");
        }

        return number < MAX_NUMBER ?
                NUMBER_TO_COLOR[number] :
                new SpreadsheetMetadataPropertyNameNumberedColor(number);
    }

    static final int MAX_NUMBER = 32;

    /**
     * Cache of 0 to {@link #MAX_NUMBER} names.
     */
    private static final SpreadsheetMetadataPropertyNameNumberedColor[] NUMBER_TO_COLOR = new SpreadsheetMetadataPropertyNameNumberedColor[MAX_NUMBER];

    /*
     * Fills the cache of {@link SpreadsheetMetadataPropertyNameNumberedColor} for color numbers 0 to {@link #MAX_NUMBER}.
     */
    static {
        IntStream.range(0, MAX_NUMBER)
                .forEach(SpreadsheetMetadataPropertyNameNumberedColor::registerColor);
    }

    private static void registerColor(final int i) {
        final SpreadsheetMetadataPropertyNameNumberedColor name = new SpreadsheetMetadataPropertyNameNumberedColor(i);
        NUMBER_TO_COLOR[i] = name;
        CONSTANTS.put(name.value(), name);
    }

    /**
     * Private constructor use factory.
     */
    private SpreadsheetMetadataPropertyNameNumberedColor(final int number) {
        super(COLOR_PREFIX + number);
        this.number = number;
        this.compareToName = COLOR_PREFIX + CharSequences.padLeft(String.valueOf(number), 5, '0');
    }

    final int number;

    @Override
    void accept(final Color value,
                final SpreadsheetMetadataVisitor visitor) {
        visitor.visitNumberedColor(this.number, value);
    }

    @Override
    String compareToName() {
        return this.compareToName;
    }

    private final String compareToName;
}
