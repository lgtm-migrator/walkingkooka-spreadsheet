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

import walkingkooka.compare.Range;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HasHateosLinkId;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.text.CharSequences;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * An abstract {@link HateosHandler} that includes uses a {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext} to do things.
 */
abstract class SpreadsheetEngineHateosHandler<I extends Comparable<I> & HasHateosLinkId>
        implements HateosHandler<I, SpreadsheetDelta<Optional<I>>, SpreadsheetDelta<Range<I>>> {

    /**
     * Checks required factory method parameters are not null.
     */
    static void check(final SpreadsheetEngine engine,
                      final SpreadsheetEngineContext context) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(context, "context");
    }

    /**
     * Package private to limit sub classing.
     */
    SpreadsheetEngineHateosHandler(final SpreadsheetEngine engine,
                                   final SpreadsheetEngineContext context) {
        super();
        this.engine = engine;
        this.context = context;
    }

    final SpreadsheetEngine engine;
    final SpreadsheetEngineContext context;

    @Override
    public final String toString() {
        return SpreadsheetEngine.class.getSimpleName() + "." + this.operation();
    }

    abstract String operation();

    // Optional<I>......................................................................................................

    final I checkIdRequired(final Optional<I> id) {
        checkIdNotNull(id);

        return id.orElseThrow(() -> new IllegalArgumentException("Id missing"));
    }

    final Optional<I> checkIdNotNull(final Optional<I> id) {
        Objects.requireNonNull(id, "id");
        return id;
    }

    // Range<I>.........................................................................................................

    /**
     * Checks that the range bounds are not null and both are inclusive.
     */
    final void checkRangeBounded(final Range<?> range, final String label) {
        Objects.requireNonNull(range, label);

        if (!range.lowerBound().isInclusive() || !range.upperBound().isInclusive()) {
            throw new IllegalArgumentException("Range with both " + label + " required=" + range);
        }
    }

    final Range<I> checkRangeNotNull(final Range<I> ids) {
        Objects.requireNonNull(ids, "ids");
        return ids;
    }

    // Optional<RESOURCE>...............................................................................................

    /**
     * Complains if the resource is null.
     */
    final void checkResource(final Optional<? extends HateosResource<?>> resource) {
        Objects.requireNonNull(resource, "resource");
    }

    /**
     * Complains if the resource is null or present.
     */
    final void checkResourceEmpty(final Optional<? extends HateosResource<?>> resource) {
        checkResource(resource);
        resource.ifPresent((r) -> {
            throw new IllegalArgumentException("Resource not allowed=" + r);
        });
    }

    /**
     * Complains if the resource is absent.
     */
    final <T extends HateosResource<?>> T checkResourceNotEmpty(final Optional<T> resource) {
        checkResource(resource);
        return resource.orElseThrow(() -> new IllegalArgumentException("Required resource missing"));
    }

    // parameters.......................................................................................................

    /**
     * Checks parameters are present.
     */
    final void checkParameters(final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(parameters, "parameters");
    }

    /**
     * Fetches a required single parameter value and converts or fails.
     */
    final <T> T parameterValueOrFail(final Map<HttpRequestAttribute<?>, Object> parameters,
                                     final UrlParameterName parameter,
                                     final Function<String, T> converter) {
        final Optional<List<String>> maybeValues = parameter.parameterValue(parameters);
        if (!maybeValues.isPresent()) {
            throw new IllegalArgumentException("Required parameter " + parameter + " missing");
        }
        final List<String> values = maybeValues.get();
        if (values.size() != 1) {
            throw new IllegalArgumentException("Required parameter " + parameter + " incorrect=" + values);
        }
        final String value = values.get(0);
        try {
            return converter.apply(value);
        } catch (final NullPointerException | IllegalArgumentException cause) {
            throw cause;
        } catch (final Exception cause) {
            throw new IllegalArgumentException("Invalid parameter " + parameter + " value " + CharSequences.quoteIfChars(value));
        }
    }
}