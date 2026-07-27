#!/bin/bash

# Exit on any error
set -e

echo "Starting Code Analysis..."

echo "1. Applying Formatting (Spotless & ktlint)..."
./gradlew spotlessApply ktlintFormat

echo "2. Running Spotless Check..."
./gradlew spotlessCheck

echo "3. Running ktlint Check..."
./gradlew ktlintCheck

echo "4. Running Detekt..."
./gradlew detekt

echo "5. Running Tests and Generating JaCoCo Report..."
./gradlew jacocoTestReport

echo "Code Analysis Finished Successfully!"
