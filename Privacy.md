## Easy Diary Privacy policy [(한국어)](https://github.com/hanjoongcho/aaf-easydiary/blob/master/Privacy_ko.md)
![](https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/app/src/main/res/mipmap-xxhdpi/ic_launcher.png)

v2023.07.29-01   
The Awesome Application Factory ("AAF"), which develops and distributes Easy Diary, is not a for-profit corporation or sole proprietorship.
It aims to develop and distribute applications by mashup of open source libraries so that users can use convenient applications free of charge and without advertisements.
Therefore, AAF's application does not operate a separate server to store user's data and personal information, and does not handle user's personal information unnecessarily. If the user wants to back up and restore the application in use, the application data is stored in the user's personal Google Drive through Google's OAuth authentication with the user's consent and used when requesting a backup afterwards.

Backup data is not disclosed to the outside unless the user sets up sharing in Google Drive.

### ☑️ Privacy Policy

This privacy policy document is to clearly state, and provide information on what, how, and why personal data or information is collected, used, and disclosed, when this software is utilized, and the choices you have associated with that data. 

The objective of this policy document is to provide an open environment between developer and users of this software by openly stating what information the developer collects or not. By utilizing this software, it means you accept all the terms and conditions of the developer peculiar to only Easy Diary software. This Policy applies to this software only, and the developer would not be held responsible for the content or privacy practices on any third party app not operated by the developer to which this software links or that links to this software, therefore it is advisable to read up the privacy policies of third party services that may be linked to this software. 

### ☑️ Normal permission requested and the reason for the request
Normal Permissions are Permissions that are automatically granted when an application is installed without the user's additional approval.

#### SET_ALARM
* Allows an application to broadcast an Intent to set an alarm for the user.   
* When using the Easy Diary Scheduler function, this permission is required to provide a notification at the time set by the user and proceed with the scheduled task.
 
#### INTERNET
* Allows applications to open network sockets.
* This permission is required when the user reads the manual, frequently asked questions, and privacy policy.
  
#### FOREGROUND_SERVICE
* Allows a regular application to use Service.startForeground.
* This permission is required to provide progress status in the notification window when backing up or restoring all data.
  
#### RECEIVE_BOOT_COMPLETED
* Allows an application to receive the Intent.ACTION_BOOT_COMPLETED that is broadcast after the system finishes booting. If you don't request this permission, you will not receive the broadcast at that time. Though holding this permission does not have any security implications, it can have a negative impact on the user experience by increasing the amount of time it takes the system to start and allowing applications to have themselves running without the user being aware of them. As such, you must explicitly declare your use of this facility to make that visible to the user.
* This permission is required to re-register the task set in the scheduler when the device is restarted.
  
#### VIBRATE
* Allows access to the vibrator.
* This permission is required to give a vibration notification to the user when a task scheduled with the scheduler is started or completed.
  
#### SCHEDULE_EXACT_ALARM
* Allows applications to use exact alarm APIs.
* When using the Easy Diary Scheduler function, this permission is required to provide a notification at the time set by the user and proceed with the scheduled task.
  
#### USE_BIOMETRIC
* Allows an app to use device supported biometric modalities.
* This permission is required when the user uses the fingerprint lock function.
  
#### USE_FINGERPRINT
* Allows an app to use fingerprint hardware.
* This permission is required when the user uses the fingerprint lock function.

### ☑️ Dangerous permission requested and the reason for the request

Dangerous Permissions은 사용자의 추가 승인을 받은 경우에만 부여되는 Permission입니다.  

#### READ_EXTERNAL_STORAGE
* Allows an application to read from external storage.
* This permission is necessary to read font files or photos in the user's storage.
  
#### WRITE_EXTERNAL_STORAGE
* Allows an application to write to external storage.
* This permission is required to save post cards, backup files, etc. in the user's storage.
  
#### ACCESS_FINE_LOCATION
* Allows an app to access precise location. Alternatively, you might want ACCESS_COARSE_LOCATION.
* If the user activates the option to save the diary writing location, it is a necessary permission to acquire the exact address information of the diary writing location.
  
#### ACCESS_COARSE_LOCATION
* Allows an app to access approximate location. Alternatively, you might want ACCESS_FINE_LOCATION.
* If the user activates the option to save the diary writing location, it is a necessary permission to acquire the approximate address information of the diary writing location.  

### ☑️ Information Collection And Use
In order to provide more advanced functions, Easy Diary may request permission to allow the software to operate normally, and use the acquired permission to access the storage of the device or acquire location information.
The information obtained in this way may be stored on the user's device storage along with the content created by the user, but is not viewed by the developer or transmitted to a separate server.
Even if the user connects the user's personal Google Drive through Google's OAuth authentication and stores the diary data in the personal Google Drive, the developer cannot view the information.  

### ☑️ Types of Data Collected

#### Personal Data

__Privacy__  
* Developers do not collect personally identifiable data while using the software.

__Location__  
* If the user activates the option to use location information, the location information is saved together with the written content when writing the diary.

__Use of network data__  
* Easy Diary does not use data by default, but does use data in the following cases:
  1. When a user performs a backup or restore function using Google Drive
  2. When users share diary postcards
  3. When the user reads the manual, frequently asked questions, privacy policy, etc.

__Use of device data__  
* When repository access rights are enabled, non-developer software will have the following rights:
  1. Read contents of SD card  
  2. Modify or delete the contents of the SD card  

---

## Security Of Data

The security of your data is important, but remember that no method of transmission over the Internet, or method of electronic storage is 100% secure therefore the developer doesn't guarantee its absolute security. 

---

## Children's Privacy

This software is intended for general use and has no restrictions on utilization by children except otherwise stated in a law governing that locale. 

---

## Links To Other Sites

Easy Diary does not contain links to any other service provider. The software works independently on its own.
 
---

## Changes To This Privacy Policy

The developer reserves the right to make modifications to the policy statement at any time without any notice. This privacy policy may be updated from time to time as the functionalities of the software are further developed and improved and may likely contain disparities that will nullify this current policy. Endeavour to check this document occasionally for any of such changes.

---

## Detailed information on Google API integration function
* EasyDiary can access the user's Profile, Googel Drive, and Google Calendar data using the Google OAuth API if the user agrees.

|API|Request API details|Information displayed to users|Description|
|---|---|---|---|
| OAuth | openid  | Google에서 내 개인 정보를 나와 연결 |  |  
| OAuth | /auth/userinfo.profile  | 개인정보(공개로 설정한 개인정보 포함) 보기  | Display the user's account and profile picture in the diary |  
| Google Drive API | /auth/drive.appdata  | Google Drive의 앱 구성 데이터 확인, 생성, 삭제 | Search diary backup file |  
| Google Drive API | /auth/drive.file  | 앱에서 사용하는 특정 Google Drive 파일에 한해 확인, 수정, 생성, 삭제 | Upload or download diary backup file |  
| Google Calendar API | /auth/calendar.calendars.readonly  | 액세스 권한이 있는 Google Calendar의 제목, 설명, 기본 시간대 및 기타 속성 조회 | Calendar list lookup |  
| Google Calendar API | /auth/calendar.events.readonly  | 모든 캘린더의 일정 보기 | Automatic diary creation using calendar events |  

* EasyDiary complies with the [Google API Service User Data Policy](https://developers.google.com/terms/api-services-user-data-policy) in using Google API.  
* Example of user consent for Google API integration  
  * Google Drive
 ![image](https://github.com/AAFactory/aafactory.github.io/assets/7098851/2ae20a0a-218b-45cf-9562-8b4fe54ba900)
   
  * Goolge Calendar
 ![image](https://github.com/AAFactory/aafactory.github.io/assets/7098851/39531151-13fc-4195-8160-0c87d83dedb1)


---
      
## Contact 

For further inquiries, regarding this privacy policy, you can contact the developer via email. Send an email to hanjoongcho@gmail.com. The developer can also be contacted by opening an [issue](https://github.com/hanjoongcho/aaf-easydiary/issues/new) on the repository.

You can also check out the developer's profile [here](https://github.com/hanjoongcho).
