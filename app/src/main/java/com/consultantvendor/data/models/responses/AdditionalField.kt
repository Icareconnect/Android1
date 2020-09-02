package com.consultantvendor.data.models.responses

import java.io.Serializable

class AdditionalField : Serializable {
    var id: String? = null
    var name: String? = null
    var category_id: String? = null
    var type: String? = null
    var is_enable: String? = null
    var documents = ArrayList<AdditionalFieldDocument>()
    var created_at: String? = null
    var updated_at: String? = null
}