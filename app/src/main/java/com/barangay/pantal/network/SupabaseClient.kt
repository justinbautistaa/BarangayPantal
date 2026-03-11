package com.barangay.pantal.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://pjejgvuhxlubtuuriiqz.supabase.co",
        supabaseKey = "sb_publishable_a7Q1uphu5ZZDB_mQox0JmQ_l3qnzYeM"
    ) {
        install(Auth)
        install(Postgrest)
    }
}
