package edu.fullerton.ecs.cpsc411.mealpal.db

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class Converters {
	private val moshi = Moshi.Builder().build()
	private val listType = Types.newParameterizedType(List::class.java, String::class.java)
	private val listAdapter = moshi.adapter<List<String>>(listType)

	@TypeConverter
	fun restoreList(listOfString: String): List<String> {
		return listAdapter.fromJson(listOfString).orEmpty()
	}

	@TypeConverter
	fun saveList(listOfString: List<String>): String {
		return listAdapter.toJson(listOfString)
	}
}
