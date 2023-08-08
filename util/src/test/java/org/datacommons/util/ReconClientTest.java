package org.datacommons.util;

import static com.google.common.truth.Truth.assertThat;
import static java.util.stream.Collectors.toList;
import static org.datacommons.util.McfUtil.newEntitySubGraph;
import static org.datacommons.util.ReconClient.NUM_API_CALLS_COUNTER;
import static org.datacommons.util.TestUtil.getCounter;
import static org.datacommons.util.TestUtil.newLogCtx;

import java.net.http.HttpClient;
import org.datacommons.proto.Recon.*;
import org.datacommons.proto.Recon.ResolveCoordinatesRequest.Coordinate;
import org.junit.Test;

public class ReconClientTest {
  private static final String INDIA_DCID = "country/IND";
  private static final String USA_DCID = "country/USA";
  private static final String GBR_DCID = "country/GBR";
  private static final String CA_DCID = "geoId/06";
  private static final String INDIA_SOURCE_ID = "isoCode:IN";
  private static final String CA_SOURCE_ID = "geoId:06";
  private static final IdWithProperty INDIA_RESOLVED_ID =
      IdWithProperty.newBuilder().setProp("dcid").setVal(INDIA_DCID).build();
  private static final IdWithProperty CA_RESOLVED_ID =
      IdWithProperty.newBuilder().setProp("dcid").setVal(CA_DCID).build();
  // India using isoCode
  private static final EntitySubGraph INDIA_ENTITY = newEntitySubGraph("isoCode", "IN");
  // CA using geo ID
  private static final EntitySubGraph CA_ENTITY = newEntitySubGraph("geoId", "06");
  private static final Coordinate SF_COORDINATES =
      Coordinate.newBuilder().setLatitude(37.77493).setLongitude(-122.41942).build();
  private static final Coordinate BIG_BEN_COORDINATES =
      Coordinate.newBuilder().setLatitude(51.510357).setLongitude(-0.116773).build();

  @Test
  public void resolveEntities() throws Exception {
    LogWrapper logWrapper = newLogCtx();
    ReconClient client = new ReconClient(HttpClient.newHttpClient(), logWrapper);

    ResolveEntitiesRequest request =
        ResolveEntitiesRequest.newBuilder()
            .addEntities(INDIA_ENTITY)
            .addEntities(CA_ENTITY)
            .build();

    ResolveEntitiesResponse result = client.resolveEntities(request).get();

    assertThat(result.getResolvedEntitiesCount()).isEqualTo(2);
    assertThat(result.getResolvedEntities(0).getSourceId()).isEqualTo(INDIA_SOURCE_ID);
    assertThat(result.getResolvedEntities(0).getResolvedIdsCount()).isEqualTo(1);
    assertThat(result.getResolvedEntities(0).getResolvedIds(0).getIdsList())
        .contains(INDIA_RESOLVED_ID);
    assertThat(result.getResolvedEntities(1).getSourceId()).isEqualTo(CA_SOURCE_ID);
    assertThat(result.getResolvedEntities(1).getResolvedIdsCount()).isEqualTo(1);
    assertThat(result.getResolvedEntities(1).getResolvedIds(0).getIdsList())
        .contains(CA_RESOLVED_ID);
    assertThat(getCounter(logWrapper.getLog(), NUM_API_CALLS_COUNTER)).isEqualTo(1);
  }

  @Test
  public void resolveEntities_chunked() throws Exception {
    LogWrapper logWrapper = newLogCtx();
    ReconClient client = new ReconClient(HttpClient.newHttpClient(), logWrapper, 1);

    ResolveEntitiesRequest request =
        ResolveEntitiesRequest.newBuilder()
            .addEntities(INDIA_ENTITY)
            .addEntities(CA_ENTITY)
            .build();

    ResolveEntitiesResponse result = client.resolveEntities(request).get();

    assertThat(result.getResolvedEntitiesCount()).isEqualTo(2);
    assertThat(result.getResolvedEntities(0).getSourceId()).isEqualTo(INDIA_SOURCE_ID);
    assertThat(result.getResolvedEntities(0).getResolvedIdsCount()).isEqualTo(1);
    assertThat(result.getResolvedEntities(0).getResolvedIds(0).getIdsList())
        .contains(INDIA_RESOLVED_ID);
    assertThat(result.getResolvedEntities(1).getSourceId()).isEqualTo(CA_SOURCE_ID);
    assertThat(result.getResolvedEntities(1).getResolvedIdsCount()).isEqualTo(1);
    assertThat(result.getResolvedEntities(1).getResolvedIds(0).getIdsList())
        .contains(CA_RESOLVED_ID);
    assertThat(getCounter(logWrapper.getLog(), NUM_API_CALLS_COUNTER)).isEqualTo(2);
  }

  @Test
  public void resolveCoordinates() throws Exception {
    LogWrapper logWrapper = newLogCtx();
    ReconClient client = new ReconClient(HttpClient.newHttpClient(), logWrapper);

    ResolveCoordinatesRequest request =
        ResolveCoordinatesRequest.newBuilder()
            .addCoordinates(SF_COORDINATES)
            .addCoordinates(BIG_BEN_COORDINATES)
            .build();

    ResolveCoordinatesResponse result = client.resolveCoordinates(request).get();

    assertThat(result.getPlaceCoordinatesCount()).isEqualTo(2);
    assertThat(
            result.getPlaceCoordinates(0).getPlacesList().stream()
                .map(ResolveCoordinatesResponse.Place::getDcid)
                .collect(toList()))
        .contains(USA_DCID);
    assertThat(
            result.getPlaceCoordinates(1).getPlacesList().stream()
                .map(ResolveCoordinatesResponse.Place::getDcid)
                .collect(toList()))
        .contains(GBR_DCID);
    assertThat(getCounter(logWrapper.getLog(), NUM_API_CALLS_COUNTER)).isEqualTo(1);
  }

  @Test
  public void resolveCoordinates_chunked() throws Exception {
    LogWrapper logWrapper = newLogCtx();
    ReconClient client = new ReconClient(HttpClient.newHttpClient(), logWrapper, 1);

    ResolveCoordinatesRequest request =
        ResolveCoordinatesRequest.newBuilder()
            .addCoordinates(SF_COORDINATES)
            .addCoordinates(BIG_BEN_COORDINATES)
            .build();

    ResolveCoordinatesResponse result = client.resolveCoordinates(request).get();

    assertThat(result.getPlaceCoordinatesCount()).isEqualTo(2);
    assertThat(
            result.getPlaceCoordinates(0).getPlacesList().stream()
                .map(ResolveCoordinatesResponse.Place::getDcid)
                .collect(toList()))
        .contains(USA_DCID);
    assertThat(
            result.getPlaceCoordinates(1).getPlacesList().stream()
                .map(ResolveCoordinatesResponse.Place::getDcid)
                .collect(toList()))
        .contains(GBR_DCID);
    assertThat(getCounter(logWrapper.getLog(), NUM_API_CALLS_COUNTER)).isEqualTo(2);
  }
}
