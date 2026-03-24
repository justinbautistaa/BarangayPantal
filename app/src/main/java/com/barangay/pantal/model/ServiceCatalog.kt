package com.barangay.pantal.model

object ServiceCatalog {

    private val builtInServices = listOf(
        Service(
            name = "TUPAD",
            description = "Tulong Panghanapbuhay sa Ating Disadvantaged/Displaced Workers emergency employment program for underemployed or displaced workers.",
            category = "Employment",
            schedule = "Quarterly (March, June, September, December)",
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
            schedule = "Monday to Friday, 8:00 AM - 5:00 PM",
            venue = "Barangay Health Center",
            requirements = listOf(
                "Valid ID ng magulang o guardian",
                "Birth certificate ng bata",
                "Barangay resident record"
            ),
            howToAvail = "Makipag-ugnayan sa Barangay Health Center o Barangay Secretary para sa schedule, screening, at enrollment ng bata sa feeding sessions."
        ),
        Service(
            name = "Barangay Sports League",
            description = "Basketball, volleyball, at iba pang sports tournament para sa kabataan ng barangay.",
            category = "Youth",
            schedule = "Monday to Friday, 8:00 AM - 5:00 PM",
            venue = "Barangay Covered Court",
            requirements = listOf(
                "Valid ID",
                "Barangay residency proof",
                "Parent consent for minors"
            ),
            howToAvail = "Magparehistro sa Barangay Hall o sa assigned sports coordinator at hintayin ang anunsyo ng tryouts, bracket, at game schedule."
        ),
        Service(
            name = "Medical Mission",
            description = "Libreng check-up, laboratory tests, at gamot para sa lahat ng residente.",
            category = "Health",
            schedule = "Monday to Friday, 8:00 AM - 5:00 PM",
            venue = "Barangay Health Center",
            requirements = listOf(
                "Valid ID",
                "Barangay residency proof",
                "Medical records if available"
            ),
            howToAvail = "Pumunta sa Barangay Health Center o Barangay Hall para sa schedule ng medical mission at dalhin ang kinakailangang records kung mayroon."
        ),
        Service(
            name = "Barangay Scholarship",
            description = "Financial assistance para sa mga deserving college students ng Barangay Pantal.",
            category = "Education",
            schedule = "Monday to Friday, 8:00 AM - 5:00 PM",
            venue = "Barangay Hall",
            requirements = listOf(
                "Valid ID",
                "Certificate of registration",
                "Grades or transcript of records",
                "Certificate of indigency"
            ),
            howToAvail = "Makipag-ugnayan sa Barangay Secretary para sa application period, isumite ang school documents, at hintayin ang evaluation ng scholarship committee."
        ),
        Service(
            name = "Senior Citizen Benefits",
            description = "Monthly gathering, libreng check-up, at grocery packages para sa senior citizens.",
            category = "Social",
            schedule = "Monday to Friday, 8:00 AM - 5:00 PM",
            venue = "Barangay Hall",
            requirements = listOf(
                "Senior Citizen ID o valid ID",
                "Barangay residency proof"
            ),
            howToAvail = "Magparehistro o magpa-update ng records sa Barangay Hall at makipag-ugnayan para sa schedule ng distributions, gatherings, at health activities."
        ),
        Service(
            name = "PWD Support Program",
            description = "Assistance at support para sa Persons with Disability.",
            category = "Social",
            schedule = "Monday to Friday, 8:00 AM - 5:00 PM",
            venue = "Barangay Hall",
            requirements = listOf(
                "PWD ID o medical certificate",
                "Valid ID",
                "Barangay residency proof"
            ),
            howToAvail = "Makipag-ugnayan sa Barangay Social Worker o Barangay Secretary para sa assessment, profiling, at mga available assistance programs."
        ),
        Service(
            name = "Urban Gardening Program",
            description = "Free seeds, gardening materials, at training para sa home gardening.",
            category = "Livelihood",
            schedule = "Monday to Friday, 8:00 AM - 5:00 PM",
            venue = "Barangay Demo Garden",
            requirements = listOf(
                "Valid ID",
                "Barangay residency proof",
                "Interest form or registration"
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
            venue = service.venue ?: builtIn.venue,
            requirements = if (service.requirements.isNotEmpty()) service.requirements else builtIn.requirements,
            howToAvail = service.howToAvail ?: builtIn.howToAvail
        )
    }

    fun mergeWithBuiltIns(services: List<Service>): List<Service> {
        val merged = services.map(::enrich).toMutableList()

        builtInServices.forEach { builtIn ->
            val exists = merged.any { it.name.equals(builtIn.name, ignoreCase = true) }
            if (!exists) merged.add(builtIn)
        }

        return merged
    }
}
