package com.consultantvendor.data.network

const val PER_PAGE_LOAD = 20


object ApiKeys {

    /*facbook,google,email,phone*/
    const val PROVIDER_TYPE = "provider_type"

    /*optional only phone and email*/
    const val PROVIDER_ID = "provider_id"

    /*access_token or password or otp*/
    const val PROVIDER_VERIFICATION = "provider_verification"
    const val USER_TYPE = "user_type"

    const val AFTER = "after"
    const val PER_PAGE = "per_page"
}

object ProviderType {
    const val facebook = "facebook"
    const val google = "google"
    const val email = "email"
    const val phone = "phone"
}

object LoadingStatus {
    const val ITEM = 0
    const val LOADING = 1
}


object PushType {
    const val PROFILE_APPROVED = "PROFILE_APPROVED"
    const val CHAT = "chat"
    const val NEW_REQUEST = "NEW_REQUEST"
    const val REQUEST_FAILED = "REQUEST_FAILED"
    const val CANCELED_REQUEST = "CANCELED_REQUEST"
    const val RESCHEDULED_REQUEST = "RESCHEDULED_REQUEST"
    const val REQUEST_COMPLETED = "REQUEST_COMPLETED"
    const val AMOUNT_RECEIVED = "AMOUNT_RECEIVED"
    const val PAYOUT_PROCESSED="PAYOUT_PROCESSED"
    const val ASSINGED_USER = "ASSINGED_USER"

    const val CALL_RINGING = "CALL_RINGING"
    const val CALL_ACCEPTED = "CALL_ACCEPTED"
    const val CALL_CANCELED = "CALL_CANCELED"

}