package me.blog.korn123.easydiary.googledrive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper;
import me.blog.korn123.easydiary.diary.ReadDiaryActivity;

/**
 * Created by CHO HANJOONG on 2016-09-28.
 */
public class GoogleDriveDownloader extends GoogleDriveUtils {

    /**
     * File that is selected with the open file activity.
     */
    private DriveId mSelectedFileDriveId;

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        // If there is a selected file, open its contents.
        if (mSelectedFileDriveId != null) {
            open();
            return;
        }

        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[] { EasyDiaryUtils.getEasyDiaryMimeType() })
                .build(getGoogleApiClient());

        try {
            startIntentSenderForResult(
                    intentSender, Constants.REQUEST_CODE_GOOGLE_DRIVE_DOWNLOAD, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GOOGLE_DRIVE_DOWNLOAD && resultCode == RESULT_OK && data != null) { // 계정선택 후 최초 진입 시 data가 null 임
            // 파일다운로드 성공
            mSelectedFileDriveId = (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        CommonUtils.saveLongPreference(GoogleDriveDownloader.this, Constants.PAUSE_MILLIS, System.currentTimeMillis());
    }

    private void open() {
        DriveFile driveFile = mSelectedFileDriveId.asDriveFile();
        DriveFile.DownloadProgressListener listener = new DriveFile.DownloadProgressListener() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {
                // Update progress dialog with the latest progress.
                int progress = (int)(bytesDownloaded*100/bytesExpected);
            }
        };
        driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, listener)
                .setResultCallback(driveContentsCallback);
    }

    private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
        new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                if (result.getStatus().isSuccess()) {
                    try {
                        InputStream driveContents = result.getDriveContents().getInputStream();
                        OutputStream outputStream = new FileOutputStream(EasyDiaryDbHelper.getRealmInstance().getPath());
                        IOUtils.copy(driveContents, outputStream);
                        IOUtils.closeQuietly(outputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Context context = GoogleDriveDownloader.this;
                Intent readDiaryIntent = new Intent(context, ReadDiaryActivity.class);
                readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, readDiaryIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        };

}
