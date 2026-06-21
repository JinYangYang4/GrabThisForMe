package com.example.grabthisforme.model.location.data

import android.content.Context
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.example.grabthisforme.model.location.domain.AppLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class AmapLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun getCurrentLocation(): Result<AppLocation> {
        return suspendCancellableCoroutine { continuation ->
            val client = runCatching { AMapLocationClient(context) }
                .getOrElse { error ->
                    continuation.resume(Result.failure(error))
                    return@suspendCancellableCoroutine
                }

            val option = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                isOnceLocation = true
                isOnceLocationLatest = true
                isNeedAddress = true
                isMockEnable = false
                isLocationCacheEnable = true
                httpTimeOut = LOCATION_TIMEOUT_MS
            }

            fun releaseClient() {
                runCatching { client.stopLocation() }
                runCatching { client.onDestroy() }
            }

            client.setLocationOption(option)
            client.setLocationListener { location ->
                if (!continuation.isActive) {
                    releaseClient()
                    return@setLocationListener
                }
                val result = location.toResult()
                releaseClient()
                continuation.resume(result)
            }

            continuation.invokeOnCancellation {
                releaseClient()
            }

            runCatching { client.startLocation() }
                .onFailure { error ->
                    if (continuation.isActive) {
                        releaseClient()
                        continuation.resume(Result.failure(error))
                    }
                }
        }
    }

    private fun AMapLocation?.toResult(): Result<AppLocation> {
        if (this == null) {
            return Result.failure(IllegalStateException("定位失败：未获取到定位结果"))
        }
        if (errorCode != 0) {
            val message = buildString {
                append("定位失败")
                if (!errorInfo.isNullOrBlank()) {
                    append("：")
                    append(errorInfo)
                }
                if (locationDetail?.isNotBlank() == true) {
                    append("（")
                    append(locationDetail)
                    append("）")
                }
            }
            return Result.failure(IllegalStateException(message))
        }
        return Result.success(
            AppLocation(
                latitude = latitude,
                longitude = longitude,
                city = city.orEmpty(),
                district = district.orEmpty(),
                address = address.orEmpty(),
                timestamp = time.takeIf { it > 0L } ?: System.currentTimeMillis()
            )
        )
    }

    companion object {
        private const val LOCATION_TIMEOUT_MS = 10_000L
    }
}
