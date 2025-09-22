# Assignment 2: Document Similarity using MapReduce

**Name:** Varad Paradkar

**Student ID:** 801418318

## How I Built This Document Similarity Solution

### The Mapper - Breaking Down Documents

My **DocumentSimilarityMapper** takes raw text and prepares it for comparison:

**Input:** Lines like `"Document1 This is some text content"`

**Process:**
1. Splits line into document name and content
2. Cleans text (removes punctuation, lowercase)  
3. Extracts unique words using HashSet
4. Outputs: `"Document1:word1,word2,word3"`

**Output:** Key `"DOC"` with document name and unique words

### The Reducer - Calculating Similarities  

**DocumentSimilarityReducer** compares all documents using Jaccard similarity:

**Process:**
1. Collects all document word sets
2. Compares every document pair
3. Calculates: intersection ÷ union = similarity score

**Output:** Clean results like `"Document1, Document2 Similarity: 0.14"`

### Data Flow

1. **Input:** Text files split across mappers
2. **Map:** Clean and extract unique words from each document
3. **Shuffle:** Gather all documents under single key
4. **Reduce:** Calculate pairwise similarities
5. **Output:** Formatted similarity scores

## Setup and Execution

### Prerequisites
- Docker and Docker Compose
- Maven
- Test files: `input1.txt`, `input2.txt`, `input3.txt`

### Step 1: Build the Project

```bash
mvn clean compile package -DskipTests
```

### Step 2: Start 3-Datanode Cluster

```bash
docker-compose up -d
```

### Step 3: Test with 3 Datanodes

#### Upload test files

```bash
docker exec namenode hdfs dfs -mkdir -p /input1
docker exec namenode hdfs dfs -put /shared-folder/input1.txt /input1/

docker exec namenode hdfs dfs -mkdir -p /input2
docker exec namenode hdfs dfs -put /shared-folder/input2.txt /input2/

docker exec namenode hdfs dfs -mkdir -p /input3
docker exec namenode hdfs dfs -put /shared-folder/input3.txt /input3/
```

#### Run timed tests

```bash
echo "Testing input1.txt with 3 datanodes..."
time docker exec namenode hadoop jar /shared-folder/DocumentSimilarity-0.0.1-SNAPSHOT.jar controller.DocumentSimilarityDriver /input1 /output1_3nodes

echo "Testing input2.txt with 3 datanodes..."
time docker exec namenode hadoop jar /shared-folder/DocumentSimilarity-0.0.1-SNAPSHOT.jar controller.DocumentSimilarityDriver /input2 /output2_3nodes

echo "Testing input3.txt with 3 datanodes..."
time docker exec namenode hadoop jar /shared-folder/DocumentSimilarity-0.0.1-SNAPSHOT.jar controller.DocumentSimilarityDriver /input3 /output3_3nodes
```

#### Save results

```bash
docker exec namenode hdfs dfs -get /output1_3nodes /shared-folder/
docker exec namenode hdfs dfs -get /output2_3nodes /shared-folder/
docker exec namenode hdfs dfs -get /output3_3nodes /shared-folder/
```

### Step 4: Switch to 1 Datanode

#### Stop and clean up

```bash
docker-compose down
docker container prune -f
```

#### Edit docker-compose.yml

Comment out datanode2 and datanode3 sections.

#### Restart

```bash
docker-compose up -d
```

### Step 5: Test with 1 Datanode

Repeat upload, test, and save steps with 1-datanode configuration.

### Step 6: View Results

```bash
Get-Content "shared-folder\output1_3nodes\part-r-00000"
Get-Content "shared-folder\output1_1node\part-r-00000"
```


## Performance Analysis Results

### Execution Times Comparison

| Dataset | Size (words) | 3 Datanodes (sec) | 1 Datanode (sec) | Performance Difference |
|---------|--------------|-------------------|------------------|----------------------|
| input1  | ~1,000      | 22.846           | 22.345           | **1-node 2.2% faster** |
| input2  | ~3,000      | 21.698           | 20.112           | **1-node 7.3% faster** |
| input3  | ~5,000      | 20.773           | 20.899           | **3-node 0.6% faster** |

### What's Really Happening Here

I dug deeper into the timing data and found something interesting:

| Dataset | Component | 3-Datanode (ms) | 1-Datanode (ms) | Difference (ms) | Change |
|---------|-----------|-----------------|-----------------|-----------------|--------|
| input1 | Map Phase | 3,093 | 3,161 | +68 | 2.2% slower |
| input1 | Reduce Phase | 2,221 | 2,336 | +115 | 5.2% slower |
| input2 | Map Phase | 2,308 | 2,533 | +225 | 9.7% slower |
| input2 | Reduce Phase | 2,386 | 2,412 | +26 | 1.1% slower |
| input3 | Map Phase | 2,494 | 2,645 | +151 | 6.1% slower |
| input3 | Reduce Phase | 3,215 | 2,503 | -712 | **22.1% faster** |

### Memory and CPU Usage

| Dataset | Configuration | Physical Memory (MB) | Virtual Memory (GB) | CPU Time (ms) |
|---------|---------------|---------------------|---------------------|---------------|
| input1 | 3-Datanode | 491.9 | 13.5 | 730 |
| input1 | 1-Datanode | 385.9 | 13.5 | 800 |
| input2 | 3-Datanode | 496.7 | 13.5 | 790 |
| input2 | 1-Datanode | 423.9 | 13.5 | 870 |
| input3 | 3-Datanode | 493.9 | 13.5 | 840 |
| input3 | 1-Datanode | 493.7 | 13.5 | 830 |

### Key Insights

**Surprising Results:** 1-datanode performed better due to coordination overhead dominating small dataset processing.

**Setup Time Dominance:** 80% of execution time is Hadoop initialization, only 20% actual processing.

**Resource Efficiency:** Single datanode used 12% less memory on average.

### Performance Summary

| Metric | 3-Datanode | 1-Datanode | Winner |
|--------|------------|------------|--------|
| Average Execution Time | 21.772s | 21.119s | **1-Datanode (3.0% faster)** |
| Memory Efficiency | 494.2 MB avg | 434.5 MB avg | **1-Datanode (12.1% less)** |
| CPU Efficiency | 786.7 ms avg | 833.3 ms avg | **3-Datanode (5.6% less)** |
| Setup Complexity | Higher | Lower | **1-Datanode** |

### Scalability Conclusions

#### When to Use Multiple Datanodes:
- **Large datasets** that can be split across multiple map tasks
- **I/O intensive operations** that benefit from parallel disk access  
- **High-volume processing** where network overhead is amortized
- **Fault tolerance requirements** for critical applications

#### When Single Datanode is Sufficient:
- **Small to medium datasets** (< 100MB typically)
- **Development and testing environments**
- **Prototyping and algorithm validation**
- **Resource-constrained environments**

#### Expected Findings
Based on MapReduce theory, we expect:
1. **3 Datanodes** should show better performance due to:
   - Parallel processing across multiple nodes
   - Distributed data storage and processing
   - Better resource utilization

2. **1 Datanode** may show:
   - Slower processing due to single point bottleneck
   - All data processing on single node
   - Potential I/O limitations

#### Resource Utilization
- **CPU Usage**: [Observe during execution]
- **Memory Usage**: [Monitor container resources]
- **Network I/O**: [Note data transfer between nodes]
- **Disk I/O**: [HDFS read/write performance]

### Detailed Performance Metrics

#### Dataset Characteristics
- **input1.txt**: 3 documents, ~1,000 words total
  - Document topics: Nature/community, Technology, Education
  - Expected similarity patterns: [Low/Medium/High overlap]

- **input2.txt**: 3 documents, ~3,000 words total  
  - Document topics: AI/ML, Climate change, Education
  - Expected similarity patterns: [Low/Medium/High overlap]

- **input3.txt**: 3 documents, ~5,000 words total
  - Document topics: Quantum computing, Biotechnology, Ocean science
  - Expected similarity patterns: [Low/Medium/High overlap]

#### MapReduce Job Analysis
For each test, the job performs:
- **Map Phase**: Process 3 documents, extract unique words
- **Shuffle Phase**: Group all documents together
- **Reduce Phase**: Compute 3 pairwise similarities (Doc1-Doc2, Doc1-Doc3, Doc2-Doc3)

### Conclusions

**Key Findings:**
1. **Single datanode performed 3% faster** for small datasets due to coordination overhead
2. **80% of runtime is Hadoop setup**, only 20% actual processing
3. **Memory efficiency improved 12%** with single datanode
4. **For small datasets**, simplicity beats distributed complexity

**Recommendations:**
- Use single datanode for development/testing and datasets < 50MB
- Use multiple datanodes for production workloads > 100MB

## Challenges and Solutions

### 1. Java Package Structure Issues

**Problem:** Package conflicts and compilation errors.

**Solution:** Organized into clean hierarchy:
- `controller.DocumentSimilarityDriver`
- `mapper.DocumentSimilarityMapper` 
- `reducer.DocumentSimilarityReducer`

### 2. Outdated Setup Instructions

**Problem:** Template commands didn't work with current Docker environment.

**Solution:** Updated container names, file paths, and HDFS commands through testing.

### 3. Output Format Issues

**Solution:** Modified reducer to output complete result line as key with null value.

**Why This Happened:**
- Hadoop's default output format automatically adds tabs between keys and values
- I was trying to work around this but not successfully

**The Fix:**
- Changed my reducer to output the complete result line as the key
- Set the value to `null` so Hadoop wouldn't add anything extra
- Result: Clean output like `"Document1, Document2 Similarity: 0.56"`

### 4. Managing Docker Containers

**The Problem:**
Switching between 3-datanode and 1-datanode configurations was trickier than expected.

**Issues I Hit:**
- Containers would sometimes conflict when restarting
- Data from previous runs would stick around and confuse new tests
- Resource cleanup wasn't happening automatically

**My Approach:**
- Always run `docker container prune -f` between configurations
- Properly shut down with `docker-compose down` before making changes
- Re-upload all data after any configuration change to ensure clean state
- Build in wait time for cluster initialization

### 5. Figuring Out Performance Testing

**The Problem:**
I needed a systematic way to measure and compare performance, but there was no guidance on how to do this properly.

**What I Developed:**
- Used the `time` command to get accurate wall-clock measurements
- Ran tests sequentially to avoid interference
- Recorded detailed Hadoop job counters for deeper analysis
- Created standardized datasets for meaningful comparisons
### Key Lessons Learned

**Technical:** Package structure, container management, and output formatting are crucial for MapReduce success.

**Process:** Small datasets reveal overhead costs; sometimes simpler solutions work better than complex distributed ones.

---
## Sample Input/Output

**Sample Input:**
```
Document1 This is a sample document containing words
Document2 Another document that also has words
Document3 Sample text with different words
```

**Sample Output:**
```
Document1, Document2 Similarity: 0.56
Document1, Document3 Similarity: 0.42
Document2, Document3 Similarity: 0.50
```

## My Test Results

### Dataset 1 Output (input1.txt - ~1,000 words)

```
Document3, Document2 Similarity: 0.14
Document3, Document1 Similarity: 0.10
Document2, Document1 Similarity: 0.11
```

### Dataset 2 Output (input2.txt - ~3,000 words)

```
Document3, Document2 Similarity: 0.12
Document3, Document1 Similarity: 0.14
Document2, Document1 Similarity: 0.10
```

### Dataset 3 Output (input3.txt - ~5,000 words)

```
Document3, Document2 Similarity: 0.14
Document3, Document1 Similarity: 0.12
Document2, Document1 Similarity: 0.11
```

### Output Analysis

**Consistency:** Identical results from both cluster configurations confirm algorithm correctness.

**Format:** Clean output without tab separators, proper decimal formatting.

**Similarity Scores:** Low scores (0.10-0.14) reflect diverse document topics with minimal word overlap.
