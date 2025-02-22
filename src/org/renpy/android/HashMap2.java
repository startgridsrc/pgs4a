package org.renpy.android;

import java.util.HashMap;

public class HashMap2<K, V> extends HashMap<K, V> {
    // Default constructor
    public HashMap2() {
        super(); // Call the parent HashMap constructor
    }

    public void Put(K key, V value) {
        this.put(key, value); // Call the parent put method
    }

}
