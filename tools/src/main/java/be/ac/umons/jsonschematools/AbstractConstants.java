package be.ac.umons.jsonschematools;

/**
 * Constants used for our abstractions for strings, integers, numbers, and enum
 * values.
 * 
 * Strings are always equal to "\S", ignoring the requirements of the schema. In
 * a similar way, integers are always "\I", numbers "\D", and enum values "\E".
 * 
 * @author Gaëtan Staquet
 */
public class AbstractConstants {
    public static String stringConstant = "\\S";
    public static String integerConstant = "\\I";
    public static String numberConstant = "\\D";
    public static String enumConstant = "\\E";
}
