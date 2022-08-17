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
