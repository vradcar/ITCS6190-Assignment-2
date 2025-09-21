package mapper;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DocumentSimilarityMapper extends Mapper<LongWritable, Text, Text, Text> {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        if (line.isEmpty()) return;
        String[] tokens = line.split("\\s+", 2);
        if (tokens.length < 2) return;
        String docId = tokens[0];
        String text = tokens[1].replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
        String[] words = text.split("\\s+");
        Set<String> uniqueWords = new HashSet<>();
        for (String word : words) {
            if (!word.isEmpty()) uniqueWords.add(word);
        }
        // Emit: key = "DOC", value = docId:word1,word2,...
        StringBuilder sb = new StringBuilder();
        sb.append(docId).append(":");
        boolean first = true;
        for (String word : uniqueWords) {
            if (!first) sb.append(",");
            sb.append(word);
            first = false;
        }
        context.write(new Text("DOC"), new Text(sb.toString()));
    }
}
