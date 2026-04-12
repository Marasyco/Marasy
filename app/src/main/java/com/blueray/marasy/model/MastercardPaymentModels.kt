package com.blueray.marasy.model

import com.google.gson.annotations.SerializedName

data class MastercardSessionResponse(
    val merchant: String,
    val result: String,
    val session: MastercardSession,
    val version: String
)

data class MastercardSession(
    @SerializedName("aes256Key")
    val aes256Key: String,
    @SerializedName("authenticationLimit")
    val authenticationLimit: Int,
    val id: String,
    @SerializedName("updateStatus")
    val updateStatus: String,
    val version: String
)

data class MastercardInitiateCheckoutRequest(
    val apiOperation: String = "INITIATE_CHECKOUT",
    val interaction: MastercardInteraction,
    val order: MastercardOrder
)

data class MastercardInteraction(
    val operation: String,
    val merchant: MastercardMerchant
)

data class MastercardMerchant(
    val name: String
)

data class MastercardUpdateSessionRequest(
    val order: MastercardOrder
)

data class MastercardOrder(
    val id: String,
    val currency: String,
    val amount: String,
    val reference: String? = null,
    val description: String? = null
)

data class MastercardUpdateSessionResponse(
    val merchant: String,
    val order: MastercardOrder,
    val session: MastercardSessionUpdate,
    val version: String
)

data class MastercardSessionUpdate(
    val id: String,
    val updateStatus: String,
    val version: String
)

// Card data models for session update
data class MastercardCardUpdateRequest(
    val sourceOfFunds: MastercardSourceOfFunds
)

data class MastercardSourceOfFunds(
    val provided: MastercardProvidedCard
)

data class MastercardProvidedCard(
    val card: MastercardCard
)

data class MastercardCard(
    val number: String,
    val nameOnCard: String,
    val securityCode: String,
    val expiry: MastercardCardExpiry
)

data class MastercardCardExpiry(
    val month: String,
    val year: String
)
