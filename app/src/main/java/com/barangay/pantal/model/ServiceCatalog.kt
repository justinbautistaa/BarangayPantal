package com.barangay.pantal.model

object ServiceCatalog {

    private val hiddenDocumentServices = setOf(
        "barangay clearance",
        "certificate of indigency",
        "certificate of residency",
        "business clearance"
    )

    private val builtInServices = listOf(
        Service(
            name = "TUPAD",
            description = "Tulong Panghanapbuhay sa Ating Disadvantaged/Displaced Workers emergency employment program for underemployed or displaced workers.",
            category = "Employment",
            schedule = "Quarterly (March, June, September, December)",
            contactInfo = "Barangay Secretary - (075) 123-4567",
            icon = "\uD83D\uDCBC",
            status = "Active",
            venue = "Barangay Hall",
            requirements = listOf(
                "Barangay Clearance",
                "Valid ID",
                "Certificate of Indigency",
                "1x1 Picture"
            ),
            howToAvail = "Pumunta sa Barangay Hall at makipag-ugnayan sa Barangay Secretary. Dalhin ang mga requirements para sa assessment at orientation."
        ),
        Service(
            name = "Feeding Program",
            description = "Supplemental feeding program para sa mga batang 2-5 years old upang labanan ang malnutrition.",
            category = "Health",
            schedule = "Every Tuesday and Thursday, 9:00 AM - 11:00 AM",
            contactInfo = "Barangay Health Worker - (075) 123-4568",
            icon = "\u2764\uFE0F",
            status = "Active",
            venue = "Barangay Health Center",
            requirements = listOf(
                "Birth Certificate",
                "Barangay Clearance",
                "Medical Assessment"
            ),
            howToAvail = "Makipag-ugnayan sa Barangay Health Center o Barangay Secretary para sa schedule, screening, at enrollment ng bata sa feeding sessions."
        ),
        Service(
            name = "Barangay Sports League",
            description = "Basketball, volleyball, at iba pang sports tournament para sa kabataan ng barangay.",
            category = "Youth",
            schedule = "Every Saturday, 1:00 PM - 6:00 PM",
            contactInfo = "SK Chairman - (075) 123-4569",
            icon = "\uD83C\uDFC6",
            status = "Active",
            venue = "Barangay Covered Court",
            requirements = listOf(
                "Barangay Clearance",
                "Medical Clearance",
                "1x1 Picture",
                "Birth Certificate"
            ),
            howToAvail = "Magparehistro sa Barangay Hall o sa assigned sports coordinator at hintayin ang anunsyo ng tryouts, bracket, at game schedule."
        ),
        Service(
            name = "Medical Mission",
            description = "Libreng check-up, laboratory tests, at gamot para sa lahat ng residente.",
            category = "Health",
            schedule = "Every First Sunday of the Month, 8:00 AM - 3:00 PM",
            contactInfo = "Barangay Health Center - (075) 123-4570",
            icon = "\u2764\uFE0F",
            status = "Active",
            venue = "Barangay Health Center",
            requirements = listOf(
                "Barangay Clearance",
                "Valid ID",
                "Health Card (if available)"
            ),
            howToAvail = "Pumunta sa Barangay Health Center o Barangay Hall para sa schedule ng medical mission at dalhin ang kinakailangang records kung mayroon."
        ),
        Service(
            name = "Barangay Scholarship",
            description = "Financial assistance para sa mga deserving college students ng Barangay Pantal.",
            category = "Education",
            schedule = "Application: May - June, Interview: July",
            contactInfo = "Barangay Captain - (075) 123-4571",
            icon = "\uD83C\uDF93",
            status = "Active",
            venue = "Barangay Hall",
            requirements = listOf(
                "Form 138",
                "Good Moral Certificate",
                "Barangay Clearance",
                "Certificate of Indigency"
            ),
            howToAvail = "Makipag-ugnayan sa Barangay Secretary para sa application period, isumite ang school documents, at hintayin ang evaluation ng scholarship committee."
        ),
        Service(
            name = "Senior Citizen Benefits",
            description = "Monthly gathering, libreng check-up, at grocery packages para sa senior citizens.",
            category = "Social",
            schedule = "Every 2nd Wednesday of the Month, 1:00 PM - 4:00 PM",
            contactInfo = "OSCA Officer - (075) 123-4572",
            icon = "\uD83D\uDC65",
            status = "Active",
            venue = "Barangay Hall",
            requirements = listOf(
                "Senior Citizen ID",
                "Barangay Clearance",
                "OSCA Membership"
            ),
            howToAvail = "Magparehistro o magpa-update ng records sa Barangay Hall at makipag-ugnayan para sa schedule ng distributions, gatherings, at health activities."
        ),
        Service(
            name = "PWD Support Program",
            description = "Assistance at support para sa Persons with Disability.",
            category = "Social",
            schedule = "Every 1st Monday of the Month, 9:00 AM - 12:00 PM",
            contactInfo = "PWD Affairs Officer - (075) 123-4573",
            icon = "\uD83E\uDEC2",
            status = "Active",
            venue = "Barangay Hall",
            requirements = listOf(
                "PWD ID",
                "Barangay Clearance",
                "Medical Certificate"
            ),
            howToAvail = "Makipag-ugnayan sa Barangay Social Worker o Barangay Secretary para sa assessment, profiling, at mga available assistance programs."
        ),
        Service(
            name = "Urban Gardening Program",
            description = "Free seeds, gardening materials, at training para sa home gardening.",
            category = "Livelihood",
            schedule = "Every Saturday, 8:00 AM - 10:00 AM",
            contactInfo = "Agriculture Officer - (075) 123-4574",
            icon = "\uD83C\uDF31",
            status = "Active",
            venue = "Barangay Demo Garden",
            requirements = listOf(
                "Barangay Clearance",
                "Proof of Residency",
                "1x1 Picture"
            ),
            howToAvail = "Magparehistro sa Barangay Hall o demo garden coordinator para sa training schedule at distribution ng seeds at gardening materials."
        )
    )

    fun builtIns(): List<Service> = builtInServices

    fun findByName(name: String?): Service? {
        if (name.isNullOrBlank()) return null
        return builtInServices.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    fun enrich(service: Service): Service {
        val builtIn = findByName(service.name) ?: return service
        return builtIn.copy(
            id = service.id.ifBlank { builtIn.id },
            name = service.name.ifBlank { builtIn.name },
            description = service.description.ifBlank { builtIn.description },
            category = service.category ?: builtIn.category,
            schedule = service.schedule ?: builtIn.schedule,
            contactInfo = service.contactInfo ?: builtIn.contactInfo,
            icon = service.icon ?: builtIn.icon,
            status = service.status ?: builtIn.status,
            venue = service.venue ?: builtIn.venue,
            requirements = if (service.requirements.isNotEmpty()) service.requirements else builtIn.requirements,
            howToAvail = service.howToAvail ?: builtIn.howToAvail
        )
    }

    fun mergeWithBuiltIns(services: List<Service>): List<Service> {
        val merged = services
            .filterNot { hiddenDocumentServices.contains(it.name.trim().lowercase()) }
            .map(::enrich)
            .toMutableList()

        builtInServices.forEach { builtIn ->
            val exists = merged.any { it.name.equals(builtIn.name, ignoreCase = true) }
            if (!exists) merged.add(builtIn)
        }

        return merged
    }
}
