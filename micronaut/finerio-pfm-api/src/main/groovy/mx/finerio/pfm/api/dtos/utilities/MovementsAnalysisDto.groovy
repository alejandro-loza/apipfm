package mx.finerio.pfm.api.dtos.utilities

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.ToString

@JsonInclude(JsonInclude.Include.ALWAYS)
@ToString(includeNames = true, includePackage = false)
class MovementsAnalysisDto  {
    Long date
    List<BaseCategoryResumeDto> categories = []
}
