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

package walkingkooka.spreadsheet.function;

import walkingkooka.net.AbsoluteUrl;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;

import java.util.Optional;

/**
 * Enhances {@link ExpressionFunctionContext} adding a few extra methods required by a spreadsheet during
 * function execution.
 */
public interface SpreadsheetExpressionFunctionContext extends ExpressionFunctionContext {

    /**
     * Returns the current cell that owns the expression or formula being executed.
     */
    Optional<SpreadsheetCell> cell();

    /**
     * Loads the cell for the given {@link SpreadsheetCellReference}
     */
    Optional<SpreadsheetCell> loadCell(final SpreadsheetCellReference cell);

    /**
     * Returns the {@link SpreadsheetMetadata} for the enclosing spreadsheet.
     */
    SpreadsheetMetadata spreadsheetMetadata();

    /**
     * Returns the base server url, which can then be used to create links to cells and more.
     * This is necessary for functions such as hyperlink which creates a link to a cell.
     */
    AbsoluteUrl serverUrl();
}
