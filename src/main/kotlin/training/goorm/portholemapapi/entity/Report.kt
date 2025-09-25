package training.goorm.portholemapapi.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "report")
@EntityListeners(AuditingEntityListener::class)
class Report(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val latitude: Double,

    @Column(nullable = false)
    val longitude: Double,

    @Column(nullable = false, length = 1000)
    val address: String,

    @ElementCollection
    @CollectionTable(
        name = "report_images",
        joinColumns = [JoinColumn(name = "report_id")]
    )
    @Column(name = "image_url", length = 1000)
    val imageUrls: List<String> = emptyList(),

    @Column(length = 2000)
    val description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pothole_id")
    val pothole: Pothole,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
