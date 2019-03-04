package walkingkooka.spreadsheet.security;

import walkingkooka.tree.json.HasJsonNode;
import walkingkooka.tree.json.JsonNode;

/**
 * The primary key for a {@link Group}.
 */
public final class GroupId extends IdentityId {

    public static GroupId fromJsonNode(final JsonNode node) {
        return with(HasJsonNode.fromJsonNode(node, Long.class));
    }

    static GroupId with(final long value) {
        return new GroupId(value);
    }

    private GroupId(final long value) {
        super(value);
    }

    @Override
    boolean canBeEqual(final Object other) {
        return other instanceof GroupId;
    }

    // HasJsonNode.......................................................................................

    static {
        HasJsonNode.register("group-id",
                GroupId::fromJsonNode,
                GroupId.class);
    }
}
