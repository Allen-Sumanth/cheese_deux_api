package com.example.cheese_deux_api.navigation

sealed class Screens(val route: String) {
    data object HomePage: Screens(route = "HomePage")
    data object GamePage: Screens(route = "GamePage")
}