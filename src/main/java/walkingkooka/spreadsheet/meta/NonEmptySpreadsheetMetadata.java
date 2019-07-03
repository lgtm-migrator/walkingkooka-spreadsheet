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

import walkingkooka.Cast;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * A {@link NonEmptySpreadsheetMetadata} holds a non empty {@link Map} of {@link SpreadsheetMetadataPropertyName} and values.
 */
final class NonEmptySpreadsheetMetadata extends SpreadsheetMetadata {

    /**
     * Factory that creates a {@link NonEmptySpreadsheetMetadata} from a {@link SpreadsheetMetadataMap}.
     */
    static NonEmptySpreadsheetMetadata with(final SpreadsheetMetadataMap value) {
        return new NonEmptySpreadsheetMetadata(value);
    }

    private NonEmptySpreadsheetMetadata(final SpreadsheetMetadataMap value) {
        super();
        this.value = value;
    }

    /**
     * Always returns false
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    // Value..........................................................................................................

    @Override
    public Map<SpreadsheetMetadataPropertyName<?>, Object> value() {
        return this.value;
    }

    final SpreadsheetMetadataMap value;

    // get..............................................................................................................

    @Override
    <V> Optional<V> get0(final SpreadsheetMetadataPropertyName<V> propertyName) {
        return Optional.ofNullable(Cast.to(this.value.get(propertyName)));
    }

    // set..............................................................................................................

    @Override
    <V> SpreadsheetMetadata set0(final SpreadsheetMetadataPropertyName<V> propertyName, final V value) {
        SpreadsheetMetadataMap map = this.value;
        final List<Entry<SpreadsheetMetadataPropertyName<?>, Object>> list = Lists.array();

        int mode = 0; // new property added.

        for (Entry<SpreadsheetMetadataPropertyName<?>, Object> propertyAndValue : map.entries) {
            final SpreadsheetMetadataPropertyName<?> property = propertyAndValue.getKey();

            if (propertyName.equals(property)) {
                if (propertyAndValue.getValue().equals(value)) {
                    mode = 1; // no change
                    break;
                } else {
                    list.add(Maps.entry(property, value));
                    mode = 2; // replaced
                }
            } else {
                list.add(propertyAndValue);
            }
        }

        // replace didnt happen
        if (0 == mode) {
            list.add(Maps.entry(propertyName, value));
            SpreadsheetMetadataMapEntrySet.sort(list);
        }

        return 1 == mode ?
                this :
                new NonEmptySpreadsheetMetadata(SpreadsheetMetadataMap.withSpreadsheetMetadataMapEntrySet(SpreadsheetMetadataMapEntrySet.withList(list)));
    }

    // remove...........................................................................................................

    @Override
    SpreadsheetMetadata remove0(final SpreadsheetMetadataPropertyName<?> propertyName) {
        final List<Entry<SpreadsheetMetadataPropertyName<?>, Object>> list = Lists.array();
        boolean removed = false;

        for (Entry<SpreadsheetMetadataPropertyName<?>, Object> propertyAndValue : this.value.entries) {
            final SpreadsheetMetadataPropertyName<?> property = propertyAndValue.getKey();
            if (propertyName.equals(property)) {
                removed = true;
            } else {
                list.add(propertyAndValue);
            }
        }

        return removed ?
                this.remove1(list) :
                this;
    }

    /**
     * Accepts a list after removing a property, special casing if the list is empty.
     */
    private SpreadsheetMetadata remove1(List<Entry<SpreadsheetMetadataPropertyName<?>, Object>> list) {
        return list.isEmpty() ?
                SpreadsheetMetadata.EMPTY :
                new NonEmptySpreadsheetMetadata(SpreadsheetMetadataMap.withSpreadsheetMetadataMapEntrySet(SpreadsheetMetadataMapEntrySet.withList(list))); // no need to sort after a delete
    }

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return this.value().hashCode();
    }

    @Override
    boolean canBeEquals(final Object other) {
        return other instanceof NonEmptySpreadsheetMetadata;
    }

    @Override
    boolean equals0(final SpreadsheetMetadata other) {
        return this.value.equals(other.value());
    }

    @Override
    public final String toString() {
        return this.value.toString();
    }

    // HasJsonNode......................................................................................................

    @Override
    public JsonNode toJsonNode() {
        return this.value.toJson();
    }
}