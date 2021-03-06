package nl.ecci.hamers.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Nickname(val id: Int,
                    val userId: Int,
                    val nickname: String,
                    val description: String,
                    @SerializedName("created_at")
                    val createdAt: Date)
