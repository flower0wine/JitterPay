# AGENTS.md - Development Guidelines for JitterPay

This document provides guidelines for agentic coding agents working on the JitterPay Android project.

## Project Overview

JitterPay is a Kotlin-based Android application using Jetpack Compose for UI. The project follows modern Android architecture patterns and uses Gradle 9.0.0 with version catalogs for dependency management.

## Build Commands

### Gradle Wrapper Commands (Preferred)

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run all unit tests
./gradlew test

# Run a single unit test class
./gradlew test --tests "com.example.jitterpay.ExampleUnitTest"

# Run a single test method
./gradlew test --tests "com.example.jitterpay.ExampleUnitTest.addition_isCorrect"

# Run instrumented tests (on device/emulator)
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint

# Build and run (requires emulator/device)
./gradlew installDebug

# Clean build
./gradlew clean

# View dependency tree
./gradlew app:dependencies

# Debug build info
./gradlew properties
```

### Gradle Daemon Management

```bash
# Stop all Gradle daemons
./gradlew --stop

# Build with no daemon (useful for CI)
./gradlew build --no-daemon
```

## Code Style Guidelines

### Kotlin Conventions

- **Line Length**: Max 120 characters per line
- **Indentation**: 4 spaces (no tabs)
- **Braces**: Opening brace on same line (Kotlin style)
- **Semicolons**: Do not use semicolons

### Naming Conventions

- **Classes/Objects**: PascalCase (e.g., `MainActivity`, `UserRepository`)
- **Functions/Variables**: camelCase (e.g., `getUserData()`, `isLoading`)
- **Constants**: SCREAMING_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)
- **Package Names**: Lowercase, no underscores (e.g., `com.example.jitterpay.ui.theme`)
- **Composable Functions**: PascalCase with noun/verb names (e.g., `Greeting()`, `UserCard()`)

### Import Organization

Organize imports in the following order with blank lines between groups:

```kotlin
package com.example.jitterpay

// Android imports
import android.os.Bundle

// androidx imports
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold

// Kotlin standard library
import java.util.concurrent.TimeUnit

// Project imports (relative to package)
import com.example.jitterpay.ui.theme.JitterPayTheme
```

### Type System

- Use explicit types for public properties and function return types
- Prefer `val` over `var`; use `var` only when reassignment is required
- Use nullable types (`?`) when a value can be null
- Use `Unit` explicitly for functions that don't return a value
- Prefer data classes for simple data holders

### Error Handling

- Use `Result<T>` or sealed classes for operations that can fail
- Handle nullable types with `?.let`, `?:`, or `checkNotNull()`
- Throw specific exceptions with descriptive messages
- Use `runCatching` for exception-based error handling when appropriate

### Composable Functions

- Mark composable functions with `@Composable` annotation
- Place `@Preview` functions at the bottom of the file
- Default modifier should be the last parameter with a default value
- Use `remember` for storing local state in composables
- Prefix preview functions with the component name (e.g., `GreetingPreview()`)

### File Organization

- One public class per file (except for closely related small classes)
- File name should match the primary class/function name
- Place related composables in the same file when they're used together
- Theme files go in `ui/theme/` directory
- Group related UI components in feature-specific packages

### Android Resources

- Resource IDs in `camelCase` (e.g., `button_submit`)
- Layout XML files in `snake_case` (e.g., `activity_main.xml`)
- String resources in `SCREAMING_SNAKE_CASE` for keys (e.g., `BUTTON_SUBMIT`)
- Color resources should follow the pattern defined in `ui/theme/Color.kt`

## Testing Guidelines

### Unit Tests

- Place in `app/src/test/java/...`
- Use JUnit 4 assertions (`assertEquals`, `assertNotNull`, etc.)
- Test file naming: `[ClassName]Test.kt`
- Test method naming: `should_[expected_behavior]_[when_condition]`

### Instrumented Tests

- Place in `app/src/androidTest/java/...`
- Use AndroidX Test annotations
- Run with `./gradlew connectedAndroidTest`

## Architecture

- Follow Clean Architecture principles
- Separate UI (Compose) from business logic
- Use ViewModels for state management
- Keep composables stateless where possible
- Pass dependencies via constructor or DI framework

## Git Workflow

- Create feature branches from `main`
- Use descriptive commit messages: "Add user login functionality"
- Run tests before committing
- Squash small commits when merging PRs

## Notes

- Always use the Gradle wrapper (`./gradlew`) for consistency
- Minimum SDK: 24, Target SDK: 36
- Java compatibility: 11
- Compose compiler version tied to Kotlin 2.0.21
