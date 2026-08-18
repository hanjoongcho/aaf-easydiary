package me.blog.korn123.commons.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.DiarySymbol
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.helper.BUILDING_APARTMENT
import me.blog.korn123.easydiary.helper.BUILDING_HOSPITAL
import me.blog.korn123.easydiary.helper.BUILDING_MUSEUM
import me.blog.korn123.easydiary.helper.BUILDING_TOWN_HALL
import me.blog.korn123.easydiary.helper.BUILDING_TRAIN_STATION
import me.blog.korn123.easydiary.helper.DAILY_ACTIVITY
import me.blog.korn123.easydiary.helper.DAILY_BUS
import me.blog.korn123.easydiary.helper.DAILY_CANCEL
import me.blog.korn123.easydiary.helper.DAILY_CAR_REPAIR
import me.blog.korn123.easydiary.helper.DAILY_CAR_WASH
import me.blog.korn123.easydiary.helper.DAILY_CAR_WASH_2
import me.blog.korn123.easydiary.helper.DAILY_CIGARETTE
import me.blog.korn123.easydiary.helper.DAILY_CLEANING
import me.blog.korn123.easydiary.helper.DAILY_COFFEE
import me.blog.korn123.easydiary.helper.DAILY_CONTRACT
import me.blog.korn123.easydiary.helper.DAILY_COOKING
import me.blog.korn123.easydiary.helper.DAILY_DICTIONARY
import me.blog.korn123.easydiary.helper.DAILY_DIET
import me.blog.korn123.easydiary.helper.DAILY_DISCUSSION
import me.blog.korn123.easydiary.helper.DAILY_DOING
import me.blog.korn123.easydiary.helper.DAILY_DONE
import me.blog.korn123.easydiary.helper.DAILY_DRIVING
import me.blog.korn123.easydiary.helper.DAILY_DUMBBELL
import me.blog.korn123.easydiary.helper.DAILY_EAT
import me.blog.korn123.easydiary.helper.DAILY_FATHERHOOD
import me.blog.korn123.easydiary.helper.DAILY_GAME_PAD
import me.blog.korn123.easydiary.helper.DAILY_GARBAGE
import me.blog.korn123.easydiary.helper.DAILY_HURRY
import me.blog.korn123.easydiary.helper.DAILY_LIKE
import me.blog.korn123.easydiary.helper.DAILY_LIST
import me.blog.korn123.easydiary.helper.DAILY_MONEY
import me.blog.korn123.easydiary.helper.DAILY_NO_ALCOHOL
import me.blog.korn123.easydiary.helper.DAILY_NO_SMOKING
import me.blog.korn123.easydiary.helper.DAILY_ONLINE_SHOP
import me.blog.korn123.easydiary.helper.DAILY_PET
import me.blog.korn123.easydiary.helper.DAILY_PIN_CODE
import me.blog.korn123.easydiary.helper.DAILY_RECIPE
import me.blog.korn123.easydiary.helper.DAILY_REPAIR
import me.blog.korn123.easydiary.helper.DAILY_SCALE
import me.blog.korn123.easydiary.helper.DAILY_SHIRT
import me.blog.korn123.easydiary.helper.DAILY_SHOPPING_BAG
import me.blog.korn123.easydiary.helper.DAILY_SHOPPING_CART
import me.blog.korn123.easydiary.helper.DAILY_SHOWER
import me.blog.korn123.easydiary.helper.DAILY_SLEEP
import me.blog.korn123.easydiary.helper.DAILY_STOCK
import me.blog.korn123.easydiary.helper.DAILY_STUDY
import me.blog.korn123.easydiary.helper.DAILY_TAKE_AWAY
import me.blog.korn123.easydiary.helper.DAILY_TICKET
import me.blog.korn123.easydiary.helper.DAILY_TIE
import me.blog.korn123.easydiary.helper.DAILY_TODO
import me.blog.korn123.easydiary.helper.DAILY_TOOL_BOX
import me.blog.korn123.easydiary.helper.DAILY_TRAIN
import me.blog.korn123.easydiary.helper.DAILY_VITAMINS
import me.blog.korn123.easydiary.helper.DAILY_WAKE_UP
import me.blog.korn123.easydiary.helper.DAILY_WALLET
import me.blog.korn123.easydiary.helper.DAILY_WORKING
import me.blog.korn123.easydiary.helper.EMOTION_001
import me.blog.korn123.easydiary.helper.EMOTION_002
import me.blog.korn123.easydiary.helper.EMOTION_ALIEN
import me.blog.korn123.easydiary.helper.EMOTION_AMAZE
import me.blog.korn123.easydiary.helper.EMOTION_ANGRY
import me.blog.korn123.easydiary.helper.EMOTION_ANGRY_1
import me.blog.korn123.easydiary.helper.EMOTION_ANGRY_2
import me.blog.korn123.easydiary.helper.EMOTION_CALM
import me.blog.korn123.easydiary.helper.EMOTION_CONFUSION
import me.blog.korn123.easydiary.helper.EMOTION_CRAZY
import me.blog.korn123.easydiary.helper.EMOTION_CRY
import me.blog.korn123.easydiary.helper.EMOTION_CRYING
import me.blog.korn123.easydiary.helper.EMOTION_DELIGHT
import me.blog.korn123.easydiary.helper.EMOTION_DEMON
import me.blog.korn123.easydiary.helper.EMOTION_DEVIL
import me.blog.korn123.easydiary.helper.EMOTION_DISAPPOINTED
import me.blog.korn123.easydiary.helper.EMOTION_EMBARRASSED
import me.blog.korn123.easydiary.helper.EMOTION_EMBARRASSED2
import me.blog.korn123.easydiary.helper.EMOTION_EXCUSE
import me.blog.korn123.easydiary.helper.EMOTION_FEAR
import me.blog.korn123.easydiary.helper.EMOTION_FRUSTRATED
import me.blog.korn123.easydiary.helper.EMOTION_HAPPY
import me.blog.korn123.easydiary.helper.EMOTION_HAPPY_1
import me.blog.korn123.easydiary.helper.EMOTION_HAPPY_2
import me.blog.korn123.easydiary.helper.EMOTION_HAPPY_3
import me.blog.korn123.easydiary.helper.EMOTION_HAPPY_4
import me.blog.korn123.easydiary.helper.EMOTION_HARMFUL
import me.blog.korn123.easydiary.helper.EMOTION_IDEA
import me.blog.korn123.easydiary.helper.EMOTION_INCOMPREHENSION
import me.blog.korn123.easydiary.helper.EMOTION_INJURED
import me.blog.korn123.easydiary.helper.EMOTION_IN_LOVE
import me.blog.korn123.easydiary.helper.EMOTION_LAUGHING
import me.blog.korn123.easydiary.helper.EMOTION_LAUGHING_1
import me.blog.korn123.easydiary.helper.EMOTION_LOVE
import me.blog.korn123.easydiary.helper.EMOTION_MEMORY
import me.blog.korn123.easydiary.helper.EMOTION_OBSERVATION
import me.blog.korn123.easydiary.helper.EMOTION_RICH
import me.blog.korn123.easydiary.helper.EMOTION_SAD
import me.blog.korn123.easydiary.helper.EMOTION_SAD_1
import me.blog.korn123.easydiary.helper.EMOTION_SECRET
import me.blog.korn123.easydiary.helper.EMOTION_SICK
import me.blog.korn123.easydiary.helper.EMOTION_SLEEP
import me.blog.korn123.easydiary.helper.EMOTION_SLEEPING
import me.blog.korn123.easydiary.helper.EMOTION_SMILE
import me.blog.korn123.easydiary.helper.EMOTION_SMILE2
import me.blog.korn123.easydiary.helper.EMOTION_SUFFER
import me.blog.korn123.easydiary.helper.EMOTION_SURPRISED
import me.blog.korn123.easydiary.helper.EMOTION_THINKING
import me.blog.korn123.easydiary.helper.EMOTION_TIRED
import me.blog.korn123.easydiary.helper.EMOTION_TONGUE
import me.blog.korn123.easydiary.helper.EMOTION_TONGUE_1
import me.blog.korn123.easydiary.helper.EMOTION_WINK
import me.blog.korn123.easydiary.helper.FLAG_AMERICAN_SAMOA
import me.blog.korn123.easydiary.helper.FLAG_ANGUILLA
import me.blog.korn123.easydiary.helper.FLAG_ARGENTINA
import me.blog.korn123.easydiary.helper.FLAG_AUSTRALIA
import me.blog.korn123.easydiary.helper.FLAG_AUSTRIA
import me.blog.korn123.easydiary.helper.FLAG_BELGIUM
import me.blog.korn123.easydiary.helper.FLAG_BRAZIL
import me.blog.korn123.easydiary.helper.FLAG_CAMEROON
import me.blog.korn123.easydiary.helper.FLAG_CANADA
import me.blog.korn123.easydiary.helper.FLAG_CEUTA
import me.blog.korn123.easydiary.helper.FLAG_CHILE
import me.blog.korn123.easydiary.helper.FLAG_CHINA
import me.blog.korn123.easydiary.helper.FLAG_CHRISTMAS_ISLAND
import me.blog.korn123.easydiary.helper.FLAG_COCOS_ISLAND
import me.blog.korn123.easydiary.helper.FLAG_COLOMBIA
import me.blog.korn123.easydiary.helper.FLAG_COMOROS
import me.blog.korn123.easydiary.helper.FLAG_COOK_ISLAND
import me.blog.korn123.easydiary.helper.FLAG_CUBA
import me.blog.korn123.easydiary.helper.FLAG_CZECH_REPUBLIC
import me.blog.korn123.easydiary.helper.FLAG_DENMARK
import me.blog.korn123.easydiary.helper.FLAG_EL_SALVADOR
import me.blog.korn123.easydiary.helper.FLAG_ENGLAND
import me.blog.korn123.easydiary.helper.FLAG_ESTONIA
import me.blog.korn123.easydiary.helper.FLAG_ETHIOPIA
import me.blog.korn123.easydiary.helper.FLAG_FRANCE
import me.blog.korn123.easydiary.helper.FLAG_GERMANY
import me.blog.korn123.easydiary.helper.FLAG_GREECE
import me.blog.korn123.easydiary.helper.FLAG_HONDURAS
import me.blog.korn123.easydiary.helper.FLAG_HUNGARY
import me.blog.korn123.easydiary.helper.FLAG_ICELAND
import me.blog.korn123.easydiary.helper.FLAG_INDIA
import me.blog.korn123.easydiary.helper.FLAG_INDONESIA
import me.blog.korn123.easydiary.helper.FLAG_IRAN
import me.blog.korn123.easydiary.helper.FLAG_IRAQ
import me.blog.korn123.easydiary.helper.FLAG_IRELAND
import me.blog.korn123.easydiary.helper.FLAG_ITALY
import me.blog.korn123.easydiary.helper.FLAG_JAPAN
import me.blog.korn123.easydiary.helper.FLAG_LEBANON
import me.blog.korn123.easydiary.helper.FLAG_MALAYSIA
import me.blog.korn123.easydiary.helper.FLAG_MAURITIUS
import me.blog.korn123.easydiary.helper.FLAG_MEXICO
import me.blog.korn123.easydiary.helper.FLAG_MOROCCO
import me.blog.korn123.easydiary.helper.FLAG_NEPAL
import me.blog.korn123.easydiary.helper.FLAG_NEW_ZEALAND
import me.blog.korn123.easydiary.helper.FLAG_NICARAGUA
import me.blog.korn123.easydiary.helper.FLAG_NORTH_KOREA
import me.blog.korn123.easydiary.helper.FLAG_NORWAY
import me.blog.korn123.easydiary.helper.FLAG_OMAN
import me.blog.korn123.easydiary.helper.FLAG_PAKISTAN
import me.blog.korn123.easydiary.helper.FLAG_PHILIPPINES
import me.blog.korn123.easydiary.helper.FLAG_POLAND
import me.blog.korn123.easydiary.helper.FLAG_PUERTO_RICO
import me.blog.korn123.easydiary.helper.FLAG_QATAR
import me.blog.korn123.easydiary.helper.FLAG_RUSSIA
import me.blog.korn123.easydiary.helper.FLAG_SAO_TOME_AND_PRINCE
import me.blog.korn123.easydiary.helper.FLAG_SAUDI_ARABIA
import me.blog.korn123.easydiary.helper.FLAG_SCOTLAND
import me.blog.korn123.easydiary.helper.FLAG_SLOVAKIA
import me.blog.korn123.easydiary.helper.FLAG_SLOVENIA
import me.blog.korn123.easydiary.helper.FLAG_SOUTH_KOREA
import me.blog.korn123.easydiary.helper.FLAG_SPAIN
import me.blog.korn123.easydiary.helper.FLAG_SWAZILAND
import me.blog.korn123.easydiary.helper.FLAG_SWEDEN
import me.blog.korn123.easydiary.helper.FLAG_SWITZERLAND
import me.blog.korn123.easydiary.helper.FLAG_SYRIA
import me.blog.korn123.easydiary.helper.FLAG_TANZANIA
import me.blog.korn123.easydiary.helper.FLAG_THAILAND
import me.blog.korn123.easydiary.helper.FLAG_TURKEY
import me.blog.korn123.easydiary.helper.FLAG_UGANDA
import me.blog.korn123.easydiary.helper.FLAG_UKRAINE
import me.blog.korn123.easydiary.helper.FLAG_UNITED_KINGDOM
import me.blog.korn123.easydiary.helper.FLAG_UNITED_STATES
import me.blog.korn123.easydiary.helper.FLAG_WALES
import me.blog.korn123.easydiary.helper.FLAG_ZIMBABWE
import me.blog.korn123.easydiary.helper.FOOD_BAGUETTE
import me.blog.korn123.easydiary.helper.FOOD_BEVERAGE01
import me.blog.korn123.easydiary.helper.FOOD_BEVERAGE02
import me.blog.korn123.easydiary.helper.FOOD_BEVERAGE03
import me.blog.korn123.easydiary.helper.FOOD_BEVERAGE04
import me.blog.korn123.easydiary.helper.FOOD_BEVERAGE05
import me.blog.korn123.easydiary.helper.FOOD_BEVERAGE06
import me.blog.korn123.easydiary.helper.FOOD_BEVERAGE07
import me.blog.korn123.easydiary.helper.FOOD_BURGER
import me.blog.korn123.easydiary.helper.FOOD_CAKE
import me.blog.korn123.easydiary.helper.FOOD_CAKE_1
import me.blog.korn123.easydiary.helper.FOOD_CHEESE
import me.blog.korn123.easydiary.helper.FOOD_CURRY
import me.blog.korn123.easydiary.helper.FOOD_FEEDING_BOTTLE
import me.blog.korn123.easydiary.helper.FOOD_FRIED_CHICKEN
import me.blog.korn123.easydiary.helper.FOOD_FRIED_RICE
import me.blog.korn123.easydiary.helper.FOOD_GRILL
import me.blog.korn123.easydiary.helper.FOOD_LOBSTER
import me.blog.korn123.easydiary.helper.FOOD_MILK
import me.blog.korn123.easydiary.helper.FOOD_NOODLES
import me.blog.korn123.easydiary.helper.FOOD_PIE
import me.blog.korn123.easydiary.helper.FOOD_PIZZA
import me.blog.korn123.easydiary.helper.FOOD_RICE
import me.blog.korn123.easydiary.helper.FOOD_SALAD
import me.blog.korn123.easydiary.helper.FOOD_SANDWICH
import me.blog.korn123.easydiary.helper.FOOD_SOUP
import me.blog.korn123.easydiary.helper.FOOD_SOUP02
import me.blog.korn123.easydiary.helper.FOOD_STEAK
import me.blog.korn123.easydiary.helper.FOOD_SUSHI
import me.blog.korn123.easydiary.helper.FOOD_WATER
import me.blog.korn123.easydiary.helper.LANDSCAPE_BEACH
import me.blog.korn123.easydiary.helper.LANDSCAPE_BRIDGE
import me.blog.korn123.easydiary.helper.LANDSCAPE_CAPE
import me.blog.korn123.easydiary.helper.LANDSCAPE_CASTLE
import me.blog.korn123.easydiary.helper.LANDSCAPE_CITYSCAPE
import me.blog.korn123.easydiary.helper.LANDSCAPE_DESERT
import me.blog.korn123.easydiary.helper.LANDSCAPE_DESERT_1
import me.blog.korn123.easydiary.helper.LANDSCAPE_FIELDS
import me.blog.korn123.easydiary.helper.LANDSCAPE_FIELDS_1
import me.blog.korn123.easydiary.helper.LANDSCAPE_FOREST
import me.blog.korn123.easydiary.helper.LEISURE_BASEBALL
import me.blog.korn123.easydiary.helper.LEISURE_BASKETBALL_M
import me.blog.korn123.easydiary.helper.LEISURE_BASKETBALL_W
import me.blog.korn123.easydiary.helper.LEISURE_BICYCLE
import me.blog.korn123.easydiary.helper.LEISURE_DOCUMENTARY
import me.blog.korn123.easydiary.helper.LEISURE_FESTIVAL
import me.blog.korn123.easydiary.helper.LEISURE_FISHING
import me.blog.korn123.easydiary.helper.LEISURE_FOOTBALL_M
import me.blog.korn123.easydiary.helper.LEISURE_FOOTBALL_W
import me.blog.korn123.easydiary.helper.LEISURE_FRIENDS_1
import me.blog.korn123.easydiary.helper.LEISURE_FRIENDS_2
import me.blog.korn123.easydiary.helper.LEISURE_FRIENDS_3
import me.blog.korn123.easydiary.helper.LEISURE_GOLF
import me.blog.korn123.easydiary.helper.LEISURE_HAIR_SALON
import me.blog.korn123.easydiary.helper.LEISURE_HAIR_SALON_1
import me.blog.korn123.easydiary.helper.LEISURE_HIKING
import me.blog.korn123.easydiary.helper.LEISURE_MOVIE
import me.blog.korn123.easydiary.helper.LEISURE_NAP
import me.blog.korn123.easydiary.helper.LEISURE_PARK
import me.blog.korn123.easydiary.helper.LEISURE_PARTY
import me.blog.korn123.easydiary.helper.LEISURE_PICNIC
import me.blog.korn123.easydiary.helper.LEISURE_PLAYGROUND
import me.blog.korn123.easydiary.helper.LEISURE_READING
import me.blog.korn123.easydiary.helper.LEISURE_RUNNING
import me.blog.korn123.easydiary.helper.LEISURE_SPORTS_1
import me.blog.korn123.easydiary.helper.LEISURE_SPORTS_2
import me.blog.korn123.easydiary.helper.LEISURE_STRETCHING
import me.blog.korn123.easydiary.helper.LEISURE_SWIMMING_M
import me.blog.korn123.easydiary.helper.LEISURE_SWIMMING_W
import me.blog.korn123.easydiary.helper.LEISURE_TELEVISION
import me.blog.korn123.easydiary.helper.LEISURE_TENNIS_M
import me.blog.korn123.easydiary.helper.LEISURE_TENNIS_W
import me.blog.korn123.easydiary.helper.LEISURE_TRAINING
import me.blog.korn123.easydiary.helper.LEISURE_TRIP
import me.blog.korn123.easydiary.helper.LEISURE_VIDEO_PLAYER
import me.blog.korn123.easydiary.helper.LEISURE_WALKING
import me.blog.korn123.easydiary.helper.SYMBOL_ANDROID
import me.blog.korn123.easydiary.helper.SYMBOL_DROPBOX
import me.blog.korn123.easydiary.helper.SYMBOL_EASTER_EGG
import me.blog.korn123.easydiary.helper.SYMBOL_FACEBOOK
import me.blog.korn123.easydiary.helper.SYMBOL_FLICKR
import me.blog.korn123.easydiary.helper.SYMBOL_GIT
import me.blog.korn123.easydiary.helper.SYMBOL_GITHUB
import me.blog.korn123.easydiary.helper.SYMBOL_GOOGLE_CALENDAR
import me.blog.korn123.easydiary.helper.SYMBOL_HTML5
import me.blog.korn123.easydiary.helper.SYMBOL_INSTAGRAM
import me.blog.korn123.easydiary.helper.SYMBOL_JAVA
import me.blog.korn123.easydiary.helper.SYMBOL_JAVASCRIPT
import me.blog.korn123.easydiary.helper.SYMBOL_LINE
import me.blog.korn123.easydiary.helper.SYMBOL_LINKEDIN
import me.blog.korn123.easydiary.helper.SYMBOL_LINUX
import me.blog.korn123.easydiary.helper.SYMBOL_NETFLIX
import me.blog.korn123.easydiary.helper.SYMBOL_PAYPAL
import me.blog.korn123.easydiary.helper.SYMBOL_QUORA
import me.blog.korn123.easydiary.helper.SYMBOL_REDDIT
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.helper.SYMBOL_SLIDESHARE
import me.blog.korn123.easydiary.helper.SYMBOL_SNAPCHAT
import me.blog.korn123.easydiary.helper.SYMBOL_TRELLO
import me.blog.korn123.easydiary.helper.SYMBOL_TWITTER
import me.blog.korn123.easydiary.helper.SYMBOL_UBUNTU
import me.blog.korn123.easydiary.helper.SYMBOL_USER_CUSTOM_START
import me.blog.korn123.easydiary.helper.SYMBOL_YOUTUBE
import me.blog.korn123.easydiary.helper.WEATHER_BOLT
import me.blog.korn123.easydiary.helper.WEATHER_CLOUD_AND_SUN
import me.blog.korn123.easydiary.helper.WEATHER_DUST
import me.blog.korn123.easydiary.helper.WEATHER_DUST_STORM
import me.blog.korn123.easydiary.helper.WEATHER_MOON
import me.blog.korn123.easydiary.helper.WEATHER_NIGHT_RAIN
import me.blog.korn123.easydiary.helper.WEATHER_RAINBOW
import me.blog.korn123.easydiary.helper.WEATHER_RAIN_DROPS
import me.blog.korn123.easydiary.helper.WEATHER_SNOWING
import me.blog.korn123.easydiary.helper.WEATHER_STARS
import me.blog.korn123.easydiary.helper.WEATHER_SUNNY
import me.blog.korn123.easydiary.helper.WEATHER_TEMPERATURE_H
import me.blog.korn123.easydiary.helper.WEATHER_TEMPERATURE_L
import me.blog.korn123.easydiary.helper.WEATHER_TEMPERATURE_M
import me.blog.korn123.easydiary.helper.WEATHER_UMBRELLA

object FlavorUtils {
    fun sequenceToSymbolResourceId(sequence: Int) =
        when (sequence) {
            SYMBOL_SELECT_ALL -> R.drawable.ic_select_symbol
            WEATHER_SUNNY -> R.drawable.ic_sunny
            WEATHER_CLOUD_AND_SUN -> R.drawable.ic_clouds_and_sun
            WEATHER_RAIN_DROPS -> R.drawable.ic_raindrops
            WEATHER_BOLT -> R.drawable.ic_bolt
            WEATHER_SNOWING -> R.drawable.ic_snowing
            WEATHER_RAINBOW -> R.drawable.ic_rainbow
            WEATHER_UMBRELLA -> R.drawable.ic_umbrella_1
            WEATHER_STARS -> R.drawable.ic_stars_2
            WEATHER_MOON -> R.drawable.ic_moon_9
            WEATHER_NIGHT_RAIN -> R.drawable.ic_night_rain
            WEATHER_TEMPERATURE_L -> R.drawable.ic_temperature_2
            WEATHER_TEMPERATURE_M -> R.drawable.ic_temperature_1
            WEATHER_TEMPERATURE_H -> R.drawable.ic_temperature
            WEATHER_DUST -> R.drawable.ic_dust
            WEATHER_DUST_STORM -> R.drawable.ic_dust_storm
            DAILY_GAME_PAD -> R.drawable.ic_005_gamepad
            DAILY_SHIRT -> R.drawable.ic_008_shirt
            DAILY_VITAMINS -> R.drawable.ic_004_vitamins
            DAILY_WALLET -> R.drawable.ic_003_wallet
            DAILY_WORKING -> R.drawable.ic_001_working
            DAILY_GARBAGE -> R.drawable.ic_009_garbage
            DAILY_TIE -> R.drawable.ic_014_tie
            DAILY_TICKET -> R.drawable.ic_015_ticket
            DAILY_LIKE -> R.drawable.ic_016_like
            DAILY_STUDY -> R.drawable.ic_018_study
            DAILY_SLEEP -> R.drawable.ic_023_sleep
            DAILY_SHOPPING_CART -> R.drawable.ic_024_shopping_cart
            DAILY_SHOPPING_BAG -> R.drawable.ic_025_shopping_bag
            DAILY_REPAIR -> R.drawable.ic_028_repair
            DAILY_LIST -> R.drawable.ic_029_list
            DAILY_PET -> R.drawable.ic_032_pet
            DAILY_FATHERHOOD -> R.drawable.ic_052_fatherhood
            DAILY_COFFEE -> R.drawable.ic_050_coffee
            DAILY_EAT -> R.drawable.ic_044_eat
            DAILY_ACTIVITY -> R.drawable.ic_031_activity
            DAILY_DUMBBELL -> R.drawable.ic_019_dumbbell
            DAILY_HURRY -> R.drawable.ic_041_hurry
            DAILY_COOKING -> R.drawable.ic_049_cooking
            DAILY_CLEANING -> R.drawable.ic_051_cleaning
            DAILY_DISCUSSION -> R.drawable.ic_discussion
            DAILY_DIET -> R.drawable.ic_diet
            DAILY_NO_ALCOHOL -> R.drawable.ic_no_alcohol
            DAILY_WAKE_UP -> R.drawable.ic_bed
            DAILY_SCALE -> R.drawable.ic_scale
            DAILY_TAKE_AWAY -> R.drawable.ic_take_away
            DAILY_CAR_WASH -> R.drawable.ic_car_wash
            DAILY_CAR_WASH_2 -> R.drawable.ic_car_wash_2
            DAILY_SHOWER -> R.drawable.ic_shower
            DAILY_ONLINE_SHOP -> R.drawable.ic_online_shop
            DAILY_DRIVING -> R.drawable.ic_car
            DAILY_CAR_REPAIR -> R.drawable.ic_car_repair
            DAILY_TOOL_BOX -> R.drawable.ic_tool_box
            DAILY_TRAIN -> R.drawable.ic_train
            DAILY_BUS -> R.drawable.ic_bus
            DAILY_TODO -> R.drawable.ic_todo
            DAILY_DOING -> R.drawable.ic_doing
            DAILY_DONE -> R.drawable.ic_done
            DAILY_CANCEL -> R.drawable.ic_cancel
            DAILY_RECIPE -> R.drawable.ic_recipe
            DAILY_STOCK -> R.drawable.ic_dollar
            DAILY_CONTRACT -> R.drawable.ic_contract
            DAILY_PIN_CODE -> R.drawable.ic_pin_code
            DAILY_DICTIONARY -> R.drawable.ic_dictionary
            DAILY_MONEY -> R.drawable.ic_money
            DAILY_CIGARETTE -> R.drawable.ic_cigarette
            DAILY_NO_SMOKING -> R.drawable.ic_no_smoking
            LANDSCAPE_BEACH -> R.drawable.ic_beach
            LANDSCAPE_BRIDGE -> R.drawable.ic_bridge
            LANDSCAPE_CAPE -> R.drawable.ic_cape
            LANDSCAPE_CASTLE -> R.drawable.ic_castle
            LANDSCAPE_CITYSCAPE -> R.drawable.ic_cityscape
            LANDSCAPE_DESERT -> R.drawable.ic_desert
            LANDSCAPE_DESERT_1 -> R.drawable.ic_desert_1
            LANDSCAPE_FIELDS -> R.drawable.ic_fields
            LANDSCAPE_FIELDS_1 -> R.drawable.ic_fields_1
            LANDSCAPE_FOREST -> R.drawable.ic_forest
            BUILDING_APARTMENT -> R.drawable.ic_apartment
            BUILDING_HOSPITAL -> R.drawable.ic_hospital
            BUILDING_MUSEUM -> R.drawable.ic_museum
            BUILDING_TOWN_HALL -> R.drawable.ic_town_hall
            BUILDING_TRAIN_STATION -> R.drawable.ic_train_station
            EMOTION_HAPPY -> R.drawable.ic_001_happy
            EMOTION_LAUGHING -> R.drawable.ic_002_laughing
            EMOTION_CRYING -> R.drawable.ic_003_crying
            EMOTION_ANGRY -> R.drawable.ic_004_angry
            EMOTION_TONGUE -> R.drawable.ic_005_tongue
            EMOTION_ANGRY_1 -> R.drawable.ic_006_angry_1
            EMOTION_WINK -> R.drawable.ic_007_wink
            EMOTION_DISAPPOINTED -> R.drawable.ic_008_disappointed
            EMOTION_SAD -> R.drawable.ic_009_sad
            EMOTION_EMBARRASSED -> R.drawable.ic_010_embarrassed
            EMOTION_THINKING -> R.drawable.ic_014_thinking
            EMOTION_SICK -> R.drawable.ic_019_sick
            EMOTION_SECRET -> R.drawable.ic_020_secret
            EMOTION_SLEEPING -> R.drawable.ic_021_sleeping
            EMOTION_RICH -> R.drawable.ic_025_rich
            EMOTION_DEVIL -> R.drawable.ic_026_devil
            EMOTION_002 -> R.drawable.ic_027_002
            EMOTION_001 -> R.drawable.ic_030_001
            EMOTION_ALIEN -> R.drawable.ic_032_alien
            EMOTION_SURPRISED -> R.drawable.ic_033_surprised_2
            EMOTION_LAUGHING_1 -> R.drawable.ic_041_laughing_1
            EMOTION_INJURED -> R.drawable.ic_042_injured
            EMOTION_HAPPY_1 -> R.drawable.ic_035_happy_2
            EMOTION_DEMON -> R.drawable.ic_046_demon
            EMOTION_IN_LOVE -> R.drawable.ic_047_in_love
            EMOTION_TONGUE_1 -> R.drawable.ic_048_tongue_1
            EMOTION_CALM -> R.drawable.ic_050_calm
            EMOTION_ANGRY_2 -> R.drawable.ic_039_angry_2
            EMOTION_CRY -> R.drawable.ic_cry
            EMOTION_HAPPY_2 -> R.drawable.ic_happy_2
            EMOTION_LOVE -> R.drawable.ic_love
            EMOTION_HAPPY_3 -> R.drawable.ic_happy_1
            EMOTION_SLEEP -> R.drawable.ic_sleep_1
            EMOTION_SMILE -> R.drawable.ic_smile
            EMOTION_SUFFER -> R.drawable.ic_suffer
            EMOTION_EXCUSE -> R.drawable.ic_excuse_1
            EMOTION_HAPPY_4 -> R.drawable.ic_happy
            EMOTION_HARMFUL -> R.drawable.ic_harmful
            EMOTION_INCOMPREHENSION -> R.drawable.ic_incomprehension
            EMOTION_SAD_1 -> R.drawable.ic_sad_2
            EMOTION_IDEA -> R.drawable.ic_idea
            EMOTION_CONFUSION -> R.drawable.ic_confusion
            EMOTION_MEMORY -> R.drawable.ic_memory
            EMOTION_OBSERVATION -> R.drawable.ic_observation
            EMOTION_SMILE2 -> R.drawable.ic_smile_4
            EMOTION_AMAZE -> R.drawable.ic_amaze
            EMOTION_DELIGHT -> R.drawable.ic_delight
            EMOTION_EMBARRASSED2 -> R.drawable.ic_embarrassed_1
            EMOTION_FRUSTRATED -> R.drawable.ic_frustrated
            EMOTION_CRAZY -> R.drawable.ic_crazy
            EMOTION_TIRED -> R.drawable.ic_tired
            EMOTION_FEAR -> R.drawable.ic_fear
            FOOD_BURGER -> R.drawable.ic_burger
            FOOD_PIZZA -> R.drawable.ic_pizza
            FOOD_SANDWICH -> R.drawable.ic_004_sandwich
            FOOD_STEAK -> R.drawable.ic_steak
            FOOD_GRILL -> R.drawable.ic_grill
            FOOD_PIE -> R.drawable.ic_pie
            FOOD_WATER -> R.drawable.ic_010_water
            FOOD_MILK -> R.drawable.ic_milk
            FOOD_SALAD -> R.drawable.ic_023_salad
            FOOD_BAGUETTE -> R.drawable.ic_024_baguette
            FOOD_BEVERAGE03 -> R.drawable.ic_beverage03
            FOOD_BEVERAGE04 -> R.drawable.ic_beverage04
            FOOD_CAKE -> R.drawable.ic_034_cake_1
            FOOD_BEVERAGE05 -> R.drawable.ic_beverage05
            FOOD_FEEDING_BOTTLE -> R.drawable.ic_milk_powder
            FOOD_BEVERAGE01 -> R.drawable.ic_beverage01
            FOOD_SUSHI -> R.drawable.ic_sushi
            FOOD_BEVERAGE06 -> R.drawable.ic_beverage06
            FOOD_BEVERAGE07 -> R.drawable.ic_plastic_bottle
            FOOD_RICE -> R.drawable.ic_rice
            FOOD_FRIED_CHICKEN -> R.drawable.ic_fried_chicken
            FOOD_CAKE_1 -> R.drawable.ic_cake
            FOOD_BEVERAGE02 -> R.drawable.ic_beverage02
            FOOD_SOUP -> R.drawable.ic_soup
            FOOD_CURRY -> R.drawable.ic_curry
            FOOD_NOODLES -> R.drawable.ic_noodles
            FOOD_FRIED_RICE -> R.drawable.ic_fried_rice
            FOOD_SOUP02 -> R.drawable.ic_soup2
            FOOD_LOBSTER -> R.drawable.ic_lobster
            FOOD_CHEESE -> R.drawable.ic_02_cheese
            LEISURE_PICNIC -> R.drawable.ic_picnic
            LEISURE_MOVIE -> R.drawable.ic_movie
            LEISURE_HIKING -> R.drawable.ic_hiking
            LEISURE_READING -> R.drawable.ic_open_book
            LEISURE_NAP -> R.drawable.ic_relaxing
            LEISURE_PARK -> R.drawable.ic_architecture_and_city
            LEISURE_FISHING -> R.drawable.ic_fishing
            LEISURE_FESTIVAL -> R.drawable.ic_castle
            LEISURE_HAIR_SALON -> R.drawable.ic_hairdresser_m
            LEISURE_HAIR_SALON_1 -> R.drawable.ic_hairdresser_w
            LEISURE_TELEVISION -> R.drawable.ic_television
            LEISURE_BASEBALL -> R.drawable.ic_baseball
            LEISURE_BASKETBALL_M -> R.drawable.ic_basketball_player_m
            LEISURE_BASKETBALL_W -> R.drawable.ic_basketball_player_w
            LEISURE_FOOTBALL_M -> R.drawable.ic_football_m
            LEISURE_FOOTBALL_W -> R.drawable.ic_football_w
            LEISURE_SWIMMING_M -> R.drawable.ic_swimmer_m
            LEISURE_SWIMMING_W -> R.drawable.ic_swimmer_w
            LEISURE_TENNIS_M -> R.drawable.ic_tennis_player_m
            LEISURE_TENNIS_W -> R.drawable.ic_tennis_player_w
            LEISURE_VIDEO_PLAYER -> R.drawable.ic_video_player
            LEISURE_PARTY -> R.drawable.ic_party
            LEISURE_STRETCHING -> R.drawable.ic_stretching
            LEISURE_SPORTS_1 -> R.drawable.ic_sport
            LEISURE_SPORTS_2 -> R.drawable.ic_sports
            LEISURE_RUNNING -> R.drawable.ic_running_shoes
            LEISURE_GOLF -> R.drawable.ic_golf_stick
            LEISURE_WALKING -> R.drawable.ic_walking
            LEISURE_TRAINING -> R.drawable.ic_checklist
            LEISURE_DOCUMENTARY -> R.drawable.ic_documentary
            LEISURE_BICYCLE -> R.drawable.ic_bicycle
            LEISURE_FRIENDS_1 -> R.drawable.ic_friends1
            LEISURE_FRIENDS_2 -> R.drawable.ic_friends2
            LEISURE_FRIENDS_3 -> R.drawable.ic_friends3
            LEISURE_TRIP -> R.drawable.ic_automobile
            LEISURE_PLAYGROUND -> R.drawable.ic_playground
            SYMBOL_YOUTUBE -> R.drawable.ic_youtube
            SYMBOL_NETFLIX -> R.drawable.ic_netflix
            SYMBOL_DROPBOX -> R.drawable.ic_dropbox
            SYMBOL_PAYPAL -> R.drawable.ic_paypal
            SYMBOL_HTML5 -> R.drawable.ic_html5
            SYMBOL_SNAPCHAT -> R.drawable.ic_snapchat
            SYMBOL_ANDROID -> R.drawable.ic_android
            SYMBOL_LINKEDIN -> R.drawable.ic_linkedin
            SYMBOL_TWITTER -> R.drawable.ic_twitter
            SYMBOL_INSTAGRAM -> R.drawable.ic_instagram
            SYMBOL_FACEBOOK -> R.drawable.ic_facebook
            SYMBOL_FLICKR -> R.drawable.ic_flickr
            SYMBOL_REDDIT -> R.drawable.ic_reddit
            SYMBOL_TRELLO -> R.drawable.ic_trello
            SYMBOL_QUORA -> R.drawable.ic_quora
            SYMBOL_LINE -> R.drawable.ic_line
            SYMBOL_GITHUB -> R.drawable.ic_github_logo
            SYMBOL_LINUX -> R.drawable.ic_linux
            SYMBOL_UBUNTU -> R.drawable.ic_ubuntu
            SYMBOL_JAVA -> R.drawable.ic_java
            SYMBOL_SLIDESHARE -> R.drawable.ic_slideshare
            SYMBOL_GIT -> R.drawable.ic_git
            SYMBOL_JAVASCRIPT -> R.drawable.ic_javascript
            SYMBOL_GOOGLE_CALENDAR -> R.drawable.logo_google_calendar
            FLAG_MAURITIUS -> R.drawable.ic_001_mauritius
            FLAG_AUSTRIA -> R.drawable.ic_003_austria
            FLAG_OMAN -> R.drawable.ic_004_oman
            FLAG_ETHIOPIA -> R.drawable.ic_005_ethiopia
            FLAG_TANZANIA -> R.drawable.ic_006_tanzania
            FLAG_NICARAGUA -> R.drawable.ic_007_nicaragua
            FLAG_ESTONIA -> R.drawable.ic_008_estonia
            FLAG_UGANDA -> R.drawable.ic_009_uganda
            FLAG_SLOVENIA -> R.drawable.ic_010_slovenia
            FLAG_ZIMBABWE -> R.drawable.ic_011_zimbabwe
            FLAG_SAO_TOME_AND_PRINCE -> R.drawable.ic_012_sao_tome_and_prince
            FLAG_ITALY -> R.drawable.ic_013_italy
            FLAG_WALES -> R.drawable.ic_014_wales
            FLAG_EL_SALVADOR -> R.drawable.ic_015_el_salvador
            FLAG_NEPAL -> R.drawable.ic_016_nepal
            FLAG_CHRISTMAS_ISLAND -> R.drawable.ic_017_christmas_island
            FLAG_LEBANON -> R.drawable.ic_018_lebanon
            FLAG_CEUTA -> R.drawable.ic_019_ceuta
            FLAG_IRAQ -> R.drawable.ic_020_iraq
            FLAG_COOK_ISLAND -> R.drawable.ic_021_cook_islands
            FLAG_SYRIA -> R.drawable.ic_022_syria
            FLAG_COCOS_ISLAND -> R.drawable.ic_023_cocos_island
            FLAG_HONDURAS -> R.drawable.ic_024_honduras
            FLAG_ANGUILLA -> R.drawable.ic_025_anguilla
            FLAG_QATAR -> R.drawable.ic_026_qatar
            FLAG_AMERICAN_SAMOA -> R.drawable.ic_027_american_samoa
            FLAG_PUERTO_RICO -> R.drawable.ic_028_puerto_rico
            FLAG_COMOROS -> R.drawable.ic_029_comoros
            FLAG_NORTH_KOREA -> R.drawable.ic_030_north_korea
            FLAG_CHINA -> R.drawable.ic_034_china
            FLAG_SCOTLAND -> R.drawable.ic_055_scotland
            FLAG_JAPAN -> R.drawable.ic_063_japan
            FLAG_ICELAND -> R.drawable.ic_080_iceland
            FLAG_SLOVAKIA -> R.drawable.ic_091_slovakia
            FLAG_SOUTH_KOREA -> R.drawable.ic_094_south_korea
            FLAG_PAKISTAN -> R.drawable.ic_100_pakistan
            FLAG_CAMEROON -> R.drawable.ic_105_cameroon
            FLAG_HUNGARY -> R.drawable.ic_115_hungary
            FLAG_MALAYSIA -> R.drawable.ic_118_malasya
            FLAG_NEW_ZEALAND -> R.drawable.ic_121_new_zealand
            FLAG_SPAIN -> R.drawable.ic_128_spain
            FLAG_CHILE -> R.drawable.ic_131_chile
            FLAG_SAUDI_ARABIA -> R.drawable.ic_133_saudi_arabia
            FLAG_IRAN -> R.drawable.ic_136_iran
            FLAG_NORWAY -> R.drawable.ic_143_norway
            FLAG_UKRAINE -> R.drawable.ic_145_ukraine
            FLAG_CZECH_REPUBLIC -> R.drawable.ic_149_czech_republic
            FLAG_CUBA -> R.drawable.ic_153_cuba
            FLAG_SWAZILAND -> R.drawable.ic_154_swaziland
            FLAG_GERMANY -> R.drawable.ic_162_germany
            FLAG_BELGIUM -> R.drawable.ic_165_belgium
            FLAG_MOROCCO -> R.drawable.ic_166_morocco
            FLAG_GREECE -> R.drawable.ic_170_greece
            FLAG_DENMARK -> R.drawable.ic_174_denmark
            FLAG_COLOMBIA -> R.drawable.ic_177_colombia
            FLAG_IRELAND -> R.drawable.ic_179_ireland
            FLAG_SWEDEN -> R.drawable.ic_184_sweden
            FLAG_PHILIPPINES -> R.drawable.ic_192_philippines
            FLAG_FRANCE -> R.drawable.ic_195_france
            FLAG_ARGENTINA -> R.drawable.ic_198_argentina
            FLAG_SWITZERLAND -> R.drawable.ic_205_switzerland
            FLAG_INDONESIA -> R.drawable.ic_209_indonesia
            FLAG_POLAND -> R.drawable.ic_211_poland
            FLAG_ENGLAND -> R.drawable.ic_216_england
            FLAG_TURKEY -> R.drawable.ic_218_turkey
            FLAG_UNITED_STATES -> R.drawable.ic_226_united_states
            FLAG_AUSTRALIA -> R.drawable.ic_234_australia
            FLAG_CANADA -> R.drawable.ic_243_canada
            FLAG_INDIA -> R.drawable.ic_246_india
            FLAG_RUSSIA -> R.drawable.ic_248_russia
            FLAG_MEXICO -> R.drawable.ic_252_mexico
            FLAG_BRAZIL -> R.drawable.ic_255_brazil
            FLAG_UNITED_KINGDOM -> R.drawable.ic_260_united_kingdom
            FLAG_THAILAND -> R.drawable.ic_238_thailand
            SYMBOL_EASTER_EGG -> R.drawable.ic_easter_egg
            else -> 0
        }

    fun initWeatherView(
        context: Context,
        imageView: ImageView?,
        weatherFlag: Int,
        isShowEmptyWeatherView: Boolean = false,
        applyWhiteFilter: Boolean = false,
    ) {
        imageView?.run {
            visibility = if (!isShowEmptyWeatherView && weatherFlag < 1) View.GONE else View.VISIBLE
            if (weatherFlag < SYMBOL_USER_CUSTOM_START) {
//                setImageResource(sequenceToSymbolResourceId(weatherFlag))
                Glide
                    .with(this.context)
                    .load(sequenceToSymbolResourceId(weatherFlag))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(this)
            } else {
                // FIXME: WIP START
                if (context.config.enableDebugMode) {
                    val targetIndex = weatherFlag.minus(SYMBOL_USER_CUSTOM_START)

                    val filePath =
                        if (context.config.customSymbolPaths.size > targetIndex) {
                            context.config.customSymbolPaths[targetIndex].getFilePath()
                        } else {
                            ""
                        }
//                    setImageBitmap(BitmapFactory.decodeFile(EasyDiaryUtils.getApplicationDataDirectory(context) + filePath))
                    Glide
                        .with(this)
                        .load(EasyDiaryUtils.getApplicationDataDirectory(context) + filePath)
                        .listener(
                            object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable?>,
                                    isFirstResource: Boolean,
                                ): Boolean = false

                                override fun onResourceReady(
                                    resource: Drawable,
                                    model: Any,
                                    target: Target<Drawable?>?,
                                    dataSource: DataSource,
                                    isFirstResource: Boolean,
                                ): Boolean = false
                            },
                        ).transform(CenterCrop(), RoundedCorners(context.dpToPixel(5F)))
                        .into(this)
                } else {
                    setImageResource(0)
                }
                // FIXME: WIP END
            }
        }
    }

//    private fun customSymbolSequenceToBitmap(context: Context, sequence: Int): Bitmap {
//        return BitmapFactory.decodeFile(
//            EasyDiaryUtils.getApplicationDataDirectory(context) + EasyDiaryDbHelper.findDiaryBy(
//                sequence
//            )?.photoUris?.get(0)?.getFilePath()
//        )
//    }

    fun getDiarySymbolMap(context: Context): HashMap<Int, String> {
        val symbolMap = hashMapOf<Int, String>()
        val symbolArray =
            arrayOf(
                *context.resources.getStringArray(R.array.weather_item_array),
                *context.resources.getStringArray(R.array.emotion_item_array),
                *context.resources.getStringArray(R.array.daily_item_array),
                *context.resources.getStringArray(R.array.tasks_item_array),
                *context.resources.getStringArray(R.array.food_item_array),
                *context.resources.getStringArray(R.array.leisure_item_array),
                *context.resources.getStringArray(R.array.landscape_item_array),
                *context.resources.getStringArray(R.array.symbol_item_array),
                *context.resources.getStringArray(R.array.flag_item_array),
            )

        symbolArray.map { item ->
            val symbolItem = DiarySymbol(item)
            symbolMap.put(symbolItem.sequence, symbolItem.description)
        }
        return symbolMap
    }
}
