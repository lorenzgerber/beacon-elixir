package org.ega_archive.elixirbeacon.model.elixirbeacon;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;

import lombok.Data;


/**
 * The persistent class for the beacon_data database table.
 * 
 */
@Data
@Entity
@Table(name = "beacon_data")
@NamedQuery(name = "BeaconData.findAll", query = "SELECT b FROM BeaconData b")
@NamedStoredProcedureQuery(name = "queryData", procedureName = "public.query_data",
    resultClasses = {BeaconData.class}, parameters = {
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_variant_type", type = String.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_start", type = Integer.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_start_min", type = Integer.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_start_max", type = Integer.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_end", type = Integer.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_end_min", type = Integer.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_end_max", type = Integer.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_chromosome", type = String.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_reference_bases", type = String.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_alternate_bases", type = String.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_reference_genome", type = String.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "_dataset_ids", type = String.class)})
public class BeaconData implements Serializable {
  private static final long serialVersionUID = 1L;

  @Column(name = "alternate_bases")
  private String alternateBases;

  @Column(name = "call_cnt")
  private Integer callCnt;

  private String chromosome;

  private Integer end;

  @Id
  private Integer id;

  @Column(name = "reference_bases")
  private String referenceBases;

  @Column(name = "reference_genome")
  private String referenceGenome;

  @Column(name = "sample_cnt")
  private Integer sampleCnt;

  @Column(name = "dataset_id")
  private Integer datasetId;

  private Integer start;

  @Column(name = "sv_length")
  private Integer svLength;

  private String type;

  @Column(name = "variant_cnt")
  private Integer variantCnt;
  
  private Double frequency;

}
