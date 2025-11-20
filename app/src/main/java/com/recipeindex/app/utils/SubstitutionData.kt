package com.recipeindex.app.utils

import com.recipeindex.app.data.Substitute
import com.recipeindex.app.data.entities.IngredientSubstitution

/**
 * SubstitutionData - Pre-populated ingredient substitution data
 *
 * Common ingredient substitutions organized by category
 */
object SubstitutionData {

    fun getDefaultSubstitutions(): List<IngredientSubstitution> {
        return listOf(
            // DAIRY
            IngredientSubstitution(
                ingredient = "butter",
                category = "Dairy",
                substitutes = listOf(
                    Substitute("margarine", 1.0, "Use equal amount", "Similar texture and flavor", 9, listOf("dairy-free")),
                    Substitute("coconut oil", 1.0, "Use equal amount", "Best for baking, slight coconut flavor", 8, listOf("vegan", "dairy-free")),
                    Substitute("vegetable oil", 0.75, "Use 3/4 the amount", "For baking only, different texture", 6, listOf("vegan", "dairy-free")),
                    Substitute("applesauce", 0.5, "Use half the amount", "For baking only, reduces fat", 5, listOf("vegan", "dairy-free", "low-fat"))
                ),
                isUserAdded = false
            ),

            IngredientSubstitution(
                ingredient = "milk",
                category = "Dairy",
                substitutes = listOf(
                    Substitute("almond milk", 1.0, "Use equal amount", "Slightly nutty flavor", 9, listOf("vegan", "dairy-free", "lactose-free")),
                    Substitute("oat milk", 1.0, "Use equal amount", "Creamy texture, neutral flavor", 9, listOf("vegan", "dairy-free", "lactose-free")),
                    Substitute("soy milk", 1.0, "Use equal amount", "High protein, neutral flavor", 8, listOf("vegan", "dairy-free", "lactose-free")),
                    Substitute("coconut milk", 1.0, "Use equal amount", "Rich and creamy, coconut flavor", 7, listOf("vegan", "dairy-free", "lactose-free")),
                    Substitute("water plus butter", 1.0, "Use equal water + 1 tbsp butter per cup", "For baking", 6, emptyList())
                ),
                isUserAdded = false
            ),

            IngredientSubstitution(
                ingredient = "heavy cream",
                category = "Dairy",
                substitutes = listOf(
                    Substitute("half and half plus butter", 1.0, "Use 3/4 cup half & half + 1/4 cup melted butter per cup", "Similar richness", 8, emptyList()),
                    Substitute("milk plus butter", 1.0, "Use 3/4 cup milk + 1/3 cup melted butter per cup", "Good for most recipes", 7, emptyList()),
                    Substitute("coconut cream", 1.0, "Use equal amount", "Dairy-free, slight coconut flavor", 8, listOf("vegan", "dairy-free")),
                    Substitute("evaporated milk", 1.0, "Use equal amount", "Thinner consistency", 6, emptyList())
                ),
                isUserAdded = false
            ),

            IngredientSubstitution(
                ingredient = "sour cream",
                category = "Dairy",
                substitutes = listOf(
                    Substitute("greek yogurt", 1.0, "Use equal amount", "Tangy flavor, higher protein", 9, emptyList()),
                    Substitute("plain yogurt", 1.0, "Use equal amount", "Similar tang, thinner", 8, emptyList()),
                    Substitute("cottage cheese blended", 1.0, "Blend until smooth, use equal amount", "High protein", 7, emptyList()),
                    Substitute("coconut cream plus lemon juice", 1.0, "Mix 1 cup coconut cream + 1 tbsp lemon juice", "Dairy-free option", 7, listOf("vegan", "dairy-free"))
                ),
                isUserAdded = false
            ),

            // EGGS
            IngredientSubstitution(
                ingredient = "eggs",
                category = "Baking",
                substitutes = listOf(
                    Substitute("flax egg", 1.0, "1 tbsp ground flax + 3 tbsp water per egg", "Best for baked goods", 8, listOf("vegan")),
                    Substitute("chia egg", 1.0, "1 tbsp ground chia + 3 tbsp water per egg", "Best for baked goods", 8, listOf("vegan")),
                    Substitute("applesauce", 0.25, "1/4 cup per egg", "For moist baked goods", 7, listOf("vegan", "low-fat")),
                    Substitute("mashed banana", 0.25, "1/4 cup per egg", "Adds banana flavor", 6, listOf("vegan")),
                    Substitute("commercial egg replacer", 1.0, "Follow package directions", "Consistent results", 8, listOf("vegan"))
                ),
                isUserAdded = false
            ),

            // BAKING
            IngredientSubstitution(
                ingredient = "all-purpose flour",
                category = "Baking",
                substitutes = listOf(
                    Substitute("bread flour", 1.0, "Use equal amount", "Higher protein, chewier texture", 8, emptyList()),
                    Substitute("cake flour", 1.0, "Add 2 tbsp per cup", "Lighter, more delicate", 7, emptyList()),
                    Substitute("whole wheat flour", 0.5, "Replace up to half", "Denser, nuttier flavor", 6, emptyList()),
                    Substitute("gluten-free flour blend", 1.0, "Use equal amount, may need xanthan gum", "For gluten-free", 7, listOf("gluten-free"))
                ),
                isUserAdded = false
            ),

            IngredientSubstitution(
                ingredient = "baking powder",
                category = "Baking",
                substitutes = listOf(
                    Substitute("baking soda plus cream of tartar", 1.0, "1/4 tsp soda + 1/2 tsp cream of tartar per 1 tsp baking powder", "Exact replacement", 9, emptyList()),
                    Substitute("baking soda plus vinegar", 1.0, "1/4 tsp soda + 1/2 tsp vinegar per 1 tsp baking powder", "Works well", 8, emptyList()),
                    Substitute("baking soda plus lemon juice", 1.0, "1/4 tsp soda + 1/2 tsp lemon juice per 1 tsp baking powder", "Adds slight citrus", 8, emptyList())
                ),
                isUserAdded = false
            ),

            IngredientSubstitution(
                ingredient = "brown sugar",
                category = "Sweeteners",
                substitutes = listOf(
                    Substitute("white sugar plus molasses", 1.0, "1 cup sugar + 1-2 tbsp molasses", "Very close match", 10, emptyList()),
                    Substitute("white sugar", 1.0, "Use equal amount", "Less moisture and flavor", 7, emptyList()),
                    Substitute("coconut sugar", 1.0, "Use equal amount", "Caramel-like flavor", 8, listOf("less-processed")),
                    Substitute("honey", 0.75, "Use 3/4 cup per cup, reduce liquid by 1/4 cup", "Sweeter, different flavor", 6, emptyList())
                ),
                isUserAdded = false
            ),

            IngredientSubstitution(
                ingredient = "white sugar",
                category = "Sweeteners",
                substitutes = listOf(
                    Substitute("honey", 0.75, "Use 3/4 cup per cup, reduce liquid by 1/4 cup", "Sweeter, adds moisture", 7, emptyList()),
                    Substitute("maple syrup", 0.75, "Use 3/4 cup per cup, reduce liquid by 3 tbsp", "Distinct flavor", 7, emptyList()),
                    Substitute("coconut sugar", 1.0, "Use equal amount", "Caramel notes", 8, listOf("less-processed")),
                    Substitute("brown sugar", 1.0, "Use equal amount", "Adds moisture", 8, emptyList())
                ),
                isUserAdded = false
            ),

            // OILS & FATS
            IngredientSubstitution(
                ingredient = "vegetable oil",
                category = "Oils",
                substitutes = listOf(
                    Substitute("canola oil", 1.0, "Use equal amount", "Neutral flavor", 10, emptyList()),
                    Substitute("melted coconut oil", 1.0, "Use equal amount", "Slight coconut flavor", 8, listOf("less-processed")),
                    Substitute("olive oil", 1.0, "Use equal amount", "Stronger flavor", 7, emptyList()),
                    Substitute("applesauce", 0.5, "Use half the amount", "For baking, reduces fat", 6, listOf("low-fat"))
                ),
                isUserAdded = false
            ),

            // VINEGARS
            IngredientSubstitution(
                ingredient = "white vinegar",
                category = "Acids",
                substitutes = listOf(
                    Substitute("apple cider vinegar", 1.0, "Use equal amount", "Slightly fruity", 9, emptyList()),
                    Substitute("lemon juice", 1.0, "Use equal amount", "Fresh citrus flavor", 8, emptyList()),
                    Substitute("lime juice", 1.0, "Use equal amount", "Citrus flavor", 8, emptyList()),
                    Substitute("rice vinegar", 1.0, "Use equal amount", "Milder flavor", 7, emptyList())
                ),
                isUserAdded = false
            ),

            IngredientSubstitution(
                ingredient = "apple cider vinegar",
                category = "Acids",
                substitutes = listOf(
                    Substitute("white vinegar", 1.0, "Use equal amount", "More acidic", 8, emptyList()),
                    Substitute("lemon juice", 1.0, "Use equal amount", "Similar acidity", 9, emptyList()),
                    Substitute("red wine vinegar", 1.0, "Use equal amount", "Stronger flavor", 7, emptyList())
                ),
                isUserAdded = false
            ),

            // FRESH HERBS
            IngredientSubstitution(
                ingredient = "fresh basil",
                category = "Herbs",
                substitutes = listOf(
                    Substitute("dried basil", 0.33, "Use 1 tsp dried per 1 tbsp fresh", "Less aromatic", 8, emptyList()),
                    Substitute("fresh oregano", 0.5, "Use half the amount", "Different but works", 6, emptyList()),
                    Substitute("fresh thyme", 0.5, "Use half the amount", "Different flavor profile", 5, emptyList())
                ),
                isUserAdded = false
            ),

            IngredientSubstitution(
                ingredient = "fresh cilantro",
                category = "Herbs",
                substitutes = listOf(
                    Substitute("fresh parsley", 1.0, "Use equal amount", "Milder flavor", 7, emptyList()),
                    Substitute("fresh basil", 0.5, "Use half the amount", "Different but fresh", 6, emptyList()),
                    Substitute("dried cilantro", 0.33, "Use 1 tsp dried per 1 tbsp fresh", "Less potent", 5, emptyList())
                ),
                isUserAdded = false
            ),

            // SPICES
            IngredientSubstitution(
                ingredient = "cinnamon",
                category = "Spices",
                substitutes = listOf(
                    Substitute("allspice", 0.5, "Use half the amount", "Similar warm notes", 7, emptyList()),
                    Substitute("nutmeg", 0.5, "Use half the amount", "Warm, slightly different", 6, emptyList()),
                    Substitute("cardamom", 0.5, "Use half the amount", "More complex flavor", 6, emptyList())
                ),
                isUserAdded = false
            ),

            // CHOCOLATE
            IngredientSubstitution(
                ingredient = "unsweetened cocoa powder",
                category = "Chocolate",
                substitutes = listOf(
                    Substitute("unsweetened chocolate", 1.0, "3 tbsp cocoa + 1 tbsp fat per 1 oz chocolate", "Richer", 9, emptyList()),
                    Substitute("carob powder", 1.0, "Use equal amount", "Naturally sweeter", 6, listOf("caffeine-free"))
                ),
                isUserAdded = false
            ),

            // THICKENERS
            IngredientSubstitution(
                ingredient = "cornstarch",
                category = "Thickeners",
                substitutes = listOf(
                    Substitute("all-purpose flour", 2.0, "Use 2 tbsp flour per 1 tbsp cornstarch", "Works but cloudier", 8, emptyList()),
                    Substitute("arrowroot powder", 1.0, "Use equal amount", "Clear sauce, gluten-free", 9, listOf("gluten-free")),
                    Substitute("tapioca starch", 1.0, "Use equal amount", "Clear sauce", 8, listOf("gluten-free"))
                ),
                isUserAdded = false
            ),

            // BREAD CRUMBS
            IngredientSubstitution(
                ingredient = "breadcrumbs",
                category = "Baking",
                substitutes = listOf(
                    Substitute("panko", 1.0, "Use equal amount", "Lighter, crispier", 9, emptyList()),
                    Substitute("crushed crackers", 1.0, "Use equal amount", "Similar texture", 8, emptyList()),
                    Substitute("crushed cornflakes", 1.0, "Use equal amount", "Extra crispy", 7, listOf("gluten-free option")),
                    Substitute("oats", 1.0, "Pulse in food processor, use equal amount", "Heartier texture", 6, listOf("gluten-free option"))
                ),
                isUserAdded = false
            ),

            // BUTTERMILK
            IngredientSubstitution(
                ingredient = "buttermilk",
                category = "Dairy",
                substitutes = listOf(
                    Substitute("milk plus lemon juice", 1.0, "1 cup milk + 1 tbsp lemon juice, let sit 5 min", "Best substitute", 10, emptyList()),
                    Substitute("milk plus vinegar", 1.0, "1 cup milk + 1 tbsp vinegar, let sit 5 min", "Very similar", 10, emptyList()),
                    Substitute("plain yogurt", 0.75, "Thin with milk to desired consistency", "Tangier", 8, emptyList()),
                    Substitute("sour cream", 0.75, "Thin with milk to desired consistency", "Rich and tangy", 7, emptyList())
                ),
                isUserAdded = false
            ),

            // MAYONNAISE
            IngredientSubstitution(
                ingredient = "mayonnaise",
                category = "Condiments",
                substitutes = listOf(
                    Substitute("greek yogurt", 1.0, "Use equal amount", "Healthier, tangier", 8, listOf("low-fat")),
                    Substitute("sour cream", 1.0, "Use equal amount", "Similar creaminess", 7, emptyList()),
                    Substitute("avocado mashed", 1.0, "Use equal amount", "Different flavor", 6, listOf("vegan")),
                    Substitute("hummus", 1.0, "Use equal amount", "For sandwiches/wraps", 5, listOf("vegan"))
                ),
                isUserAdded = false
            ),

            // TOMATO PASTE
            IngredientSubstitution(
                ingredient = "tomato paste",
                category = "Condiments",
                substitutes = listOf(
                    Substitute("tomato sauce", 3.0, "Use 3 tbsp sauce per 1 tbsp paste, simmer to reduce", "Thinner", 8, emptyList()),
                    Substitute("ketchup", 2.0, "Use 2 tbsp per 1 tbsp paste", "Sweeter", 6, emptyList()),
                    Substitute("crushed tomatoes", 2.0, "Simmer to reduce", "Less concentrated", 7, emptyList())
                ),
                isUserAdded = false
            )
        )
    }
}
