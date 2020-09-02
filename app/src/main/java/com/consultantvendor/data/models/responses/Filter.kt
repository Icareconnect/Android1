package com.consultantvendor.data.models.responses

import java.io.Serializable

class Filter :Serializable{
    var id: Int? = null
    var category_id: Int? = null
    var filter_name: String? = null
    var preference_name: String? = null
    var is_multi: String? = null
    var options: List<FilterOption>? = null

}