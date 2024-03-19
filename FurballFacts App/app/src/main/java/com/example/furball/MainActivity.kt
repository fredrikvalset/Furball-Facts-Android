package com.example.furball

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException


private const val TAG: String = "MainActivity"

class MainActivity : Activity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView

    private lateinit var imageRequestURL: String
    private lateinit var textRequestURL: String

    private lateinit var image: String
    private lateinit var text: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.ivImage)
        textView = findViewById(R.id.tvText)
        imageRequestURL = "https://api.thecatapi.com/v1/images/search"
        textRequestURL = "https://cat-fact.herokuapp.com/facts/random?animal_type=cat&amount=50"

        getData()
    }


    fun onClick(view: View) {
        getData()
    }


    fun onShare(view: View) {
        val bitmapUri = getBitmapUri()

        val sendIntent: Intent = Intent(Intent.ACTION_SEND).apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Pawsome Furball Facts")
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_STREAM, bitmapUri)

            type = "image/*"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }


    private fun getData() {
        val requestPicture = JsonArrayRequest(Request.Method.GET, imageRequestURL, null,
            { response ->
                try {
                    val jsonObject = JSONArray(response.toString()).getJSONObject(0)

                    image = jsonObject.getString("url")

                    Picasso.with(this).load(image).resize(
                        Resources.getSystem().displayMetrics.widthPixels,
                        Resources.getSystem().displayMetrics.heightPixels / 2
                    ).centerCrop().into(imageView)

                } catch (error: JSONException) {
                    Log.d(TAG, "msg: Error parsing JSON array: ${error.message}")
                }
            }, { error -> Log.d(TAG, "msg: Error sending API request: ${error.message} ") })


        val requestText = JsonArrayRequest(Request.Method.GET, textRequestURL, null,
            { response ->
                try {
                    val jsonArray = JSONArray(response.toString())

                    for (i in 0 until jsonArray.length()) {

                        val jsonObject = jsonArray.getJSONObject(i)
                        if (jsonObject.getJSONObject("status").getString("verified")
                                .equals("true")
                        ) {
                            text = jsonObject.getString("text")
                        }
                    }
                    textView.text = text
                } catch (error: JSONException) {
                    Log.d(TAG, "msg: Error parsing JSON array: ${error.message}")
                }
            }, { error -> Log.d(TAG, "msg: Error sending API request: ${error.message} ") })

        val queue = Volley.newRequestQueue(this)
        queue.add(requestPicture)
        queue.add(requestText)
    }


    private fun getBitmapUri(): Uri {
        val bitmapDrawable = imageView.drawable as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap
        val bitmapPath =
            MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Purrfect Picture", null)

        return Uri.parse(bitmapPath)
    }

}