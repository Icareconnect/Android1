package com.consultantvendor.data.models.requests

import java.io.File
import java.io.Serializable

class DocImage : Serializable {
    var imageFile: File? = null
    var type: String? = null

    var image: String? = null
}