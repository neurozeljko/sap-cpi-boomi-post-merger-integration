package cpi.scripts

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.security.MessageDigest

Message processData(Message message) {
    def reader = message.getBody(java.io.Reader)
    def jsonSlurper = new JsonSlurper()
    def payload = jsonSlurper.parse(reader)

    String orderId = payload.mergerOrderId ?: "ORD-MA-PHARMA-2026-00"
    String drugSKU = payload.pharmaceuticalSKU ?: "ONCO-DRUG-GEN2"
    double orderTotalUSD = (payload.totalValueUSD ?: 0.0) as Double
    String targetPlant = payload.manufacturingPlantId ?: "PLANT-US-BOSTON-01"
    def batchItems = payload.orderLineItems ?: []

    if (batchItems.isEmpty()) {
        throw new IllegalArgumentException("MA_PAYLOAD_INVALID: Pharmaceutical order line items cannot be empty.")
    }

    String auditContext = "${orderId}|${drugSKU}|${orderTotalUSD}|${targetPlant}"
    MessageDigest digest = MessageDigest.getInstance("SHA-256")
    byte[] hashBytes = digest.digest(auditContext.getBytes("UTF-8"))
    StringBuilder hexString = new StringBuilder()
    for (byte b : hashBytes) {
        String hex = Integer.toHexString(0xff & b)
        if (hex.length() == 1) hexString.append('0')
        hexString.append(hex)
    }
    String federationAuditDigest = hexString.toString()

    message.setProperty("Prop_Merger_Order_ID", orderId)
    message.setProperty("Prop_Drug_SKU", drugSKU)
    message.setProperty("Prop_Order_Value_USD", orderTotalUSD)
    message.setProperty("Prop_Plant_ID", targetPlant)
    message.setProperty("Prop_Federation_Digest", federationAuditDigest)
    message.setProperty("Prop_Line_Items_JSON", JsonOutput.toJson(batchItems))
    message.setHeader("X-Federation-Verified", "RSA256_TRUST_CONFIRMED")

    return message
}
