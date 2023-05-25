package com.louro_horo24.bulletinboardapp.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.louro_horo24.bulletinboardapp.databinding.ProgressDialogLayoutBinding
import com.louro_horo24.bulletinboardapp.databinding.SignDialogBinding

object ProgressDialog {

    //Диалог для отображения Прогрессбара
    fun createProgressDialog(act: Activity): AlertDialog{

        val builder = AlertDialog.Builder(act)

        val rootDialogElement = ProgressDialogLayoutBinding.inflate(act.layoutInflater)

        builder.setView(rootDialogElement.root)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCancelable(false)

        dialog.show()

        return dialog
    }

}