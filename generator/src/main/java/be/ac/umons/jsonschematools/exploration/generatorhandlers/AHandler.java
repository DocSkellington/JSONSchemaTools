package be.ac.umons.jsonschematools.exploration.generatorhandlers;

import be.ac.umons.jsonschematools.exploration.Choice;
import be.ac.umons.jsonschematools.exploration.ChoicesSequence;

/**
 * Abstract implementation of a handler.
 * 
 * @author Gaëtan Staquet
 */
abstract class AHandler implements IHandler {

    /**
     * Selects the next possible length between {@code minLength} and
     * {@code maxLength}.
     * 
     * @param minLength The minimum length
     * @param maxLength The maximum length
     * @param choices   The sequence of choices
     * @return The selected length, or -1 if there is no remaining value in the
     *         choice.
     */
    protected int length(final int minLength, final int maxLength, final ChoicesSequence choices) {
        if (choices.hasNextChoiceInExploration()) {
            Choice choiceInRun = choices.getNextChoiceInExploration();
            if (choices.hasUnseenValueFurtherInExploration()) {
                return minLength + choiceInRun.currentValue();
            }
            choices.removeAllChoicesAfterCurrentChoiceInExploration();

            if (choiceInRun.hasNextValue()) {
                return minLength + choiceInRun.nextValue();
            } else {
                choices.popLastChoice();
                return -1;
            }
        } else {
            Choice choice = choices.createNewChoice(maxLength - minLength + 1, true);
            return minLength + choice.nextValue();
        }
    }
}
