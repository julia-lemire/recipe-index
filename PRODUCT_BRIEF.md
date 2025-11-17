# Recipe Index - Product Brief

## Executive Summary

Recipe Index is an Android mobile application designed for home cooks who need a reliable, offline-first solution to store, organize, and plan meals using their personal recipe collection. The app addresses the common problem of recipes disappearing from websites or being scattered across physical books, while providing intelligent meal planning and grocery shopping features.

## Target User

**Primary Persona: The Family Chef**
- Home cook responsible for feeding their family
- Plans 3-4 dinners per week (making leftovers)
- Occasionally plans special event meals (Thanksgiving, holidays) with multiple recipes
- Frustrated by recipes disappearing from websites
- Wants quick access to trusted recipes while cooking
- Needs to adjust recipes based on portion sizes and ingredient availability
- Prefers metric measurements (grams, mL) over volumetric (cups)

## Problem Statement

Home cooks face several challenges:
1. **Recipe Loss**: Favorite recipes disappear when websites go down or change URLs
2. **Fragmentation**: Recipes scattered across bookmarks, cookbooks, handwritten notes, and screenshots
3. **Meal Planning Overhead**: Planning weekly meals and generating shopping lists is time-consuming
4. **Recipe Adaptation**: Scaling portions, converting units, and substituting ingredients requires manual calculation
5. **Cooking Friction**: Referencing recipes while cooking (screen timeout, unit conversions, timers)

## Solution Overview

An Android-native (Kotlin) mobile app that stores recipes locally, extracts recipe data from URLs and photos, enables weekly meal planning, generates grocery lists, and provides a distraction-free cooking mode.

## Core Features

### 1. Recipe Management

#### Recipe Storage & Organization
- **Local-first storage**: All data stored locally on device (no cloud dependency)
- **Recipe index with folders**: Hierarchical organization (e.g., "Desserts/Cookies", "Mains/Pasta")
- **Rich recipe cards**: Store ingredients, instructions, photos, notes, metadata
- **Photo storage**: Multiple photos per recipe (finished dish, process shots)
- **User notes**: Add personal notes, modifications, cooking tips
- **Edit capabilities**: Modify ingredients, quantities, instructions after import

#### Recipe Import
- **URL extraction**: Parse recipe websites and extract structured data (ingredients, instructions, metadata)
- **PDF import**: Extract recipes from PDF files
- **Photo-to-recipe**: OCR and AI extraction from recipe photos
- **Manual entry**: Create recipes from scratch

#### Recipe Discovery & Filtering
- **Advanced filtering**: By tags, ratings, dietary restrictions, cuisine, cook method, season
- **Sorting**: By name, rating, date added, date last cooked, prep time
- **Search**: Full-text search across recipe name, ingredients, instructions, notes
- **Favorites**: Quick access to starred recipes
- **Recipe history**: Track when recipes were last cooked, cooking frequency

### 2. Meal Planning

#### Weekly Planning
- **Week-based organization**: Label meal plans by week (e.g., "Mon Nov 17 - Fri Nov 21")
- **Flexible meal scheduling**: Plan 3-4 dinners typically, with leftover tracking
- **Special event planning**: Multi-recipe meal planning (e.g., Thanksgiving with appetizers, mains, sides, desserts)
- **Drag-and-drop interface**: Easy recipe assignment to days
- **Plan templates**: Save and reuse favorite weekly plans

### 3. Grocery Shopping

#### Smart List Generation
- **Recipe-based lists**: Generate grocery list from:
  - Current week's meal plan
  - Selected recipes (ad-hoc selection)
  - Special event meal plans
- **Ingredient consolidation**: Combine quantities when same ingredient appears in multiple recipes
- **Manual additions**: Add non-recipe items to list
- **Check-off functionality**: Mark items as purchased while shopping

### 4. Recipe Intelligence

#### Portion Scaling
- **Automatic scaling**: Adjust all ingredient quantities by changing portion size (e.g., "serves 4" → "serves 6")
- **Intelligent rounding**: Smart rounding of scaled quantities (e.g., 2.3 eggs → 2 eggs)

#### Unit Conversion
- **Real-time conversion**: Toggle between volumetric (cups, tbsp) and metric (grams, mL)
- **Ingredient-aware**: Conversion accounts for ingredient density (1 cup flour ≠ 1 cup sugar in grams)
- **Display preferences**: User can set preferred unit system

#### Substitution Reference
- **Substitution guide**: Lookup common ingredient substitutions
- **Context-aware suggestions**: Substitutions organized by ingredient category (dairy, baking, proteins)
- **Quick access**: Available from recipe view and cooking mode

#### Nutritional Information
- **Calorie calculation**: Per serving and total recipe
- **Macro breakdown**: Protein, carbs, fat in grams
- **Portion recommendations**: Suggested serving size in grams
- **Dietary labels**: Automatic detection of common dietary patterns (vegan, gluten-free, dairy-free)

#### Recipe Suggestions
- **Ingredient-based recommendations**: Suggest recipes based on ingredients user has on hand
- **User input**: Simple interface to mark available ingredients
- **Match scoring**: Show recipes with highest ingredient match percentage

### 5. Cooking Mode

#### Distraction-Free Interface
- **Keep screen on**: Prevent screen timeout during cooking
- **Large, readable text**: Easy-to-read instructions and ingredient list
- **Step-by-step view**: One instruction at a time, or full view
- **Built-in timers**: Quick-access timers for cooking steps
- **Splatter-proof controls**: Large touch targets, swipe gestures
- **Hands-free scrolling**: Voice-free, gesture-based navigation

### 6. Metadata & Tagging

#### Manual Tags
- **Cuisine type**: Italian, Mexican, Chinese, Indian, etc.
- **Cook method**: Slow cooker, Instant Pot, rice cooker, stovetop, oven, grill, one-pot
- **Season**: Spring, summer, fall, winter
- **Meal type**: Breakfast, lunch, dinner, snack, dessert, appetizer
- **Dietary restrictions**: Vegan, vegetarian, gluten-free, dairy-free, nut-free, low-carb, keto
- **Difficulty**: Easy, medium, hard
- **Time**: Quick (<30 min), medium (30-60 min), long (>60 min)
- **Custom tags**: User-defined tags

#### Automated Tagging (AI/ML)
- **Ingredient analysis**: Auto-detect dietary patterns (vegan, gluten-free)
- **Cuisine detection**: Infer cuisine from ingredients and recipe name
- **Cook method detection**: Identify from instructions (e.g., "add to slow cooker")
- **Time estimation**: Calculate prep + cook time from instructions
- **User review**: Suggest tags for user approval/modification

### 7. Recipe Sharing

#### Samsung Quick Share Integration
- **Share recipe cards**: Export recipe as shareable format
- **Receive recipes**: Import recipes shared by others
- **Photo inclusion**: Include recipe photos in share
- **Metadata preservation**: Maintain tags, ratings, notes in shared recipes

### 8. Recipe Ratings & Feedback

- **5-star rating system**: Rate recipes after cooking
- **Cooking notes**: Add notes after each cooking session
- **Success tracking**: Track successful/failed attempts
- **Rating-based sorting**: Filter by highly-rated recipes

## Technical Requirements

### Platform
- **Android native**: Kotlin, Android SDK
- **Minimum SDK**: Android 8.0 (API 26) for broad compatibility
- **Target SDK**: Latest stable Android version

### Storage
- **Local database**: SQLite or Room database for recipe data
- **File storage**: Local file system for photos
- **No cloud dependency**: All data stored and processed locally
- **Backup options**: Export to external storage, Samsung Quick Share

### Third-Party Integrations
- **Samsung Quick Share SDK**: For recipe sharing
- **OCR/ML Kit**: For photo-to-recipe extraction
- **Recipe parsing**: Web scraping library for URL extraction
- **PDF parsing**: PDF text extraction library

### Performance
- **Fast search**: Sub-second search results for recipe library
- **Smooth scrolling**: 60fps for recipe lists and cooking mode
- **Photo optimization**: Image compression to manage storage
- **Battery efficiency**: Optimize for cooking mode battery consumption

## User Experience Principles

1. **Offline-first**: All features work without internet connection
2. **Fast access**: Maximum 2 taps to reach any frequently-used feature
3. **Cooking-friendly**: Large touch targets, minimal scrolling during cooking
4. **Forgiving**: Easy undo, auto-save, no data loss
5. **Flexible**: Support different cooking styles and planning preferences
6. **Smart defaults**: Sensible defaults with easy customization

## Success Metrics

### Primary Metrics
- **Recipe retention**: Average number of recipes stored per active user
- **Weekly active usage**: Users accessing app at least once per week
- **Meal plan completion**: % of planned meals marked as cooked
- **Cooking mode engagement**: % of recipes viewed in cooking mode

### Secondary Metrics
- **Recipe import success rate**: % of successful URL/PDF/photo imports
- **Shopping list usage**: % of meal plans generating shopping lists
- **Recipe sharing**: Number of recipes shared via Quick Share
- **Tag accuracy**: % of automated tags accepted by users

## Future Considerations

### Phase 2 Features (Post-Launch)
- Calendar integration for meal planning
- Multi-device sync (optional cloud backup)
- Recipe discovery/community features
- Cost tracking per recipe/meal plan
- Meal prep batching suggestions
- More advanced nutritional analysis

### Potential Integrations
- Smart home devices (voice assistants)
- Grocery delivery services
- Connected kitchen appliances

## Development Roadmap

### MVP (Phase 1) - Core Features
1. Recipe storage and manual entry
2. URL import with basic parsing
3. Recipe index with folders
4. Basic search and filtering
5. Weekly meal planning
6. Grocery list generation
7. Portion scaling
8. Cooking mode with timers
9. Ratings and favorites

### Phase 1.5 - Enhanced Import & Intelligence
1. Photo-to-recipe extraction
2. PDF import
3. Unit conversion
4. Nutritional information
5. Automated tagging
6. Substitution guide

### Phase 2 - Sharing & Discovery
1. Samsung Quick Share integration
2. Recipe suggestions based on ingredients
3. Recipe history tracking
4. Dietary restriction filtering
5. Advanced filtering options

## Open Questions

1. **Recipe format standardization**: What structured format for recipe import/export? (e.g., Recipe Schema, JSON-LD)
2. **Photo limits**: Maximum photos per recipe to manage storage?
3. **URL parsing reliability**: Fallback strategy when recipe extraction fails?
4. **Timer limits**: How many simultaneous timers in cooking mode?
5. **Grocery list organization**: Default sort order (alphabetical, by recipe, by category)?
6. **Sharing permissions**: Can shared recipes include user notes, or only original recipe?
7. **Offline ML**: Can photo-to-recipe work offline, or require internet for ML model?

## Competitive Landscape

### Existing Solutions
- **Paprika**: Cross-platform, cloud sync, strong URL parsing (but requires subscription)
- **Cookmate**: Android app with meal planning (limited import options)
- **Cooklist**: Recipe manager with meal planning (no photo extraction)
- **Mela**: iOS-only, excellent UX (not available on Android)

### Differentiation
- **Offline-first**: No cloud dependency, no subscription
- **Photo extraction**: Advanced OCR/ML for recipe photos
- **Cooking mode**: Purpose-built for in-kitchen use
- **Samsung ecosystem**: Native Quick Share integration
- **Free**: No subscription model, one-time purchase or ad-supported

## Appendix

### User Stories

**Recipe Import**
- As a home cook, I want to save recipes from websites so they don't disappear
- As a user, I want to photograph recipes from cookbooks so I have digital copies
- As a cook, I want to import my grandma's handwritten recipe from a photo

**Meal Planning**
- As a busy parent, I want to plan my week's dinners in advance
- As a host, I want to plan multi-recipe holiday meals
- As a cook, I want to generate a shopping list for the week

**Cooking**
- As a cook, I want to keep my screen on while following a recipe
- As a user, I want to set timers without leaving the recipe
- As a cook, I want to convert cups to grams while cooking

**Organization**
- As a user, I want to organize recipes by cuisine and meal type
- As a cook, I want to find vegetarian recipes quickly
- As a user, I want to mark my favorite go-to recipes

**Sharing**
- As a friend, I want to share my best recipes with others
- As a family member, I want to receive recipes from my mom

---

**Document Version**: 1.0
**Last Updated**: November 17, 2025
**Author**: Product Team
**Status**: Draft
