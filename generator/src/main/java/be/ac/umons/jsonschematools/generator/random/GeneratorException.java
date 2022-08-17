package be.ac.umons.jsonschematools.generator.random;

/**
 * The exception thrown if the generator can not generate a value.
 * 
 * @author Gaëtan Staquet
 */
public class GeneratorException extends Exception {
    public GeneratorException(String message) {
        super(message);
    }
}
