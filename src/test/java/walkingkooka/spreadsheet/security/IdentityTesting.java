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

package walkingkooka.spreadsheet.security;

import org.junit.jupiter.api.Test;
import walkingkooka.test.ClassTesting2;
import walkingkooka.test.HashCodeEqualsDefinedTesting;
import walkingkooka.test.ToStringTesting;
import walkingkooka.tree.json.HasJsonNode;
import walkingkooka.tree.json.HasJsonNodeTesting;
import walkingkooka.type.JavaVisibility;

import static org.junit.jupiter.api.Assertions.assertThrows;

public interface IdentityTesting<I extends Identity<ID> & HasJsonNode, ID extends IdentityId>
        extends ClassTesting2<I>,
        HasJsonNodeTesting<I>,
        HashCodeEqualsDefinedTesting<I>,
        ToStringTesting<I> {

    @Test
    default void testWithNullIdFails() {
        assertThrows(NullPointerException.class, () -> {
            this.createIdentity(null);
        });
    }

    @Override
    default I createObject() {
        return this.createIdentity();
    }

    default I createIdentity() {
        return this.createIdentity(this.createId());
    }

    @Override
    default I createHasJsonNode() {
        return this.createObject();
    }

    I createIdentity(final ID id);

    ID createId();

    @Override
    default JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
