/*
 * JSONSchemaTools - Generators and validator for JSON schema, with abstract values
 *
 * Copyright 2022 University of Mons, University of Antwerp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.ac.umons.jsonschematools.generator.exploration;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestChoice {
    @Test
    public void testNextPossibilityNotExclusive() {
        Choice choice = new Choice(4, false);

        Assert.assertEquals(choice.numberOfValues(), 16);

        for (int i = 0; i < 16; i++) {
            Assert.assertTrue(choice.hasNextValue());
            Assert.assertEquals(choice.nextValue(), i);
        }

        Assert.assertFalse(choice.hasNextValue());
    }

    @Test
    public void testNextPossibilityExclusive() {
        Choice choice = new Choice(4, true);

        Assert.assertEquals(choice.numberOfValues(), 4);

        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 0);

        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 1);

        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 2);

        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 3);

        Assert.assertFalse(choice.hasNextValue());
    }

    @Test
    public void testNextPossibilitySinglePossibility() {
        Choice choice = new Choice(1, true);
        Assert.assertEquals(choice.numberOfValues(), 1);

        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 0);

        Assert.assertFalse(choice.hasNextValue());

        choice = new Choice(1, false);
        Assert.assertEquals(choice.numberOfValues(), 2);

        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 0);

        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 1);

        Assert.assertFalse(choice.hasNextValue());
        Assert.assertEquals(choice.currentValue(), 1);
    }

    @Test
    public void testNoChoice() {
        Choice choice = new Choice(0, true);
        Assert.assertEquals(choice.numberOfValues(), 0);

        Assert.assertFalse(choice.hasNextValue());
        Assert.assertEquals(choice.currentValue(), 0);

        choice = new Choice(0, false);
        Assert.assertEquals(choice.numberOfValues(), 0);

        Assert.assertFalse(choice.hasNextValue());
        Assert.assertEquals(choice.currentValue(), 0);
    }
}
