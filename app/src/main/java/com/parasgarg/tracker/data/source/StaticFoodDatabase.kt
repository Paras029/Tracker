package com.parasgarg.tracker.data.source

import com.parasgarg.tracker.data.model.domain.FoodOption

object StaticFoodDatabase {

    private val foods = listOf(
        FoodOption("s_chicken_breast", "Chicken Breast", 165.0, 31.0, 0.0, 3.6),
        FoodOption("s_egg_whole", "Egg (whole)", 155.0, 13.0, 1.1, 11.0),
        FoodOption("s_egg_white", "Egg White", 52.0, 11.0, 0.7, 0.2),
        FoodOption("s_oats", "Oats", 389.0, 17.0, 66.0, 7.0),
        FoodOption("s_brown_rice", "Brown Rice (cooked)", 112.0, 2.3, 24.0, 0.8),
        FoodOption("s_basmati_rice", "Basmati Rice (cooked)", 130.0, 2.7, 28.0, 0.3),
        FoodOption("s_chapati", "Chapati / Roti", 297.0, 9.0, 53.0, 7.0),
        FoodOption("s_dal", "Dal (cooked)", 116.0, 7.0, 18.0, 2.0),
        FoodOption("s_paneer", "Paneer", 265.0, 18.0, 1.2, 21.0),
        FoodOption("s_tofu", "Tofu (firm)", 76.0, 8.0, 2.0, 4.8),
        FoodOption("s_salmon", "Salmon", 208.0, 20.0, 0.0, 13.0),
        FoodOption("s_tuna_canned", "Tuna (canned in water)", 100.0, 23.0, 0.0, 0.5),
        FoodOption("s_milk", "Milk (full fat)", 61.0, 3.2, 4.8, 3.3),
        FoodOption("s_greek_yogurt", "Greek Yogurt (plain)", 59.0, 10.0, 3.6, 0.4),
        FoodOption("s_banana", "Banana", 89.0, 1.1, 23.0, 0.3),
        FoodOption("s_apple", "Apple", 52.0, 0.3, 14.0, 0.2),
        FoodOption("s_orange", "Orange", 47.0, 0.9, 12.0, 0.1),
        FoodOption("s_broccoli", "Broccoli", 34.0, 2.8, 7.0, 0.4),
        FoodOption("s_spinach", "Spinach", 23.0, 2.9, 3.6, 0.4),
        FoodOption("s_sweet_potato", "Sweet Potato", 86.0, 1.6, 20.0, 0.1),
        FoodOption("s_almonds", "Almonds", 579.0, 21.0, 22.0, 50.0),
        FoodOption("s_peanut_butter", "Peanut Butter", 588.0, 25.0, 20.0, 50.0),
        FoodOption("s_olive_oil", "Olive Oil", 884.0, 0.0, 0.0, 100.0),
        FoodOption("s_white_bread", "White Bread", 265.0, 9.0, 49.0, 3.2),
        FoodOption("s_pasta", "Pasta (cooked)", 131.0, 5.0, 25.0, 1.1),
        FoodOption("s_whey_protein", "Whey Protein Powder", 400.0, 80.0, 7.0, 5.0),
        FoodOption("s_idli", "Idli", 58.0, 1.9, 12.0, 0.1),
        FoodOption("s_dosa", "Dosa (plain)", 168.0, 3.7, 27.0, 4.9),
        FoodOption("s_curd", "Curd / Yogurt (full fat)", 61.0, 3.1, 4.7, 3.3),
        FoodOption("s_mixed_nuts", "Mixed Nuts", 607.0, 18.0, 21.0, 54.0),
    )

    fun search(query: String): List<FoodOption> =
        if (query.isBlank()) emptyList()
        else foods.filter { it.name.contains(query, ignoreCase = true) }

    fun getAll(): List<FoodOption> = foods
}
