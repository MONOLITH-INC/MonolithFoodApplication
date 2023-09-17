package pe.edu.upc.MonolithFoodApplication.controllers;


import pe.edu.upc.MonolithFoodApplication.dto.FoodNutrientDTO;
import pe.edu.upc.MonolithFoodApplication.dto.SearchFoodDTO;
import pe.edu.upc.MonolithFoodApplication.services.FoodService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @GetMapping("/searchByName")
    public ResponseEntity<List<SearchFoodDTO>> searchFoodsByName(@RequestParam String foodName) {
        return new ResponseEntity<List<SearchFoodDTO>>(foodService.searchFoodsByName(foodName), HttpStatus.OK);
    }

    @GetMapping("/searchByCategory")
    public ResponseEntity<List<SearchFoodDTO>> searchFoodsByCategory(@RequestParam String categoryName) {
        return new ResponseEntity<List<SearchFoodDTO>>(foodService.searchFoodsByCategory(categoryName), HttpStatus.OK);
    }

    @GetMapping("/searchByNutrient")
    public ResponseEntity<List<FoodNutrientDTO>> searchFoodsByNutrient(@RequestParam String nutrientName) {
        return new ResponseEntity<List<FoodNutrientDTO>>(foodService.searchFoodsByNutrient(nutrientName), HttpStatus.OK);
    }

    // @GetMapping("/search")
    // public ResponseEntity<String> searchFood(
    //         @RequestParam(name = "name", required = false) String name,
    //         @RequestParam(name = "category", required = false) String category,
    //         @RequestParam(name = "nutrient", required = false) String nutrient) {

    //     // Call the service to search for foods
    //     String searchResultMessage = foodService.searchFoods(name, category, nutrient);

    //     return new ResponseEntity<>(searchResultMessage, HttpStatus.OK);
    // }
    
   
}
