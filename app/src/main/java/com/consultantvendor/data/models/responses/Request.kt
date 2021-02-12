package com.consultantvendor.data.models.responses

import java.io.Serializable

class Request : Serializable {

    var id: String? = null
    var booking_date: String? = null
    var from_user: UserData? = null
    var to_user: UserData? = null
    var canceled_by: UserData? = null
    var time: String? = null
    var service_type: String? = null
    var status: String? = null
    var price: String? = null
    var created_at: String? = null
    var bookingDateUTC: String? = null
    var canReschedule = false
    var rating: String? = null
    var comment:String?=null
    var user_status: String? = null
    var user_comment: String? = null
    var canCancel = false

    var call_id: String? = null
    var extra_detail: Extra_detail? = null
    var duties:ArrayList<FilterOption>?=null
}