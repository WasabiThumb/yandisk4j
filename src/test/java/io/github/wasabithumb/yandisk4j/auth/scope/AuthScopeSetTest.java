package io.github.wasabithumb.yandisk4j.auth.scope;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AuthScopeSetTest {

    @Test
    void named() {
        Set<AuthScope> set = new AuthScopeSet();
        List<AuthScope> list = new LinkedList<>();

        add(set, list, AuthScope.WRITE);
        add(set, list, AuthScope.INFO);
        add(set, list, AuthScope.READ);

        assertTrue(set.contains(AuthScope.READ));
        assertTrue(set.contains(AuthScope.WRITE));
        assertTrue(set.contains(AuthScope.INFO));
        assertFalse(set.contains(AuthScope.APP_FOLDER));
        assertFalse(set.contains(AuthScope.of("foo")));
        assertFalse(set.contains(AuthScope.of("bar")));
        assertFalse(set.contains(AuthScope.of("baz")));
        assertFalse(set.contains(AuthScope.of("bad")));

        check(set, list);
    }

    @Test
    void custom() {
        Set<AuthScope> set = new AuthScopeSet();
        List<AuthScope> list = new LinkedList<>();

        add(set, list, AuthScope.of("foo"));
        add(set, list, AuthScope.of("bar"));
        add(set, list, AuthScope.of("baz"));

        assertFalse(set.contains(AuthScope.READ));
        assertFalse(set.contains(AuthScope.WRITE));
        assertFalse(set.contains(AuthScope.INFO));
        assertFalse(set.contains(AuthScope.APP_FOLDER));
        assertTrue(set.contains(AuthScope.of("foo")));
        assertTrue(set.contains(AuthScope.of("bar")));
        assertTrue(set.contains(AuthScope.of("baz")));
        assertFalse(set.contains(AuthScope.of("bad")));

        check(set, list);
    }

    @Test
    void mixed() {
        Set<AuthScope> set = new AuthScopeSet();
        List<AuthScope> list = new LinkedList<>();

        add(set, list, AuthScope.WRITE);
        add(set, list, AuthScope.INFO);
        add(set, list, AuthScope.READ);

        add(set, list, AuthScope.of("foo"));
        add(set, list, AuthScope.of("bar"));
        add(set, list, AuthScope.of("baz"));

        assertTrue(set.contains(AuthScope.READ));
        assertTrue(set.contains(AuthScope.WRITE));
        assertTrue(set.contains(AuthScope.INFO));
        assertFalse(set.contains(AuthScope.APP_FOLDER));
        assertTrue(set.contains(AuthScope.of("foo")));
        assertTrue(set.contains(AuthScope.of("bar")));
        assertTrue(set.contains(AuthScope.of("baz")));
        assertFalse(set.contains(AuthScope.of("bad")));

        check(set, list);
    }

    private void check(Set<AuthScope> set, List<AuthScope> list) {
        assertEquals(list.size(), set.size());

        Comparator<AuthScope> cmp = Comparator.comparing(AuthScope::token);
        List<AuthScope> cpy = new ArrayList<>(set);
        cpy.sort(cmp);
        list.sort(cmp);

        assertEquals(list.size(), cpy.size());

        for (int i=0; i < list.size(); i++) {
            assertEquals(list.get(i), cpy.get(i));
        }
    }

    private void add(Set<AuthScope> set, List<AuthScope> list, AuthScope scope) {
        set.add(scope);
        list.add(scope);
    }

}