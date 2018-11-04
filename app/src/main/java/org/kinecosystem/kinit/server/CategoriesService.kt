package org.kinecosystem.kinit.server

import android.content.Context
import android.util.Log
import org.kinecosystem.kinit.model.earn.Category
import org.kinecosystem.kinit.model.earn.preload
import org.kinecosystem.kinit.repository.CategoriesRepository
import org.kinecosystem.kinit.repository.UserRepository
import org.kinecosystem.kinit.server.api.CategoriesApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriesService(private val context: Context, private val api: CategoriesApi, private val userRepository: UserRepository, private val categoryRepository: CategoriesRepository) {

    fun retrieveCategories(callback: OperationCompletionCallback? = null) {
        api.categories(userRepository.userId()).enqueue(object : Callback<CategoriesApi.CategoriesResponse> {

            override fun onResponse(call: Call<CategoriesApi.CategoriesResponse>, response: Response<CategoriesApi.CategoriesResponse>) {
                if (response != null && response.isSuccessful && response.body() != null) {
                    response.body()?.let {
                        categoryRepository.hasErrors.set(false)
                        categoryRepository.updateCategories(it.categories)
                        //preload images
                        for(category in it.categories){
                            category.preload(context.applicationContext)
                        }
                        categoryRepository.updateHeader(it.headerMessage)
                        callback?.onSuccess()
                    }
                } else {
                    callback?.onError(0)
                    categoryRepository.hasErrors.set(true)
                    Log.d("####", "#### list no  cat")

                }
            }

            override fun onFailure(call: Call<CategoriesApi.CategoriesResponse>, t: Throwable) {
                callback?.onError(-1)
                Log.d("####", "#### list no cat")
            }

        })
    }
//
//    fun retrieveCategories(callback: OperationResultCallback<Pair<List<Category>, HeaderMessage>>) {
//        api.categories(userRepository.userId()).enqueue(object : Callback<CategoriesApi.CategoriesResponse> {
//
//            override fun onResponse(call: Call<CategoriesApi.CategoriesResponse>, response: Response<CategoriesApi.CategoriesResponse>) {
//                if (response != null && response.isSuccessful && response.body() != null) {
//                    val body: CategoriesApi.CategoriesResponse? = response.body()
//                    response.body()?.let {
//                        Log.d("####", "#### list cat" + it.categories)
//                        Log.d("####", "#### list message" + it.headerMessage)
//                        callback.onResult(Pair(it.categories, it.headerMessage))
//
//                    }
//                } else {
//                    callback.onError(0)
//                    Log.d("####", "#### list no  cat")
//
//                }
//            }
//
//            override fun onFailure(call: Call<CategoriesApi.CategoriesResponse>, t: Throwable) {
//                callback.onError(-1)
//                Log.d("####", "#### list no cat")
//            }
//
//        })
//    }
}


