import React, { useEffect, useState } from 'react';
import { SafeAreaView, StyleSheet, Alert, Modal, View, Text, Button } from 'react-native';
import { WebView } from 'react-native-webview';
import messaging from '@react-native-firebase/messaging';

const App = () => {
  const [fcmToken, setFcmToken] = useState<string | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [messageData, setMessageData] = useState<any>(null);

  // FCM 토큰을 가져오는 함수
  const getFcmToken = async () => {
    try {
      const token = await messaging().getToken();
      setFcmToken(token); // 상태에 토큰 저장
    } catch (error) {
      console.error('FCM 토큰을 가져오는 데 실패했습니다:', error);
    }
  };

  // 포그라운드 상태에서 수신된 메시지를 처리하는 함수
  const handleForegroundMessage = async (remoteMessage: any) => {
    Alert.alert('새로운 알림', JSON.stringify(remoteMessage.notification));
  };

  useEffect(() => {
    // 알림 권한 요청 (iOS 전용)
    messaging().requestPermission().then(permission => {
      if (permission) {
        console.log('알림 권한이 허용되었습니다.');
        getFcmToken(); // FCM 토큰 가져오기
      } else {
        console.log('알림 권한이 거부되었습니다.');
      }
    });

    // 포그라운드에서 메시지를 수신하는 리스너 설정
    const unsubscribe = messaging().onMessage(async remoteMessage => {
      handleForegroundMessage(remoteMessage);
    });

    // 백그라운드 상태에서 메시지를 처리하는 핸들러 설정
    messaging().setBackgroundMessageHandler(async remoteMessage => {
      console.log('[백그라운드 메시지]', remoteMessage);
      setMessageData(remoteMessage);
      setModalVisible(true); // 모달을 띄우기
    });

    // 컴포넌트가 언마운트될 때 리스너 정리
    return unsubscribe;
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <WebView
        // 웹뷰에서 특정 URL 로드
        source={{ uri: 'http://192.168.0.82:3000?token=' + (fcmToken ? encodeURIComponent(fcmToken) : '') }}
        style={{ flex: 1 }}
      />

      {/* 모달 컴포넌트 */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={modalVisible}
        onRequestClose={() => setModalVisible(false)}
      >
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
          <View style={{ width: 300, padding: 20, backgroundColor: 'white', borderRadius: 10 }}>
            <Text>{messageData?.notification?.title}</Text>
            <Text>{messageData?.notification?.body}</Text>
            <Button title="닫기" onPress={() => setModalVisible(false)} />
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

export default App;