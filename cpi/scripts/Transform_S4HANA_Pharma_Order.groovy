package cpi.scripts

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

Message processData(Message message) {
    String orderId = message.getProperty("Prop_Merger_Order_ID")
    String drugSKU = message.getProperty("Prop_Drug_SKU")
    double totalUSD = message.getProperty("Prop_Order_Value_USD") as Double
    String plantId = message.getProperty("Prop_Plant_ID")
    String federationDigest = message.getProperty("Prop_Federation_Digest")
    String correlationId = message.getProperty("Prop_Correlation_ID") ?: UUID.randomUUID().toString()
    String lineItemsJson = message.getProperty("Prop_Line_Items_JSON")

    def jsonSlurper = new JsonSlurper()
    def lineItems = jsonSlurper.parseText(lineItemsJson)

    def s4SalesOrder = [
        SalesOrderType: "OR",
        SalesOrganization: "1010",
        DistributionChannel: "10",
        OrganizationDivision: "00",
        SoldToParty: "PHARMA-GLOBAL-DIST-01",
        PurchaseOrderByCustomer: "MA-MERGER-${orderId}".take(35),
        CustomerPurchaseOrderDate: new Date().format("yyyy-MM-dd"),
        TransactionCurrency: "USD",
        TotalNetAmount: totalUSD,
        CompleteDeliveryIsDefined: true,
        to_Item: lineItems.collect { item ->
            [
                SalesOrderItem: item.itemPosition ?: "10",
                Material: drugSKU,
                ProductionPlant: plantId.take(4),
                RequestedQuantity: (item.quantity ?: 1000.0).toString(),
                RequestedQuantityUnit: item.unitOfMeasure ?: "VIAL",
                Batch: item.batchLotNumber ?: "BATCH-GMP-2026",
                NetPriceAmount: (item.unitPrice ?: 0.0) as Double
            ]
        },
        stranglerAuditContext: [
            routingStatus: "STRANGLER_FIG_MIGRATED_ACTIVE",
            originatingFederationDigest: federationDigest,
            mplCorrelationTrackingId: correlationId,
            cleanCorePostingTimestamp: new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
        ]
    ]

    message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(s4SalesOrder)))
    message.setHeader("Content-Type", "application/json")
    message.setHeader("X-CleanCore-Target", "S4HANA_SALES_ORDER_ODATA_V4")
    return message
}
