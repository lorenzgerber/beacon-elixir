package org.ega_archive.elixirbeacon.convert;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ega_archive.elixirbeacon.constant.BeaconConstants;
import org.ega_archive.elixirbeacon.dto.AdamDataUseProfile;
import org.ega_archive.elixirbeacon.dto.ConsentCode;
import org.ega_archive.elixirbeacon.dto.ConsentCodeCondition;
import org.ega_archive.elixirbeacon.dto.ConsentCodeDataUseProfile;
import org.ega_archive.elixirbeacon.dto.DataUseCondition;
import org.ega_archive.elixirbeacon.dto.Dataset;
import org.ega_archive.elixirbeacon.enums.ConsentCodeCategory;
import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDataset;
import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDatasetAdam;
import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDatasetConsentCode;
import org.ega_archive.elixircore.enums.DatasetAccessType;

public class Operations {

  public static Dataset convert(BeaconDataset dataset, boolean authorized,
      List<BeaconDatasetConsentCode> ccDataUseConditions,
      List<BeaconDatasetAdam> adamDataUseConditions) {
    
    Dataset beaconDataset = new Dataset();
    beaconDataset.setId(dataset.getId());
    beaconDataset.setDescription(dataset.getDescription());
    beaconDataset.setVariantCount(new Long(dataset.getSize()));
    Map<String, String> info = new HashMap<String, String>();
    info.put(BeaconConstants.ACCESS_TYPE, DatasetAccessType.parse(dataset.getAccessType()).getType());
    info.put(BeaconConstants.AUTHORIZED, Boolean.toString(authorized));
    beaconDataset.setInfo(info);
    beaconDataset.setAssemblyId(dataset.getReferenceGenome());
    
    // Data Use Conditions
    if(ccDataUseConditions != null && !ccDataUseConditions.isEmpty()) {
      beaconDataset.addDataUseCondition(convertConsentCodes(ccDataUseConditions));
    }
    if(adamDataUseConditions != null && !adamDataUseConditions.isEmpty()) {
      beaconDataset.addDataUseCondition(convertAdamDuc(adamDataUseConditions));
    }
    
    return beaconDataset;
  }

  private static DataUseCondition convertConsentCodes(
      List<BeaconDatasetConsentCode> ccDataUseConditions) {
    
    ConsentCode consentCodeAttr = new ConsentCode();
    for(BeaconDatasetConsentCode consentCode : ccDataUseConditions) {
      ConsentCodeCategory category = ConsentCodeCategory.parse(consentCode.getCategory());
      switch(category) {
        case PRIMARY:
          consentCodeAttr.setPrimaryCategory(ConsentCodeCondition.builder()
              .code(consentCode.getId().getCode())
              .description(consentCode.getDescription())
              .details(consentCode.getDetail())
              .build());
          break;
        case REQUIREMENTS:
          consentCodeAttr.addRequirement(ConsentCodeCondition.builder()
              .code(consentCode.getId().getCode())
              .description(consentCode.getDescription())
              .details(consentCode.getDetail())
              .build());
          break;
        case SECONDARY:
          consentCodeAttr.addSecondaryCategory(ConsentCodeCondition.builder()
              .code(consentCode.getId().getCode())
              .description(consentCode.getDescription())
              .details(consentCode.getDetail())
              .build());
          break;
        default:
          break;
      }
    }
    ConsentCodeDataUseProfile ccCondition = new ConsentCodeDataUseProfile();
    ccCondition.setProfile(consentCodeAttr);
    return ccCondition;
  }

  private static DataUseCondition convertAdamDuc(List<BeaconDatasetAdam> adamDataUseConditions) {

    Map<String, String> adamAttributes = new LinkedHashMap<String, String>();
    for (BeaconDatasetAdam condition : adamDataUseConditions) {
      adamAttributes.put(condition.getId().getAttribute(), condition.getValue());
    }
    AdamDataUseProfile adamCondition = new AdamDataUseProfile();
    adamCondition.setProfile(adamAttributes);
    return adamCondition;
  }

}
