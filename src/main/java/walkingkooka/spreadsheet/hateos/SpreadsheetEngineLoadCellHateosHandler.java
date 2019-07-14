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
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetCellReference;
import walkingkooka.spreadsheet.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HateosHandler} that calls {@link SpreadsheetEngine#saveCell(SpreadsheetCell, SpreadsheetEngineContext)}.
 */
final class SpreadsheetEngineLoadCellHateosHandler extends SpreadsheetEngineHateosHandler<SpreadsheetCellReference> {

    static SpreadsheetEngineLoadCellHateosHandler with(final SpreadsheetEngineEvaluation evaluation,
                                                       final SpreadsheetEngine engine,
                                                       final SpreadsheetEngineContext context) {
        Objects.requireNonNull(evaluation, "evaluation");

        check(engine, context);
        return new SpreadsheetEngineLoadCellHateosHandler(evaluation,
                engine,
                context);
    }

    private SpreadsheetEngineLoadCellHateosHandler(final SpreadsheetEngineEvaluation evaluation,
                                                   final SpreadsheetEngine engine,
                                                   final SpreadsheetEngineContext context) {
        super(engine, context);
        this.evaluation = evaluation;
    }

    @Override
    public Optional<SpreadsheetDelta<Optional<SpreadsheetCellReference>>> handle(final Optional<SpreadsheetCellReference> id,
                                                                                 final Optional<SpreadsheetDelta<Optional<SpreadsheetCellReference>>> resource,
                                                                                 final Map<HttpRequestAttribute<?>, Object> parameters) {
        final SpreadsheetCellReference reference = this.checkIdRequired(id);
        this.checkResourceEmpty(resource);
        this.checkParameters(parameters);

        return Optional.of(this.engine.loadCell(reference,
                this.evaluation,
                this.context));
    }

    private final SpreadsheetEngineEvaluation evaluation;

    @Override
    public Optional<SpreadsheetDelta<Range<SpreadsheetCellReference>>> handleCollection(final Range<SpreadsheetCellReference> ids,
                                                                                        final Optional<SpreadsheetDelta<Range<SpreadsheetCellReference>>> resource,
                                                                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        this.checkRangeNotNull(ids);
        this.checkResource(resource);
        this.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    String operation() {
        return "loadCell " + this.evaluation;
    }
}