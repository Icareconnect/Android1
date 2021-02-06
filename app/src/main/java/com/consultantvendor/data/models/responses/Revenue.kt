package com.consultantvendor.data.models.responses

class Revenue {

    var id: String? = null
    var totalChat: String? = null
    var totalCall: String? = null
    var totalRequest: String? = null
    var completedRequest: String? = null
    var unSuccesfullRequest: String? = null
    var totalRevenue: String? = null
    var services: ArrayList<Service>? = null
    var monthlyRevenue: List<MonthlyRevenue>? = null

    var totalShiftCompleted: String? = null
    var totalHourCompleted: String? = null
    var totalShiftDecline: String? = null
}