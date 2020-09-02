package com.consultantvendor.data.models.requests

class BookingRequest {
    var type: String? = null
    var doctor: String? = null
    var checkAvailability = false
    var timeZone: String? = null

    /*Edit Appointment*/
    var appointmentId: String? = null
    var updatedDate: String? = null
    var userType: Int? = null
}