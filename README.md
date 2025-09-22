# Assignment 2: Document Similarity using MapReduce

**Name:** Varad Paradkar

**Student ID:** 801418318

## Approach and Implementation

### Mapper Design
[Explain the logic of your Mapper class. What is its input key-value pair? What does it emit as its output key-value pair? How does it help in solving the overall problem?]

### Reducer Design
[Explain the logic of your Reducer class. What is its input key-value pair? How does it process the values for a given key? What does it emit as the final output? How do you calculate the Jaccard Similarity here?]

### Overall Data Flow
[Describe how data flows from the initial input files, through the Mapper, shuffle/sort phase, and the Reducer to produce the final output.]

---

## Setup and Execution

### Prerequisites
- Docker and Docker Compose installed
- Maven installed for building the project
- Three input datasets: `input1.txt`, `input2.txt`, `input3.txt`

### 1. **Build the Project**

Build the project using Maven:

```bash
mvn clean compile package -DskipTests
```

### 2. **Start the Hadoop Cluster (3 Datanodes)**

Start the full Hadoop cluster with 3 datanodes:

```bash
docker-compose up -d
```

Wait for the cluster to initialize (approximately 30-60 seconds).

### 3. **Performance Testing with 3 Datanodes**

#### Upload Input Datasets to HDFS

```bash
# Upload input1.txt
docker exec namenode hdfs dfs -mkdir -p /input1
docker exec namenode hdfs dfs -put /shared-folder/input1.txt /input1/

# Upload input2.txt  
docker exec namenode hdfs dfs -mkdir -p /input2
docker exec namenode hdfs dfs -put /shared-folder/input2.txt /input2/

# Upload input3.txt
docker exec namenode hdfs dfs -mkdir -p /input3
docker exec namenode hdfs dfs -put /shared-folder/input3.txt /input3/
```

#### Execute MapReduce Jobs with Timing

```bash
# Test Dataset 1 (1000 words) - 3 datanodes
echo "Testing input1.txt with 3 datanodes..."
time docker exec namenode hadoop jar /shared-folder/DocumentSimilarity-0.0.1-SNAPSHOT.jar controller.DocumentSimilarityDriver /input1 /output1_3nodes

# Test Dataset 2 (3000 words) - 3 datanodes  
echo "Testing input2.txt with 3 datanodes..."
time docker exec namenode hadoop jar /shared-folder/DocumentSimilarity-0.0.1-SNAPSHOT.jar controller.DocumentSimilarityDriver /input2 /output2_3nodes

# Test Dataset 3 (5000 words) - 3 datanodes
echo "Testing input3.txt with 3 datanodes..."
time docker exec namenode hadoop jar /shared-folder/DocumentSimilarity-0.0.1-SNAPSHOT.jar controller.DocumentSimilarityDriver /input3 /output3_3nodes
```

#### Copy Results to Local Directory

```bash
# Copy outputs from HDFS to shared folder
docker exec namenode hdfs dfs -get /output1_3nodes /shared-folder/
docker exec namenode hdfs dfs -get /output2_3nodes /shared-folder/
docker exec namenode hdfs dfs -get /output3_3nodes /shared-folder/
```

### 4. **Configure Cluster for 1 Datanode**

#### Stop the Current Cluster

```bash
docker-compose down
```

#### Modify docker-compose.yml

Comment out `datanode2` and `datanode3` services in `docker-compose.yml`:

```yaml
# Comment out these sections:
# datanode2:
#   image: bde2020/hadoop-datanode:2.0.0-hadoop3.2.1-java8
#   ...
# datanode3:
#   image: bde2020/hadoop-datanode:2.0.0-hadoop3.2.1-java8
#   ...
```

#### Restart with 1 Datanode

```bash
docker-compose up -d
```

Wait for the cluster to initialize with the new configuration.

### 5. **Performance Testing with 1 Datanode**

#### Re-upload Input Datasets

```bash
# Re-upload datasets to the new cluster
docker exec namenode hdfs dfs -mkdir -p /input1
docker exec namenode hdfs dfs -put /shared-folder/input1.txt /input1/

docker exec namenode hdfs dfs -mkdir -p /input2
docker exec namenode hdfs dfs -put /shared-folder/input2.txt /input2/

docker exec namenode hdfs dfs -mkdir -p /input3
docker exec namenode hdfs dfs -put /shared-folder/input3.txt /input3/
```

#### Execute MapReduce Jobs with Timing

```bash
# Test Dataset 1 (1000 words) - 1 datanode
echo "Testing input1.txt with 1 datanode..."
time docker exec namenode hadoop jar /shared-folder/DocumentSimilarity-0.0.1-SNAPSHOT.jar controller.DocumentSimilarityDriver /input1 /output1_1node

# Test Dataset 2 (3000 words) - 1 datanode
echo "Testing input2.txt with 1 datanode..."
time docker exec namenode hadoop jar /shared-folder/DocumentSimilarity-0.0.1-SNAPSHOT.jar controller.DocumentSimilarityDriver /input2 /output2_1node

# Test Dataset 3 (5000 words) - 1 datanode
echo "Testing input3.txt with 1 datanode..."
time docker exec namenode hadoop jar /shared-folder/DocumentSimilarity-0.0.1-SNAPSHOT.jar controller.DocumentSimilarityDriver /input3 /output3_1node
```

#### Copy Results to Local Directory

```bash
# Copy outputs from HDFS to shared folder
docker exec namenode hdfs dfs -get /output1_1node /shared-folder/
docker exec namenode hdfs dfs -get /output2_1node /shared-folder/
docker exec namenode hdfs dfs -get /output3_1node /shared-folder/
```

### 6. **View and Compare Results**

#### View Output Files

```bash
# View results for 3 datanodes
Get-Content "shared-folder\output1_3nodes\part-r-00000"
Get-Content "shared-folder\output2_3nodes\part-r-00000"  
Get-Content "shared-folder\output3_3nodes\part-r-00000"

# View results for 1 datanode
Get-Content "shared-folder\output1_1node\part-r-00000"
Get-Content "shared-folder\output2_1node\part-r-00000"
Get-Content "shared-folder\output3_1node\part-r-00000"
```

### 7. **Performance Analysis Commands**

#### Record Execution Times

Create a performance log file to track results:

```bash
# Example timing format - record these manually
echo "Dataset,DatanodeCount,ExecutionTime_Seconds" > performance_results.csv
echo "input1,3,XX.XX" >> performance_results.csv
echo "input1,1,XX.XX" >> performance_results.csv
echo "input2,3,XX.XX" >> performance_results.csv  
echo "input2,1,XX.XX" >> performance_results.csv
echo "input3,3,XX.XX" >> performance_results.csv
echo "input3,1,XX.XX" >> performance_results.csv
```

#### Monitor Cluster Resources (Optional)

```bash
# Monitor cluster status during execution
docker exec namenode hdfs dfsadmin -report

# View running applications
docker exec resourcemanager yarn application -list

# Monitor container resources
docker stats
```

### 8. **Clean Up**

```bash
# Stop the cluster
docker-compose down

# Clean up Docker containers and images (optional)
docker container prune -f
docker image prune -f
```


## Performance Analysis Results

### Execution Times Comparison

| Dataset | Size (words) | 3 Datanodes (sec) | 1 Datanode (sec) | Performance Difference |
|---------|--------------|-------------------|------------------|----------------------|
| input1  | ~1,000      | [TO BE FILLED]    | [TO BE FILLED]   | [TO BE CALCULATED]   |
| input2  | ~3,000      | [TO BE FILLED]    | [TO BE FILLED]   | [TO BE CALCULATED]   |
| input3  | ~5,000      | [TO BE FILLED]    | [TO BE FILLED]   | [TO BE CALCULATED]   |

### Performance Observations

#### Scalability Analysis
- **Data Size Impact**: [Describe how execution time changes with dataset size]
- **Cluster Size Impact**: [Describe how execution time changes with number of datanodes]
- **Efficiency Metrics**: [Calculate and discuss speedup ratios]

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

[TO BE FILLED after running experiments]

1. **Performance Impact of Cluster Size**:
   - [Quantify the performance difference between 1 and 3 datanodes]

2. **Scalability with Data Size**:
   - [Analyze how execution time scales with input size]

3. **MapReduce Efficiency**:
   - [Discuss the effectiveness of the MapReduce approach for this problem]

4. **Recommendations**:
   - [Provide recommendations for optimal cluster configuration]

---
## Sample Input

**Input from `small_dataset.txt`**
```
Document1 This is a sample document containing words
Document2 Another document that also has words
Document3 Sample text with different words
```
## Sample Output

**Output from `small_dataset.txt`**
```
"Document1, Document2 Similarity: 0.56"
"Document1, Document3 Similarity: 0.42"
"Document2, Document3 Similarity: 0.50"
```
## Obtained Output: (Place your obtained output here.)
