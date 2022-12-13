package edu.fullerton.ecs.cpsc411.mealpal.ui.recipedetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.db.IngredientEntity

// Since this is a static list there is no need for ListAdapter with DiffUtil
class IngredientsAdapter(private val ingredientsList: List<IngredientEntity>) :
    RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)

        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredientsList[position]
        holder.bind(ingredient)
    }

    override fun getItemCount(): Int {
        return ingredientsList.size
    }

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientText: TextView = itemView.findViewById(R.id.recipe_text)
        private val ingredientImage: ImageView = itemView.findViewById(R.id.ingredientImage)

        fun bind(ingredient: IngredientEntity) {
            ingredientText.text = ingredient.text
            ingredient.imageUrl?.let { url ->
                Glide.with(ingredientImage.context).load(url).into(ingredientImage)
            }
        }
    }
}