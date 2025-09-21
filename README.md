# Assignment 2: Document Similarity using MapReduce

**Name:** 

**Student ID:** 

## Approach and Implementation

### Mapper Design
[Explain the logic of your Mapper class. What is its input key-value pair? What does it emit as its output key-value pair? How does it help in solving the overall problem?]

### Reducer Design
[Explain the logic of your Reducer class. What is its input key-value pair? How does it process the values for a given key? What does it emit as the final output? How do you calculate the Jaccard Similarity here?]

### Overall Data Flow
[Describe how data flows from the initial input files, through the Mapper, shuffle/sort phase, and the Reducer to produce the final output.]

---

## Setup and Execution

### 1. **Start the Hadoop Cluster**

Run the following command to start the Hadoop cluster:

```bash
docker compose up -d
```

### 2. **Build the Code**

Build the code using Maven:

```bash
mvn clean package
```

### 3. **Copy JAR to Docker Container**

Copy the JAR file to the Hadoop ResourceManager container (update the JAR name if your version is different):

```bash
docker cp target/DocumentSimilarity-0.0.1-SNAPSHOT.jar resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 4. **Move Dataset to Docker Container**

Copy your dataset file (e.g., `input.txt`) to the Hadoop ResourceManager container:

```bash
docker cp input.txt resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 5. **Connect to Docker Container**

Access the Hadoop ResourceManager container:

```bash
docker exec -it resourcemanager /bin/bash
```

### 6. **Set Up HDFS**

Create a folder in HDFS for the input dataset:

```bash
hadoop fs -mkdir -p /input/data
```

Copy the input dataset to the HDFS folder:

```bash
cd /opt/hadoop-3.2.1/share/hadoop/mapreduce/
hadoop fs -put input.txt /input/data
```

### 7. **Execute the MapReduce Job**

Run your MapReduce job (update the main class path if needed):

```bash
hadoop jar /opt/hadoop-3.2.1/share/hadoop/mapreduce/DocumentSimilarity-0.0.1-SNAPSHOT.jar controller.DocumentSimilarityDriver /input/data/input.txt /output1
```

### 8. **View the Output**

To view the output of your MapReduce job, use:

```bash
hadoop fs -cat /output1/*
```

### 9. **Copy Output from HDFS to Local OS**

To copy the output from HDFS to your local machine:

1. Use the following command to copy from HDFS:
    ```bash
    hdfs dfs -get /output1 /opt/hadoop-3.2.1/share/hadoop/mapreduce/
    ```

2. Use Docker to copy from the container to your local machine:
    ```bash
    exit
    docker cp resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/output1/ shared-folder/output/
    ```

3. Commit and push to your repo so that we can see your output.


## Challenges and Solutions

[Describe any challenges you faced during this assignment. This could be related to the algorithm design (e.g., how to generate pairs), implementation details (e.g., data structures, debugging in Hadoop), or environmental issues. Explain how you overcame these challenges.]

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
