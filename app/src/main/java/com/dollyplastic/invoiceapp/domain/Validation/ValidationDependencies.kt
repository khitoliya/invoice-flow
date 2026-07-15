package com.dollyplastic.invoiceapp.domain.Validation

object ValidationDependencies {

    /**
     * Defines the "Prerequisites" for each field.
     * When a field is interacted with (touched), all its prerequisites
     * are also marked as touched.
     */
    val dependencyGraph = mapOf(
        // Party Section depends on Firm Section
        "billToParty" to listOf("firm"),
        "shipToParty" to listOf("firm"),

        // Items depend on Firm & Party
        "items" to listOf("firm", "billToParty"),

        // Transport Section depends on everything above
        "distance" to listOf("firm", "billToParty", "shipToParty"),
        "mode" to listOf("firm", "billToParty"),
        "vehicleNumber" to listOf("mode", "vehicleType", "firm", "billToParty"),
        "transporterName" to listOf("mode", "firm"),
        
        // E-Way Bill / Compliance depend on everything
        "generateEInvoice" to listOf("items", "transportDetails")
    )

    /**
     * Returns a recursive list of all dependencies for a given field
     */
    fun getAllDependencies(field: String): Set<String> {
        val result = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(field)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            dependencyGraph[current]?.forEach { dep ->
                if (result.add(dep)) { // If new dependency found
                    queue.add(dep)
                }
            }
        }
        return result
    }
}
