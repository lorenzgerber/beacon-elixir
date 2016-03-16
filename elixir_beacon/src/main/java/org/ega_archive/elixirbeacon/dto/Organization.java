package org.ega_archive.elixirbeacon.dto;

import java.util.Map;

import org.ega_archive.elixirbeacon.constant.BeaconConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Organization {

  // Unique identifier of the organization
  private String id = BeaconConstants.ORGANIZATION_ID;
  
  // Name of the organization
  private String name = BeaconConstants.ORGANIZATION_NAME;;
  
  // Description of the organization
  private String description = BeaconConstants.ORGANIZATION_DESCRIPTION;
  
  // Address of the organization
  private String address = BeaconConstants.ORGANIZATION_ADDRESS;
  
  // Web of the organization (URL)
  private String welcomeUrl = BeaconConstants.ORGANIZATION_HOMEPAGE;
 
  // URL with the contact for the beacon operator/maintainer, e.g. link to a contact form or an email in RFC 2368 scheme
  private String contactUrl = BeaconConstants.ORGANIZATION_CONTACT;
  
  // URL to the logo of the organization (image)
  private String logoUrl = BeaconConstants.ORGANIZATION_LOGO;
  
  // Additional structured metadata, key-value pairs
  private Map<String, String> info;
  
}
