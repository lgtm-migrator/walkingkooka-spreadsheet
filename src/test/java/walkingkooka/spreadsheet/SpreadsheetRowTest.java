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

package walkingkooka.spreadsheet;

import org.junit.jupiter.api.Test;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;
import walkingkooka.tree.json.patch.PatchableTesting;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetRowTest extends SpreadsheetColumnOrRowTestCase<SpreadsheetRow, SpreadsheetRowReference>
        implements PatchableTesting<SpreadsheetRow> {

    private final static int ROW = 20;
    private final static SpreadsheetRowReference REFERENCE = reference(ROW);

    @Test
    public void testWithNullReferenceFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetRow.with(null));
    }

    @Test
    public void testWith() {
        final SpreadsheetRow row = this.createRow();

        this.checkReference(row);
        this.checkHidden(row);
    }

    // SetReference.....................................................................................................

    @Test
    public void testSetReferenceNullFails() {
        assertThrows(NullPointerException.class, () -> this.createRow().setReference(null));
    }

    @Test
    public void testSetReferenceSame() {
        final SpreadsheetRow row = this.createRow();
        assertSame(row, row.setReference(row.reference()));
    }

    @Test
    public void testSetReferenceDifferent() {
        final SpreadsheetRow row = this.createRow();
        final SpreadsheetRowReference differentReference = differentReference();
        final SpreadsheetRow different = row.setReference(differentReference);
        assertNotSame(row, different);

        this.checkReference(different, differentReference);

        this.checkReference(row);
    }

    // SetHidden........................................................................................................

    @Test
    public void testSetHiddenSame() {
        final SpreadsheetRow row = this.createRow();

        this.checkEquals(row, row.setHidden(row.hidden()));
    }

    @Test
    public void testSetHiddenDifferent() {
        final SpreadsheetRow row = this.createRow();
        final boolean differentHidden = differentHidden();
        final SpreadsheetRow different = row.setHidden(differentHidden);
        assertNotSame(row, different);

        this.checkHidden(different, differentHidden);

        this.checkHidden(row);
    }

    // equals .............................................................................................

    @Test
    public void testCompareDifferentRow() {
        this.compareToAndCheckLess(
                this.createComparable(ROW + 999)
        );
    }

    @Test
    public void testCompareDifferentHidden() {
        this.compareToAndCheckEquals(
                this.createComparable()
                        .setHidden(differentHidden())
        );
    }

    // compareTo0..........................................................................................................

    @Test
    public void testArraySort() {
        final SpreadsheetRow row1 = SpreadsheetSelection.parseRow("1").row();
        final SpreadsheetRow row2 = SpreadsheetSelection.parseRow("2").row();
        final SpreadsheetRow row3 = SpreadsheetSelection.parseRow("3").row();
        final SpreadsheetRow row4 = SpreadsheetSelection.parseRow("$4").row();

        this.compareToArraySortAndCheck(row3, row1, row4, row2,
                row1, row2, row3, row4);
    }

    // JsonNodeMarshallingTesting.......................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
                SpreadsheetSelection.parseRow("123").row(),
                "{\n" +
                        "  \"123\": {\n" +
                        "    \"hidden\": false\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testMarshallAbsolute() {
        this.marshallAndCheck(
                this.createRow(),
                "{\n" +
                        "  \"$21\": {\n" +
                        "    \"hidden\": false\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testMarshallAbsoluteHidden() {
        this.marshallAndCheck(
                this.createRow()
                        .setHidden(true),
                "{\n" +
                        "  \"$21\": {\n" +
                        "    \"hidden\": true\n" +
                        "  }\n" +
                        "}"
        );
    }

    // Patchable........................................................................................................

    @Test
    public void testPatchHiddenTrue() {
        final SpreadsheetRow row = SpreadsheetSelection.parseRow("1").row();

        this.patchAndCheck(
                row,
                "{\n" +
                        "  \"hidden\": true\n" +
                        "}",
                row.setHidden(true)
        );
    }

    @Test
    public void testPatchHiddenFalse() {
        final SpreadsheetRow row = SpreadsheetSelection.parseRow("2").row();

        this.patchAndCheck(
                row.setHidden(true),
                "{\n" +
                        "  \"hidden\": false\n" +
                        "}",
                row.setHidden(false)
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrintableHidden() {
        final SpreadsheetRow row = this.createObject()
                .setHidden(true);

        this.treePrintAndCheck(
                row,
                "" + REFERENCE + EOL +
                        "  hidden" + EOL
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                REFERENCE.row(),
                "$21"
        );
    }

    @Test
    public void testToStringHiddenTrue() {
        this.toStringAndCheck(
                REFERENCE.row()
                        .setHidden(true),
                "$21 hidden=true"
        );
    }

    // helpers.........................................................................................................

    private SpreadsheetRow createRow() {
        return this.createComparable();
    }

    @Override
    public SpreadsheetRow createComparable() {
        return this.createComparable(ROW);
    }

    private SpreadsheetRow createComparable(final int row) {
        return reference(row).row();
    }

    private static SpreadsheetRowReference differentReference() {
        return reference(999);
    }

    private static SpreadsheetRowReference reference(final int row) {
        return SpreadsheetReferenceKind.ABSOLUTE.row(row);
    }

    private void checkReference(final SpreadsheetRow row) {
        this.checkReference(row, REFERENCE);
    }

    private void checkHidden(final SpreadsheetRow row) {
        this.checkHidden(row, false);
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetRow> type() {
        return SpreadsheetRow.class;
    }

    // JsonNodeMarshallingTesting......................................................................................

    @Override
    public SpreadsheetRow unmarshall(final JsonNode jsonNode,
                                     final JsonNodeUnmarshallContext context) {
        return SpreadsheetRow.unmarshall(jsonNode, context);
    }

    // PatchableTesting.................................................................................................

    @Override
    public SpreadsheetRow createPatchable() {
        return this.createRow();
    }

    @Override
    public JsonNode createPatch() {
        return JsonNode.object();
    }

    @Override
    public JsonNodeUnmarshallContext createPatchContext() {
        return JsonNodeUnmarshallContexts.fake();
    }
}
