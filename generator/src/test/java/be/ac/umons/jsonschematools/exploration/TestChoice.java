package be.ac.umons.jsonschematools.exploration;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestChoice {
    @Test
    public void testNextPossibilityNotExclusive() {
        Choice choice = new Choice(4, false);

        Assert.assertEquals(choice.numberOfValues(), 16);

        for (int i = 0 ; i < 16 ; i++) {
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
