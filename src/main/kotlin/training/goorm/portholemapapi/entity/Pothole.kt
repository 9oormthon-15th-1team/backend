package training.goorm.portholemapapi.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "potholes")
class Pothole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val latitude: Double,

    @Column(nullable = false)
    val longitude: Double,

    @Column(nullable = false, length = 500)
    var description: String,

    @Column(name = "image_url", length = 1000)
    var imageUrl: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
