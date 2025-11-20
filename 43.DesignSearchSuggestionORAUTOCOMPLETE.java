import java.util.*;

// ------------------------------------------------------------
// AUTOCOMPLETE LLD - JAVA (Single File) with Line-by-Line EXPL
// ------------------------------------------------------------
public class AutoComplete {

    // --------------------------
    // Trie Node Definition
    // --------------------------
    static class TrieNode {
        Map<Character, TrieNode> children;  // Each child node for characters
        boolean isEnd;                      // Marks end of a valid word

        TrieNode() {                        // Constructor
            children = new HashMap<>();     // Initialize child map
            isEnd = false;                  // Initially not an end node
        }
    }

    // ----------------------------------------
    // AutoCompleteSystem using Trie (Facade)
    // ----------------------------------------
    static class AutoCompleteSystem {

        private TrieNode root;              // Root of Trie

        AutoCompleteSystem() {              // Constructor
            root = new TrieNode();          // Initialize root node
        }

        // Insert a word into the Trie
        public void insert(String word) {
            TrieNode node = root;           // Start from root

            for (char ch : word.toCharArray()) {  // Iterate each character
                if (!node.children.containsKey(ch)) {    // If child missing
                    node.children.put(ch, new TrieNode()); // Create new node
                }
                node = node.children.get(ch); // Move to child node
            }
            node.isEnd = true;    // Mark completion of the word
        }

        // Collect all words after a given prefix node (DFS)
        private void collect(TrieNode node, String prefix, List<String> result) {
            if (node.isEnd) {                 // If valid word ends here
                result.add(prefix);           // Add to result list
            }

            List<Character> keys = new ArrayList<>(node.children.keySet()); 
            Collections.sort(keys);           // Sort keys for lexicographic output

            for (char ch : keys) {            // DFS into each child
                collect(node.children.get(ch), prefix + ch, result);
            }
        }

        // Suggest words based on prefix
        public List<String> suggest(String prefix) {
            TrieNode node = root;             // Start from root

            for (char ch : prefix.toCharArray()) {  // Traverse Trie
                if (!node.children.containsKey(ch)) {  // Prefix not found
                    return new ArrayList<>();          // Empty list
                }
                node = node.children.get(ch);          // Move deeper
            }

            List<String> result = new ArrayList<>();   // Store suggestions
            collect(node, prefix, result);             // DFS to collect all words
            return result;                             // Return results
        }
    }

    // ---------------------------------------
    // MAIN METHOD — RUN DEMO
    // ---------------------------------------
    public static void main(String[] args) {

        AutoCompleteSystem ac = new AutoCompleteSystem();  // Create system

        // Sample dataset of words
        String[] words = {"apple", "app", "ape", "bat", "ball", "banana"};

        for (String w : words) ac.insert(w);     // Insert into Trie

        // Print demo outputs
        System.out.println("Suggestions for 'ap' : " + ac.suggest("ap"));
        System.out.println("Suggestions for 'ba' : " + ac.suggest("ba"));
        System.out.println("Suggestions for 'app': " + ac.suggest("app"));
    }
}
