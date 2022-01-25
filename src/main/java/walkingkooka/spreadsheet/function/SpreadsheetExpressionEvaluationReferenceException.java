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

import walkingkooka.spreadsheet.HasSpreadsheetErrorKind;
import walkingkooka.spreadsheet.SpreadsheetErrorKind;
import walkingkooka.tree.expression.ExpressionEvaluationReferenceException;
import walkingkooka.tree.expression.ExpressionReference;

public final class SpreadsheetExpressionEvaluationReferenceException extends ExpressionEvaluationReferenceException implements HasSpreadsheetErrorKind {

    private static final long serialVersionUID = 1L;

    protected SpreadsheetExpressionEvaluationReferenceException() {
        super();
    }

    public SpreadsheetExpressionEvaluationReferenceException(final String message,
                                                             final ExpressionReference reference) {
        super(message, reference);
    }

    public SpreadsheetExpressionEvaluationReferenceException(final String message,
                                                             final ExpressionReference reference,
                                                             final Throwable cause) {
        super(message, reference, cause);
    }

    @Override
    public SpreadsheetErrorKind spreadsheetErrorKind() {
        return SpreadsheetErrorKind.REF;
    }
}