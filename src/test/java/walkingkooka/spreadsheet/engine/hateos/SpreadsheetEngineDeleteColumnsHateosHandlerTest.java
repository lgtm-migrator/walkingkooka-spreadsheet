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

package walkingkooka.spreadsheet.engine.hateos;

import org.junit.jupiter.api.Test;
import walkingkooka.compare.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SpreadsheetEngineDeleteColumnsHateosHandlerTest extends SpreadsheetEngineHateosHandlerTestCase2<SpreadsheetEngineDeleteColumnsHateosHandler,
        SpreadsheetColumnReference> {

    @Test
    public void testDeleteColumn() {
        final Optional<SpreadsheetColumnReference> column = this.id();
        final Optional<SpreadsheetDelta<Optional<SpreadsheetColumnReference>>> resource = this.resource();

        this.handleAndCheck(this.createHandler(new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta<Range<SpreadsheetColumnReference>> deleteColumns(final SpreadsheetColumnReference c,
                                                                                             final int count,
                                                                                             final SpreadsheetEngineContext context) {
                        assertEquals(column.get(), c, "column");
                        assertEquals(1, count, "count");
                        return delta(Range.singleton(column.get()));
                    }
                }),
                column,
                resource,
                HateosHandler.NO_PARAMETERS,
                Optional.of(SpreadsheetDelta.withId(column, SpreadsheetDelta.NO_CELLS)));
    }

    @Test
    public void testDeleteSeveralColumns() {
        final Optional<SpreadsheetDelta<Range<SpreadsheetColumnReference>>> resource = this.collectionResource();

        final Range<SpreadsheetColumnReference> range = SpreadsheetColumnReference.parseRange("C:E");
        final SpreadsheetDelta<Range<SpreadsheetColumnReference>> delta = delta(range);

        this.handleCollectionAndCheck(this.createHandler(new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta<Range<SpreadsheetColumnReference>> deleteColumns(final SpreadsheetColumnReference c,
                                                                                             final int count,
                                                                                             final SpreadsheetEngineContext context) {
                        assertEquals(SpreadsheetColumnReference.parse("C"), c, "column");
                        assertEquals(3, count, "count"); // C, D & E
                        return delta;
                    }
                }),
                range, // 2 inclusive
                resource,
                HateosHandler.NO_PARAMETERS,
                Optional.of(delta));
    }

    @Test
    public void testDeleteAllColumnsFails() {
        this.handleCollectionFails2(Range.all());
    }

    @Test
    public void testDeleteOpenRangeBeginFails() {
        this.handleCollectionFails2(Range.lessThanEquals(SpreadsheetColumnReference.parse("A")));
    }

    @Test
    public void testDeleteOpenRangeEndFails() {
        this.handleCollectionFails2(Range.greaterThanEquals(SpreadsheetColumnReference.parse("A")));
    }

    private void handleCollectionFails2(final Range<SpreadsheetColumnReference> columns) {
        assertEquals("Range with both columns required=" + columns,
                this.handleCollectionFails(columns,
                        this.collectionResource(),
                        HateosHandler.NO_PARAMETERS,
                        IllegalArgumentException.class).getMessage(),
                "message");
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler().toString(), "SpreadsheetEngine.deleteColumns");
    }

    private SpreadsheetEngineDeleteColumnsHateosHandler createHandler(final SpreadsheetEngine engine) {
        return this.createHandler(engine, this.engineContext());
    }

    @Override
    SpreadsheetEngineDeleteColumnsHateosHandler createHandler(final SpreadsheetEngine engine,
                                                              final SpreadsheetEngineContext context) {
        return SpreadsheetEngineDeleteColumnsHateosHandler.with(engine, context);
    }

    @Override
    public Optional<SpreadsheetColumnReference> id() {
        return Optional.of(SpreadsheetColumnReference.parse("C"));
    }

    @Override
    public Range<SpreadsheetColumnReference> collection() {
        return SpreadsheetColumnReference.parseRange("C:E");
    }

    @Override
    public Optional<SpreadsheetDelta<Optional<SpreadsheetColumnReference>>> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetDelta<Range<SpreadsheetColumnReference>>> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosHandler.NO_PARAMETERS;
    }

    @Override
    SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine();
    }

    private SpreadsheetDelta<Range<SpreadsheetColumnReference>> delta(final Range<SpreadsheetColumnReference> range) {
        return SpreadsheetDelta.withRange(range, SpreadsheetDelta.NO_CELLS);
    }

    @Override
    public Class<SpreadsheetEngineDeleteColumnsHateosHandler> type() {
        return SpreadsheetEngineDeleteColumnsHateosHandler.class;
    }
}