package com.pdg.gesundheitscloudtest

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ExpandableListView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.pdg.gesundheitscloudtest.model.SearchResultItem
import com.pdg.gesundheitscloudtest.viewcontroller.CustomExpandableListAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var resultArray: ArrayList<SearchResultItem>
    private lateinit var mainListView: ExpandableListView

    var TAG = "GHC"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)

        mainListView = findViewById(R.id.main_listview)

        var queryText: String?

        main_searchTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                queryText = text.toString()

                if(queryText.isNullOrBlank()){
                    if(resultArray!=null){
                        resultArray.clear()
                    }
                }else {
                    fetchItemsFromURL(queryText!!)

                    if (resultArray != null) {
                        mainListView.setAdapter(CustomExpandableListAdapter(this@MainActivity, resultArray))
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        
        mainListView.setOnGroupClickListener { expandableListView, view, groupPosition, id ->
            if(expandableListView.isGroupExpanded(groupPosition)){
                val songIntent = Intent(this, SongPlayer::class.java)
                songIntent.putExtra("array", resultArray)
                songIntent.putExtra("selectedSong", groupPosition)
                startActivity(songIntent)
            }
            return@setOnGroupClickListener false
        }
    }

    private fun fetchItemsFromURL(searchString: String) {
        Log.i(TAG, "Fetching from URL")

        resultArray = ArrayList()

        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, "https://itunes.apple.com/search?term=$searchString",
                null, Response.Listener { response ->
                    Log.i(TAG, "onResponse: Response received: " + response.length())
                    //                    Log.i(TAG, "Response received: " + response.toString(4));
                    try {
                        val responseResults = response.getJSONArray("results")
                        for (i in 0 until responseResults.length()) {
                            val searchResultItem = SearchResultItem(
                                responseResults.getJSONObject(i).getString("wrapperType"),
                                responseResults.getJSONObject(i).getString("kind"),
                                responseResults.getJSONObject(i).getInt("artistId"),
                                responseResults.getJSONObject(i).getInt("collectionId"),
                                responseResults.getJSONObject(i).getInt("trackId"),
                                responseResults.getJSONObject(i).getString("artistName"),
                                responseResults.getJSONObject(i).getString("collectionName"),
                                responseResults.getJSONObject(i).getString("trackName"),
                                responseResults.getJSONObject(i).getString("collectionCensoredName"),
                                responseResults.getJSONObject(i).getString("trackCensoredName"),
                                responseResults.getJSONObject(i).getString("artistViewUrl"),
                                responseResults.getJSONObject(i).getString("collectionViewUrl"),
                                responseResults.getJSONObject(i).getString("trackViewUrl"),
                                responseResults.getJSONObject(i).getString("previewUrl"),
                                responseResults.getJSONObject(i).getString("artworkUrl30"),
                                responseResults.getJSONObject(i).getString("artworkUrl60"),
                                responseResults.getJSONObject(i).getString("artworkUrl100"),
                                responseResults.getJSONObject(i).getDouble("collectionPrice").toFloat(),
                                responseResults.getJSONObject(i).getDouble("trackPrice").toFloat(),
                                responseResults.getJSONObject(i).getString("releaseDate"),
                                responseResults.getJSONObject(i).getString("collectionExplicitness"),
                                responseResults.getJSONObject(i).getString("trackExplicitness"),
                                responseResults.getJSONObject(i).getInt("discCount"),
                                responseResults.getJSONObject(i).getInt("discNumber"),
                                responseResults.getJSONObject(i).getInt("trackCount"),
                                responseResults.getJSONObject(i).getInt("trackNumber"),
                                responseResults.getJSONObject(i).getLong("trackTimeMillis"),
                                responseResults.getJSONObject(i).getString("country"),
                                responseResults.getJSONObject(i).getString("currency"),
                                responseResults.getJSONObject(i).getString("primaryGenreName"),
                                responseResults.getJSONObject(i).getBoolean("isStreamable")
                            )

                            resultArray.add(searchResultItem)
                        }
                    } catch (jsonError: JSONException) {
                        Log.e(TAG, "Error parsing JSON: " + jsonError.message)
                        jsonError.printStackTrace()
                    }

                    Log.d(TAG, "onResponse: array size:" + resultArray.size)
                    (mainListView.expandableListAdapter as CustomExpandableListAdapter).notifyDataSetChanged()
                }, Response.ErrorListener { error ->
                    Log.e("TAG", "Error retrieving JSON from URL: " + error.message)
                    error.printStackTrace()
                })

        requestQueue.add(jsonObjectRequest)
        Log.i(TAG, "Request sent.")
    }
}

