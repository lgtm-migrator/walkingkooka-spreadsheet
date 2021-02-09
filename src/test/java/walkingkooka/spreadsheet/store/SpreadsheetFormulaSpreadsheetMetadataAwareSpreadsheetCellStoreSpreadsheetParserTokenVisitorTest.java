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

import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.parser.SpreadsheetParserTokenVisitor;
import walkingkooka.text.cursor.parser.ParserToken;
import walkingkooka.visit.VisitorTesting;

public final class SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStoreSpreadsheetParserTokenVisitorTest implements ClassTesting<SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStoreSpreadsheetParserTokenVisitor>,
        VisitorTesting<SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStoreSpreadsheetParserTokenVisitor, ParserToken> {

    @Override
    public SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStoreSpreadsheetParserTokenVisitor createVisitor() {
        return new SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStoreSpreadsheetParserTokenVisitor(null);
    }

    @Override
    public Class<SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStoreSpreadsheetParserTokenVisitor> type() {
        return SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStoreSpreadsheetParserTokenVisitor.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public String typeNamePrefix() {
        return SpreadsheetFormulaSpreadsheetMetadataAwareSpreadsheetCellStore.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return SpreadsheetParserTokenVisitor.class.getSimpleName();
    }
}
