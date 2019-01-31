package me.blog.korn123.easydiary.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class BackupPhotoService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
} 