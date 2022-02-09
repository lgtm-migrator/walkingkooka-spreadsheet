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

package walkingkooka.spreadsheet.store;

import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRow;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRowReference;
import walkingkooka.spreadsheet.reference.store.SpreadsheetStore;

import java.util.Optional;

/**
 * A common {@link SpreadsheetStore} for both {@link walkingkooka.spreadsheet.reference.SpreadsheetColumn} or {@link walkingkooka.spreadsheet.reference.SpreadsheetRow}.
 */
public interface SpreadsheetColumnOrRowStore<R extends SpreadsheetColumnOrRowReference, V extends SpreadsheetColumnOrRow<R>> extends SpreadsheetStore<R, V> {

    /**
     * Tests if the given {@link SpreadsheetColumnOrRow} is hidden. Column or rows that do not exist will return false.
     */
    default boolean isHidden(final R reference) {
        final Optional<V> columnOrRow = this.load(reference);
        return columnOrRow.isPresent() && columnOrRow.get().hidden();
    }
}