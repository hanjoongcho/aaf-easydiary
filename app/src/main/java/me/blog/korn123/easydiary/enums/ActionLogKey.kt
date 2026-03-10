package me.blog.korn123.easydiary.enums

enum class ActionLogKey {
    DEBUG,   // Detailed information for development and testing
    INFO,    // General user actions and information
    WARN,    // Situations requiring attention (potential issues)
    ERROR,   // Task failures and exceptions
    FATAL    // Severe errors that prevent the app from running
}