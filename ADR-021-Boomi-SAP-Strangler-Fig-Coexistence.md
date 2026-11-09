# ADR-021: Global M&A Post-Merger Integration (SAP Integration Suite & Boomi Coexistence)

## Status
Accepted

## Context
A $15B pharmaceutical acquisition requires order and inventory systems integration within 30 days. The target operates hundreds of Boomi AtomSphere nodes. A full rip-and-replace migration would take 24 months and trigger severe compliance and revenue disruptions.

## Decision
1. Establish a cross-platform Event Mesh peering SAP Advanced Event Mesh (AEM) with Boomi Atoms via AMQP 1.0.
2. Deploy SAP API Management as an edge facade implementing the Strangler Fig pattern, routing legacy lines to Boomi and migrated lines to SAP S/4HANA.
3. Enforce Zero-Trust JWT federation using asymmetric RSA-256 tokens and SHA-256 in-flight digests for B2B payload integrity.

## Consequences
- Cuts go-live timeline from 2 years to under 30 days.
- Eliminates cutover downtime on global pharmaceutical manufacturing lines.
- Ensures gradual, risk-free decommissioning of legacy Boomi licenses.
