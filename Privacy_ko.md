## 이지 다이어리 개인정보 보호정책 [(English)](https://github.com/hanjoongcho/aaf-easydiary/blob/master/Privacy.md)
![](https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/app/src/main/res/mipmap-xxhdpi/ic_launcher.png)   

v2022.08.16-01   
이지 다이어리를 개발하고 배포하는 Awesome Application Factory(이하"AAF")는 영리를 목적으로 하는 법인 또는 개인 사업자가 아닙니다.  
오프소스 라이브러리를 매쉬업하여 사용자가 광고없이 무료로 편리한 애프리케이션을 사용 할 수 있도록 애플리케이션을 개발하고 배포하는 것을 목적으로 합니다.  
따라서 AAF의 애플리케이션은 사용자의 데이터 및 개인정보를 저장하기 위해 별도의 서버를 운영하지 않으며, 불필요하게 사용자의 개인정보를 취급하지도 않습니다. 만약 사용중인 애플리케이션에 대한 백업 및 복구를 사용자가 원하는경우 사용자의 동의하에 Google의 OAuth인증을 통하여 사용자 개인의 Google Drive에 애플리케이션 데이터를 저장하고 이후 백업 요청 시 사용합니다.  
백업 데이터는 사용자가 Google Drive에서 별도로 공유설정을 진행하지 않는한 외부로 공개되지 않습니다.   

---

### ☑️ 개인정보 보호정책  
이 개인 정보 취급 방침 문서는 개인 정보 또는 데이터가 수집, 사용 및 공개된 때, 이 소프트웨어를 사용할 때 무엇을 어떻게, 왜, 그리고 왜 공개했는지 사용자에게 관련된 사항을 명확히 진술하고 제공하려는 목적으로 작성되었습니다.  

이 정책 문서의 목적은 공개적으로 개발자가 수집하는 정보를 명시하여 개발자와 소프트웨어 사용자 간에 공개 환경을 제공하는 것입니다.  
이 소프트웨어를 사용하는 것은 이지 다이어리의 모든 이용 약관에 동의한다는 의미입니다. 이 정책은 이 소프트웨어에만 적용되며 개발자는 이 소프트웨어가 링크되거나 개발자가 운영하지 않는 제3자 앱의 콘텐츠 또는 개인 정보 보호 관행에 대해 책임을 지지 않습니다.  
그러므로 이 소프트웨어에 연결될 수 있는 제3자 서비스의 개인 정보 보호 정책을 읽는 것이 좋습니다.  

---

### ☑️ 요청되는 기본권한과 요청 이유
Normal Permissions은 사용자의 추가 승인없이 애플리케이션이 설치되면 자동으로 부여되는 Permission입니다.

#### SET_ALARM
* Allows an application to broadcast an Intent to set an alarm for the user.   
* 이지 다이어리 스케줄러 기능을 사용하는 경우 사용자가 설정한 시간에 맞춰 Notification을 제공하고 예약된 작업을 진행하기 위해 필요한 권한입니다.
#### INTERNET
* Allows applications to open network sockets.
* 사용자가 매뉴얼, 자주 묻는 질문, 개인정보 보호정책등을 열람하는 경우 필요한 권한입니다.
#### FOREGROUND_SERVICE
* Allows a regular application to use Service.startForeground.
* 전체데이터를 백업하거나 복구할때 진행 상태를 알림창에서 제공하기 위해 필요한 권한입니다.
#### RECEIVE_BOOT_COMPLETED
* Allows an application to receive the Intent.ACTION_BOOT_COMPLETED that is broadcast after the system finishes booting. If you don't request this permission, you will not receive the broadcast at that time. Though holding this permission does not have any security implications, it can have a negative impact on the user experience by increasing the amount of time it takes the system to start and allowing applications to have themselves running without the user being aware of them. As such, you must explicitly declare your use of this facility to make that visible to the user.
* 디바이스가 다시 시작되었을때 스케줄러에 설정된 작업을 재등록 하기위해 필요한 권한입니다.
#### VIBRATE
* Allows access to the vibrator.
* 스케줄러로 예약된 작업이 시작되거나 완료되었을때 사용자에게 진동알림을 주기위해 필요한 권한입니다.
#### SCHEDULE_EXACT_ALARM
* Allows applications to use exact alarm APIs.
* 이지 다이어리 스케줄러 기능을 사용하는 경우 사용자가 설정한 시간에 맞춰 Notification을 제공하고 예약된 작업을 진행하기 위해 필요한 권한입니다.
#### USE_BIOMETRIC
* Allows an app to use device supported biometric modalities.
* 사용자가 지문 잠금 기능을 사용할때 필요한 권한입니다.
#### USE_FINGERPRINT
* Allows an app to use fingerprint hardware.
* 사용자가 지문 잠금 기능을 사용할때 필요한 권한입니다.

---

### ☑️ 요청되는 위험권한 요청 이유
Dangerous Permissions은 사용자의 추가 승인을 받은 경우에만 부여되는 Permission입니다.
#### READ_EXTERNAL_STORAGE
* Allows an application to read from external storage.
* 사용자의 저장소에 있는 폰트파일이나, 사진등을 읽기 위해 필요한 권한입니다. 
#### WRITE_EXTERNAL_STORAGE
* Allows an application to write to external storage.
* 사용자의 저장소에 포스트카드, 백업파일등을 저장하기 위해 필요한 권한입니다.
#### ACCESS_FINE_LOCATION
* Allows an app to access precise location. Alternatively, you might want ACCESS_COARSE_LOCATION.
* 사용자가 다이어리 작성위치 저장 옵션을 활성화 하는 경우 다이어리가 작성된 위치의 정확한 주소정보를 취득하기 위해 필요한 권한입니다.
#### ACCESS_COARSE_LOCATION
* Allows an app to access approximate location. Alternatively, you might want ACCESS_FINE_LOCATION.
* 사용자가 다이어리 작성위치 저장 옵션을 활성화 하는 경우 다이어리가 작성된 위치의 근사치 주소정보를 취득하기 위해 필요한 권한입니다.

---

### ☑️ 정보의 수집 및 사용  
이지 다이어리는 보다 향상된 기능 제공을 위해 소프트웨어가 정상적으로 동작이 가능하도록 권한을 요청하고 취득한 권한을 이용하여 디바이스의 저장소에 접근 하거나 위치정보를 취득 할 수 있습니다.   
이렇게 취득된 정보는 사용자가 작성한 콘텐츠와 함께 사용자의 디바이스 저장소에 저장될 수 있지만 개발자가 열람하거나 별도의 서버로 전송되지 않습니다.
사용자가 Google의 OAuth인증을 통하여 사용자 개인의 Google Drive를 연결하여 다이어리 데이터를 개인의 Google Drive에 저장하더라도 개발자는 해당 정보를 열람 할 수 없습니다.

---

### ☑️ 수집하는 데이터의 종류

__개인정보__  
* 개발자는 소프트웨어를 사용하는 동안 개인을 식별할 수 있는 데이터를 수집하지 않습니다.

__위치정보__  
* 사용자가 위치정보 사용 옵션을 활성화 하는경우 다이어리 작성 시 위치정보가 작성된 콘텐츠와 함께 저장됩니다.

__네트워크 데이터의 사용__  
* 이지 다이어리는 기본적으로 데이터를 사용하지 않지만 다음의 경우에는 데이터를 사용합니다.  
  1. 사용자가 구글 드라이브를 이용하여 백업 또는 복구 기능을 실행하는 경우  
  2. 사용자가 다이어리 포스트카드를 공유하는 경우  
  3. 사용자가 매뉴얼, 자주 묻는 질문, 개인정보 보호정책등을 열람하는 경우  

__디바이스 데이터의 사용__  
* 저장소 접근 권한이 활성화되면 개발자가 아닌 소프트웨어가 다음과 같은 권한을 갖게 됩니다.  
  1. SD 카드의 내용 읽기  
  2. SD 카드의 내용 수정 또는 삭제  

---

### ☑️ 데이터 보안

데이터의 보안은 중요하지만 인터넷을 통한 전송 방법이나 전자 저장 방법이 100% 안전하지 않으므로 개발자는 절대적인 보안을 보장하지 않습니다.

---


### ☑️ 어린이 개인정보 보호

이 소프트웨어는 일반적인 용도로 사용되며 해당 지역을 규제하는 법률에 달리 명시된 경우를 제외하고는 어린이의 사용에 대한 제한이 없습니다.

---

### ☑️ 다른 사이트 링크

이 소프트웨어는 사용자가 백업 및 복구 기능 사용을 위해 구글 드라이브 접근 기능을 부여하는 경우를 제외하고 독립적으로 동작하며 다른 서비스 제공 업체에 대한 링크는 없습니다.  
 
---

### ☑️ 이 개인정보 보호정책의 변경  
개발자는 사전 통보없이 언제든지 이 문서를 수정할 권리를 보유합니다.  
이 개인정보 보호정책은 소프트웨어의 기능이 추가로 개발되고 개선 될 때 수시로 업데이트 될 수 있으며 현재 정책을 무효화 할 수있는 불균형을 포함 할 수 있습니다. 이러한 변경 사항이있을 때마다 이 문서를 확인하기 위해 노력하십시오.

---
      
### ☑️ 문의 및 개선요청 

개인 정보 보호 정책과 관련된 추가 문의 사항은 이메일(hanjoongcho@gmail.com)을 이용하여 개발자에게 문의하시기 바랍니다. Github 코드 저장소에 [이슈](https://github.com/hanjoongcho/aaf-easydiary/issues/new)를 등록하는 방법으로도 개발자에게 피드백을 받으실 수도 있습니다.  

개발자에 대한 프로파일은 [여기](https://github.com/hanjoongcho)서 확인하실 수 있습니다.
