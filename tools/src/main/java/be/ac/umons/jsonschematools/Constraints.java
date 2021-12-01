package be.ac.umons.jsonschematools;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Constraints {
    private final Map<String, Set<Object>> constraints = new HashMap<>();

    public void addConstraint(String key, Object value) {
        if (!constraints.containsKey(key)) {
            constraints.put(key, new HashSet<>());
        }
        constraints.get(key).add(value);
    }

    @SuppressWarnings("unchecked")
    public <T> Set<T> getConstraints(String key) {
        Set<T> set = new HashSet<>();
        constraints.getOrDefault(key, Collections.emptySet()).forEach(o -> set.add((T) o));
        return set;
    }

    public Set<Type> getAllowedTypes() {
        Set<Type> types = getConstraints("types").stream().
            map(str -> Type.valueOf((String)str)).
            collect(Collectors.toSet())
        ;
        if (types.isEmpty()) {
            return EnumSet.allOf(Type.class);
        }
        return types;
    }

    public Set<String> keys() {
        return constraints.keySet();
    }
}
