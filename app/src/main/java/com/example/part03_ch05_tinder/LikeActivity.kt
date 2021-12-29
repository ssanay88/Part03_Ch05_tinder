package com.example.part03_ch05_tinder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.part03_ch05_tinder.databinding.ActivityLikeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LikeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLikeBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 모든 등록된 User에 관한 DB
        userDB = Firebase.database.reference.child("Users")

        val currentUserDB = userDB.child(getCurrentUserId())    // 로그인한 User에 대한 DB들을 가지고 온다.
        // DB에서 데이터를 가져오는 법
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            // DB에 데이터 변경이 일어났을 경우 -> 이름이 변경되는 경우 and 남이 나를 좋아요 했을 경우
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot : 현재 유저 정보 , 이름이 설정되어 있지 않다면 이름을 입려하는 팝업 호출
                if (snapshot.child("name").value == null) {
                    showNameInputPopUp()
                    return
                }

                // 유저 정보를 갱신
            }

            // 취소된 경우
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    // 이름이 없는 경우 이름을 입력할 팝업을 띄우는 메서드
    private fun showNameInputPopUp() {

        val editTextView = EditText(this)    // 이름을 입력 받을 editText

        AlertDialog.Builder(this)
            .setTitle("이름을 입력해주세요")
            .setView(editTextView)    // View를 하나 설정해줄수있다.
            .setPositiveButton("저장") {_,_ ->
                if (editTextView.text.isEmpty()) {
                    showNameInputPopUp()    // 이름이 빈칸일 경우 다시 반복
                } else {
                    saveUserName(editTextView.text.toString())    // 이름을 저장
                }
            }
            .setCancelable(false)    // 뒤로가기나 팝업 바깥쪽 클릭으로 취소하지 못하도록 설정
            .show()

    }

    // editText에 입력한 이름을 DB에 저장하는 메서드
    private fun saveUserName(name:String) {

        // 유저 ID를 가져온다.
        val userId = getCurrentUserId()
        // 현재 유저의 DB를 가져오는 변수
        // Firebase의 DB에서 최상위인 reference에서 바로 밑의 "Users"라는 자식을 가져온다. 그 후 등록된 User들 사이에서 로그인 한 user에 대한 정보를 가져온다.
        val currentUserDB = userDB.child(userId)
        // 유저에 대한 정보를 저장
        val user = mutableMapOf<String,Any>()
        user["userId"] = userId    // Map은 <Key,Value> 형태로 저장하므로 "userId"에 다가 로그인한 UserID를 저장해준다.
        user["name"] = name    // 입력한 이름을 "name"에다가 저장해준다.
        currentUserDB.updateChildren(user)    // 위의 정보들을 가지는 하위 목록이 추가된다. 즉 Users / (로그인한 ID) / Map형태의 정보들 이 들어가는

    }


    private fun getCurrentUserId(): String {

        if (auth.currentUser == null) {
            Toast.makeText(this,"로그인이 되어있지 않습니다.",Toast.LENGTH_SHORT).show()
            finish()    // 로그인 실패시 현재 액티비티 종료 후 메인 액티비티로 이동, 이동 시 로그인이 안됐으므로 로그인 액티비티로 이동
        }

        return auth.currentUser?.uid.orEmpty()
    }



}