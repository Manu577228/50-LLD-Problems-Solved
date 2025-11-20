/* ----------------------------------------------------------------------------  */
/*   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
 */
/* --------------------------------------------------------------------------   */
/*    Youtube: https://youtube.com/@code-with-Bharadwaj                        */
/*    Github : https://github.com/Manu577228                                  */
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio       */
/* -----------------------------------------------------------------------  */

import java.io.*;
import java.util.*;
import java.util.regex.*;

/*
===============================================
1) REQUIREMENTS
===============================================
a) Functional Requirements:
   - The system indexes multiple text documents.
   - It tokenizes and builds an inverted index (word → list of document IDs).
   - Supports quick keyword search returning all matching documents.
   - Should show results instantly from memory.

b) Non-Functional Requirements:
   - Runs without database (pure Java in-memory).
   - Lightweight, single-threaded, easily extendable.
   - Lookup O(1) on average due to HashMap.
*/

/*
===============================================
2) ALGORITHM CHOICE DISCUSSION
===============================================
Algorithm: Inverted Index
   - Each word is mapped to a list of document IDs.
   - Lookup is O(1) average via HashMap.
   - Space complexity O(N * M) where N=docs, M=unique words per doc.
*/

/*
===============================================
3) CONCURRENCY & DATA MODEL DISCUSSION
===============================================
Data Model:
   - documents : Map<Integer, String>
   - index     : Map<String, Set<Integer>>

Concurrency:
   - Single-threaded for demo simplicity.
   - Can later use ConcurrentHashMap and locks for thread safety.
*/

/*
===============================================
4) UML DIAGRAM (TEXTUAL)
===============================================

         ┌────────────────────┐
         │   SearchIndexer    │
         ├────────────────────┤
         │ - documents : Map  │
         │ - index : Map      │
         ├────────────────────┤
         │ + addDocument()    │
         │ + buildIndex()     │
         │ + search()         │
         └────────────────────┘
*/

public class SearchIndexer {

    private final Map<Integer, String> documents; // stores docId -> content
    private final Map<String, Set<Integer>> index; // inverted index: word -> docIds

    public SearchIndexer() {
        this.documents = new HashMap<>();
        this.index = new HashMap<>();
    }

    // Adds a document to memory
    public void addDocument(int id, String content) {
        documents.put(id, content);
    }

    // Tokenizes and builds inverted index
    public void buildIndex() {
        Pattern pattern = Pattern.compile("\\w+"); // regex for words
        for (Map.Entry<Integer, String> entry : documents.entrySet()) {
            int docId = entry.getKey();
            String text = entry.getValue().toLowerCase();
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String word = matcher.group();
                index.computeIfAbsent(word, k -> new HashSet<>()).add(docId);
            }
        }
    }

    // Searches and returns document IDs containing the given keyword
    public List<Integer> search(String keyword) {
        keyword = keyword.toLowerCase();
        Set<Integer> result = index.getOrDefault(keyword, new HashSet<>());
        return new ArrayList<>(result);
    }

    /*
    ===============================================
    5) IMPLEMENTATION + EXPLANATION
    ===============================================
    - addDocument()  : store documents in map
    - buildIndex()   : iterate all docs → extract words → fill map
    - search()       : return list of doc IDs having that keyword
    */

    public static void main(String[] args) throws IOException {
        SearchIndexer s = new SearchIndexer();

        // Step 1: Add some demo documents
        s.addDocument(1, "The cat sat on the mat");
        s.addDocument(2, "The dog chased the cat");
        s.addDocument(3, "The bird sang sweetly");

        // Step 2: Build inverted index
        s.buildIndex();

        // Step 3: Search and display results
        System.out.println("Search Results for 'cat': " + s.search("cat"));
        System.out.println("Search Results for 'dog': " + s.search("dog"));
        System.out.println("Search Results for 'bird': " + s.search("bird"));
        System.out.println("Search Results for 'lion': " + s.search("lion"));
    }
}

/*
===============================================
EXPLANATION OF OUTPUT:
===============================================
Search Results for 'cat': [1, 2]  → 'cat' in doc 1 & 2
Search Results for 'dog': [2]     → 'dog' in doc 2 only
Search Results for 'bird': [3]    → 'bird' in doc 3 only
Search Results for 'lion': []     → not found
*/

/*
===============================================
6) LIMITATIONS OF CURRENT CODE
===============================================
- No ranking (TF-IDF) implemented.
- No partial/fuzzy matching.
- No phrase search (exact sentence).
- Index is volatile (in-memory only).
*/

/*
===============================================
7) ALTERNATIVE ALGORITHMS & TRADE-OFFS
===============================================
a) Trie-based Index:
   + Supports prefix search.
   - Higher memory usage.

b) TF-IDF Ranking:
   + Provides ranked search results.
   - Requires more computation & pre-processing.

c) External Search Engines (Elasticsearch, Whoosh):
   + Full-featured indexing and ranking.
   - Heavy setup and dependencies (not pure Java).

Future Scope:
   - Add ranking system (TF-IDF).
   - Implement multithreaded indexing.
   - Add persistent file-based storage.
*/
