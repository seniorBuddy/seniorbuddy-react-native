import React, { useCallback, useEffect, useState } from 'react';
import { SafeAreaView, StyleSheet, PermissionsAndroid, Alert, Platform, Modal, View, Text, Button } from 'react-native';
import { WebView, WebViewMessageEvent } from 'react-native-webview';
import messaging from '@react-native-firebase/messaging';
import { check, request, PERMISSIONS, RESULTS } from 'react-native-permissions';
import Tts from 'react-native-tts';

const App = () => {
  const [fcmToken, setFcmToken] = useState<string | null>(null);
  // FCM 토큰을 가져오는 함수
  const getFcmToken = async () => {
    try {
      const token = await messaging().getToken();
      setFcmToken(token); // 상태에 토큰 저장
    } catch (error) {
      console.error('FCM 토큰을 가져오는 데 실패했습니다:', error);
    }
  };
  
  // 오디오 권한 요청 함수
  const requestAudioPermissions = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
          {
            title: '오디오 권한 요청',
            message: '이 앱이 오디오를 녹음할 수 있도록 권한을 허용해 주세요.',
            buttonNeutral: '나중에',
            buttonNegative: '거부',
            buttonPositive: '허용',
          },
        );
  
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          console.log('오디오 권한이 허용되었습니다.');
        } else {
          console.log('오디오 권한이 거부되었습니다.');
          Alert.alert('오디오 권한이 필요합니다.');
        }
      } catch (err) {
        console.warn(err);
      }
    } else if (Platform.OS === 'ios') {
      const result = await check(PERMISSIONS.IOS.MICROPHONE);
      if (result === RESULTS.GRANTED) {
        console.log('오디오 권한이 이미 허용되었습니다.');
      } else {
        const requestResult = await request(PERMISSIONS.IOS.MICROPHONE);
        if (requestResult === RESULTS.GRANTED) {
          console.log('오디오 권한이 허용되었습니다.');
        } else {
          console.log('오디오 권한이 거부되었습니다.');
          Alert.alert('오디오 권한이 필요합니다.');
        }
      }
    }
  };

  // 연락처 권한 요청 함수
  const requestContactsPermissions = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.READ_CONTACTS,
          {
            title: '연락처 권한 요청',
            message: '이 앱이 연락처에 접근할 수 있도록 권한을 허용해 주세요.',
            buttonNeutral: '나중에',
            buttonNegative: '거부',
            buttonPositive: '허용',
          },
        );

        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          console.log('연락처 권한이 허용되었습니다.');
        } else {
          console.log('연락처 권한이 거부되었습니다.');
          Alert.alert('연락처 권한이 필요합니다.');
        }
      } catch (err) {
        console.warn(err);
      }
    } else if (Platform.OS === 'ios') {
      const result = await check(PERMISSIONS.IOS.CONTACTS);
      if (result === RESULTS.GRANTED) {
        console.log('연락처 권한이 이미 허용되었습니다.');
      } else {
        const requestResult = await request(PERMISSIONS.IOS.CONTACTS);
        if (requestResult === RESULTS.GRANTED) {
          console.log('연락처 권한이 허용되었습니다.');
        } else {
          console.log('연락처 권한이 거부되었습니다.');
          Alert.alert('연락처 권한이 필요합니다.');
        }
      }
    }
  };
  const requestCallPermissions = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.CALL_PHONE,
          {
            title: '전화 권한 요청',
            message: '이 앱이 전화를 걸 수 있도록 권한을 허용해 주세요.',
            buttonNeutral: '나중에',
            buttonNegative: '거부',
            buttonPositive: '허용',
          },
        );
  
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          console.log('전화 권한이 허용되었습니다.');
        } else {
          console.log('전화 권한이 거부되었습니다.');
          Alert.alert('전화 권한이 필요합니다.');
        }
      } catch (err) {
        console.warn(err);
      }
    } else if (Platform.OS === 'ios') {
      // iOS에서의 권한 요청 로직 (필요시 추가)
    }
  };
  const handleMessage = useCallback((event: WebViewMessageEvent) => { // 타입 지정
    const message = event.nativeEvent.data;
    console.log("handleMessage", message);
    
    try {
      const parsedMessage = JSON.parse(message);
      if (parsedMessage.message === "speechSynthesis을 지원하지 않습니다.") { // 메시지 감지
        console.log("감지된 메시지:", parsedMessage);
        Tts.speak(parsedMessage.assistantResponse); // TTS로 assistantResponse 읽기
      }
    } catch (error) {
      console.error("메시지 파싱 오류:", error);
    }
  }, []);

  
  useEffect(() => {
    Tts.setDefaultLanguage('ko-KR'); // 한국어로 설정
    Tts.setDefaultRate(0.5); // 속도 설정
    
    // 알림 권한 요청 (iOS 전용)
    messaging().requestPermission().then(permission => {
      if (permission) {
        console.log('알림 권한이 허용되었습니다.');
        getFcmToken(); // FCM 토큰 가져오기
      } else {
        console.log('알림 권한이 거부되었습니다.');
      }
    });
    
    requestAudioPermissions();
    requestContactsPermissions();
    requestCallPermissions();

  }, []);
  

  return (
    <SafeAreaView style={styles.container}>
      <WebView
        // 웹뷰에서 특정 URL 로드
        source={{ uri: 'http:/192.168.0.82:3000?token=' + (fcmToken ? encodeURIComponent(fcmToken) : '') }}
        style={{ flex: 1 }}
        originWhitelist={['*']}
        mediaPlaybackRequiresUserAction={false}
        onMessage={handleMessage}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

export default App;