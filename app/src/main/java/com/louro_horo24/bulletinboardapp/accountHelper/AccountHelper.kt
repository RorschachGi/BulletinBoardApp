package com.louro_horo24.bulletinboardapp.accountHelper

import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.louro_horo24.bulletinboardapp.MainActivity
import com.louro_horo24.bulletinboardapp.R
import com.louro_horo24.bulletinboardapp.constance.FirebaseAuthConstance
import com.louro_horo24.bulletinboardapp.dialoghelper.GoogleAccConst

class AccountHelper(activity: MainActivity) {

    private val act = activity

    private lateinit var signInClient: GoogleSignInClient

    /*
        Регистрация по Email/Password
        Проверки:
        1) Email уже зарегестрирован
        2) Некорректный Email
        3) Некорректный пароль
    */
    fun signUpWithEmail(email: String, password: String){

        if(email.isNotEmpty() && password.isNotEmpty()){
            act.myAuth.currentUser?.delete()?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    //createUserWithEmailAndPassword() - создание пользователя по Email/Password
                    //addOnCompleteListener() - возвращает task, проверяем успешность регистрации
                    act.myAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task ->
                        if(task.isSuccessful){
                            signUpWithEmailSuccessful(task.result?.user!!)
                        }else{
                            signUpWithEmailException(task.exception!!, email, password)
                        }
                    }
                }
            }

        }

    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser){
        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun signUpWithEmailException(e: Exception, email: String, password: String){
        if(e is FirebaseAuthUserCollisionException){

            val exception = e as FirebaseAuthUserCollisionException
            if(exception.errorCode == FirebaseAuthConstance.ERROR_EMAIL_ALREADY_IN_USE){
                Toast.makeText(act, FirebaseAuthConstance.ERROR_EMAIL_ALREADY_IN_USE,Toast.LENGTH_LONG).show()
                linkEmailToGoogle(email, password)
            }

        }else if(e is FirebaseAuthInvalidCredentialsException){

            val exception = e as FirebaseAuthInvalidCredentialsException
            if(exception.errorCode == FirebaseAuthConstance.ERROR_INVALID_EMAIL){
                Toast.makeText(act, FirebaseAuthConstance.ERROR_INVALID_EMAIL,Toast.LENGTH_LONG).show()
            }

        }
        if(e is FirebaseAuthWeakPasswordException){

            val exception = e as FirebaseAuthWeakPasswordException
            if(exception.errorCode == FirebaseAuthConstance.ERROR_WEAK_PASSWORD){
                Toast.makeText(act, FirebaseAuthConstance.ERROR_WEAK_PASSWORD,Toast.LENGTH_LONG).show()
            }

        }
    }

    /*Авторизация по Email/Password
      Проверки:
      1) Некорректный Email
      2) Авторизация без проведенной ранее регистрации

     */
    fun signInWithEmail(email: String, password: String){

        if(email.isNotEmpty() && password.isNotEmpty()){
            act.myAuth.currentUser?.delete()?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    act.myAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{task ->
                        if(task.isSuccessful){

                            act.uiUpdate(task.result?.user)

                        }else{
                            signInWithEmailException(task.exception!!, email, password)
                        }
                    }
                }
            }

        }

    }

    private fun signInWithEmailException(e: Exception, email: String, password: String){
        if(e is FirebaseAuthInvalidCredentialsException){

            val exception = e as FirebaseAuthInvalidCredentialsException

            if(exception.errorCode == FirebaseAuthConstance.ERROR_INVALID_EMAIL){

                Toast.makeText(act, FirebaseAuthConstance.ERROR_INVALID_EMAIL,Toast.LENGTH_LONG).show()

            }else if(exception.errorCode == FirebaseAuthConstance.ERROR_WRONG_PASSWORD){

                Toast.makeText(act, FirebaseAuthConstance.ERROR_WRONG_PASSWORD,Toast.LENGTH_LONG).show()

            }
        }else if(e is FirebaseAuthInvalidUserException){

            val exception = e as FirebaseAuthInvalidUserException

            if(exception.errorCode == FirebaseAuthConstance.ERROR_USER_NOT_FOUND){
                Toast.makeText(act, FirebaseAuthConstance.ERROR_USER_NOT_FOUND,Toast.LENGTH_LONG).show()
            }
        }
    }

    /*
        На случай, когда пользователь зарегистрировавшийся по Google-аккаунту попытается пройти
        обычную регистрацию по этой же почте - связываем их.
     */
    private fun linkEmailToGoogle(email: String, password: String){
        val credential = EmailAuthProvider.getCredential(email, password)
        if(act.myAuth?.currentUser != null){
            act.myAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(act, act.resources.getString(R.string.link_done),Toast.LENGTH_LONG).show()
                }
            }
        }else{
            Toast.makeText(act, act.resources.getString(R.string.enter_to_google),Toast.LENGTH_LONG).show()
        }

    }

    //Запрашиваем доступ к Google-аккаунту
    private fun getSignInClient(): GoogleSignInClient{

        //Берем токен
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build()

        return GoogleSignIn.getClient(act, gso)

    }


    fun signInWithGoogle(){
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        act.googleSignInLauncher.launch(intent)
    }

    fun signOutGoogle(){
        getSignInClient().signOut()
    }

    //Входим по google-аккаунту
    fun signInFirebaseWithGoogle(token: String){

        val credential = GoogleAuthProvider.getCredential(token, null)

        //Перед входом удалеяем анонимного пользователя
        act.myAuth.currentUser?.delete()?.addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                act.myAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if(task.isSuccessful){

                        Toast.makeText(act, act.resources.getString(R.string.signIn_success), Toast.LENGTH_LONG).show()
                        act.uiUpdate(task.result?.user)

                    }else{
                        Log.d("MyLog", "Google Sign In Exception : ${task.exception}")
                    }
                }
            }
        }

    }


    /*
        Метод для отправки подтверждающего письма на почту
        FirebaseUser - предоставляет информацию профиля пользователя в базе данных
        пользователей вашего проекта Firebase.
     */
    private fun sendEmailVerification(user: FirebaseUser){
        user.sendEmailVerification().addOnCompleteListener {task ->
            if(task.isSuccessful){
                Toast.makeText(act, act.resources.getString(R.string.send_verification_done),Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(act, act.resources.getString(R.string.send_verification_email_error),Toast.LENGTH_LONG).show()
            }

        }
    }

    //Анонимный вход
    fun signInAnonymously(listetenr: onCompleteListener){
        act.myAuth.signInAnonymously().addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                listetenr.onComplete()
                Toast.makeText(act, "Вы вошли, как гость", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(act, "Не удалось войти, как гость", Toast.LENGTH_LONG).show()
            }
        }
    }

    interface onCompleteListener{
        fun onComplete()
    }

}