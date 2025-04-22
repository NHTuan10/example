package com.gmail.nhtuan10;


public class Tree<V extends Comparable> {
    Node<V> root;

    Tree() {
    }

    public static class Node<V> {
        V value;
        Node<V> left;
        Node<V> right;

        public Node(V value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "value=" + value +
                    ", left=" + left +
                    ", right=" + right +
                    '}';
        }
    }

    public Node<V> insert(Node<V> root, V value) {
        if (root == null) {
            root = new Node<>(value);
            return root;
        }
        if (value.compareTo(root.value) < 0) {
            root.left = insert(root.left, value);
        } else {
            root.right = insert(root.right, value);
        }
        return root;
    }

    public Node<V> insert(V value) {
        Node<V> node = new Node<>(value);
        if (root == null) {
            root = node;
        } else {
            insert(root, value);
        }
        return node;
    }

    public Node<V> search(Node<V> root, V value) {
        if (root == null) {
            return null;
        }
        if (value.compareTo(root.value) == 0) {
            return root;
        } else if (value.compareTo(root.value) < 0) {
            return search(root.left, value);
        } else {
            return search(root.right, value);
        }

    }

    public Node<V> remove( V value) {
        return remove(root, value,null);
    }
    public Node<V> remove(Node<V> node, V value, Node<V> parent) {
        Node<V> newNode;
        if (node == null) {
            return null;
        }
        if (value.compareTo(node.value) == 0) {
            // if leaf
            if (node.left == null && node.right == null) {
                newNode = null;
            } else if (node.left == null) {
                newNode = node.right;
            } else if (node.right == null) {
                newNode = node.left;
            } else {
                newNode = findSmallestNode(node.right, node, true);
                newNode.left = node.left;
                newNode.right = node.right;
            }
            if (parent == null) {
                root = newNode;
            } else {
                changeParentPointer(parent, node, newNode);
            }
            node.right = null;
            node.left = null;
        } else if (value.compareTo(node.value) < 0) {
            return remove(node.left, value, node);
        } else if (value.compareTo(node.value) > 0) {
            return remove(node.right, value, node);
        }
        return node;
    }

    public void changeParentPointer(Node<V> parent, Node<V> node, Node<V> newNode) {
        if (parent.left == node) {
            parent.left = newNode;
        } else if (parent.right == node) {
            parent.right = newNode;
        }
    }

    public Node<V> findSmallestNode(Node<V> node, Node<V> parent, boolean startNode) {
        if (node.left == null) {
            if (startNode) {
                parent.right = node.right;
            }
            else {
                parent.left = node.right;
            }
            return node;
        }
        return findSmallestNode(node.left, node, false);
    }

    public static void main(String[] args) {
        Tree<Integer> t = new Tree<>();
        t.insert(15);
        t.insert(75);
        t.insert(96);
        t.insert(45);
        t.insert(7);
        t.insert(5);
        t.insert(50);
        t.insert(117);
        System.out.println(t.root);
        System.out.println(t.search(t.root, 15));
        System.out.println(t.search(t.root, 45));
        System.out.println(t.search(t.root, 75));
        System.out.println(t.search(t.root, 80));
        System.out.println(t.search(t.root, 3));
        System.out.println(t.remove( 15));
        System.out.println("Tree: " + t.root);
        System.out.println(t.remove( 96));
        System.out.println("Tree: " + t.root);
        System.out.println(t.remove( 75));
        System.out.println("Tree: " + t.root);
        System.out.println(t.remove( 45));
        System.out.println("Tree: " + t.root);
        System.out.println(t.remove( 117));
        System.out.println("Tree: " + t.root);
        System.out.println(t.remove( 7));
        System.out.println("Tree: " + t.root);
        System.out.println(t.remove( 5));
        System.out.println("Tree: " + t.root);
        System.out.println(t.remove( 50));
        System.out.println("Tree: " + t.root);
    }
}
