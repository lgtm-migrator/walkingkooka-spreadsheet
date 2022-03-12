
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

import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;

/**
 * A store that contains {@link SpreadsheetRow} including some methods that for frequent queries.
 */
public interface SpreadsheetRowStore extends SpreadsheetColumnOrRowStore<SpreadsheetRowReference, SpreadsheetRow> {

    /**
     * Returns the first row moving up from the given starting point that is not hidden.
     * If all rows to the up are hidden, the original {@link SpreadsheetRowReference} is returned.
     */
    SpreadsheetRowReference upSkipHidden(final SpreadsheetRowReference reference);

    /**
     * Returns the last row moving down from the given starting point that is not hidden.
     * If all rows to the down are hidden, the original {@link SpreadsheetRowReference} is returned.
     */
    SpreadsheetRowReference downSkipHidden(final SpreadsheetRowReference reference);
}
