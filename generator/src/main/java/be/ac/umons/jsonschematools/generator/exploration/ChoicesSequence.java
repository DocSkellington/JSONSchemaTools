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

import java.util.ArrayList;
import java.util.BitSet;
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

    /**
     * Using a {@link Choice}, gets the next possible value between {@code minValue}
     * and {@code maxValue} (both bounds are included).
     * 
     * If there are no other possibilities, throws a {@code RuntimeException}.
     * 
     * @param minValue The minimal value
     * @param maxValue The maximal value
     * @return The next possible value
     */
    public Integer getNextValueBetween(int minValue, int maxValue) {
        if (hasNextChoiceInExploration()) {
            Choice choiceInRun = getNextChoiceInExploration();
            if (hasUnseenValueFurtherInExploration()) {
                return minValue + choiceInRun.currentValue();
            }
            removeAllChoicesAfterCurrentChoiceInExploration();

            if (choiceInRun.hasNextValue()) {
                return minValue + choiceInRun.nextValue();
            } else {
                return null;
            }
        } else {
            Choice choice = createNewChoice(maxValue - minValue + 1, true);
            return minValue + choice.nextValue();
        }
    }

    /**
     * Gets the next possible index of a list, i.e., a number between 0 and
     * {@code listSize - 1}.
     * 
     * @param listSize The size of the list
     * @return The next possible index
     */
    public Integer getIndexNextExclusiveSelectionInList(int listSize) {
        return getNextValueBetween(0, listSize - 1);
    }

    /**
     * Gets the next possible {@link BitSet} that represents a selection of
     * {@code lengthSelection} elements in a list.
     * 
     * @param listSize        The size of the whole list
     * @param lengthSelection The number of elements to select
     * @return A {@link BitSet} representing the selection
     */
    public BitSet getBitSetNextInclusiveSelectionInList(int listSize, int lengthSelection) {
        if (hasNextChoiceInExploration()) {
            Choice choiceInRun = getNextChoiceInExploration();
            if (hasUnseenValueFurtherInExploration()) {
                return choiceInRun.getBitSet();
            } else {
                removeAllChoicesAfterCurrentChoiceInExploration();

                BitSet selection = getNextValidBitSetForOptionalKeys(lengthSelection, choiceInRun);
                if (selection == null) {
                    popLastChoice();
                }
                return selection;
            }
        } else {
            Choice choice = createNewChoice(listSize, false);
            BitSet selection = getNextValidBitSetForOptionalKeys(lengthSelection, choice);
            if (selection == null) {
                popLastChoice();
            }
            return selection;
        }
    }

    private BitSet getNextValidBitSetForOptionalKeys(final int lengthSelection, final Choice choice) {
        BitSet selection = null;
        while (selection == null || selection.cardinality() != lengthSelection) {
            if (choice.hasNextValue()) {
                choice.nextValue();
                selection = choice.getBitSet();
            } else {
                return null;
            }
        }
        return selection;
    }

    /**
     * Gets the next possible {@link Choice} for selecting one or multiple elements
     * in a list.
     * 
     * @param listSize        The size of the list
     * @param exclusiveChoice Whether one element or multiple elements can be
     *                        selected at any time
     * @param skipFirst       If true, skips the first possible value (i.e., zero)
     * @return The {@link Choice}
     */
    public Choice getChoiceForSelectionInList(int listSize, boolean exclusiveChoice, boolean skipFirst) {
        if (hasNextChoiceInExploration()) {
            return getNextChoiceInExploration();
        } else {
            Choice choice = createNewChoice(listSize, exclusiveChoice);
            if (skipFirst) {
                choice.nextValue();
            }
            return choice;
        }
    }

    /**
     * Gets the next Boolean value.
     * 
     * @return The next Boolean
     */
    public Boolean getNextBooleanValue() {
        if (hasNextChoiceInExploration()) {
            Choice choiceInExploration = getNextChoiceInExploration();
            if (hasUnseenValueFurtherInExploration()) {
                return choiceInExploration.currentValue() == 1;
            }
            removeAllChoicesAfterCurrentChoiceInExploration();

            if (choiceInExploration.hasNextValue()) {
                return choiceInExploration.nextValue() == 1;
            } else {
                return null;
            }
        } else {
            Choice choice = createNewChoice(1, false);
            return choice.nextValue() == 1;
        }
    }
}
