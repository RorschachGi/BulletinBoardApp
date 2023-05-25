package com.louro_horo24.bulletinboardapp.dialoghelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.louro_horo24.bulletinboardapp.MainActivity
import com.louro_horo24.bulletinboardapp.R
import com.louro_horo24.bulletinboardapp.accountHelper.AccountHelper
import com.louro_horo24.bulletinboardapp.databinding.SignDialogBinding

//Для работы с диалогами
class DialogHelper(activity: MainActivity) {

    private val act = activity

    //private val rootDialogElement = SignDialogBinding.inflate(act.layoutInflater)

    val accHelper = AccountHelper(act)

    //Диалог для входа или регистрации
    fun createSignDialog(index: Int){
        val builder = AlertDialog.Builder(act)

        val rootDialogElement = SignDialogBinding.inflate(act.layoutInflater)


        builder.setView(rootDialogElement.root)
            .setCancelable(true)

        setDialogState(index, rootDialogElement)

        val dialog = builder.create()

        rootDialogElement.btSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, rootDialogElement, dialog)
        }

        rootDialogElement.btForgetP.setOnClickListener {
            setOnClickResetPassword(rootDialogElement, dialog)
        }

        rootDialogElement.btGoogleSignIn.setOnClickListener {
            accHelper.signInWithGoogle()
            dialog.dismiss()
        }

        dialog.show()
    }

    //Отслеживание статуса диалога: регистрация/авторизация
    private fun setDialogState(index: Int, rootDialogElement: SignDialogBinding){
        if(index == DialogConst.SIGN_UP_STATE){
            rootDialogElement.tvSignTitle.text = act.resources.getString(R.string.ac_sign_up)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sign_up_action)
        }else{
            rootDialogElement.tvSignTitle.text = act.resources.getString(R.string.ac_sign_in)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sign_in_action)
            rootDialogElement.btForgetP.visibility = View.VISIBLE
        }
    }

    //Слушатель нажатия для диалога аутентификации
    private fun setOnClickSignUpIn(index: Int, rootDialogElement: SignDialogBinding, dialog: AlertDialog?){
        dialog?.dismiss()
        if(index == DialogConst.SIGN_UP_STATE){

            accHelper.signUpWithEmail(rootDialogElement.edSignEmail.text.toString(),
                rootDialogElement.edSignPassword.text.toString())

        }else{
            accHelper.signInWithEmail(rootDialogElement.edSignEmail.text.toString(),
                rootDialogElement.edSignPassword.text.toString())
        }
    }

    //Слушатель нажатия кнопки для восстановления пароля
    private fun setOnClickResetPassword(rootDialogElement: SignDialogBinding ,dialog: AlertDialog?){
        if(rootDialogElement.edSignEmail.text.isNotEmpty()){
            act.myAuth.sendPasswordResetEmail(rootDialogElement.edSignEmail.text.toString()).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(act, R.string.email_reset_password_was_sent, Toast.LENGTH_LONG).show()
                }
            }
            dialog?.dismiss()
        }else{
            rootDialogElement.tvDialogMessage.visibility = View.VISIBLE
            rootDialogElement.edSignPassword.visibility = View.GONE
        }
    }

}