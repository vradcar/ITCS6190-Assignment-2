import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;
import java.util.*;

public class DocumentSimilarityReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        // Collect all documents and their word sets
        List<String> docIds = new ArrayList<>();
        List<Set<String>> docWordSets = new ArrayList<>();
        for (Text val : values) {
            String[] parts = val.toString().split(":", 2);
            if (parts.length != 2) continue;
            String docId = parts[0];
            Set<String> words = new HashSet<>(Arrays.asList(parts[1].split(",")));
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
                String outKey = docIds.get(i) + ", " + docIds.get(j);
                String outVal = String.format("Similarity: %.2f", similarity);
                context.write(new Text(outKey), new Text(outVal));
            }
        }
    }
}
