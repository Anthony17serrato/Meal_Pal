package edu.fullerton.ecs.cpsc411.mealpal.shared

import androidx.annotation.StringRes
import edu.fullerton.ecs.cpsc411.mealpal.R

enum class HealthLabels(val apiValue: String, @StringRes val resId: Int) {
    AlcoholFree("alcohol-free", R.string.health_alcohol_free),
    AlcoholCocktail("alcohol-cocktail", R.string.health_alcohol_cocktail),
    CeleryFree("celery-free", R.string.health_celery_free),
    CrustaceanFree("crustacean-free", R.string.health_crustacean_free),
    DairyFree("dairy-free", R.string.health_dairy_free),
    DASH("DASH", R.string.health_dash),
    EggFree("egg-free", R.string.health_egg_free),
    FishFree("fish-free", R.string.health_fish_free),
    FodmapFree("fodmap-free", R.string.health_fodmap_free),
    GlutenFree("gluten-free", R.string.health_gluten_free),
    ImmunoSupportive("immuno-supportive", R.string.health_immuno_supportive),
    KetoFriendly("keto-friendly", R.string.health_keto_friendly),
    KidneyFriendly("kidney-friendly", R.string.health_kidney_friendly),
    Kosher("kosher", R.string.health_kosher),
    LowFatAbs("low-fat-abs", R.string.health_low_fat_abs),
    LowPotassium("low-potassium", R.string.health_low_potassium),
    LowSugar("low-sugar", R.string.health_low_sugar),
    LupineFree("lupine-free", R.string.healt_lupine_free),
    Mediterranean("Mediterranean", R.string.health_mediterranean),
    MolluskFree("mollusk-free", R.string.health_mollusk_free),
    MustardFree("mustard-free", R.string.health_mustard_free),
    NoOilAdded("no-oil-added", R.string.health_no_oil_added),
    Paleo("paleo", R.string.health_paleo),
    PeanutFree("peanut-free", R.string.health_peanut_free),
    Pescatarian("pescatarian", R.string.health_pescatarian),
    PorkFree("pork-free", R.string.health_pork_free),
    RedMeatFree("red-meat-free", R.string.health_red_meat_free),
    SesameFree("sesame-free", R.string.health_sesame_free),
    ShellfishFree("shellfish-free", R.string.health_shellfish_free),
    SoyFree("soy-free", R.string.health_soy_free),
    SugarConscious("sugar-conscious", R.string.health_sugar_conscious),
    SulfiteFree("sulfite-free", R.string.health_sulfite_free),
    TreeNutFree("tree-nut-free", R.string.health_tree_nut_free),
    Vegan("vegan", R.string.health_vegan),
    Vegetarian("vegetarian", R.string.health_vegetarian),
    WheatFree("wheat-free", R.string.health_wheat_free)
}