package mx.finerio.pfm.api.validation

import groovy.transform.ToString
import mx.finerio.pfm.api.enums.BudgetStatusEnum
import javax.validation.constraints.Pattern

@ToString(includeNames = true, includePackage = false)
class WebHookCreateCommand extends ValidationCommand  {

    @Pattern(regexp="((http|ftp|https):\\/\\/)?[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?")
    String url
    BudgetStatusEnum nature
}
