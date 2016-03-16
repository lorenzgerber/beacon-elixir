package org.ega_archive.elixirbeacon.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
@AllArgsConstructor
public class ConsentCode {
  
  // Primary data use category.
  private ConsentCodeCondition primaryCategory;
  
  //  Secondary data use categories.
  private List<ConsentCodeCondition> secondaryCategories;
  
  // Data use requirements.
  private List<ConsentCodeCondition> requirements;
  
  public ConsentCode() {
    primaryCategory = null;
    secondaryCategories = null;
    requirements = null;
  }
  
  public void addSecondaryCategory(ConsentCodeCondition condition) {
    if(secondaryCategories == null) {
      secondaryCategories = new ArrayList<ConsentCodeCondition>();
    }
    secondaryCategories.add(condition);
  }
  
  public void addRequirement(ConsentCodeCondition condition) {
    if(requirements == null) {
      requirements = new ArrayList<ConsentCodeCondition>();
    }
    requirements.add(condition);
  }
  
}
