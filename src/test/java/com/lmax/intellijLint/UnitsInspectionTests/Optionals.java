package com.lmax.intellijLint.UnitsInspectionTests;

public class Optionals extends Base {
    public void testRightUntypedOnOptional() throws Exception {
        expectInspection("Assigning null to variable of type foo");
    }

    public void testLeftUntypedOnOptional() throws Exception {
        expectInspection("Assigning foo to variable of type null");
    }

    public void testMismatchedUnitsOnOptional() throws Exception {
        expectInspection("Assigning bar to variable of type foo");
    }

    /*
    TODO: fix these tests. Handling of optionals is incorrect atm.
    public void testOptionalOfUntyped() throws Exception {
        expectAssignmentInspection("null", "foo");
    }

    public void testCorrectOptionalOf() throws Exception {
        expectNoInspections();
    }

    public void testOptionalOfIncorrectType() throws Exception {
        expectAssignmentInspection("bar", "foo");
    }

    public void testOptionalEmpty() throws Exception {
        expectNoInspections();
    }
    */
}
