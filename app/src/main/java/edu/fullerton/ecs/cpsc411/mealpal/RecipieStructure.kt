package edu.fullerton.ecs.cpsc411.mealpal

import java.io.Serializable

class RecipieStructure: Serializable {
    var title: String = String()
    var image: String = String()
    var ingredients = ArrayList<String>()
    var url: String = String()
    var calories: Double = 0.0
    var yield: Double = 0.0
    var dietLabels = ArrayList<String>()
    var healthLabels = ArrayList<String>()
    var cautions = ArrayList<String>()
    var saves = 0

}