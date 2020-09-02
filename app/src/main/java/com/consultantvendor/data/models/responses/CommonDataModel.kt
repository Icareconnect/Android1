package com.consultantvendor.data.models.responses

import com.consultantvendor.data.models.requests.Interval
import com.consultantvendor.data.models.responses.chat.ChatList
import com.consultantvendor.data.models.responses.chat.ChatMessage

class CommonDataModel {
    var dcotor_detail: UserData? = null
    var requests: List<Request>? = null
    var payments: List<Wallet>? = null
    var bank_accounts: List<Bank>? = null
    var lists: List<ChatList>? = null
    var messages: List<ChatMessage>? = null
    var notifications: List<Notification>? = null
    var classes_category: List<Categories>? = null
    var services: List<Service>? = null
    var filters: List<Filter>? = null
    var classes: List<ClassData>? = null
    var slots: List<Interval>? = null

    var balance: String? = null
    var after: String? = null
    var before: String? = null
    var per_page: String? = null
    var isOnline: Boolean? = null
    var image_name: String? = null
    var isAprroved: Boolean? = null

    var CALLING_TYPE: String? = null
    var request_status: String? = null
    var currentTimer: Long? = null

    var subscriptions: ArrayList<Subscription>? = null

    var isCallFrom: String? = null

    /*Twili*/
    var twilioToken: String? = null

    var action: String? = null

    /*Pages*/
    var pages: List<Page>? = null

    /*Add money stripe authentication*/
    var requires_source_action: Boolean? = null
    var url: String? = null
    var transaction_id: String? = null

    var order_id: String? = null

    var cards: List<Wallet>? = null

    /*Country*/
    var type: String? = null
    var country: List<CountryCity>? = null
    var city: List<CountryCity>? = null
    var state: List<CountryCity>? = null

    /*Additional Field*/
    var additional_details: List<AdditionalField>? = null
    var additionals: List<AdditionalField>? = null


    /*Caller id*/
    var call_id: String? = null

    /*Feeds*/
    var feeds: List<Feed>? = null
    var feed: Feed? = null
}