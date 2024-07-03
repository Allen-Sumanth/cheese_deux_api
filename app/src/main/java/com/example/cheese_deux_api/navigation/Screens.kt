package com.example.cheesechase.navigation

sealed class Screens(val route: String) {
    data object HomePage: Screens(route = "HomePage")
    data object GamePage: Screens(route = "GamePage")
}