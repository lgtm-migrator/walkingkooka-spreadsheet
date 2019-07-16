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

import org.junit.jupiter.api.Test;
import walkingkooka.InvalidCharacterException;
import walkingkooka.naming.NameTesting2;
import walkingkooka.naming.PropertiesPath;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.tree.expression.ExpressionReference;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonNodeException;
import walkingkooka.tree.visit.Visiting;
import walkingkooka.type.JavaVisibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final public class SpreadsheetLabelNameTest extends SpreadsheetExpressionReferenceTestCase<SpreadsheetLabelName>
        implements NameTesting2<SpreadsheetLabelName, SpreadsheetLabelName> {

    @Test
    public void testCreateContainsSeparatorFails() {
        assertThrows(InvalidCharacterException.class, () -> {
            SpreadsheetLabelName.with("xyz" + PropertiesPath.SEPARATOR.string());
        });
    }

    @Test
    public void testWithInvalidInitialFails() {
        assertThrows(InvalidCharacterException.class, () -> {
            SpreadsheetLabelName.with("1abc");
        });
    }

    @Test
    public void testWithInvalidPartFails() {
        assertThrows(InvalidCharacterException.class, () -> {
            SpreadsheetLabelName.with("abc$def");
        });
    }

    @Test
    public void testCellReferenceFails() {
        assertThrows(IllegalArgumentException.class, () -> {
            SpreadsheetLabelName.with("A1");
        });
    }

    @Test
    public void testCellReferenceFails2() {
        assertThrows(IllegalArgumentException.class, () -> {
            SpreadsheetLabelName.with("AB12");
        });
    }

    @Test//(expected = IllegalArgumentException.class)
    public void testCellReferenceFails3() {
        SpreadsheetLabelName.with(SpreadsheetColumnReference.MAX_ROW_NAME + "1");
    }

    @Test
    public void testWith2() {
        this.createNameAndCheck("ZZZ1");
    }

    @Test
    public void testWith3() {
        this.createNameAndCheck("A123Hello");
    }

    @Test
    public void testWith4() {
        this.createNameAndCheck("A1B2C2");
    }

    @Test
    public void testWithMissingRow() {
        this.createNameAndCheck("A");
    }

    @Test
    public void testWithMissingRow2() {
        this.createNameAndCheck("ABC");
    }

    @Test
    public void testWithEnormousColumn() {
        this.createNameAndCheck("ABCDEF1");
    }

    @Test
    public void testWithEnormousColumn2() {
        this.createNameAndCheck("ABCDEF");
    }

    @Test
    public void testWithEnormousRow() {
        this.createNameAndCheck("A" + (SpreadsheetRowReference.MAX + 1));
    }

    @Test
    public void testMapping() {
        final SpreadsheetLabelName label = SpreadsheetLabelName.with("LABEL123");
        final SpreadsheetExpressionReference reference = SpreadsheetExpressionReference.parse("A1");

        final SpreadsheetLabelMapping mapping = label.mapping(reference);
        assertSame(label, mapping.label(), "label");
        assertSame(reference, mapping.reference(), "reference");
    }

    @Override
    public void testNameValidChars() {
        // test ignored because short generated names will clash with valid cell references and fail the test.
    }

    // Comparator ......................................................................................................

    @Test
    public void testSort() {
        final SpreadsheetLabelName a1 = SpreadsheetLabelName.with("LABELa1");
        final SpreadsheetLabelName b2 = SpreadsheetLabelName.with("LABELB2");
        final SpreadsheetLabelName c3 = SpreadsheetLabelName.with("LABELC3");
        final SpreadsheetLabelName d4 = SpreadsheetLabelName.with("LABELd4");

        this.compareToArraySortAndCheck(d4, c3, a1, b2,
                a1, b2, c3, d4);
    }

    // HasJsonNode......................................................................................................

    @Test
    public void testHateosLinkIdAbsoluteReference() {
        this.hateosLinkIdAndCheck(SpreadsheetLabelName.labelName("LABEL1234"), "LABEL1234");
    }

    // SpreadsheetExpressionReferenceVisitor.............................................................................

    @Test
    public void testAccept() {
        final StringBuilder b = new StringBuilder();
        final SpreadsheetLabelName reference = this.createReference();

        new FakeSpreadsheetExpressionReferenceVisitor() {
            @Override
            protected Visiting startVisit(final ExpressionReference r) {
                assertSame(reference, r);
                b.append("1");
                return Visiting.CONTINUE;
            }

            @Override
            protected void endVisit(final ExpressionReference r) {
                assertSame(reference, r);
                b.append("2");
            }

            @Override
            protected Visiting startVisit(final SpreadsheetExpressionReference r) {
                assertSame(reference, r);
                b.append("3");
                return Visiting.CONTINUE;
            }

            @Override
            protected void endVisit(final SpreadsheetExpressionReference r) {
                assertSame(reference, r);
                b.append("4");
            }

            @Override
            protected void visit(final SpreadsheetLabelName r) {
                assertSame(reference, r);
                b.append("5");
            }
        }.accept(reference);
        assertEquals("13542", b.toString());
    }

    // HasJsonNode..................................................................................................

    @Test
    public void testFromJsonNodeBooleanFails() {
        this.fromJsonNodeFails(JsonNode.booleanNode(true), JsonNodeException.class);
    }

    @Test
    public void testFromJsonNodeNumberFails() {
        this.fromJsonNodeFails(JsonNode.number(123), JsonNodeException.class);
    }

    @Test
    public void testFromJsonNodeArrayFails() {
        this.fromJsonNodeFails(JsonNode.array(), JsonNodeException.class);
    }

    @Test
    public void testFromJsonNodeObjectFails() {
        this.fromJsonNodeFails(JsonNode.object(), JsonNodeException.class);
    }

    @Test
    public void testFromJsonNodeString() {
        final String value = "LABEL123";
        this.fromJsonNodeAndCheck(JsonNode.string(value),
                SpreadsheetLabelName.with(value));
    }

    @Override
    SpreadsheetLabelName createReference() {
        return this.createComparable();
    }

    @Override
    public SpreadsheetLabelName createName(final String name) {
        return SpreadsheetLabelName.with(name);
    }

    @Override
    public CaseSensitivity caseSensitivity() {
        return CaseSensitivity.INSENSITIVE;
    }

    @Override
    public String nameText() {
        return "state";
    }

    @Override
    public String differentNameText() {
        return "different";
    }

    @Override
    public String nameTextLess() {
        return "postcode";
    }

    @Override
    public int minLength() {
        return 1;
    }

    @Override
    public int maxLength() {
        return SpreadsheetLabelName.MAX_LENGTH;
    }

    @Override
    public String possibleValidChars(final int position) {
        return 0 == position ?
                ASCII_LETTERS :
                ASCII_LETTERS_DIGITS + "_";
    }

    @Override
    public String possibleInvalidChars(final int position) {
        return 0 == position ?
                ASCII_DIGITS + CONTROL + "_!@#$%^&*()" :
                CONTROL + "!@#$%^&*()";
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetLabelName> type() {
        return SpreadsheetLabelName.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    // HasJsonNodeTesting...............................................................................................

    @Override
    public SpreadsheetLabelName fromJsonNode(final JsonNode from) {
        return SpreadsheetLabelName.fromJsonNodeLabelName(from);
    }
}