package me.blog.korn123.easydiary.activities

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import com.simplemobiletools.commons.extensions.showErrorToast
import kotlinx.android.synthetic.main.activity_diary_reminder.*
import me.blog.korn123.easydiary.R

class DiaryReminderActivity : EasyDiaryActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var lastVolumeValue = 0.1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_reminder)
        showOverLockScreen()

        setupAudio()
        setupEvent()
    }

    private fun setupEvent() {
        confirm.setOnClickListener {
            finishActivity()
        }
    }

    private fun setupAudio() {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_ALARM)
                setDataSource(this@DiaryReminderActivity, soundUri)
                setVolume(lastVolumeValue, lastVolumeValue)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyPlayer()
    }

    private fun destroyPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun finishActivity() {
        destroyPlayer()
        finish()
        overridePendingTransition(0, 0)
    }
}