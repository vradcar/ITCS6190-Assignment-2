package reducer;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;
import java.util.*;

public class DocumentSimilarityReducer extends Reducer<Text, Text, Text, Text> {
    /**
     * For each key, receives values in the format 'docId:word1,word2,...'.
     * Computes Jaccard similarity for each unique document pair and outputs in a standardized, readable format.
     */
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        // Store document IDs and their word sets
        List<String> docIds = new ArrayList<>();
        List<Set<String>> docWordSets = new ArrayList<>();

        for (Text val : values) {
            String[] parts = val.toString().split(":", 2);
            if (parts.length != 2) continue; // Skip malformed input
            String docId = parts[0].trim();
            Set<String> words = new HashSet<>();
            for (String word : parts[1].split(",")) {
                String w = word.trim();
                if (!w.isEmpty()) words.add(w);
            }
            docIds.add(docId);
            docWordSets.add(words);
        }

        // Compute Jaccard Similarity for each unique pair
        int n = docIds.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Set<String> setA = docWordSets.get(i);
                Set<String> setB = docWordSets.get(j);
                Set<String> intersection = new HashSet<>(setA);
                intersection.retainAll(setB);
                Set<String> union = new HashSet<>(setA);
                union.addAll(setB);
                double similarity = union.size() == 0 ? 0.0 : (double) intersection.size() / union.size();
                // Standardized output: docId1,docId2\tSimilarity: x.xx
                String outKey = docIds.get(i) + "," + docIds.get(j);
                String outVal = String.format("Similarity: %.2f", similarity);
                context.write(new Text(outKey), new Text(outVal));
            }
        }
    }
}
