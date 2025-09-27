package training.goorm.potholemapapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.s3")
data class S3Properties(
    var bucketName: String = "",
    var region: String = "ap-northeast-2",
    var accessKey: String = "",
    var secretKey: String = "",
    var cloudfrontDomain: String = "",
    var publicAccess: Boolean = true
)

// S3Config는 AWS SDK 의존성 문제로 임시 비활성화
// TODO: AWS SDK 의존성 해결 후 다시 활성화
