@echo off
echo ========================================
echo 🚀 HEALTHBOOK - BUILD & DEPLOY
echo ========================================
echo.

echo 📦 Étape 1: Nettoyage du projet...
call mvn clean

echo.
echo 📦 Étape 2: Construction du JAR...
call mvn package -DskipTests

echo.
if %errorlevel% neq 0 (
    echo ❌ ERREUR: Le build a échoué!
    echo.
    pause
    exit /b 1
)

echo ✅ SUCCÈS: Build terminé avec succès!
echo 📁 Fichier JAR créé: target/health-book-0.0.1-SNAPSHOT.jar
echo.
echo 🎯 Pour démarrer l'application:
echo    java -jar target/health-book-0.0.1-SNAPSHOT.jar
echo.
echo ⏳ Fermeture dans 30 secondes...
timeout /t 30