/*
 *    Copyright 2017 LMAX Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.lmax.intellijLint.UnitsInspectionTests;

import java.io.IOException;

public class AnnotateVariableQuickFix extends Base {

    public void testMethod() throws Exception {
        expectInspection("Returning null when expecting foo");
        applyQuickFix();
    }

    private void applyQuickFix() throws IOException {
        applyQuickFix(getTestDirectoryName());
    }

    private void applyQuickFix(String filename) throws IOException {
        myFixture.getAllQuickFixes()
                .stream()
                .filter(x -> x.getText().startsWith("Annotate variable"))
                .forEach(intention -> myFixture.launchAction(intention));

        expectNoInspections();

        myFixture.checkResultByFile(filename + "Fixed.java");
    }
}
