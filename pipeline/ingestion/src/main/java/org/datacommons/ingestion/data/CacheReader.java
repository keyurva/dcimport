package org.datacommons.ingestion.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.datacommons.proto.CacheData.EntityInfo;
import org.datacommons.proto.CacheData.PagedEntities;
import org.datacommons.proto.ChartStoreOuterClass.ChartStore;
import org.datacommons.proto.ChartStoreOuterClass.ObsTimeSeries.SourceSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;

/**
 * CacheReader is a class that has utility methods to read data from BT cache.
 */
public class CacheReader implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheReader.class);
    private static final String OUT_ARC_CACHE_PREFIX = "d/m/";
    private static final String IN_ARC_CACHE_PREFIX = "d/l/";
    private static final String OBS_TIME_SERIES_CACHE_PREFIX = "d/3/";
    private static final int CACHE_KEY_PREFIX_SIZE = 4;
    private static final String CACHE_KEY_VALUE_SEPARATOR_REGEX = ",";
    private static final String CACHE_KEY_SEPARATOR_REGEX = "\\^";

    private final List<String> skipPredicatePrefixes;

    public CacheReader(List<String> skipPredicatePrefixes) {
        this.skipPredicatePrefixes = skipPredicatePrefixes;
    }

    /**
     * Returns the GCS cache path for the import group.
     */
    public static String getCachePath(String projectId, String bucketId, String importGroup) {
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        Page<Blob> blobs = storage.list(
                bucketId,
                Storage.BlobListOption.prefix(importGroup),
                Storage.BlobListOption.currentDirectory());

        // Currently this fetches the "last" blob in the directory which typically tends
        // to be the latest.
        // TODO: Update this code to fetch a specific blob and update the logic to
        // definitively fetch the latest if none is provided.
        String prefix = "";
        for (Blob blob : blobs.iterateAll()) {
            prefix = blob.getName();
        }
        if (prefix.isEmpty()) {
            throw new RuntimeException(String.format("No blobs found for import group: %s", importGroup));
        }
        return String.format("gs://%s/%scache.csv*", bucketId, prefix);
    }

    /**
     * Parses an arc cache row to extract nodes and edges.
     */
    public NodesEdges parseArcRow(String row) {
        NodesEdges result = new NodesEdges();

        // Cache format: <dcid^predicate^type^page>, PagedEntities
        if (row != null && !row.isEmpty()) {
            String[] items = row.split(CACHE_KEY_VALUE_SEPARATOR_REGEX);
            if (items.length == 2) {
                String key = items[0];
                String value = items[1];
                String suffix = key.substring(CACHE_KEY_PREFIX_SIZE);
                String[] keys = suffix.split(CACHE_KEY_SEPARATOR_REGEX);
                if (!(value.isEmpty()) && keys.length >= 2) {
                    String dcid = keys[0];
                    String predicate = keys[1];
                    String typeOf = keys[2];

                    // Skip predicates that are in the skip set.
                    if (skipPredicate(predicate)) {
                        LOGGER.info("Skipping predicate: {}", predicate);
                        return result;
                    }

                    PagedEntities elist = parseProto(value, PagedEntities.parser());
                    for (EntityInfo entity : elist.getEntitiesList()) {
                        String subjectId, objectId, nodeId = "";
                        // Add a self edge if value is populated.
                        if (!entity.getValue().isEmpty()) {
                            subjectId = dcid;
                            objectId = dcid;
                            // Terminal edges won't produce any object nodes.
                        } else {
                            if (isOutArcCacheRow(row)) {
                                subjectId = dcid;
                                objectId = entity.getDcid();
                                nodeId = entity.getDcid();
                            } else { // in arc row
                                subjectId = entity.getDcid();
                                objectId = dcid;
                                nodeId = entity.getDcid();
                            }
                        }

                        List<String> types = entity.getTypesList();
                        if (types.isEmpty() && !typeOf.isEmpty()) {
                            types = Arrays.asList(typeOf);
                        }

                        // Add node.
                        if (!nodeId.isEmpty() && !types.isEmpty()) {
                            result.addNode(Node.builder()
                                    .subjectId(nodeId)
                                    .name(entity.getName())
                                    .types(types)
                                    .build());
                        }

                        // Add edge.
                        if (!subjectId.isEmpty() && !objectId.isEmpty()) {
                            result.addEdge(Edge.builder()
                                    .subjectId(subjectId)
                                    .predicate(predicate)
                                    .objectId(objectId)
                                    .objectValue(entity.getValue())
                                    .provenance(entity.getProvenanceId())
                                    .build());
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Parses a time series cache row to extract observations.
     */
    public List<Observation> parseTimeSeriesRow(String row) {
        List<Observation> result = new ArrayList<>();

        // Cache format: <placeId^statVarId>, ChartStore containing ObsTimeSeries.
        if (row != null && !row.isEmpty()) {
            String[] items = row.split(CACHE_KEY_VALUE_SEPARATOR_REGEX);
            if (items.length == 2) {
                String key = items[0];
                String value = items[1];
                String suffix = key.substring(CACHE_KEY_PREFIX_SIZE);
                String[] keys = suffix.split(CACHE_KEY_SEPARATOR_REGEX);
                if (!(value.isEmpty()) && keys.length >= 2) {
                    String variableMeasured = keys[1];
                    String observationAbout = keys[0];
                    ChartStore chart = parseProto(value, ChartStore.parser());
                    for (SourceSeries source : chart.getObsTimeSeries().getSourceSeriesList()) {
                        Observation.Builder builder = Observation.builder()
                                .variableMeasured(variableMeasured)
                                .observationAbout(observationAbout)
                                .observationPeriod(source.getObservationPeriod())
                                .measurementMethod(source.getMeasurementMethod())
                                .scalingFactor(source.getScalingFactor())
                                .unit(source.getUnit())
                                .provenance(source.getProvenanceDomain())
                                .provenanceUrl(source.getProvenanceUrl())
                                .importName(source.getImportName());
                        for (Map.Entry<String, Double> e : source.getValMap().entrySet()) {
                            builder.observation(new DateValue(e.getKey(), e.getValue().toString()));
                        }
                        for (Map.Entry<String, String> e : source.getStrValMap().entrySet()) {
                            builder.observation(new DateValue(e.getKey(), e.getValue()));
                        }
                        result.add(builder.build());
                    }
                }
            }
        }

        return result;
    }

    private boolean skipPredicate(String predicate) {
        for (String prefix : skipPredicatePrefixes) {
            if (predicate.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a Base64-encoded, GZIP-compressed protobuf message from a cache row
     * string.
     */
    private static <T extends Message> T parseProto(String value, Parser<T> parser) {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(
                new ByteArrayInputStream(Base64.getDecoder().decode(value)))) {
            return parser.parseFrom(gzipInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing protobuf message: " + e.getMessage(), e);
        }
    }

    private static final boolean isInArcCacheRow(String row) {
        return row.startsWith(IN_ARC_CACHE_PREFIX);
    }

    private static final boolean isOutArcCacheRow(String row) {
        return row.startsWith(OUT_ARC_CACHE_PREFIX);
    }

    public static final boolean isArcCacheRow(String row) {
        return isInArcCacheRow(row) || isOutArcCacheRow(row);
    }

    public static final boolean isObsTimeSeriesCacheRow(String row) {
        return row.startsWith(OBS_TIME_SERIES_CACHE_PREFIX);
    }
}
