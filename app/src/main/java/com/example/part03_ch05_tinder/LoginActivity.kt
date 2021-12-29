package com.example.part03_ch05_tinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.part03_ch05_tinder.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        callbackManager = CallbackManager.Factory.create()


        initLogInBtn()
        initSignUpBtn()
        initEmailAndPasswordEditText()
        initFacebookLogInBtn()

    }


    // 로그인 버튼 클릭 시 이벤트
    private fun initLogInBtn() {
        binding.loginBtn.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            // firebass에서 이메일과 패스워드를 이용하여 sign in 할 수 있는 기능 이용 가능
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    // sign in이 성공적으로 실행
                    if (task.isSuccessful) {
                        successLogin()    // 로그인 성공
                    } else {
                        Toast.makeText(this,"로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요",Toast.LENGTH_SHORT).show()
                    }

                }

        }
    }

    // 회원가입 버튼 클릭 시 이벤트트
   private fun initSignUpBtn() {
        binding.signUpBtn.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            // firebase에서 이메일과 패스워드로 sign up을 할 수있는 메서드
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,"회원가입에 성공했습니다.로그인 버튼을 눌러 로그인해주세요.",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this,"회원가입에 실패했습니다.",Toast.LENGTH_SHORT).show()
                    }

                }

        }
    }

    private fun initEmailAndPasswordEditText() {

        // EditText에서 입력이 될 때마다 구현한 이벤트 확인
        binding.emailEditText.addTextChangedListener {
            // 두 개의 EditText 값들이 모두 채워진 경우에만 True 반환
            val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
            binding.loginBtn.isEnabled = enable
            binding.signUpBtn.isEnabled = enable
        }

        binding.passwordEditText.addTextChangedListener {
            val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
            binding.loginBtn.isEnabled = enable
            binding.signUpBtn.isEnabled = enable
        }



    }

    // Facebook 로그인 버튼 클릭 시
    private fun initFacebookLogInBtn() {
        // 로그인 버튼 클릭 시 유저에게 받아올 정보들을 선언 , email과 공개 정보들을 가져온다.
        binding.facebookLogInBtn.setPermissions("email" , "public_profile")
        binding.facebookLogInBtn.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            // 성공적인 로그인
            // 로그인 성공시 Access Token을 가져오는데 이를 Firebase로 넘겨줘서 인증해야한다.
           override fun onSuccess(result: LoginResult) {
                // 로그인 access token을 가져오는 과정
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this@LoginActivity) { task ->
                        if (task.isSuccessful) {
                            successLogin()    // 로그인 성공
                        } else {
                            Toast.makeText(this@LoginActivity,"페이스북 로그인이 실패했습니다.",Toast.LENGTH_SHORT).show()
                        }

                    }
            }

            // 로그인하다 취소
            override fun onCancel() {}


            override fun onError(error: FacebookException?) {
                Toast.makeText(this@LoginActivity,"페이스북 로그인이 실패했습니다.",Toast.LENGTH_SHORT).show()
            }

        })
    }


    // 입력된 이메일을 가져오는 메서드
    private fun getInputEmail():String {
        return binding.emailEditText.text.toString()
    }

    // 입력된 비밀번호를 가져오는 메서드
    private fun getInputPassword():String {
        return binding.passwordEditText.text.toString()
    }

    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode , resultCode , data)

    }

    // 로그인 시
    private fun successLogin() {
        // 다시 한번 로그인이 성공적인지 확인
        if (auth.currentUser == null) {
            Toast.makeText(this,"로그인에 실패했습니다.",Toast.LENGTH_SHORT).show()
            return
        }

        // 유저 ID를 가져온다. currentUser가 nullable이기 때문에 null처리도 해주지만 위에서 예외 처리했기 때문에 상관없다.
        val userId = auth.currentUser?.uid.orEmpty()
        // 현재 유저의 DB를 가져오는 변수
        // Firebase의 DB에서 최상위인 reference에서 바로 밑의 "Users"라는 자식을 가져온다. 그 후 등록된 User들 사이에서 로그인 한 user에 대한 정보를 가져온다.
        val currentUserDB = Firebase.database.reference.child("Users").child(userId)
        // 유저에 대한 정보를 저장
        val user = mutableMapOf<String,Any>()
        user["userId"] = userId    // Map은 <Key,Value> 형태로 저장하므로 "userId"에 다가 로그인한 UserID를 저장해준다.
        currentUserDB.updateChildren(user)    // 위의 정보들을 가지는 하위 목록이 추가된다. 즉 Users / (로그인한 ID) / Map형태의 정보들 이 들어가는 것

        finish()

    }


}