package io.github.wasabithumb.yandisk4j.auth.scope;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An efficient {@link Set} to contain {@link AuthScope}s
 */
public class AuthScopeSet extends AbstractSet<AuthScope> {

    private Set<?> backing = EnumSet.noneOf(NamedAuthScope.class);
    private boolean simple = true;

    //

    @SuppressWarnings("unchecked")
    private @NotNull Set<NamedAuthScope> named() {
        assert this.simple;
        return (Set<NamedAuthScope>) this.backing;
    }

    @SuppressWarnings("unchecked")
    private @NotNull Set<String> custom() {
        assert !this.simple;
        return (Set<String>) this.backing;
    }

    //

    @Override
    public @NotNull Iterator<AuthScope> iterator() {
        if (this.simple) {
            return this.named()
                    .stream()
                    .map(AuthScope.class::cast)
                    .iterator();
        } else {
            return this.custom()
                    .stream()
                    .map(AuthScope::of)
                    .iterator();
        }
    }

    @Override
    public int size() {
        return this.backing.size();
    }

    @Override
    public void clear() {
        this.backing.clear();
    }

    @Override
    @Contract("null -> fail")
    public boolean add(AuthScope scope) {
        if (scope == null)
            throw new NullPointerException("Cannot add null to AuthScopeSet");
        if (this.simple) {
            final Set<NamedAuthScope> named = this.named();
            if (scope.isNamed()) {
                return named.add((NamedAuthScope) scope);
            } else {
                Set<String> copy = new HashSet<>(named.size());
                for (NamedAuthScope existing : named)
                    copy.add(existing.token());
                this.backing = copy;
                this.simple = false;
                return copy.add(scope.token());
            }
        } else {
            return this.custom().add(scope.token());
        }
    }

    @Override
    @Contract("null -> false")
    public boolean remove(Object o) {
        if (!(o instanceof AuthScope scope)) return false;
        if (this.simple) {
            if (!scope.isNamed()) return false;
            return this.backing.remove(scope);
        } else {
            return this.custom().remove(scope.token());
        }
    }

    @Override
    @Contract("null -> false")
    public boolean contains(Object o) {
        if (!(o instanceof AuthScope scope)) return false;
        if (this.simple) {
            if (!scope.isNamed()) return false;
            return this.backing.contains(scope);
        } else {
            return this.custom().contains(scope.token());
        }
    }

}
