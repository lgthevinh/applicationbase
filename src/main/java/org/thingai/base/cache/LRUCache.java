package org.thingai.base.cache;

import java.util.Map;

public class LRUCache<K, V> {
    static class CacheNode<K, V> {
        K key;
        V value;
        CacheNode<K, V> prev;
        CacheNode<K, V> next;
    }

    private final int maxCacheSize;
    private final Map<K, CacheNode<K, V>> cacheMap;
    private final CacheNode<K, V> head;
    private final CacheNode<K, V> tail;

    public LRUCache(int maxCacheSize, Map<K, CacheNode<K, V>> cacheMap) {
        this.maxCacheSize = maxCacheSize;
        this.cacheMap = cacheMap;

        // Init linked list with dummy head and tail
        this.head = new CacheNode<>();
        this.tail = new CacheNode<>();
        this.head.next = tail;
        this.tail.prev = head;
    }

    public V get(K key) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }
        CacheNode<K, V> node = cacheMap.get(key);
        removeNode(node);
        addToHead(node);
        return node.value;
    }

    public void put(K key, V value) {
        if (cacheMap.containsKey(key)) {
            CacheNode<K, V> node = cacheMap.get(key);
            node.value = value;
            removeNode(node);
            addToHead(node);
        } else {
            if (cacheMap.size() >= maxCacheSize) {
                // Remove least recently used node
                CacheNode<K, V> leastUseNode = tail.prev;
                removeNode(leastUseNode);
                cacheMap.remove(leastUseNode.key);
            }
            CacheNode<K, V> newNode = new CacheNode<>();
            newNode.key = key;
            newNode.value = value;
            cacheMap.put(key, newNode);
            addToHead(newNode);
        }
    }

    private void addToHead(CacheNode<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(CacheNode<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
