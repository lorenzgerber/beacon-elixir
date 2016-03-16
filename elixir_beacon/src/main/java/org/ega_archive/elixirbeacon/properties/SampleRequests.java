package org.ega_archive.elixirbeacon.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix="querySamples")
public class SampleRequests {

  @NotNull
  private String assemblyId1;
  
  @NotNull
  private String assemblyId2;
  
  @NotNull
  private String assemblyId3;
  
  @NotNull
  private Integer position1;
  
  @NotNull
  private Integer position2;
  
  @NotNull
  private Integer position3;
  
  @NotNull
  private String referenceName1;
  
  @NotNull
  private String referenceName2;
  
  @NotNull
  private String referenceName3;
  
  private List<String> datasetIds1;
  
  private List<String> datasetIds2;
  
  private List<String> datasetIds3;
  
  private String alternateBases1;
  
  private String alternateBases2;
  
  private String alternateBases3;
  
}
