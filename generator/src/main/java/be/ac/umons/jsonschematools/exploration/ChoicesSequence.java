package be.ac.umons.jsonschematools.exploration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A finite sequence of choices.
 * 
 * When exploring the possibilities described by a schema, multiple choices must
 * be done in a row.
 * This class stores these choices in a sequential order.
 * 
 * It is possible to iterate over the choices starting from the first choice, to
 * add new choices, and to remove choices.
 * 
 * @author Gaëtan Staquet
 */
public class ChoicesSequence implements Iterable<Choice> {

    private final List<Choice> choices = new ArrayList<>();
    private int indexInExploration;

    public Choice createNewChoice(int nPossibilities, boolean exclusive) {
        Choice choice = new Choice(nPossibilities, exclusive);
        choices.add(choice);
        indexInExploration = choices.size();
        return choice;
    }

    /**
     * Remove all the choices that are after the given choice.
     * 
     * That is, the given choice become the last choice in the sequence.
     * 
     * If the given choice is not present in the sequence, the function throws an
     * {@link IndexOutOfBoundsException}.
     * 
     * @param choice The choice that must be the last in the sequence.
     */
    public void removeAllChoicesComingAfter(Choice choice) {
        while (peekLastChoice() != choice) {
            popLastChoice();
        }
        // We move the pointer one choice forward to keep a coherent behavior later on
        if (hasNextChoiceInExploration()) {
            getNextChoiceInExploration();
        }
    }

    private Choice peekLastChoice() {
        return choices.get(choices.size() - 1);
    }

    /**
     * Remove the last choice in the sequence.
     * 
     * If the sequence is empty, throws an {@link IndexOutOfBoundsException}.
     */
    public void popLastChoice() {
        choices.remove(choices.size() - 1);
        indexInExploration = choices.size() - 1;
    }

    /**
     * Checks whether the sequence contains a choice that has a value not yet
     * explored.
     * 
     * @return True if there is a choice that can take a new value in the sequence.
     */
    public boolean containsChoiceWithNextValue() {
        if (choices.isEmpty()) {
            return true;
        }
        for (Choice choice : choices) {
            if (choice.hasNextValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prepares the sequence for a new exploration of the schema.
     */
    void prepareForNewExploration() {
        indexInExploration = 0;
    }

    /**
     * Checks whether there is a choice already in the sequence for the current
     * exploration of the schema.
     * 
     * See {@link getNextChoiceInExploration} to move forward in the exploration.
     * 
     * @return True if the current choice is not the last.
     */
    public boolean hasNextChoiceInExploration() {
        return indexInExploration < choices.size();
    }

    /**
     * Checks whether there is a choice with a next value below the current choice
     * in the exploration.
     * 
     * See {@link getNextChoiceInExploration} to move forward in the exploration.
     * 
     * @return True if there is a choice further in the sequence with a next value.
     */
    public boolean hasUnseenValueFurtherInExploration() {
        if (!hasNextChoiceInExploration()) {
            return false;
        }
        for (int index = indexInExploration; index < choices.size(); index++) {
            if (choices.get(index).hasNextValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all the choices that are strictly after the current choice in the
     * exploration.
     * 
     * See {@link getNextChoiceInExploration} to move forward in the exploration.
     */
    public void removeAllChoicesAfterCurrentChoiceInExploration() {
        for (int index = indexInExploration; index < choices.size();) {
            choices.remove(choices.size() - 1);
        }
        indexInExploration = choices.size();
    }

    /**
     * Gets the next choice for the current exploration and moves forward in the
     * sequence.
     * 
     * If the exploration has reached the last choice, the function throws an
     * {@link IndexOutOfBoundsException}.
     * 
     * @return The next choice.
     */
    public Choice getNextChoiceInExploration() {
        return choices.get(indexInExploration++);
    }

    @Override
    public Iterator<Choice> iterator() {
        return choices.iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Choice choice : this) {
            builder.append(choice.toString());
            if (choice != peekLastChoice()) {
                builder.append(" -> ");
            }
        }
        return builder.toString();
    }
}
