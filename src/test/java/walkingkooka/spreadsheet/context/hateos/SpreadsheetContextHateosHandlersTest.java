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
package walkingkooka.spreadsheet.context.hateos;

import walkingkooka.test.ClassTesting2;
import walkingkooka.test.PublicStaticHelperTesting;
import walkingkooka.type.JavaVisibility;

import java.lang.reflect.Method;

public final class SpreadsheetContextHateosHandlersTest implements ClassTesting2<SpreadsheetContextHateosHandlers>,
        PublicStaticHelperTesting<SpreadsheetContextHateosHandlers> {

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetContextHateosHandlers> type() {
        return SpreadsheetContextHateosHandlers.class;
    }

    @Override
    public boolean canHavePublicTypes(final Method method) {
        return false;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}