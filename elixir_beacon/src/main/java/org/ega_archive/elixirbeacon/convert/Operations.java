package org.ega_archive.elixirbeacon.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ega_archive.elixirbeacon.constant.BeaconConstants;
import org.ega_archive.elixirbeacon.dto.Dataset;
import org.ega_archive.elixirbeacon.dto.datause.DataUseCondition;
import org.ega_archive.elixirbeacon.dto.datause.consent_code.ConsentCode;
import org.ega_archive.elixirbeacon.dto.datause.consent_code.ConsentCodeCondition;
import org.ega_archive.elixirbeacon.enums.consent_code.ConsentCodeCategory;
import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDataset;
import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDatasetConsentCode;
import org.ega_archive.elixircore.enums.DatasetAccessType;
import org.ega_archive.elixircore.exception.NotFoundException;
import org.ega_archive.elixircore.exception.PreConditionFailed;

public class Operations {

  public static Dataset convert(BeaconDataset dataset, boolean authorized,
      List<BeaconDatasetConsentCode> ccDataUseConditions) throws NotFoundException {

    Dataset beaconDataset = new Dataset();
    beaconDataset.setId(dataset.getStableId().toString());// Use the Stable Id as the ID to be used by the user
    beaconDataset.setDescription(dataset.getDescription());
    beaconDataset.setVariantCount(new Long(dataset.getVariantCnt()));
    beaconDataset.setCallCount(new Long(dataset.getCallCnt()));
    beaconDataset.setSampleCount(new Long(dataset.getSampleCnt()));
    Map<String, String> info = new HashMap<String, String>();
    info.put(BeaconConstants.ACCESS_TYPE, DatasetAccessType.parse(dataset.getAccessType())
        .getType());
    info.put(BeaconConstants.AUTHORIZED, Boolean.toString(authorized));
    beaconDataset.setInfo(info);
    beaconDataset.setAssemblyId(dataset.getReferenceGenome());

    DataUseCondition dataUseCondition = DataUseCondition.builder()
        .consentCodedataUse(convertConsentCodes(ccDataUseConditions)).build();
    beaconDataset.setDataUseConditions(dataUseCondition);

    return beaconDataset;
  }

  private static ConsentCode convertConsentCodes(List<BeaconDatasetConsentCode> ccDataUseConditions)
      throws NotFoundException {

    ConsentCode consentCode = new ConsentCode();
    String abreviation = null;
    ConsentCodeCondition condition = null;

    List<String> versions = new ArrayList<>();
    for (BeaconDatasetConsentCode beaconDatasetConsentCode : ccDataUseConditions) {
      ConsentCodeCategory category =
          ConsentCodeCategory.parse(beaconDatasetConsentCode.getCategory());
      switch (category) {
        case PRIMARY:
          condition = new ConsentCodeCondition();
          abreviation = beaconDatasetConsentCode.getId().getCode();
          condition.setCode(abreviation);
          condition.setDescription(beaconDatasetConsentCode.getDescription());
          condition.setAdditionalConstraint(beaconDatasetConsentCode.getAdditionalConstraint());
          consentCode.setPrimaryCategory(condition);
          break;
        case REQUIREMENTS:
          condition = new ConsentCodeCondition();
          abreviation = beaconDatasetConsentCode.getId().getCode();
          condition.setCode(abreviation);
          condition.setDescription(beaconDatasetConsentCode.getDescription());
          condition.setAdditionalConstraint(beaconDatasetConsentCode.getAdditionalConstraint());
          consentCode.addRequirement(condition);
          break;
        case SECONDARY:
          condition = new ConsentCodeCondition();
          abreviation = beaconDatasetConsentCode.getId().getCode();
          condition.setCode(abreviation);
          condition.setDescription(beaconDatasetConsentCode.getDescription());
          condition.setAdditionalConstraint(beaconDatasetConsentCode.getAdditionalConstraint());
          consentCode.addSecondaryCategory(condition);
          break;
        default:
          break;
      }
      versions.add(beaconDatasetConsentCode.getVersion());
    }
    // Remove duplicates
    versions = versions.stream().distinct().collect(Collectors.toList());
    if (!ccDataUseConditions.isEmpty() && versions.isEmpty()) { // Should not happen
      throw new NotFoundException("No Consent Code version found");
    }
    if (versions.size() > 1) {
      throw new PreConditionFailed("Found different versions of Consent Codes: " + versions);
    }
    if (!versions.isEmpty()) {
      consentCode.setVersion(versions.get(0));
    }
    return consentCode;
  }

}
