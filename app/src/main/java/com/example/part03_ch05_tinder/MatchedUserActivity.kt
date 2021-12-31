package com.example.part03_ch05_tinder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.part03_ch05_tinder.databinding.ActivityMatchedUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MatchedUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchedUserBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB: DatabaseReference

    private var adapter = MatchedUserAdapter()
    private val cardItems = mutableListOf<CardItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchedUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDB = Firebase.database.reference.child("Users")

        initMatchedUserRecyclerView()    // 매치된 유저를 보여주는 RecyclerView 설정
        getMatchedUsers()    // 실제로 매치되는 유저들을 DB에서 가져오는 메서드


    }


    private fun initMatchedUserRecyclerView() {
        binding.matchedUserRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.matchedUserRecyclerView.adapter = adapter

    }

    // 나와 매치된 사람들의 정보를 가져오는 메서드
    private fun getMatchedUsers() {
        val matchedDB = userDB.child(getCurrentUserId()).child("likedBy").child("match")

        matchedDB.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.key?.isNotEmpty() == true) {
                    getUserByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }


    private fun getUserByKey(userId:String) {
        userDB.child(userId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cardItems.add(CardItem(userId, snapshot.child("name").value.toString()))
                adapter.submitList(cardItems)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // 로그인된 User의 ID를 가져오는 메서드
    private fun getCurrentUserId(): String {

        if (auth.currentUser == null) {
            Toast.makeText(this,"로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            finish()    // 로그인 실패시 현재 액티비티 종료 후 메인 액티비티로 이동, 이동 시 로그인이 안됐으므로 로그인 액티비티로 이동
        }

        return auth.currentUser?.uid.orEmpty()
    }


}