package com.example.movietrailer.repository.details_page

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.movietrailer.models.detail_model.casts.CastResponse
import com.example.movietrailer.models.detail_model.details.DetailResponse
import com.example.movietrailer.models.detail_model.video.VideoResponse
import com.example.movietrailer.network.ApiClient
import com.example.movietrailer.network.IApi
import com.example.movietrailer.utils.constants.API_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class FilmDetailRepository {

    companion object {

        private const val TAG = "FilmDetailRepository"
        private var INSTANCE: FilmDetailRepository? = null

        fun instance(): FilmDetailRepository? {
            if (INSTANCE == null) {
                INSTANCE = FilmDetailRepository()
            }
            return INSTANCE
        }

    }

    fun getFilmDetailData(
        detail_list: MutableLiveData<DetailResponse>,
        loading: MutableLiveData<Boolean>,
        language: String,
        id: Int
    ): MutableLiveData<DetailResponse>{

        CoroutineScope(IO).launch {

            val api = ApiClient.getInstance().retrofit.create(IApi::class.java)
            val response =
                api.getFilmDetailInformations(id = id, language = language, api_key = API_KEY)
                    .awaitResponse();

            if (response.isSuccessful) {
                loading.postValue(false)
                detail_list.postValue(response.body())
                Log.d(
                    TAG,
                    "getFilmDetailData: detail info successfully comes -> ${response.code()}"
                )
            } else {
                loading.postValue(true)
                Log.d(TAG, "getFilmDetailData: detail is failed -> ${response.code()}")
            }

        }

        return detail_list

    }

    fun getCastsData(
        cast_list: MutableLiveData<CastResponse>,
        loading: MutableLiveData<Boolean>,
        language: String,
        id: Int
    ): MutableLiveData<CastResponse> {

        CoroutineScope(IO).launch {

            val api = ApiClient.getInstance().retrofit.create(IApi::class.java)
            val response =
                api.getFilmCastsInformation(id = id, language = language, api_key = API_KEY)
                    .awaitResponse();

            if (response.isSuccessful) {
                loading.postValue(false)
                cast_list.postValue(response.body())
                Log.d(
                    TAG,
                    "getFilmDetailData: detail info successfully comes -> ${response.code()}"
                )
            } else {
                loading.postValue(true)
                Log.d(TAG, "getFilmDetailData: detail is failed -> ${response.code()}")
            }

        }

        return cast_list

    }

    fun getVideoData(
        video_response: MutableLiveData<VideoResponse>,
        language: String,
        id: Int
    ): MutableLiveData<VideoResponse> {

        CoroutineScope(IO).launch {

            val api = ApiClient.getInstance().retrofit.create(IApi::class.java)
            val response =
                api.getVideoTrailer(id = id, language = language, api_key = API_KEY)
                    .awaitResponse();

            if (response.isSuccessful) {
                video_response.postValue(response.body())
                Log.d(
                    TAG,
                    "getVideoData: video info successfully comes -> ${response.code()}"
                )
            } else {
                Log.d(TAG, "getVideoData: video is failed -> ${response.code()}")
            }

        }

        return video_response

    }



}