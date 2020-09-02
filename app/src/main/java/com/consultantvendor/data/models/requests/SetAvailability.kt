package com.consultantvendor.data.models.requests

import java.io.Serializable

class SetAvailability : Serializable {
    var days: ArrayList<Boolean>? = null
    var slots: ArrayList<Interval>? = null
    var applyoption: String? = null
    var date: String? = null
}