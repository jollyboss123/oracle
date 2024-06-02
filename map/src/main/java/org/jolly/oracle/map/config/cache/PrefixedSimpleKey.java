package org.jolly.oracle.map.config.cache;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

public class PrefixedSimpleKey implements Serializable {
    private final String prefix;
    private final transient Object[] params;
    private final String methodName;
    private int hashCodeValue;

    public PrefixedSimpleKey(String prefix, String methodName, Object... elements) {
        Assert.notNull(prefix, "Prefix must not be null");
        Assert.notNull(elements, "Elements must not be null");
        this.prefix = prefix;
        this.methodName = methodName;
        this.params = new Object[elements.length];
        System.arraycopy(elements, 0, this.params, 0, elements.length);
        this.hashCodeValue = prefix.hashCode();
        this.hashCodeValue = 31 * this.hashCodeValue + methodName.hashCode();
        this.hashCodeValue = 31 * this.hashCodeValue + Arrays.deepHashCode(this.params);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PrefixedSimpleKey psk)) {
            return false;
        }

        return this.prefix.equals(psk.prefix) &&
                this.methodName.equals(psk.methodName) &&
                Arrays.deepEquals(this.params, psk.params);
    }

    @Override
    public final int hashCode() {
        return this.hashCodeValue;
    }

    @Override
    public String toString() {
        return this.prefix + " " + this.getClass().getSimpleName() + this.methodName + " [" + StringUtils.arrayToCommaDelimitedString(this.params) + "]";
    }
}
