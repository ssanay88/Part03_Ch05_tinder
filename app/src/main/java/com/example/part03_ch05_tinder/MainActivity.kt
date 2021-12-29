package com.example.part03_ch05_tinder

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.part03_ch05_tinder.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    override fun onStart() {
        super.onStart()

        // 로그인한 정보가 없을 경우 로그인 액티비티로 이동
        if (auth.currentUser == null) {
            startActivity(Intent(this,LoginActivity::class.java))
        } else {
            // 로그인이 된 상태면 LikeActivity로 이동
            startActivity(Intent(this,LikeActivity::class.java))
        }
    }

}


/*
Firebase Authenetication 사용하기
 - Email Login
 - Facebook Login

Firebase Realtime Database 사용하기

yuyakaido/CardStackView 사용하기

틴더
 - Firebase Authenetication을 통해 이메일 로그인과 페이스북 로그인을 할 수 있음
 - Firebase Realtime Database를 이용하여 기록을 저장하고 , 불러올 수 있음
 - Github에서 Opensource Library를 찾아 사용할 수 있음

 */