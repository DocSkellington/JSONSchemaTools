package be.ac.umons.jsonschematools.exploration;

import java.util.BitSet;

/**
 * The encoding of a choice made while exploring all the possibilities described
 * by a schema.
 * 
 * It uses a {@link BitSet} to store the current choice.
 * If the choice is declared as exclusive (needed for {@code oneOf}, for
 * instance), it is guaranteed that the bitset always has at most one set bit.
 * If the choice is non-exclusive (needed for {@code anyOf}, for instance), the
 * bitset can have more than one set bit.
 * That is, if the choice is non-exclusive, it is possible to select multiple
 * possibilities at the same time.
 * 
 * It is possible to get the value in base 10 corresponding to the current
 * choice.
 * 
 * @author Gaëtan Staquet
 */
public class Choice {
    private final BitSet choice;
    private final int nPossibleChoices;
    private final boolean exclusive;
    private boolean firstValue;
    private int previousPossibility;

    /**
     * Creates an exclusive or non-exclusive choice over the given number of
     * possibilities
     * 
     * @param nPossibilities The number of possibilities
     * @param exclusive      Whether this choice is exclusive (i.e., whether each
     *                       possibility must be taken individually, or if we can
     *                       take multiple possibilities at the same time).
     */
    public Choice(int nPossibilities, boolean exclusive) {
        this.choice = new BitSet(nPossibilities);
        this.nPossibleChoices = nPossibilities;
        this.exclusive = exclusive;
        if (nPossibilities == 0) {
            firstValue = false;
        } else {
            this.firstValue = !exclusive;
        }
    }

    public int numberOfValues() {
        if (exclusive) {
            return nPossibleChoices;
        } else if (nPossibleChoices == 0) {
            return 0;
        } else {
            return 1 << nPossibleChoices;
        }
    }

    public boolean hasNextValue() {
        if (exclusive) {
            return choice.nextSetBit(0) != (nPossibleChoices - 1);
        } else {
            return firstValue || canAddOne();
        }
    }

    public int nextValue() {
        if (exclusive) {
            int bit = choice.nextSetBit(0);
            if (bit != -1) {
                choice.set(bit, false);
            }
            choice.set(bit + 1, true);

            previousPossibility = bit + 1;
            return previousPossibility;
        } else {
            if (firstValue) {
                firstValue = false;
                return 0;
            }

            addOne();

            previousPossibility = getValue();
            return previousPossibility;
        }
    }

    public int currentValue() {
        return previousPossibility;
    }

    public BitSet getBitSet() {
        return choice;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    private boolean canAddOne() {
        for (int i = 0; i < nPossibleChoices; i++) {
            if (!choice.get(i)) {
                return true;
            }
        }

        return false;
    }

    private void addOne() {
        for (int i = 0; i < nPossibleChoices; i++) {
            if (choice.get(i)) {
                choice.set(i, false);
            } else {
                choice.set(i, true);
                return;
            }
        }
    }

    private int getValue() {
        int value = 0;
        // nextSetBit(i) is inclusive, i.e., the bit number i is considered
        for (int i = choice.nextSetBit(0); i >= 0 && i < nPossibleChoices; i = choice.nextSetBit(i + 1)) {
            value += (1 << i);
        }
        return value;
    }

    @Override
    public String toString() {
        if (exclusive) {
            return "" + (currentValue() + 1) + "/" + numberOfValues() + " (exclusive)";
        } else {
            return "" + (currentValue() + 1) + "/" + numberOfValues() + " (not exclusive)";
        }
    }
}
