package com.consultantvendor.data.models.requests

import com.consultantvendor.data.models.responses.AdditionalField

class UpdateDocument {
    var sp_id: String? = null
    var fields: ArrayList<AdditionalField>? = null
}