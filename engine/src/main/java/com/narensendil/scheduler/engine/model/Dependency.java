package com.narensendil.scheduler.engine.model;

import java.util.Objects;

public final class Dependency {

    private final String prerequisite;
    private final String dependent;

    public Dependency(String prerequisite, String dependent) {
        if (prerequisite == null || prerequisite.isBlank()
                || dependent == null || dependent.isBlank()) {
            throw new IllegalArgumentException("Dependency ids cannot be blank");
        }
        if (prerequisite.equals(dependent)) {
            throw new IllegalArgumentException("Self-dependency is not allowed");
        }
        this.prerequisite = prerequisite;
        this.dependent = dependent;
    }

    public String prerequisite() { return prerequisite; }
    public String dependent() { return dependent; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Dependency d)) return false;
        return prerequisite.equals(d.prerequisite)
                && dependent.equals(d.dependent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prerequisite, dependent);
    }
}
