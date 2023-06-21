package com.kdt.githubuser.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kdt.githubuser.BuildConfig
import com.kdt.githubuser.model.User
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeViewModel : ViewModel() {

    companion object {
        private val TAG = HomeViewModel::class.java.simpleName
        private const val GITHUB_API = BuildConfig.GithubAPI
    }
    private val listUsernames = MutableLiveData<ArrayList<User>>()
    private lateinit var errorMsg: String

    fun setUsername(username: String, context: Context) {
        val listItems = ArrayList<User>()
        val searchUrl = "https://api.github.com/search/users?q=$username"
        val client = AsyncHttpClient()
        client.addHeader("Authorization", "token $GITHUB_API")
        client.addHeader("User-Agent", "request")
        client.get(searchUrl, object : AsyncHttpResponseHandler(){
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                try {
                    val result = responseBody?.let { String(it) }
                    Log.d(TAG, result!!)
                    val responseObject = JSONObject(result)
                    val items = responseObject.getJSONArray("items")
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val dataAvatar = item.getString("avatar_url")
                        val dataUsername = item.getString("login")
                        val dataId = item.getInt("id")
                        val dataType = item.getString("type")
                        val user = User(dataAvatar, dataUsername, dataId, dataType)
                        listItems.add(user)
                    }
                    listUsernames.postValue(listItems)
                } catch (e: Exception) {
                    Log.d(TAG, e.message.toString())
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                errorMsg = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    else -> "$statusCode : ${error?.message}"
                }
                Log.d(TAG, errorMsg)
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    fun getUsernames() : LiveData<ArrayList<User>> = listUsernames
}