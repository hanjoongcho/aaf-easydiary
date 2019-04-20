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
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.util.*

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

    fun initWeatherView(context: Context, imageView: ImageView?, weatherFlag: Int, isShowEmptyWeatherView: Boolean = false, applyWhiteFilter: Boolean = false) {
        imageView?.run { 
            if (!isShowEmptyWeatherView && weatherFlag < 1) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
            }

            when (weatherFlag) {
                0 -> setImageResource(0)
                WEATHER_SUNNY -> setImageResource(R.drawable.ic_sunny)
                WEATHER_CLOUD_AND_SUN -> setImageResource(R.drawable.ic_clouds_and_sun)
                WEATHER_RAIN_DROPS -> setImageResource(R.drawable.ic_raindrops)
                WEATHER_BOLT -> setImageResource(R.drawable.ic_bolt)
                WEATHER_SNOWING -> setImageResource(R.drawable.ic_snowing)
                WEATHER_RAINBOW -> setImageResource(R.drawable.ic_rainbow)
                WEATHER_UMBRELLA -> setImageResource(R.drawable.ic_umbrella_1)
                WEATHER_STARS -> setImageResource(R.drawable.ic_stars_2)
                WEATHER_MOON -> setImageResource(R.drawable.ic_moon_9)
                WEATHER_NIGHT_RAIN -> setImageResource(R.drawable.ic_night_rain)

                ACTIVITY_CHEST -> setImageResource(R.drawable.ic_001_chest)
                ACTIVITY_BALL -> setImageResource(R.drawable.ic_002_ball)
                ACTIVITY_MEETING -> setImageResource(R.drawable.ic_003_meeting)
                ACTIVITY_MORE -> setImageResource(R.drawable.ic_004_more)
                ACTIVITY_CONNECTION -> setImageResource(R.drawable.ic_005_connection)
                ACTIVITY_ENERGY -> setImageResource(R.drawable.ic_006_energy)
                ACTIVITY_PARTY -> setImageResource(R.drawable.ic_007_party)
                ACTIVITY_FULL_BATTERY -> setImageResource(R.drawable.ic_008_full_battery)
                ACTIVITY_LUNG -> setImageResource(R.drawable.ic_009_lung)
                ACTIVITY_ROPE -> setImageResource(R.drawable.ic_010_rope)

                ACTIVITY_ALARM -> setImageResource(R.drawable.ic_011_alarm)
                ACTIVITY_TIMER -> setImageResource(R.drawable.ic_012_timer)
                ACTIVITY_CLOTHES -> setImageResource(R.drawable.ic_013_clothes)
                ACTIVITY_PUNCHING -> setImageResource(R.drawable.ic_014_punching_bag)
                ACTIVITY_CAMPING -> setImageResource(R.drawable.ic_015_camping)
                ACTIVITY_STATIONARY_BIKE -> setImageResource(R.drawable.ic_016_stationary_bike)
                ACTIVITY_STEPS -> setImageResource(R.drawable.ic_017_steps)
                ACTIVITY_STAIRS -> setImageResource(R.drawable.ic_018_stairs)
                ACTIVITY_HEALTH_FOODS -> setImageResource(R.drawable.ic_019_healthy_food)
                ACTIVITY_ENERGY_DRINK-> setImageResource(R.drawable.ic_020_energy_drink)

                ACTIVITY_DUMBBELL -> setImageResource(R.drawable.ic_021_dumbbell)
                ACTIVITY_VACUUM -> setImageResource(R.drawable.ic_022_vacuum)
                ACTIVITY_LAWN_MOWER -> setImageResource(R.drawable.ic_023_lawn_mower)
                ACTIVITY_WATERING_CAN -> setImageResource(R.drawable.ic_024_watering_can)
                ACTIVITY_BROOM -> setImageResource(R.drawable.ic_025_broom)
                ACTIVITY_WATER -> setImageResource(R.drawable.ic_026_water)
                ACTIVITY_MICROPHONE -> setImageResource(R.drawable.ic_027_microphone)
                ACTIVITY_DUMBBELL2 -> setImageResource(R.drawable.ic_028_dumbbell)
                ACTIVITY_SKATING -> setImageResource(R.drawable.ic_029_skating)
                ACTIVITY_TREADMILL -> setImageResource(R.drawable.ic_030_treadmill)

                DAILY_GAME_PAD -> setImageResource(R.drawable.ic_005_gamepad)
                DAILY_SHIRT -> setImageResource(R.drawable.ic_008_shirt)
                DAILY_VITAMINS -> setImageResource(R.drawable.ic_004_vitamins)
                DAILY_WALLET -> setImageResource(R.drawable.ic_003_wallet)
                DAILY_WORKING -> setImageResource(R.drawable.ic_001_working)
            }
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
}
