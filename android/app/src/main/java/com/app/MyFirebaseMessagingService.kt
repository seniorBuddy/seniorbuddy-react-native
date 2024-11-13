package com.seniorbuddy.abby


import android.content.Context
import android.media.AudioManager
import android.content.Intent
import android.net.Uri

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 메시지의 data 필드 확인
        remoteMessage.data["action"]?.let { action ->
            when (action) {
                "overlay" -> {
                    // Overlay 서비스 시작
                    val intent = Intent(this, OverlayService::class.java)
                    intent.putExtra("title", remoteMessage.notification?.title)
                    intent.putExtra("body", remoteMessage.notification?.body)
                    startService(intent)
                }
                "device_control" -> {
                    // 디바이스 조작 요청 처리
                    handleDeviceControl(remoteMessage.data)
                }
                else -> {
                    // 처리되지 않은 action에 대한 기본 동작 추가
                }
            }
        }
    }

    private fun handleDeviceControl(data: Map<String, String>) {
        // 예: 폰트 크기 조정, 볼륨 조정, 전화 걸기 등
        val action = data["action"]
        when (action) {
            "increase_font_size" -> {
                // 폰트 크기 증가 로직
            }
            "decrease_font_size" -> {
                // 폰트 크기 감소 로직
            }
            "increase_volume" -> {
                // 볼륨 증가 로직
                adjustVolume(this, true)
            }
            "decrease_volume" -> {
                // 볼륨 감소 로직
                adjustVolume(this, false)
            }
            "make_call" -> {
                val phoneNumber = data["phone_number"]
                phoneNumber?.let { makeCall(this, it) }
            }
            // 그 외 로직 추가할것
        }
    }
}

private fun adjustVolume(context: Context, increase: Boolean) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    if (increase) {
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    } else {
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }
}

private fun makeCall(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL)
    intent.data = Uri.parse("tel:$phoneNumber")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 플래그 추가
    context.startActivity(intent) // context를 사용하여 startActivity 호출
}