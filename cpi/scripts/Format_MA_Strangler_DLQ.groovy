package cpi.scripts

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput

Message processData(Message message) {
    def exception = message.getProperty("CamelExceptionCaught")
    String orderId = message.getProperty("Prop_Merger_Order_ID") ?: "UNKNOWN_ORDER"
    String originSystem = message.getProperty("Prop_Origin_System") ?: "UNKNOWN_ATOM"
    String correlationId = message.getHeader("SAP_MplCorrelationId", String) ?: UUID.randomUUID().toString()
    String errorMsg = exception ? exception.getMessage() : "M&A Strangler Fig cross-platform routing fault."

    def dlqPayload = [
        mergerIntegrationDeadLetterQueue: [
            incidentTrackingId: correlationId,
            mergerOrderId: orderId,
            originatingNode: originSystem,
            failureReason: errorMsg,
            quarantinePolicy: "HOLD_IN_AEM_DLQ_FOR_SECURITY_AUDIT",
            timestamp: new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
        ]
    ]

    message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(dlqPayload)))
    message.setHeader("Content-Type", "application/json")
    message.setHeader("X-MA-Status", "QUARANTINED_IN_DLQ")
    return message
}
