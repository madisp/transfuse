/**
 * Copyright 2013 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidtransfuse.gen;

import org.androidtransfuse.util.GeneratedCodeRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author John Ericksen
 */
public class VariableNamerTest {

    private UniqueVariableNamer variableNamer;

    @Before
    public void setup() {
        variableNamer = new UniqueVariableNamer();
    }

    @Test
    public void testBuildName() {
        Assert.assertEquals("fieldInjectable" + GeneratedCodeRepository.SEPARATOR + "0", variableNamer.generateName(FieldInjectable.class));
    }

    @Test
    public void testMultipleNames() {
        Assert.assertEquals("fieldInjectable" + GeneratedCodeRepository.SEPARATOR + "0", variableNamer.generateName(FieldInjectable.class));
        Assert.assertEquals("fieldInjectable" + GeneratedCodeRepository.SEPARATOR + "1", variableNamer.generateName(FieldInjectable.class));
        Assert.assertEquals("fieldInjectable" + GeneratedCodeRepository.SEPARATOR + "2", variableNamer.generateName(FieldInjectable.class));
    }

    @Test
    public void testSmallClassName() {
        Assert.assertEquals("a" + GeneratedCodeRepository.SEPARATOR + "0", variableNamer.generateName(A.class));
    }


}