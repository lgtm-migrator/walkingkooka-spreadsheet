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

package walkingkooka.spreadsheet.reference;

import walkingkooka.ToStringBuilder;
import walkingkooka.UsesToStringBuilder;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a selection within a viewport. Non ranges must not have an anchor, while ranges must have an anchor.
 */
public final class SpreadsheetViewportSelection implements UsesToStringBuilder {

    /**
     * Constant representing no anchor.
     */
    public final static Optional<SpreadsheetViewportSelectionAnchor> NO_ANCHOR = Optional.empty();

    static SpreadsheetViewportSelection with(final SpreadsheetSelection selection,
                                             final Optional<SpreadsheetViewportSelectionAnchor> anchor) {
        Objects.requireNonNull(anchor, "anchor");
        SpreadsheetViewportSelectionSpreadsheetSelectionVisitor.checkAnchor(selection, anchor.orElse(null));

        return new SpreadsheetViewportSelection(selection, anchor);
    }

    private SpreadsheetViewportSelection(final SpreadsheetSelection selection,
                                         final Optional<SpreadsheetViewportSelectionAnchor> anchor) {
        super();
        this.selection = selection;
        this.anchor = anchor;
    }

    public SpreadsheetSelection selection() {
        return this.selection;
    }

    private final SpreadsheetSelection selection;

    public Optional<SpreadsheetViewportSelectionAnchor> anchor() {
        return this.anchor;
    }

    private Optional<SpreadsheetViewportSelectionAnchor> anchor;

    @Override
    public int hashCode() {
        return Objects.hash(this.selection, this.anchor);
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof SpreadsheetViewportSelection && this.equals0((SpreadsheetViewportSelection) other);
    }

    private boolean equals0(final SpreadsheetViewportSelection other) {
        return this.selection.equals(other.selection) && this.anchor.equals(other.anchor);
    }

    @Override
    public String toString() {
        return ToStringBuilder.buildFrom(this);
    }

    @Override
    public void buildToString(final ToStringBuilder builder) {
        builder.value(this.selection).value(this.anchor);
    }

    // Json.............................................................................................................

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetViewportSelection.class),
                SpreadsheetViewportSelection::unmarshall,
                SpreadsheetViewportSelection::marshall,
                SpreadsheetViewportSelection.class
        );
    }

    /**
     * Unmarshalls a json object back into a {@link SpreadsheetViewportSelection}.
     */
    static SpreadsheetViewportSelection unmarshall(final JsonNode node,
                                                   final JsonNodeUnmarshallContext context) {
        SpreadsheetSelection selection = null;
        SpreadsheetViewportSelectionAnchor anchor = null;

        for (final JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case SELECTION_PROPERTY_STRING:
                    selection = context.unmarshallWithType(child);
                    break;
                case ANCHOR_PROPERTY_STRING:
                    anchor = SpreadsheetViewportSelectionAnchor.valueOf(child.stringOrFail());
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, node);
                    break;
            }
        }

        return new SpreadsheetViewportSelection(
                selection,
                Optional.ofNullable(anchor)
        );
    }

    /**
     * Creates a JSON object to represent this {@link SpreadsheetViewportSelection}.
     */
    private JsonNode marshall(final JsonNodeMarshallContext context) {
        JsonObject object = JsonNode.object();

        object = object.set(SELECTION_PROPERTY, context.marshallWithType(this.selection));

        final Optional<SpreadsheetViewportSelectionAnchor> anchor = this.anchor();
        if (anchor.isPresent()) {
            object = object.set(ANCHOR_PROPERTY, JsonNode.string(anchor.get().toString()));
        }

        return object;
    }

    private final static String SELECTION_PROPERTY_STRING = "selection";
    private final static String ANCHOR_PROPERTY_STRING = "anchor";

    // @VisibleForTesting

    final static JsonPropertyName SELECTION_PROPERTY = JsonPropertyName.with(SELECTION_PROPERTY_STRING);
    final static JsonPropertyName ANCHOR_PROPERTY = JsonPropertyName.with(ANCHOR_PROPERTY_STRING);
}
