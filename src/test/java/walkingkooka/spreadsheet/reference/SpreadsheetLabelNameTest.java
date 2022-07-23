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
import walkingkooka.text.CaseSensitivity;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.util.PropertiesPath;
import walkingkooka.visit.Visiting;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final public class SpreadsheetLabelNameTest extends SpreadsheetCellReferenceOrLabelNameTestCase<SpreadsheetLabelName>
        implements NameTesting2<SpreadsheetLabelName, SpreadsheetLabelName> {

    @Test
    public void testCreateContainsSeparatorFails() {
        assertThrows(InvalidCharacterException.class, () -> SpreadsheetLabelName.with("xyz" + PropertiesPath.SEPARATOR.string()));
    }

    @Test
    public void testWithInvalidInitialFails() {
        assertThrows(InvalidCharacterException.class, () -> SpreadsheetLabelName.with("1abc"));
    }

    @Test
    public void testWithInvalidPartFails() {
        assertThrows(InvalidCharacterException.class, () -> SpreadsheetLabelName.with("abc$def"));
    }

    @Test
    public void testCellReferenceFails() {
        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetLabelName.with("A1")
        );

        this.checkEquals(
                "Label cannot be a valid cell reference=\"A1\"",
                thrown.getMessage()
        );
    }

    @Test
    public void testCellReferenceFails2() {
        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetLabelName.with("AB12")
        );

        this.checkEquals(
                "Label cannot be a valid cell reference=\"AB12\"",
                thrown.getMessage()
        );
    }

    @Test//(expected = IllegalArgumentException.class)
    public void testCellReferenceFails3() {
        SpreadsheetLabelName.with(SpreadsheetColumnReference.MAX_TOSTRING + "1");
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
    public void testWithInvalidRow() {
        this.createNameAndCheck("A" + (SpreadsheetRowReference.MAX_VALUE + 1 + 1));
    }

    @Test
    public void testMappingCellReference() {
        this.mappingAndCheck(SpreadsheetSelection.parseCell("A1"));
    }

    @Test
    public void testMappingLabel() {
        this.mappingAndCheck(SpreadsheetSelection.labelName("LABEL456"));
    }

    @Test
    public void testMappingRange() {
        this.mappingAndCheck(SpreadsheetSelection.parseCellRange("A1:b2"));
    }

    private void mappingAndCheck(final SpreadsheetExpressionReference reference) {
        final SpreadsheetLabelName label = SpreadsheetLabelName.with("LABEL123");

        final SpreadsheetLabelMapping mapping = label.mapping(reference);
        assertSame(label, mapping.label(), "label");
        assertSame(reference, mapping.reference(), "reference");
    }

    @Override
    public void testNameValidChars() {
        // test ignored because short generated names will clash with valid cell references and fail the test.
    }

    // toCellOrFail.....................................................................................................

    @Test
    public void testToCellOrFailFails() {
        this.toCellOrFailFails();
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

    // count...........................................................................................................

    @Test
    public void testCount() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.createSelection().count()
        );
    }

    // Predicate........................................................................................................

    @Test
    public void testTestFails() {
        assertThrows(UnsupportedOperationException.class, () -> this.createSelection().test(SpreadsheetSelection.parseCell("A1")));
    }

    // testCellRange.....................................................................................................

    @Test
    public void testRangeFails() {
        assertThrows(UnsupportedOperationException.class, () -> this.createSelection().testCellRange(SpreadsheetSelection.parseCellRange("A1")));
    }

    // testColumnRange..................................................................................................

    @Test
    public void testColumnFails() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.createSelection()
                        .testColumn(SpreadsheetReferenceKind.RELATIVE.firstColumn())
        );
    }

    // testRowRange..................................................................................................

    @Test
    public void testRowFails() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.createSelection()
                        .testRow(SpreadsheetReferenceKind.RELATIVE.firstRow())
        );
    }

    // SpreadsheetExpressionReference...................................................................................


    @Test
    public void testCellRangeFails() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> SpreadsheetLabelName.with("Label123").cellRange()
        );
    }

    // extendRange......................................................................................................

    @Override
    SpreadsheetLabelName parseRange(final String range) {
        return SpreadsheetSelection.labelName(range);
    }

    // SpreadsheetSelectionVisitor......................................................................................

    @Test
    public void testSpreadsheetSelectionVisitorAccept() {
        final StringBuilder b = new StringBuilder();
        final SpreadsheetLabelName selection = this.createSelection();

        new FakeSpreadsheetSelectionVisitor() {
            @Override
            protected Visiting startVisit(final SpreadsheetSelection s) {
                assertSame(selection, s);
                b.append("1");
                return Visiting.CONTINUE;
            }

            @Override
            protected void endVisit(final SpreadsheetSelection s) {
                assertSame(selection, s);
                b.append("2");
            }

            @Override
            protected void visit(final SpreadsheetLabelName s) {
                assertSame(selection, s);
                b.append("3");
            }
        }.accept(selection);
        this.checkEquals("132", b.toString());
    }

    // extendRange......................................................................................................

    @Test
    public void testExtendRange() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.createSelection()
                        .extendRange(
                                Optional.empty(),
                                SpreadsheetViewportSelectionAnchor.NONE
                        )
        );
    }

    // focused..........................................................................................................

    @Test
    public void testFocusedFails() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.createSelection().focused(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    // equalsIgnoreReferenceKind.........................................................................................

    @Test
    public void testEqualsIgnoreReferenceDifferentName() {
        this.equalsIgnoreReferenceKindAndCheck(this.createSelection(),
                SpreadsheetLabelName.with("different"),
                false);
    }

    // toRelative.......................................................................................................

    @Test
    public void testToRelative() {
        final SpreadsheetLabelName labelName = this.createSelection();
        final SpreadsheetLabelName relative = labelName.toRelative();
        assertSame(labelName, relative);
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
                SpreadsheetSelection.labelName("Label123"),
                "label Label123" + EOL
        );
    }

    // JsonNodeMarshallingTesting.......................................................................................

    @Test
    public void testJsonNodeUnmarshallString() {
        final String value = "LABEL123";
        this.unmarshallAndCheck(JsonNode.string(value),
                SpreadsheetLabelName.with(value));
    }

    // equalsIgnoreReferenceKind.................................................................................................

    @Test
    public void testEqualsIgnoreReferenceKindDifferent() {
        this.equalsIgnoreReferenceKindAndCheck(
                "Label1",
                "Label2",
                false
        );
    }

    @Test
    public void testEqualsDifferentCase() {
        this.checkEqualsAndHashCode(
                SpreadsheetLabelName.with("Label123"),
                SpreadsheetLabelName.with("LABEL123")
        );
    }

    @Override
    SpreadsheetLabelName createSelection() {
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

    // JsonNodeMarshallingTesting...........................................................................................

    @Override
    public SpreadsheetLabelName unmarshall(final JsonNode from,
                                           final JsonNodeUnmarshallContext context) {
        return SpreadsheetLabelName.unmarshallLabelName(from, context);
    }

    // ParseStringTesting...............................................................................................

    @Override
    public SpreadsheetLabelName parseString(final String text) {
        return SpreadsheetSelection.labelName(text);
    }
}
