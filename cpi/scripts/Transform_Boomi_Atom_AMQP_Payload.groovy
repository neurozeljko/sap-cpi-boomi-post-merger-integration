package cpi.scripts

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

Message processData(Message message) {
    String orderId = message.getProperty("Prop_Merger_Order_ID")
    String drugSKU = message.getProperty("Prop_Drug_SKU")
    String plantId = message.getProperty("Prop_Plant_ID")
    String federationDigest = message.getProperty("Prop_Federation_Digest")
    String correlationId = message.getProperty("Prop_Correlation_ID") ?: UUID.randomUUID().toString()
    String lineItemsJson = message.getProperty("Prop_Line_Items_JSON")

    def jsonSlurper = new JsonSlurper()
    def lineItems = jsonSlurper.parseText(lineItemsJson)

    def boomiEnvelope = [
        boomiAtomSphereEvent: [
            targetAtomCluster: "ATOM-CLUSTER-${plantId}",
            executionMode: "HYBRID_STRANGLER_COEXISTENCE",
            sourceGateway: "SAP_INTEGRATION_SUITE_FACADE",
            trackingId: correlationId,
            federationSeal: federationDigest,
            legacyOrderPayload: [
                externalOrderId: orderId,
                legacySKU: drugSKU,
                allocatedFacility: plantId,
                lineItems: lineItems
            ],
            dispatchedTimestamp: new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
        ]
    ]

    message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(boomiEnvelope)))
    message.setHeader("Content-Type", "application/json")
    message.setHeader("X-Boomi-Target-Queue", "queue:/boomi/atomsphere/pharma/orders/v1")
    return message
}
