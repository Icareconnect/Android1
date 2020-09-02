package com.consultantvendor.data.models.responses

class ClassData {
    var id: String? = null
    var name: String? = null
    var status: String? = null
    var class_date: String? = null
    var created_at: String? = null
    var bookingDateUTC: String? = null
    var price: String? = null
    var category_id: Int? = null
    var created_by: UserData? = null
    var booking_date: String? = null
    var time: String? = null
    var category_data: Categories? = null
    var totalAssignedUser:Int?=null
    var enroll_users: List<UserData>? = null
}