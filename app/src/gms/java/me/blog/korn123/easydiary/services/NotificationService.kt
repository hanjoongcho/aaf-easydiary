package me.blog.korn123.easydiary.services

import android.content.Intent

class NotificationService : BaseNotificationService() {
    override fun onHandleIntent(intent: Intent?) {
        super.onHandleIntent(intent)
        intent?.let {
            when (it.action) {
                ACTION_RECOVER_CANCEL -> handleActionRecoverCancel()
                ACTION_BACKUP_CANCEL -> handleActionBackupCancel()
            }
        }
    }

    private fun handleActionRecoverCancel() {
        val recoverPhotoService = Intent(this, RecoverPhotoService::class.java)
        stopService(recoverPhotoService)
    }

    private fun handleActionBackupCancel() {
        val recoverPhotoService = Intent(this, BackupPhotoService::class.java)
        stopService(recoverPhotoService)
    }
}