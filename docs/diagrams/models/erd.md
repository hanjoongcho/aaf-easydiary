```mermaid
classDiagram
    class ActionLog {
        <<RealmObject>>
        +Int sequence <<PrimaryKey>>
        +String? className
        +String? signature
        +String? key
        +String? value
    }

    class Alarm {
        <<RealmObject>>
        +Int sequence = -1 <<PrimaryKey>>
        +Int timeInMinutes
        +Int days
        +Boolean isEnabled
        +Boolean vibrate
        +String? soundTitle
        +String? soundUri
        +String? label
        +Int workMode
        +Int retryCount
        +Int id 
    }

    class DDay {
        <<RealmObject>>
        +Int sequence = -1 <<PrimaryKey>>
        +Long targetTimeStamp
        +String? title
        +String getDayRemaining(onlyDays: Boolean = true, yearFormat: String = "", dayFormat: String = "")
        +String getOnlyDayRemaining(onlyDays: Boolean = true, yearFormat: String = "", dayFormat: String = "")
        +String getTimeRemaining()
    }

    class Diary {
        <<RealmObject>>
        +Int sequence
        +Int originSequence
        -Int sessionId <<Ignore>>
        +Long currentTimeMillis
        +String? title
        +String? contents
        +String? dateString
        +Int weather
        +RealmList\<PhotoUri\>? photoUris
        +RealmList\<Int\> linkedDiaries
        +String? fontName
        +Float fontSize
        +Boolean isAllDay
        +Boolean isEncrypt
        +String? encryptKeyHash
        +Boolean isSelected
        +Location? location
        +Boolean isHoliday

        +updateDateString()
        +photoUrisWithEncryptionPolicy(): List\<PhotoUri\>?
        +toUiModel(): DiaryUiModel
    }

    class DiarySymbol {
        <<data>>
        +Int sequence
        +String description
    }

    class History {
        <<data>>
        +String historyTag
        +String date
        +String attachedPhotoPath
        +Int sequence
    }

	 class PhotoUri {
        <<RealmObject>>
        +RealmResults\<Diary\>? diary @LinkingObjects("photoUris")
        +String? photoUri
        +String? mimeType
        +Boolean isContentUri()
        +String getFilePath()
        +Boolean isEncrypt()
    }

    class Location {
        <<RealmObject>>
        +String? address
        +Double latitude
        +Double longitude
    }

    Diary "1" --o "*" PhotoUri : photoUris
    Diary "1" --> "0..1" Location : location
    PhotoUri --> Diary : linkingObjects

    
    
````