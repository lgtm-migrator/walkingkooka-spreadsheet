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

import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

/**
 * A {@link SpreadsheetMetadataPropertyValueHandler} for {@link Character} entries.
 */
final class SpreadsheetMetadataPropertyValueHandlerCharacter extends SpreadsheetMetadataPropertyValueHandler<Character> {

    /**
     * A singleton
     */
    static final SpreadsheetMetadataPropertyValueHandlerCharacter INSTANCE = new SpreadsheetMetadataPropertyValueHandlerCharacter();

    private SpreadsheetMetadataPropertyValueHandlerCharacter() {
        super();
    }

    @Override
    void check0(final Object value, final SpreadsheetMetadataPropertyName<?> name) {
        this.checkType(value,
                v -> v instanceof Character,
                name);
    }

    @Override
    String expectedTypeName(final Class<?> type) {
        return Character.class.getSimpleName();
    }

    @Override
    public String toString() {
        return Character.class.getSimpleName();
    }

    // JsonNodeContext..................................................................................................

    @Override
    Character unmarshall(final JsonNode node,
                         final SpreadsheetMetadataPropertyName<?> name,
                         final JsonNodeUnmarshallContext context) {
        return context.unmarshall(node, Character.class);
    }

    @Override
    JsonNode marshall(final Character value,
                      final JsonNodeMarshallContext context) {
        return context.marshall(value);
    }
}
