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

public class TestChoices {
    @Test
    public void testCreatingChoices() {
        ChoicesSequence choices = new ChoicesSequence();

        Choice choice = choices.createNewChoice(1, true);
        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 0);

        choice = choices.createNewChoice(1, true);
        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 0);

        choice = choices.createNewChoice(2, true);
        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 0);

        choices.prepareForNewExploration();

        Assert.assertTrue(choices.containsChoiceWithNextValue());
        Assert.assertTrue(choices.hasUnseenValueFurtherInExploration());
        choice = choices.getNextChoiceInExploration();
        Assert.assertFalse(choice.hasNextValue());
        Assert.assertEquals(choice.currentValue(), 0);

        Assert.assertTrue(choices.containsChoiceWithNextValue());
        Assert.assertTrue(choices.hasUnseenValueFurtherInExploration());
        choice = choices.getNextChoiceInExploration();
        Assert.assertFalse(choice.hasNextValue());
        Assert.assertEquals(choice.currentValue(), 0);

        Assert.assertTrue(choices.containsChoiceWithNextValue());
        Assert.assertTrue(choices.hasUnseenValueFurtherInExploration());
        choice = choices.getNextChoiceInExploration();
        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 1);

        choices.prepareForNewExploration();

        Assert.assertFalse(choices.containsChoiceWithNextValue());
        Assert.assertFalse(choices.hasUnseenValueFurtherInExploration());
    }

    @Test
    public void testRemovingChoices() {
        ChoicesSequence choices = new ChoicesSequence();

        Choice choice = choices.createNewChoice(1, true);
        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 0);

        choice = choices.createNewChoice(1, true);
        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 0);

        choice = choices.createNewChoice(2, true);
        Assert.assertTrue(choice.hasNextValue());
        Assert.assertEquals(choice.nextValue(), 0);

        choices.prepareForNewExploration();

        choices.getNextChoiceInExploration();

        choices.removeAllChoicesAfterCurrentChoiceInExploration();
        Assert.assertFalse(choices.hasUnseenValueFurtherInExploration());
        Assert.assertFalse(choices.hasNextChoiceInExploration());
    }
}
