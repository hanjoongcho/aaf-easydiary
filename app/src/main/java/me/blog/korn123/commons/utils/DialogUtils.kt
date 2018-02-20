package me.blog.korn123.commons.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.Snackbar
import android.view.View

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object DialogUtils {
    fun makeSnackBar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
    }

    fun showAlertDialog(context: Context,
                        message: String,
                        positiveListener: DialogInterface.OnClickListener,
                        negativeListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        //        builder.setIcon(R.drawable.ic_launcher);
        //        builder.setTitle("일기삭제");
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setNegativeButton("취소", negativeListener)
        builder.setPositiveButton("확인", positiveListener)
        val alert = builder.create()
        alert.show()
    }

    fun showAlertDialog(context: Context,
                        message: String,
                        positiveListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton("확인", positiveListener)
        val alert = builder.create()
        alert.show()
    }

    fun showAlertDialog(context: Context,
                        title: String,
                        message: String,
                        positiveListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        //        builder.setIcon(R.drawable.book);
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton("확인", positiveListener)
        val alert = builder.create()
        alert.show()
    }
}
