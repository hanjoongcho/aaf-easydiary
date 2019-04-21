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

                DAILY_GAME_PAD -> setImageResource(R.drawable.ic_005_gamepad)
                DAILY_SHIRT -> setImageResource(R.drawable.ic_008_shirt)
                DAILY_VITAMINS -> setImageResource(R.drawable.ic_004_vitamins)
                DAILY_WALLET -> setImageResource(R.drawable.ic_003_wallet)
                DAILY_WORKING -> setImageResource(R.drawable.ic_001_working)
                DAILY_GARBAGE -> setImageResource(R.drawable.ic_009_garbage)
                DAILY_TIE -> setImageResource(R.drawable.ic_014_tie)
                DAILY_TICKET -> setImageResource(R.drawable.ic_015_ticket)
                DAILY_LIKE -> setImageResource(R.drawable.ic_016_like)
                DAILY_STUDY -> setImageResource(R.drawable.ic_018_study)
                DAILY_SLEEP -> setImageResource(R.drawable.ic_023_sleep)
                DAILY_SHOPPING_CART -> setImageResource(R.drawable.ic_024_shopping_cart)
                DAILY_REPAIR -> setImageResource(R.drawable.ic_028_repair)
                DAILY_LIST -> setImageResource(R.drawable.ic_029_list)
                DAILY_PET -> setImageResource(R.drawable.ic_032_pet)
                DAILY_FATHERHOOD -> setImageResource(R.drawable.ic_052_fatherhood)
                DAILY_COFFEE -> setImageResource(R.drawable.ic_050_coffee)
                DAILY_EAT -> setImageResource(R.drawable.ic_044_eat)

                EMOJI_HAPPY -> setImageResource(R.drawable.ic_001_happy)
                EMOJI_LAUGHING -> setImageResource(R.drawable.ic_002_laughing)
                EMOJI_CRYING -> setImageResource(R.drawable.ic_003_crying)
                EMOJI_ANGRY -> setImageResource(R.drawable.ic_004_angry)
                EMOJI_TONGUE -> setImageResource(R.drawable.ic_005_tongue)
                EMOJI_ANGRY_1 -> setImageResource(R.drawable.ic_006_angry_1)
                EMOJI_WINK -> setImageResource(R.drawable.ic_007_wink)
                EMOJI_DISAPPOINTED -> setImageResource(R.drawable.ic_008_disappointed)
                EMOJI_SAD -> setImageResource(R.drawable.ic_009_sad)
                EMOJI_EMBARRASSED -> setImageResource(R.drawable.ic_010_embarrassed)

                else -> setImageResource(0)
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
