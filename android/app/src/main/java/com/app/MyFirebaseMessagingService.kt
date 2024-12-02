package com.seniorbuddy.abby

import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import android.Manifest
import android.content.pm.PackageManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMsgService" // 로그 태그

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 메시지 수신 시 로그 출력
        logMessage(remoteMessage)

        // 권한 요청
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 허용되지 않은 경우
            Log.d(TAG, "연락처 권한이 필요합니다.")
            // 사용자에게 권한이 거부되었다는 메시지 표시
            // 예: Toast 또는 Log로 알림
            return
        } else {
            handleRemoteMessage(remoteMessage) // 권한이 있을 경우 메시지 처리
        }
    }

    private fun handleRemoteMessage(remoteMessage: RemoteMessage) {
        // 메세지 종류별 처리
        remoteMessage.data.let { data ->
            when (data["type"]) {
                "increaseFontSize" -> increaseFontSize()
                "decreaseFontSize" -> decreaseFontSize()
                "sendMessage" -> sendMessage(data["title"], data["body"])
                "launchSpecificApp" -> launchSpecificApp(data["title"], data["body"])
                "showOverlay" -> showOverlay(data["title"], data["body"])
                "callContact" -> callContact(data["title"], data["body"])
            }
        }
    }


    private fun getCurrentFontSize(): Int {
        return resources.configuration.fontScale.toInt() // 현재 폰트 스케일 가져오기
    }

    private fun setFontSize(size: Int) {
        // 시스템 폰트 사이즈를 설정하는 로직
        val config = resources.configuration
        config.fontScale = size.toFloat()
        resources.updateConfiguration(config, resources.displayMetrics)
        Log.d(TAG, "Font size set to $size")
    }

    private fun increaseFontSize() {
        val currentSize = getCurrentFontSize()
        val newSize = currentSize + 1
        setFontSize(newSize)
        Log.d(TAG, "Font size increased to $newSize")
    }

    private fun decreaseFontSize() {
        val currentSize = getCurrentFontSize()
        val newSize = currentSize - 1
        setFontSize(newSize)
    }



    private fun sendMessage(contact_name: String?, content: String?) {
        if (contact_name != null && content != null) {
            val number = findMostSimilarContact(contact_name) // 연락처 검색
            if (number != null) {
                // 문자 전송 인텐트 생성
                val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:$number") // 수신자 번호
                    putExtra("sms_body", content) // 메시지 내용
                    // FLAG_ACTIVITY_NEW_TASK 플래그 추가
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(smsIntent) // 문자 전송 인텐트 시작
                Log.d(TAG, "메시지 전송: $content to $contact_name")
            } else {
                Log.d(TAG, "연락처를 찾을 수 없습니다: $contact_name") // 연락처를 찾을 수 없는 경우 로그 출력
            }
        } else {
            Log.d(TAG, "연락처 이름 또는 내용이 비어 있습니다.") // 비어 있는 경우 로그 출력
        }
    }

    private fun callContact(contact_name: String?, content: String?) {
        if (contact_name != null) {
            val number = findMostSimilarContact(contact_name)
            if (number != null) {
                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(callIntent)
                Log.d(TAG, "전화 걸기: $contact_name")
            } else {
                Log.d(TAG, "연락처를 찾을 수 없습니다: $contact_name")
            }
        } else {
            Log.d(TAG, "연락처 이름이 비어 있습니다.") // 비어 있는 경우 로그 출력
        }
    }

    private fun launchSpecificApp(title: String?, body: String?) {
        val packageName = title ?: return // 패키지명이 null일 경우 함수 종료
        val activityName = body ?: return // 액티비티명이 null일 경우 함수 종료

        if (isAppInstalled(packageName)) {
            if (activityName.isNotEmpty() && isActivityAvailable(packageName, activityName)) {
                // 액티비티가 존재할 경우
                val intent = Intent().apply {
                    setClassName(packageName, activityName)
                }
                startActivity(intent)
            } else {
                Log.d(TAG, "액티비티를 찾을 수 없습니다: $activityName")
            }
        } else {
            Log.d(TAG, "앱이 설치되어 있지 않습니다: $packageName")
        }
    }
    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    private fun isActivityAvailable(packageName: String, activityName: String): Boolean {
        return try {
            val intent = Intent().apply {
                setClassName(packageName, activityName)
            }
            intent.resolveActivity(packageManager) != null
        } catch (e: Exception) {
            false
        }
    }

    private fun showOverlay(title: String?, body: String?) {
        Log.d(TAG, "Overlay added with title: $title and body: $body")
        val overlayIntent = Intent(this, OverlayService::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
        }
        startService(overlayIntent)
    }

    private fun logMessage(remoteMessage: RemoteMessage) {
        // FCM 메시지 내용 로그 출력
        Log.d(TAG, "From: ${remoteMessage.from}")
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Title: ${it.title}")
            Log.d(TAG, "Notification Body: ${it.body}")
        }
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }
    }
    private fun findMostSimilarContact(content: String): String? {
        val contactsList = getContacts() // 연락처 목록 가져오기
        var mostSimilarContact: String? = null
        var highestSimilarity = 0.0

        for (contact in contactsList) {
            val name = contact["name"] ?: ""
            val similarity = calculateSimilarity(content, name)
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity
                mostSimilarContact = contact["number"]
            }
        }
        return mostSimilarContact
    }

    private fun getContacts(): List<Map<String, String>> {
        // 연락처 반환 로직
        val contactsList = mutableListOf<Map<String, String>>()
        
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)

                // 연락처 정보를 맵에 저장
                contactsList.add(mapOf("name" to name, "number" to number))
            }
        }
        return contactsList
    }

    private fun calculateSimilarity(str1: String, str2: String): Double {
        // 간단한 유사도 계산 로직 (예: Jaro-Winkler 또는 Levenshtein 거리)
        // 여기서는 간단한 예시로 두 문자열의 길이를 비교합니다.
        val maxLength = Math.max(str1.length, str2.length)
        val distance = levenshteinDistance(str1, str2)
        return 1.0 - (distance.toDouble() / maxLength)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[s1.length][s2.length]
    }
}