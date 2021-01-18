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

package walkingkooka.spreadsheet.parser;

import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.TypeNameTesting;
import walkingkooka.text.cursor.parser.ebnf.combinator.EbnfParserCombinatorSyntaxTreeTransformer;
import walkingkooka.text.cursor.parser.ebnf.combinator.EbnfParserCombinatorSyntaxTreeTransformerTesting;

public final class SpreadsheetParsersEbnfParserCombinatorSyntaxTreeTransformerTest implements ClassTesting2<SpreadsheetParsersEbnfParserCombinatorSyntaxTreeTransformer>,
        EbnfParserCombinatorSyntaxTreeTransformerTesting<SpreadsheetParsersEbnfParserCombinatorSyntaxTreeTransformer>,
        TypeNameTesting<SpreadsheetParsersEbnfParserCombinatorSyntaxTreeTransformer> {

    // Class............................................................................................................

    @Override
    public Class<SpreadsheetParsersEbnfParserCombinatorSyntaxTreeTransformer> type() {
        return SpreadsheetParsersEbnfParserCombinatorSyntaxTreeTransformer.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    // TypeNaming.......................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetParsers.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return EbnfParserCombinatorSyntaxTreeTransformer.class.getSimpleName();
    }
}