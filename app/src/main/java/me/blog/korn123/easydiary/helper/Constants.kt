package me.blog.korn123.easydiary.helper

import android.Manifest

/**
 * Created by CHO HANJOONG on 2018-02-09.
 */

/***************************************************************************************************
 *   WorkManager
 *
 ***************************************************************************************************/
const val WORK_MANAGER_BACKUP = "work_manager_backup"
const val WORK_MANAGER_RECOVERY = "work_manager_recovery"


/***************************************************************************************************
 *   Diary Symbols
 *
 ***************************************************************************************************/
const val SYMBOL_SELECT_ALL = 9999

//  Weather 0 ~ 39
const val WEATHER_SUNNY = 1
const val WEATHER_CLOUD_AND_SUN = 2
const val WEATHER_RAIN_DROPS = 3
const val WEATHER_BOLT = 4
const val WEATHER_SNOWING = 5
const val WEATHER_RAINBOW = 6
const val WEATHER_UMBRELLA = 7
const val WEATHER_STARS = 8
const val WEATHER_MOON = 9
const val WEATHER_NIGHT_RAIN = 10
const val WEATHER_TEMPERATURE_L = 11
const val WEATHER_TEMPERATURE_M = 12
const val WEATHER_TEMPERATURE_H = 13
const val WEATHER_DUST = 14
const val WEATHER_DUST_STORM = 15

// Daily 40 ~ 99
// Symbols 80 to 83 are separated into task categories and managed
const val DAILY_WORKING = 41
const val DAILY_WALLET = 42
const val DAILY_VITAMINS = 43
const val DAILY_GAME_PAD = 44
const val DAILY_SHIRT = 45
const val DAILY_GARBAGE = 46
const val DAILY_TIE = 47
const val DAILY_TICKET = 48
const val DAILY_LIKE = 49
const val DAILY_STUDY = 50
const val DAILY_SLEEP = 51
const val DAILY_SHOPPING_CART = 52
const val DAILY_REPAIR = 53
const val DAILY_LIST = 54
const val DAILY_PET = 55
const val DAILY_FATHERHOOD = 56
const val DAILY_COFFEE = 57
const val DAILY_EAT = 58
const val DAILY_ACTIVITY = 59
const val DAILY_HURRY = 60
const val DAILY_COOKING = 61
const val DAILY_CLEANING = 62
const val DAILY_SHOPPING_BAG = 63
const val DAILY_DUMBBELL = 64
const val DAILY_DISCUSSION = 65
const val DAILY_DIET = 66
const val DAILY_NO_ALCOHOL = 67
const val DAILY_WAKE_UP = 68
const val DAILY_SCALE = 69
const val DAILY_TAKE_AWAY = 70
const val DAILY_CAR_WASH = 71
const val DAILY_CAR_WASH_2 = 72
const val DAILY_SHOWER = 73
const val DAILY_ONLINE_SHOP = 74
const val DAILY_DRIVING = 75
const val DAILY_CAR_REPAIR = 76
const val DAILY_TOOL_BOX = 77
const val DAILY_TRAIN = 78
const val DAILY_BUS = 79
const val DAILY_RECIPE = 84
const val DAILY_STOCK = 85
const val DAILY_CONTRACT = 86
const val DAILY_PIN_CODE = 87

// Tasks 80 ~ 83
const val DAILY_TODO = 80
const val DAILY_DOING = 81
const val DAILY_DONE = 82
const val DAILY_CANCEL = 83

// Emotion 100 ~ 199
const val EMOTION_HAPPY = 100
const val EMOTION_LAUGHING = 101
const val EMOTION_CRYING = 102
const val EMOTION_ANGRY = 103
const val EMOTION_TONGUE = 104
const val EMOTION_ANGRY_1 = 105
const val EMOTION_WINK = 106
const val EMOTION_DISAPPOINTED = 107
const val EMOTION_SAD = 108
const val EMOTION_EMBARRASSED = 109
const val EMOTION_THINKING = 110
const val EMOTION_SICK = 111
const val EMOTION_SECRET = 112
const val EMOTION_SLEEPING = 113
const val EMOTION_RICH = 114
const val EMOTION_DEVIL = 115
const val EMOTION_SKULL = 116
const val EMOTION_POO = 117
const val EMOTION_ALIEN = 118
const val EMOTION_SURPRISED = 119
const val EMOTION_LAUGHING_1 = 120
const val EMOTION_INJURED = 121
const val EMOTION_HAPPY_1 = 122
const val EMOTION_DEMON = 123
const val EMOTION_IN_LOVE = 124
const val EMOTION_TONGUE_1 = 125
const val EMOTION_CALM = 126
const val EMOTION_ANGRY_2 = 127
const val EMOTION_CRY = 128
const val EMOTION_HAPPY_2 = 129
const val EMOTION_LOVE = 130
const val EMOTION_HAPPY_3 = 131
const val EMOTION_SLEEP = 132
const val EMOTION_SMILE = 133
const val EMOTION_SUFFER = 134
const val EMOTION_EXCUSE = 135
const val EMOTION_HAPPY_4 = 136
const val EMOTION_HARMFUL = 137
const val EMOTION_INCOMPREHENSION = 138
const val EMOTION_SAD_1 = 139
const val EMOTION_IDEA = 140
const val EMOTION_CONFUSION = 141
const val EMOTION_MEMORY = 142
const val EMOTION_OBSERVATION = 143
const val EMOTION_SMILE2 = 144
const val EMOTION_AMAZE = 145
const val EMOTION_DELIGHT = 146
const val EMOTION_EMBARRASSED2 = 147

// Landscape & Buildings 200 ~ 249
const val LANDSCAPE_BEACH = 200
const val LANDSCAPE_BRIDGE = 201
const val LANDSCAPE_CAPE = 202
const val LANDSCAPE_CASTLE = 203
const val LANDSCAPE_CITYSCAPE = 204
const val LANDSCAPE_DESERT = 205
const val LANDSCAPE_DESERT_1 = 206
const val LANDSCAPE_FIELDS = 207
const val LANDSCAPE_FIELDS_1 = 208
const val LANDSCAPE_FOREST = 209
const val BUILDING_APARTMENT = 210
const val BUILDING_HOSPITAL = 211
const val BUILDING_MUSEUM = 212
const val BUILDING_TOWN_HALL = 213
const val BUILDING_TRAIN_STATION = 214

// Food and Drink 250 ~ 299
const val FOOD_BURGER = 250
const val FOOD_SANDWICH = 251
const val FOOD_STEAK = 252
const val FOOD_PIE = 253
const val FOOD_WATER = 254
const val FOOD_MILK = 255
const val FOOD_SALAD = 256
const val FOOD_BAGUETTE = 257
const val FOOD_BEVERAGE03 = 258
const val FOOD_BEVERAGE04 = 259
const val FOOD_CAKE = 260
const val FOOD_BEVERAGE05 = 261
const val FOOD_FEEDING_BOTTLE = 262
const val FOOD_BEVERAGE01 = 263
const val FOOD_SUSHI = 264
const val FOOD_BEVERAGE06 = 265
const val FOOD_RICE = 266
const val FOOD_FRIED_CHICKEN = 267
const val FOOD_CAKE_1 = 268
const val FOOD_BEVERAGE02 = 269
const val FOOD_SOUP = 270
const val FOOD_CURRY = 271
const val FOOD_NOODLES = 272
const val FOOD_FRIED_RICE = 273
const val FOOD_GRILL = 274
const val FOOD_PIZZA = 275

// Leisure 300 ~ 349
const val LEISURE_PICNIC = 300
const val LEISURE_MOVIE = 301
const val LEISURE_HIKING = 302
const val LEISURE_READING = 303
const val LEISURE_NAP = 304
const val LEISURE_PARK = 305
const val LEISURE_FISHING = 306
const val LEISURE_FESTIVAL = 307
const val LEISURE_HAIR_SALON = 308
const val LEISURE_HAIR_SALON_1 = 309
const val LEISURE_TELEVISION = 310
const val LEISURE_BASEBALL = 311
const val LEISURE_BASKETBALL_M = 312
const val LEISURE_BASKETBALL_W = 313
const val LEISURE_FOOTBALL_M = 314
const val LEISURE_FOOTBALL_W = 315
const val LEISURE_SWIMMING_M = 316
const val LEISURE_SWIMMING_W = 317
const val LEISURE_TENNIS_M = 318
const val LEISURE_TENNIS_W = 319
const val LEISURE_VIDEO_PLAYER = 320
const val LEISURE_PARTY = 321

// Symbol 350 ~ 449
const val SYMBOL_YOUTUBE = 350
const val SYMBOL_DROPBOX = 351
const val SYMBOL_PAYPAL = 352
const val SYMBOL_HTML5 = 353
const val SYMBOL_SNAPCHAT = 354
const val SYMBOL_ANDROID = 355
const val SYMBOL_LINKEDIN = 356
const val SYMBOL_TWITTER = 357
const val SYMBOL_INSTAGRAM = 358
const val SYMBOL_FACEBOOK = 359
const val SYMBOL_FLICKR = 360
const val SYMBOL_REDDIT = 361
const val SYMBOL_TRELLO = 362
const val SYMBOL_QUORA = 363
const val SYMBOL_LINE = 364
const val SYMBOL_GITHUB = 365
const val SYMBOL_LINUX = 366
const val SYMBOL_UBUNTU = 367
const val SYMBOL_JAVA = 368
const val SYMBOL_SLIDESHARE = 369

// FLAGS 450 ~ 749
// 001 ~ 010
const val FLAG_MAURITIUS = 450
const val FLAG_AUSTRIA = 452
const val FLAG_OMAN = 453
const val FLAG_ETHIOPIA = 454
const val FLAG_TANZANIA = 455
const val FLAG_NICARAGUA = 456
const val FLAG_ESTONIA = 457
const val FLAG_UGANDA = 458
const val FLAG_SLOVENIA = 459

// 011 ~ 030
const val FLAG_ZIMBABWE = 460
const val FLAG_SAO_TOME_AND_PRINCE = 461
const val FLAG_ITALY = 462
const val FLAG_WALES = 463
const val FLAG_EL_SALVADOR = 464
const val FLAG_NEPAL = 465
const val FLAG_CHRISTMAS_ISLAND = 466
const val FLAG_LEBANON = 467
const val FLAG_CEUTA = 468
const val FLAG_IRAQ = 469
const val FLAG_COOK_ISLAND = 470
const val FLAG_SYRIA = 471
const val FLAG_COCOS_ISLAND = 472
const val FLAG_HONDURAS = 473
const val FLAG_ANGUILLA = 474
const val FLAG_QATAR = 475
const val FLAG_AMERICAN_SAMOA = 476
const val FLAG_PUERTO_RICO = 477
const val FLAG_COMOROS = 478
const val FLAG_NORTH_KOREA = 479

// Random
const val FLAG_CHINA = 483
const val FLAG_SCOTLAND = 504
const val FLAG_JAPAN = 512
const val FLAG_ICELAND = 529
const val FLAG_SLOVAKIA = 540
const val FLAG_SOUTH_KOREA = 543
const val FLAG_PAKISTAN = 549
const val FLAG_CAMEROON = 554
const val FLAG_HUNGARY = 564
const val FLAG_MALAYSIA = 567
const val FLAG_NEW_ZEALAND = 570
const val FLAG_SPAIN = 577
const val FLAG_CHILE = 580
const val FLAG_SAUDI_ARABIA = 582
const val FLAG_IRAN = 585
const val FLAG_NORWAY = 592
const val FLAG_UKRAINE = 594
const val FLAG_CZECH_REPUBLIC = 598
const val FLAG_CUBA = 602
const val FLAG_SWAZILAND = 603
const val FLAG_GERMANY = 611
const val FLAG_BELGIUM = 614
const val FLAG_MOROCCO = 615
const val FLAG_GREECE = 619
const val FLAG_DENMARK = 623
const val FLAG_COLOMBIA = 626
const val FLAG_IRELAND = 628
const val FLAG_SWEDEN = 633
const val FLAG_PHILIPPINES = 641
const val FLAG_FRANCE = 644
const val FLAG_ARGENTINA = 647
const val FLAG_SWITZERLAND = 654
const val FLAG_INDONESIA = 658
const val FLAG_POLAND = 660
const val FLAG_ENGLAND = 665
const val FLAG_TURKEY = 667
const val FLAG_UNITED_STATES = 675
const val FLAG_AUSTRALIA = 683
const val FLAG_CANADA = 692
const val FLAG_INDIA = 695
const val FLAG_RUSSIA = 697
const val FLAG_MEXICO = 701
const val FLAG_BRAZIL = 704
const val FLAG_UNITED_KINGDOM = 709
const val FLAG_THAILAND = 710


/***************************************************************************************************
 *   Save Instance State
 *
 ***************************************************************************************************/
const val LIST_URI_STRING = "list_uri_string"
const val SELECTED_YEAR = "selected_year"
const val SELECTED_MONTH = "selected_month"
const val SELECTED_DAY = "selected_day"
const val SELECTED_HOUR = "selected_hour"
const val SELECTED_MINUTE = "selected_minute"
const val SELECTED_SECOND = "selected_second"
const val FILTER_VIEW_VISIBLE = "filter_view_visible"
const val FILTER_START_ENABLE = "filter_start_enable"
const val FILTER_START_YEAR = "filter_start_year"
const val FILTER_START_MONTH = "filter_start_month"
const val FILTER_START_DATE = "filter_start_date"
const val FILTER_END_ENABLE = "filter_end_enable"
const val FILTER_END_YEAR = "filter_end_year"
const val FILTER_END_MONTH = "filter_end_month"
const val FILTER_END_DATE = "filter_end_date"
const val FILTER_QUERY = "filter_query"


/***************************************************************************************************
 *   Calendar constants
 *
 ***************************************************************************************************/
const val SETTING_CALENDAR_START_DAY = "setting_calendar_start_day"
const val SETTING_CALENDAR_SORTING   = "setting_calendar_sorting"
const val CALENDAR_SORTING_ASC  = 1
const val CALENDAR_SORTING_DESC = 2
const val CALENDAR_START_DAY_SUNDAY    = 1
const val CALENDAR_START_DAY_MONDAY    = 2
const val CALENDAR_START_DAY_TUESDAY   = 3
const val CALENDAR_START_DAY_WEDNESDAY = 4
const val CALENDAR_START_DAY_THURSDAY  = 5
const val CALENDAR_START_DAY_FRIDAY    = 6
const val CALENDAR_START_DAY_SATURDAY  = 7


/***************************************************************************************************
 *   Showcase constants
 *
 ***************************************************************************************************/
const val SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER = 0
const val SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER = 1
const val SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER = 2
const val SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER = 3


/***************************************************************************************************
 *   Notification constants
 *
 ***************************************************************************************************/
const val NOTIFICATION_CHANNEL_ID = "easy_diary_channel_id"
const val NOTIFICATION_CHANNEL_NAME = "notification_type"
const val NOTIFICATION_CHANNEL_DESCRIPTION = "This channel is used for 'Easy-Diary' data backup and recovery operations."
const val NOTIFICATION_FOREGROUND_PHOTO_BACKUP_GMS_ID = 1000
const val NOTIFICATION_FOREGROUND_PHOTO_RECOVERY_GMS_ID = 1001
const val NOTIFICATION_GMS_BACKUP_COMPLETE_ID = 1002
const val NOTIFICATION_GMS_RECOVERY_COMPLETE_ID = 1003
const val NOTIFICATION_COMPRESS_ID = 1004
const val NOTIFICATION_DECOMPRESS_ID = 1005
const val NOTIFICATION_FOREGROUND_FULL_BACKUP_GMS_ID = 1006


/***************************************************************************************************
 *   Path constants
 *
 ***************************************************************************************************/
const val WORKING_DIRECTORY           = "/AAFactory/EasyDiary/"
const val DIARY_PHOTO_DIRECTORY       = "${WORKING_DIRECTORY}Photos/"
const val DIARY_POSTCARD_DIRECTORY    = "${WORKING_DIRECTORY}Postcards/"
const val USER_CUSTOM_FONTS_DIRECTORY = "${WORKING_DIRECTORY}Fonts/"
const val MARKDOWN_DIRECTORY          = "${WORKING_DIRECTORY}Markdown/"
const val BACKUP_EXCEL_DIRECTORY      = "${WORKING_DIRECTORY}Backup/Excel/"
const val BACKUP_DB_DIRECTORY         = "${WORKING_DIRECTORY}Backup/Database/"
const val DIARY_DB_NAME = "diary.realm"


/***************************************************************************************************
 *   Request code constants
 *
 ***************************************************************************************************/
// startActivityForResult Request Code: Permission
const val REQUEST_CODE_EXTERNAL_STORAGE = 1
const val REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD = 2
const val REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING = 3
const val REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN = 4
const val REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_EXCEL = 5
const val REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_REALM = 6
const val REQUEST_CODE_EXTERNAL_STORAGE_WITH_IMPORT_REALM = 7
const val REQUEST_CODE_EXTERNAL_STORAGE_WITH_DELETE_REALM = 8
const val REQUEST_CODE_CAPTURE_CAMERA = 9
const val REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_FULL_BACKUP = 10
const val REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS = 11

// startActivityForResult Request Code: Etc
const val REQUEST_CODE_LOCK_SETTING = 21
const val REQUEST_CODE_IMAGE_PICKER = 22

// startActivityForResult Request Code: ColorPicker
const val REQUEST_CODE_BACKGROUND_COLOR_PICKER = 31
const val REQUEST_CODE_TEXT_COLOR_PICKER = 32

const val REQUEST_CODE_SAF_WRITE_ZIP = 40
const val REQUEST_CODE_SAF_WRITE_XLS = 41
const val REQUEST_CODE_SAF_WRITE_REALM = 42
const val REQUEST_CODE_SAF_READ_ZIP = 43
const val REQUEST_CODE_SAF_READ_REALM = 44

const val REQUEST_CODE_SPEECH_INPUT = 100
const val REQUEST_CODE_GOOGLE_SIGN_IN = 101
const val REQUEST_CODE_GOOGLE_DRIVE_PERMISSIONS = 102
const val REQUEST_CODE_FONT_PICK = 103


/***************************************************************************************************
 *   Diary execution mode
 *
 ***************************************************************************************************/
const val DIARY_EXECUTION_MODE = "diary_execution_mode"
const val EXECUTION_MODE_ACCESS_FROM_OUTSIDE = "execution_mode_access_from_outside"
const val START_MAIN_ACTIVITY = 0


/***************************************************************************************************
 *   SAF mime type
 *
 ***************************************************************************************************/
const val MIME_TYPE_BINARY =  "application/octet-stream"
const val MIME_TYPE_ZIP = "application/zip"
const val MIME_TYPE_XLS = "application/vnd.ms-excel"
const val MIME_TYPE_REALM = "application/octet-stream"
const val MIME_TYPE_JPEG = "image/jpeg"


/***************************************************************************************************
 *   Etc constants
 *
 ***************************************************************************************************/
val EXTERNAL_STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

const val INIT_DUMMY_DATA_FLAG = "init_dummy_data"

const val APP_LOCK_ENABLE = "application_lock"
const val APP_LOCK_REQUEST_PASSWORD = "lock_password"
const val APP_LOCK_SAVED_PASSWORD = "application_lock_password"
const val APP_LOCK_DEFAULT_PASSWORD = "0000"

const val DIARY_SEQUENCE = "diary_sequence"
const val DIARY_CONTENTS_SCROLL_Y = "diary_contents_scroll_y"
const val DIARY_SEARCH_QUERY = "diary_search_query"
const val DIARY_ATTACH_PHOTO_INDEX = "diary_attach_photo_index"
const val DIARY_ENCRYPT_PASSWORD = "diary_encrypt_password"
const val DIARY_MODE = "diary_mode"
const val POSTCARD_SEQUENCE = "postcard_sequence"

const val OPEN_URL_INFO = "open_url_info"
const val THUMBNAIL_BACKGROUND_ALPHA = 170

const val DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE = 20
const val DEFAULT_FONT_SIZE_UN_SUPPORT_LANGUAGE = 15
const val DEFAULT_THUMBNAIL_SIZE_DP = 50
const val DEFAULT_CALENDAR_FONT_SCALE = -1F

const val POSTCARD_BG_COLOR = "postcard_bg_color"
const val POSTCARD_TEXT_COLOR = "postcard_text_color"
const val POSTCARD_BG_COLOR_VALUE = -0x1
const val POSTCARD_TEXT_COLOR_VALUE = -0xb5b5b4
const val POSTCARD_CROP_MODE = "postcard_crop_mode"

const val APP_BACKGROUND_ALPHA = 200
const val INTRO_BACKGROUND_ALPHA = 255
const val SETTING_FONT_SIZE = "font_size"
const val SETTING_CALENDAR_FONT_SCALE = "setting_calendar_font_scale"
const val SETTING_FONT_NAME = "font_setting"
const val SETTING_THUMBNAIL_SIZE = "thumbnail_size"
const val SETTING_BOLD_STYLE = "setting_bold_style"
const val SETTING_MULTIPLE_PICKER = "setting_multiple_picker"
const val SETTING_FINGERPRINT_LOCK = "setting_fingerprint_lock"
const val SETTING_CONTENTS_SUMMARY = "setting_contents_summary"
const val SETTING_CLEAR_LEGACY_TOKEN = "setting_clear_legacy_token"
const val SETTING_SUMMARY_MAX_LINES = "setting_summary_max_lines"
const val SETTING_SUMMARY_MAX_LINES_DEFAULT = 3
const val SETTING_ENABLE_DEBUG_CONSOLE = "setting_enable_debug_console"
const val SETTING_COUNT_CHARACTERS = "setting_count_characters"
const val SETTING_LOCATION_INFO = "setting_location_info"
const val SETTING_TASK_SYMBOL_TOP_ORDER = "setting_task_symbol_top_order"
const val HOLD_POSITION_ENTER_EDIT_SCREEN = "hold_position_enter_edit_screen"
const val FINGERPRINT_ENCRYPT_DATA = "fingerprint_encrypt_data"
const val FINGERPRINT_ENCRYPT_DATA_IV = "fingerprint_encrypt_data_iv"
const val FINGERPRINT_AUTHENTICATION_FAIL_COUNT = "fingerprint_authentication_fail_count"
const val ENABLE_CARD_VIEW_POLICY = "enable_card_view_policy"
const val CUSTOM_FONTS_SUPPORT_LANGUAGE = "en|ko" // custom fonts supported language
const val CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT = "NanumPen.ttf"
const val CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT = "Default"
const val DIARY_SEARCH_QUERY_CASE_SENSITIVE = "case_sensitive"
const val CONTENT_URI_PREFIX = "content:/"
const val FILE_URI_PREFIX = "file:/"
const val LINE_SPACING_SCALE_FACTOR = "line_spacing_scale_factor"
const val LINE_SPACING_SCALE_DEFAULT = 1.0F
const val DIARY_LAST_BACKUP_TIMESTAMP_GOOGLE_DRIVE = "diary_last_backup_time_google_drive"
const val DIARY_LAST_BACKUP_TIMESTAMP_LOCAL = "diary_last_backup_time_local"
const val PHOTO_LAST_BACKUP_TIMESTAMP_GOOGLE_DRIVE = "photo_last_backup_time_google_drive"
const val CAPTURE_CAMERA_FILE_NAME = "capture.jpg"
const val UPDATE_SHARED_PREFERENCE = "update_shared_preference"

const val SETTING_FLAG_EXPORT_GOOGLE_DRIVE = 1
const val SETTING_FLAG_IMPORT_GOOGLE_DRIVE = 2
const val SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE = 3
const val SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE = 4

const val PREVIOUS_ACTIVITY = "previous_activity"
const val PREVIOUS_ACTIVITY_CREATE = 1
const val AAF_TEST = "aaf-test"
const val DOZE_SCHEDULE = "doze_schedule"