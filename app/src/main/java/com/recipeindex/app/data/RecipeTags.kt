package com.recipeindex.app.data

/**
 * Predefined recipe and meal plan tags
 *
 * Used for hybrid tagging: predefined suggestions + custom user tags
 */
object RecipeTags {

    /**
     * Season tags
     */
    val SEASONS = listOf(
        "Summer",
        "Fall",
        "Winter",
        "Spring"
    )

    /**
     * Main ingredient tags (proteins and key vegetables)
     */
    val INGREDIENTS = listOf(
        // Proteins
        "Chicken",
        "Beef",
        "Pork",
        "Lamb",
        "Turkey",
        "Duck",
        "Fish",
        "Salmon",
        "Tuna",
        "Cod",
        "Shrimp",
        "Scallops",
        "Crab",
        "Lobster",
        "Tofu",
        "Tempeh",
        "Seitan",
        "Eggs",

        // Key vegetables
        "Eggplant",
        "Mushroom",
        "Broccoli",
        "Cauliflower",
        "Zucchini",
        "Squash",
        "Pumpkin",
        "Potato",
        "Sweet Potato",
        "Carrot",
        "Spinach",
        "Kale",

        // Starches
        "Pasta",
        "Rice",
        "Quinoa",
        "Couscous",
        "Polenta",
        "Beans",
        "Lentils"
    )

    /**
     * Special event tags
     */
    val SPECIAL_EVENTS = listOf(
        "Thanksgiving",
        "Christmas",
        "Easter",
        "Birthday",
        "Halloween",
        "Hanukkah",
        "Passover",
        "New Year",
        "Valentine's Day",
        "4th of July",
        "Memorial Day",
        "Labor Day",
        "Super Bowl",
        "Game Day"
    )

    /**
     * Type of dish tags
     */
    val DISH_TYPES = listOf(
        "Breakfast",
        "Lunch",
        "Dinner",
        "Dessert",
        "Appetizer",
        "Side Dish",
        "Salad",
        "Soup",
        "Sandwich",
        "Snack",
        "Drink",
        "Sauce",
        "Condiment"
    )

    /**
     * Cooking method tags
     */
    val COOKING_METHODS = listOf(
        "Baked",
        "Roasted",
        "Grilled",
        "BBQ",
        "Pan-Fry",
        "Stir-Fry",
        "Deep-Fry",
        "Steamed",
        "Boiled",
        "Simmered",
        "Braised",
        "Slow Cooker",
        "Instant Pot",
        "Pressure Cooker",
        "Air Fryer",
        "Microwave",
        "No-Cook",
        "Raw",
        "Casserole",
        "Dutch Oven",
        "Sheet Pan",
        "One Pot Wonder",
        "Stovetop"
    )

    /**
     * Cuisine tags
     */
    val CUISINES = listOf(
        "American",
        "Italian",
        "Mexican",
        "Chinese",
        "Japanese",
        "Thai",
        "Indian",
        "Korean",
        "Vietnamese",
        "French",
        "Greek",
        "Mediterranean",
        "Middle Eastern",
        "Spanish",
        "Caribbean",
        "Brazilian",
        "Moroccan",
        "Ethiopian",
        "German",
        "British",
        "Irish",
        "Southern",
        "Cajun",
        "Tex-Mex",
        "Fusion"
    )

    /**
     * Dietary restriction tags
     */
    val DIETARY = listOf(
        "Vegetarian",
        "Vegan",
        "Gluten-Free",
        "Dairy-Free",
        "Nut-Free",
        "Egg-Free",
        "Soy-Free",
        "Keto",
        "Low-Carb",
        "Paleo",
        "Whole30",
        "Low-Sodium",
        "Low-Fat",
        "High-Protein"
    )

    /**
     * Time/effort tags
     */
    val TIME = listOf(
        "Quick (<30min)",
        "Weeknight",
        "Weekend Project",
        "Make-Ahead",
        "Meal Prep",
        "Freezer-Friendly",
        "Leftovers-Friendly"
    )

    /**
     * All predefined tags grouped by category
     */
    val ALL_CATEGORIES = mapOf(
        "Season" to SEASONS,
        "Ingredient" to INGREDIENTS,
        "Special Event" to SPECIAL_EVENTS,
        "Dish Type" to DISH_TYPES,
        "Cooking Method" to COOKING_METHODS,
        "Cuisine" to CUISINES,
        "Dietary" to DIETARY,
        "Time" to TIME
    )

    /**
     * All predefined tags as a flat list (for searching/filtering)
     */
    val ALL_TAGS = ALL_CATEGORIES.values.flatten()

    /**
     * Get tag category for a given tag
     */
    fun getCategoryForTag(tag: String): String? {
        return ALL_CATEGORIES.entries.firstOrNull { (_, tags) ->
            tags.contains(tag)
        }?.key
    }
}
