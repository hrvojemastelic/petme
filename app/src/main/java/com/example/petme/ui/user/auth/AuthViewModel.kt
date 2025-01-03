package com.example.petme.ui.user.auth
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petme.session.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> get() = _authState

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _phoneNumbar = MutableLiveData<String>()
    val phoneNumbar: LiveData<String> get() = _phoneNumbar

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fetchUserData(firebaseAuth.currentUser?.uid)
                    this._email.value = email // Store email
                    _authState.value = AuthState.Authenticated
                } else {

                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    private fun fetchUserData(userId: String?) {
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fetchedUsername = document.getString("username")
                        val fetchedEmail = document.getString("email")
                        val fetchedPhoneNumber = document.getString("phoneNumber")
                        val fetchedAddress = document.getString("address")


                        _username.value = fetchedUsername
                        _email.value = fetchedEmail
                        _phoneNumbar.value = fetchedPhoneNumber
                        _address.value = fetchedAddress

                        // Update Singleton
                        UserSession.userId = userId
                        UserSession.username = fetchedUsername
                        UserSession.email = fetchedEmail
                        UserSession.phoneNumber = fetchedPhoneNumber
                        UserSession.address = fetchedAddress
                    }
                }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun checkAuthState() {
        if (firebaseAuth.currentUser != null) {
            fetchUserData(firebaseAuth.currentUser?.uid) // Fetch user data if already logged in
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    sealed class AuthState {
        object Authenticated : AuthState()
        object Unauthenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
