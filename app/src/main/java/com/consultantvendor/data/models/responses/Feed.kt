package com.consultantvendor.data.models.responses

import java.io.Serializable

class Feed :Serializable{
    var id: String? = null
    var title: String? = null
    var image: String? = null
    var description: String? = null
    var like: Any? = null
    var user_id: Int? = null
    var created_at: String? = null
    var user_data: UserData? = null
    var favorite: String? = null
    var views: String? = null
    var is_favorite: Boolean? = null
}