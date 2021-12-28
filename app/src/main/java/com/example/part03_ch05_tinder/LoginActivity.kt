package com.example.part03_ch05_tinder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.part03_ch05_tinder.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth


        initLogInBtn()
        initSignUpBtn()
        initEmailAndPasswordEditText()

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
                        finish()    // 액티비티 종료
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
                        Toast.makeText(this,"회원가입에 성공했습니다.로그인 버튼을 눌러 로그인해주세요.",Toast.LENGTH_SHORT).show()
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


    // 입력된 이메일을 가져오는 메서드
    private fun getInputEmail():String {
        return binding.emailEditText.text.toString()
    }

    // 입력된 비밀번호를 가져오는 메서드
    private fun getInputPassword():String {
        return binding.passwordEditText.text.toString()
    }



}