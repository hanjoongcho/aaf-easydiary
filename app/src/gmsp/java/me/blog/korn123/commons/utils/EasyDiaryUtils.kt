package me.blog.korn123.commons.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import id.zelory.compressor.Compressor
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.CALCULATION
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.SecondItemAdapter
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiarySymbol
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by hanjoong on 2017-04-30.
 */

object EasyDiaryUtils {
    val easyDiaryMimeType: String
        get() = "text/aaf_v" + EasyDiaryDbHelper.getInstance().version

    val easyDiaryMimeTypeAll: Array<String?>
        get() {
            val currentVersion = EasyDiaryDbHelper.getInstance().version.toInt()
            val easyDiaryMimeType = arrayOfNulls<String>(currentVersion)
            for (i in 0 until currentVersion) {
                easyDiaryMimeType[i] = "text/aaf_v" + (i + 1)
            }
            return easyDiaryMimeType
        }

    fun initWorkingDirectory(context: Context) {
        if (context.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            makeDirectory(Environment.getExternalStorageDirectory().absolutePath + USER_CUSTOM_FONTS_DIRECTORY)
            makeDirectory(Environment.getExternalStorageDirectory().absolutePath + DIARY_PHOTO_DIRECTORY)
            makeDirectory(Environment.getExternalStorageDirectory().absolutePath + DIARY_POSTCARD_DIRECTORY)    
        }
    }
    
    private fun makeDirectory(path: String) {
        val workingDirectory = File(path)
        if (!workingDirectory.exists()) workingDirectory.mkdirs()
    }

    fun sequenceToSymbolResourceId(sequence: Int) = when (sequence) {
        0 -> 0
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
        WEATHER_DUST-> R.drawable.ic_dust
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
        DAILY_WAKE_UP -> R.drawable.ic_wake_up

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
        EMOTION_SKULL -> R.drawable.ic_027_skull
        EMOTION_POO -> R.drawable.ic_030_poo
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
        EMOTION_SAD_1-> R.drawable.ic_sad_2

        FOOD_BURGER -> R.drawable.ic_burger
        FOOD_SANDWICH -> R.drawable.ic_004_sandwich
        FOOD_STEAK -> R.drawable.ic_steak
        FOOD_PIE -> R.drawable.ic_pie
        FOOD_WATER -> R.drawable.ic_010_water
        FOOD_MILK -> R.drawable.ic_milk
        FOOD_SALAD -> R.drawable.ic_023_salad
        FOOD_BAGUETTE -> R.drawable.ic_024_baguette
        FOOD_WHISKEY -> R.drawable.ic_whiskey
        FOOD_WINE -> R.drawable.ic_wine
        FOOD_CAKE -> R.drawable.ic_034_cake_1
        FOOD_CHAMPAGNE -> R.drawable.ic_036_champagne
        FOOD_FEEDING_BOTTLE -> R.drawable.ic_milk_powder
        FOOD_BEER -> R.drawable.ic_beer
        FOOD_SUSHI -> R.drawable.ic_sushi
        FOOD_SAKE -> R.drawable.ic_044_sake
        FOOD_RICE -> R.drawable.ic_rice
        FOOD_FRIED_CHICKEN -> R.drawable.ic_fried_chicken
        FOOD_CAKE_1 -> R.drawable.ic_cake
        FOOD_SOJU -> R.drawable.ic_soju
        FOOD_SOUP -> R.drawable.ic_soup
        FOOD_CURRY -> R.drawable.ic_curry
        FOOD_NOODLES -> R.drawable.ic_noodles
        FOOD_FRIED_RICE -> R.drawable.ic_fried_rice

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

        SYMBOL_YOUTUBE -> R.drawable.ic_youtube
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

        else -> 0
    }
    
    fun initWeatherView(context: Context, imageView: ImageView?, weatherFlag: Int, isShowEmptyWeatherView: Boolean = false, applyWhiteFilter: Boolean = false) {
        imageView?.run {
            visibility = if (!isShowEmptyWeatherView && weatherFlag < 1) View.GONE else View.VISIBLE
            setImageResource(sequenceToSymbolResourceId(weatherFlag))
            
        }
    }

    fun boldString(context: Context, textView: TextView?) {
        if (context.config.boldStyleEnable) {
            textView?.let { tv ->
                val spannableString = SpannableString(tv.text)
                spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, tv.text.length, 0)
                tv.text = spannableString
            }
        }
    }
    
    fun highlightString(textView: TextView?, input: String?) {
        textView?.let { tv ->
            input?.let { targetString ->
                //Get the text from text view and create a spannable string
                val spannableString = SpannableString(tv.text)

                //Get the previous spans and remove them
                val backgroundSpans = spannableString.getSpans(0, spannableString.length, BackgroundColorSpan::class.java)

                for (span in backgroundSpans) {
                    spannableString.removeSpan(span)
                }

                //Search for all occurrences of the keyword in the string
                var indexOfKeyword = spannableString.toString().indexOf(targetString)

                while (indexOfKeyword >= 0) {
                    //Create a background color span on the keyword
                    spannableString.setSpan(BackgroundColorSpan(Color.YELLOW), indexOfKeyword, indexOfKeyword + targetString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    //Get the next index of the keyword
                    indexOfKeyword = spannableString.toString().indexOf(targetString, indexOfKeyword + targetString.length)
                }

                //Set the final text on TextView
                tv.text = spannableString    
            }
        }
    }

    fun highlightStringIgnoreCase(textView: TextView?, input: String?) {
        textView?.let { tv -> 
            input?.let { targetString ->
                val inputLower = targetString.toLowerCase()
                val contentsLower = tv.text.toString().toLowerCase()
                val spannableString = SpannableString(tv.text)

                val backgroundSpans = spannableString.getSpans(0, spannableString.length, BackgroundColorSpan::class.java)

                for (span in backgroundSpans) {
                    spannableString.removeSpan(span)
                }

                var indexOfKeyword = contentsLower.indexOf(inputLower)

                while (indexOfKeyword >= 0) {
                    spannableString.setSpan(BackgroundColorSpan(Color.YELLOW), indexOfKeyword, indexOfKeyword + inputLower.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    indexOfKeyword = contentsLower.indexOf(inputLower, indexOfKeyword + inputLower.length)
                }
                tv.text = spannableString
            }
        }
    }

    fun createSecondsPickerBuilder(context: Context, itemClickListener: AdapterView.OnItemClickListener, second: Int): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setNegativeButton(context.getString(android.R.string.cancel), null)
        builder.setTitle(context.getString(R.string.common_create_seconds))
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val secondsPicker = inflater.inflate(R.layout.dialog_seconds_picker, null)
        val listView = secondsPicker.findViewById<ListView>(R.id.seconds)
        val listSecond = ArrayList<Map<String, String>>()
        for (i in 0..59) {
            val map = hashMapOf<String, String>()
            map.put("label", i.toString() + "s")
            map.put("value", i.toString())
            listSecond.add(map)
        }
        val adapter = SecondItemAdapter(context, R.layout.item_second, listSecond, second)
        listView.adapter = adapter
        listView.onItemClickListener = itemClickListener
        builder.setView(secondsPicker)
        return builder
    }

    fun photoUriToDownSamplingBitmap(
            context: Context,
            photoUriDto: PhotoUriDto,
            requiredSize: Int = 50,
            fixedWidth: Int = 45,
            fixedHeight: Int = 45
    ): Bitmap = try {
        when (photoUriDto.isContentUri()) {
            true -> {
                BitmapUtils.decodeFile(context, Uri.parse(photoUriDto.photoUri), CommonUtils.dpToPixel(context, fixedWidth.toFloat(), CALCULATION.FLOOR), CommonUtils.dpToPixel(context, fixedHeight.toFloat(), CALCULATION.FLOOR))
            }
            false -> {
                when (fixedWidth == fixedHeight) {
                    true -> BitmapUtils.decodeFileCropCenter(photoUriDto.getFilePath(), CommonUtils.dpToPixel(context, fixedWidth.toFloat(), CALCULATION.FLOOR))
                    false -> BitmapUtils.decodeFile(photoUriDto.getFilePath(), CommonUtils.dpToPixel(context, fixedWidth.toFloat(), CALCULATION.FLOOR), CommonUtils.dpToPixel(context, fixedHeight.toFloat(), CALCULATION.FLOOR))
                }

            }
        }
    } catch (fe: FileNotFoundException) {
        fe.printStackTrace()
        BitmapFactory.decodeResource(context.resources, R.drawable.question_shield)
    } catch (se: SecurityException) {
        se.printStackTrace()
        BitmapFactory.decodeResource(context.resources, R.drawable.question_shield)
    } catch (e: Exception) {
        e.printStackTrace()
        BitmapFactory.decodeResource(context.resources, R.drawable.question_shield)
    }

    fun photoUriToBitmap(context: Context, photoUriDto: PhotoUriDto): Bitmap? {
        val bitmap: Bitmap? = try {
            when (photoUriDto.isContentUri()) {
                true -> {
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(Uri.parse(photoUriDto.photoUri)))
                }
                false -> {
                    BitmapFactory.decodeFile(photoUriDto.getFilePath())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return bitmap
    }
    
    fun downSamplingImage(context: Context, srcFile: File, destFile: File) {
        val compressedFile = Compressor(context).setQuality(70).compressToFile(srcFile)
        FileUtils.copyFile(compressedFile, destFile)
    }

    fun changeDrawableIconColor(context: Context, color: Int, resourceId: Int) {
        ContextCompat.getDrawable(context, resourceId)?.apply {
            setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    fun getDiarySymbolMap(context: Context): HashMap<Int, String> {
        val symbolMap = hashMapOf<Int, String>()
        val symbolArray = arrayOf(
                *context.resources.getStringArray(R.array.weather_item_array),
                *context.resources.getStringArray(R.array.emotion_item_array),
                *context.resources.getStringArray(R.array.daily_item_array),
                *context.resources.getStringArray(R.array.food_item_array),
                *context.resources.getStringArray(R.array.leisure_item_array),
                *context.resources.getStringArray(R.array.landscape_item_array),
                *context.resources.getStringArray(R.array.symbol_item_array)
        )

        symbolArray.map { item ->
            val symbolItem = DiarySymbol(item)
            symbolMap.put(symbolItem.sequence, symbolItem.description)
        }
        return symbolMap
    }
}
