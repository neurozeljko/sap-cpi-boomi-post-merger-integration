package cpi.scripts

import com.sap.gateway.ip.core.customdev.util.Message

Message processData(Message message) {
    String drugSKU = message.getProperty("Prop_Drug_SKU") as String
    String migrationPhase = message.getProperty("Prop_Migration_Phase") as String

    boolean isMigratedToSAP = drugSKU.startsWith("ONCO-") || 
                              drugSKU.startsWith("BIO-") || 
                              migrationPhase == "MIGRATED_FULL_CUTOVER"

    String targetEngine = isMigratedToSAP ? "SAP_CLEAN_CORE" : "BOOMI_ATOM_LEGACY"

    message.setProperty("Prop_Target_Engine", targetEngine)
    message.setHeader("X-Strangler-Route", targetEngine)
    return message
}
