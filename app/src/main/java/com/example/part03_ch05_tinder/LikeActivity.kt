package com.example.part03_ch05_tinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.part03_ch05_tinder.databinding.ActivityLikeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction

class LikeActivity : AppCompatActivity() , CardStackListener {

    private lateinit var binding: ActivityLikeBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB:DatabaseReference

    private val adapter = CardItemAdapter()    // CardStackView 어댑터 전역변수 선언
    private val cardItems = mutableListOf<CardItem>()    // 카드 아이템들을 모아둔 리스트 선언

    private val cardViewManager by lazy {
        CardStackLayoutManager(this,this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 모든 등록된 User에 관한 DB
        userDB = Firebase.database.reference.child("Users")

        val currentUserDB = userDB.child(getCurrentUserId())    // 로그인한 User에 대한 DB들을 가지고 온다.
        // DB에서 데이터를 가져오는 법
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            // 로그인 된 유저의 DB에 데이터 변경이 일어났을 경우 -> 이름이 변경되는 경우 and 남이 나를 좋아요 했을 경우
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot : 현재 유저 정보 , 이름이 설정되어 있지 않다면 이름을 입력하는 팝업 호출
                if (snapshot.child("name").value == null) {
                    showNameInputPopUp()
                    return
                }

                // 로그인 유저가 아직 선택하지 않은 데이터들을 불러온다.
                getUnSelectedUsers()
            }

            // 취소된 경우
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        initCardStackView()
        initSignOutBtn()
        initMatchListBtn()

    }

    // CardStackView 리스트 설정
    private fun initCardStackView() {
        binding.cardStackView.layoutManager = cardViewManager
        binding.cardStackView.adapter = adapter
    }

    // 로그아웃 버튼 클릭
    private fun initSignOutBtn() {
        binding.signOutBtn.setOnClickListener {
            auth.signOut()    // 로그 아웃
            startActivity(Intent(this, MainActivity::class.java))    // 메인액티비티 이동 후 현재 액티비티 종료
            finish()
        }
    }

    private fun initMatchListBtn() {
        binding.matchListBtn.setOnClickListener {
            startActivity(Intent(this, MatchedUserActivity::class.java))    // 메인액티비티 이동 후 현재 액티비티 종료
        }
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

        getUnSelectedUsers()

    }


    // 로그인된 User의 ID를 가져오는 메서드
    private fun getCurrentUserId(): String {

        if (auth.currentUser == null) {
            Toast.makeText(this,"로그인이 되어있지 않습니다.",Toast.LENGTH_SHORT).show()
            finish()    // 로그인 실패시 현재 액티비티 종료 후 메인 액티비티로 이동, 이동 시 로그인이 안됐으므로 로그인 액티비티로 이동
        }

        return auth.currentUser?.uid.orEmpty()
    }

    // 로그인 후 닉네임까지 입력하면 tinder 메인 등장
    // 선택하지 않은 유저들을 불러온다.
    private fun getUnSelectedUsers() {
        // Child에 변화가 있을 시 불러오는 Listener 선언
        userDB.addChildEventListener(object : ChildEventListener {
            // 새로운 하위 목록이 생기는 경우
            // DataSnapshot == 전체 DB
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // 새로운 유저가 등록되는 경우 , 초기에 데이터를 불러올 때
                // 지금 보고 있는 ID가 나와 같지 않고 , 상대방의 Like나 disLike에 내가 등록되어 있지 않다면
                // 해당 유저는 내가 한번도 선택한 적이 없는 유저이다.
                if (snapshot.child("userId").value != getCurrentUserId()
                    && snapshot.child("likedBy").child("like").hasChild(getCurrentUserId()).not()
                    && snapshot.child("likedBy").child("disLike").hasChild(getCurrentUserId()).not()) {

                    // 선택한적 없는 유저 정보를 가져온다.
                    val userId = snapshot.child("userId").value.toString()    // 상대 유저 아이디를 불러온다.
                    var name = "undecided"    // 상대 유저가 닉네임을 설정하지 않았을 경우 기본값
                    // 설정해둔 닉네임이 있을 경우 변경
                    if (snapshot.child("name").value != null) {
                        name = snapshot.child("name").value.toString()
                    }

                    cardItems.add(CardItem(userId, name))
                    adapter.submitList(cardItems)
                    adapter.notifyDataSetChanged()

                }
            }

            // 등록된 유저의 정보가 바뀌는 경우
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // cardItem에 있는 ID와 변경된 데이터의 키가 같을 경우
                cardItems.find { it.userId == snapshot.key }?.let {
                    it.name = snapshot.child("name").value.toString()
                }

                adapter.submitList(cardItems)
                adapter.notifyDataSetChanged()

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun Like() {
        // CardStackView는 데이터를 1부터 가져오기 때문에 -1을 해준다.
        val card = cardItems[cardViewManager.topPosition -1 ]
        cardItems.removeFirst()    // 카드에 대한 처리를 했기 때문에 삭제 시킨다.

        // 상대방의 DB에 내가 좋아요를 했다고 저장
        userDB.child(card.userId)
            .child("likedBy")
            .child("like")
            .child(getCurrentUserId())
            .setValue(true)    // 좋아요로 설정한 유저의 likedBy 밑에 like밑에 현재 유저의 value값을 true로 설정

        // TODO 매칭이 된 시점을 봐야한다.
        saveMatchIfOtherUserLikeMe()

        Toast.makeText(this,"${card.name}님을 Like 했습니다.",Toast.LENGTH_SHORT).show()

    }

    private fun DisLike() {
        // CardStackView는 데이터를 1부터 가져오기 때문에 -1을 해준다.
        val card = cardItems[cardViewManager.topPosition -1 ]
        cardItems.removeFirst()    // 카드에 대한 처리를 했기 때문에 삭제 시킨다.

        // 상대방의 DB에 내가 싫어요를 했다고 저장
        userDB.child(card.userId)
            .child("likedBy")
            .child("disLike")
            .child(getCurrentUserId())
            .setValue(true)    // 좋아요로 설정한 유저의 likedBy 밑에 dislike밑에 현재 유저의 value값을 true로 설정


        Toast.makeText(this,"${card.name}님을 disLike 했습니다.",Toast.LENGTH_SHORT).show()
    }

    private fun saveMatchIfOtherUserLikeMe(otherUserId:String) {
        // 자신을 좋아요한 사람들을 가져온다.
        val otherUserDB = userDB.child(getCurrentUserId()).child("likedBy").child("like").child(otherUserId)
        otherUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == true) {
                    userDB.child(getCurrentUserId())
                        .child("likedBy")
                        .child("match")
                        .child(otherUserId)
                        .setValue(true)

                    userDB.child(otherUserId)
                        .child("likedBy")
                        .child("match")
                        .child(getCurrentUserId())
                        .setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    // CardStackView Listener들 설정
   override fun onCardDragging(direction: Direction?, ratio: Float) {
    }

    // 카드를 스와이프 할 때
    override fun onCardSwiped(direction: Direction?) {
        when (direction) {
            Direction.Right -> {
                Like()
            }

            Direction.Left -> {
                DisLike()
            }

            else -> {}

        }
    }

    override fun onCardRewound() {
    }

    override fun onCardCanceled() {
    }

    override fun onCardAppeared(view: View?, position: Int) {
    }

    override fun onCardDisappeared(view: View?, position: Int) {
    }


}