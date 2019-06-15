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

package walkingkooka.spreadsheet.hateos;

import walkingkooka.compare.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetDelta;
import walkingkooka.spreadsheet.SpreadsheetRowReference;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;

import java.util.Map;
import java.util.function.Supplier;

/**
 * A {@link HateosHandler} that handles inserting a single or range of rows.
 */
final class SpreadsheetEngineInsertRowsHateosHandler extends SpreadsheetEngineHateosHandler2<SpreadsheetRowReference> {

    static SpreadsheetEngineInsertRowsHateosHandler with(final SpreadsheetEngine engine,
                                                         final Supplier<SpreadsheetEngineContext> context) {
        check(engine, context);
        return new SpreadsheetEngineInsertRowsHateosHandler(engine, context);
    }

    /**
     * Private ctor
     */
    private SpreadsheetEngineInsertRowsHateosHandler(final SpreadsheetEngine engine,
                                                     final Supplier<SpreadsheetEngineContext> context) {
        super(engine, context);
    }

    @Override
    String id() {
        return "row";
    }

    @Override
    SpreadsheetDelta handle0(final SpreadsheetRowReference row,
                             final SpreadsheetDelta resource,
                             final Map<HttpRequestAttribute<?>, Object> parameters) {
        return this.engine.insertRows(row,
                this.count(parameters),
                this.context.get());
    }

    @Override
    void checkRange(final Range<SpreadsheetRowReference> rows) {
        checkIdsInclusive(rows, "rows");
    }

    @Override
    SpreadsheetDelta handleCollection0(final Range<SpreadsheetRowReference> rows,
                                       final SpreadsheetDelta resource,
                                       final Map<HttpRequestAttribute<?>, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    String operation() {
        return "insertRows";
    }
}

