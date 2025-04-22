package com.gmail.nhtuan10;

import java.util.ArrayList;
import java.util.List;

public class BPlusTree<K extends Comparable,V> {
    int maxNodeSize;
    public  class Node<K,V> {
        protected Node<K,V> parent;
        protected List<K> keys;
        protected List<V> values;
        protected boolean isLeaf;
        protected List<Node<K,V>> children;

        public Node(Node<K,V> parent, V value, boolean isLeaf) {
            this.parent = parent;
            this.values = new ArrayList<>(maxNodeSize);
            this.values.add(value);
            this.keys = new ArrayList<>(maxNodeSize);
            this.children = new ArrayList<>(maxNodeSize+1);
            this.isLeaf = isLeaf;
        }
    }

//    public static class InnerNode<K> {
//        protected Node<K>[] children;
//    }
//
//    public  class LeafNode<K,V> extends Node<K> {
//
//        V[] values;
//        public LeafNode(K[] keys, V[] values, Node<K> parent) {
//            this.values = values;
//            this.keys = keys;
//            this.parent = parent;
//        }
//
//        public LeafNode(Node<K> parent) {
//            this.parent = parent;
//            this.values = (V[]) new Object[maxNodeSize];
//            this.keys = (K[]) new Object[maxNodeSize];
//        }
//    }

    Node<K,V> root;

    public BPlusTree() {
        maxNodeSize = 10;
    }


//    public Tree.Node<V> insert(V value) {
//        Tree.Node<V> node = new Tree.Node<>(value);
//        if (root == null) {
//            root = node;
//        } else {
//            insert(root, value);
//        }
//        return node;
//    }

//    public Tree.Node<V> insert(Tree.Node<V> root, V value) {
//        if (root == null) {
//            root = new Tree.Node<>(value);
//            return root;
//        }
//        if (value.compareTo(root.value) < 0) {
//            root.left = insert(root.left, value);
//        } else {
//            root.right = insert(root.right, value);
//        }
//        return root;
//    }

    public Node<K,V>  insert(K key, V value, Node<K,V> node, Node<K,V> child) {
        if (node == null) {
            node = new Node<>(null, value, child == null);
        }
        int i = 0;
        while (i <= node.keys.size() - 1 && key.compareTo(node.keys.get(i)) > 0) {
            i++;
        }
        node.keys.add(i, key);
        node.values.add(i, value);
        node.children.add(i+1, child);
        if (node.keys.size() > maxNodeSize) {
            int splitIndex = maxNodeSize / 2;
            Node<K,V> newNodes = new Node<K,V>(node.parent,value, node.isLeaf);
            newNodes.isLeaf = node.isLeaf;
            for (int j = 0; j < splitIndex; j++) {
                newNodes.keys.add(node.keys.remove(i));
                newNodes.values.add(node.values.remove(i));
//                newNodes.keys[i] = node.keys[splitIndex + i];
//                newNodes.values[i] = node.values[splitIndex + i];
//                node.keys[splitIndex + i] = null;
//                node.values[splitIndex + i] = null;
            }
            insert(newNodes.keys.getFirst(),newNodes.values.getFirst(),node.parent, newNodes);
        }
        return node;
    }


}
