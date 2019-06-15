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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.test.ClassTesting2;
import walkingkooka.test.ToStringTesting;
import walkingkooka.type.JavaVisibility;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface StoreTesting<S extends Store<K, V>, K, V> extends ClassTesting2<S>,
        ToStringTesting<S> {

    @Test
    default void testLoadNullIdFails() {
        assertThrows(NullPointerException.class, () -> {
            this.createStore().load(null);
        });
    }

    @Test
    default void testSaveNullFails() {
        assertThrows(NullPointerException.class, () -> {
            this.createStore().save(null);
        });
    }

    @Test
    default void testAddSaveWatcherNullFails() {
        assertThrows(NullPointerException.class, () -> {
            this.createStore().addSaveWatcher(null);
        });
    }

    @Test
    default void testAddSaveWatcherAndSave() {
        final V value = this.value();

        final S store = this.createStore();

        final List<V> fired = Lists.array();
        store.addSaveWatcher((s) -> fired.add(s));

        final V saved = store.save(value);

        assertEquals(Lists.of(saved), fired, "fired values");
    }

    @Test
    default void testAddSaveWatcherAndSaveTwiceFiresOnce() {
        final V value = this.value();

        final S store = this.createStore();

        final List<V> fired = Lists.array();
        store.addSaveWatcher((s) -> fired.add(s));

        final V saved = store.save(value);
        store.save(value);

        assertEquals(Lists.of(saved), fired, "fired values");
    }

    @Test
    default void testAddSaveWatcherAndRemove() {
        this.createStore().addSaveWatcher((v) -> {
        }).run();
    }

    @Test
    default void testDeleteNullFails() {
        assertThrows(NullPointerException.class, () -> {
            this.createStore().delete(null);
        });
    }

    @Test
    default void testAddDeleteWatcherNullFails() {
        assertThrows(NullPointerException.class, () -> {
            this.createStore().addDeleteWatcher(null);
        });
    }

    @Test
    default void testAddDeleteWatcherAndRemove() {
        this.createStore().addDeleteWatcher((k) -> {
        }).run();
    }

    @Test
    default void testAddDeleteWatcherAndDelete() {
        final V value = this.value();

        final S store = this.createStore();

        final List<K> fired = Lists.array();
        store.addDeleteWatcher((d) -> fired.add(d));

        store.save(value);

        final K id = this.id();
        store.delete(id);

        assertEquals(Lists.of(id), fired, "fired values");
    }

    @Test
    default void testIdsInvalidFromFails() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.createStore().ids(-1, 0);
        });
    }

    @Test
    default void testIdsInvalidCountFails() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.createStore().ids(0, -1);
        });
    }

    @Test
    default void testIdsFrom0AndCountZero() {
        this.idsAndCheck(this.createStore(), 0, 0);
    }

    @Test
    default void testValuesNullFromIdFails() {
        assertThrows(NullPointerException.class, () -> {
            this.createStore().values(null, 0);
        });
    }

    @Test
    default void testValuesInvalidCountFails() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.createStore().values(this.id(), -1);
        });
    }

    @Test
    default void testValueFromAndZeroCount() {
        this.valuesAndCheck(this.createStore(), this.id(), 0);
    }

    @Test
    default void testFirstIdWhenEmpty() {
        assertEquals(Optional.empty(),
                this.createStore().firstId());
    }

    @Test
    default void testFirstValueWhenEmpty() {
        assertEquals(Optional.empty(),
                this.createStore().firstValue());
    }

    @Test
    default void testAllWhenEmpty() {
        assertEquals(Lists.empty(),
                this.createStore().all());
    }

    S createStore();

    K id();

    V value();

    default void loadAndCheck(final S store, final K id, final V value) {
        assertEquals(Optional.of(value),
                store.load(id),
                () -> " store load " + id);
    }

    default void loadFailCheck(final K id) {
        this.loadFailCheck(this.createStore(), id);
    }

    default void loadFailCheck(final S store, final K id) {
        final Optional<V> value = store.load(id);
        assertEquals(Optional.empty(), value, () -> "Expected id " + id + " to fail");
    }

    default void countAndCheck(final Store<?, ?> store, final int count) {
        assertEquals(count, store.count(), () -> "Wrong count " + store);
    }

    default void idsAndCheck(final S store,
                             final int from,
                             final int to,
                             final K... ids) {
        this.idsAndCheck(store, from, to, Sets.of(ids));
    }

    default void idsAndCheck(final S store,
                             final int from,
                             final int to,
                             final Set<K> ids) {
        assertEquals(ids,
                store.ids(from, to),
                "ids from " + from + " count=" + to);
    }

    default void valuesAndCheck(final S store,
                                final K from,
                                final int count,
                                final V... values) {
        this.valuesAndCheck(store, from, count, Lists.of(values));
    }

    default void valuesAndCheck(final S store,
                                final K from,
                                final int count,
                                final List<V> values) {
        assertEquals(values,
                store.values(from, count),
                "values from " + from + " count=" + count);
    }

    @Override
    default JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
