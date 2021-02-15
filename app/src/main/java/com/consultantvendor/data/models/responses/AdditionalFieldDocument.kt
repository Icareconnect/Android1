package com.consultantvendor.data.models.responses

import java.io.Serializable

class AdditionalFieldDocument : Serializable {
    var id: String? = null
    var title: String? = null
    var description: String? = null
    var file_name: String? = null
    var type: String? = null
    var is_edit: Boolean? = null

    var status: String? = null
}