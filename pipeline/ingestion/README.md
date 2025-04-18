# Spanner Graph Data Ingestion Pipeline

## org.datacommons.ingestion.pipeline.SimpleGraphPipeline

This module loads nodes and edges from a single import group into Spanner.

It is a simple pipeline that creates mutation groups (by subject ID) from each cache row
and writes them to spanner.

Example usage:

```shell
mvn -Pdataflow-runner compile exec:java -pl ingestion -am -Dexec.mainClass=org.datacommons.ingestion.pipeline.SimpleGraphPipeline \
-Dexec.args="--importGroup=biomedical --spannerNodeTableName=NodeNoFK --spannerEdgeTableName=EdgeNoFK --project=datcom-store --gcpTempLocation=gs://keyurs-dataflow/temp --runner=DataflowRunner --region=us-east1  --numWorkers=5 --dataflowServiceOptions=enable_google_cloud_profiler --workerMachineType=n2-highmem-8"
```

## org.datacommons.IngestionPipeline

This module implements a Dataflow pipeline that loads Spanner DB with StatVar observations and graph reading from GCS cache. The Pipeline can be run in local mode for testing (DirectRunner) or in the cloud (DataflowRunner).

```shell
mvn -Pdataflow-runner compile exec:java -pl ingestion -am -Dexec.mainClass=org.datacommons.IngestionPipeline -Dexec.args="--project=<project-id> --gcpTempLocation=<gs://path> --runner=DataflowRunner --region=<region>  --projectId=<project-id> --spannerInstanceId=<instance-id> --spannerDatabaseId=<database-id> --cacheType=<observation/graph> --importGroupList=auto1d,auto1w,auto2w --storageBucketId=<bucket-id>"
```
